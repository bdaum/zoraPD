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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Messages;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.program.BatchUtilities;
import com.sun.jna.platform.FileUtils;

/**
 * Trash item for a catalog entry
 *
 */
public class Trash extends HistoryItem {

	private Asset asset;
	private Date date;
	private List<String> compositions;
	private boolean files;
	private String name;
	private List<IdentifiableObject> objects;
	private List<String> locationCreated;
	private List<String> creatorsContact;

	public Trash() {
	}

	/**
	 * Constructor
	 *
	 * @param opId
	 *            - ID of operation that created the trash
	 * @param asset
	 *            - trashed image asset
	 * @param files
	 *            - true if image files are to be deleted, too.
	 * @param date
	 *            - deletion date
	 */
	public Trash(String opId, Asset asset, boolean files, Date date) {
		super(opId);
		this.asset = asset;
		this.date = date;
		this.files = files;
		this.name = asset.getName();
	}

	/**
	 * Returns deletion date
	 *
	 * @return deletion date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets deletion date
	 *
	 * @param date
	 *            deletion date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Adds IDs of ComposedTo relations that are to be deleted, too
	 *
	 * @param anId
	 *            - IDs of ComposedTo relation
	 */
	public void addComposition(String anId) {
		if (compositions == null)
			compositions = new ArrayList<String>(1);
		compositions.add(anId);
	}

	/**
	 * Tests if also image files are to be deleted
	 *
	 * @return true if also image files are to be deleted
	 */
	public boolean isFiles() {
		return files;
	}

	/**
	 * Sets if also image files are to be deleted
	 *
	 * @param files
	 *            - true if also image files are to be deleted
	 */
	public void setFiles(boolean files) {
		this.files = files;
	}

	/**
	 * Sets a name for the trash object
	 *
	 * @return name for the trash object
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a name for the trash object
	 *
	 * @param name
	 *            - a name for the trash object
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Adds depending objects that are to be deleted, too
	 *
	 * @param obj
	 *            - depending object
	 */
	public void addObject(IdentifiableObject obj) {
		if (objects == null)
			objects = new ArrayList<IdentifiableObject>(4);
		objects.add(obj);
	}

	/**
	 * Adds depending objects that are to be deleted, too
	 *
	 * @param obj
	 *            - depending objects
	 */
	public void addObjects(List<? extends IdentifiableObject> objs) {
		if (objects == null)
			objects = new ArrayList<IdentifiableObject>(4);
		objects.addAll(objs);
	}

	/**
	 * Add ID of locations that are to be deleted, too.
	 *
	 * @param anId
	 *            - ID of location
	 */
	public void addLocationCreated(String anId) {
		if (locationCreated == null)
			locationCreated = new ArrayList<String>(1);
		locationCreated.add(anId);
	}

	/**
	 * Add ID of creator contacts that are to be deleted, too.
	 *
	 * @param anId
	 *            - ID of creator contact
	 */
	public void addCreatorsContact(String anId) {
		if (creatorsContact == null)
			creatorsContact = new ArrayList<String>(1);
		creatorsContact.add(anId);
	}

