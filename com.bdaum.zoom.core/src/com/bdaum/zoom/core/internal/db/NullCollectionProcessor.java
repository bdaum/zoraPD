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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core.internal.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.db.ICollectionProcessor;

public class NullCollectionProcessor implements ICollectionProcessor {

	private static final List<Asset> EMPTY = new ArrayList<Asset>(0);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.ICollectionProcessor#addPostProcessor(com.bdaum.zoom
	 * .core.IPostProcessor)
	 */

	public void addPostProcessor(IPostProcessor processor) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#select(boolean)
	 */

	public List<Asset> select(boolean isSorted) {
		return EMPTY;
	}

	public List<Asset> processContentSearch(Criterion criterion,
			Set<String> preselectedIds, IAssetFilter[] filters) {
		return EMPTY;
	}

	public SmartCollection getCollection() {
		return null;
	}

	public void reset() {
		// no op
	}

	public IAssetFilter[] getFilters() {
		return null;
	}

	public SortCriterion getCustomSort() {
		return null;
	}

	public IScoreFormatter getScoreFormatter() {
		return null;
	}

	public boolean isEmpty() {
		return true;
	}

}
