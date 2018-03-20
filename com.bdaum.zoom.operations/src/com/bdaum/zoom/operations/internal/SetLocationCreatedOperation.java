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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class SetLocationCreatedOperation extends DbOperation {

	private SmartCollectionImpl locationColl;
	private List<Asset> assets;
	private String[] oldLocCreatedId;
	private LocationImpl newLocation;
	private LocationCreatedImpl newLocationCreated;
	private ArrayList<String> oldLcAssets;
	private LocationCreatedImpl locationCreated;

	public SetLocationCreatedOperation(SmartCollectionImpl locationColl, List<Asset> assets) {
		super(Messages.getString("SetLocationCreatedOperation.set_location_created")); //$NON-NLS-1$
		this.locationColl = locationColl;
		this.assets = assets;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets.size() > 2 ? Job.LONG : Job.SHORT;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		int size = assets.size();
		init(monitor, size + 2);
		oldLocCreatedId = new String[size];
		List<Object> toBeStored = new ArrayList<>();
		List<Object> toBeDeleted = new ArrayList<>();
		LocationImpl location = null;
		List<Object> namesValuesRelations = new ArrayList<>(5);
		for (Criterion crit : locationColl.getCriterion()) {
			namesValuesRelations.add(crit.getSubfield());
			namesValuesRelations.add(crit.getValue());
			namesValuesRelations.add(crit.getRelation());
		}
		locationCreated = null;
		Iterator<LocationImpl> it = dbManager.obtainObjects(LocationImpl.class, false, namesValuesRelations.toArray())
				.iterator();
		if (it.hasNext()) {
			location = it.next();
			List<LocationCreatedImpl> lcSet = dbManager.obtainObjects(LocationCreatedImpl.class, "location", //$NON-NLS-1$
					location.getStringId(), QueryField.EQUALS);
			if (!lcSet.isEmpty()) {
				locationCreated = lcSet.get(0);
				oldLcAssets = new ArrayList<>(locationCreated.getAsset());
			}

		} else {
			newLocation = location = new LocationImpl();
			SmartCollection parent = locationColl;
			while (parent != null) {
				String name = parent.getName();
				String id = parent.getStringId();
				int count = 0;
				for (int i = 0; i < id.length(); i++)
					if (id.charAt(i) == '|')
						++count;
				int p = id.lastIndexOf('|');
				if (p >= 0)
					id = id.substring(p + 1);
				switch (count) {
				case 0:
					location.setWorldRegionCode(id);
					location.setWorldRegion(name);
					break;
				case 1:
					location.setCountryISOCode(id);
					location.setCountryName(name);
					break;
				case 2:
					location.setProvinceOrState(name);
					break;
				default:
					location.setCity(name);
					break;
				}
				parent = parent.getSmartCollection_subSelection_parent();
			}
			location.setAltitude(Double.NaN);
			location.setLatitude(Double.NaN);
			location.setLongitude(Double.NaN);
			toBeStored.add(location);
		}
		String locationId = location.getStringId();
		if (locationCreated == null)
			newLocationCreated = locationCreated = new LocationCreatedImpl(locationId);
		toBeStored.add(locationCreated);
		monitor.worked(1);
		lp: for (int i = 0; i < size; i++) {
			Asset asset = assets.get(i);
			oldLocCreatedId[i] = asset.getLocationCreated_parent();
			String assetId = asset.getStringId();
			List<LocationCreatedImpl> oldLocCreated = dbManager.obtainStructForAsset(LocationCreatedImpl.class, assetId,
					true);
			for (LocationCreatedImpl lc : oldLocCreated) {
				if (lc.getLocation().equals(locationId))
					continue lp;
				lc.removeAsset(assetId);
				if (lc.getAsset().isEmpty())
					toBeDeleted.add(lc);
				else
					toBeStored.add(lc);
			}
			locationCreated.addAsset(assetId);
			asset.setLocationCreated_parent(locationCreated.getStringId());
			toBeStored.add(asset);
			monitor.worked(1);
		}
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		fireAssetsModified(new BagChange<>(null, new HashSet<Asset>(assets), null, null),
				QueryField.IPTC_LOCATIONCREATED);
		return close(info, assets);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		int size = assets.size();
		initUndo(monitor, size + 1);
		List<Object> toBeStored = new ArrayList<>();
		List<Object> toBeDeleted = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Asset asset = assets.get(i);
			asset.setLocationCreated_parent(oldLocCreatedId[i]);
			toBeStored.add(asset);
			monitor.worked(1);
		}
		if (newLocation != null)
			toBeDeleted.add(newLocation);
		if (newLocationCreated != null)
			toBeDeleted.add(newLocationCreated);
		else if (oldLcAssets != null) {
			locationCreated.setAsset(oldLcAssets);
			toBeStored.add(locationCreated);
		}
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		fireAssetsModified(new BagChange<>(null, new HashSet<Asset>(assets), null, null),
				QueryField.IPTC_LOCATIONCREATED);
		return close(info, assets);
	}

}
