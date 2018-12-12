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
 * (c) 2018 Berthold Daum  
 */

package com.bdaum.zoom.ui;

public interface ITransformProvider {
	/**
	 * Adds a listener listening to pan and zoom translations
	 * @param listener
	 */
	void addTransformListener(ITransformListener listener);

	/**
	 * Removes the specified transform listener
	 * @param listener
	 */
	void removeTransformListener(ITransformListener listener);

	/**
	 * Sets the necessary transform to achieve pan and zoom operations
	 * @param translateX - translation in X direction
	 * @param translateY - translation in Y direction
	 * @param scale - scaling
	 */
	void setTransform(double translateX, double translateY, double scale);

	/**
	 * Sets the synchronization state
	 * @param sync - true if pan and zoom are synchronized, false otherwise
	 * @param vertical - true if the transform provider objects are vertically organized, false if horizontally organized
	 */
	void setSync(boolean sync, boolean vertical);
}
