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
package com.bdaum.zoom.db.internal;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IRatingFilter;

public class RatingFilter extends AssetFilter implements IRatingFilter {

	int rating;

	public RatingFilter(int rating) {
		this.rating = rating;
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#accept(com.bdaum.zoom.cat.model.asset.
	 * Asset)
	 */
	@Override
	public boolean accept(Asset asset) {
		if (rating == QueryField.SELECTALL)
			return true;
		if (rating == QueryField.SELECTUNDEF)
			return asset.getRating() < 0;
		return asset.getRating() >= rating;
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#getConstraint(com.db4o.query.Query)
	 */
//	@Override
//	public Constraint getConstraint(DbManager dbManager, Query query) {
//		if (rating == QueryField.SELECTALL)
//			return null;
//		if (rating == QueryField.SELECTUNDEF)
//			return query.descend(QueryField.RATING.getKey()).constrain(0).smaller();
//		return query.descend(QueryField.RATING.getKey()).constrain(rating).smaller().not();
//	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IRatingFilter#getRating()
	 */
	public int getRating() {
		return rating;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof IRatingFilter ? rating == ((IRatingFilter) obj).getRating() : false;
	}

	@Override
	public int hashCode() {
		return rating;
	}
}
