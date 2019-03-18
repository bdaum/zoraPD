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

package com.bdaum.zoom.gps.google.internal.views;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.gps.google.internal.GoogleActivator;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.preferences.GpsPreferencePage;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent {

	private static final String GMAP = "gmap/"; //$NON-NLS-1$
	private static String markerclusterUrl;
	private static String zoomMapUrl;
	private static String imagesUrl;
	private static String additionalVariables;
	private int answers = 0;

	static {
		URL url = findUrl(GoogleActivator.getDefault().getBundle(), GMAP);
		String folderUrl = url == null ? GMAP : url.toString();
		markerclusterUrl = folderUrl + "markerclusterer.js"; //$NON-NLS-1$
		zoomMapUrl = folderUrl + "zoomMap.js"; //$NON-NLS-1$
		imagesUrl = folderUrl + "images/m1.png"; //$NON-NLS-1$
		additionalVariables = new StringBuilder().append("var imagesUrl = \"") //$NON-NLS-1$
				.append(imagesUrl.substring(0, imagesUrl.length() - 5)).append("\";\n").toString(); //$NON-NLS-1$
	}

	@Override
	protected String createSetPosDetailScript(HistoryItem item) {
		return NLS.bind("map.setZoom({0});\nmap.panTo({1});", (int) item.getDetail(), //$NON-NLS-1$
				createLatLng(item.getLatitude(), item.getLongitude()));
	}

	@Override
	protected String createLatLngBounds(double swLat, double swLon, double neLat, double neLon) {
		return NLS.bind("new google.maps.LatLngBounds({0},{1})", createLatLng(swLat, swLon), //$NON-NLS-1$
				createLatLng(neLat, neLon));
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
	 * @see com.bdaum.zoom.gps.widgets.AbstractMapComponent#createScriptEntries()
	 */

	@Override
	protected String createScriptEntries() {
		StringBuilder sb = new StringBuilder();
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String clientId = preferencesService.getString(GpsActivator.PLUGIN_ID, PreferenceConstants.GOOGLECLIENTID, "", //$NON-NLS-1$
				null);
		if (clientId == null || clientId.trim().isEmpty()) {
			if (answers < 3) {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					PreferencesUtil.createPreferenceDialogOn(activeWorkbenchWindow.getShell(), GpsPreferencePage.ID,
							null, GpsPreferencePage.ACCOUNTS).open();
					++answers;
					clientId = preferencesService.getString(GpsActivator.PLUGIN_ID, PreferenceConstants.GOOGLECLIENTID,
							"", //$NON-NLS-1$
							null);
				}
			}
		}
		if (clientId != null && !clientId.trim().isEmpty())
			sb.append("<script src=\"https://maps.googleapis.com/maps/api/js?key=").append(clientId.trim()) //$NON-NLS-1$
					.append("\" type=\"text/javascript\"></script>\n"); //$NON-NLS-1$
		else
			sb.append("<script src=\"https://maps.googleapis.com/maps/api/js\" type=\"text/javascript\"></script>\n"); //$NON-NLS-1$
		sb.append(createScriptEntry(markerclusterUrl)).append('\n');
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
	 * com.bdaum.zoom.gps.widgets.AbstractMapComponent#createAdditionalVariables ()
	 */

	@Override
	protected String createAdditionalVariables() {
		return additionalVariables;
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
