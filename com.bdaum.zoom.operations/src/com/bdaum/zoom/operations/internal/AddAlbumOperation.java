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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

	private Collection<SmartCollectionImpl> albums;
	private List<Asset> assets;
	private String[][] oldAlbums;
	private String[][] oldPersons;
	private List<List<String>> oldAssetIds = new ArrayList<List<String>>();
	private List<Region> updatedRegions = new ArrayList<>();
	private ImportOperation op;
	private int subalbums;
	private Region region;
	private boolean deleteRegion;
	private String[] oldRegionAlbums;

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
			fireAssetsModified(null, null);
		return close(info, (String[]) null);
	}

	private boolean setAlbums(IProgressMonitor monitor) {
		Set<Asset> modifiedAssets = new HashSet<Asset>(assets.size() * 3 / 2);
		Set<Object> toBeDeleted = new HashSet<Object>();
		Set<Object> toBeStored = new HashSet<Object>();
		if (region != null) {
			Asset asset = assets.get(0);
			String albumId = region.getAlbum();
			if (deleteRegion) {
				asset.setPerson(Utilities.removeFromStringArray(region.getStringId(), asset.getPerson()));
				toBeDeleted.add(region);
				updatedRegions.add(region);
				modifiedAssets.add(asset);
			}
			if (albumId != null) {
				if (!deleteRegion) {
					region.setAlbum(null);
					toBeStored.add(region);
					updatedRegions.add(region);
				}
				SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, albumId);
				if (album != null) {
					album.removeAsset(asset.getStringId());
					toBeStored.add(album);
				}
				asset.setAlbum(Utilities.removeFromStringArray(albumId, asset.getAlbum()));
				modifiedAssets.add(asset);
			}
		}
		if (!deleteRegion) {
			for (SmartCollection album : albums) {
				SmartCollection tempAlbum = album;
				while (tempAlbum != null) {
					toBeStored.add(tempAlbum);
					oldAssetIds.add(new ArrayList<String>(tempAlbum.getAsset()));
					String name = tempAlbum.getName();
					if (name != null)
						for (Asset asset : assets) {
							if (asset.getFileState() == IVolumeManager.PEER)
								continue;
							String[] albums = asset.getAlbum();
							String[] newAlbums = Utilities.addToStringArray(name, albums, false);
							if (albums == null || newAlbums.length != albums.length) {
								asset.setAlbum(newAlbums);
								modifiedAssets.add(asset);
								String assetId = asset.getStringId();
								List<String> assetIds = tempAlbum.getAsset();
								if (assetIds == null)
									assetIds = Collections.singletonList(assetId);
								else if (!assetIds.contains(assetId))
									assetIds.add(assetId);
								String[] persons = asset.getPerson();
								if (tempAlbum.getAlbum() && tempAlbum.getSystem() && persons != null
										&& persons.length > 0) {
									if (region != null) {
										updatedRegions.add(region);
										region.setAlbum(tempAlbum.getStringId());
										toBeStored.add(region);
									} else {
										List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class,
												"asset_person_parent", assetId, QueryField.EQUALS); //$NON-NLS-1$
										Region nullRegion = null;
										int i = 0;
										for (RegionImpl region : regions)
											if (region.getAlbum() == null) {
												++i;
												if (nullRegion == null)
													nullRegion = region;
											}
										if (i == 1) {
											updatedRegions.add(nullRegion);
											nullRegion.setAlbum(tempAlbum.getStringId());
											toBeStored.add(nullRegion);
										}
									}
								}
							}
							monitor.worked(1);
						}
					tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
				}
			}
		}
		oldRegionAlbums = new String[updatedRegions.size()];
		for (int i = 0; i < updatedRegions.size(); i++)
			oldRegionAlbums[i] = updatedRegions.get(i).getAlbum();
		toBeStored.addAll(modifiedAssets);
		return storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
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
		i = 0;
		for (SmartCollection album : albums) {
			SmartCollection tempAlbum = album;
			while (tempAlbum != null && i < oldAssetIds.size()) {
				toBeStored.add(tempAlbum);
				tempAlbum.setAsset(oldAssetIds.get(i));
				tempAlbum = tempAlbum.getSmartCollection_subSelection_parent();
				monitor.worked(1);
				++i;
			}
		}
		i = 0;
		for (Region region : updatedRegions) {
			region.setAlbum(oldRegionAlbums[i++]);
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
