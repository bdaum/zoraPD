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
package com.bdaum.zoom.core;

import com.bdaum.zoom.cat.model.asset.Asset;

/**
 * @author bdaum
 *
 *         Implementations must provide equals() and hash() implementations
 */
public interface IAssetFilter {

	/**
	 * This method may be called after assets have been returned from the
	 * database and a postprocessing step is necessary. It filters the returned
	 * assets according to the same criteria given in method getConstraint()
	 *
	 * @param asset
	 *            - asset to be checked
	 * @return true if asset is accepted
	 */
	boolean accept(Asset asset);

}
