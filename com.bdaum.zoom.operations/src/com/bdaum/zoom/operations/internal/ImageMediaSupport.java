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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.operations.internal;

import java.awt.color.ICC_Profile;
import java.awt.image.ColorConvertOp;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.ConversionException;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.batch.internal.ExifToolSubstitute;
import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.IPreferenceUpdater;
import com.bdaum.zoom.core.internal.ImportException;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.IniReader;
import com.bdaum.zoom.core.internal.MakerSupport;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.db.AssetEnsemble.MWGRegion;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.image.IExifLoader;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class ImageMediaSupport extends AbstractMediaSupport {

	private static final String PICASA_INI = ".picasa.ini"; //$NON-NLS-1$
	private ImportState importState;
	private int twidth;

	/*** Importing ***/

	public int importFile(File file, String extension, ImportState importState, IProgressMonitor aMonitor, URI remote)
			throws Exception {
		this.importState = importState;
		IRawConverter rc = importState.getConfiguration().rawConverter;
		twidth = importState.computeThumbnailWidth();
		int work = IMediaSupport.IMPORTWORKSTEPS;
		boolean relationDone = false;
		final List<Object> toBeStored = new ArrayList<Object>();
		final List<Object> toBeDeleted = new ArrayList<Object>();
		final List<Object> trashed = new ArrayList<Object>();
		List<Asset> deletedAssets = new ArrayList<Asset>();
		byte[] oldThumbnail = null;
		byte[] oldDngThumbnail = null;
		File originalFile = null;
		String oldId = null;
		String oldDngId = null;
		int icnt = 0;
		String originalFileName = file.getName();
		File parent = file.getParentFile();
		while (parent != null) {
			String name = parent.getName();
			if ("DCIM".equals(name)) //$NON-NLS-1$
				break;
			originalFileName = name + '/' + originalFileName;
			parent = parent.getParentFile();
		}
		long lastMod = file.lastModified();

		CoreActivator coreActivator = CoreActivator.getDefault();
		IDbManager dbManager = coreActivator.getDbManager();
		URI uri = file.toURI();
		String uriAsString = uri.toString();
		if (importState.importFromDeviceData == null && rc != null)
			lastMod = rc.getLastRecipeModification(uriAsString, lastMod, importState.recipeDetectorIds);
		Date lastModified = new Date(lastMod);
		boolean isDng = ImageConstants.isDng(uriAsString);
		boolean isRaw = ImageConstants.isRaw(uriAsString, false);
		boolean isJpeg = ImageConstants.isJpeg(extension);
		ImportFromDeviceData importFromDeviceData = importState.importFromDeviceData;
		if (importFromDeviceData != null) {
			int skipPolicy = importFromDeviceData.getSkipPolicy();
			if (skipPolicy == Constants.SKIP_JPEG && isJpeg || skipPolicy == Constants.SKIP_RAW && (isDng || isRaw))
				return 0;
			if (importState.skipDuplicates(originalFileName, lastModified))
				return 0;
			originalFile = file;
			file = importState.transferFile(file, importState.importNo, aMonitor);
			if (file == null)
				return 0;
			uriAsString = file.toURI().toString();
		}
		ImportConfiguration configuration = importState.getConfiguration();
		boolean importRaw = configuration.rawOptions.equals(Constants.RAWIMPORT_ONLYRAW)
				|| configuration.rawOptions.equals(Constants.RAWIMPORT_BOTH);
		boolean importDng = !configuration.rawOptions.equals(Constants.RAWIMPORT_ONLYRAW);
		boolean importDngEmbeddedRaw = configuration.rawOptions.equals(Constants.RAWIMPORT_DNGEMBEDDEDRAW);
		boolean fromImportFilter = false;
		if (isDng || isRaw)
			importState.fileSource = Constants.FILESOURCE_DIGITAL_CAMERA;
		else
			fromImportFilter = ImageActivator.getDefault().getImportFilters().get(extension) != null;
		File convert = null;
		File dngFolder = null;
		try {
			AssetEnsemble ensemble = null;
			AssetEnsemble dngEnsemble = null;
			List<AssetEnsemble> existing = null;
			List<AssetEnsemble> existingDng = null;
			if (importRaw || !isRaw)
				existing = AssetEnsemble.getAllAssets(dbManager, remote != null ? remote : file.toURI(), importState);
			String dngUriAsString = null;
			URI dngURI = null;
			if (importDng && isRaw) {
				dngUriAsString = Core.removeExtensionFromUri(uriAsString) + ".dng"; //$NON-NLS-1$
				try {
					if (configuration.dngFolder != null && configuration.dngFolder.length() > 0) {
						int p = dngUriAsString.lastIndexOf('/');
						String dngFolderUri = dngUriAsString.substring(0, p + 1) + configuration.dngFolder;
						dngUriAsString = dngFolderUri + dngUriAsString.substring(p);
						dngFolder = new File(new URI(dngFolderUri));
						dngFolder.mkdir();
					}
					dngURI = new URI(dngUriAsString);
					existingDng = AssetEnsemble.getAllAssets(dbManager, dngURI, importState);
				} catch (URISyntaxException e) {
					importState.operation.addError(
							NLS.bind(Messages.getString("ImageMediaSupport.Bad_DNG_URI"), dngUriAsString), //$NON-NLS-1$
							e);
				}
			}
			Meta meta = importState.meta;
			if (importFromDeviceData == null) {
				Asset asset = null;
				if (existing != null && !existing.isEmpty()) {
					asset = existing.get(0).getAsset();
					oldId = asset.getStringId();
					oldThumbnail = importState.useOldThumbnail(asset.getJpegThumbnail());
				}
				if (existingDng != null && !existingDng.isEmpty()) {
					asset = existingDng.get(0).getAsset();
					oldDngId = asset.getStringId();
					oldDngThumbnail = importState.useOldThumbnail(asset.getJpegThumbnail());
				}
				if (asset != null) {
					if (configuration.conflictPolicy == ImportState.IGNOREALL)
						return 0;
					if (configuration.isSynchronize || (oldId == null && oldDngId != null && importState.isSilent()
							&& importDng && importRaw)) {
						ensemble = (existing != null && !existing.isEmpty()) ? existing.remove(0) : null;
						dngEnsemble = (existingDng != null && !existingDng.isEmpty()) ? existingDng.remove(0) : null;
						importState.reimport = true;
						importState.canUndo = false;
					} else {
						int ret = importState.promptForOverride(file, asset);
						configuration = importState.getConfiguration();
						switch (ret) {
						case ImportState.CANCEL:
							aMonitor.setCanceled(true);
							return 0;
						case ImportState.OVERWRITENEWER:
							if (!isOutDated(existing, lastMod))
								return 0;
							//$FALL-THROUGH$
						case ImportState.OVERWRITE:
							AssetEnsemble.deleteAll(existing, deletedAssets, toBeDeleted, toBeStored);
							AssetEnsemble.deleteAll(existingDng, deletedAssets, toBeDeleted, toBeStored);
							break;
						case ImportState.SYNCNEWER:
							if (!isOutDated(existing, lastMod))
								return 0;
							//$FALL-THROUGH$
						case ImportState.SYNC:
							ensemble = (existing != null && !existing.isEmpty()) ? existing.remove(0) : null;
							dngEnsemble = (existingDng != null && !existingDng.isEmpty()) ? existingDng.remove(0)
									: null;
							importState.reimport = true;
							importState.canUndo = false;
							break;
						default:
							return 0;
						}
					}
				}
			}
			if (importRaw || !isRaw) {
				List<Ghost_typeImpl> ghosts = dbManager.obtainGhostsForFile(remote != null ? remote : file.toURI());
				importState.allDeletedGhosts.addAll(ghosts);
				toBeDeleted.addAll(ghosts);
				if (ensemble == null)
					ensemble = new AssetEnsemble(dbManager, importState, oldId);
			}
			Asset asset = importState.resetEnsemble(ensemble, remote != null ? remote : file.toURI(), file,
					lastModified, originalFileName, importState.importDate);
			Asset dngAsset = null;
			ZImage dngImage = null;
			aMonitor.worked(1);
			--work;
			// Convert to DNG
			if (isRaw && importDng) {
				if (dngEnsemble == null)
					dngEnsemble = new AssetEnsemble(dbManager, importState, oldDngId);
				dngAsset = importState.resetEnsemble(dngEnsemble, dngURI, file, importState.importDate,
						originalFileName, importState.importDate);
				String dngLocation = configuration.dngLocator.getDngLocation();
				File locat = (dngLocation == null || dngLocation.length() == 0) ? null : new File(dngLocation);
				if (locat == null || !locat.exists()) {
					locat = Core.getCore().getDbFactory().getErrorHandler().showDngDialog(locat, importState.info);
					if (locat != null && locat.getName().length() > 0) {
						IPreferenceUpdater locator = importState.info.getAdapter(IPreferenceUpdater.class);
						if (locator != null)
							locator.setDngLocation(locat);
					}
				}
				if (locat == null) {
					aMonitor.setCanceled(true);
					return 0;
				}
				if (locat.getName().length() > 0) {
					Options options = new Options();
					options.put("uncompressed", //$NON-NLS-1$
							configuration.dngUncompressed);
					options.put("linear", configuration.dngLinear); //$NON-NLS-1$
					options.put("embedded", importDngEmbeddedRaw); //$NON-NLS-1$
					options.put("highres", (!meta.getThumbnailResolution() //$NON-NLS-1$
							.equals(Meta_type.thumbnailResolution_low)));
					if (dngFolder != null)
						options.put("outputFolder", dngFolder //$NON-NLS-1$
								.getAbsolutePath());
					File dngFile = null;
					try {
						dngFile = BatchActivator.getDefault().convertFile(file, "dng", //$NON-NLS-1$
								configuration.dngLocator.getDngLocation(), options, false,
								coreActivator.getFileWatchManager(), importState.operation.getOpId(), aMonitor);
					} catch (ConversionException e) {
						importState.operation
								.addError(NLS.bind(Messages.getString("ImageMediaSupport.DNG_conversion_failed"), //$NON-NLS-1$
										file), e);
					}
					if (dngFile != null) {
						List<Ghost_typeImpl> ghosts = dbManager.obtainGhostsForFile(dngURI);
						importState.allDeletedGhosts.addAll(ghosts);
						toBeDeleted.addAll(ghosts);
						dngAsset.setLastModification(new Date(dngFile.lastModified()));
						dngAsset.setFileSize(dngFile.length());
						IExifLoader etool = importState.getExifTool(dngFile, true);
						Recipe dngRecipe = null;
						if (oldDngThumbnail == null) {
							if (meta.getThumbnailFromPreview())
								dngImage = loadPreviewImage(etool, meta, dngAsset);
							if (dngImage == null) {
								dngRecipe = rc == null ? null
										: rc.getRecipe(dngUriAsString, false, etool, importState.overlayMap,
												importState.recipeDetectorIds);
								try {
									convert = rawConvert(dngFile, dngRecipe, aMonitor);
									if (convert != null)
										dngImage = ZImage.loadThumbnail(convert, null, twidth);
									else {
										importState.reportError(NLS.bind(
												Messages.getString("ImageMediaSupport.DCRAW_conversion_DNG_failed"), //$NON-NLS-1$
												file), null);
										dngAsset = null;
									}
								} catch (ConversionException e) {
									importState.reportError(NLS.bind(
											Messages.getString("ImageMediaSupport.DCRAW_conversion_DNG_failed"), //$NON-NLS-1$
											file), e);
								}
							}
						}
						if ((dngImage != null || oldDngThumbnail != null) && dngAsset != null) {
							if (createImageEntry(dngFile, dngFile.toURI(), "dng", true, dngEnsemble, //$NON-NLS-1$
									dngImage, oldDngThumbnail, etool, dngRecipe, importState.importDate, toBeStored,
									toBeDeleted, aMonitor)) {
								setRawFormat(dngAsset, null);
								AssetEnsemble.deleteAll(existingDng, deletedAssets, toBeDeleted, toBeStored);
								dngEnsemble.removeFromTrash(trashed);
								dngEnsemble.store(toBeDeleted, toBeStored);
								icnt++;
							}
						}
					} else
						dngAsset = null;
				} else
					configuration.rawOptions = Constants.RAWIMPORT_ONLYRAW;
			}
			aMonitor.worked(1);
			--work;
			// Read Image
			String imageURI = remote != null ? remote.toString() : uriAsString;
			if (asset != null) {
				ZImage image = null;
				IExifLoader etool;
				if (fromImportFilter)
					etool = new ExifToolSubstitute(file);
				else {
					File exifFile = file;
					if (importFromDeviceData != null && originalFile != null) {
						String originalUri = (remote != null ? remote : originalFile.toURI()).toString();
						if (isRaw || isDng) {
							File jpegFile = findJpegSibling(originalUri);
							if (jpegFile != null && importFromDeviceData.getSkipPolicy() == Constants.SKIP_JPEG_IF_RAW)
								importState.skipFile = jpegFile;
							if (importFromDeviceData.getExifTransferPrefix().length() > 0) {
								int q = originalUri.lastIndexOf('/');
								String name = (q < 0) ? originalUri : originalUri.substring(q + 1);
								if (jpegFile != null && name.startsWith(importFromDeviceData.getExifTransferPrefix()))
									exifFile = jpegFile;
							}
						} else if (isJpeg) {
							File rawFile = findRawSibling(originalUri);
							if (rawFile != null && importFromDeviceData.getSkipPolicy() == Constants.SKIP_RAW_IF_JPEG)
								importState.skipFile = rawFile;
						}
					}
					etool = importState.getExifTool(exifFile, true);
				}
				Recipe rawRecipe = null;
				if (isDng) {
					if (oldThumbnail == null) {
						if (meta.getThumbnailFromPreview())
							image = loadPreviewImage(etool, meta, asset);
						if (image == null)
							try {
								rawRecipe = rc == null ? null
										: rc.getRecipe(uriAsString, false, etool, importState.overlayMap,
												importState.recipeDetectorIds);
								convert = rawConvert(file, rawRecipe, aMonitor);
								if (convert != null)
									image = ZImage.loadThumbnail(convert, null, twidth);
								else {
									importState.reportError(NLS.bind(
											Messages.getString("ImageMediaSupport.DCRAW_conversion_DNG_failed"), //$NON-NLS-1$
											file), null);
									asset = null;
								}
							} catch (ConversionException e) {
								importState.reportError(
										NLS.bind(Messages.getString("ImageMediaSupport.DCRAW_conversion_DNG_failed"), //$NON-NLS-1$
												file),
										e);
							}
					}
					setRawFormat(asset, null);
				} else if (isRaw) {
					rawRecipe = rc == null ? null
							: rc.getRecipe(uriAsString, false, etool, importState.overlayMap,
									importState.recipeDetectorIds);
					if ((dngImage != null || oldDngThumbnail != null) && dngEnsemble != null && dngAsset != null
							&& ensemble != null && rawRecipe == null) {
						dngEnsemble.transferTo(ensemble);
						image = dngImage;
						oldThumbnail = oldDngThumbnail;
						asset.setUri(imageURI);
						asset.setFileSize(file.length());
						setRawFormat(asset, extension);
						AssetEnsemble.deleteAll(existing, deletedAssets, toBeDeleted, toBeStored);
						ensemble.store(toBeDeleted, toBeStored);
						StringBuilder sb = new StringBuilder();
						sb.append(configuration.dngUncompressed ? "uncompressed" //$NON-NLS-1$
								: "compressed") //$NON-NLS-1$
								.append(configuration.dngLinear ? ", linear" //$NON-NLS-1$
										: ", logarithmic"); //$NON-NLS-1$
						if (importDngEmbeddedRaw)
							sb.append(", embedded RAW file"); //$NON-NLS-1$
						if (!configuration.isSynchronize || configuration.isResetImage)
							updateOrCreateDerivedByRelation(asset.getStringId(), imageURI, dngAsset.getStringId(),
									dngUriAsString, sb.toString(), false, null, dngAsset.getLastModification(),
									dngAsset.getSoftware(), "Adobe DNG Converter", toBeStored, toBeDeleted); //$NON-NLS-1$
						relationDone = true;
						asset = null;
					} else {
						if (oldThumbnail == null) {
							if (meta.getThumbnailFromPreview())
								image = loadPreviewImage(etool, meta, asset);
							if (image == null)
								try {
									convert = rawConvert(file, rawRecipe, aMonitor);
									if (convert != null)
										image = ZImage.loadThumbnail(convert, null, twidth);
									else {
										importState.reportError(NLS.bind(
												Messages.getString("ImageMediaSupport.DCRAW_conversion_failed"), //$NON-NLS-1$
												file), null);
										asset = null;
									}
								} catch (ConversionException e) {
									importState.reportError(NLS.bind(
											Messages.getString("ImageMediaSupport.dcraw_conversion_failed_because"), //$NON-NLS-1$
											file, e), e);
								}
						}
					}
					setRawFormat(asset, extension);
				} else {
					if (oldThumbnail == null) {
						if (fromImportFilter)
							image = ((ExifToolSubstitute) etool).loadThumbnail(twidth, twidth / 4 * 3,
									ImportState.MCUWidth, 0f);
						else
							try {
								image = ZImage.loadThumbnail(file, extension, twidth);
							} catch (OutOfMemoryError e) {
								throw e;
							} catch (NoClassDefFoundError e) {
								importState
										.reportError(NLS.bind(Messages.getString("ImageMediaSupport.codec_not_found"), //$NON-NLS-1$
												file, Constants.APPLICATION_NAME), e);
								return 0;
							} catch (Exception e) {
								if (e instanceof IndexOutOfBoundsException
										|| e.getCause() instanceof ArrayIndexOutOfBoundsException)
									importState.reportError(NLS.bind(
											Messages.getString("ImageMediaSupport.error_when_reading_swt"), file), e); //$NON-NLS-1$
								else
									importState.reportError(
											NLS.bind(Messages.getString("ImageMediaSupport.error_reading"), file), e); //$NON-NLS-1$
								return 0;
							}
					}
				}
				aMonitor.worked(1);
				--work;
				if (asset != null && ensemble != null) {
					if (createImageEntry(file, uri, extension, convert != null, ensemble, image, oldThumbnail, etool,
							rawRecipe, importState.importDate, toBeStored, toBeDeleted, aMonitor)) {
						if (isRaw)
							setRawFormat(asset, extension);
						else if (isDng)
							setRawFormat(asset, null);
						AssetEnsemble.deleteAll(existing, deletedAssets, toBeDeleted, toBeStored);
						ensemble.removeFromTrash(trashed);
						ensemble.store(toBeDeleted, toBeStored);
						icnt++;
					}
				}
				aMonitor.worked(1);
				--work;
			}
			if (asset != null || dngAsset != null) {
				meta.setLastSequenceNo(meta.getLastSequenceNo() + 1);
				meta.setLastYearSequenceNo(meta.getLastYearSequenceNo() + 1);
				toBeStored.add(meta);
			}
			if (asset != null) {
				if (!relationDone) {
					boolean detected = false;
					for (IRelationDetector detector : configuration.relationDetectors) {
						URI targetUri = detector.detectRelation(imageURI, isDng, isRaw, ensemble, toBeDeleted,
								toBeStored);
						if (targetUri != null) {
							Iterator<AssetImpl> it = dbManager.obtainAssetsForFile(targetUri).iterator();
							if (it.hasNext()) {
								AssetImpl orig = it.next();
								updateOrCreateDerivedByRelation(orig.getStringId(), orig.getUri(), asset.getStringId(),
										asset.getUri(), null, false, null, asset.getLastModification(),
										asset.getSoftware(), null, toBeStored, toBeDeleted);
								propagateXmp(orig, asset, ensemble, toBeDeleted, toBeStored);
							} else
								updateOrCreateDerivedByRelation(targetUri.toString(), targetUri.toString(),
										asset.toString(), asset.getUri(), detector.getName(), false, null,
										asset.getLastModification(), asset.getSoftware(), null, toBeStored,
										toBeDeleted);
							detected = true;
							break;
						}
					}
					if (!configuration.deriveRelations.equals(Constants.DERIVE_NO))
						detected = detectAndCreateDerivateByName(imageURI, isDng, isRaw, asset, ensemble, toBeDeleted,
								toBeStored);
					if (!detected && configuration.autoDerive)
						detectAndCreateDerivateByProfile(isDng, isRaw, asset, ensemble, toBeDeleted, toBeStored);
				}
				String assetId = asset.getStringId();
				List<DerivedByImpl> set = dbManager.obtainObjects(DerivedByImpl.class, "derivative", imageURI, //$NON-NLS-1$
						QueryField.EQUALS);
				for (DerivedByImpl rel : set) {
					if (!toBeDeleted.contains(rel)) {
						toBeDeleted.add(rel);
						importState.allDeletedRelations.add(rel);
						Date date = rel.getDate();
						String tool = rel.getTool();
						toBeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(),
								tool == null ? asset.getSoftware() : tool,
								date.getTime() == 0 ? asset.getLastModification() : date, assetId, rel.getOriginal()));
					}
				}
				set = dbManager.obtainObjects(DerivedByImpl.class, "original", imageURI, QueryField.EQUALS); //$NON-NLS-1$
				for (DerivedByImpl rel : set) {
					if (toBeDeleted.contains(rel)) {
						toBeDeleted.add(rel);
						importState.allDeletedRelations.add(rel);
						toBeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(), rel.getTool(),
								rel.getDate(), rel.getDerivative(), assetId));
					}
				}
			}

			List<Asset> assetsToIndex = new ArrayList<Asset>();
			if (asset != null)
				assetsToIndex.add(asset);
			if (dngAsset != null)
				assetsToIndex.add(dngAsset);
			importState.storeIntoCatalog(assetsToIndex, deletedAssets, toBeDeleted, toBeStored, trashed,
					importState.importNo);
			if (configuration.rules != null)
				OperationJob.executeSlaveOperation(new AutoRuleOperation(configuration.rules, assetsToIndex, null),
						importState.getConfiguration().info);
			--work;
			boolean changed = importState.operation.updateFolderHierarchies(asset, true, configuration.timeline,
					configuration.locations, false);
			changed |= importState.operation.updateFolderHierarchies(dngAsset, true, configuration.timeline,
					configuration.locations, false);
			if (importFromDeviceData != null && importFromDeviceData.getWatchedFolder() != null && originalFile != null
					&& !file.equals(originalFile))
				originalFile.delete();
			// from transfer folder. Delete original file
			return (changed) ? -icnt : icnt;
		} catch (ImportException e) {
			return 0;
		} finally {
			if (convert != null)
				convert.delete();
			if (importFromDeviceData != null && isRaw
					&& (configuration.rawOptions.equals(Constants.RAWIMPORT_DNGEMBEDDEDRAW)
							|| configuration.rawOptions.equals(Constants.RAWIMPORT_ONLYDNG)))
				// When importing from camera, the transmitted raw file can be
				// deleted if only the converted DNG file is wanted
				file.delete();
			aMonitor.worked(work);
		}
	}

	private ZImage loadPreviewImage(IExifLoader tool, Meta meta, Asset asset) {
		try {
			ZImage previewImage = tool.getPreviewImage(true);
			if (previewImage != null) {
				int psize = Math.max(previewImage.width, previewImage.height);
				if (asset != null)
					asset.setPreviewSize(psize);
				int theight = twidth / 4 * 3;
				if (psize >= twidth) {
					if (previewImage.height > previewImage.width) {
						int www = theight;
						theight = twidth;
						twidth = www;
					}
					previewImage.setScaling(twidth, theight, false, ImportState.MCUWidth, null); // , ZImage.SCALE_DEFAULT);
					String s = tool.getMetadata().get(QueryField.EXIF_ORIENTATION.getExifToolKey());
					if (s != null) {
						try {
							ExifTool.fixOrientation(previewImage, BatchUtilities.parseInt(s), 0);
						} catch (NumberFormatException e) {
							// ignore
						}
					}
					return previewImage;
				}
			}
		} catch (Exception e) {
			// do nothing - ignore preview image
		}
		return null;
	}

	private File rawConvert(File dngFile, Recipe recipe, IProgressMonitor monitor) throws ConversionException {
		IRawConverter rawConverter = importState.getConfiguration().rawConverter;
		if (rawConverter == null || !rawConverter.isValid()) {
			BatchActivator activator = BatchActivator.getDefault();
			if (!activator.isRawQuestionAsked()) {
				rawConverter = Core.getCore().getDbFactory().getErrorHandler().showRawDialog(importState.info);
				BatchActivator.getDefault().setRawQuestionAsked(true);
			}
			if (rawConverter == null || !rawConverter.isValid()) {
				importState.operation.addError(Messages.getString("ImageMediaSupport.no_raw_convert"), null); //$NON-NLS-1$
				return null;
			}
			importState.getConfiguration().rawConverter = rawConverter;
		}
		Options options = null;
		if (recipe != null && recipe != Recipe.NULL) {
			options = new Options();
			recipe.setSampleFactor(rawConverter.deriveOptions(recipe, options, IRawConverter.THUMB));
		}
		return BatchActivator.getDefault().convertFile(dngFile, rawConverter.getId(), rawConverter.getPath(), options,
				true, CoreActivator.getDefault().getFileWatchManager(), importState.operation.getOpId(), monitor);
	}

	private static void setRawFormat(Asset asset, String ext) {
		if (asset != null) {
			asset.setFormat(ext == null ? ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE
					: ImageConstants.getRawFormatDescription(ext));
			asset.setDigitalZoomRatio(Double.NaN);
		}
	}

	private static File findJpegSibling(String originalUri) throws URISyntaxException {
		String jpegUri = Core.removeExtensionFromUri(originalUri);
		for (String ext : ImageConstants.JPEGEXTENSIONS) {
			File jpegFile = new File(new URI(jpegUri + ext));
			if (jpegFile.exists())
				return jpegFile;
			jpegFile = new File(new URI(jpegUri + ext.toUpperCase()));
			if (jpegFile.exists())
				return jpegFile;
		}
		return null;
	}

	private static File findRawSibling(String originalUri) throws URISyntaxException {
		String rawUri = Core.removeExtensionFromUri(originalUri);
		for (String ext : ImageConstants.getRawFormatMap().keySet()) {
			File rawFile = new File(new URI(rawUri + '.' + ext));
			if (rawFile.exists())
				return rawFile;
			rawFile = new File(new URI(rawUri + '.' + ext.toUpperCase()));
			if (rawFile.exists())
				return rawFile;
		}
		return null;
	}

	private byte[] archiveRecipe(String parmFile, byte[] archivedRecipe) {
		try {
			File file = new File(new URI(parmFile));
			byte[] bytes = new byte[(int) file.length()];
			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
				in.read(bytes);
			}
			CRC32 crc = new CRC32();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try (ZipOutputStream s = new ZipOutputStream(bout)) {
				s.setLevel(6);
				ZipEntry entry = new ZipEntry(file.getName());
				entry.setSize(bytes.length);
				crc.reset();
				crc.update(bytes);
				entry.setCrc(crc.getValue());
				s.putNextEntry(entry);
				s.write(bytes, 0, bytes.length);
				s.finish();
			}
			archivedRecipe = bout.toByteArray();
		} catch (IOException e) {
			importState.operation.addError(NLS.bind(Messages.getString("ImageMediaSupport.io-error_archiving_recipe"), //$NON-NLS-1$
					parmFile), e);
		} catch (URISyntaxException e) {
			importState.operation
					.addError(NLS.bind(Messages.getString("ImageMediaSupport.bad_uri_archiving_recipe"), parmFile), e); //$NON-NLS-1$
		}
		return archivedRecipe;
	}

	private void updateOrCreateDerivedByRelation(String origId, String origUri, String derivId, String derivUri,
			String recipe, boolean archiveRecipe, Date creationDate, Date lastModification, String tool,
			String parmFile, Collection<Object> toBeStored, Collection<Object> toBeDeleted) {
		IDbManager dbManager = Core.getCore().getDbManager();
		// First check if inverse relationship
		// exists
		List<DerivedByImpl> set = dbManager.obtainObjects(DerivedByImpl.class, false, "derivative", origId, //$NON-NLS-1$
				QueryField.EQUALS, "original", //$NON-NLS-1$
				derivId, QueryField.EQUALS);
		if (set.isEmpty()) {
			set = dbManager.obtainObjects(DerivedByImpl.class, false, "original", //$NON-NLS-1$
					origId, QueryField.EQUALS, "derivative", //$NON-NLS-1$
					derivId, QueryField.EQUALS);
			importState.allDeletedRelations.addAll(set);
			toBeDeleted.addAll(set);
			if (origUri != origId) {
				set = dbManager.obtainObjects(DerivedByImpl.class, false, "original", //$NON-NLS-1$
						origUri, QueryField.EQUALS, "derivative", //$NON-NLS-1$
						derivId, QueryField.EQUALS);
				importState.allDeletedRelations.addAll(set);
				toBeDeleted.addAll(set);
			}
			if (derivUri != derivId) {
				set = dbManager.obtainObjects(DerivedByImpl.class, false, "original", //$NON-NLS-1$
						origId, QueryField.EQUALS, "derivative", //$NON-NLS-1$
						derivUri, QueryField.EQUALS);
				importState.allDeletedRelations.addAll(set);
				toBeDeleted.addAll(set);
			}
			DerivedByImpl derivedBy;
			byte[] archivedRecipe = null;
			if (parmFile != null && archiveRecipe && importState.getConfiguration().archiveRecipes)
				archivedRecipe = archiveRecipe(parmFile, archivedRecipe);
			derivedBy = new DerivedByImpl(recipe, parmFile, tool,
					creationDate != null ? creationDate : lastModification, derivId, origId);
			if (archivedRecipe != null)
				derivedBy.setArchivedRecipe(archivedRecipe);
			toBeDeleted.remove(derivedBy);
			if (!toBeStored.contains(derivedBy))
				toBeStored.add(derivedBy);
		}
	}

	private boolean detectAndCreateDerivateByName(String uri, boolean isDng, boolean isRaw, Asset asset,
			AssetEnsemble ensemble, Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		boolean detected = false;
		String uri1 = Core.removeExtensionFromUri(uri);
		int q = uri1.lastIndexOf('/');
		String name = (q >= 0) ? uri1.substring(q + 1) : uri1;
		List<AssetImpl> family;
		List<AssetImpl> candidates = Core.getCore().getDbManager().obtainObjects(AssetImpl.class,
				QueryField.NAME.getKey(), Core.decodeUrl(name), QueryField.EQUALS);
		if (importState.getConfiguration().deriveRelations.equals(Constants.DERIVE_FOLDER)) {
			family = new ArrayList<AssetImpl>(candidates.size());
			for (AssetImpl cand : candidates)
				if (cand.getUri().startsWith(uri1))
					family.add(cand);
		} else
			family = candidates;
		for (AssetImpl sibling : family) {
			if (!asset.equals(sibling))
				continue;
			Asset orig = null;
			Asset deriv = null;
			AssetEnsemble derivEnsemble = null;
			String suri = sibling.getUri();
			boolean sIsDng = ImageConstants.isDng(suri);
			boolean sIsRaw = ImageConstants.isRaw(suri, false);
			if (isRaw) {
				if (!sIsRaw) {
					deriv = sibling;
					orig = asset;
				}
			} else if (isDng) {
				if (sIsRaw) {
					deriv = asset;
					derivEnsemble = ensemble;
					orig = sibling;
				} else if (!sIsDng) {
					deriv = sibling;
					orig = asset;
				}
			} else {
				if (sIsDng || sIsRaw) {
					deriv = asset;
					derivEnsemble = ensemble;
					orig = sibling;
				}
			}
			if (deriv != null && orig != null) {
				detected = true;
				updateOrCreateDerivedByRelation(orig.getStringId(), orig.getUri(), deriv.getStringId(), deriv.getUri(),
						null, false, null, deriv.getLastModification(), deriv.getSoftware(), null, toBeStored,
						toBeDeleted);
				propagateXmp(orig, deriv, derivEnsemble, toBeDeleted, toBeStored);
			}
		}
		return detected;
	}

	private void propagateXmp(Asset orig, Asset deriv, AssetEnsemble ensemble, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored) {
		if (importState.getConfiguration().applyXmp) {
			URI[] derivXmpURIs = ImportState.getXmpURIs(deriv);
			URI[] origXmpURIs = ImportState.getXmpURIs(orig);
			for (int i = 0; i < origXmpURIs.length; i++) {
				File origXmpFile = new File(origXmpURIs[i]);
				if (origXmpFile.exists()) {
					if (i < derivXmpURIs.length && new File(derivXmpURIs[i]).exists())
						continue;
					if (ensemble == null)
						ensemble = new AssetEnsemble(Core.getCore().getDbManager(), deriv, importState);
					importState.safeReadXmp(ensemble, deriv, origXmpURIs[i]);
					ensemble.store(toBeDeleted, toBeStored);
				}
			}
		}
	}

	private boolean detectAndCreateDerivateByProfile(boolean isDng, boolean isRaw, Asset asset, AssetEnsemble ensemble,
			Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		Date oDate = asset.getDateTimeOriginal();
		if (oDate == null)
			return false;
		boolean detected = false;
		List<AssetImpl> family = Core.getCore().getDbManager().obtainObjects(AssetImpl.class,
				QueryField.EXIF_DATETIMEORIGINAL.getKey(), oDate, QueryField.EQUALS);
		for (AssetImpl sibling : family) {
			if (asset.equals(sibling))
				continue;
			if (asset.getExposureTime() != sibling.getExposureTime())
				continue;
			if (!isEqual(asset.getModel(), sibling.getModel()))
				continue;
			if (!isEqual(asset.getMake(), sibling.getMake()))
				continue;
			if (asset.getFNumber() != sibling.getFNumber())
				continue;
			if (asset.getFlashFired() != sibling.getFlashFired())
				continue;
			if (asset.getFocalLength() != sibling.getFocalLength())
				continue;
			if (!Arrays.equals(asset.getIsoSpeedRatings(), sibling.getIsoSpeedRatings()))
				continue;
			Asset orig = null;
			Asset deriv = null;
			AssetEnsemble derivEnsemble = null;
			String suri = sibling.getUri();
			boolean sIsDng = ImageConstants.isDng(suri);
			boolean sIsRaw = ImageConstants.isRaw(suri, false);
			if (isRaw) {
				if (!sIsRaw) {
					deriv = sibling;
					orig = asset;
				}
			} else if (isDng) {
				if (sIsRaw) {
					deriv = asset;
					derivEnsemble = ensemble;
					orig = sibling;
				} else if (!sIsDng) {
					deriv = sibling;
					orig = asset;
				}
			} else {
				if (sIsDng || sIsRaw) {
					deriv = asset;
					derivEnsemble = ensemble;
					orig = sibling;
				} else {
					Date assetLast = asset.getLastModification();
					Date siblingLastMod = sibling.getLastModification();
					if (assetLast != null && siblingLastMod != null && siblingLastMod.compareTo(assetLast) > 0) {
						deriv = sibling;
						orig = asset;
					} else {
						deriv = asset;
						derivEnsemble = ensemble;
						orig = sibling;
					}
				}
			}
			if (deriv != null && orig != null) {
				detected = true;
				updateOrCreateDerivedByRelation(orig.getStringId(), orig.getUri(), deriv.getStringId(), deriv.getUri(),
						null, false, null, deriv.getLastModification(), deriv.getSoftware(), null, toBeStored,
						toBeDeleted);
				propagateXmp(orig, deriv, derivEnsemble, toBeDeleted, toBeStored);
			}
		}
		return detected;
	}

	private static boolean isEqual(String s1, String s2) {
		if (s1 == s2)
			return true;
		if (s1 == null && s2 != null)
			return false;
		return s1.equals(s2);
	}

	private boolean createImageEntry(final File originalFile, URI uri, String extension, boolean isConverted,
			AssetEnsemble ensemble, ZImage image, byte[] oldThumbnail, IExifLoader exifTool, Recipe recipe, Date now,
			Collection<Object> toBeStored, Collection<Object> toBeDeleted, IProgressMonitor monitor) {
		Asset asset = ensemble.getAsset();
		String mime = asset.getMimeType();
		if (mime == null || mime.isEmpty())
			asset.setMimeType(ImageConstants.getMimeMap().get(extension));
		int assetStatus = asset.getStatus();
		if (assetStatus < Constants.STATE_DEVELOPED) {
			assetStatus = Constants.STATE_DEVELOPED;
			if (isConverted) {
				if (recipe != null)
					asset.setStatus(assetStatus = Constants.STATE_CORRECTED);
				else
					assetStatus = (extension.equals("dng") ? Constants.STATE_CONVERTED //$NON-NLS-1$
							: Constants.STATE_RAW);
			}
		} else if (assetStatus == Constants.STATE_RAW && recipe != null)
			asset.setStatus(assetStatus = Constants.STATE_CORRECTED);
		ensemble.resetEnsemble(assetStatus);
		if (importState.analogProperties != null)
			ensemble.setAnalogProperties(importState.analogProperties);
		if (recipe == Recipe.NULL)
			recipe = null;
		if (recipe != null) {
			List<Recipe.Derivative> derivatives = recipe.getDerivatives();
			if (derivatives != null) {
				for (Recipe.Derivative derivative : derivatives) {
					try {
						String section = null;
						if (derivative.id != null && derivative.id.length() > 0)
							section = NLS.bind(Messages.getString("ImageMediaSupport.section"), derivative.id); //$NON-NLS-1$
						List<AssetImpl> derivedAssets = Core.getCore().getDbManager()
								.obtainAssetsForFile(new URI(derivative.url));
						if (!derivedAssets.isEmpty()) {
							AssetImpl deriv = derivedAssets.get(0);
							updateOrCreateDerivedByRelation(asset.getStringId(), asset.getUri(), deriv.getStringId(),
									deriv.getUri(), section, true, derivative.creationDate, deriv.getLastModification(),
									recipe.getTool() != null ? recipe.getTool() : deriv.getSoftware(),
									recipe.getLocation(), toBeStored, toBeDeleted);
						} else
							updateOrCreateDerivedByRelation(asset.getStringId(), asset.getUri(), derivative.url,
									derivative.url, section, true, derivative.creationDate, new Date(0L),
									recipe.getTool(), recipe.getLocation(), toBeStored, toBeDeleted);
					} catch (URISyntaxException e) {
						importState.operation
								.addWarning(NLS.bind(Messages.getString("ImageMediaSupport.bad_derivative_URL"), //$NON-NLS-1$
										derivative.url, asset.getName()), e);
					}
				}
			}
		}
		if (!importState.processExifData(ensemble, originalFile, true))
			return false;
		if (importState.processSidecars())
			importState.processXmpSidecars(uri, monitor, ensemble);
		MakerSupport makerSupport = MakerSupport.getMakerSupport(asset.getMake());
		try {
			if (makerSupport != null)
				makerSupport.processFaceData(ensemble, exifTool);
			else {
				Map<String, String> metadata = exifTool.getMetadata();
				String numPositions = metadata.get("FacesDetected"); //$NON-NLS-1$
				if (numPositions != null)
					asset.setNoPersons(Math.max(asset.getNoPersons(), Integer.parseInt(numPositions)));
			}
		} catch (NumberFormatException e) {
			importState.operation.addError(NLS.bind(Messages.getString("ImageMediaSupport.bad_face_data"), //$NON-NLS-1$
					asset.getName()), e);
		}
		processPicasaFaceData(originalFile, ensemble);
		ensemble.cleanUp(now);
		if (oldThumbnail != null) {
			asset.setJpegThumbnail(oldThumbnail);
			return true;
		}
		try {
			if (image != null) {
				ICC_Profile profile = (image.isSRGB() || asset.getProfileDescription() == null) ? null
						: exifTool.getICCProfile();
				ColorConvertOp op = ImageActivator.getDefault().computeColorConvertOp(profile, ImageConstants.SRGB);
				UnsharpMask umask = ImageActivator.getDefault().computeSharpenOp(importState.meta.getSharpen());
				int angle = importState.getThumbAngle(asset, isConverted);
				int height = twidth / 4 * 3;
				int origWidth = image.width;
				if (image.height > origWidth) {
					int www = height;
					height = twidth;
					twidth = www;
				}
				double scale = image.setScaling(twidth, height, false, ImportState.MCUWidth, null); //,
//						ZImage.SCALE_DEFAULT);
				if (recipe != null)
					recipe.setScaling((float) scale);
				if (angle != 0)
					image.setRotation(angle, 1f, 1f);
				image.setOutputColorConvert(op);
				image.setOutputSharpening(umask);
				image.setRecipe(recipe, false);
				return importState.writeThumbnail(image, asset, angle);
			}
		} catch (OutOfMemoryError e) {
			importState.operation
					.addError(Messages.getString("ImageMediaSupport.Not_enough_memory_to_compute_thumbnail"), e); //$NON-NLS-1$
		}
		return false;
	}

	private void processPicasaFaceData(File originalFile, AssetEnsemble ensemble) {
		File iniFile = new File(originalFile.getParentFile(), PICASA_INI);
		if (iniFile.exists()) {
			IniReader iniReader = null;
			try {
				iniReader = new IniReader(iniFile, true);
			} catch (IOException e) {
				CoreActivator.getDefault().logError(Messages.getString("ImageMediaSupport.io_error_reading_picasa_ini"), //$NON-NLS-1$
						e);
			}
			if (iniReader != null) {
				String iniEntry = iniReader.getPropertyString(originalFile.getName(), "faces", null); //$NON-NLS-1$
				List<String> faces = Core.fromStringList(iniEntry, ";"); //$NON-NLS-1$
				int i = 0;
				for (String face : faces) {
					if (face.startsWith("rect64(")) { //$NON-NLS-1$
						int p = face.indexOf(",", 7); //$NON-NLS-1$
						if (p >= 0) {
							String rect64 = face.substring(7, p);
							while (rect64.endsWith(" ") //$NON-NLS-1$
									|| rect64.endsWith(")")) //$NON-NLS-1$
								rect64 = rect64.substring(0, rect64.length() - 1);
							String faceId = face.substring(p + 1).trim();
							MWGRegion region = ensemble.getRegion(AssetEnsemble.PICASA, ++i);
							try {
								region.setRect64(rect64);
							} catch (NumberFormatException e) {
								importState.operation
										.addError(NLS.bind(Messages.getString("ImageMediaSupport.bad_face_data"), //$NON-NLS-1$
												ensemble.getAsset().getName()), e);
							}
							region.setPicasaIniEntry(iniEntry);
							String faceName = iniReader.getPropertyString("Contacts2", faceId, null); //$NON-NLS-1$
							if (faceName != null) {
								int q = faceName.indexOf(';');
								if (q >= 0)
									faceName = faceName.substring(0, q);
								region.setName(faceName);
							}
						}
					}
				}
			}
		}
	}

	public int getPropertyFlags() {
		return IMediaSupport.PHOTO | IMediaSupport.EXHIBITION | IMediaSupport.SLIDESHOW | IMediaSupport.WEBGALLERY
				| IMediaSupport.KML | IMediaSupport.PDF;
	}

	public Image getIcon40() {
		return null;
	}

	public String getName() {
		return Messages.getString("ImageMediaSupport.Photo"); //$NON-NLS-1$
	}

	public void setName(String name) {
		// do nothing
	}

	public String[] getFileExtensions() {
		Set<String> allFormats = ImageConstants.getAllFormats();
		return allFormats.toArray(new String[allFormats.size()]);
	}

	public boolean testProperty(int flags) {
		return (flags & getPropertyFlags()) == flags;
	}

	public void transferExtension(Asset sourceAsset, Asset targetAsset) {
		// do nothing
	}

	public void resetExtension(Asset asset) {
		// do nothing
	}

	public boolean setFieldValue(QueryField qfield, Asset asset, Object value) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return false;
	}

	public MediaExtension getMediaExtension(Asset asset) {
		return null;
	}

	public boolean handles(String key) {
		return false;
	}

	public Object getFieldValue(QueryField qfield, MediaExtension ext) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return null;
	}

	public Set<String> query(IDbManager dbManager, String field, int relation, Object value, Set<String> idSet) {
		return idSet;
	}

	public Class<? extends MediaExtension> getExtensionType() {
		return null;
	}

	public String getFieldName(String key) {
		return null;
	}

	public boolean resetBag(QueryField queryField, Asset obj) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		// do nothing
		return false;
	}

	public String getPlural() {
		return Messages.getString("ImageMediaSupport.photos"); //$NON-NLS-1$
	}

	public void setPlural(String plural) {
		// do nothing
	}

	public File getMediaFolder(File file) {
		return file;
	}

	public boolean undoImport(Asset asset, Set<Object> toBeDeleted, List<Object> toBeStored) {
		// Delete the relationships, too.
		if (!importState.allDeletedAssets.contains(asset)) {
			IDbManager dbManager = Core.getCore().getDbManager();
			String assetId = asset.getStringId();
			toBeDeleted.addAll(dbManager.obtainObjects(DerivedByImpl.class, "derivative", //$NON-NLS-1$
					assetId, QueryField.EQUALS));
			toBeDeleted.addAll(dbManager.obtainObjects(DerivedByImpl.class, "original", //$NON-NLS-1$
					assetId, QueryField.EQUALS));
			toBeDeleted.addAll(dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
					assetId, QueryField.EQUALS));
		}
		return false;
	}

	@Override
	public void setMimeMap(Map<String, String> mimeMap) {
		// not for images
	}

	@Override
	public void setCollectionId(String collectionID) {
		// not for images
	}

}
