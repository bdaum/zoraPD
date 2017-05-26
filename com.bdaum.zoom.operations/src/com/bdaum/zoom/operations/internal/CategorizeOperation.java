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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class CategorizeOperation extends DbOperation {

	private static final String[] EMPTYSTRINGARRAY = new String[0];
	private List<Asset> assets;
	private Map<String, Category> categories;
	private CatResult[] catResults;
	private String[][] oldKeywords;
	private String[][] oldSupplemental;
	private String[] oldPrimary;
	private int assetCount;
	private Meta meta;
	private HashSet<String> metaKeywordBackup;
	private Map<String, Category> oldCategories;
	private int[] oldPrivacy;
	private int[] oldNoPersons;
	private String[] oldDescription;
	private String[][] oldRegionIds;
	private List<Region> deletedRegions = new ArrayList<>();
	private List<Region> newRegions = new ArrayList<>();

	public CategorizeOperation(List<Asset> assets, CatResult[] catResults, Map<String, Category> categories,
			Map<String, Category> oldCategories) {
		super(Messages.getString("CategorizeOperation.categorize_images")); //$NON-NLS-1$
		this.assets = assets;
		this.catResults = catResults;
		this.categories = categories;
		this.oldCategories = oldCategories;
		assetCount = assets.size();
		oldPrimary = new String[assetCount];
		oldSupplemental = new String[assetCount][];
		oldKeywords = new String[assetCount][];
		oldPrivacy = new int[assetCount];
		oldNoPersons = new int[assetCount];
		oldDescription = new String[assetCount];
		oldRegionIds = new String[assetCount][];
		meta = dbManager.getMeta(true);
	}

	@Override
	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.META;
	}

	@Override
	public int getUndoProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.META;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		init(monitor, assetCount);
		List<Object> toBeDeleted = new ArrayList<Object>();
		List<Object> toBeStored = new ArrayList<Object>();
		int i = 0;
		for (Asset asset : assets) {
			oldPrimary[i] = asset.getCategory();
			oldSupplemental[i] = asset.getSupplementalCats();
			oldPrivacy[i] = asset.getSafety();
			oldNoPersons[i] = asset.getNoPersons();
			oldDescription[i] = asset.getImageDescription();
			oldRegionIds[i] = asset.getPerson();
			CatResult catResult = catResults[i];
			asset.setCategory(catResult.getPrimary());
			asset.setSupplementalCats(catResult.getSupplemental());
			int privacy = catResult.getPrivacy();
			asset.setSafety(privacy == 2 ? QueryField.SAFETY_RESTRICTED
					: privacy == 1 ? QueryField.SAFETY_MODERATE : QueryField.SAFETY_SAFE);
			if (catResult.getDescription() != null)
				asset.setImageDescription(catResult.getDescription());
			Set<String> proposedKeywords = catResult.getKeywords();
			if (proposedKeywords != null) {
				oldKeywords[i] = asset.getKeyword();
				if (oldKeywords[i] == null)
					oldKeywords[i] = EMPTYSTRINGARRAY;
				Set<String> set = new HashSet<>(Arrays.asList(asset.getKeyword()));
				set.addAll(proposedKeywords);
				String[] newKeywords = set.toArray(new String[set.size()]);
				Arrays.sort(newKeywords);
				asset.setKeyword(newKeywords);
				if (metaKeywordBackup == null)
					metaKeywordBackup = new HashSet<String>(meta.getKeywords());
				meta.getKeywords().addAll(proposedKeywords);
			}
			Rectangle imageBounds = catResult.getImageBounds();
			List<Rectangle> newFaces = catResult.getNewFaces();
			if (imageBounds != null && newFaces != null) {
				String[] regionIds = asset.getPerson();
				Set<String> regionSet = new HashSet<>();
				if (regionIds != null && regionIds.length > 0) {
					List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
							asset.getStringId(), QueryField.EQUALS);
					for (RegionImpl region : regions) {
						String type = region.getType();
						if (type == null || type.equals(Region.type_face)) {
							toBeDeleted.add(region);
							deletedRegions.add(region);
						} else
							regionSet.add(region.getStringId());
					}
				}
				for (Rectangle face : newFaces) {
					double x = (double) face.x / imageBounds.width;
					double w = (double) face.width / imageBounds.width;
					double y = (double) face.y / imageBounds.height;
					double h = (double) face.height / imageBounds.height;
					String rect64 = Utilities.toRect64(x, y, w, h);
					regionSet.add(rect64);
					Region region = new RegionImpl(meta.getPersonsToKeywords(), null, null, null, Region.type_face,
							null);
					region.setAsset_person_parent(asset.getStringId());
					region.setStringId(rect64);
					toBeStored.add(region);
					newRegions.add(region);
				}
				asset.setPerson(regionSet.toArray(new String[regionSet.size()]));
				asset.setNoPersons(regionSet.size());
			}
			toBeStored.add(asset);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			if (++i % 10 == 0) {
				if (metaKeywordBackup != null)
					toBeStored.add(meta);
				dbManager.safeTransaction(null, toBeStored);
				toBeStored.clear();
			}
			monitor.worked(1);
		}
		if (categories != null && oldCategories != null) {
			Utilities.updateCategories(dbManager, oldCategories, categories, toBeDeleted, toBeStored);
			meta.setCategory(categories);
		}
		if (metaKeywordBackup != null || categories != null && oldCategories != null)
			toBeStored.add(meta);
		if (!toBeStored.isEmpty() || !toBeDeleted.isEmpty())
			dbManager.safeTransaction(toBeDeleted, toBeStored);
		fireApplyRules(assets, null);
		fireAssetsModified(new BagChange<>(null, assets, null, null), null);
		fireStructureModified();
		return close(info, assets);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		initUndo(monitor, assetCount + 2);
		List<Object> toBeDeleted = new ArrayList<Object>(newRegions);
		List<Object> toBeStored = new ArrayList<Object>(deletedRegions);
		newRegions.clear();
		deletedRegions.clear();
		int i = 0;
		for (Asset asset : assets) {
			asset.setCategory(oldPrimary[i]);
			asset.setSupplementalCats(oldSupplemental[i]);
			if (oldKeywords[i] != null)
				asset.setKeyword(oldKeywords[i]);
			asset.setImageDescription(oldDescription[i]);
			asset.setSafety(oldPrivacy[i]);
			asset.setNoPersons(oldNoPersons[i]);
			asset.setPerson(oldRegionIds[i]);
			toBeStored.add(asset);
			if (++i % 10 == 0) {
				dbManager.safeTransaction(null, toBeStored);
				toBeStored.clear();
			}
			monitor.worked(1);
		}
		if (categories != null && oldCategories != null) {
			Utilities.updateCategories(dbManager, oldCategories, categories, toBeDeleted, toBeStored);
			meta.setCategory(categories);
		}
		if (metaKeywordBackup != null || categories != null && oldCategories != null) {
			if (metaKeywordBackup != null)
				meta.setKeywords(metaKeywordBackup);
			toBeStored.add(meta);
		}
		monitor.worked(1);
		if (!toBeStored.isEmpty() && !toBeDeleted.isEmpty())
			dbManager.safeTransaction(toBeDeleted, toBeStored);
		return close(info, assets);
	}

	@Override
	public int getPriority() {
		return assetCount > 3 ? Job.LONG : Job.SHORT;
	}
}
