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

import com.adobe.xmp.XMPException;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPField;

@SuppressWarnings("restriction")
public class PasteMetadataOperation extends DbOperation {

	private final Collection<XMPField> selectedFields;
	private final int mode;
	private final List<Asset> assets;
	private final Map<XMPField, Object>[] undoMaps;
	private boolean fullTextSearch = false;

	@SuppressWarnings("unchecked")
	public PasteMetadataOperation(List<Asset> selectedAssets,
			Collection<XMPField> selectedFields, int mode) {
		super(Messages.getString("PasteMetadataOperation.paste_metadata")); //$NON-NLS-1$
		this.assets = selectedAssets;
		this.selectedFields = selectedFields;
		this.mode = mode;
		undoMaps = new Map[(selectedAssets.size() * 4 / 3) + 1];
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				List<Object> toBeStored = new ArrayList<Object>();
				Set<Object> toBeDeleted = new HashSet<Object>();
				HashMap<XMPField, Object> undoMap = new HashMap<XMPField, Object>(
						selectedFields.size() * 4 / 3);
				undoMaps[i++] = undoMap;
				AssetEnsemble ensemble = new AssetEnsemble(dbManager, asset,
						null);
				boolean modified = false;
				for (XMPField field : selectedFields) {
					QueryField qfield = field.getQfield();
					XMPPropertyInfo prop = field.getProp();
					try {
						Object oldValue = field.getIndex1() <= 1 ? field
								.fetchStoredValue(ensemble) : undoMap
								.get(field);
						if (mode == Constants.SKIP && !qfield.isNeutralValue(oldValue))
							continue;
						undoMap.put(field, oldValue);
						String assetId = asset.getStringId();
						if (field.getIndex1() <= 1)
							deleteRelations(qfield, assetId, toBeDeleted,
									toBeStored);
						if (!createRelation(qfield, assetId, prop.getValue()
								.toString(), toBeStored))
							field.assignValue(ensemble, null);
						fullTextSearch |= qfield.isFullTextSearch();
						modified = true;
					} catch (XMPException e) {
						addWarning(
								NLS.bind(
										Messages.getString("PasteMetadataOperation.io_error_while_accessing_xmp"), //$NON-NLS-1$
										prop.getPath()), e);
					} catch (Exception e) {
						addError(
								NLS.bind(
										Messages.getString("PasteMetadataOperation.internal_error_while_accessing_clipboard"), //$NON-NLS-1$
										qfield.getKey()), e);
					}
				}
				if (modified) {
					ensemble.store(toBeDeleted, toBeStored);
					storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				}
			}
		}
		fireApplyRules(assets, null);
		fireAssetsModified(new BagChange<>(null, assets, null, null), null);
		return close(info, fullTextSearch ? assets : null);
	}

	private boolean createRelation(QueryField qfield, String assetId,
			String id, List<Object> toBeStored) {
		if (qfield == QueryField.LOCATIONSHOWN_ID) {
			List<LocationShownImpl> rels = dbManager.obtainStruct(
					LocationShownImpl.class, assetId, false, "location", id, //$NON-NLS-1$
					false);
			if (rels.isEmpty())
				toBeStored.add(new LocationShownImpl(id, assetId));
			return true;
		}
		if (qfield == QueryField.LOCATIONCREATED_ID) {
			List<LocationCreatedImpl> set = dbManager.obtainObjects(
					LocationCreatedImpl.class, "location", id, //$NON-NLS-1$
					QueryField.EQUALS);
			if (set.isEmpty()) {
				LocationCreatedImpl rel = new LocationCreatedImpl(id);
				rel.addAsset(assetId);
				toBeStored.add(rel);
			} else {
				LocationCreatedImpl rel = set.get(0);
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					toBeStored.add(rel);
				}
			}
			return true;
		}
		if (qfield == QueryField.CONTACT_ID) {
			List<CreatorsContactImpl> set = dbManager.obtainObjects(
					CreatorsContactImpl.class, "contact", id, //$NON-NLS-1$
					QueryField.EQUALS);
			if (set.isEmpty()) {
				CreatorsContactImpl rel = new CreatorsContactImpl(id);
				rel.addAsset(assetId);
				toBeStored.add(rel);
			} else {
				CreatorsContactImpl rel = set.get(0);
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					toBeStored.add(rel);
				}
			}
			return true;
		}
		if (qfield == QueryField.ARTWORK_ID) {
			List<ArtworkOrObjectShownImpl> rels = dbManager.obtainStruct(
					ArtworkOrObjectShownImpl.class, assetId, false,
					"artworkOrObject", id, false); //$NON-NLS-1$
			if (rels.isEmpty())
				toBeStored.add(new ArtworkOrObjectShownImpl(id,
						assetId));
			return true;
		}
		return false;
	}

	private void deleteRelations(QueryField qfield, String assetId,
			Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		if (qfield == QueryField.LOCATIONSHOWN_ID)
			for (LocationShownImpl obj : new ArrayList<LocationShownImpl>(
					dbManager.obtainStructForAsset(LocationShownImpl.class,
							assetId, false)))
				toBeDeleted.add(obj);
		else if (qfield == QueryField.LOCATIONCREATED_ID)
			for (LocationCreatedImpl obj : new ArrayList<LocationCreatedImpl>(
					dbManager.obtainStructForAsset(LocationCreatedImpl.class,
							assetId, true))) {
				LocationCreatedImpl rel = obj;
				rel.removeAsset(assetId);
				if (rel.getAsset().isEmpty())
					toBeDeleted.add(rel);
				else
					toBeStored.add(rel);
			}
		else if (qfield == QueryField.CONTACT_ID)
			for (CreatorsContactImpl obj : new ArrayList<CreatorsContactImpl>(
					dbManager.obtainStructForAsset(CreatorsContactImpl.class,
							assetId, true))) {
				CreatorsContactImpl rel = obj;
				rel.removeAsset(assetId);
				if (rel.getAsset().isEmpty())
					toBeDeleted.add(rel);
				else
					toBeStored.add(rel);
			}
		else if (qfield == QueryField.ARTWORK_ID)
			for (ArtworkOrObjectShownImpl obj : new ArrayList<ArtworkOrObjectShownImpl>(
					dbManager.obtainStructForAsset(
							ArtworkOrObjectShownImpl.class, assetId, false)))
				toBeDeleted.add(obj);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				List<Object> toBeStored = new ArrayList<Object>();
				Set<Object> toBeDeleted = new HashSet<Object>();
				Map<XMPField, Object> undoMap = undoMaps[i++];
				if (undoMap != null && !undoMap.isEmpty()) {
					AssetEnsemble ensemble = new AssetEnsemble(dbManager,
							asset, null);
					String assetId = asset.getStringId();
					for (Map.Entry<XMPField, Object> entry : undoMap.entrySet()) {
						Object oldValue = entry.getValue();
						XMPField field = entry.getKey();
						QueryField qfield = field.getQfield();
						deleteRelations(qfield, assetId, toBeDeleted,
								toBeStored);
						boolean done = false;
						if (oldValue instanceof String)
							done = createRelation(qfield, assetId,
									(String) oldValue, toBeStored);
						else if (oldValue instanceof String[])
							for (String id : (String[]) oldValue)
								done |= createRelation(qfield, assetId, id,
										toBeStored);
						if (!done)
							try {
								field.assignValue(ensemble, oldValue);
							} catch (Exception e) {
								addError(
										NLS.bind(
												Messages.getString("PasteMetadataOperation.internal_error_when_restoring"), //$NON-NLS-1$
												qfield.getKey()), e);
							}
					}
					ensemble.store(toBeDeleted, toBeStored);
					storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				}
			}
		}
		return close(info, fullTextSearch ? assets : null);
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
