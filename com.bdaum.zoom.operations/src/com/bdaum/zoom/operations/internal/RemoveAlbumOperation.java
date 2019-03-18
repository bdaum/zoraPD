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

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class RemoveAlbumOperation extends DbOperation {

	private SmartCollectionImpl album;
	private List<? extends Asset> assets;
	private List<String[]> oldAlbums;
	private Map<String, List<String>> oldAssetIds = new HashMap<String, List<String>>();
	private Map<RegionImpl, String> oldRegionAlbums = new HashMap<RegionImpl, String>();

	public RemoveAlbumOperation(SmartCollectionImpl album, List<? extends Asset> assets) {
		super(Messages.getString("RemoveAlbumOperation.remove_from_album")); //$NON-NLS-1$
		this.assets = assets;
		this.album = album;
		oldAlbums = new ArrayList<String[]>(assets.size());
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, 2 * assets.size() + 1);
		saveOldAlbums();
		aMonitor.worked(1);
		if (setAlbums())
			fireAssetsModified(null, QueryField.ALBUMASSETS);
		return close(info);
	}

	private boolean setAlbums() {
		List<Object> toBeStored = new ArrayList<Object>(assets);
		removeFromAllSubalbums(album, toBeStored);
		SmartCollection tempAlbum = album;
		while (tempAlbum != null) {
			removeAssetFromAlbum(tempAlbum, toBeStored, false);
			tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
		}
		return storeSafely(null, toBeStored.size(), toBeStored.toArray());
	}

	private void removeFromAllSubalbums(SmartCollection album, List<Object> toBeStored) {
		for (SmartCollection sub : album.getSubSelection()) {
			removeAssetFromAlbum(sub, toBeStored, true);
			removeFromAllSubalbums(sub, toBeStored);
		}

	}

	private void removeAssetFromAlbum(SmartCollection album, List<Object> toBeStored, boolean force) {
		toBeStored.add(album);
		String name = album.getName();
		String personAlbumId = null;
		if (album.getAlbum() && album.getSystem())
			dbManager.addDirtyCollection(personAlbumId = album.getStringId());
		List<String> assetIds = album.getAsset();
		if (assetIds != null)
			for (Iterator<String> it = assetIds.iterator(); it.hasNext();) {
				String id = it.next();
				if (id == null || !dbManager.exists(AssetImpl.class, id))
					it.remove();
			}
		oldAssetIds.put(album.getStringId(), new ArrayList<String>(album.getAsset()));
		for (Asset asset : assets)
			if (asset.getFileState() != IVolumeManager.PEER) {
				String[] albums = asset.getAlbum();
				if (albums != null && (force || !containedInSubAlbum(album, albums))) {
					if (name != null)
						asset.setAlbum(Utilities.removeFromStringArray(name, albums));
					String assetId = asset.getStringId();
					if (assetIds != null)
						for (Iterator<String> it = assetIds.iterator(); it.hasNext();)
							if (assetId.equals(it.next()))
								it.remove();
					if (personAlbumId != null && asset.getPerson() != null)
						for (RegionImpl region : dbManager.obtainObjects(RegionImpl.class, false,
								"asset_person_parent", //$NON-NLS-1$
								assetId, QueryField.EQUALS, "album", personAlbumId, QueryField.EQUALS)) { //$NON-NLS-1$
							oldRegionAlbums.put(region, personAlbumId);
							region.setAlbum(null);
							toBeStored.add(region);
						}
				}
			}
	}

	private boolean containedInSubAlbum(SmartCollection album, String[] albums) {
		for (SmartCollection sub : album.getSubSelection()) {
			if (!oldAssetIds.containsKey(sub.getStringId())) {
				String name = sub.getName();
				if (name != null)
					for (String a : albums)
						if (name.equals(a))
							return true;
			}
			if (containedInSubAlbum(sub, albums))
				return true;
		}
		return false;
	}

	private void saveOldAlbums() {
		for (Asset asset : assets)
			oldAlbums.add(asset.getAlbum());
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		if (setAlbums())
			fireAssetsModified(null, QueryField.ALBUM);
		return close(info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, assets.size() + 2);
		int i = 0;
		List<Object> toBeStored = new ArrayList<Object>(assets);
		toBeStored.add(album);
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER)
				asset.setAlbum(oldAlbums.get(i));
			++i;
			monitor.worked(1);
		}
		undoSubAlbums(album, toBeStored);
		SmartCollection tempAlbum = album;
		while (tempAlbum != null) {
			List<String> list = oldAssetIds.get(tempAlbum.getStringId());
			if (list != null) {
				toBeStored.add(tempAlbum);
				tempAlbum.setAsset(list);
			}
			tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
		}
		monitor.worked(1);
		for (Map.Entry<RegionImpl, String> entry : oldRegionAlbums.entrySet()) {
			RegionImpl region = entry.getKey();
			region.setAlbum(entry.getValue());
			toBeStored.add(region);
		}
		oldRegionAlbums.clear();
		monitor.worked(1);
		if (storeSafely(null, toBeStored.size(), toBeStored.toArray()))
			fireAssetsModified(null, QueryField.ALBUM);
		return close(info);
	}

	private void undoSubAlbums(SmartCollection album, List<Object> toBeStored) {
		for (SmartCollection sub : album.getSubSelection()) {
			List<String> list = oldAssetIds.get(sub.getStringId());
			if (list != null) {
				toBeStored.add(sub);
				sub.setAsset(list);
			}
			undoSubAlbums(sub, toBeStored);
		}
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

}
