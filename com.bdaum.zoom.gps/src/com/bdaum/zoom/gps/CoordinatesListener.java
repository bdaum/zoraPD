/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps;

/**
 * Listens to coordinate changes in a map marker
 */
public interface CoordinatesListener {

	/**
	 * New coordinates
	 * @param latitude - Latitude
	 * @param longitude - Longitude
	 * @param zoomLevel - Zoom level
	 */
	void setCoordinates(double latitude, double longitude, int zoomLevel);

}
