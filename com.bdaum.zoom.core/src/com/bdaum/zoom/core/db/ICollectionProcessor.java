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
package com.bdaum.zoom.core.db;

import java.util.List;
import java.util.Set;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.Messages;

public interface ICollectionProcessor {

	SmartCollectionImpl EMPTYCOLLECTION = new SmartCollectionImpl(
			Messages.CollectionProcessor_Empty, false, false, false, false,
			null, 0, null, 0, null, null);
	String SIMILARITY = "*similarity"; //$NON-NLS-1$
	String TEXTSEARCH = "*textsearch"; //$NON-NLS-1$
	String ORPHANS = "*orphans"; //$NON-NLS-1$

	/**
	 * Adds a query post processor
	 *
	 * @param processor
	 *            - the post processor
	 */
	void addPostProcessor(IPostProcessor processor);

	/**
	 * Returns the current collection processed by this processor
	 *
	 * @return current collection
	 */
	SmartCollection getCollection();

	/**
	 * Reset the processor for a new query evaluation
	 */
	void reset();

	/**
	 * Performs the query specified by the current collection
	 *
	 * @param sorted
	 *            - true if the sort specified in the collection query
	 *            should be performed, false when no sort or only custom sorts are required
	 * @return resulting assets
	 */
	List<Asset> select(boolean sorted);

	/**
	 * Returns the current filters used for narrowing down the query result
	 *
	 * @return current filter
	 */
	IAssetFilter[] getFilters();

	/**
	 * Returns the current custom sort or null
	 *
	 * @return custom sort or null
	 */
	SortCriterion getCustomSort();

	/**
	 * Returns the current score formatter or null
	 *
	 * @return score formatter or null
	 */
	IScoreFormatter getScoreFormatter();

	/**
	 * Performs an isolated content search
	 *
	 * @param criterion
	 *            - search criterion
	 * @param preselectedIds
	 *            - Set of asset ids in which the search is performed or null
	 *            for all assets
	 * @param filters
	 *            - asset filters for restricting the result set
	 * @return - list of found assets sorted by score
	 */
	List<Asset> processContentSearch(Criterion criterion,
			Set<String> preselectedIds, IAssetFilter[] filters);

	/**
	 * Tests if this collection has elements
	 *
	 * @return - true if the collection has no elements
	 */
	boolean isEmpty();

}