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

package com.bdaum.zoom.gps.gmap3.internal.views;

import java.net.URL;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.gps.gmap3.internal.Gmap3Activator;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent {

	private URL markerclusterUrl;
	private URL zoomMapUrl;
	private URL imagesUrl;

	@Override
	protected void findResources() {
		markerclusterUrl = findUrl("/gmap/markerclusterer.js"); //$NON-NLS-1$
		zoomMapUrl = findUrl("/gmap/zoomMap.js"); //$NON-NLS-1$
		imagesUrl = findUrl("/gmap/images/m1.png"); //$NON-NLS-1$
	}

	@Override
	protected String createSetPosDetailScript(HistoryItem item) {
		return NLS.bind("map.setZoom({0});\nmap.panTo({1});", (int) item.getDetail(), //$NON-NLS-1$
				createLatLng(item.getLatitude(), item.getLongitude()));
	}

	@Override
	protected String createLatLngBounds(double swLat, double swLon, double neLat, double neLon) {
		return NLS.bind("new google.maps.LatLngBounds({0},{1})", createLatLng(swLat, swLon), createLatLng(neLat, neLon)); //$NON-NLS-1$
	}

	private URL findUrl(String path) {
		return findUrl(Gmap3Activator.getDefault().getBundle(), path);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.AbstractMapComponent#createLatLng(double,
	 * double)
	 */

	@Override
	protected String createLatLng(double lat, double lon) {
		return NLS.bind("new google.maps.LatLng({0},{1})", usformat.format(lat), usformat.format(lon)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.AbstractMapComponent#createScriptEntries()
	 */

	@Override
	protected String createScriptEntries() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<script src=\"http://maps.google.com/maps/api/js?sensor=false\" type=\"text/javascript\"></script>\n"); //$NON-NLS-1$
		sb.append(createScriptEntry(markerclusterUrl)).append('\n');
		// sb.append(createScriptEntry(iconmakerUrl)).append('\n');
		sb.append(createScriptEntry(zoomMapUrl));
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.IMapComponent#dispose()
	 */

	@Override
	public void dispose() {
		getControl().dispose();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.AbstractMapComponent#createAdditionalVariables
	 * ()
	 */

	@Override
	protected String createAdditionalVariables() {
		StringBuilder sb = new StringBuilder();
		String s = imagesUrl.toString();
		sb.append("var imagesUrl = \"").append(s.substring(0, s.length() - 5)) //$NON-NLS-1$
				.append("\";\n"); //$NON-NLS-1$
		return sb.toString();
	}

	@Override
	protected String getAppKey() {
		return null;
	}

	@Override
	protected String getMappingSystemName() {
		return "Google Maps 3"; //$NON-NLS-1$
	}

}
