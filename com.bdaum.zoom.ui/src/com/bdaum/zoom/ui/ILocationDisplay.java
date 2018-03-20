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

package com.bdaum.zoom.ui;

import com.bdaum.zoom.cat.model.location.Location;

/**
 * ILocationDisplay instances can display and define geographic locations
 *
 */
public interface ILocationDisplay {

	/**
	 * Displays a location
	 * @param loc - the location to be displayed
	 */
	void display(Location loc);

	/**
	 * Defines a location
	 * @param loc  - preset location values
	 * @return - completed location
	 */
	Location defineLocation(Location loc);

	/**
	 * Displays the location of selected assets
	 * @param selectedAssets
	 */
	void display(AssetSelection selectedAssets);
}
