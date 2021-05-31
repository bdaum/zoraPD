/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.video.internal;

import static org.bytedeco.javacpp.avutil.AV_LOG_PANIC;
import static org.bytedeco.javacpp.avutil.av_log_set_level;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.batch.internal.ExifToolSubstitute;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.QueryField.Category;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportException;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.image.IExifLoader;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.operations.internal.AbstractMediaSupport;
import com.bdaum.zoom.operations.internal.AutoRuleOperation;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.video.model.Video;
import com.bdaum.zoom.video.model.VideoImpl;

@SuppressWarnings("restriction")
public class VideoSupport extends AbstractMediaSupport {

	private static final String BAK = ".bak"; //$NON-NLS-1$
	private static final Class<?>[] NOPARMS = new Class[0];
	private static final Object[] NOARGS = new Object[0];

	public static final IFormatter durationFormatter = new Format.FormatAdapter() {

		public String format(double d) {
			return Format.getDecimalFormat(3).format(d) + " s"; //$NON-NLS-1$
		}

		public Object parse(String s) throws ParseException {
			s = s.trim().toLowerCase();
			if (s.endsWith("s")) //$NON-NLS-1$
				s = s.substring(0, s.length() - 1).trim();
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				throw new ParseException(Messages.VideoSupport_bad_duration_value, 0);
			}
		}
	};

	private static final IFormatter frameFormatter = new Format.FormatAdapter() {

		public String format(double d) {
			return Format.getDecimalFormat(2).format(d) + " fps"; //$NON-NLS-1$
		}

		public Object parse(String s) throws ParseException {
			s = s.trim().toLowerCase();
			if (s.endsWith("fps")) //$NON-NLS-1$
				s = s.substring(0, s.length() - 3).trim();
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				throw new ParseException(Messages.VideoSupport_bad_fps, 0);
			}
		}
	};

	private static final IFormatter bitrateFormatter = new Format.FormatAdapter() {

		public String format(int d) {
			if (d > 2000000)
				return Format.getDecimalFormat(3).format(d / 1000000d) + " Mbps"; //$NON-NLS-1$
			return Format.getDecimalFormat(1).format(d / 1000d) + " kbps"; //$NON-NLS-1$
		}

		public Object parse(String s) throws ParseException {
			s = s.trim().toLowerCase();
			double f = 1d;
			if (s.endsWith("mbps")) { //$NON-NLS-1$
				s = s.substring(0, s.length() - 4).trim();
				f = 1000000d;
			} else if (s.endsWith("kbps")) { //$NON-NLS-1$
				s = s.substring(0, s.length() - 4).trim();
				f = 1000d;
			}
			try {
				return (int) (Double.parseDouble(s) * f);
			} catch (NumberFormatException e) {
				throw new ParseException(Messages.VideoSupport_bad_fps, 0);
			}
		}
	};

	// 0x0 = Reserved 0x12 = MPEG-4 generic
	// 0x1 = MPEG-1 Video 0x13 = ISO 14496-1 SL-packetized
	// 0x2 = MPEG-2 Video 0x14 = ISO 13818-6 Synchronized Download Protocol
	// 0x3 = MPEG-1 Audio 0x1b = H.264 Video
	// 0x4 = MPEG-2 Audio 0x80 = DigiCipher II Video
	// 0x5 = ISO 13818-1 private sections 0x81 = A52/AC-3 Audio
	// 0x6 = ISO 13818-1 PES private data 0x82 = HDMV DTS Audio
	// 0x7 = ISO 13522 MHEG 0x83 = LPCM Audio
	// 0x8 = ISO 13818-1 DSM-CC 0x84 = SDDS Audio
	// 0x9 = ISO 13818-1 auxiliary 0x85 = ATSC Program ID
	// 0xa = ISO 13818-6 multi-protocol encap 0x86 = DTS-HD Audio
	// 0xb = ISO 13818-6 DSM-CC U-N msgs 0x87 = E-AC-3 Audio
	// 0xc = ISO 13818-6 stream descriptors 0x8a = DTS Audio
	// 0xd = ISO 13818-6 sections 0x91 = A52b/AC-3 Audio
	// 0xe = ISO 13818-1 auxiliary 0x92 = DVD_SPU vls Subtitle
	// 0xf = MPEG-2 AAC Audio 0x94 = SDDS Audio
	// 0x10 = MPEG-4 Video 0xa0 = MSCODEC Video
	// 0x11 = MPEG-4 LATM AAC Audio 0xea = Private ES (VC-1)

	private static final int[] STREAMTYPEKEYS = new int[] { -1, 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa,
			0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12, 0x13, 0x14, 0x1b, 0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
			0x8a, 0x91, 0x92, 0x94, 0xa0, 0xea };

	private static final String[] STREAMTYPELABELS = new String[] { Messages.VideoSupport_unknown,
			Messages.VideoSupport_reserved, "MPEG-1 Video", //$NON-NLS-1$
			"MPEG-2 Video", //$NON-NLS-1$
			"MPEG-1 Audio", //$NON-NLS-1$
			"MPEG-2 Audio", //$NON-NLS-1$
			"ISO 13818-1 private sections", //$NON-NLS-1$
			"ISO 13818-1 PES private data", //$NON-NLS-1$
			"ISO 13522 MHEG", //$NON-NLS-1$
			"ISO 13818-1 DSM-CC", //$NON-NLS-1$
			"ISO 13818-1 auxiliary", //$NON-NLS-1$
			"ISO 13818-6 multi-protocol encap", //$NON-NLS-1$
			"ISO 13818-6 DSM-CC U-N msgs", //$NON-NLS-1$
			"ISO 13818-6 stream descriptors", //$NON-NLS-1$
			"ISO 13818-6 sections", //$NON-NLS-1$
			"ISO 13818-1 auxiliary", //$NON-NLS-1$
			"MPEG-2 AAC Audio", //$NON-NLS-1$
			"MPEG-4 Video", //$NON-NLS-1$
			"MPEG-4 LATM AAC Audio", //$NON-NLS-1$
			"MPEG-4 generic", //$NON-NLS-1$
			"ISO 14496-1 SL-packetized", //$NON-NLS-1$
			"ISO 13818-6 Synchronized Download Protocol", //$NON-NLS-1$
			"H.264 Video", //$NON-NLS-1$
			"DigiCipher II Video", //$NON-NLS-1$
			"A52/AC-3 Audio", //$NON-NLS-1$
			"HDMV DTS Audio", //$NON-NLS-1$
			"LPCM Audio", //$NON-NLS-1$
			"SDDS Audio", //$NON-NLS-1$
			"ATSC Program ID", //$NON-NLS-1$
			"DTS-HD Audio", //$NON-NLS-1$
			"E-AC-3 Audio", //$NON-NLS-1$
			"DTS Audio", //$NON-NLS-1$
			"A52b/AC-3 Audio", //$NON-NLS-1$
			"DVD_SPU vls Subtitle", //$NON-NLS-1$
			"SDDS Audio", //$NON-NLS-1$
			"MSCODEC Video", //$NON-NLS-1$
			"Private ES (VC-1)" //$NON-NLS-1$
	};

	/*** Video fields ***/

	public static final Category CATEGORY_VIDEO = new Category(Messages.QueryField_cat_video, true, true);

	public static final QueryField VIDEO_ALL = new QueryField(QueryField.ALL, null, null, null, null,
			Messages.QueryField_video, QueryField.ACTION_NONE, VIDEO | QueryField.EDIT_NEVER, CATEGORY_VIDEO,
			QueryField.T_NONE, 1, QueryField.T_NONE, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	// Audio fields
	public static final QueryField VIDEO_AUDIO = new QueryField(VIDEO_ALL, "audio", null, null, null, //$NON-NLS-1$
			Messages.QueryField_audio, QueryField.ACTION_NONE, VIDEO | QueryField.EDIT_NEVER, CATEGORY_VIDEO,
			QueryField.T_NONE, 1, QueryField.T_NONE, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField VIDEO_AUDIOBITRATE = new QueryField(VIDEO_AUDIO, "vx/audioBitrate", //$NON-NLS-1$
			"AudioBitrate", //$NON-NLS-1$
			null, "AudioBitrate", //$NON-NLS-1$
			Messages.QueryField_audio_bitrate, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEINTEGER, 1, QueryField.T_NONE, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getAudioBitrate();
		}
	};
	public static final QueryField VIDEO_AUDIOBITSPERSAMPLE = new QueryField(VIDEO_AUDIO, "vx/audioBitsPerSample", //$NON-NLS-1$
			"AudioBitsPerSample", //$NON-NLS-1$
			null, "AudioBitsPerSample", //$NON-NLS-1$
			Messages.QueryField_audio_bits_per_sample, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEINTEGER, 1, QueryField.T_NONE, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getAudioBitsPerSample();
		}
	};
	public static final QueryField VIDEO_AUDIOCHANNELS = new QueryField(VIDEO_AUDIO, "vx/audioChannels", //$NON-NLS-1$
			"AudioChannels", //$NON-NLS-1$
			null, "AudioChannels", Messages.QueryField_audio_channels, QueryField.ACTION_QUERY, //$NON-NLS-1$
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.ESSENTIAL
					| QueryField.HOVER | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_INTEGER, 1, QueryField.T_NONE,
			new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 }, new String[] { "1 + 1", //$NON-NLS-1$
					"1", "2", //$NON-NLS-1$ //$NON-NLS-2$
					"3", "2/1", "3/1", "2/2", "3/2", //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					"1", "2 max", "3 max", "4 max", "5 max", "6 max" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			null, 0f, ISpellCheckingService.NOSPELLING) {
		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getAudioChannels();
		}
	};

	public static final QueryField VIDEO_AUDIOSAMPLERATE = new QueryField(VIDEO_AUDIO, "vx/audioSampleRate", //$NON-NLS-1$
			"AudioSampleRate", //$NON-NLS-1$
			null, "AudioSampleRate", Messages.QueryField_audio_sample_rate, QueryField.ACTION_QUERY, //$NON-NLS-1$
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.ESSENTIAL
					| QueryField.HOVER | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEINTEGER, 1, QueryField.T_NONE, null, null, null, 0f,
			ISpellCheckingService.NOSPELLING) {
		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getAudioSampleRate();
		}
	};

	public static final QueryField VIDEO_SURROUNDMODE = new QueryField(VIDEO_AUDIO, "vx/surroundMode", "SurroundMode", //$NON-NLS-1$ //$NON-NLS-2$
			null, "SurroundMode", Messages.QueryField_surround_mode, QueryField.ACTION_QUERY, //$NON-NLS-1$
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_DISCRETE | QueryField.ESSENTIAL
					| QueryField.HOVER | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_INTEGER, 1, QueryField.T_NONE, new int[] { -1, 0, 1, 2 },
			new String[] { Messages.VideoSupport_unknown, Messages.QueryField_not_indicated,
					Messages.QueryField_not_dolby_surround, Messages.QueryField_dolby_surround },
			null, 0f, ISpellCheckingService.NOSPELLING) {
		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getSurroundMode();
		}
	};
	public static final QueryField VIDEO_AUDIOSTREAMTYPE = new QueryField(VIDEO_AUDIO, "vx/audioStreamType", //$NON-NLS-1$
			"AudioStreamType", //$NON-NLS-1$
			null, "AudioStreamType", //$NON-NLS-1$
			Messages.QueryField_audio_stream_type, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_DISCRETE | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_INTEGER, 1, QueryField.T_NONE, STREAMTYPEKEYS, STREAMTYPELABELS, null, 0f,
			ISpellCheckingService.NOSPELLING) {
		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getAudioStreamType();
		}
	};

	// Video / General

	public static final QueryField VIDEO_GENERAL = new QueryField(VIDEO_ALL, "general", null, null, null, //$NON-NLS-1$
			Messages.QueryField_general, QueryField.ACTION_NONE, VIDEO | QueryField.EDIT_NEVER, CATEGORY_VIDEO,
			QueryField.T_NONE, 1, QueryField.T_NONE, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField VIDEO_BITDEPTH = new QueryField(VIDEO_GENERAL, "vx/bitDepth", //$NON-NLS-1$
			"BitDepth", //$NON-NLS-1$
			null, "BitDepth", //$NON-NLS-1$
			Messages.QueryField_bit_depth, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEINTEGER, 1, QueryField.T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getBitDepth();
		}
	};
	public static final QueryField VIDEO_VIDEOFRAMERATE = new QueryField(VIDEO_GENERAL, "vx/videoFrameRate", //$NON-NLS-1$
			"VideoFrameRate", //$NON-NLS-1$
			null, "VideoFrameRate", //$NON-NLS-1$
			Messages.QueryField_video_frame_rate, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.ESSENTIAL
					| QueryField.HOVER | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEFLOAT, 1, QueryField.T_NONE, null, null, frameFormatter, 0.01f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? Double.NaN : v.getVideoFrameRate();
		}
	};
	public static final QueryField VIDEO_AVGBITRATE = new QueryField(VIDEO_GENERAL, "vx/avgBitrate", //$NON-NLS-1$
			"AvgBitrate", //$NON-NLS-1$
			null, "AvgBitrate", //$NON-NLS-1$
			Messages.VideoSupport_avgbitrate, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR, CATEGORY_VIDEO,
			QueryField.T_POSITIVEINTEGER, 1, QueryField.T_NONE, null, null, bitrateFormatter, 5f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			if (v != null) {
				int b = v.getAvgBitrate();
				if (b > 0)
					return b;
			}
			return -1;
		}
	};
	public static final QueryField VIDEO_DURATION = new QueryField(VIDEO_GENERAL, "vx/duration", //$NON-NLS-1$
			"Duration", //$NON-NLS-1$
			null, "Duration", //$NON-NLS-1$
			Messages.QueryField_duration, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_LINEAR | QueryField.ESSENTIAL
					| QueryField.HOVER | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_POSITIVEFLOAT, 1, QueryField.T_NONE, durationFormatter, 0.001f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		protected double getDouble(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? Double.NaN : v.getDuration();
		}
	};

	public static final QueryField VIDEO_VIDEOSTREAMTYPE = new QueryField(VIDEO_GENERAL, "vx/videoStreamType", //$NON-NLS-1$
			"VideoStreamType", //$NON-NLS-1$
			null, "VideoStreamType", //$NON-NLS-1$
			Messages.QueryField_video_stream_type, QueryField.ACTION_QUERY,
			VIDEO | QueryField.EDIT_NEVER | QueryField.QUERY | QueryField.AUTO_DISCRETE | QueryField.REPORT,
			CATEGORY_VIDEO, QueryField.T_INTEGER, 1, QueryField.T_NONE, STREAMTYPEKEYS, STREAMTYPELABELS, null, 0f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			Video v = getVx(asset, false);
			return v == null ? -1 : v.getVideoStreamType();
		}
	};

	private static final String VIDEOROLE = "vx/"; //$NON-NLS-1$

	private static final int CONVERTALL = 0;

	private static final int CONVERT = 1;

	private static final int NOCONVERT = 2;

	static {
		QueryField.IMAGE_FILE.setFlag(VIDEO);
		QueryField.SAMPLESPERPIXEL.setFlag(VIDEO);
		QueryField.HEIGHT.setFlag(VIDEO);
		QueryField.WIDTH.setFlag(VIDEO);
		QueryField.EMULSION.setFlag(VIDEO);
		QueryField.ANALOGPROCESSING.setFlag(VIDEO);
		QueryField.TIMEOFDAY.setFlag(VIDEO);
		QueryField.DATE.setFlag(VIDEO);
		QueryField.IMAGE_FINANCE.setFlag(VIDEO);
		QueryField.IMAGE_ZOOM.setFlag(VIDEO);
		QueryField.EXIF_XRES.setFlag(VIDEO);
		QueryField.EXIF_YRES.setFlag(VIDEO);
		QueryField.EXIF_DATETIME.setFlag(VIDEO);
		QueryField.EXIF_MAKE.setFlag(VIDEO);
		QueryField.EXIF_MODEL.setFlag(VIDEO);
		QueryField.EXIF_SOFTWARE.setFlag(VIDEO);
		QueryField.EXIF_DATETIMEORIGINAL.setFlag(VIDEO);
		QueryField.EXIF_GPS.setFlag(VIDEO);
		QueryField.IPTC_ALL.setFlag(VIDEO);
	}

	private ImportState importState;
	private String name, plural, collectionID;
	private boolean convertall;
	private List<File> backups = new ArrayList<File>();
	private Map<String, String> mimeMap;

	public int importFile(StorageObject object, String extension, ImportState importState, IProgressMonitor aMonitor,
			URI remote) throws IOException, DiskFullException {
		if (this.importState != importState) {
			this.importState = importState;
			convertall = false;
			backups.clear();
		}
		int work = IMediaSupport.IMPORTWORKSTEPS;
		final List<Object> toBeStored = new ArrayList<Object>();
		final Set<Object> toBeDeleted = new HashSet<Object>();
		final List<Object> trashed = new ArrayList<Object>();
		List<Asset> deletedAssets = new ArrayList<Asset>();
		StorageObject originalFile = null;
		byte[] oldThumbnail = null;
		int oldFrameNo = 0;
		String oldId = null;
		int icnt = 0;
		String originalFileName = object.getName();
		long lastMod = object.lastModified();

		Date lastModified = new Date(lastMod);
		URI uri = object.toURI();
		boolean fromImportFilter = VideoActivator.getDefault().getImportFilters().get(extension) != null;
		File[] files = null;
		if (importState.transferNeeded()) {
			files = importState.transferFile(originalFile = object, aMonitor);
			if (files == null || !files[0].exists())
				return 0;
			if (importState.skipDuplicates(files[0], originalFileName, lastModified)) {
				files[0].delete();
				return 0;
			}
		} else if (object.isLocal())
			files = new File[] { (File) object.getNativeObject() };
		IDbManager dbManager = CoreActivator.getDefault().getDbManager();
		ImportConfiguration configuration = importState.getConfiguration();
		try {
			AssetEnsemble ensemble = null;
			List<AssetEnsemble> existing = AssetEnsemble.getAllAssets(dbManager,
					remote != null ? remote : files[0].toURI(), importState);
			Asset asset = null;
			if (!importState.transferNeeded()) {
				if (existing != null && !existing.isEmpty()) {
					asset = existing.get(0).getAsset();
					oldId = asset.getStringId();
					oldThumbnail = importState.useOldThumbnail(asset.getJpegThumbnail());
					VideoImpl vx = Utilities.getMediaExtension(asset, VideoImpl.class);
					oldFrameNo = vx == null ? 0 : vx.getFrameNo();
				}
				if (asset != null) {
					if (configuration.conflictPolicy == ImportState.IGNOREALL)
						return 0;
					if (configuration.isSynchronize) {
						ensemble = (existing != null && !existing.isEmpty()) ? existing.remove(0) : null;
						importState.reimport = true;
						importState.canUndo = false;
					} else
						switch (importState.promptForOverride(files[0], asset)) {
						case ImportState.CANCEL:
							aMonitor.setCanceled(true);
							return 0;
						case ImportState.SYNCNEWER:
							if (!isOutDated(existing, lastMod))
								return 0;
							//$FALL-THROUGH$
						case ImportState.SYNC:
							ensemble = (existing != null && !existing.isEmpty()) ? existing.remove(0) : null;
							importState.reimport = true;
							importState.canUndo = false;
							break;
						case ImportState.OVERWRITENEWER:
							if (!isOutDated(existing, lastMod))
								return 0;
							//$FALL-THROUGH$
						case ImportState.OVERWRITE:
							AssetEnsemble.deleteAll(existing, deletedAssets, toBeDeleted, toBeStored);
							break;
						default:
							return 0;
						}
				}
			}
			List<Ghost_typeImpl> ghosts = dbManager.obtainGhostsForFile(remote != null ? remote : files[0].toURI());
			importState.allDeletedGhosts.addAll(ghosts);
			toBeDeleted.addAll(ghosts);
			if (ensemble == null)
				ensemble = new AssetEnsemble(dbManager, importState, oldId);
			asset = importState.resetEnsemble(ensemble, remote != null ? remote : files[0].toURI(), files[0],
					lastModified, originalFileName, importState.importDate);
			aMonitor.worked(1);
			--work;
			// Read Image
			if (asset != null) {
				ZImage image = null;
				File exifFile = files[0];
				IExifLoader etool = fromImportFilter ? new ExifToolSubstitute(files[0])
						: importState.getExifTool(exifFile, 0);
				if (oldThumbnail == null) {
					int twidth = importState.computeThumbnailWidth();
					try {
						image = fromImportFilter
								? ((ExifToolSubstitute) etool).loadThumbnail(twidth, twidth / 4 * 3,
										importState.thumbnailRaster, 0f)
								: decodeAndCaptureFrames(files[0], twidth, importState.thumbnailRaster, 1, null,
										aMonitor);
					} catch (UnsupportedOperationException e) {
						// should not happen
					}
					if (image == null)
						asset = null;
				}
				aMonitor.worked(1);
				--work;
				if (asset != null && createImageEntry(files[0], uri, extension, false, ensemble, image, oldThumbnail,
						oldFrameNo, etool, null, ensemble.xmpTimestamp, importState.importDate, toBeStored, toBeDeleted,
						aMonitor)) {
					AssetEnsemble.deleteAll(existing, deletedAssets, toBeDeleted, toBeStored);
					ensemble.removeFromTrash(trashed);
					ensemble.store(toBeDeleted, toBeStored);
					icnt++;
				}
				aMonitor.worked(1);
				--work;
			}
			if (asset != null) {
				Meta meta = importState.meta;
				meta.setLastSequenceNo(meta.getLastSequenceNo() + 1);
				meta.setLastYearSequenceNo(meta.getLastYearSequenceNo() + 1);
				toBeStored.add(meta);
			}
			List<Asset> assetsToIndex = new ArrayList<Asset>();
			if (asset != null)
				assetsToIndex.add(asset);
			boolean changed = false;
			if (collectionID != null && !collectionID.isEmpty()
					&& !dbManager.exists(SmartCollectionImpl.class, collectionID)) {
				changed = true;
				GroupImpl group = getMediaGroup(dbManager);
				SmartCollectionImpl coll = new SmartCollectionImpl(Messages.VideoSupport_Videos, true, false, false,
						false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
				coll.setStringId(collectionID);
				coll.addCriterion(
						new CriterionImpl(QueryField.MIMETYPE.getKey(), null, "video/", null, QueryField.STARTSWITH, //$NON-NLS-1$
								false));
				coll.addSortCriterion(new SortCriterionImpl(QueryField.NAME.getKey(), null, false));
				coll.setGroup_rootCollection_parent(group.getStringId());
				group.addRootCollection(collectionID);
				toBeStored.add(group);
				Utilities.storeCollection(coll, false, toBeStored);
			}
			importState.storeIntoCatalog(assetsToIndex, deletedAssets, toBeDeleted, toBeStored, trashed,
					importState.importNo);
			if (configuration.rules != null)
				OperationJob.executeSlaveOperation(new AutoRuleOperation(configuration.rules, assetsToIndex, null),
						configuration.info, configuration.silent);
			--work;
			changed |= importState.operation.updateFolderHierarchies(asset, true, configuration.timeline,
					configuration.locations, false);
			removeFromTransferfolder(importState, originalFile, files);
			return (changed) ? -icnt : icnt;
		} catch (ImportException e) {
			return 0;
		} finally {
			aMonitor.worked(work);
		}
	}

	private boolean createImageEntry(final File originalFile, URI uri, String extension, boolean isConverted,
			AssetEnsemble ensemble, ZImage image, byte[] oldThumbnail, int frameNo, IExifLoader tool, Recipe recipe,
			Date xmpTimestamp, Date now, List<Object> toBeStored, Set<Object> toBeDeleted, IProgressMonitor monitor) {
		Asset asset = ensemble.getAsset();
		int assetStatus = asset.getStatus();
		if (assetStatus < Constants.STATE_DEVELOPED)
			assetStatus = Constants.STATE_DEVELOPED;
		ensemble.resetEnsemble(assetStatus);
		ensemble.setAnalogProperties(importState.getAnalogProperties());
		if (!importState.processExifData(ensemble, originalFile, 0))
			return false;
		if ("file".equals(uri.getScheme())) //$NON-NLS-1$
			importState.processXmpSidecars(uri, monitor, ensemble);
		ensemble.cleanUp(now, importState.getTimeshift());
		asset.setContentType(QueryField.CONTENTTYPE_PHOTO);
		Video vx = getVx(asset, false);
		if (vx != null) {
			vx.setFrameNo(frameNo);
			if (vx.getVideoFrameRate() < 8)
				asset.setContentType(QueryField.CONTENTTYPE_SCREENSHOT);
		}
		if (asset.getMimeType() == null || asset.getMimeType().isEmpty()) {
			String mime = mimeMap.get(extension.toUpperCase());
			if (mime != null)
				asset.setMimeType(mime);
		}
		if (oldThumbnail != null) {
			asset.setJpegThumbnail(oldThumbnail);
			return true;
		}
		int angle = importState.getThumbAngle(asset, false);
		if (image != null && angle != 0)
			image.setRotation(angle, 1f, 1f);
		return (image != null) ? importState.writeThumbnail(image, asset, angle) : false;
	}

	/**
	 * Construct a DecodeAndCaptureFrames which reads and captures frames from a
	 * video file.
	 * 
	 * @param twidth
	 * @param frameNo
	 * @param filename
	 *            the name of the media file to read
	 * 
	 * @throws IOException
	 */

	public static ZImage decodeAndCaptureFrames(File file, int twidth, int raster, int frameNo, double[] frameCountBox,
			IProgressMonitor aMonitor) throws IOException, UnsupportedOperationException {
		av_log_set_level(AV_LOG_PANIC);
		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			Java2DFrameConverter converter = new Java2DFrameConverter();
			grabber.start();
			int h = grabber.getImageHeight();
			int w = grabber.getImageWidth();
			double f = (double) twidth / Math.max(w, h);
			int newWidth = (int) (w * f);
			int newHeight = (int) (h * f);
			if (raster > 0) {
				newWidth = (newWidth + (raster / 2)) / raster * raster;
				newHeight = (newHeight + raster / 2) / raster * raster;
			}
			grabber.setImageWidth(newWidth);
			grabber.setImageHeight(newHeight);
			if (frameCountBox != null) {
				frameCountBox[0] = grabber.getLengthInFrames();
				frameCountBox[1] = grabber.getFrameRate();
			}
			grabber.setFrameNumber(frameNo);
			Frame frame = grabber.grabImage();
			return frame != null ? new ZImage(converter.getBufferedImage(frame), file.getAbsolutePath()) : null;
		} catch (Exception e) {
			if (frameNo > 0 && e.getMessage().contains("_seek_")) //$NON-NLS-1$
				throw new UnsupportedOperationException(e.getMessage());
			throw new IOException(NLS.bind(Messages.VideoSupport_taking_snapshot_failed, file), e);
		}
	}

	public void log(int eLevel, String msg, Throwable t) {
		switch (eLevel) {
		case IStatus.ERROR:
			importState.operation.addError(msg, t);
			break;
		case IStatus.WARNING:
			importState.operation.addWarning(msg, t);
			break;
		default:
			importState.operation.addInfo(msg);
			break;
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getPropertyFlags()
	 */
	public int getPropertyFlags() {
		return VIDEO;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getIcon40()
	 */
	public Image getIcon40() {
		return Icons.video40.getImage();
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getFileExtensions()
	 */
	public String[] getFileExtensions() {
		Set<String> keySet = mimeMap.keySet();
		String[] extensions = new String[keySet.size()];
		int i = 0;
		for (String s : keySet)
			extensions[i++] = s.toLowerCase();
		return extensions;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#testProperty(int)
	 */
	public boolean testProperty(int flags) {
		return (flags & VIDEO) == flags;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#transferExtension(com.bdaum
	 * .zoom.cat.model.asset.Asset, com.bdaum.zoom.cat.model.asset.Asset)
	 */
	public void transferExtension(Asset sourceAsset, Asset targetAsset) {
		MediaExtension[] sourceExtensions = sourceAsset.getMediaExtension();
		if (sourceExtensions != null) {
			for (MediaExtension ext : sourceExtensions) {
				if (ext instanceof Video) {
					MediaExtension[] targetExtensions = targetAsset.getMediaExtension();
					MediaExtension[] newExtensions;
					if (targetExtensions == null)
						newExtensions = new MediaExtension[1];
					else {
						for (int i = 0; i < targetExtensions.length; i++)
							if (targetExtensions[i] instanceof Video) {
								transfer((Video) ext, (Video) targetExtensions[i]);
								return;
							}
						newExtensions = new MediaExtension[sourceExtensions.length + 1];
						System.arraycopy(sourceExtensions, 0, newExtensions, 0, sourceExtensions.length);
					}
					Video clone = createVideoInstance();
					transfer((Video) ext, clone);
					clone.setAsset_parent(targetAsset);
					newExtensions[newExtensions.length - 1] = clone;
					targetAsset.setMediaExtension(newExtensions);
					return;
				}
			}
			MediaExtension[] targetExtensions = targetAsset.getMediaExtension();
			for (int i = 0; i < targetExtensions.length; i++) {
				MediaExtension ext = targetExtensions[i];
				if (ext instanceof Video) {
					if (targetExtensions.length == 1) {
						targetAsset.setMediaExtension(null);
						return;
					}
					MediaExtension[] newExtensions = new MediaExtension[targetExtensions.length - 1];
					if (i > 0)
						System.arraycopy(sourceExtensions, 0, newExtensions, 0, i - 1);
					if (i < sourceExtensions.length - 1)
						System.arraycopy(sourceExtensions, i + 1, newExtensions, i, sourceExtensions.length - i - 1);
					targetAsset.setMediaExtension(newExtensions);
					return;
				}
			}
		} else
			targetAsset.setMediaExtension(null);
	}

	private static void transfer(Video vx, Video targetVx) {
		targetVx.setAudioBitrate(vx.getAudioBitrate());
		targetVx.setAudioBitsPerSample(vx.getAudioBitsPerSample());
		targetVx.setAudioChannels(vx.getAudioChannels());
		targetVx.setAudioSampleRate(vx.getAudioSampleRate());
		targetVx.setAudioStreamType(vx.getAudioStreamType());
		targetVx.setBitDepth(vx.getBitDepth());
		targetVx.setDuration(vx.getDuration());
		targetVx.setSurroundMode(vx.getSurroundMode());
		targetVx.setVideoFrameRate(vx.getVideoFrameRate());
		targetVx.setVideoStreamType(vx.getVideoStreamType());
	}

	private static Video getVx(Asset asset, boolean create) {
		MediaExtension[] mediaExtension = asset.getMediaExtension();
		if (mediaExtension != null) {
			for (MediaExtension ext : mediaExtension)
				if (ext instanceof Video)
					return (Video) ext;
			if (create) {
				MediaExtension[] newExtensions = new MediaExtension[mediaExtension.length + 1];
				System.arraycopy(mediaExtension, 0, newExtensions, 0, mediaExtension.length);
				mediaExtension = newExtensions;
				asset.setMediaExtension(mediaExtension);
			}
		} else if (create) {
			mediaExtension = new MediaExtension[1];
			asset.setMediaExtension(mediaExtension);
		}
		if (create) {
			VideoImpl video = createVideoInstance();
			video.setAsset_parent(asset);
			mediaExtension[mediaExtension.length - 1] = video;
			return video;
		}
		return null;
	}

	private static VideoImpl createVideoInstance() {
		return new VideoImpl(-1, -1, -1, -1, -1, -1, Double.NaN, -1, Double.NaN, -1, -1);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#resetExtension(com.bdaum.zoom
	 * .cat.model.asset.Asset)
	 */
	public void resetExtension(Asset asset) {
		MediaExtension[] mediaExtension = asset.getMediaExtension();
		if (mediaExtension != null)
			for (MediaExtension ext : mediaExtension)
				if (ext instanceof Video) {
					Video vx = (Video) ext;
					vx.setAudioBitrate(-1);
					vx.setAudioBitsPerSample(-1);
					vx.setAudioChannels(-1);
					vx.setAudioSampleRate(-1);
					vx.setAudioStreamType(-1);
					vx.setBitDepth(-1);
					vx.setDuration(Double.NaN);
					vx.setSurroundMode(-1);
					vx.setVideoFrameRate(Double.NaN);
					vx.setVideoStreamType(-1);
					return;
				}
	}

	public int promptForConvert(File videoFile) {
		if (convertall)
			return CONVERT;
		boolean multiple = importState.nFiles > 1;
		IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
		if (errorHandler == null)
			return NOCONVERT;
		if (multiple) {
			int ret = errorHandler.showMessageDialog(Messages.VideoSupport_convert_video_file, null,
					NLS.bind(Messages.VideoSupport_convert_to_streaming, videoFile), AcousticMessageDialog.QUESTION,
					new String[] { Messages.VideoSupport_yes_to_all, Messages.VideoSupport_yes,
							Messages.VideoSupport_no },
					1, importState.info);
			switch (ret) {
			case CONVERTALL:
				convertall = true;
				return CONVERT;
			default:
				return ret;
			}
		}
		int ret = errorHandler.showMessageDialog(Messages.VideoSupport_convert_video_file, null,
				NLS.bind(Messages.VideoSupport_convert_to_streaming, videoFile), AcousticMessageDialog.QUESTION,
				new String[] { Messages.VideoSupport_yes, Messages.VideoSupport_no }, 0, importState.info);
		switch (ret) {
		case 0:
			return CONVERT;
		default:
			return NOCONVERT;
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#setFieldValue(com.bdaum.zoom
	 * .core.QueryField, com.bdaum.zoom.cat.model.asset.Asset, java.lang.Object)
	 */
	public boolean setFieldValue(QueryField qfield, Asset asset, Object value) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String key = qfield.getKey();
		if (handles(key)) {
			Video.class.getMethod(QueryField.getSetAccessor(key.substring(VIDEOROLE.length())), qfield.getJavaType())
					.invoke(getVx(asset, true), value);
			return true;
		}
		return false;
	}

	public boolean resetBag(QueryField qfield, Asset asset) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		String key = qfield.getKey();
		if (handles(key)) {
			Video.class.getMethod(QueryField.getSetAccessor(key.substring(VIDEOROLE.length())), qfield.getJavaType())
					.invoke(getVx(asset, true), new Object[] { null });
			return true;
		}
		return false;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getMediaExtension(com.bdaum
	 * .zoom.cat.model.asset.Asset)
	 */
	public MediaExtension getMediaExtension(Asset asset) {
		return getVx(asset, false);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#handles(java.lang.String)
	 */
	public boolean handles(String key) {
		return key.startsWith(VIDEOROLE);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getFieldValue(com.bdaum.zoom
	 * .core.QueryField, com.bdaum.zoom.cat.model.asset.MediaExtension)
	 */
	public Object getFieldValue(QueryField qfield, MediaExtension ext) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return Video.class.getMethod(QueryField.getGetAccessor(qfield.getKey().substring(VIDEOROLE.length())), NOPARMS)
				.invoke(ext, NOARGS);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.IMediaSupport#getExtensionType()
	 */
	public Class<? extends MediaExtension> getExtensionType() {
		return Video.class;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.IMediaSupport#getFieldName(java.lang.String)
	 */
	public String getFieldName(String key) {
		return handles(key) ? key.substring(VIDEOROLE.length()) : null;
	}

	public String getPlural() {
		return plural;
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}

	public StorageObject getMediaFolder(StorageObject file) {
		try {
			while (true) {
				if ("DCIM".equals(file.getName())) //$NON-NLS-1$
					return file;
				if (file.isStorage())
					return file;
				file = file.getParentObject();
			}
		} catch (IOException e) {
			return null;
		}
	}

	public boolean undoImport(Asset asset, Set<Object> toBeDeleted, List<Object> toBeStored) {
		for (File backup : backups)
			if (backup.exists()) {
				String name = backup.getName();
				if (name.endsWith(BAK)) {
					File videoFile = new File(backup.getParent(),
							name = name.substring(0, name.length() - BAK.length()));
					videoFile.delete();
					backup.renameTo(videoFile);
				}
			}
		backups.clear();
		return false;
	}

	@Override
	public void setMimeMap(Map<String, String> mimeMap) {
		this.mimeMap = mimeMap;
	}

	@Override
	public void setCollectionId(String collectionID) {
		this.collectionID = collectionID;
	}

	@Override
	public String getGalleryHoverId() {
		return "com.bdaum.zoom.ui.hover.galleryItem.video"; //$NON-NLS-1$
	}

}
