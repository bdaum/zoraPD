package com.bdaum.zoom.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;

import com.adobe.xmp.XMPException;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedBy;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.operations.AbstractImportOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPField;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class ImportState {

	public static final int CANCEL = -1;
	public static final int OVERWRITE = 0;
	public static final int OVERWRITEALL = 1;
	public static final int OVERWRITENEWER = 2;
	public static final int IGNORE = 3;
	public static final int IGNOREALL = 4;
	public static final int SYNC = 5;
	public static final int SYNCALL = 6;
	public static final int SYNCNEWER = 7;
	public static final int ASK = 8;
	public static final int MCUWidth = 16;

	private static final File[] EMPTYFILES = new File[0];
	private static final int FLASH_VALUE_FIRED = 0x1;
	private static final int FLASH_VALUE_NO_FLASH_FUNCTION = 0x20;
	private static final int RESOLUTION_UNIT_VALUE_CM = 3;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_CM = 3;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_MM = 4;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_UM = 5;

	private final GregorianCalendar cal = new GregorianCalendar();

	public StorageObject skipFile;
	public boolean reimport;
	public AbstractImportOperation operation;
	public int fileSource;
	public Meta meta = Core.getCore().getDbManager().getMeta(true);
	public IAdaptable info;
	public int nFiles;
	public Set<Asset> importedAssets;
	public Set<Asset> allDeletedAssets = new HashSet<>();
	public Set<DerivedBy> allDeletedRelations = new HashSet<>();
	public List<Ghost_typeImpl> allDeletedGhosts = new ArrayList<>();
	public boolean canUndo = true;
	public Map<String, String> overlayMap = new HashMap<String, String>(49);
	public String[] recipeDetectorIds;
	public int importNo = 1;
	public Date importDate = new Date();
	public Set<Asset> added = new HashSet<>();
	public Set<Asset> modified = new HashSet<>();
	public int thumbnailRaster;

	private ImportConfiguration configuration;
	private String volumeLabel;
	private ExifTool exifTool;
	private File exifFile;
	private ImportConfiguration tempConfiguration;
	private boolean changed;
	private Set<String> errorSet = new HashSet<>();
	private ImportFromDeviceData importFromDeviceData;
	private AnalogProperties analogProperties;
	private int sidecarTimeshift = 0;

	public ImportState(ImportConfiguration configuration, ImportFromDeviceData importFromDeviceData,
			AnalogProperties analogProperties, AbstractImportOperation operation, int fileSource) {
		this.configuration = configuration;
		this.importFromDeviceData = importFromDeviceData;
		this.analogProperties = analogProperties;
		this.operation = operation;
		this.fileSource = fileSource;
		this.thumbnailRaster = configuration.useWebP ? 0 : MCUWidth;
		if (analogProperties != null) {
			if (analogProperties.type == Constants.ANALOGTYPE_NEGATIVE
					|| analogProperties.type == Constants.ANALOGTYPE_TRANSPARENCY)
				this.fileSource = Constants.FILESOURCE_FILMSCANNER;
			if (analogProperties.type == Constants.ANALOGTYPE_REFLECTIVE)
				this.fileSource = Constants.FILESOURCE_REFLECTIVE_SCANNER;
		}
	}

	public int getExecuteProfile() {
		int profile = IProfiledOperation.CONTENT | IProfiledOperation.SYNCHRONIZE;
		if (getConfiguration().rawOptions.equals(Constants.RAWIMPORT_BOTH) || transferNeeded())
			profile |= IProfiledOperation.FILE;
		return profile;
	}

	public int getUndoProfile() {
		int profile = IProfiledOperation.CONTENT | IProfiledOperation.INDEX;
		if (getConfiguration().rawOptions.equals(Constants.RAWIMPORT_BOTH) || transferNeeded())
			profile |= IProfiledOperation.FILE;
		return profile;
	}

	public ExifTool getExifTool(File file, int fast) {
		if (exifTool == null)
			exifTool = new ExifTool(file, false);
		else if (!file.equals(exifFile))
			exifTool.reset(file);
		exifTool.setFast(fast);
		exifFile = file;
		return exifTool;
	}

	public boolean processExifData(AssetEnsemble ensemble, File file, int fast) {
		ExifTool tool = getExifTool(file, fast);
		Map<String, String> metaData = tool.getMetadata();
		if (metaData.isEmpty())
			return false;
		Asset asset = ensemble.getAsset();
		String lensInfo = metaData.get("LensInfo"); //$NON-NLS-1$
		asset.setNoLensInfo(lensInfo != null && lensInfo.indexOf("undef") >= 0); //$NON-NLS-1$
		Set<String> makerNotes = getConfiguration().makerNotes ? tool.getMakerNotes() : null;
		for (Map.Entry<String, String> entry : metaData.entrySet()) {
			String exifKey = entry.getKey();
			if (!overlayMap.containsKey(exifKey)) {
				QueryField qfield = QueryField.findExifProperty(exifKey);
				if (qfield != null)
					setProperty(ensemble, qfield, entry.getValue(), file);
			}
		}
		for (Map.Entry<String, String> entry : overlayMap.entrySet())
			setProperty(ensemble, QueryField.findExifProperty(entry.getKey()), entry.getValue(), file);
		if (makerNotes != null) {
			List<String> notes = new ArrayList<String>(makerNotes.size());
			for (String key : makerNotes)
				if (QueryField.findExifProperty(key) == null) {
					String value = metaData.get(key);
					if (!value.startsWith("<")) //$NON-NLS-1$
						notes.add(new StringBuilder().append(key).append(": ").append(value).toString()); //$NON-NLS-1$
				}
			String[] n = notes.toArray(new String[notes.size()]);
			Arrays.sort(n);
			asset.setMakerNotes(n);
		}
		String flash = metaData.get("Flash"); //$NON-NLS-1$
		if (flash != null)
			try {
				int v = BatchUtilities.parseInt(flash);
				asset.setFlashFired((v & FLASH_VALUE_FIRED) != 0);
				asset.setReturnLightDetected(((v & 6) / 2));
				asset.setFlashAuto(((v & 24) / 8));
				asset.setFlashFunction((v & FLASH_VALUE_NO_FLASH_FUNCTION) != 0);
			} catch (NumberFormatException e) {
				// ignore
			}
		String units = metaData.get("ResolutionUnit"); //$NON-NLS-1$
		if (units != null)
			try {
				double f = BatchUtilities.parseInt(units) == RESOLUTION_UNIT_VALUE_CM ? 2.54d : 1d;
				asset.setXResolution(asset.getXResolution() * f);
				asset.setYResolution(asset.getYResolution() * f);
			} catch (NumberFormatException e) {
				// ignore
			}
		units = metaData.get("FocalPlaneResolutionUnit"); //$NON-NLS-1$
		if (units != null)
			try {
				double f = 1d;
				switch (BatchUtilities.parseInt(units)) {
				case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_CM:
					f = 2.54d;
					break;
				case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_MM:
					f = 25.4d;
					break;
				case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_UM:
					f = 25400d;
					break;
				}
				asset.setFocalPlaneXResolution(asset.getFocalPlaneXResolution() * f);
				asset.setFocalPlaneYResolution(asset.getFocalPlaneYResolution() * f);
			} catch (NumberFormatException e) {
				// ignore
			}
		if (!Double.isNaN(asset.getGPSDestDistance())) {
			String distref = metaData.get("GPSDestDistanceRef"); //$NON-NLS-1$
			if (distref != null)
				convertDestDist(asset, distref);
		}
		if (asset.getWidth() <= 0 || asset.getHeight() <= 0) {
			String size = metaData.get("ImageSize"); //$NON-NLS-1$
			if (size != null) {
				int p = size.indexOf('x');
				if (p > 0) {
					try {
						asset.setWidth(BatchUtilities.parseInt(size.substring(0, p)));
						asset.setHeight(BatchUtilities.parseInt(size.substring(p + 1)));
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
		}
		String whiteBalance = asset.getWhiteBalance();
		if ("0".equals(whiteBalance)) //$NON-NLS-1$
			asset.setWhiteBalance("Auto"); //$NON-NLS-1$
		else if ("1".equals(whiteBalance)) //$NON-NLS-1$
			asset.setWhiteBalance("Manual"); //$NON-NLS-1$
		if (asset.getColorSpace() == 65535 && "R03".equals(metaData.get("InteropIndex"))) //$NON-NLS-1$//$NON-NLS-2$
			asset.setColorSpace(2);
		int[] isoSpeedRatings = asset.getIsoSpeedRatings();
		asset.setScalarSpeedRatings(
				isoSpeedRatings != null && isoSpeedRatings.length > 0 ? isoSpeedRatings[0] : CANCEL);
		if (importFromDeviceData != null) {
			String artist = importFromDeviceData.getArtist();
			if (artist != null && !artist.isEmpty())
				asset.setArtist(Utilities.addToStringArray(artist, asset.getArtist(), true));
			if (asset.getEvent() == null || asset.getEvent().isEmpty())
				asset.setEvent(importFromDeviceData.getEvent());
			String[] kw = importFromDeviceData.getKeywords();
			if (kw != null && kw.length > 0)
				QueryField.IPTC_KEYWORDS.setPlainFieldValue(asset, kw);
			asset.setSafety(importFromDeviceData.getPrivacy());
		}
		meta.getKeywords().addAll(QueryField.getKeywordFilter().filter(asset.getKeyword()));
		return true;
	}

	protected void convertDestDist(Asset asset, String ref) {
		if ("M".equalsIgnoreCase(ref)) //$NON-NLS-1$
			asset.setGPSDestDistance(asset.getGPSDestDistance() * 1.609344);
		else if ("N".equalsIgnoreCase(ref)) //$NON-NLS-1$
			asset.setGPSDestDistance(asset.getGPSDestDistance() * 1.852);
	}

	private void setProperty(AssetEnsemble ensemble, QueryField qfield, String v, File file) {
		try {
			ensemble.setProperty(qfield, v);
		} catch (NumberFormatException e) {
			operation.addWarning(NLS.bind(Messages.ImportState_Bad_numeric_data,
					new Object[] { qfield.debugInfo(), file.getPath(), v }), null);
		} catch (XMPException e) {
			operation.addWarning(NLS.bind(Messages.ImportState_Bad_date_value,
					new Object[] { qfield.debugInfo(), file.getPath(), v }), e);
		} catch (Exception e) {
			operation.addWarning(
					NLS.bind(Messages.ImportState_Internal_error_processing_field, qfield.debugInfo(), file.getPath()),
					e);
		}
	}

	public Asset resetEnsemble(AssetEnsemble ensemble, URI uri, File file, Date lastModified, String originalFileName,
			Date now) {
		Asset asset = null;
		if (ensemble != null) {
			importedAssets.add(asset = ensemble.getAsset());
			ensemble.xmpTimestamp = asset.getXmpModifiedAt();
			ensemble.resetImageData(uri, getVolumeLabel(file), now, lastModified, file.length(), originalFileName,
					reimport ? CANCEL : fileSource);
		}
		return asset;
	}

	public byte[] useOldThumbnail(byte[] jpegThumbnail) {
		if (!getConfiguration().isSynchronize || getConfiguration().isResetImage)
			return null;
		int w = computeThumbnailWidth();
		int r = 3;
		int ow = 0;
		while (r < jpegThumbnail.length - 7) {
			if (jpegThumbnail[r] == -64) {
				ow = Math.max(BatchUtilities.readInt(jpegThumbnail, r + 4),
						BatchUtilities.readInt(jpegThumbnail, r + 6));
				break;
			}
			r += 2 + BatchUtilities.readInt(jpegThumbnail, r + 1);
		}
		return (ow != w) ? null : jpegThumbnail;
	}

	public int computeThumbnailWidth() {
		return computeThumbnailWidth(meta.getThumbnailResolution());
	}

	public static int computeThumbnailWidth(String res) {
		if (res != null && res.length() > 0) {
			char c = res.charAt(0);
			if (c == Meta_type.thumbnailResolution_high.charAt(0))
				return 640;
			if (c == Meta_type.thumbnailResolution_veryHigh.charAt(0))
				return 1280;
			if (c == Meta_type.thumbnailResolution_low.charAt(0))
				return 160;
		}
		return 320;
	}

	public String getVolumeLabel(File file) {
		if (volumeLabel == null)
			volumeLabel = Core.getCore().getVolumeManager().getVolumeForFile(file);
		return volumeLabel;
	}

	public boolean transferNeeded() {
		return importFromDeviceData != null && importFromDeviceData.getTargetDir() != null;
	}

	public boolean isMedia() {
		return importFromDeviceData != null && importFromDeviceData.isMedia();
	}

	public String getDcimOwner() {
		return importFromDeviceData == null ? null : importFromDeviceData.getDcimOwner();
	}
	
	public String getMedia() {
		return importFromDeviceData == null ? null : importFromDeviceData.getMedia();
	}

	public int getTimeshift() {
		return importFromDeviceData == null ? sidecarTimeshift : importFromDeviceData.getTimeshift() + sidecarTimeshift;
	}

	public int getSkipPolicy() {
		return importFromDeviceData == null ? Constants.SKIP_NONE : importFromDeviceData.getSkipPolicy();
	}

	public String getExifTransferPrefix() {
		if (importFromDeviceData == null)
			return null;
		String prefix = importFromDeviceData.getExifTransferPrefix();
		return prefix == null || prefix.isEmpty() ? null : prefix;
	}

	public boolean fromTransferFolder() {
		return transferNeeded() && importFromDeviceData.getWatchedFolder() != null; // from transfer folder
	}

	public void deleteTransferredFile(Asset asset) {
		if (transferNeeded())
			try {
				URI uri = new URI(asset.getUri());
				if (uri.getPath().startsWith(importFromDeviceData.getTargetDir()))
					new File(uri).delete();
			} catch (URISyntaxException e) {
				// ignore
			}
	}

	public boolean removeMedia() {
		return importFromDeviceData != null && importFromDeviceData.isRemoveMedia();
	}

	public void importFinished() {
		if (!added.isEmpty() || !modified.isEmpty())
			Core.getCore().fireAssetsModified(new BagChange<>(added, modified, null, null), null);
		if (changed)
			Core.getCore().fireStructureModified();
		analogProperties = null;
		importedAssets = null;
		meta = null;
		skipFile = null;
		if (exifTool != null) {
			exifTool.close();
			exifTool = null;
		}
	}

	public boolean isSilent() {
		return getConfiguration().inBackground;
	}

	public static File[] getXmpURIs(Asset asset) {
		URI uri = Core.getCore().getVolumeManager().findFile(asset);
		return Constants.FILESCHEME.equals(uri.getScheme()) ? Core.getSidecarFiles(uri, false) : EMPTYFILES;
	}

	public void safeReadXmp(AssetEnsemble ensemble, Asset asset, URI xmpURI) {
		try (InputStream in = xmpURI.toURL().openStream()) {
			Date impDate = asset.getImportDate();
			String impBy = asset.getImportedBy();
			readXMP(in, xmpURI.toString(), ensemble);
			asset.setImportDate(impDate);
			asset.setImportedBy(impBy);
		} catch (MalformedURLException e) {
			operation.addWarning(NLS.bind(Messages.ImportState_Bad_XMP_URL, xmpURI), e);
		} catch (IOException e) {
			operation.addError(NLS.bind(Messages.ImportState_IO_Error_XMP, xmpURI), e);
		} catch (Throwable e) {
			operation.addError(NLS.bind(Messages.ImportState_internal_error_xmp, xmpURI), e);
		}
	}

	public void readXMP(InputStream in, String file, AssetEnsemble ensemble) {
		try {
			for (XMPField field : XMPUtilities.readXMP(in)) {
				QueryField qfield = field.getQfield();
				if (qfield == QueryField.TIMESHIFT)
					sidecarTimeshift = field.getIntegerValue();
				else if (!overlayMap.containsKey(qfield.getExifToolKey()))
					assignValue(field, ensemble, file);
			}
		} catch (XMPException e) {
			operation.addError(NLS.bind(Messages.ImportState_Malformed_XMP, file), e);
		}
	}

	private void assignValue(XMPField field, AssetEnsemble ensemble, String file) {
		try {
			field.assignValue(ensemble, null);
		} catch (XMPException e) {
			operation.addWarning(NLS.bind(Messages.ImportState_IO_error_accessing_XMP, field.getProp().getPath(), file),
					e);
		} catch (Exception e) {
			operation.addError(
					NLS.bind(Messages.ImportState_Internal_error_assigning, field.getQfield().getKey(), file), e);
		}
	}

	public int promptForOverride(File file, Asset asset) {
		if (tempConfiguration != null) {
			switch (tempConfiguration.conflictPolicy) {
			case OVERWRITEALL:
				reimport = true;
				canUndo = false;
				return OVERWRITE;
			case OVERWRITENEWER:
				reimport = true;
				canUndo = false;
				return OVERWRITENEWER;
			case SYNCALL:
				reimport = true;
				canUndo = false;
				return SYNC;
			case SYNCNEWER:
				reimport = true;
				canUndo = false;
				return SYNCNEWER;
			case IGNOREALL:
				return IGNORE;
			}
		}
		boolean multiple = nFiles > 1;
		IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
		int ret = CANCEL;
		if (errorHandler != null) {
			tempConfiguration = errorHandler.showConflictDialog(Messages.ImportState_File_exists,
					NLS.bind(Messages.ImportState_File_already_imported, file), asset, getConfiguration(), multiple,
					info);
			if (tempConfiguration != null)
				ret = tempConfiguration.conflictPolicy;
		}
		switch (ret) {
		case OVERWRITEALL:
			//$FALL-THROUGH$
		case OVERWRITE:
			reimport = true;
			canUndo = false;
			return OVERWRITE;
		case SYNCALL:
			//$FALL-THROUGH$
		case SYNC:
			reimport = true;
			canUndo = false;
			return SYNC;
		case IGNOREALL:
			//$FALL-THROUGH$
		case IGNORE:
			return IGNORE;
		default:
			return CANCEL;
		}
	}

	public void storeIntoCatalog(List<Asset> assetsToIndex, Collection<? extends Asset> deletedAssets,
			final Collection<Object> toBeDeleted, final Collection<Object> toBeStored, final Collection<Object> trashed,
			int importNo) throws ImportException {
		CoreActivator coreActivator = CoreActivator.getDefault();
		final IDbManager dbManager = coreActivator.getDbManager();
		if (!operation.storeSafely(() -> {
			for (Object del : toBeDeleted)
				dbManager.delete(del);
			for (Object obj1 : toBeStored)
				dbManager.store(obj1);
			for (Object obj2 : trashed)
				dbManager.deleteTrash(obj2);
		}, 1))
			throw new ImportException();
		allDeletedAssets.addAll(deletedAssets);
		for (Asset asset : assetsToIndex) {
			if (deletedAssets.contains(asset))
				modified.add(asset);
			else
				added.add(asset);
		}
		if (!assetsToIndex.isEmpty() && !dbManager.getMeta(true).getNoIndex()) {
			Job job = Core.getCore().getDbFactory().getLireService(true).createIndexingJob(assetsToIndex, reimport,
					nFiles, importNo, isSilent() || coreActivator.isNoProgress());
			if (job != null)
				job.schedule();
		}
	}

	public List<Asset> obtainImportedAssets() {
		return new ArrayList<Asset>(Core.getCore().getDbManager().obtainObjects(AssetImpl.class, "importDate", //$NON-NLS-1$
				importDate, QueryField.EQUALS));
	}

	public File[] transferFile(StorageObject file, IProgressMonitor monitor) throws DiskFullException {
		long lastModified = file.lastModified();
		cal.setTimeInMillis(lastModified);
		String filename = file.getName();
		File subFolder = new File(importFromDeviceData.getTargetDir());
		boolean deep = importFromDeviceData.isDeepSubfolders();
		int subfolderPolicy = importFromDeviceData.getSubfolderPolicy();
		if (subfolderPolicy != ImportFromDeviceData.SUBFOLDERPOLICY_NO) {
			Date date = cal.getTime();
			subFolder = new File(subFolder, Format.YEAR_FORMAT.get().format(date)); 
			subFolder.mkdir();
			switch (subfolderPolicy) {
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTH:
				subFolder = new File(subFolder, Format.YEAR_MONTH_FORMAT.get().format(date)); 
				subFolder.mkdir();
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTHDAY:
				if (deep) {
					subFolder = new File(subFolder, Format.YEAR_MONTH_FORMAT.get().format(date)); 
					subFolder.mkdir();
				}
				subFolder = new File(subFolder, Format.YEAR_MONTH_DAY_FORMAT.get().format(date)); 
				subFolder.mkdir();
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARWEEK:
				subFolder = new File(subFolder, Format.YEAR_WEEK_FORMAT.get().format(date)); 
				subFolder.mkdir();
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARWEEKDAY:
				if (deep) {
					subFolder = new File(subFolder, Format.YEAR_WEEK_FORMAT.get().format(date)); 
					subFolder.mkdir();
				}
				subFolder = new File(subFolder, Format.YEAR_WEEK_DAY_FORMAT.get().format(date)); 
				subFolder.mkdir();
				break;
			}
		}
		String ext = BatchUtilities.getTrueFileExtension(filename);
		String newFilename = Utilities.evaluateTemplate(importFromDeviceData.getRenamingTemplate(),
				importFromDeviceData.getWatchedFolder() != null ? Constants.TV_TRANSFER : Constants.TV_ALL, filename,
				cal, importNo, meta.getLastSequenceNo() + 1, meta.getLastYearSequenceNo() + 1,
				importFromDeviceData.getCue(), null, "", //$NON-NLS-1$
				BatchConstants.MAXPATHLENGTH - subFolder.getAbsolutePath().length() - 1 - ext.length(), true);
		File target = BatchUtilities.makeUniqueFile(subFolder, newFilename, ext);
		File voiceTarget = null;
		try {
			CoreActivator.getDefault().getFileWatchManager().copyFileSilently(file, target, lastModified,
					operation.getOpId(), monitor);
			if (!file.isRemote()) {
				StorageObject voiceAttachment = file.getVoiceAttachment();
				if (voiceAttachment != null) {
					String targetPath = target.getAbsolutePath();
					String name = voiceAttachment.getName();
					voiceTarget = new File(targetPath.substring(0, targetPath.length() - ext.length())
							+ name.substring(name.lastIndexOf('.')));
					if (!voiceTarget.exists())
						CoreActivator.getDefault().getFileWatchManager().copyFileSilently(voiceAttachment, voiceTarget,
								lastModified, operation.getOpId(), monitor);
				}
				if (file.isLocal())
					for (IRelationDetector detector : configuration.relationDetectors)
						detector.transferFile(file.resolve(), target, importNo == 1, info, operation.getOpId());
			}
		} catch (IOException e) {
			operation.addError(NLS.bind(Messages.ImportState_IO_error_importing, file), e);
		}
		return voiceTarget == null ? new File[] { target } : new File[] { target, voiceTarget };
	}

	public boolean skipFile(StorageObject file, String extension) {
		if (file.equals(skipFile)) {
			skipFile = null;
			return true;
		}
		if (importFromDeviceData != null) {
			Set<String> skippedFormats = importFromDeviceData.getSkippedFormats();
			if (skippedFormats != null && skippedFormats.contains(extension))
				return true;
		}
		return false;
	}

	public boolean skipDuplicates(File file, String originalFileName, Date lastModified) {
		if (importFromDeviceData != null && importFromDeviceData.isDetectDuplicates()) {
			String exifOriginalFile = getExifTool(file, getConfiguration().getExifFastMode()).getMetadata()
					.get(QueryField.EXIF_ORIGINALFILENAME.getExifToolKey());
			if (exifOriginalFile == null)
				exifOriginalFile = originalFileName;
			if (Core.getCore().getDbManager()
					.obtainObjects(AssetImpl.class, false, QueryField.EXIF_DATETIME.getKey(), lastModified,
							QueryField.EQUALS, QueryField.EXIF_ORIGINALFILENAME.getKey(), exifOriginalFile,
							QueryField.EQUALS)
					.iterator().hasNext()) {
				operation.addWarning(NLS.bind(Messages.ImportState_already_in_cat, originalFileName), null);
				return true;
			}
		}
		return false;
	}

	public void processXmpSidecars(URI uri, IProgressMonitor monitor, AssetEnsemble ensemble) {
		File[] sidecars = Core.getSidecarFiles(uri, false);
		for (int i = 0; i < sidecars.length & !monitor.isCanceled(); i++) {
			File xmpFile = sidecars[i];
			if (xmpFile.exists()) {
				if (reimport) {
					File backupFile = new File(xmpFile.getAbsolutePath() + ".original"); //$NON-NLS-1$
					if (backupFile.exists())
						try {
							CoreActivator.getDefault().getFileWatchManager().moveFileSilently(backupFile, xmpFile,
									operation.getOpId(), monitor);
						} catch (IOException e) {
							operation.addError(NLS.bind(Messages.ImportState_io_error_restoring_xmp, xmpFile), e);
						} catch (DiskFullException e) {
							operation.addError(NLS.bind(Messages.ImportState_disk_full_restoring_xmp, xmpFile), e);
						}
					else if (!getConfiguration().isResetExif && !getConfiguration().isResetGps
							&& !getConfiguration().isResetFaceData && !getConfiguration().isResetIptc
							&& ensemble.xmpTimestamp != null
							&& xmpFile.lastModified() == ensemble.xmpTimestamp.getTime())
						xmpFile = null; // XMP file is not new
				}
			} else
				xmpFile = null;
			if (xmpFile != null) {
				ensemble.getAsset().setXmpModifiedAt(new Date(xmpFile.lastModified()));
				safeReadXmp(ensemble, ensemble.getAsset(), xmpFile.toURI());
			}
		}
	}

	public boolean writeThumbnail(ZImage image, Asset asset, int angle) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
			ImportConfiguration conf = getConfiguration();
			image.saveToStream(null, true, ZImage.CROPMASK, SWT.DEFAULT, SWT.DEFAULT, out,
					conf.useWebP ? ZImage.IMAGE_WEBP : SWT.IMAGE_JPEG, conf.jpegQuality);
			asset.setJpegThumbnail(out.toByteArray());
			asset.setRotation(angle);
			if (asset.getWidth() < 0)
				asset.setWidth(image.sourceWidth);
			if (asset.getHeight() < 0)
				asset.setHeight(image.sourceHeight);
			return true;
		} catch (Exception e) {
			operation.addError(Messages.ImportState_error_writing_thumbnail, e);
			return false;
		}
	}

	public int getThumbAngle(Asset asset, boolean isConverted) {
		return reimport ? asset.getRotation() : isConverted ? 0 : Utilities.orientationDegrees(asset);
	}

	public boolean processSidecars() {
		return getConfiguration().processSidecars;
	}

	/**
	 * @return configuration
	 */
	public ImportConfiguration getConfiguration() {
		return tempConfiguration != null ? tempConfiguration : configuration;
	}

	/**
	 * @param configuration
	 *            - the import configuration
	 */
	public void setConfiguration(ImportConfiguration configuration) {
		this.configuration = configuration;
	}

	public void nextPicture(int ret) {
		sidecarTimeshift = 0;
		reimport = false;
		if (tempConfiguration != null && tempConfiguration.conflictPolicy == CANCEL)
			tempConfiguration = null;
		overlayMap.clear();
		changed |= ret < 0;
		if (ret != 0) {
			if ((importNo & 7) == 0 && !isSilent()) {
				Core.getCore().fireAssetsModified(new BagChange<>(added, modified, null, null), null);
				added.clear();
				modified.clear();
				if (changed) {
					Core.getCore().fireStructureModified();
					changed = false;
				}
			}
			++importNo;
		}
	}

	public void reportError(String message, Throwable t) {
		if (isSilent())
			operation.addWarning(message, t);
		else
			operation.addError(message, t);
	}

	public void addErrorOnce(String message, Throwable t) {
		if (errorSet.add(message))
			operation.addError(message, t);
	}

	public AnalogProperties getAnalogProperties() {
		return analogProperties;
	}

	public boolean showImported() {
		ImportConfiguration conf = getConfiguration();
		return conf.showImported && (!conf.inBackground && !conf.isSynchronize || fromTransferFolder());
	}

	public boolean isUserImport() {
		ImportConfiguration conf = getConfiguration();
		return !conf.inBackground && !conf.isSynchronize;
	}

}