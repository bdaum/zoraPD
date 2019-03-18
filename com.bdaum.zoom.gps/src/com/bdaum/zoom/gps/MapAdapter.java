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

public abstract class MapAdapter implements MapListener {
	
	@Override
	public void setCoordinates(String[] assetIds, double latitude, double longitude, int zoomLevel, int type,
			String uuid) {
		// do nothing by default
	}

	@Override
	public void historyChanged(double lat, double lon, int zoom) {
		// do nothing by default
	}

	@Override
	public void setMaptype(String maptype) {
		// do nothing by default
	}

}
