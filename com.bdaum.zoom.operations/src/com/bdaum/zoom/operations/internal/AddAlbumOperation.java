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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class AddAlbumOperation extends DbOperation {

	private static final String[] EMPTY = new String[0];
	private Collection<SmartCollectionImpl> albums;
	private List<Asset> assets;
	private String[][] oldAlbums;
	private String[][] oldPersons;
	private Map<SmartCollection, List<String>> oldAssetIds = new HashMap<>();
	private List<Region> updatedRegions = new ArrayList<>();
	private List<String> oldRegionAlbums = new ArrayList<>();
	private ImportOperation op;
	private int subalbums;
	private Region region;
	private boolean deleteRegion;

	public AddAlbumOperation(Collection<SmartCollectionImpl> coll, List<Asset> assets, Region region,
			boolean deleteRegion) {
		super(deleteRegion ? Messages.getString("AddAlbumOperation.delete_region") //$NON-NLS-1$
				: NLS.bind(Messages.getString("AddAlbumOperation.add_to_albums"), coll.size())); //$NON-NLS-1$
		this.albums = coll;
		this.assets = assets;
		this.region = region;
		this.deleteRegion = deleteRegion;
	}

	public AddAlbumOperation(SmartCollectionImpl coll, List<Asset> assets, Region region) {
		super(NLS.bind(Messages.getString("AddAlbumOperation.add_to_album"), coll.getName())); //$NON-NLS-1$
		this.region = region;
		albums = Collections.singletonList(coll);
		this.assets = assets;
	}

	public AddAlbumOperation(SmartCollectionImpl coll, ImportOperation op) {
		super(NLS.bind(Messages.getString("AddAlbumOperation.add_to_album"), coll.getName())); //$NON-NLS-1$
		albums = Collections.singletonList(coll);
		this.op = op;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		if (op != null)
			assets = op.obtainImportedAssets();
		subalbums = 0;
		if (albums != null)
			for (SmartCollection album : albums) {
				SmartCollection tempAlbum = album;
				while (tempAlbum != null) {
					++subalbums;
					tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
				}
			}
		init(aMonitor, subalbums * assets.size() + 1);
		oldAlbums = new String[assets.size()][];
		oldPersons = new String[assets.size()][];
		saveOldAlbums();
		aMonitor.worked(1);
		if (setAlbums(aMonitor))
			fireAssetsModified(null, QueryField.ALBUMASSETS);
		return close(info, (String[]) null);
	}

	private boolean setAlbums(IProgressMonitor monitor) {
		Set<Object> toBeDeleted = new HashSet<Object>();
		Set<Object> toBeStored = new HashSet<Object>();
		if (deleteRegion) {
			Asset asset = assets.get(0);
			String assetId = asset.getStringId();
			if (region != null) {
				toBeDeleted.add(region);
				removeRegionFeatures(asset, region, toBeStored);
				asset.setPerson(Utilities.removeFromStringArray(region.getStringId(), asset.getPerson()));
			} else {
				for (RegionImpl r : dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", assetId, //$NON-NLS-1$
						QueryField.EQUALS)) {
					toBeDeleted.add(r);
					removeRegionFeatures(asset, r, toBeStored);
				}
				asset.setPerson(EMPTY);
			}
			toBeStored.add(asset);
		} else {
			if (region != null) {
				Asset asset = assets.get(0);
				toBeStored.add(region);
				removeRegionFeatures(asset, region, toBeStored);
				toBeStored.add(asset);
			}
			for (SmartCollection album : albums) {
				SmartCollection tempAlbum = album;
				while (tempAlbum != null) {
					toBeStored.add(tempAlbum);
					if (!oldAssetIds.containsKey(tempAlbum))
						oldAssetIds.put(tempAlbum, new ArrayList<String>(tempAlbum.getAsset()));
					String name = tempAlbum.getName();
					if (name != null)
						for (Asset asset : assets) {
							if (asset.getFileState() == IVolumeManager.PEER)
								continue;
							String[] albums = asset.getAlbum();
							String[] newAlbums = Utilities.addToStringArray(name, albums, false);
							String assetId = asset.getStringId();
							if (albums == null || newAlbums.length != albums.length) {
								asset.setAlbum(newAlbums);
								toBeStored.add(asset);
								List<String> assetIds = tempAlbum.getAsset();
								if (assetIds == null)
									assetIds = Collections.singletonList(assetId);
								else if (!assetIds.contains(assetId))
									assetIds.add(assetId);
							}
							String[] persons = asset.getPerson();
							if (tempAlbum.getAlbum() && tempAlbum.getSystem() && persons != null
									&& persons.length > 0) {
								String albumId = tempAlbum.getStringId();
								if (region != null) {
									String oldRegionAlbumId = region.getAlbum();
									if (!albumId.equals(oldRegionAlbumId)) {
										oldRegionAlbums.add(oldRegionAlbumId);
										updatedRegions.add(region);
										region.setAlbum(albumId);
										toBeStored.add(region);
									}
								} else {
									Region nullRegion = null;
									int i = 0;
									for (RegionImpl region : dbManager.obtainObjects(RegionImpl.class,
											"asset_person_parent", assetId, QueryField.EQUALS)) //$NON-NLS-1$
										if (region.getAlbum() == null) {
											++i;
											if (nullRegion == null)
												nullRegion = region;
										}
									if (i == 1) {
										oldRegionAlbums.add(nullRegion.getAlbum());
										updatedRegions.add(nullRegion);
										nullRegion.setAlbum(albumId);
										toBeStored.add(nullRegion);
									}
								}
							}
							monitor.worked(1);
						}
					tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
				}
			}
		}
		return storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
	}

	private void removeRegionFeatures(Asset asset, Region r, Set<Object> toBeStored) {
		updatedRegions.add(r);
		String albumId = r.getAlbum();
		if (albumId != null) {
			SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, albumId);
			SmartCollection tempAlbum = album;
			while (tempAlbum != null) {
				albumId = tempAlbum.getStringId();
				oldAssetIds.put(tempAlbum, new ArrayList<String>(tempAlbum.getAsset()));
				tempAlbum.removeAsset(asset.getStringId());
				toBeStored.add(tempAlbum);
				dbManager.addDirtyCollection(albumId);
				asset.setAlbum(Utilities.removeFromStringArray(albumId, asset.getAlbum()));
				tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
			}
		}
	}

	private void saveOldAlbums() {
		for (int i = 0; i < assets.size(); i++) {
			oldPersons[i] = assets.get(i).getPerson();
			oldAlbums[i] = assets.get(i).getAlbum();
		}
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, subalbums * assets.size() + 1);
		if (setAlbums(aMonitor))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.ALBUM);
		return close(info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		int n = assets.size() + subalbums + 1;
		initUndo(aMonitor, n + 1);
		List<Object> toBeStored = new ArrayList<Object>(n);
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setAlbum(oldAlbums[i]);
				asset.setPerson(oldPersons[i]);
				toBeStored.add(asset);
			}
			monitor.worked(1);
			++i;
		}
		for (Map.Entry<SmartCollection, List<String>> entry : oldAssetIds.entrySet()) {
			SmartCollection album = entry.getKey();
			toBeStored.add(album);
			album.setAsset(oldAssetIds.get(album));
			monitor.worked(1);
		}
		i = 0;
		for (Region region : updatedRegions) {
			region.setAlbum(oldRegionAlbums.get(i++));
			toBeStored.add(region);
		}
		updatedRegions.clear();
		monitor.worked(1);
		if (storeSafely(null, 1, toBeStored.toArray()))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.ALBUM);
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets == null || assets.size() > 10 ? Job.LONG : Job.SHORT;
	}

}
