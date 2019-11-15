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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.gps.google.internal.GoogleActivator;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.preferences.GpsPreferencePage;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent {

	private static final String MARKERCLUSTERER_JS = "markerclusterer.js"; //$NON-NLS-1$
	private static final String ZOOM_MAP_JS = "zoomMap.js"; //$NON-NLS-1$
	private static String markerclusterUrl;
	private static String zoomMapUrl;
	private static String imagesUrl;
	private static String additionalVariables;
	private int answers = 0;

	static {
		URL url = findUrl(GoogleActivator.getDefault().getBundle(), GMAP);
		String folderUrl = url == null ? GMAP : url.toString();
		markerclusterUrl = folderUrl + MARKERCLUSTERER_JS;
		zoomMapUrl = folderUrl + ZOOM_MAP_JS; // $NON-NLS-1$
		imagesUrl = folderUrl + "images/m1.png"; //$NON-NLS-1$
		additionalVariables = new StringBuilder().append("var imagesUrl = \"") //$NON-NLS-1$
				.append(imagesUrl.substring(0, imagesUrl.length() - 5)).append("\";\n").toString(); //$NON-NLS-1$
	}

	@Override
	public List<String> getScriptUrls() {
		String googleApi = "https://maps.googleapis.com/maps/api/js"; //$NON-NLS-1$
		String clientId = Platform.getPreferencesService().getString(GpsActivator.PLUGIN_ID,
				PreferenceConstants.GOOGLECLIENTID, "", //$NON-NLS-1$
				null);
		if (clientId != null && !clientId.trim().isEmpty())
			googleApi += "?key=" + clientId.trim(); //$NON-NLS-1$
		String folder = PLUGINS + GoogleActivator.PLUGIN_ID + '/' + GMAP;
		return Arrays.asList(googleApi, folder + MARKERCLUSTERER_JS, folder + ZOOM_MAP_JS);
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
	public String createAdditionalVariables() {
		return additionalVariables;
	}

	@Override
	protected String getMappingSystemName() {
		return "Google Maps 3"; //$NON-NLS-1$
	}

}
