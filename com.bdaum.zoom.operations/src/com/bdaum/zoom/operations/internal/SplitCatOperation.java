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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ICatalogContributor;
import com.bdaum.zoom.core.internal.Theme;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;

@SuppressWarnings("restriction")
public class SplitCatOperation extends AbstractCloneCatOperation {

	final IDbManager newDbManager;
	final String description;
	private final List<Asset> selectedAssets;
	private final String timeline;
	private Set<String> visited = new HashSet<String>();
	private final String locationOptions;

	public SplitCatOperation(IDbManager newDbManager, String description, List<Asset> selectedAssets, String timeline,
			String locationOptions) {
		super(Messages.getString("SplitCatOperation.splitting_cat")); //$NON-NLS-1$
		this.newDbManager = newDbManager;
		this.description = description;
		this.selectedAssets = selectedAssets;
		this.timeline = timeline;
		this.locationOptions = locationOptions;
	}

	@SuppressWarnings("resource")
	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		Collection<ICatalogContributor> contributors = CoreActivator.getDefault().getCatalogContributors();
		final int na = selectedAssets.size();
		init(aMonitor, na * 2000 + 2000 + 1000 * contributors.size());
		aMonitor.subTask(Messages.getString("SplitCatOperation.preparing_cat")); //$NON-NLS-1$
		final Meta oldMeta = dbManager.getMeta(true);
		Meta newMeta = newDbManager.getMeta(true);
		List<Object> toBeStored = cloneMeta(newDbManager, oldMeta, newMeta, description);
		List<Object> toBeDeleted = new ArrayList<>();
		Theme currentTheme = CoreActivator.getDefault().getCurrentTheme();
		InputStream in = Utilities.openPropertyFile(currentTheme.getKeywords());
		if (in != null)
			try {
				List<String> loadedKeywords = Utilities.loadKeywords(in);
				newMeta.setKeywords(loadedKeywords == null ? new ArrayList<String>() : loadedKeywords);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		Utilities.obtainCatGroup(newDbManager, toBeDeleted, toBeStored);
		in = Utilities.openPropertyFile(currentTheme.getCategories());
		if (in != null)
			try {
				Utilities.loadCategories(newDbManager, newMeta.getCategory(), in, toBeStored);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// do nothing
				}
			}