	/**
	 * Restores the catalog entry
	 *
	 * @param dbManager
	 *            - database manager
	 * @param iw
	 *            - Indexwriter token (Lucene)
	 * @param status
	 *            - status object
	 * @return - the restored asset
	 */
	public Asset restore(IDbManager dbManager, Object iw, MultiStatus status) {
		String assetid = asset.getStringId();
		dbManager.deleteTrash(asset);
		IVolumeManager vm = CoreActivator.getDefault().getVolumeManager();
		URI uri = vm.findFile(asset);
		if (uri != null) {
			List<Ghost_typeImpl> ghosts = dbManager.obtainGhostsForFile(uri);
			for (Ghost_typeImpl ghost : ghosts)
				dbManager.delete(ghost);
		}
		// Albums
		String[] albums = asset.getAlbum();
		if (albums != null)
			for (String name : albums) {
				List<SmartCollectionImpl> allAlbums = dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", //$NON-NLS-1$
						true, QueryField.EQUALS, "name", name, QueryField.EQUALS); //$NON-NLS-1$
				for (SmartCollectionImpl album : allAlbums)
					if (album.addAsset(assetid))
						dbManager.store(album);
			}
		// Relations
		for (DerivedByImpl rel : dbManager.obtainStruct(DerivedByImpl.class, null, false, "derivative", assetid, false)) //$NON-NLS-1$
			dbManager.delete(rel);
		for (DerivedByImpl rel : dbManager.obtainStruct(DerivedByImpl.class, null, false, "original", assetid, false)) //$NON-NLS-1$
			dbManager.delete(rel);
		if (objects != null)
			for (IdentifiableObject rel : objects) {
				dbManager.deleteTrash(rel);
				dbManager.store(rel);
			}
		if (compositions != null) {
			for (String compId : compositions) {
				ComposedToImpl impl = dbManager.obtainById(ComposedToImpl.class, compId);
				if (impl != null && !impl.getComponent().contains(assetid)) {
					impl.addComponent(assetid);
					dbManager.store(impl);
				}
			}
		}
		// Structures
		if (locationCreated != null)
			for (String locId : locationCreated) {
				LocationCreatedImpl impl = dbManager.obtainById(LocationCreatedImpl.class, locId);
				if (impl != null && !impl.getAsset().contains(assetid)) {
					impl.addAsset(assetid);
					asset.setLocationCreated_parent(impl.getStringId());
					dbManager.store(impl);
				}
			}
		if (creatorsContact != null)
			for (String contactId : creatorsContact) {
				CreatorsContactImpl impl = dbManager.obtainById(CreatorsContactImpl.class, contactId);
				if (impl != null && !impl.getAsset().contains(assetid)) {
					impl.addAsset(assetid);
					dbManager.store(impl);
				}
			}
		if (iw != null)
			try (ByteArrayInputStream in = new ByteArrayInputStream(asset.getJpegThumbnail())) {
				BufferedImage image = ImageIO.read(in);
				if (image != null)
					Core.getCore().getDbFactory().getLuceneService().addDocument(iw, image, assetid);
			} catch (IOException e) {
				status.add(
						new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, Messages.Trash_error_adding_to_lucene, e));
			}
		dbManager.store(asset);
		dbManager.deleteTrash(this);
		return asset;
	}

	/**
	 * Returns the trashed asset
	 *
	 * @return the trashed asset
	 */
	public Asset getAsset() {
		return asset;
	}

	/**
	 * Deletes the catalog entry permanently
	 *
	 * @param dbManager
	 *            - database manager
	 */
	public void removeItem(IDbManager dbManager) {
		deleteFiles();
		dbManager.deleteTrash(asset);
		if (objects != null)
			for (IdentifiableObject obj : objects)
				dbManager.deleteTrash(obj);
		dbManager.deleteTrash(this);
	}

	/**
	 * Deletes the image files permanently
	 */
	public void deleteFiles() {
		if (files) {
			List<File> toBeDeleted = new ArrayList<File>(3);
			ICore activator = CoreActivator.getDefault();
			IVolumeManager volumeManager = activator.getVolumeManager();
			URI uri = volumeManager.findExistingFile(asset, true);
			if (uri != null && Constants.FILESCHEME.equals(uri.getScheme()))
				toBeDeleted.add(new File(uri));
			if (asset.getXmpModifiedAt() != null)
				for (File sidecar : Core.getSidecarFiles(uri, false))
					if (sidecar.exists() && sidecar.lastModified() == asset.getXmpModifiedAt().getTime())
						toBeDeleted.add(sidecar);
			if (AssetEnsemble.hasCloseVoiceNote(asset)) { 
				URI voiceFileUri = volumeManager.findVoiceFile(asset);
				if (voiceFileUri != null && Constants.FILESCHEME.equals(voiceFileUri.getScheme()))
					toBeDeleted.add(new File(voiceFileUri));
			}
			FileUtils fu = FileUtils.getInstance();
			boolean trashed = false;
			IOException exc = null;
			if (fu.hasTrash()) {
				try {
					fu.moveToTrash(toBeDeleted.toArray(new File[toBeDeleted.size()]));
					trashed = true;
				} catch (IOException e) {
					exc = e;
				}
			}
			if (!trashed) {
				if (Constants.LINUX)
					try {
						String[] cmd = new String[] { "trash", null }; //$NON-NLS-1$
						for (File file : toBeDeleted) {
							cmd[1] = file.getAbsolutePath();
							BatchUtilities.executeCommand(cmd, null, null, Messages.Trash_send_to_trash, IStatus.OK,
									IStatus.ERROR, -1, 1500L, "UTF-8", null); //$NON-NLS-1$
						}
						return;
					} catch (IOException | ExecutionException e) {
						activator.logError(Messages.Trash_install_trash_cli, e);
					}
				else if (exc == null)
					activator.logError(Messages.Trash_no_waste_basket, null);
				else
					activator.logError(Messages.Trash_io_error_moving_to_waste_basket, exc);
				for (File file : toBeDeleted)
					if (!file.delete())
						activator.logError(NLS.bind(Messages.Trash_file_not_deleted, file), null);
			}
		}
	}

	/**
	 * Returns the image URI
	 *
	 * @return - image URI
	 */
	public String getUri() {
		return asset.getUri();
	}

	/**
	 * Returns the trashed depending objects
	 *
	 * @return - trashed depending objects
	 */
	public List<IdentifiableObject> getObjects() {
		return objects;
	}

	/**
	 * Returns the image volume label
	 *
	 * @return - image volume label
	 */
	public String getVolume() {
		return asset.getVolume();
	}

}
