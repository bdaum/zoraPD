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
package com.bdaum.zoom.core;

import java.util.List;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;

public interface IAssetProvider {

	/**
	 * Invalidates the current collection if the collection is affected by a
	 * value change of the specified node
	 *
	 * @param node
	 *            - node whose value was modified
	 * @return - true if invalid
	 */
	boolean invalidate(QueryField node);

	/**
	 * Loads the specified collection
	 *
	 * @param id
	 *            - collection id
	 * @return - collection
	 */
	SmartCollectionImpl loadCollection(String id);

	/**
	 * Sets the current collection
	 *
	 * @param currentCollection
	 */
	void setCurrentCollection(SmartCollectionImpl currentCollection);

	/**
	 * Returns the current collection
	 *
	 * @return the current collection
	 */
	SmartCollectionImpl getCurrentCollection();

	/**
	 * Returns the id of the last collection set
	 *
	 * @return collection id
	 */
	String getLastSelection();

	/**
	 * Evaluates the current collection querz
	 */
	void selectAssets();

	/**
	 * Returns the number of resulting assets
	 *
	 * @return number of assets
	 */
	int getAssetCount();

	/**
	 * Tests if the collection is emptz
	 *
	 * @return true if emptz
	 */
	boolean isEmpty();

	/**
	 * Returns the asset at the specified index
	 *
	 * @param index
	 * @return asset
	 */
	Asset getAsset(int index);

	/**
	 * Resets the query processor. Typically used after the catalog content or
	 * the catalog structure was modified
	 */
	void resetProcessor();

	/**
	 * Returns all assets resulting from the query
	 * @return - resulting assets
	 */
	List<Asset> getAssets();

	/**
	 * Returns the IDs of all assets resulting from the query
	 * @return - asset IDs
	 */
	String[] getAssetIds();

	/**
	 * Sets filters for narrowing the selection criteria in the current collection
	 * @param filters - filter object, must not be null, use IAssetFilter.NULLFILTER instead
	 */
	void setCurrentFilters(IAssetFilter[] filters);

	/**
	 * Test of this provider is in a state so that it can return assets
	 * @return true if ready to return assets
	 */
	boolean canProvideAssets();

	/**
	 * Returns the current sort criterion
	 * @return current sort criterion
	 */
	SortCriterion getCurrentSort();

	/**
	 * Sets the current sort criterion
	 * @param currentSort - new sort criterion
	 */
	void setCurrentSort(SortCriterion currentSort);

	/**
	 * Returns the score formatter for the current collection or null if there is none
	 * @return score formatter or null
	 */
	IScoreFormatter getScoreFormatter();

	/**
	 * Returns the parent collection or null if there is none
	 * @return parent collection or null
	 */
	SmartCollectionImpl getParentCollection();

	/**
	 * Finds the index of the specified asset
	 * @param asset
	 * @return index of asset
	 */
	int indexOf(Asset asset);

	/**
	 * Finds the index of the specified asset
	 * @param asset ID
	 * @return index of asset
	 */
	int indexOf(String assetId);


	/**
	 * Returns the first asset not owned by a peer or null
	 * @return - the first asset not owned by a peer or null
	 */
	Asset getFirstLocalAsset();

	/**
	 * Returns a list of assets not owned by a peer or null
	 * @return list of assets not owned by a peer or null
	 */
	List<Asset> getLocalAssets();

}