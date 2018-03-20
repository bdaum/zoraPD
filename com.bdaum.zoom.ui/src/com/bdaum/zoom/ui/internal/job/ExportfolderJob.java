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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.TrackRecord_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.job.TransferJob;
import com.bdaum.zoom.operations.internal.AddTrackRecordsOperation;
import com.bdaum.zoom.operations.internal.job.SourceAndTarget;
import com.bdaum.zoom.operations.jobs.AbstractExportJob;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class ExportfolderJob extends AbstractExportJob {

	private static final int[] p3x8 = new int[] { 8, 8, 8 };
	private static final String[] EMPTYSTRINGARRAY = new String[0];
	private File folder;
	private final int target;
	private final FtpAccount acc;
	private List<SourceAndTarget> todo = new ArrayList<SourceAndTarget>();
	private final boolean addToCat;
	private final int cropMode;
	private final boolean addToWatched;
	private final String subfolderOption;
	private SimpleDateFormat df;
	private Set<File> errands;
	private Date now;

	public ExportfolderJob(List<Asset> assets, int mode, int sizing, double scale, int maxSize, UnsharpMask umask,
			int jpegQuality, int cropMode, int target, FtpAccount acc, String folder, String subfolderOption,
			Set<QueryField> xmpFilter, boolean createWatermark, String copyright, int rating, boolean addToCat,
			boolean addToWatched, IAdaptable adaptable) {
		super(Messages.ExportfolderJob_exporting_to_folder, assets, mode, sizing, scale, maxSize, umask, jpegQuality,
				xmpFilter, createWatermark, copyright, rating, adaptable);
		this.cropMode = cropMode;
		this.target = target;
		this.acc = acc;
		this.subfolderOption = subfolderOption;
		this.addToCat = addToCat;
		this.addToWatched = addToWatched;
		this.folder = new File(folder);
		now = new Date();
		String timelinemode = Core.getCore().getDbManager().getMeta(true).getTimeline();
		String mask = null;
		if (Constants.BY_TIMELINE.equals(subfolderOption)) {
			if (Meta_type.timeline_year.equals(timelinemode))
				mask = "/yyyy"; //$NON-NLS-1$
			else if (Meta_type.timeline_month.equals(timelinemode))
				mask = "/yyyy/MMMMMMMMMM"; //$NON-NLS-1$
			else if (Meta_type.timeline_day.equals(timelinemode))
				mask = "/yyyy/MMMMMMMMMM/dd"; //$NON-NLS-1$
			else if (Meta_type.timeline_week.equals(timelinemode))
				mask = "/yyyy/'W'ww"; //$NON-NLS-1$
			else if (Meta_type.timeline_weekAndDay.equals(timelinemode))
				mask = "/yyyy/'W'ww/EEEEEEEEEE"; //$NON-NLS-1$
		} else if (Constants.BY_NUM_TIMELINE.equals(subfolderOption)) {
			if (Meta_type.timeline_year.equals(timelinemode))
				mask = "/yyyy"; //$NON-NLS-1$
			else if (Meta_type.timeline_month.equals(timelinemode))
				mask = "/yyyy/MM"; //$NON-NLS-1$
			else if (Meta_type.timeline_day.equals(timelinemode))
				mask = "/yyyy/MM/dd"; //$NON-NLS-1$
			else if (Meta_type.timeline_week.equals(timelinemode))
				mask = "/yyyy/ww"; //$NON-NLS-1$
			else if (Meta_type.timeline_weekAndDay.equals(timelinemode))
				mask = "/yyyy/ww/EE"; //$NON-NLS-1$
		} else if (Constants.BY_DATE.equals(subfolderOption))
			mask = "yyyy-MM-dd"; //$NON-NLS-1$
		else if (Constants.BY_TIME.equals(subfolderOption))
			mask = "yyyy-MM-dd_HH:mm:ss"; //$NON-NLS-1$
		if (mask != null)
			df = new SimpleDateFormat(mask);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		URI firstExport = null;
		Date now = new Date();
		MultiStatus status = new MultiStatus(UiActivator.PLUGIN_ID, 0, Messages.ExportfolderJob_export_report, null);
		int work = assets.size() + 1;
		monitor.beginTask(Messages.ExportfolderJob_exporting_to_folder, work);
		SubMonitor progress = SubMonitor.convert(monitor, (addToCat ? 1100 : 1000) * work);
		File targetFolder;
		if (target == Constants.FTP)
			try {
				targetFolder = Core.createTempDirectory("FtpTransfer"); //$NON-NLS-1$
			} catch (IOException e) {
				status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
						Messages.ExportfolderJob_error_when_exporting_to_ftp, e));
				return status;
			}
		else {
			folder.mkdirs();
			targetFolder = folder;
		}
		IVolumeManager vm = Core.getCore().getVolumeManager();
		try (Assetbox box = new Assetbox(assets, status, false)) {
			for (File file : box) {
				if (file != null) {
					Asset asset = box.getAsset();
					File subfolder = createSubfolder(targetFolder, asset);
					String path = subfolder.getAbsolutePath();
					if (path.length() > BatchConstants.MAXPATHLENGTH - 13) {
						if (errands == null)
							errands = new HashSet<File>();
						if (errands.add(subfolder)) {
							StringBuilder sb = new StringBuilder();
							sb.append(path, 0, 20).append("...").append(path, path.length() - 20, path.length()); //$NON-NLS-1$
							status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
									NLS.bind(Messages.ExportfolderJob_path_too_long, sb.toString()), null));
						}
						continue;
					}
					File outfile = makeUniqueTargetFile(subfolder, box.getUri(), mode);
					SourceAndTarget sourceAndTarget;
					if (mode == Constants.FORMAT_ORIGINAL) {
						sourceAndTarget = copyImage(status, asset, file, outfile, monitor);
						progress.worked(1000);
					} else
						sourceAndTarget = downScaleImage(status, progress, asset, file, outfile, 1d, cropMode);
					if (sourceAndTarget != null) {
						todo.add(sourceAndTarget);
						if (firstExport == null)
							firstExport = sourceAndTarget.getOutfile().toURI();
					}
				}
				if (monitor.isCanceled()) {
					status.add(new Status(IStatus.WARNING, UiActivator.PLUGIN_ID,
							Messages.ExportfolderJob_export_was_cancelled));
					return status;
				}
			}
		}
		if (target == Constants.FTP) {
			TransferJob transferJob = new TransferJob(targetFolder.listFiles(), acc, true);
			transferJob.schedule();
			if (acc.isTrackExport()) {
				List<TrackRecord> track = new ArrayList<TrackRecord>();
				for (SourceAndTarget sourceAndTarget : todo) {
					String serviceId = "ftp." + acc.getHost(); //$NON-NLS-1$
					TrackRecord record = new TrackRecordImpl(TrackRecord_type.type_ftp, serviceId, serviceId,
							acc.getName(), sourceAndTarget.getOutfile().getName(), new Date(), false, acc.getWebUrl());
					record.setAsset_track_parent(sourceAndTarget.getAsset().getStringId());
				}
				if (!track.isEmpty())
					OperationJob.executeOperation(new AddTrackRecordsOperation(track), adaptable);
			}
		}
		if (addToCat) {
			final IDbManager dbManager = Core.getCore().getDbManager();
			boolean changed = false;
			for (SourceAndTarget sourceAndTarget : todo) {
				Asset asset = sourceAndTarget.getAsset();
				AssetEnsemble ensemble = new AssetEnsemble(dbManager, asset, null);
				AssetImpl newAsset = new AssetImpl();
				AssetEnsemble newEnsemble = new AssetEnsemble(dbManager, newAsset, null);
				ensemble.transferTo(newEnsemble);
				Rectangle targetBounds = sourceAndTarget.getTargetBounds();
				String recipe = Messages.ExportfolderJob_copied;
				if (targetBounds != null) {
					int width = targetBounds.width;
					newAsset.setWidth(width);
					newAsset.setHeight(targetBounds.height);
					if (asset.getImageWidth() > 0) {
						NumberFormat nf = (NumberFormat.getNumberInstance());
						nf.setMaximumFractionDigits(3);
						recipe = NLS.bind(Messages.ExportfolderJob_downsampled,
								nf.format((float) width / asset.getImageWidth()));
					}
					newAsset.setMimeType(ImageConstants.IMAGE_JPEG);
					newAsset.setFileSize(sourceAndTarget.getOutfile().length());
					newAsset.setFormat("JPEG"); //$NON-NLS-1$
					newAsset.setBitsPerSample(p3x8);
					newAsset.setSamplesPerPixel(3);
				}
				newAsset.setAlbum(EMPTYSTRINGARRAY);
				newAsset.setVoiceFileURI(null);
				newAsset.setVoiceVolume(null);
				newAsset.setLastEditor(null);
				File outfile = sourceAndTarget.getOutfile();
				long lastModified = outfile.lastModified();
				newAsset.setLastModification(new Date(lastModified));
				newAsset.setImportDate(now);
				newAsset.setImportedBy(System.getProperty("user.name")); //$NON-NLS-1$
				if (target == Constants.FTP) {
					newAsset.setVolume(null);
					newAsset.setUri(acc.computeTargetURI(outfile));
				} else {
					newAsset.setUri(outfile.toURI().toString());
					newAsset.setVolume(vm.getVolumeForFile(outfile));
				}
				final List<Object> toBeStored = new ArrayList<Object>();
				final Set<Object> toBeDeleted = new HashSet<Object>();
				newEnsemble.store(toBeDeleted, toBeStored);
				toBeStored.add(new DerivedByImpl(recipe, null, null, new Date(lastModified), newAsset.getStringId(),
						asset.getStringId()));
				dbManager.safeTransaction(toBeDeleted, toBeStored);
				changed |= dbManager.createFolderHierarchy(newAsset);
				progress.worked(100);
			}
			ICore core = Core.getCore();
			core.fireAssetsModified(null, null);
			if (changed)
				core.fireStructureModified();
			if (addToWatched && target != Constants.FTP) {
				Meta meta = dbManager.getMeta(true);
				String volume = Core.getCore().getVolumeManager().getVolumeForFile(folder);
				String id = Utilities.computeWatchedFolderId(folder, volume);
				if (!meta.getWatchedFolder().contains(id)) {
					WatchedFolderImpl wf = new WatchedFolderImpl(folder.toURI().toString(), volume, 0L, true, null,
							false, null, false, 0, null, 2, null, null, Constants.FILESOURCE_DIGITAL_CAMERA);
					wf.setStringId(id);
					CoreActivator activator = CoreActivator.getDefault();
					activator.putObservedFolder(wf);
					activator.getFileWatchManager().addImageFolder(wf);
					meta.addWatchedFolder(id);
					dbManager.safeTransaction(null, wf);
				}
			}
		}
		monitor.done();
		OperationJob.signalJobEnd(now.getTime());
		if (target != Constants.FTP && firstExport != null)
			BatchUtilities.showInFolder(new File(firstExport));
		return status;
	}

	private File createSubfolder(File parent, Asset asset) {
		File subfolder = parent;
		if (Constants.BY_CATEGORY.equals(subfolderOption))
			subfolder = createCatSubfolder(parent, asset);
		else if (Constants.BY_TIMELINE.equals(subfolderOption) || Constants.BY_NUM_TIMELINE.equals(subfolderOption))
			subfolder = createTimelineSubfolder(parent, asset);
		else if (Constants.BY_RATING.equals(subfolderOption))
			subfolder = createRatingSubfolder(parent, asset);
		else if (Constants.BY_STATE.equals(subfolderOption))
			subfolder = createGenericSubfolder(parent, asset, QueryField.STATUS);
		else if (Constants.BY_DATE.equals(subfolderOption) || Constants.BY_TIME.equals(subfolderOption))
			subfolder = createDateSubfolder(parent, asset);
		else if (Constants.BY_JOB.equals(subfolderOption))
			subfolder = createGenericSubfolder(parent, asset, QueryField.IPTC_JOBID);
		else if (Constants.BY_EVENT.equals(subfolderOption))
			subfolder = createGenericSubfolder(parent, asset, QueryField.IPTC_EVENT);
		if (subfolder == parent && !Constants.BY_NONE.equals(subfolderOption))
			subfolder = new File(parent, Messages.ExportfolderJob_undefined);
		subfolder.mkdirs();
		return subfolder;
	}

	private File createDateSubfolder(File parent, Asset asset) {
		return new File(parent, df.format(now));
	}

	private static File createCatSubfolder(File parent, Asset asset) {
		File subFolder = parent;
		Object value = QueryField.IPTC_CATEGORY.obtainFieldValue(asset);
		if (value instanceof String && !((String) value).isEmpty()) {
			StringTokenizer st = new StringTokenizer((String) value, "."); //$NON-NLS-1$
			while (st.hasMoreTokens())
				subFolder = new File(subFolder, BatchUtilities.toValidFilename(st.nextToken()));
		}
		return subFolder;
	}

	private File createTimelineSubfolder(File parent, Asset asset) {
		Date dateCreated = null;
		if (df != null) {
			dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
		}
		return dateCreated == null ? parent : new File(parent.getAbsolutePath() + df.format(dateCreated));
	}

	private static File createRatingSubfolder(File parent, Asset asset) {
		Object value = QueryField.RATING.obtainFieldValue(asset);
		if (value instanceof Integer) {
			int rating = (Integer) value;
			if (rating >= 0)
				return new File(parent, rating + Messages.ExportfolderJob_stars);
		}
		return parent;
	}

	private static File createGenericSubfolder(File parent, Asset asset, QueryField qfield) {
		String text = qfield.formatScalarValue(qfield.obtainFieldValue(asset), true, false, null);
		if (text != null && !text.isEmpty())
			return new File(parent, BatchUtilities.toValidFilename(text));
		return parent;
	}

}
