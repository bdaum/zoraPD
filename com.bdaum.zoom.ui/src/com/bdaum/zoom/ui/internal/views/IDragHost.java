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
 * (c) 2014-2017 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.ui.AssetSelection;

public interface IDragHost extends IAdaptable {

	/**
	 * Test if the cursor is above an image
	 * 
	 * @param x
	 *            - cursor x
	 * @param y
	 *            - cursor y
	 * @return true if the cursor is above an image
	 */
	boolean cursorOverImage(int x, int y);

	/**
	 * Get current asset selection
	 * 
	 * @return asset selection
	 */
	AssetSelection getAssetSelection();

	/**
	 * Get the hosts control
	 * 
	 * @return - main host control
	 */
	Control getControl();

	/**
	 * Indicate that a drag is in progress
	 * 
	 * @param dragging
	 *            true if drag is in progress, false otherwise
	 */
	void setDragging(boolean dragging);

	/**
	 * Find the closest face region
	 * 
	 * @param x
	 *            - cursor x
	 * @param y
	 *            - cursor y
	 * @param all
	 *            - true if also unnamed regions not under the cursor shall be
	 *            returned
	 * @return - face region or null
	 */
	ImageRegion findBestFaceRegion(int x, int y, boolean all);

}
