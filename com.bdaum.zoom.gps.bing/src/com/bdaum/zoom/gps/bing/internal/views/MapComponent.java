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

package com.bdaum.zoom.gps.bing.internal.views;

import java.net.URL;
import java.util.Locale;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.gps.bing.internal.BingActivator;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent {

	private URL zoomMapUrl;
	
	private URL findUrl(String path) {
		return super.findUrl(BingActivator.getDefault().getBundle(), path);
	}


	@Override
	protected String getCountryCode(String input) {
		if (input != null && input.length() != 3) {
			Locale[] availableLocales = Locale.getAvailableLocales();
			for (Locale locale : availableLocales)
				if (input.equals(locale.getCountry()))
					return locale.getISO3Country();
		}
		return input;
	}


	@Override
	protected String createLatLng(double lat, double lon) {
		return NLS.bind("new VELatLong({0},{1})", usformat.format(lat),usformat.format(lon)); //$NON-NLS-1$
	}

	@Override
	protected String createLatLngBounds(double swLat, double swLon, double neLat, double neLon) {
		double height = neLat - swLat;
		double width = swLon >= neLon ? swLon-neLon : 360+neLon-swLon;
		double cLat = (swLat+neLat)/2;
		double cLon = swLon >= neLon ? (swLon+neLon)/2 : (swLon+neLon-360)/2;
		if (cLon < 0)
			cLon += 360;
		return NLS.bind("new LocationRect({0},{1},{2})", new Object[] { createLatLng(cLat, cLon) ,width, height}); //$NON-NLS-1$
	}



	@Override
	protected void findResources() {
		zoomMapUrl = findUrl("/gmap/zoomMap.js"); //$NON-NLS-1$
	}


	@Override
	protected String createSetPosDetailScript(
			com.bdaum.zoom.gps.widgets.AbstractMapComponent.HistoryItem item) {
		int zoom = (int) item.getDetail();
		return "map.SetZoomLevel(" + zoom + ");\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "map.PanToLatLong(new VELatLong(" + item.getLatitude() + "," //$NON-NLS-1$ //$NON-NLS-2$
				+ item.getLongitude() + "));"; //$NON-NLS-1$
	}


	@Override
	protected String createAdditionalVariables() {
		StringBuilder sb = new StringBuilder();
		sb.append("var keyInvalid='").append(Messages.getString("MapComponent.map_key_invalid")).append("';\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return sb.toString();
	}


	@Override
	protected String createScriptEntries() {
		return "<script type=\"text/javascript\" src=\"http://ecn.dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.3\"></script>\n" //$NON-NLS-1$
			+ createScriptEntry(zoomMapUrl);
	}


	@Override
	protected String getAppKey() {
		return BingActivator.getDefault().getMapKey();
	}


	@Override
	protected String getMappingSystemName() {
		return "Microsoft Bing"; //$NON-NLS-1$
	}



}
