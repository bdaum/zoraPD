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

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ICatalogContributor;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class MergeCatOperation extends AbstractCloneCatOperation {

	private final IDbManager externalDb;
	private final int duplicatePolicy;
	private Map<String, String> idMap = new HashMap<String, String>();
	private List<AssetImpl> assetsToIndex;
	private final GregorianCalendar cal = new GregorianCalendar();
	private final Set<QueryField> xmpFilter;
	private final int metaDataPolicy;

	public MergeCatOperation(IDbManager externalDb, int duplicatePolicy, Set<QueryField> xmpFilter,
			int metaDataPolicy) {
		super(Messages.getString("MergeCatOperation.merge_catalogs")); //$NON-NLS-1$
		this.externalDb = externalDb;
		this.duplicatePolicy = duplicatePolicy;
		this.xmpFilter = xmpFilter;
		this.metaDataPolicy = metaDataPolicy;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		Collection<ICatalogContributor> catalogContributors = CoreActivator.getDefault().getCatalogContributors();
		Meta externalMeta = externalDb.getMeta(true);
		List<Object> toBeStored = new ArrayList<Object>();
		List<Object> toBeDeleted = new ArrayList<Object>();
		Meta meta = dbManager.getMeta(true);
		String timeline = meta.getTimeline();
		String locations = meta.getLocationFolders();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		List<AssetImpl> assets = externalDb.obtainAssets();
		assetsToIndex = new ArrayList<AssetImpl>(assets.size());
		int na = assets.size();
		init(aMonitor, na * 2000 + 4500 + catalogContributors.size() * 1000);
		aMonitor.subTask(Messages.getString("MergeCatOperation.merging_import_collections")); //$NON-NLS-1$
		if (externalMeta.getLastImport().compareTo(meta.getLastImport()) < 0) {
			SmartCollectionImpl coll = externalDb.obtainById(SmartCollectionImpl.class, Constants.LAST_IMPORT_ID);
			String importId = Utilities.downGradeLastImport(coll);
			SmartCollectionImpl shadow = dbManager.obtainById(SmartCollectionImpl.class, importId);
			if (shadow == null) {
				GroupImpl importGroup = dbManager.getImportGroup();
				importGroup.addRootCollection(coll.getStringId());
				coll.setGroup_rootCollection_parent(importGroup.getStringId());
				toBeStored.add(coll);
				toBeStored.add(importGroup);
			}
		}
		cal.setTime(meta.getLastImport());
		int year = cal.get(Calendar.YEAR);
		Date importDate = new Date();
		cal.setTime(importDate);
		if (year != cal.get(Calendar.YEAR))
			meta.setLastYearSequenceNo(0);
		meta.setLastImport(importDate);
		toBeStored.add(meta);
		storeSafely(null, 500, toBeStored.toArray());
		int i = 0;
		int n = 0;
		for (AssetImpl asset : assets) {
			aMonitor.subTask(NLS.bind(Messages.getString("MergeCatOperation.transferring_image"), ++n, na)); //$NON-NLS-1$
			toBeStored.clear();
			toBeDeleted.clear();
			final String sourceAssetId = asset.getStringId();
			AssetImpl targetAsset = dbManager.obtainAsset(sourceAssetId);
			if (targetAsset == null) {
				List<AssetImpl> set = dbManager.obtainObjects(AssetImpl.class, QueryField.NAME.getKey(),
						asset.getName(), QueryField.EQUALS);
				if (!set.isEmpty()) {
					URI sourceFile = volumeManager.findFile(asset);
					if (sourceFile != null)
						for (AssetImpl obj : set)
							if (sourceFile.equals(volumeManager.findFile(obj))) {
								targetAsset = obj;
								break;
							}
				}
			}
			boolean structChanged = false;
			boolean catChanged = false;
			String targetAssetId = null;
			if (targetAsset != null) {
				switch (duplicatePolicy) {
				case Constants.SKIP:
					// do nothing
					break;
				case Constants.REPLACE:
					targetAssetId = targetAsset.getStringId();
					toBeDeleted.add(targetAsset);
					asset.setStringId(targetAssetId);
					toBeStored.add(asset);
					catChanged = true;
					mergeStructures(sourceAssetId, targetAssetId, true, toBeStored, toBeDeleted);
					mergeRegions(asset, targetAsset, true, toBeStored, toBeDeleted);
					mergeExtensions(asset, targetAsset, true, toBeStored, toBeDeleted);
					mergeGhosts(asset, targetAsset, toBeStored);
					break;
				default:
					targetAssetId = targetAsset.getStringId();
					mergeAsset(asset, targetAsset);
					toBeStored.add(targetAsset);
					catChanged = true;
					mergeStructures(sourceAssetId, targetAssetId, false, toBeStored, toBeDeleted);
					mergeRegions(asset, targetAsset, false, toBeStored, toBeDeleted);
					mergeExtensions(asset, targetAsset, false, toBeStored, toBeDeleted);
					mergeGhosts(asset, targetAsset, toBeStored);
					structChanged |= updateFolderHierarchies(asset, true, timeline, locations, true);
					break;
				}
			} else {
				targetAssetId = sourceAssetId;
				toBeStored.add(asset);
				catChanged = true;
				mergeStructures(sourceAssetId, sourceAssetId, false, toBeStored, toBeDeleted);
				mergeRegions(asset, null, false, toBeStored, toBeDeleted);
				mergeExtensions(asset, null, false, toBeStored, toBeDeleted);
				mergeGhosts(asset, targetAsset, toBeStored);
				structChanged |= updateFolderHierarchies(asset, true, timeline, locations, false);
			}
			if (targetAssetId != null) {
				idMap.put(sourceAssetId, targetAssetId);
				List<TrackRecordImpl> trackRecords = externalDb.obtainObjects(TrackRecordImpl.class,
						"asset_track_parent", sourceAssetId, QueryField.EQUALS); //$NON-NLS-1$
				for (TrackRecordImpl record : trackRecords) {
					record.setAsset_track_parent(targetAssetId);
					toBeStored.add(record);
				}
			}

			if (catChanged) {
				catChanged = false;
				meta.setLastSequenceNo(meta.getLastSequenceNo() + 1);
				meta.setLastYearSequenceNo(meta.getLastYearSequenceNo() + 1);
				toBeStored.add(meta);
				if (i == 0) {
					if (!isSilent()) {
						fireAssetsModified(null, null);
						if (structChanged) {
							fireStructureModified();
							structChanged = false;
						}
					}
					System.gc();
				}
				if (i++ == 8)
					i = 0;
			}
			if (!toBeDeleted.isEmpty() || !toBeStored.isEmpty())
				storeSafely(toBeDeleted.toArray(), 1000, toBeStored.toArray());
			else
				aMonitor.worked(1000);
			if (aMonitor.isCanceled())
				break;
		}
		if (i > 0 && !isSilent())
			fireAssetsModified(null, null);

		// Relations
		aMonitor.subTask(Messages.getString("MergeCatOperation.merging_relations")); //$NON-NLS-1$
		List<DerivedByImpl> derived = externalDb.obtainObjects(DerivedByImpl.class, null, null, -1);
		for (DerivedByImpl rel : derived) {
			String targetDerivative = idMap.get(rel.getDerivative());
			if (targetDerivative != null) {
				String targetOriginal = idMap.get(rel.getOriginal());
				if (targetOriginal != null) {
					rel.setDerivative(targetDerivative);
					rel.setOriginal(targetOriginal);
					if (findShadow(rel) == null)
						storeSafely(null, 500, rel);
				}
			}
			if (aMonitor.isCanceled())
				break;
		}
		List<ComposedToImpl> composed = externalDb.obtainObjects(ComposedToImpl.class, null, null, -1);
		for (ComposedToImpl rel : composed) {
			toBeStored.clear();
			String targetComposite = idMap.get(rel.getComposite());
			if (targetComposite != null) {
				List<String> newComponents = new ArrayList<String>(rel.getComponent().size());
				Iterator<String> it = rel.getComponent().iterator();
				while (it.hasNext()) {
					String component = it.next();
					String targetComponent = idMap.get(component);
					if (targetComponent != null)
						newComponents.add(targetComponent);
				}
				if (!newComponents.isEmpty()) {
					List<ComposedToImpl> shadows = dbManager.obtainObjects(ComposedToImpl.class, "composite", //$NON-NLS-1$
							targetComposite, QueryField.EQUALS);
					if (shadows.isEmpty()) {
						rel.setComponent(newComponents);
						toBeStored.add(rel);
					} else {
						ComposedToImpl shadow = shadows.get(0);
						Set<String> components = new HashSet<String>(shadow.getComponent());
						components.addAll(newComponents);
						shadow.setComponent(components);
						toBeStored.add(shadow);
					}
				}
			}
			if (!storeSafely(null, 500, toBeStored.toArray()))
				break;
		}
		List<Ghost_typeImpl> ghosts = externalDb.obtainObjects(Ghost_typeImpl.class, null, null, -1);
		for (Ghost_typeImpl ghost : ghosts) {
			String id = ghost.getStringId();
			ghost.setStringId(null);
			List<Ghost_typeImpl> set = dbManager.queryByExample(ghost);
			if (set.isEmpty()) {
				ghost.setStringId(id);
				if (!storeSafely(null, 5, ghost))
					break;
			}
		}
		aMonitor.subTask(Messages.getString("MergeCatOperation.merging_collections")); //$NON-NLS-1$
		List<GroupImpl> groupsToBeUpdated = new ArrayList<GroupImpl>();
		Map<String, GroupImpl> groupById = new HashMap<String, GroupImpl>();
		Map<String, GroupImpl> groupsByName = new HashMap<String, GroupImpl>();
		List<GroupImpl> groupSet = dbManager.obtainObjects(GroupImpl.class, "system", false, QueryField.EQUALS); //$NON-NLS-1$
		for (GroupImpl group : groupSet)
			groupsByName.put(group.getName(), group);
		List<GroupImpl> newGroups = externalDb.obtainObjects(GroupImpl.class, null, null, -1);
		for (GroupImpl group : newGroups) {
			GroupImpl shadow;
			if (group.getSystem())
				shadow = dbManager.obtainById(GroupImpl.class, group.getStringId());
			else
				shadow = groupsByName.get(group.getName());
			if (shadow != null) {
				groupsToBeUpdated.add(shadow);
				groupById.put(group.getStringId(), shadow);
			} else {
				group.clearExhibition();
				group.clearRootCollection();
				group.clearSlideshow();
				group.clearWebGallery();
				groupsToBeUpdated.add(group);
				groupById.put(group.getStringId(), group);
			}
		}

		Map<String, String> idMap1 = new HashMap<String, String>();
		Map<Object, String> inverse = new HashMap<Object, String>();
		for (SmartCollectionImpl coll : dbManager.obtainObjects(SmartCollectionImpl.class))
			if (coll.getName() != null) {
				if (coll.getAlbum())
					inverse.put(Utilities.getExternalAlbumName(coll), coll.getStringId());
				else if (!coll.getSystem())
					inverse.put(coll, coll.getStringId());
			}
		toBeStored.clear();
		toBeDeleted.clear();
		for (SmartCollectionImpl coll : externalDb.obtainObjects(SmartCollectionImpl.class)) {
			if (coll.getName() != null) {
				if (coll.getAlbum()) {
					String targetId = inverse.get(Utilities.getExternalAlbumName(coll));
					if (targetId != null) {
						// use collection in target db
						idMap1.put(coll.getStringId(), targetId);
						SmartCollectionImpl target = dbManager.obtainById(SmartCollectionImpl.class, targetId);
						LinkedHashSet<String> ids = new LinkedHashSet<String>();
						if (target.getAsset() != null)
							ids.addAll(target.getAsset());
						if (coll.getAsset() != null)
							ids.addAll(coll.getAsset());
						target.setAsset(ids);
						toBeStored.add(target);
					} else {
						// import external collection
						String groupId = coll.getGroup_rootCollection_parent();
						if (groupId != null) {
							GroupImpl shadow = groupById.get(groupId);
							if (shadow != null) {
								// assign to group in target db
								coll.setGroup_rootCollection_parent(shadow.getStringId());
								shadow.addRootCollection(coll.getStringId());
								// shadow will be stored further below
							}
						} else {
							SmartCollection parent = coll.getSmartCollection_subSelection_parent();
							if (parent != null) {
								SmartCollectionImpl newParent = null;
								String parentShadowId = inverse.get(Utilities.getExternalAlbumName(parent));
								if (parentShadowId != null)
									newParent = dbManager.obtainById(SmartCollectionImpl.class, parentShadowId);
								if (newParent != null) {
									coll.setSmartCollection_subSelection_parent(newParent);
									if (parent.removeSubSelection(coll))
										newParent.addSubSelection(coll);
									toBeStored.add(newParent);
								}
							}
						}
						aMonitor.worked(10);
						if (aMonitor.isCanceled())
							break;
						toBeStored.add(coll);
					}
				} else if (!coll.getSystem()) {
					String targetId = inverse.get(coll);
					if (targetId != null) {
						// use collection in target db
						idMap1.put(coll.getStringId(), targetId);
					} else {
						// import external collection
						String groupId = coll.getGroup_rootCollection_parent();
						if (groupId != null) {
							GroupImpl shadow = groupById.get(groupId);
							if (shadow != null) {
								// assign to group in target db
								coll.setGroup_rootCollection_parent(shadow.getStringId());
								shadow.addRootCollection(coll.getStringId());
								// shadow will be stored further below
							}
						} else {
							SmartCollection parent = coll.getSmartCollection_subSelection_parent();
							if (parent != null) {
								SmartCollectionImpl newParent = null;
								String parentShadowId = inverse.get(parent);
								if (parentShadowId != null)
									newParent = dbManager.obtainById(SmartCollectionImpl.class, parentShadowId);
								if (newParent != null) {
									coll.setSmartCollection_subSelection_parent(newParent);
									if (parent.removeSubSelection(coll))
										newParent.addSubSelection(coll);
									toBeStored.add(newParent);
								}
							}
						}
						aMonitor.worked(10);
						if (aMonitor.isCanceled())
							break;
						toBeStored.add(coll);
					}
				}
			}
		}
		aMonitor.subTask(Messages.getString("MergeCatOperation.merging_presentations")); //$NON-NLS-1$
		if (!aMonitor.isCanceled()) {
			for (SlideShowImpl show : externalDb.obtainObjects(SlideShowImpl.class, null, null, -1)) {
				IdentifiableObject presentationShadow = getPresentationShadow(show, show.getName());
				switch (duplicatePolicy) {
				case Constants.REPLACE:
					if (presentationShadow != null) {
						String groupId = ((SlideShowImpl) presentationShadow).getGroup_slideshow_parent();
						show.setStringId(presentationShadow.getStringId());
						show.setGroup_slideshow_parent(groupId);
						for (String slideId : ((SlideShowImpl) presentationShadow).getEntry()) {
							SlideImpl slide = dbManager.obtainById(SlideImpl.class, slideId);
							if (slide != null)
								toBeDeleted.add(slide);
						}
						toBeDeleted.add(presentationShadow);
					}
					transferSlideshow(groupById, show, toBeStored);
					break;
				default:
					if (presentationShadow == null)
						transferSlideshow(groupById, show, toBeStored);
					break;
				}
			}
		}
		aMonitor.worked(1000);
		if (!aMonitor.isCanceled()) {
			for (ExhibitionImpl show : externalDb.obtainObjects(ExhibitionImpl.class, null, null, -1)) {
				IdentifiableObject presentationShadow = getPresentationShadow(show, show.getName());
				switch (duplicatePolicy) {
				case Constants.REPLACE:
					if (presentationShadow != null) {
						String groupId = ((ExhibitionImpl) presentationShadow).getGroup_exhibition_parent();
						show.setStringId(presentationShadow.getStringId());
						show.setGroup_exhibition_parent(groupId);
						for (Wall wall : ((ExhibitionImpl) presentationShadow).getWall()) {
							for (String exhibitId : wall.getExhibit()) {
								ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
								if (exhibit != null)
									toBeDeleted.add(exhibit);
							}
						}
						toBeDeleted.addAll(((Exhibition) presentationShadow).getWall());
					}
					transferExhibition(groupById, show, toBeStored);
					break;
				default:
					if (presentationShadow == null)
						transferExhibition(groupById, show, toBeStored);
					break;
				}
			}
		}
		aMonitor.worked(1000);
		if (!aMonitor.isCanceled()) {
			for (WebGalleryImpl show : externalDb.obtainObjects(WebGalleryImpl.class, null, null, -1)) {
				IdentifiableObject presentationShadow = getPresentationShadow(show, show.getName());
				switch (duplicatePolicy) {
				case Constants.REPLACE:
					if (presentationShadow instanceof WebGalleryImpl) {
						String groupId = ((WebGalleryImpl) presentationShadow).getGroup_webGallery_parent();
						show.setStringId(presentationShadow.getStringId());
						show.setGroup_webGallery_parent(groupId);
						for (Storyboard storyboard : ((WebGalleryImpl) presentationShadow).getStoryboard()) {
							for (String exhibitId : storyboard.getExhibit()) {
								WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
								if (exhibit != null)
									toBeDeleted.add(exhibit);
							}
						}
						toBeDeleted.addAll(((WebGallery) presentationShadow).getStoryboard());
						toBeDeleted.addAll(((WebGallery) presentationShadow).getParameter().values());
					}
					transferWebGallery(groupById, show, toBeStored);
					break;
				default:
					if (presentationShadow == null)
						transferWebGallery(groupById, show, toBeStored);
					break;
				}
			}
		}
		aMonitor.worked(1000);
		aMonitor.subTask(Messages.getString("MergeCatOperation.finalizing")); //$NON-NLS-1$
		if (!aMonitor.isCanceled()) {
			for (GroupImpl group : groupsToBeUpdated) {
				List<String> ids = new ArrayList<String>(group.getRootCollection().size());
				boolean changed = false;
				for (String id : group.getRootCollection()) {
					String newId = idMap1.get(id);
					if (newId == null)
						ids.add(id);
					else {
						ids.add(newId);
						changed = true;
					}
				}
				if (changed) {
					group.setRootCollection(ids);
					aMonitor.worked(1);
					if (aMonitor.isCanceled())
						break;
					toBeStored.add(group);
				}
			}
		}
		meta.setCleaned(false);
		storeSafely(toBeDeleted.toArray(), 100, toBeStored.toArray());
		for (ICatalogContributor contributor : catalogContributors) {
			try {
				contributor.merge(aMonitor, dbManager, externalDb, duplicatePolicy, idMap1);
			} catch (Exception e) {
				addError(Messages.getString("MergeCatOperation.internal_error_contributor"), e); //$NON-NLS-1$
			}
			if (aMonitor.isCanceled())
				break;
			aMonitor.worked(1000);
		}
		externalDb.close(CatalogListener.NORMAL);
		fireStructureModified();
		fireAssetsModified(null, null);
		return close(info, assetsToIndex);
	}

	private void mergeGhosts(AssetImpl asset, AssetImpl targetAsset, List<Object> toBeStored) {
		List<Ghost_typeImpl> ghosts = externalDb.obtainObjects(Ghost_typeImpl.class, QueryField.NAME.getKey(),
				asset.getName(), QueryField.EQUALS);
		for (Ghost_typeImpl ghost : ghosts)
			if (ghost.getUri() != null && ghost.getUri().equals(asset.getUri())) {
				if (targetAsset != null)
					ghost.setUri(targetAsset.getUri());
				toBeStored.add(ghost);
			}
	}

	private void transferWebGallery(Map<String, GroupImpl> groupById, WebGalleryImpl show, List<Object> toBeStored) {
		String groupId = show.getGroup_webGallery_parent();
		if (groupId != null) {
			GroupImpl shadow = groupById.get(groupId);
			if (shadow != null) {
				show.setGroup_webGallery_parent(shadow.getStringId());
				shadow.addWebGallery(show.getStringId());
			}
		}
		toBeStored.add(show);
		for (Storyboard storyboard : show.getStoryboard()) {
			for (String exhibitId : storyboard.getExhibit()) {
				WebExhibitImpl exhibit = externalDb.obtainById(WebExhibitImpl.class, exhibitId);
				if (exhibit != null)
					toBeStored.add(exhibit);
			}
			toBeStored.add(storyboard);
		}
	}

	private void transferExhibition(Map<String, GroupImpl> groupById, ExhibitionImpl show, List<Object> toBeStored) {
		String groupId = show.getGroup_exhibition_parent();
		if (groupId != null) {
			GroupImpl shadow = groupById.get(groupId);
			if (shadow != null) {
				show.setGroup_exhibition_parent(shadow.getStringId());
				shadow.addExhibition(show.getStringId());
			}
		}
		toBeStored.add(show);
		for (Wall wall : show.getWall()) {
			for (String exhibitId : wall.getExhibit()) {
				ExhibitImpl exhibit = externalDb.obtainById(ExhibitImpl.class, exhibitId);
				if (exhibit != null)
					toBeStored.add(exhibit);
			}
			toBeStored.add(wall);
		}
	}

	private void transferSlideshow(Map<String, GroupImpl> groupById, SlideShowImpl show, List<Object> toBeStored) {
		String groupId = show.getGroup_slideshow_parent();
		if (groupId != null) {
			GroupImpl shadow = groupById.get(groupId);
			if (shadow != null) {
				show.setGroup_slideshow_parent(shadow.getStringId());
				shadow.addSlideshow(show.getStringId());
			}
		}
		toBeStored.add(show);
		for (String slideId : show.getEntry()) {
			SlideImpl slide = externalDb.obtainById(SlideImpl.class, slideId);
			if (slide != null)
				toBeStored.add(slide);
		}
	}

	private IdentifiableObject getPresentationShadow(IdentifiableObject show, String value) {
		Iterator<? extends IdentifiableObject> it = dbManager
				.obtainObjects(show.getClass(), "name", value, QueryField.EQUALS).iterator(); //$NON-NLS-1$
		return it.hasNext() ? it.next() : null;
	}

	private void mergeStructures(final String sourceAssetId, String targetAssetId, boolean replace,
			List<Object> toBeStored, List<Object> toBeDeleted) {
		List<IdentifiableObject> structs = new ArrayList<IdentifiableObject>(
				externalDb.obtainStructForAsset(LocationCreatedImpl.class, sourceAssetId, true));
		if (replace && structs.isEmpty()) {
			List<IdentifiableObject> rels = new ArrayList<IdentifiableObject>(
					dbManager.obtainStructForAsset(LocationShownImpl.class, targetAssetId, true));
			for (IdentifiableObject rel : rels) {
				LocationCreatedImpl locCreated = (LocationCreatedImpl) rel;
				Iterator<String> it = locCreated.getAsset().iterator();
				while (it.hasNext())
					if (targetAssetId.equals(it.next()))
						it.remove();
				if (locCreated.getAsset().isEmpty())
					toBeDeleted.add(locCreated);
				else
					toBeStored.add(locCreated);
			}
		} else
			for (IdentifiableObject struct : structs) {
				LocationCreatedImpl rel = (LocationCreatedImpl) struct;
				String locId = rel.getLocation();
				final LocationImpl loc = externalDb.obtainById(LocationImpl.class, locId);
				if (loc != null) {
					LocationImpl shadow = (LocationImpl) findShadow(loc);
					if (shadow == null) {
						toBeStored.add(loc);
						shadow = loc;
					}
					String shadowId = shadow.getStringId();
					List<IdentifiableObject> existing = new ArrayList<IdentifiableObject>(
							dbManager.obtainStruct(LocationCreatedImpl.class, targetAssetId, false,
									replace ? null : "location", shadowId, false)); //$NON-NLS-1$
					if (replace)
						for (IdentifiableObject srel : existing) {
							LocationCreatedImpl locCreated = (LocationCreatedImpl) srel;
							Iterator<String> it = locCreated.getAsset().iterator();
							while (it.hasNext())
								if (targetAssetId.equals(it.next()))
									it.remove();
							if (locCreated.getAsset().isEmpty())
								toBeDeleted.add(locCreated);
							else
								toBeStored.add(locCreated);
						}
					if (replace || existing.isEmpty()) {
						if (targetAssetId != sourceAssetId) {
							List<String> newAssetIds = new ArrayList<String>(rel.getAsset().size());
							for (String id : rel.getAsset())
								newAssetIds.add(sourceAssetId.equals(id) ? targetAssetId : id);
							rel.setAsset(newAssetIds);
						}
						rel.setLocation(shadowId);
						toBeStored.add(rel);
					}
				}
				break;
			}
		structs = new ArrayList<IdentifiableObject>(
				externalDb.obtainStructForAsset(LocationShownImpl.class, sourceAssetId, false));
		if (replace && structs.isEmpty())
			for (LocationShownImpl rel : dbManager.obtainStructForAsset(LocationShownImpl.class, targetAssetId, false))
				toBeDeleted.add(rel);
		else
			for (IdentifiableObject struct : structs) {
				LocationShownImpl rel = (LocationShownImpl) struct;
				String locId = rel.getLocation();
				final LocationImpl loc = externalDb.obtainById(LocationImpl.class, locId);
				if (loc != null) {
					LocationImpl shadow = (LocationImpl) findShadow(loc);
					if (shadow == null) {
						toBeStored.add(loc);
						shadow = loc;
					}
					String shadowId = shadow.getStringId();
					List<LocationShownImpl> existing = dbManager.obtainStruct(LocationShownImpl.class, targetAssetId,
							false, replace ? null : "location", shadowId, false); //$NON-NLS-1$
					if (replace)
						for (LocationShownImpl srel : existing)
							toBeDeleted.add(srel);
					if (replace || existing.isEmpty()) {
						rel.setAsset(targetAssetId);
						rel.setLocation(shadowId);
						toBeStored.add(rel);
					}
				}
			}
		structs = new ArrayList<IdentifiableObject>(
				externalDb.obtainStructForAsset(ArtworkOrObjectShownImpl.class, sourceAssetId, false));
		if (replace && structs.isEmpty())
			for (ArtworkOrObjectShownImpl rel : dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class,
					targetAssetId, false))
				toBeDeleted.add(rel);
		else
			for (IdentifiableObject struct : structs) {
				ArtworkOrObjectShownImpl rel = (ArtworkOrObjectShownImpl) struct;
				String artId = rel.getArtworkOrObject();
				final ArtworkOrObjectImpl art = externalDb.obtainById(ArtworkOrObjectImpl.class, artId);
				if (art != null) {
					ArtworkOrObjectImpl shadow = (ArtworkOrObjectImpl) findShadow(art);
					if (shadow == null) {
						toBeStored.add(art);
						shadow = art;
					}
					String shadowId = shadow.getStringId();
					List<ArtworkOrObjectShownImpl> existing = dbManager.obtainStruct(ArtworkOrObjectShownImpl.class,
							targetAssetId, false, replace ? null : "artworkOrObject", shadowId, false); //$NON-NLS-1$
					if (replace)
						for (ArtworkOrObjectShownImpl srel : existing)
							toBeDeleted.add(srel);
					if (replace || existing.isEmpty()) {
						rel.setAsset(targetAssetId);
						rel.setArtworkOrObject(shadowId);
						toBeStored.add(rel);
					}
				}
			}
		structs = new ArrayList<IdentifiableObject>(
				externalDb.obtainStructForAsset(CreatorsContactImpl.class, sourceAssetId, true));
		if (replace && structs.isEmpty())
			for (CreatorsContactImpl rel : dbManager.obtainStructForAsset(CreatorsContactImpl.class, targetAssetId,
					true)) {
				CreatorsContactImpl creatorsContact = rel;
				Iterator<String> it = creatorsContact.getAsset().iterator();
				while (it.hasNext())
					if (targetAssetId.equals(it.next()))
						it.remove();
				if (creatorsContact.getAsset().isEmpty())
					toBeDeleted.add(creatorsContact);
				else
					toBeStored.add(creatorsContact);
			}
		else
			for (IdentifiableObject struct : structs) {
				CreatorsContactImpl rel = (CreatorsContactImpl) struct;
				String contactId = rel.getContact();
				final ArtworkOrObjectImpl art = externalDb.obtainById(ArtworkOrObjectImpl.class, contactId);
				if (art != null) {
					ArtworkOrObjectImpl shadow = (ArtworkOrObjectImpl) findShadow(art);
					if (shadow == null) {
						toBeStored.add(art);
						shadow = art;
					}
					String shadowId = shadow.getStringId();
					List<CreatorsContactImpl> existing = dbManager.obtainStruct(CreatorsContactImpl.class,
							targetAssetId, false, replace ? null : "contact", shadowId, false); //$NON-NLS-1$
					if (replace)
						for (CreatorsContactImpl srel : existing) {
							CreatorsContactImpl creatorsContact = srel;
							Iterator<String> it = creatorsContact.getAsset().iterator();
							while (it.hasNext()) {
								String assetId = it.next();
								if (targetAssetId.equals(assetId))
									it.remove();
							}
							if (creatorsContact.getAsset().isEmpty())
								toBeDeleted.add(creatorsContact);
							else
								toBeStored.add(creatorsContact);
						}
					if (replace || existing.isEmpty()) {
						if (targetAssetId != sourceAssetId) {
							List<String> newAssetIds = new ArrayList<String>(rel.getAsset().size());
							for (String id : rel.getAsset())
								newAssetIds.add(sourceAssetId.equals(id) ? targetAssetId : id);
							rel.setAsset(newAssetIds);
						}
						rel.setContact(shadowId);
						toBeStored.add(rel);
					}
				}
				break;
			}
	}

	private void mergeRegions(final Asset sourceAsset, Asset targetAsset, boolean replace, List<Object> toBeStored,
			List<Object> toBeDeleted) {
		String sourceAssetId = sourceAsset.getStringId();
		String targetAssetId = targetAsset == null ? sourceAssetId : targetAsset.getStringId();
		if (replace || targetAsset == null) {
			toBeDeleted.addAll(dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", targetAssetId, //$NON-NLS-1$
					QueryField.EQUALS));
			toBeStored.addAll(externalDb.obtainObjects(RegionImpl.class, "asset_person_parent", sourceAssetId, //$NON-NLS-1$
					QueryField.EQUALS));
		}
	}

	private void mergeExtensions(final Asset sourceAsset, Asset targetAsset, boolean replace, List<Object> toBeStored,
			List<Object> toBeDeleted) {
		String sourceAssetId = sourceAsset.getStringId();
		String targetAssetId = targetAsset == null ? sourceAssetId : targetAsset.getStringId();
		if (replace || targetAsset == null) {
			toBeDeleted.addAll(dbManager.obtainObjects(MediaExtension.class, "asset_parent", targetAssetId, //$NON-NLS-1$
					QueryField.EQUALS));
			toBeStored.addAll(externalDb.obtainObjects(MediaExtension.class, "asset_parent", sourceAssetId, //$NON-NLS-1$
					QueryField.EQUALS));
		}
	}

	private IdentifiableObject findShadow(final IdentifiableObject orig) {
		String id = orig.getStringId();
		orig.setStringId(null);
		Iterator<IdentifiableObject> it = dbManager.queryByExample(orig).iterator();
		orig.setStringId(id);
		return (it.hasNext()) ? it.next() : null;
	}

	private void mergeAsset(AssetImpl source, AssetImpl target) {
		if (xmpFilter != null) {
			for (QueryField queryField : QueryField.getQueryFields()) {
				if (!queryField.isVirtual() && xmpFilter.contains(queryField))
					switch (metaDataPolicy) {
					case Constants.SKIP:
						Object oldValue = queryField.obtainPlainFieldValue(target);
						if (!queryField.isNeutralValue(oldValue))
							continue;
						//$FALL-THROUGH$
					case Constants.REPLACE:
						Object newValue = queryField.obtainPlainFieldValue(source);
						queryField.setPlainFieldValue(target, newValue);
						break;
					default:
						queryField.mergeValues(source, target);
						break;
					}
			}
		}
	}

	public int getExecuteProfile() {
		return (duplicatePolicy == Constants.SKIP) ? IProfiledOperation.CAT
				: IProfiledOperation.CONTENT | IProfiledOperation.CAT;
	}

	public int getUndoProfile() {
		return (duplicatePolicy == Constants.SKIP) ? IProfiledOperation.CAT
				: IProfiledOperation.CONTENT | IProfiledOperation.CAT;
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		// do nothing
	}

}
