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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class MultiModifyAssetOperation extends DbOperation {

	private List<Asset> assets;
	private Map<QueryField, Object> valueMap;
	private Map<QueryField, Object> oldValueMap;
	private List<String> oldKeywords;
	private Map<String, Category> oldCategories;
	private RenameAssetOperation renameOperation;

	public MultiModifyAssetOperation(QueryField qfield, Object value, Object oldValue, List<Asset> assets) {
		super(Messages.getString("MultiModifyAssetOperation.modify_values")); //$NON-NLS-1$
		this.assets = assets;
		if (value != null) {
			valueMap = new HashMap<QueryField, Object>(2);
			valueMap.put(qfield, value);
		}
		if (oldValue != null) {
			oldValueMap = new HashMap<QueryField, Object>(2);
			oldValueMap.put(qfield, oldValue);
		}
	}

	public MultiModifyAssetOperation(Map<QueryField, Object> valueMap, Map<QueryField, Object> oldValueMap,
			List<Asset> assets) {
		super(Messages.getString("MultiModifyAssetOperation.modify_values")); //$NON-NLS-1$
		this.assets = assets;
		this.valueMap = new HashMap<QueryField, Object>(valueMap);
		this.oldValueMap = new HashMap<QueryField, Object>(oldValueMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		try {
			renameOperation = null;
			oldKeywords = null;
			oldCategories = null;
			int assetCount = assets.size();
			init(aMonitor, assetCount + 1);
			List<Object> toBeStored = new ArrayList<Object>();
			List<Object> toBeDeleted = new ArrayList<Object>();
			boolean changed = false;
			boolean assetsChanged = false;
			boolean timeLineChange = false;
			boolean locationChange = false;
			boolean folderHierarchyChange = false;
			if (assets != null) {
				Set<QueryField> keySet = valueMap == null ? oldValueMap.keySet() : valueMap.keySet();
				List<QueryField> qfields = new ArrayList<QueryField>(keySet.size() + 1);
				boolean updateLastEdit = false;
				for (QueryField qfield : keySet) {
					if (!qfield.isStruct() && !(qfield == QueryField.NAME && assetCount == 1)) {
						qfields.add(qfield);
						timeLineChange |= qfield == QueryField.EXIF_DATETIMEORIGINAL
								|| qfield == QueryField.IPTC_DATECREATED;
						locationChange |= qfield == QueryField.IPTC_LOCATIONCREATED;
						folderHierarchyChange |= (qfield == QueryField.URI || qfield == QueryField.VOLUME);
						if (qfield == QueryField.EXIF_DATETIMEORIGINAL && keySet.contains(QueryField.IPTC_DATECREATED))
							qfields.add(QueryField.IPTC_DATECREATED);
					}
					updateLastEdit = isIptc(qfield);
				}
				updateLastEdit &= !qfields.contains(QueryField.IPTC_LASTEDITED);
				if (updateLastEdit)
					qfields.add(QueryField.IPTC_LASTEDITED);
				QueryField[] fields = qfields.toArray(new QueryField[qfields.size()]);
				String key = null;
				try {
					boolean first = true;
					for (Asset asset : assets) {
						if (asset.getFileState() != IVolumeManager.PEER) {
							if (timeLineChange || locationChange || folderHierarchyChange)
								dbManager.markSystemCollectionsForPurge(asset);
							String assetId = asset.getStringId();
							for (QueryField qfield : keySet) {
								key = qfield.getKey();
								Object value = valueMap == null ? null : valueMap.get(qfield);
								Backup backup = new Backup(opId, asset, fields);
								if (qfield == QueryField.NAME && assetCount == 1) {
									renameOperation = new RenameAssetOperation(assets.get(0), (String) value, first);
									renameOperation.execute(aMonitor, info);
								} else if (qfield == QueryField.EXIF_DATETIMEORIGINAL) {
									asset.setDateTimeOriginal((Date) value);
									Object v2 = valueMap == null ? null : valueMap.get(QueryField.IPTC_DATECREATED);
									if (v2 == null && asset.getDateCreated() == null)
										asset.setDateCreated(asset.getDateTimeOriginal());
								} else if (qfield == QueryField.IPTC_DATECREATED) {
									asset.setDateCreated(value == null ? asset.getDateTimeOriginal() : (Date) value);
								} else if (qfield.isStruct()) {
									String id = (value instanceof String) ? (String) value
											: (value instanceof IdentifiableObject)
													? ((IdentifiableObject) value).getStringId() : null;
									Object oldValue = oldValueMap == null ? null : oldValueMap.get(qfield);
									String oldId = (oldValue instanceof String) ? (String) oldValue
											: (oldValue instanceof IdentifiableObject)
													? ((IdentifiableObject) oldValue).getStringId() : null;
									if (qfield == QueryField.IPTC_LOCATIONCREATED) {
										if (oldId != null) {
											List<LocationCreatedImpl> set = dbManager.obtainStruct(
													LocationCreatedImpl.class, assetId, true, "location", oldId, //$NON-NLS-1$
													false);
											for (LocationCreatedImpl rel : set) {
												rel.removeAsset(assetId);
												if (rel.getAsset().isEmpty()) {
													backup.addDeleted(rel);
													toBeDeleted.add(rel);
												} else {
													backup.addModification(key, rel.getStringId(), true);
													rel.removeAsset(assetId);
													toBeStored.add(rel);
												}
											}
										}
										if (id != null) {
											List<LocationCreatedImpl> set = dbManager.obtainObjects(
													LocationCreatedImpl.class, "location", id, QueryField.EQUALS); //$NON-NLS-1$
											LocationCreatedImpl rel;
											if (set.isEmpty()) {
												rel = new LocationCreatedImpl(id);
												backup.addObject(rel.getStringId());
												toBeStored.add(rel);
											} else {
												rel = set.get(0);
												backup.addModification(key, rel.getStringId(), false);
												toBeStored.add(rel);
											}
											rel.addAsset(assetId);
										}
									} else if (qfield == QueryField.IPTC_LOCATIONSHOWN) {
										if (oldId != null) {
											List<LocationShownImpl> set = dbManager.obtainStruct(
													LocationShownImpl.class, assetId, false, "location", id, //$NON-NLS-1$
													false);
											for (LocationShownImpl rel : set) {
												backup.addDeleted(rel);
												toBeDeleted.add(rel);
											}
										}
										if (id != null) {
											LocationShownImpl rel = new LocationShownImpl(id, assetId);
											backup.addObject(rel.getStringId());
											toBeStored.add(rel);
										}
									} else if (qfield == QueryField.IPTC_CONTACT) {
										if (oldId != null) {
											List<CreatorsContactImpl> set = dbManager.obtainStruct(
													CreatorsContactImpl.class, assetId, true, "contact", oldId, //$NON-NLS-1$
													false);
											for (CreatorsContactImpl rel : set) {
												rel.removeAsset(assetId);
												if (rel.getAsset().isEmpty()) {
													backup.addDeleted(rel);
													toBeDeleted.add(rel);
												} else {
													backup.addModification(key, rel.getStringId(), true);
													toBeStored.add(rel);
												}
											}
										}
										if (id != null) {
											List<IdentifiableObject> set = dbManager.obtainObjects(
													CreatorsContactImpl.class, false, "contact", id, QueryField.EQUALS); //$NON-NLS-1$
											CreatorsContactImpl rel;
											if (set.isEmpty()) {
												rel = new CreatorsContactImpl(id);
												backup.addObject(rel.getStringId());
												toBeStored.add(rel);
											} else {
												rel = (CreatorsContactImpl) set.get(0);
												backup.addModification(key, rel.getStringId(), false);
												toBeStored.add(rel);
											}
											rel.addAsset(assetId);
										}
									} else if (qfield == QueryField.IPTC_ARTWORK) {
										if (oldId != null) {
											List<ArtworkOrObjectShownImpl> set = dbManager.obtainStruct(
													ArtworkOrObjectShownImpl.class, assetId, false, "artworkOrObject", //$NON-NLS-1$
													oldId, false);
											for (ArtworkOrObjectShownImpl rel : set) {
												backup.addDeleted(rel);
												toBeDeleted.add(rel);
											}
										}
										if (id != null) {
											ArtworkOrObjectShownImpl rel = new ArtworkOrObjectShownImpl(id, assetId);
											backup.addObject(rel.getStringId());
											toBeStored.add(rel);
										}
									}
								} else {
									int card = qfield.getCard();
									if ((card == QueryField.CARD_BAG || card == QueryField.CARD_MODIFIABLEBAG)
											&& !(value instanceof BagChange))
										qfield.resetBag(asset);
									qfield.setFieldValue(asset, value);
								}
								if (!backup.isEmpty()) {
									dbManager.storeTrash(backup);
									if (updateLastEdit)
										asset.setLastEdited(new Date());
									toBeStored.add(asset);
									if (storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray())) {
										assetsChanged = true;
										changed |= updateFolderHierarchies(asset, folderHierarchyChange,
												timeLineChange ? dbManager.getMeta(true).getTimeline() : null,
												locationChange ? dbManager.getMeta(true).getLocationFolders() : null,
												false);
									}
								}
								toBeDeleted.clear();
								toBeStored.clear();
								aMonitor.worked(1);
							}
							first = false;
						}
					}
					if (valueMap != null) {
						for (QueryField qfield : keySet) {
							key = qfield.getKey();
							if (qfield == QueryField.IPTC_KEYWORDS) {
								Meta meta = dbManager.getMeta(true);
								Set<String> keywords = meta.getKeywords();
								oldKeywords = new ArrayList<String>(keywords);
								Object v = valueMap.get(qfield);
								FilterChain keywordFilter = QueryField.getKeywordFilter();
								if (v instanceof BagChange) {
									Set<String> added = ((BagChange<String>) v).getAdded();
									if (added != null)
										keywords.addAll(keywordFilter.filter(added));
								} else
									keywords.addAll(keywordFilter.filter((String[]) v));
								toBeStored.add(meta);
							} else if (qfield == QueryField.IPTC_CATEGORY) {
								Meta meta = dbManager.getMeta(true);
								oldCategories = new HashMap<String, Category>(meta.getCategory());
								Object value = valueMap.get(qfield);
								if (value instanceof String)
									changed |= updateCategories(meta, (String) value, toBeDeleted, toBeStored);
								else if (value instanceof String[])
									for (String cat : ((String[]) value))
										changed |= updateCategories(meta, cat, toBeDeleted, toBeStored);
								if (!toBeStored.contains(meta))
									toBeStored.add(meta);
							}
						}
					}
					if (storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray()) || assetsChanged) {
						QueryField node = keySet.size() == 1 ? keySet.toArray(new QueryField[keySet.size()])[0] : null;
						HashSet<Asset> set = new HashSet<Asset>(assets);
						fireApplyRules(set, node);
						fireAssetsModified(new BagChange<>(null, set, null, null), node);
					}
					if (changed)
						fireStructureModified();
				} catch (Throwable e) {
					addError(NLS.bind(Messages.getString("MultiModifyAssetOperation.error_assigning_field"), key), e); //$NON-NLS-1$
				}
			}
		} catch (Exception e) {
			addError(Messages.getString("MultiModifyAssetOperation.error_accessing_fields"), e); //$NON-NLS-1$
		}
		return close(info, isFullTextSearch() ? assets : null);
	}

	private static boolean isIptc(QueryField qfield) {
		while (qfield != null) {
			if (qfield == QueryField.IPTC_ALL)
				return true;
			qfield = qfield.getParent();
		}
		return false;
	}

	private boolean isFullTextSearch() {
		for (QueryField qfield : valueMap == null ? oldValueMap.keySet() : valueMap.keySet())
			if (qfield.isFullTextSearch())
				return true;
		return false;
	}

	private boolean updateCategories(Meta meta, String catPath, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored) {
		GroupImpl group = Utilities.obtainCatGroup(dbManager, toBeDeleted, toBeStored);
		Map<String, Category> categories = meta.getCategory();
		boolean changed = Utilities.ensureCatConsistency(categories);
		Category cat = null;
		int p = catPath.length();
		String label = null;
		while (p >= 0) {
			int q = catPath.lastIndexOf('.', p);
			label = catPath.substring(q + 1, p);
			cat = Utilities.findCategory(categories, label);
			if (cat != null)
				break;
			p = q;
		}
		String subPath = p == catPath.length() ? null : catPath.substring(p + 1);
		if (cat == null) {
			cat = new CategoryImpl(label);
			categories.put(label, cat);
			changed |= Utilities.addCategoryCollection(dbManager, group, cat, toBeStored) != null;
		}
		if (subPath != null) {
			StringTokenizer st = new StringTokenizer(subPath, "."); //$NON-NLS-1$
			Category parent = cat;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				cat = new CategoryImpl(token);
				parent.putSubCategory(cat);
				// Add to cat view
				changed |= Utilities.addCategoryCollection(dbManager, group, cat, toBeStored) != null;
				parent = cat;
			}
		}
		return changed;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, assets.size() + 2);
		if (renameOperation != null)
			renameOperation.undo(aMonitor, info);
		aMonitor.worked(1);
		List<Object> toBeStored = new ArrayList<Object>();
		List<Object> toBeDeleted = new ArrayList<Object>();
		boolean changed = false;
		boolean assetsModified = false;
		List<Backup> set = dbManager.getTrash(Backup.class, opId);
		for (Backup backup : set) {
			try {
				changed |= backup.restore(toBeStored, toBeDeleted);
				assetsModified |= dbManager.safeTransaction(toBeDeleted, toBeStored);
				toBeDeleted.clear();
				toBeStored.clear();
				aMonitor.worked(1);
			} catch (Exception e) {
				addError(Messages.getString("MultiModifyAssetOperation.error_assigning_former_value"), //$NON-NLS-1$
						e);
			}
		}
		if (oldKeywords != null) {
			Meta meta = dbManager.getMeta(true);
			meta.setKeywords(oldKeywords);
			toBeStored.add(meta);
		} else if (oldCategories != null) {
			Meta meta = dbManager.getMeta(true);
			meta.setCategory(oldCategories);
			if (!toBeStored.contains(meta))
				toBeStored.add(meta);
			changed = true;
		}
		if (storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray()) || assetsModified)
			fireAssetsModified(new BagChange<>(null, assets, null, null), null);
		if (changed)
			fireStructureModified();
		return close(info, isFullTextSearch() ? assets : null);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets.size() > 1 ? Job.LONG : Job.SHORT;
	}

}
