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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.FileInfo;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.core.trash.FileLocationBackup;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class MoveOperation extends DbOperation {

	private static final int OVERWRITE = 0;
	private static final int IGNORE = 1;
	private static final int RENAME = 2;
	private static final int CANCEL = 3;
	private List<Asset> assets;
	private File folder;
	private boolean ignoreAll;
	private boolean overwriteAll;
	private boolean renameAll;
	protected FileWatchManager fileWatchManager = CoreActivator.getDefault().getFileWatchManager();
	private Date now;
	private final GregorianCalendar cal = new GregorianCalendar();
	protected Date previousImport;

	public MoveOperation(List<Asset> assets, File folder) {
		super(Messages.getString("MoveOperation.Move_to_folder")); //$NON-NLS-1$
		this.assets = assets;
		this.folder = folder;
	}

	@Override
	public IStatus execute(final IProgressMonitor aMonitor, final IAdaptable info) throws ExecutionException {
		for (IRelationDetector detector : info.getAdapter(IRelationDetector[].class))
			detector.reset();
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		init(aMonitor, assets.size() + 2);
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		final Ticketbox box = new Ticketbox();
		try {
			for (Asset asset1 : assets) {
				if (asset1.getFileState() == IVolumeManager.PEER) {
					AssetOrigin assetOrigin = null;
					String assetId = asset1.getStringId();
					if (peerService != null)
						assetOrigin = peerService.getAssetOrigin(assetId);
					FileInfo fileInfo = null;
					if (assetOrigin != null) {
						try {
							fileInfo = peerService.getFileInfo(assetOrigin, asset1.getUri(), asset1.getVolume());
							if (fileInfo != null) {
								final FileLocationBackup backup = new FileLocationBackup(opId, assetId, null, false,
										true);
								File newFile = copyRemoteFile(aMonitor, asset1, assetOrigin, fileInfo, info,
										fileWatchManager);
								if (newFile == null) {
									newFile = fileInfo.getFile();
									backup.setFileMoved(false);
								}
								final List<Object> toBeStored = new ArrayList<Object>();
								toBeStored.add(backup);
								copyRemoteAsset(asset1, assetOrigin, toBeStored, fileInfo, newFile);
								if (!storeSafely(() -> {
									dbManager.storeTrash(backup);
									for (Object o : toBeStored)
										dbManager.store(o);
								}, 1))
									return close(info);
								dbManager.createFolderHierarchy(asset1);
							} else
								errands.add(assetId);
						} catch (ConnectionLostException e) {
							errands.add(assetId);
						}
					} else
						errands.add(assetId);
				} else {
					final Asset a = asset1;
					final URI file = volumeManager.findFile(asset1);
					if (file != null) {
						if (volumeManager.findExistingFile(asset1, true) != null) {
							dbManager.markSystemCollectionsForPurge(a);
							final List<Object> toBeStored = new ArrayList<Object>(1);
							final List<Object> toBeTrashed = new ArrayList<Object>(1);
							moveAsset(box, a, file, folder, info, aMonitor, errands, fileWatchManager, toBeStored,
									toBeTrashed);
							if (!storeSafely(new Runnable() {
								public void run() {
									for (Object o : toBeTrashed)
										dbManager.storeTrash(o);
									for (Object o : toBeStored)
										dbManager.store(o);
								}
							}, 1))
								return close(info);
							IRelationDetector[] detectors = info.getAdapter(IRelationDetector[].class);
							for (IRelationDetector detector : detectors)
								try {
									if (detector.moveAsset(asset1, new File(file), new File(new URI(asset1.getUri())),
											info, opId))
										break;
								} catch (URISyntaxException e) {
									break;
								}
							dbManager.createFolderHierarchy(asset1);
						} else {
							String volume = asset1.getVolume();
							if (volume != null && !volume.isEmpty())
								volumes.add(volume);
							errands.add(file.toString());
							if (aMonitor.isCanceled())
								return abort();
						}
					}
				}
			}
		} finally {
			fileWatchManager.stopIgnoring(opId);
			box.endSession();
		}
		fireAssetsModified(null, null);
		fireStructureModified();
		if (!errands.isEmpty()) {
			final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
			if (errorHandler != null)
				errorHandler.showInformation(Messages.getString("MoveOperation.Unable_to_move"), //$NON-NLS-1$
						Ticketbox.computeErrorMessage(errands, volumes), info);
		}
		assets = null;
		return close(info);
	}

	private File copyRemoteFile(final IProgressMonitor aMonitor, Asset movedAsset, AssetOrigin assetOrigin,
			FileInfo fileInfo, IAdaptable info, FileWatchManager fwManager) throws ConnectionLostException {
		try {
			File file = fileInfo.getFile();
			String fileName = file.getName();
			File newFile = new File(folder, fileName);
			String remoteHost = assetOrigin.getHost();
			String localHost = peerService.getHost();
			if (remoteHost.equals(localHost) && file.equals(newFile))
				return newFile;
			if (newFile.exists()) {
				if (newFile.exists()) {
					switch (promptForOverwrite(newFile, info)) {
					case IGNORE:
						return null;
					case RENAME:
						newFile = makeUnique(newFile);
						break;
					case OVERWRITE:
						newFile.delete();
						break;
					default:
						aMonitor.setCanceled(true);
						return null;
					}
				}
			}
			SubMonitor sub = SubMonitor.convert(aMonitor, 1);
			fileWatchManager.ignore(newFile, opId);
			URI uri = file.toURI();
			peerService.transferRemoteFile(sub, assetOrigin, fileInfo, newFile);
			URI[] xmpOrigURIs = Core.getSidecarURIs(uri);
			URI newURI = newFile.toURI();
			URI[] xmpTargetURIs = Core.getSidecarURIs(newURI);
			String volume = movedAsset.getVolume();
			long modmax = -1;
			for (int i = 0; i < xmpOrigURIs.length; i++) {
				File xmpTarget = new File(xmpTargetURIs[i]);
				xmpTarget.delete();
				fileWatchManager.ignore(xmpTarget, opId);
				sub = SubMonitor.convert(aMonitor, 1);
				FileInfo xmpInfo = peerService.getFileInfo(assetOrigin, xmpOrigURIs[i].toString(), volume);
				peerService.transferRemoteFile(sub, assetOrigin, xmpInfo, xmpTarget);
				modmax = Math.max(modmax, xmpTarget.lastModified());
			}
			movedAsset.setXmpModifiedAt(modmax < 0 ? null : new Date(modmax));
			if (peerService.checkCredentials(IPeerService.VOICE, movedAsset.getSafety(), assetOrigin)) {
				String voiceFileURI = movedAsset.getVoiceFileURI();
				String voiceOrigURI = null;
				URI voiceTargetURI = null;
				if (".".equals(voiceFileURI)) { //$NON-NLS-1$
					URI u = Core.getVoicefileURI(file);
					voiceOrigURI = u == null ? null : u.toString();
					voiceTargetURI = Core.getVoicefileURI(newFile);
				} else {
					voiceOrigURI = voiceFileURI;
					volume = movedAsset.getVoiceVolume();
				}
				if (voiceOrigURI != null) {
					if (!voiceOrigURI.startsWith("?")) { //$NON-NLS-1$
						File voiceTarget = null;
						voiceTarget = new File(voiceTargetURI);
						voiceTarget.delete();
						fileWatchManager.ignore(voiceTarget, opId);
						sub = SubMonitor.convert(aMonitor, 1);
						FileInfo voiceInfo = peerService.getFileInfo(assetOrigin, voiceOrigURI, volume);
						peerService.transferRemoteFile(sub, assetOrigin, voiceInfo, voiceTarget);
						movedAsset.setVoiceFileURI("."); //$NON-NLS-1$
					}
				} else
					movedAsset.setVoiceFileURI(null);
			} else
				movedAsset.setVoiceFileURI(null);
			return newFile;
		} catch (IOException e) {
			addError(NLS.bind(Messages.getString("MoveOperation.io_error_remote_file"), //$NON-NLS-1$
					fileInfo.getFile()), e);
		} catch (DiskFullException e) {
			addError(NLS.bind(Messages.getString("MoveOperation.Moving_of_image_failed"), fileInfo.getFile()), e); //$NON-NLS-1$
		}
		return null;
	}

	private void copyRemoteAsset(Asset movedAsset, AssetOrigin assetOrigin, List<Object> toBeStored, FileInfo fileInfo,
			File newFile) throws ConnectionLostException {
		if (now == null) {
			now = new Date();
			createLastImport();
		}
		AssetImpl asset = new AssetImpl();
		AssetEnsemble.transferAssetData(movedAsset, asset, null);
		asset.setUri(newFile.toURI().toString());
		asset.setVolume(Core.getCore().getVolumeManager().getVolumeForFile(newFile));
		asset.setImportDate(now);
		asset.setImportedBy(System.getProperty("user.name")); //$NON-NLS-1$
		asset.setFileState(IVolumeManager.ONLINE);
		asset.setLastEditor(null);
		String assetId = asset.getStringId();
		toBeStored.add(asset);
		String relId = asset.getCreatorsContact_parent();
		if (relId != null) {
			Iterator<CreatorsContactImpl> it = peerService
					.obtainStructForAsset(assetOrigin, CreatorsContactImpl.class, assetId, true).iterator();
			if (it.hasNext()) {
				CreatorsContactImpl rel = it.next();
				String contactId = rel.getContact();
				if (contactId == null)
					asset.setCreatorsContact_parent(null);
				else {
					ContactImpl contact = peerService.obtainById(assetOrigin, ContactImpl.class, contactId);
					if (contact == null)
						asset.setCreatorsContact_parent(null);
					else {
						contact.setCreatorsContact_parent(null);
						Iterator<ContactImpl> cit = dbManager.queryByExample(contact).iterator();
						if (cit.hasNext()) {
							contact = cit.next();
							String creatorsContact_parent = contact.getCreatorsContact_parent();
							if (creatorsContact_parent != null) {
								CreatorsContactImpl lc = dbManager.obtainById(CreatorsContactImpl.class,
										creatorsContact_parent);
								if (lc != null)
									rel = lc;
								else
									rel.setContact(contactId);
							} else
								rel.setContact(contactId);
						} else {
							contact.setCreatorsContact_parent(rel.getStringId());
							toBeStored.add(contact);
						}
						rel.getAsset().clear();
						rel.addAsset(assetId);
						toBeStored.add(rel);
					}
				}
			} else
				asset.setCreatorsContact_parent(null);
		}
		relId = asset.getLocationCreated_parent();
		if (relId != null) {
			Iterator<LocationCreatedImpl> it = peerService
					.obtainStructForAsset(assetOrigin, LocationCreatedImpl.class, assetId, true).iterator();
			if (it.hasNext()) {
				LocationCreatedImpl rel = it.next();
				rel.getAsset().clear();
				String locationId = rel.getLocation();
				if (locationId == null)
					asset.setLocationCreated_parent(null);
				else {
					LocationImpl location = peerService.obtainById(assetOrigin, LocationImpl.class, locationId);
					if (location == null)
						asset.setLocationCreated_parent(null);
					else {
						location.setLocationCreated_parent(null);
						location.getLocationShown_parent().clear();
						Iterator<LocationImpl> cit = dbManager.queryByExample(location).iterator();
						if (cit.hasNext()) {
							location = cit.next();
							String locationCreated_parent = location.getLocationCreated_parent();
							if (locationCreated_parent != null) {
								LocationCreatedImpl lc = dbManager.obtainById(LocationCreatedImpl.class,
										locationCreated_parent);
								if (lc != null)
									rel = lc;
								else
									rel.setLocation(locationId);
							} else
								rel.setLocation(locationId);
						} else {
							location.setLocationCreated_parent(rel.getStringId());
							toBeStored.add(location);
						}
						rel.addAsset(assetId);
						toBeStored.add(rel);
					}
				}
			} else
				asset.setLocationCreated_parent(null);
		}
		List<String> relIds = asset.getLocationShown_parent();
		if (!relIds.isEmpty()) {
			Iterator<LocationShownImpl> it = peerService
					.obtainStructForAsset(assetOrigin, LocationShownImpl.class, assetId, true).iterator();
			if (it.hasNext()) {
				while (it.hasNext()) {
					LocationShownImpl r = it.next();
					LocationImpl location = peerService.obtainById(assetOrigin, LocationImpl.class, r.getStringId());
					if (location == null)
						relIds.remove(r.getStringId());
					else {
						location.getLocationShown_parent().clear();
						location.setLocationCreated_parent(null);
						Iterator<LocationImpl> cit = dbManager.queryByExample(location).iterator();
						if (cit.hasNext()) {
							location = cit.next();
							r.setLocation(location.getStringId());
						} else
							toBeStored.add(location);
						location.addLocationShown_parent(r.getStringId());
						r.setAsset(assetId);
						toBeStored.add(r);
					}
				}
			} else
				relIds.clear();
		}
		relIds = asset.getArtworkOrObjectShown_parent();
		if (!relIds.isEmpty()) {
			List<ArtworkOrObjectShownImpl> set = peerService.obtainStructForAsset(assetOrigin,
					ArtworkOrObjectShownImpl.class, assetId, true);
			if (set.isEmpty())
				relIds.clear();
			else {
				for (ArtworkOrObjectShownImpl r : set) {
					ArtworkOrObjectImpl artwork = peerService.obtainById(assetOrigin, ArtworkOrObjectImpl.class,
							r.getStringId());
					if (artwork == null)
						relIds.remove(r.getStringId());
					else {
						artwork.getArtworkOrObjectShown_parent().clear();
						List<ArtworkOrObjectImpl> cset = dbManager.queryByExample(artwork);
						if (cset.isEmpty()) {
							toBeStored.add(artwork);
						} else {
							artwork = cset.get(0);
							r.setArtworkOrObject(artwork.getStringId());
						}
						artwork.addArtworkOrObjectShown_parent(r.getStringId());
						r.setAsset(assetId);
						toBeStored.add(r);
					}
				}
			}
		}
	}

	private void createLastImport() {
		final Meta meta = dbManager.getMeta(true);
		cal.setTime(meta.getLastImport());
		int year = cal.get(Calendar.YEAR);
		meta.setLastImport(now);
		cal.setTime(now);
		if (year != cal.get(Calendar.YEAR))
			meta.setLastYearSequenceNo(0);
		if (storeSafely(() -> {
			previousImport = dbManager.createLastImportCollection(now, false,
					Messages.getString("MoveOperation.import_caused")); //$NON-NLS-1$
			dbManager.store(meta);
		}, 1))
			fireStructureModified();
	}

	private void moveAsset(Ticketbox box, Asset movedAsset, URI uri, File target, IAdaptable info,
			IProgressMonitor aMonitor, List<String> errands, FileWatchManager fwManager, List<Object> toBeStored,
			List<Object> toBeTrashed) {
		File file = null;
		if (uri != null) {
			try {
				file = box.obtainFile(uri);
			} catch (IOException e) {
				errands.add(uri.toString());
			}
		}
		String assetId = movedAsset.getStringId();
		if (file != null)
			try {
				Path path = new Path(file.getAbsolutePath());
				toBeTrashed.add(new FileLocationBackup(opId, assetId, box.isLocal() ? path : null, true, false));
				Path folderPath = new Path(target.getAbsolutePath());
				IPath newPath = folderPath.append(path.lastSegment());
				if (!newPath.equals(path)) {
					File newFile = newPath.toFile();
					if (newFile.exists()) {
						switch (promptForOverwrite(newFile, info)) {
						case IGNORE:
							return;
						case RENAME:
							newFile = makeUnique(newFile);
							break;
						case OVERWRITE:
							newFile.delete();
							break;
						default:
							aMonitor.setCanceled(true);
							return;
						}
					}
					URI[] xmpOrigURIs = Core.getSidecarURIs(uri);
					URI newURI = newFile.toURI();
					URI[] xmpTargetURIs = Core.getSidecarURIs(newURI);
					URI voiceOrigURI = null;
					URI voiceTargetURI = null;
					String voiceFileURI = movedAsset.getVoiceFileURI();
					if (".".equals(voiceFileURI)) { //$NON-NLS-1$
						voiceOrigURI = Core.getVoicefileURI(file);
						voiceTargetURI = Core.getVoicefileURI(newFile);
					}
					try {
						fwManager.moveFileSilently(file, newFile, opId, aMonitor);
						movedAsset.setUri(newURI.toString());
						toBeStored.add(movedAsset);
						for (int i = 0; i < xmpOrigURIs.length; i++) {
							File xmpFile = new File(xmpOrigURIs[i]);
							File xmpTarget = null;
							if (xmpFile.exists()) {
								xmpTarget = new File(xmpTargetURIs[i]);
								xmpTarget.delete();
								fwManager.moveFileSilently(xmpFile, xmpTarget, opId, aMonitor);
							}
						}
						if (voiceOrigURI != null) {
							File voiceFile = new File(voiceOrigURI);
							File voiceTarget = null;
							if (voiceFile.exists()) {
								voiceTarget = new File(voiceTargetURI);
								voiceTarget.delete();
								fwManager.moveFileSilently(voiceFile, voiceTarget, opId, aMonitor);
							}
						}
					} catch (IOException e) {
						addError(NLS.bind(Messages.getString("MoveOperation.Moving_of_image_failed"), file), e); //$NON-NLS-1$
					} catch (DiskFullException e) {
						addError(NLS.bind(Messages.getString("MoveOperation.Moving_of_image_failed"), file), e); //$NON-NLS-1$
					}
				}
			} finally {
				box.cleanup();
			}
		else
			toBeTrashed.add(new FileLocationBackup(opId, assetId, null, true, false));
	}

	private int promptForOverwrite(File newFile, IAdaptable info) {
		if (ignoreAll)
			return IGNORE;
		if (renameAll)
			return RENAME;
		if (overwriteAll)
			return OVERWRITE;
		int ret = 6;
		IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
		if (errorHandler != null)
			ret = errorHandler.showMessageDialog(Messages.getString("MoveOperation.Same_file_exists"), //$NON-NLS-1$
					null,
					NLS.bind(Messages.getString("MoveOperation.File_already_exists"), //$NON-NLS-1$
							newFile.getName()),
					MessageDialog.QUESTION,
					new String[] { Messages.getString("MoveOperation.Overwrite"), //$NON-NLS-1$
							Messages.getString("MoveOperation.Overwrite_all"), IDialogConstants.SKIP_LABEL, //$NON-NLS-1$
							Messages.getString("MoveOperation.Skip_all"), //$NON-NLS-1$
							Messages.getString("MoveOperation.Rename"), Messages.getString("MoveOperation.Rename_all"), //$NON-NLS-1$ //$NON-NLS-2$
							IDialogConstants.CANCEL_LABEL },
					0, info);
		switch (ret) {
		case 1:
			overwriteAll = true;
			//$FALL-THROUGH$
		case 0:
			return OVERWRITE;
		case 3:
			ignoreAll = true;
			//$FALL-THROUGH$
		case 2:
			return IGNORE;
		case 5:
			renameAll = true;
			//$FALL-THROUGH$
		case 4:
			return RENAME;
		default:
			return CANCEL;
		}
	}

	private static File makeUnique(File file) {
		String path = file.getAbsolutePath();
		int q = path.lastIndexOf('/');
		int p = path.lastIndexOf('.');
		String ext = ""; //$NON-NLS-1$
		if (p > q) {
			ext = path.substring(p);
			path = path.substring(0, p);
		}
		int i = 0;
		while (true) {
			File newFile = new File(path + "-" + (++i) + ext); //$NON-NLS-1$
			if (!newFile.exists())
				return newFile;
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

	@Override
	public IStatus undo(final IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		boolean changed = false;
		final List<Object> toBeStored = new ArrayList<Object>();
		final Set<Object> toBeDeleted = new HashSet<Object>();
		try {
			List<FileLocationBackup> set = dbManager.getTrash(FileLocationBackup.class, opId);
			initUndo(aMonitor, set.size());
			for (FileLocationBackup backup : set) {
				toBeDeleted.clear();
				toBeStored.clear();
				Asset asset = backup.restore(dbManager, opId, aMonitor, toBeStored, toBeDeleted);
				final FileLocationBackup t = backup;
				if (storeSafely(() -> {
					dbManager.deleteTrash(t);
					for (Object o1 : toBeDeleted)
						dbManager.delete(o1);
					for (Object o2 : toBeStored)
						dbManager.store(o2);
				}, 1) && asset != null)
					changed |= dbManager.createFolderHierarchy(asset);
			}
			fireAssetsModified(null, null);
		} finally {
			fileWatchManager.stopIgnoring(opId);
			if (now != null) {
				toBeDeleted.clear();
				toBeStored.clear();
				Utilities.popLastImport(toBeStored, toBeDeleted, previousImport, true);
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				changed = true;
			}
			if (changed)
				fireStructureModified();
		}
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.FILE;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.FILE;
	}

	@Override
	public int getPriority() {
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		//do nothing
	}

}
