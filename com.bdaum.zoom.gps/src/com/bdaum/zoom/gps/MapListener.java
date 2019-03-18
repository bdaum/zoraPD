/*******************************************************************************
 * Copyright (c) 2019 Berthold Daum.
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
public interface MapListener {
	
	// Event types
	public static final int ADDLOC = 0;
	public static final int CAMPOS = 1;
	public static final int IMGDIR = 2;
	public static final int SHOWNLOC = 3;
	public static final int AREA = 4;

	/**
	 * New coordinates
	 * @param assetIds - Asset ID (modify event) or null
	 * @param latitude - Latitude
	 * @param longitude - Longitude
	 * @param zoomLevel - Zoom level
	 * @param type - event type
	 * @param uuid - proposed UUID for new location shown entries
	 */
	void setCoordinates(String[] assetIds, double latitude, double longitude, int zoomLevel, int type, String uuid);

	/**
	 * Inform that a location location history has changed
	 * @param lat - latitude
	 * @param lon - longitude
	 * @param zoom - detail level
	 */
	void historyChanged(double lat, double lon, int zoom);

	/**
	 * New maptype
	 * @param maptype - new maptype
	 */
	void setMaptype(String maptype);

}
