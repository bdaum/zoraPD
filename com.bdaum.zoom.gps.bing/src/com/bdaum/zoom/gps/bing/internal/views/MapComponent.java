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
		return NLS.bind("new Microsoft.Maps.Location({0},{1})", usformat.format(lat), usformat.format(lon)); //$NON-NLS-1$
	}

	@Override
	protected String createLatLngBounds(double swLat, double swLon, double neLat, double neLon) {
		double height = Math.abs(neLat - swLat);
		double width = Math.abs(neLon - swLon);
		double cLat = (swLat + neLat) / 2;
		double cLon = (swLon + neLon) / 2;
		return NLS.bind("new Microsoft.Maps.LocationRect({0},{1},{2})", //$NON-NLS-1$
				new Object[] { createLatLng(cLat, cLon), width, height });
	}

	@Override
	protected void findResources() {
		zoomMapUrl = findUrl("/gmap/zoomMap.js"); //$NON-NLS-1$
	}

	@Override
	protected String createSetPosDetailScript(com.bdaum.zoom.gps.widgets.AbstractMapComponent.HistoryItem item) {
		int zoom = (int) item.getDetail();
		return "map.setView({zoom: " + zoom + ", center: " + createLatLng(item.getLatitude(), item.getLongitude()) //$NON-NLS-1$ //$NON-NLS-2$
				+ "});\n"; //$NON-NLS-1$
	}

	@Override
	protected String createAdditionalVariables() {
		return null;
	}

	@Override
	protected String createScriptEntries() {
		return new StringBuilder()
				.append("<script type='text/javascript' src='http://www.bing.com/api/maps/mapcontrol'></script>\n") //$NON-NLS-1$
				.append(createScriptEntry(zoomMapUrl)).toString();
	}

	@Override
	protected String getMappingSystemName() {
		return "Microsoft Bing"; //$NON-NLS-1$
	}

	@Override
	protected String getAppKey() {
		return null;
	}

}
