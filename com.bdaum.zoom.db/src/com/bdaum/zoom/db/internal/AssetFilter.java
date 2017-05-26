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
package com.bdaum.zoom.db.internal;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.IAssetFilter;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

/**
 * @author bdaum
 *
 *         Implementations must provide equals() and hash() implementations
 */
public class AssetFilter implements IAssetFilter{

	/* (non-Javadoc)
	 * @see com.bdaum.zoom.core.IAssetFilter#accept(com.bdaum.zoom.cat.model.asset.Asset)
	 */

	public boolean accept(Asset asset) {
		return true;
	}

	/**
	 * This method is called before the database is queried. It delivers a
	 * constraint that is ANDed with the constraints induced by the collection
	 * criteria. The filter should be identical to the one given in method accept().
	 *
	 * @param dbManager
	 *            - current database manager
	 * @param query
	 *            - current database query
	 * @return constraint that expresses the additional selection criteria
	 */
	public Constraint getConstraint(DbManager dbManager, Query query) {
		return null;
	}
}