		for (Object object : toBeDeleted)
			newDbManager.delete(object);
		for (Object object : toBeStored)
			newDbManager.store(object);
		newDbManager.store(newMeta);
		aMonitor.worked(1000);
		Date now = new Date();
		Utilities.initSystemCollections(newDbManager);
		newDbManager.createLastImportCollection(now, false,
				NLS.bind(Messages.getString("SplitCatOperation.initial_import"), dbManager.getFile())); //$NON-NLS-1$
		newMeta.setLastImport(now);
		newDbManager.storeAndCommit(newMeta);
		aMonitor.worked(1000);
		int i = 0;
		for (Asset asset : selectedAssets) {
			aMonitor.subTask(NLS.bind(Messages.getString("SplitCatOperation.transferring_image"), ++i, na)); //$NON-NLS-1$
			transferAsset(dbManager, newDbManager, asset, timeline, locationOptions, now, newMeta);
			if (aMonitor.isCanceled())
				return abort();
			aMonitor.worked(1000);
		}
		aMonitor.subTask(Messages.getString("SplitCatOperation.transferring_image_relationships")); //$NON-NLS-1$
		List<DerivedByImpl> derived = dbManager.obtainObjects(DerivedByImpl.class);
		for (DerivedByImpl rel : derived) {
			if (visited.contains(rel.getDerivative())) {
				if (visited.contains(rel.getOriginal()))
					newDbManager.storeAndCommit(rel);
				if (aMonitor.isCanceled())
					return abort();
				aMonitor.worked(500);
			}
		}
		List<ComposedToImpl> set = dbManager.obtainObjects(ComposedToImpl.class);
		for (ComposedToImpl rel : set) {
			if (visited.contains(rel.getComposite())) {
				List<String> components = new LinkedList<String>(rel.getComponent());
				Iterator<String> it = components.iterator();
				while (it.hasNext()) {
					String id = it.next();
					if (!visited.contains(id))
						it.remove();
				}
				if (!components.isEmpty()) {
					List<String> backup = rel.getComponent();
					rel.setComponent(components);
					newDbManager.storeAndCommit(rel);
					rel.setComponent(backup);
				}
				if (aMonitor.isCanceled())
					return abort();
				aMonitor.worked(500);
			}
		}
		aMonitor.subTask(Messages.getString("SplitCatOperation.transfering_collections")); //$NON-NLS-1$
		lp: for (SmartCollectionImpl coll : dbManager.obtainObjects(SmartCollectionImpl.class)) {
			if (!coll.getSystem() || coll.getAlbum()) {
				SmartCollection parent = coll;
				while (parent != null) {
					if (parent.getSmartCollection_subSelection_parent() != null)
						parent = parent.getSmartCollection_subSelection_parent();
					else {
						if (parent.getGroup_rootCollection_parent() == Constants.GROUP_ID_IMPORTS)
							continue lp;
						newDbManager.store(coll);
						Utilities.storeCollection(coll, true, toBeStored);
						for (Object object : toBeStored)
							newDbManager.store(object);
						newDbManager.commit();
						if (aMonitor.isCanceled())
							return abort();
					}
				}
			}
		}
		if (aMonitor.isCanceled())
			return abort();
		for (ICatalogContributor contributor : contributors) {
			try {
				contributor.split(aMonitor, dbManager, newDbManager);
			} catch (Exception e) {
				addError(Messages.getString("SplitCatOperation.internal_error_split_contributor"), e); //$NON-NLS-1$
			}
			if (aMonitor.isCanceled())
				return abort();
			aMonitor.worked(1000);
		}

