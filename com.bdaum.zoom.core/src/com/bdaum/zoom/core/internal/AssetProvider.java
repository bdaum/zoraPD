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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.BusyIndicator;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;

public class AssetProvider implements IAssetProvider {

	private static final String[] EMPTYIDS = new String[0];
	private List<Asset> assets;
	private int count = -1;
	private boolean empty;
	private boolean emptydefined = false;
	private SmartCollectionImpl currentCollection;
	private ICollectionProcessor currentProcessor;
	private String lastSelection;
	private IAssetFilter[] currentFilters = null;
	private final IDbManager dbManager;
	private SortCriterion currentSort;
	private boolean invalid = false;
	private Map<String, AssetProvider> cloneMap;

	public AssetProvider(IDbManager dbManager) {
		this.dbManager = dbManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IAssetProvider#invalidate(com.bdaum.zoom.core.QueryField
	 * )
	 */
	public boolean invalidate(QueryField node) {
		if (node == QueryField.ALL || node == QueryField.ALBUMASSETS && currentCollection.getAlbum())
			invalid = true;
		else {
			String key = node.getKey();
			if (currentCollection != null)
				invalid |= isAffectedBy(currentCollection, key);
			if (!invalid) {
				SortCriterion customSort = currentProcessor == null ? null : currentProcessor.getCustomSort();
				invalid = (customSort != null && !customSort.getField().equals(key));
			}
		}
		if (cloneMap != null)
			for (AssetProvider child : cloneMap.values())
				child.invalidate(node);
		if (invalid)
			return true;
		QueryField[] children = node.getChildren();
		if (children != null)
			for (QueryField child : children)
				if (invalidate(child))
					return true;
		return false;
	}

	private static boolean isAffectedBy(SmartCollection sm, String key) {
		for (Criterion crit : sm.getCriterion())
			if (crit.getField().equals(key))
				return true;
		for (SortCriterion crit : sm.getSortCriterion())
			if (crit.getField().equals(key))
				return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#loadCollection(java.lang.String)
	 */

	public SmartCollectionImpl loadCollection(String id) {
		return currentCollection = dbManager.obtainById(SmartCollectionImpl.class, lastSelection = id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getLastSelection()
	 */

	public String getLastSelection() {
		return lastSelection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#selectAssets()
	 */

	public void selectAssets() {
		if (currentCollection == null)
			return;
		if (currentProcessor == null || currentProcessor.getCollection() != currentCollection
				|| !Arrays.equals(currentProcessor.getFilters(), currentFilters)
				|| (currentSort == null && currentProcessor.getCustomSort() != null)
				|| (currentSort != null && !currentSort.equals(currentProcessor.getCustomSort()))) {
			BusyIndicator.showWhile(null, () -> {
				currentProcessor = dbManager.createCollectionProcessor(currentCollection, currentFilters, currentSort);
				assets = currentProcessor.select(true);
				count = -1;
				emptydefined = false;
			});
		} else if (invalid)
			BusyIndicator.showWhile(null, () -> {
				assets = currentProcessor.select(true);
				count = -1;
				emptydefined = false;
			});
		invalid = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getAssetCount()
	 */

	public int getAssetCount() {
		if (count < 0)
			count = (assets != null) ? assets.size() : 0;
		return count;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#isEmpty()
	 */

	public boolean isEmpty() {
		if (assets == null)
			return true;
		if (count >= 0)
			return count == 0;
		if (emptydefined)
			return empty;
		emptydefined = true;
		return empty = !assets.iterator().hasNext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#hasLocalAssets()
	 */
	public Asset getFirstLocalAsset() {
		if (assets != null)
			for (Asset a : assets)
				if (a.getFileState() != IVolumeManager.PEER)
					return a;
		return null;
	}

	public List<Asset> getLocalAssets() {
		if (assets != null) {
			if (Core.getCore().isNetworked()) {
				List<Asset> localAssets = new ArrayList<Asset>(assets.size());
				for (Asset a : assets)
					if (a.getFileState() != IVolumeManager.PEER)
						localAssets.add(a);
				return localAssets;
			}
			return assets;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getAsset(int)
	 */

	public Asset getAsset(int index) {
		return (assets == null) ? null : (index < assets.size() ? assets.get(index) : null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#resetProcessor()
	 */

	public void resetProcessor() {
		if (currentProcessor != null)
			currentProcessor.reset();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getAssets()
	 */

	public List<Asset> getAssets() {
		return assets;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getAssetIds()
	 */

	public String[] getAssetIds() {
		if (assets != null) {
			int i = 0;
			String[] ids = new String[assets.size()];
			for (Asset asset : assets)
				ids[i++] = asset.getStringId();
			return ids;
		}
		return EMPTYIDS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getCurrentCollection()
	 */

	public SmartCollectionImpl getCurrentCollection() {
		return currentCollection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#setCurrentCollection(com.bdaum.zoom
	 * .cat.model.group.SmartCollectionImpl)
	 */

	public void setCurrentCollection(SmartCollectionImpl currentCollection) {
		this.currentCollection = currentCollection;
		if (currentCollection != null)
			lastSelection = currentCollection.getStringId();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#setCurrentFilter(com.bdaum.zoom.core
	 * .IAssetFilter)
	 */

	public void setCurrentFilters(IAssetFilter[] filters) {
		this.currentFilters = filters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#canProvideAssets()
	 */

	public boolean canProvideAssets() {
		return assets != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getCurrentSort()
	 */

	public SortCriterion getCurrentSort() {
		return currentSort;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#setCurrentSort(com.bdaum.zoom.cat.
	 * model.group.SortCriterion)
	 */

	public void setCurrentSort(SortCriterion currentSort) {
		this.currentSort = currentSort;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getScoreFormatter()
	 */

	public IScoreFormatter getScoreFormatter() {
		return currentProcessor == null ? null : currentProcessor.getScoreFormatter();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IAssetProvider#getParentCollection()
	 */

	public SmartCollectionImpl getParentCollection() {
		SmartCollectionImpl coll = getCurrentCollection();
		return (coll == null || coll.getCriterion().size() == 1 && coll.getCriterion(0).getField().startsWith("*")) //$NON-NLS-1$
				? null
				: coll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IAssetProvider#indexOf(com.bdaum.zoom.cat.model.asset
	 * .Asset)
	 */

	public int indexOf(Asset o) {
		return indexOf(o.getStringId());
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.IAssetProvider#indexOf(java.lang.String)
	 */
	public int indexOf(String assetId) {
		if (assets != null) {
			int i = 0;
			for (Asset asset : assets) {
				if (asset.getStringId().equals(assetId))
					return i;
				++i;
			}
		}
		return -1;
	}

	/**
	 * Creates or retrieves a slave provider for the given ID
	 *
	 * @param id
	 *            - owner ID
	 * @return - slave asset provider
	 */
	public IAssetProvider getSlave(String id) {
		if (cloneMap == null)
			cloneMap = new HashMap<String, AssetProvider>(5);
		AssetProvider clone = cloneMap.get(id);
		if (clone == null)
			cloneMap.put(id, clone = new AssetProvider(dbManager));
		return clone;
	}

}
