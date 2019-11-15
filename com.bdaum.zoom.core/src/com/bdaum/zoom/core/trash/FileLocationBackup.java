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

package com.bdaum.zoom.core.trash;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Messages;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.program.DiskFullException;

/**
 * A trash item keeping an image asset and the image file location
 */
public class FileLocationBackup extends HistoryItem {

	protected IPath path;
	protected String assetId;
	private boolean fileMoved = true;
	private boolean peer;
	private Set<IIdentifiableObject> createdObjects;

	public FileLocationBackup() {
	}

	/**
	 * @param opId
	 *            - ID of operation that created the trash
	 * @param assetId
	 *            - ID of backed up asset
	 * @param path
	 *            - origin of moved image, null in the case of peer images
	 * @param fileMoved
	 *            - true if file had been moved from local location
	 * @param peer
	 *            - true if this represents a trashed peer item
	 */
	public FileLocationBackup(String opId, String assetId, IPath path, boolean fileMoved, boolean peer) {
		super(opId);
		this.assetId = assetId;
		this.path = path;
		this.fileMoved = fileMoved;
		this.peer = peer;
	}

	/**
	 * Restores the trash item
	 *
	 * @param dbManager
	 *            - database manager
	 * @param anOpId
	 *            - operation ID
	 * @param monitor
	 *            -progress monitor or null
	 * @param trashToBeDeleted
	 * @param toBeStored
	 * @return - restored asset
	 */
	public Asset restore(IDbManager dbManager, String anOpId, IProgressMonitor monitor, List<Object> toBeStored,
			Set<Object> toBeDeleted) {
		if (createdObjects != null)
			toBeDeleted.addAll(createdObjects);
		Asset asset = dbManager.obtainAsset(assetId);
		if (asset != null) {
			CoreActivator activator = CoreActivator.getDefault();
			IVolumeManager volumeManager = activator.getVolumeManager();
			URI uri = volumeManager.findExistingFile(asset, true);
			File file = uri == null ? null : new File(uri);
			if (peer) {
				if (file != null && fileMoved)
					deleteFile(asset, file);
				toBeDeleted.add(asset);
				CreatorsContactImpl rel = dbManager.obtainById(CreatorsContactImpl.class,
						asset.getCreatorsContact_parent());
				if (rel != null) {
					rel.removeAsset(assetId);
					if (rel.getAsset().isEmpty()) {
						toBeDeleted.add(rel);
						String contactId = rel.getContact();
						if (contactId != null) {
							ContactImpl contact = dbManager.obtainById(ContactImpl.class, contactId);
							if (contact != null)
								toBeDeleted.add(contact);
						}
					} else
						toBeStored.add(rel);
				}
				LocationCreatedImpl loc = dbManager.obtainById(LocationCreatedImpl.class,
						asset.getLocationCreated_parent());
				if (loc != null) {
					loc.removeAsset(assetId);
					if (loc.getAsset().isEmpty())
						toBeDeleted.add(loc);
					else
						toBeStored.add(loc);
				}
				if (!asset.getLocationShown_parent().isEmpty())
					toBeDeleted.addAll(dbManager.obtainStructForAsset(LocationShownImpl.class, assetId, true));
				if (!asset.getArtworkOrObjectShown_parent().isEmpty())
					toBeDeleted.addAll(dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class, assetId, true));
				dbManager.markSystemCollectionsForPurge(asset);
				return null;
			}
			if (file == null)
				return asset;
			if (path != null) {
				File newFile = path.toFile();
				FileWatchManager fileWatchManager = activator.getFileWatchManager();
				try {
					fileWatchManager.moveFileSilently(file, newFile, anOpId, monitor);
					File[] xmps = Core.getSidecarFiles(file.toURI(), false);
					File[] xmpTargets = Core.getSidecarFiles(newFile.toURI(), false);
					URI voiceOrigURI = null;
					URI voiceTargetURI = null;
					if (AssetEnsemble.hasCloseVoiceNote(asset)) {
						voiceOrigURI = Core.getVoicefileURI(file);
						voiceTargetURI = Core.getVoicefileURI(newFile);
					}
					for (int i = 0; i < xmps.length; i++) {
						File xmpFile = xmps[i];
						if (xmpFile.exists())
							fileWatchManager.moveFileSilently(xmpFile, xmpTargets[i], anOpId, monitor);
					}
					if (voiceOrigURI != null) {
						File voiceFile = new File(voiceOrigURI);
						if (voiceFile.exists())
							fileWatchManager.moveFileSilently(voiceFile, new File(voiceTargetURI), anOpId, monitor);
					}
					dbManager.markSystemCollectionsForPurge(asset);
					asset.setUri(newFile.toURI().toString());
					toBeStored.add(asset);
					return asset;
				} catch (IOException e) {
					CoreActivator.getDefault()
							.logError(NLS.bind(Messages.FileLocationBackup_IO_error_restoring_file, file), e);
				} catch (DiskFullException e) {
					CoreActivator.getDefault()
							.logError(NLS.bind(Messages.FileLocationBackup_IO_error_restoring_file, file), e);
				}
			} else {
				deleteFile(asset, file);
				return asset;
			}
		}
		return null;
	}

	private static void deleteFile(Asset asset, File file) {
		file.delete();
		File[] xmps = Core.getSidecarFiles(file.toURI(), false);
		URI voiceOrigURI = null;
		if (AssetEnsemble.hasCloseVoiceNote(asset))
			voiceOrigURI = Core.getVoicefileURI(file);
		for (int i = 0; i < xmps.length; i++)
			xmps[i].delete();
		if (voiceOrigURI != null)
			new File(voiceOrigURI).delete();
	}

	public void setFileMoved(boolean fileMoved) {
		this.fileMoved = fileMoved;
	}

	public void addCreatedObject(IIdentifiableObject object) {
		if (createdObjects == null)
			createdObjects = new HashSet<>(7);
		createdObjects.add(object);
	}
}