		List<SmartCollectionImpl> tobeChecked = new ArrayList<SmartCollectionImpl>();
		for (SmartCollectionImpl coll : newDbManager.obtainObjects(SmartCollectionImpl.class)) {
			SmartCollection root = getRootCollection(coll);
			String grp = root.getGroup_rootCollection_parent();
			if (Constants.GROUP_ID_IMPORTS.equals(grp) && !root.getSystem()
					|| Constants.GROUP_ID_TIMELINE.equals(grp) && root.getSystem()
					|| Constants.GROUP_ID_FOLDERSTRUCTURE.equals(grp) && root.getSystem())
				tobeChecked.add(coll);
		}
		for (SmartCollectionImpl sm : tobeChecked) {
			newDbManager.pruneSystemCollection(sm);
			if (aMonitor.isCanceled())
				return abort();
		}
		newMeta.setCleaned(true);
		newDbManager.safeTransaction(null, newMeta);
		return close(info);
	}

	private void transferAsset(IDbManager db, IDbManager newDb, Asset asset, String aTimeLine, String locationOption,
			Date now, Meta_type meta) {
		List<Object> toBeStored = new ArrayList<Object>();
		Date oldImportDate = asset.getImportDate();
		asset.setImportDate(now);
		toBeStored.add(asset);
		FilterChain keywordFilter = QueryField.getKeywordFilter();
		meta.getKeywords().addAll(keywordFilter.filter(asset.getKeyword()));
		String assetId = asset.getStringId();
		visited.add(assetId);
		for (MediaExtension mediaExt : asset.getMediaExtension())
			toBeStored.add(mediaExt);
		List<Ghost_typeImpl> ghosts = db.obtainObjects(Ghost_typeImpl.class, QueryField.NAME.getKey(), asset.getName(),
				QueryField.EQUALS);
		for (Ghost_typeImpl ghost : ghosts)
			if (ghost.getUri() != null && ghost.getUri().equals(asset.getUri()))
				toBeStored.add(ghost);
		transferAssetRelations(db, newDb, toBeStored, locationOption, assetId);
		newDb.createFolderHierarchy(asset);
		newDb.createTimeLine(asset, aTimeLine);
		meta.setLastSequenceNo(meta.getLastSequenceNo() + 1);
		meta.setLastYearSequenceNo(meta.getLastYearSequenceNo() + 1);
		toBeStored.add(meta);
		asset.setImportDate(oldImportDate);
		newDb.safeTransaction(null, toBeStored);
	}

	protected void transferAssetRelations(IDbManager db, IDbManager newDb, List<Object> toBeStored,
			String locationOption, String assetId) {
		for (ArtworkOrObjectShownImpl rel : db.obtainStructForAsset(ArtworkOrObjectShownImpl.class, assetId, false)) {
			if (newDb.obtainById(ArtworkOrObjectShownImpl.class, rel.getStringId()) == null) {
				String id = rel.getArtworkOrObject();
				ArtworkOrObjectImpl art = db.obtainById(ArtworkOrObjectImpl.class, id);
				if (art != null) {
					if (newDb.obtainById(ArtworkOrObjectImpl.class, id) == null)
						toBeStored.add(art);
					toBeStored.add(rel);
				}
			}
		}
		for (LocationShownImpl rel : db.obtainStructForAsset(LocationShownImpl.class, assetId, false)) {
			if (newDb.obtainById(LocationShownImpl.class, rel.getStringId()) == null)
				if (newDb.obtainById(LocationShownImpl.class, rel.getStringId()) == null) {
					String locId = rel.getLocation();
					LocationImpl loc = db.obtainById(LocationImpl.class, locId);
					if (loc != null) {
						if (newDb.obtainById(LocationImpl.class, locId) == null)
							toBeStored.add(loc);
						toBeStored.add(rel);
					}
				}
		}
		for (LocationCreatedImpl rel : db.obtainStructForAsset(LocationCreatedImpl.class, assetId, true)) {
			LocationCreatedImpl obj = newDb.obtainById(LocationCreatedImpl.class, rel.getStringId());
			if (obj == null) {
				obj = new LocationCreatedImpl(rel.getLocation());
				obj.setStringId(rel.getStringId());
			}
			if (obj.getAsset().isEmpty()) {
				String id = rel.getLocation();
				LocationImpl loc = db.obtainById(LocationImpl.class, id);
				if (loc != null && newDb.obtainById(LocationImpl.class, id) == null) {
					toBeStored.add(loc);
					if (locationOption != null)
						newDb.createLocationFolders(loc, locationOption);
					newDb.markSystemCollectionsForPurge(loc);
				}
			}
			obj.addAsset(assetId);
			toBeStored.add(obj);
		}
		for (CreatorsContactImpl rel : db.obtainStructForAsset(CreatorsContactImpl.class, assetId, true)) {
			CreatorsContactImpl obj = newDb.obtainById(CreatorsContactImpl.class, rel.getStringId());
			if (obj == null) {
				obj = new CreatorsContactImpl(rel.getContact());
				obj.setStringId(rel.getStringId());
			}
			if (obj.getAsset().isEmpty()) {
				String id = rel.getContact();
				ContactImpl cont = db.obtainById(ContactImpl.class, id);
				if (cont != null && newDb.obtainById(ContactImpl.class, id) == null)
					toBeStored.add(cont);
			}
			obj.addAsset(assetId);
			toBeStored.add(obj);
		}
		for (TrackRecordImpl record : db.obtainObjects(TrackRecordImpl.class, "asset_track_parent", assetId, //$NON-NLS-1$
				QueryField.EQUALS))
			toBeStored.add(record);
		for (RegionImpl regionImpl : db.obtainObjects(RegionImpl.class, "asset_person_parent", assetId, //$NON-NLS-1$
				QueryField.EQUALS))
			toBeStored.add(regionImpl);
	}

}
