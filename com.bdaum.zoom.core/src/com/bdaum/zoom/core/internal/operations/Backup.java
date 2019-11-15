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

package com.bdaum.zoom.core.internal.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.trash.HistoryItem;

public class Backup extends HistoryItem {

	public class Modification {

		final String key;
		final String relId;
		final boolean removed;

		public Modification(String key, String relId, boolean removed) {
			this.key = key;
			this.relId = relId;
			this.removed = removed;
		}
	}

	private static final List<AomObject> EMPTYOBJ = new ArrayList<AomObject>(0);
	private List<AomObject> relations;
	private List<String> objects;
	private String assetId;
	private List<AomObject> deleted;
	private String[] keys;
	private Object[] oldValues;
	private List<Modification> modifications;
	private boolean timeLineChange;
	private boolean locationChange;
	private boolean folderHierarchyChange;
	private AssetImpl asset;

	public Backup(String opId, Asset asset, QueryField... qfields)
			throws SecurityException, IllegalArgumentException {
		super(opId);
		this.assetId = asset.getStringId();
		this.keys = new String[qfields.length];
		this.oldValues = new Object[qfields.length];
		for (int i = 0; i < qfields.length; i++) {
			QueryField qfield = qfields[i];
			this.keys[i] = qfield.getKey();
			if (!qfield.isStruct()) {
				oldValues[i] = qfields[i].obtainPlainFieldValue(asset);
				timeLineChange |= qfield == QueryField.EXIF_DATETIMEORIGINAL
						|| qfield == QueryField.IPTC_DATECREATED;
				locationChange |= qfield == QueryField.IPTC_LOCATIONCREATED;
				folderHierarchyChange |= (qfield == QueryField.URI || qfield == QueryField.VOLUME);
			}
		}
	}
	
	public void indicateChange(boolean timelineChange, boolean locationChange, boolean folderHierarchyChange) {
		this.timeLineChange |= timelineChange;
		this.locationChange |= locationChange;
		this.folderHierarchyChange |= folderHierarchyChange;
	}

	public void addRelation(AomObject rel) {
		if (relations == null)
			relations = new ArrayList<AomObject>(1);
		relations.add(rel);
	}

	public List<AomObject> getRelations() {
		return relations == null ? EMPTYOBJ : relations;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public void addObject(String oid) {
		if (objects == null)
			objects = new ArrayList<String>(2);
		objects.add(oid);
	}

	public void addDeleted(AomObject obj) {
		if (deleted == null)
			deleted = new ArrayList<AomObject>(2);
		deleted.add(obj);
	}

	public void addAllDeleted(Collection<? extends AomObject> set) {
		if (deleted == null)
			deleted = new ArrayList<AomObject>(2);
		deleted.addAll(set);
	}

	public boolean restore(List<Object> toBeStored, List<Object> toBeDeleted)
			throws IllegalArgumentException, SecurityException {
		boolean structureChange = false;
		IDbManager dbManager = Core.getCore().getDbManager();
		asset = dbManager.obtainAsset(assetId);
		if (asset == null)
			return false;
		if (timeLineChange || folderHierarchyChange || locationChange)
			dbManager.markSystemCollectionsForPurge(asset);
		for (int i = 0; i < keys.length; i++)
			QueryField.findQueryField(keys[i]).setFieldValue(asset,
					oldValues[i]);
		asset.setLastEdited(new Date());
		toBeStored.add(asset);
		if (timeLineChange)
			structureChange = dbManager.createTimeLine(asset, dbManager
					.getMeta(true).getTimeline());
		if (locationChange)
			structureChange |= dbManager.createLocationFolders(asset, dbManager
					.getMeta(true).getLocationFolders());
		if (folderHierarchyChange)
			structureChange |= dbManager.createFolderHierarchy(asset);
		if (timeLineChange || folderHierarchyChange || locationChange)
			dbManager.markSystemCollectionsForPurge(asset); // just in case
		if (objects != null)
			for (String oid : objects) {
				IdentifiableObject object = dbManager.obtainById(
						IdentifiableObject.class, oid);
				if (object != null)
					toBeDeleted.add(object);
			}
		if (deleted != null)
			for (AomObject obj : deleted)
				toBeStored.add(obj);
		if (modifications != null) {
			for (Modification mod : modifications) {
				if (mod.key == QueryField.IPTC_LOCATIONCREATED.getKey()) {
					LocationCreatedImpl rel = dbManager.obtainById(
							LocationCreatedImpl.class, mod.relId);
					if (rel != null) {
						if (mod.removed) {
							if (!rel.getAsset().contains(assetId)) {
								rel.addAsset(assetId);
								asset.setLocationCreated_parent(rel.getStringId());
							}
						} else {
							rel.removeAsset(assetId);
							asset.setLocationCreated_parent(null);
						}
						toBeStored.add(rel);
					}
				} else if (mod.key == QueryField.IPTC_CONTACT.getKey()) {
					CreatorsContactImpl rel = dbManager.obtainById(
							CreatorsContactImpl.class, mod.relId);
					if (rel != null) {
						if (mod.removed) {
							if (!rel.getAsset().contains(assetId)) {
								rel.addAsset(assetId);
								asset.setCreatorsContact_parent(rel.getStringId());
							}
						} else {
							rel.removeAsset(assetId);
							asset.setCreatorsContact_parent(null);
						}
						toBeStored.add(rel);
					}
				}

			}
		}
		toBeDeleted.add(this);
		return structureChange;
	}

	public void addModification(String key, String relId, boolean removed) {
		if (modifications == null)
			modifications = new ArrayList<Modification>(2);
		modifications.add(new Modification(key, relId, removed));
	}

	public boolean isEmpty() {
		return keys.length == 0 && deleted != null && objects != null
				&& modifications != null;
	}

	/**
	 * Only to be used after restore
	 * @return asset or null
	 */
	public AssetImpl getAsset() {
		return asset;
	}

}
