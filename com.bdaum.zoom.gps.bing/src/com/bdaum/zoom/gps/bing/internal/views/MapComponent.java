/*******************************************************************************
 * Copyright (c) 2009-2019 Berthold Daum.
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.gps.bing.internal.BingActivator;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent {

	private static final String GMAP_ZOOM_MAP_JS = "gmap/zoomMap.js"; //$NON-NLS-1$
	private static String zoomMapUrl;
	private static String scriptEntries;

	static {
		URL url = findUrl(BingActivator.getDefault().getBundle(), GMAP_ZOOM_MAP_JS);
		zoomMapUrl = url == null ? GMAP_ZOOM_MAP_JS : url.toString();
	}

	@Override
	public List<String> getScriptUrls() {
		return Arrays.asList("https://www.bing.com/api/maps/mapcontrol", //$NON-NLS-1$
				PLUGINS + BingActivator.PLUGIN_ID + '/' + GMAP_ZOOM_MAP_JS);
	}

	@Override
	protected String getCountryCode(String input) {
		if (input != null && input.length() != 3)
			for (Locale locale : Locale.getAvailableLocales())
				if (input.equals(locale.getCountry()))
					return locale.getISO3Country();
		return input;
	}

	@Override
	protected String createSetPosDetailScript(com.bdaum.zoom.gps.widgets.AbstractMapComponent.HistoryItem item) {
		return NLS.bind("setZoomDetails({0}, {1})", (int) item.getDetail(), //$NON-NLS-1$
				latLon(item.getLatitude(), item.getLongitude()));
	}

	@Override
	public String createAdditionalVariables() {
		return null;
	}

	@Override
	protected String createScriptEntries() {
		if (scriptEntries == null)
			scriptEntries = new StringBuilder()
					.append("<script type='text/javascript' src='http://www.bing.com/api/maps/mapcontrol'></script>\n") //$NON-NLS-1$
					.append(createScriptEntry(zoomMapUrl)).toString();
		return scriptEntries;
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
