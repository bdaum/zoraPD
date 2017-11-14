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
 * (c) 2009-2011 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.LastDeviceImportImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.operations.AbstractImportOperation;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class ImportOperation extends AbstractImportOperation {
	static final SimpleDateFormat dfYear = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
	static final SimpleDateFormat dfShort = new SimpleDateFormat("yyyy-MM"); //$NON-NLS-1$
	static final SimpleDateFormat dfLong = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private FileNameExtensionFilter filenameFilter;

	private FileInput fileInput;
	private URI[] uris;
	private Date lastDeviceImportDate = new Date();
	private Date previousImport;
	private final GregorianCalendar cal = new GregorianCalendar();
	private File[] folders;
	private Set<IMediaSupport> contributors = new HashSet<IMediaSupport>(5);
	private ImageMediaSupport imageMediaSupport;

	public ImportOperation(FileInput fileInput, ImportConfiguration configuration, AnalogProperties properties,
			File[] folders) {
		this(NLS.bind(Messages.getString("ImportOperation.Import_operation"), fileInput.getName(), //$NON-NLS-1$
				fileInput.size() > 1 ? ",..." : ""), configuration, null, properties, Constants.FILESOURCE_UNKNOWN); //$NON-NLS-1$ //$NON-NLS-2$
		this.folders = folders;
		this.fileInput = fileInput;
	}

	public ImportOperation(ImportFromDeviceData importData, ImportConfiguration configuration, int fileSource) {
		this(importData.isMedia() ? Messages.getString("ImportOperation.Import_from_device") //$NON-NLS-1$
				: Messages
						.getString("ImportOperation.import_new_folder_structure"), //$NON-NLS-1$
				configuration, importData, null, fileSource);
		this.fileInput = importData.getFileInput();
	}

	public ImportOperation(String name, FileInput fileInput, ImportConfiguration configuration) {
		this(name, configuration, null, null, Constants.FILESOURCE_UNKNOWN);
		this.fileInput = fileInput;
	}

	public ImportOperation(String name, URI[] uris, ImportConfiguration configuration) {
		this(name, configuration, null, null, Constants.FILESOURCE_UNKNOWN);
		this.uris = uris;

	}

	private ImportOperation(String name, ImportConfiguration configuration, ImportFromDeviceData importData,
			AnalogProperties properties, int fileSource) {
		super(name);
		importState = new ImportState(configuration, importData, properties, this, fileSource);
		filenameFilter = CoreActivator.getDefault().getFilenameExtensionFilter();
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		importState.info = info;
		try {
			int n = 0;
			if (fileInput != null)
				n += fileInput.size();
			if (uris != null)
				n += uris.length;
			List<URI> allFiles = new ArrayList<URI>(n);
			listFiles(fileInput, uris, allFiles);
			if (allFiles.isEmpty() && !isSilent()) {
				IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
				if (errorHandler != null)
					errorHandler.showWarning(Messages.getString("ImportOperation.file_import"), //$NON-NLS-1$
							Messages.getString("ImportOperation.no_supported_file_format"), //$NON-NLS-1$
							info);
			}
			importState.nFiles = allFiles.size();
			if (importState.importFromDeviceData != null) {
				final int skipPolicy = importState.importFromDeviceData.getSkipPolicy();
				if (skipPolicy == Constants.SKIP_RAW_IF_JPEG || skipPolicy == Constants.SKIP_JPEG_IF_RAW
						|| importState.importFromDeviceData.getExifTransferPrefix() == null
						|| !importState.importFromDeviceData.getExifTransferPrefix().isEmpty()) {
					Collections.sort(allFiles, new Comparator<URI>() {
						public int compare(URI o1, URI o2) {
							String p1 = o1.toString();
							String p2 = o2.toString();
							String e1 = ""; //$NON-NLS-1$
							String e2 = ""; //$NON-NLS-1$
							int q = p1.lastIndexOf('/');
							int p = p1.lastIndexOf('.');
							if (p > q) {
								e1 = p1.substring(p);
								p1 = p1.substring(0, p);
							}
							q = p2.lastIndexOf('/');
							p = p2.lastIndexOf('.');
							if (p > q) {
								e2 = p2.substring(p);
								p2 = p2.substring(0, p);
							}
							int ret = p1.compareTo(p2);
							if (ret != 0)
								return ret;
							if (ImageConstants.isJpeg(e1.toLowerCase()))
								return skipPolicy == Constants.SKIP_RAW_IF_JPEG ? -1 : 1;
							if (ImageConstants.isJpeg(e2.toLowerCase()))
								return skipPolicy == Constants.SKIP_RAW_IF_JPEG ? 1 : -1;
							return e1.compareTo(e2);
						}
					});
				} else
					Collections.sort(allFiles);
			}
			final Meta meta = dbManager.getMeta(true);
			if (!allFiles.isEmpty()) {
				int work = IMediaSupport.IMPORTWORKSTEPS * allFiles.size();
				final boolean userImport = !importState.getConfiguration().isSynchronize && !isSilent();
				if (userImport || meta.getCumulateImports()) {
					init(aMonitor, work + 1);
					cal.setTime(meta.getLastImport());
					int year = cal.get(Calendar.YEAR);
					meta.setLastImport(importState.importDate);
					cal.setTime(importState.importDate);
					if (year != cal.get(Calendar.YEAR))
						meta.setLastYearSequenceNo(0);
					final String description = createImportDescription(userImport);
					if (storeSafely(() -> {
						previousImport = dbManager.createLastImportCollection(importState.importDate, !userImport,
								description);
						dbManager.store(meta);
					}, 1))
						fireStructureModified();
				} else
					init(aMonitor, work);
				importFiles(aMonitor, allFiles, info);
				List<Object> toBeStored = new ArrayList<Object>();
				if (folders != null && meta.getAutoWatch())
					updateWatchedFolders(meta, toBeStored);
				if (importState.importFromDeviceData != null && fileInput.size() > 0 && lastDeviceImportDate != null) {
					String key = importState.importFromDeviceData.isMedia() ? fileInput.getVolume()
							: fileInput.getAbsolutePath();
					if (key != null) {
						if (meta.getLastDeviceImport() == null)
							meta.setLastDeviceImport(new HashMap<String, LastDeviceImport>());
						LastDeviceImport lastImport = meta.getLastDeviceImport(key);
						if (lastImport == null) {
							lastImport = new LastDeviceImportImpl(key, lastDeviceImportDate.getTime(), null, null);
							meta.putLastDeviceImport(lastImport);
						} else
							lastImport.setTimestamp(lastDeviceImportDate.getTime());
						toBeStored.add(lastImport);
					}
				}
				toBeStored.add(meta);
				if (storeSafely(null, 1, toBeStored.toArray())) {
					fireAssetsModified(null, null);
					if (importState.getConfiguration().showImported && !isSilent()) {
						SmartCollectionImpl coll = dbManager.obtainById(SmartCollectionImpl.class,
								Constants.LAST_IMPORT_ID);
						if (coll != null)
							fireCatalogSelection(new StructuredSelection(coll), true);
					}
				}
			} else
				init(aMonitor, 0);
		} finally {
			if (importState.importFromDeviceData != null && importState.importFromDeviceData.isRemoveMedia()
					&& fileInput.size() > 0)
				try {
					BatchUtilities.ejectMedia(fileInput.getAbsolutePath());
					final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
					if (errorHandler != null)
						errorHandler.showInformation(Constants.APPLICATION_NAME,
								Messages.getString("ImportOperation.media_have_been_ejected"), //$NON-NLS-1$
								info);
				} catch (IOException e) {
					addWarning(Messages.getString("ImportOperation.io_error_ejecting"), e); //$NON-NLS-1$
				}
			importState.importFinished();
		}
		return close(info);
	}

	private String createImportDescription(boolean userImport) {
		if (userImport) {
			String user = System.getProperty("user.name"); //$NON-NLS-1$
			if (importState.importFromDeviceData != null) {
				if (importState.importFromDeviceData.isMedia())
					return NLS.bind(Messages.getString("ImportOperation.user_import_device"), user); //$NON-NLS-1$
				return NLS.bind(Messages.getString("ImportOperation.user_import_new_structure"), user); //$NON-NLS-1$
			}
			return NLS.bind(Messages.getString("ImportOperation.user_import"), user); //$NON-NLS-1$
		}
		SimpleDateFormat df = null;
		Date importDate = importState.importDate;
		if (importState.importFromDeviceData != null) {
			File[] dcims = importState.importFromDeviceData.getDcims();
			if (dcims.length > 0) {
				df = new SimpleDateFormat(Messages.getString("ImportOperation.MMM_dd_yyyy")); //$NON-NLS-1$
				return NLS.bind(Messages.getString("ImportOperation.import_transfer"), //$NON-NLS-1$
						dcims[0].getParent(), df.format(importDate));
			}
			return ""; //$NON-NLS-1$
		}
		String timeline = importState.getConfiguration().timeline;
		if (timeline.equals(Meta_type.timeline_year))
			df = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
		else if (timeline.equals(Meta_type.timeline_month))
			df = new SimpleDateFormat(Messages.getString("ImportOperation.MMM_yyyy")); //$NON-NLS-1$
		else if (timeline.equals(Meta_type.timeline_day))
			df = new SimpleDateFormat(Messages.getString("ImportOperation.MMM_dd_yyyy")); //$NON-NLS-1$
		else if (timeline.equals(Meta_type.timeline_week))
			df = new SimpleDateFormat(Messages.getString("ImportOperation.ww_yyyy")); //$NON-NLS-1$
		else if (timeline.equals(Meta_type.timeline_weekAndDay))
			df = new SimpleDateFormat(Messages.getString("ImportOperation.EEE_ww_yyyy")); //$NON-NLS-1$
		if (df != null)
			return NLS.bind(Messages.getString("ImportOperation.watched_folder_imports_for"), df.format(importDate)); //$NON-NLS-1$
		return Messages.getString("ImportOperation.watched_folder_imports"); //$NON-NLS-1$
	}

	private void updateWatchedFolders(Meta meta, List<Object> toBeStored) {
		CoreActivator activator = CoreActivator.getDefault();
		IVolumeManager volumeManager = activator.getVolumeManager();
		lp: for (File folder : folders) {
			for (String folderId : meta.getWatchedFolder()) {
				WatchedFolder observedFolder = activator.getObservedFolder(folderId);
				if (observedFolder != null) {
					File watchedFolder = volumeManager.findExistingFile(observedFolder.getUri(),
							observedFolder.getVolume());
					if (watchedFolder != null) {
						if (watchedFolder.equals(folder)) {
							if (!observedFolder.getRecursive()) {
								observedFolder.setRecursive(true);
								toBeStored.add(observedFolder);
							}
							continue lp;
						}
						if (observedFolder.getRecursive()) {
							File parent = folder.getParentFile();
							while (parent != null) {
								if (parent.equals(watchedFolder))
									continue lp;
								parent = parent.getParentFile();
							}
						}
					}
				}
			}
			WatchedFolderImpl newWatchedFolder = new WatchedFolderImpl(folder.toURI().toString(),
					volumeManager.getVolumeForFile(folder), importState.importDate.getTime(), true, null, false, null,
					false, 0, null, 2, null, null, Constants.FILESOURCE_DIGITAL_CAMERA);
			toBeStored.add(newWatchedFolder);
			meta.addWatchedFolder(newWatchedFolder.getStringId());
			activator.putObservedFolder(newWatchedFolder);
		}
	}

	private void listFiles(FileInput in, URI[] allUris, List<URI> allFiles) {
		if (in != null)
			in.getURIs(filenameFilter, allFiles);
		if (allUris != null)
			for (URI uri : allUris)
				allFiles.add(uri);
	}

	private void importFiles(IProgressMonitor aMonitor, List<URI> allUris, IAdaptable info) {
		int cnt = 0;
		importState.importedAssets = new HashSet<Asset>(allUris.size());
		File tempFile = null;
		Ticketbox box = new Ticketbox();
		try {
			for (URI uri : allUris) {
				if (tempFile != null)
					tempFile.delete();
				URI remote = null;
				File file = null;
				if (Constants.FILESCHEME.equals(uri.getScheme())) {
					file = new File(uri);
					lastDeviceImportDate.setTime(file.lastModified() + 1);
				} else {
					try {
						file = box.download(uri);
						tempFile = file;
						remote = uri;
					} catch (MalformedURLException e) {
						addError(NLS.bind(Messages.getString("ImportOperation.not_a_valid_url"), uri), //$NON-NLS-1$
								e);
					} catch (IOException e) {
						addError(NLS.bind(Messages.getString("ImportOperation.transfer_from_url_failed"), uri), e); //$NON-NLS-1$
					}
				}
				if (file != null) {
					String extension = Core.getFileExtension(file.toURI().toString());
					if (importState.skipFile(file, extension))
						aMonitor.worked(IMediaSupport.IMPORTWORKSTEPS);
					else {
						aMonitor.subTask(NLS.bind(Messages.getString("ImportOperation.x_of_y"), importState.importNo, //$NON-NLS-1$
								importState.nFiles));
						IMediaSupport mediaSupport = getMediaSupport(extension.toUpperCase());
						contributors.add(mediaSupport);
						try {
							int icnt = mediaSupport.importFile(file, extension, importState, aMonitor, remote);
							cnt += Math.abs(icnt);
							importState.nextPicture(icnt);
						} catch (Exception e) {
							addError(
									NLS.bind(
											importState.isSilent()
													? Messages.getString(
															"ImportOperation.error_when_importing_concurrent_access") //$NON-NLS-1$
													: Messages.getString("ImportOperation.Error_importing"), //$NON-NLS-1$
											uri),
									e, uri, monitor);
						}
					}
					if (aMonitor.isCanceled())
						break;
					yieldRule();
				}
			}
		} finally {
			if (tempFile != null)
				tempFile.delete();
			CoreActivator.getDefault().getFileWatchManager().stopIgnoring(opId);
			box.endSession();
			for (Asset a : importState.allDeletedAssets)
				dbManager.markSystemCollectionsForPurge(a);
			if (cnt == 0 && previousImport != null) {
				final List<Object> toBeStored = new ArrayList<Object>();
				final Set<Object> toBeDeleted = new HashSet<Object>();
				boolean changed = Utilities.popLastImport(toBeStored, toBeDeleted, previousImport, false);
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				previousImport = null;
				if (changed)
					fireStructureModified();
			}
		}
	}

	private IMediaSupport getMediaSupport(String format) {
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(format);
		if (mediaSupport != null)
			return mediaSupport;
		if (imageMediaSupport == null)
			imageMediaSupport = new ImageMediaSupport();
		return imageMediaSupport;
	}

	@Override
	public boolean canUndo() {
		return importState.canUndo;
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		importState.info = info;
		List<Asset> assets = obtainImportedAssets();
		initUndo(aMonitor,
				assets.size() + importState.allDeletedAssets.size() + importState.allDeletedRelations.size() + 3);
		openIndexWriter();
		final List<Object> toBeStored = new ArrayList<Object>();
		final Set<Object> toBeDeleted = new HashSet<Object>();
		CoreActivator activator = CoreActivator.getDefault();
		int i = 0;
		try {
			toBeStored.addAll(importState.allDeletedGhosts);
			aMonitor.worked(1);
			for (Asset asset : assets) {
				// Delete transmitted files, too, when import from camera is
				// undone.
				if (importState.importFromDeviceData != null)
					deletePhysicalFile(asset);
				new AssetEnsemble(dbManager, asset, importState).delete(toBeDeleted, toBeStored);
				// Perform specific media type undo
				for (IMediaSupport contributor : contributors)
					contributor.undoImport(asset, toBeDeleted, toBeStored);
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				if (!importState.allDeletedAssets.contains(asset))
					deleteIndexEntry(asset.getStringId());
				dbManager.markSystemCollectionsForPurge(asset);
				if (i == 0)
					fireAssetsModified(null, null);
				if (i++ == 16)
					i = 0;
			}
			storeSafely(null, importState.allDeletedAssets.size(), importState.allDeletedAssets.toArray());
			storeSafely(null, importState.allDeletedRelations.size(), importState.allDeletedRelations.toArray());
			if (!importState.allDeletedAssets.isEmpty() && !dbManager.getMeta(true).getNoIndex()) {
				Job job = activator.getDbFactory().getLireService(true).createIndexingJob(importState.allDeletedAssets,
						true, -1, 0, activator.isNoProgress());
				if (job != null)
					job.schedule();
			}
		} finally {
			closeIndex();
			activator.getFileWatchManager().stopIgnoring(opId);
			toBeDeleted.clear();
			toBeStored.clear();
			if (previousImport != null) {
				Utilities.popLastImport(toBeStored, toBeDeleted, previousImport, true);
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
			}
			if (i > 0)
				fireAssetsModified(null, null);
			fireStructureModified();
		}
		return close(info);
	}

	private void deletePhysicalFile(Asset asset) {
		try {
			URI uri = new URI(asset.getUri());
			if (uri.getPath().startsWith(importState.importFromDeviceData.getTargetDir()))
				new File(uri).delete();
		} catch (URISyntaxException e) {
			// ignore
		}
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	public int getExecuteProfile() {
		return importState.getExecuteProfile();
	}

	public int getUndoProfile() {
		return importState.getUndoProfile();
	}

	@Override
	public boolean isSilent() {
		return importState.isSilent();
	}

	public List<Asset> obtainImportedAssets() {
		return importState.obtainImportedAssets();
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		//do nothing
	}

}
