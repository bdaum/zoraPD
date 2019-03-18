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
 * (c) 2017-2019 Berthold Daum  
 */
package com.bdaum.zoom.gps.leaflet.internal.views;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.gps.leaflet.internal.LeafletActivator;
import com.bdaum.zoom.gps.widgets.AbstractMapComponent;

public class MapComponent extends AbstractMapComponent implements IExecutableExtension {

	private static final String GMAP = "gmap/"; //$NON-NLS-1$
	private static String leafletUrl;
	private static String zoomMapUrl;
	private static String leafletCss;
	private static String providersUrl;
	private static String minimapUrl;
	private static String minimapCss;
	private static String markerClusterGroupUrl;
	private static String markerClusterGroupCss;
	private static String markerClusterGroupDefaultCss;
	private static String scriptEntries;
	private String additionalVariables;

	static {
		URL url = findUrl(LeafletActivator.getDefault().getBundle(), GMAP);
		String folderUrl = url == null ? GMAP : url.toString();
		providersUrl = folderUrl + "leaflet-providers.js"; //$NON-NLS-1$
		leafletCss = folderUrl + "leaflet.css"; //$NON-NLS-1$
		leafletUrl = folderUrl + "leaflet.js"; //$NON-NLS-1$
		zoomMapUrl = folderUrl + "zoomMap.js"; //$NON-NLS-1$
		minimapUrl = folderUrl + "Control.MiniMap.min.js"; //$NON-NLS-1$
		minimapCss = folderUrl + "Control.MiniMap.min.css"; //$NON-NLS-1$
		markerClusterGroupUrl = folderUrl + "leaflet.markercluster.js"; //$NON-NLS-1$
		markerClusterGroupCss = folderUrl + "MarkerCluster.css"; //$NON-NLS-1$
		markerClusterGroupDefaultCss = folderUrl + "MarkerCluster.Default.css"; //$NON-NLS-1$
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		additionalVariables = new StringBuilder().append("var selectedProvider = \"").append(data) //$NON-NLS-1$
				.append("\";\n").toString(); //$NON-NLS-1$
	}

	@Override
	protected String createSetPosDetailScript(HistoryItem item) {
		return NLS.bind("map.setZoom({0});\nmap.panTo({1});", (int) item.getDetail(), //$NON-NLS-1$
				createLatLng(item.getLatitude(), item.getLongitude()));
	}

	@Override
	protected String getMappingSystemName() {
		return "Leaflet"; //$NON-NLS-1$
	}

	@Override
	protected String getAppKey() {
		return null;
	}

	@Override
	protected String createAdditionalVariables() {
		return additionalVariables;
	}

	@Override
	protected String createScriptEntries() {
		if (scriptEntries == null)
			scriptEntries = new StringBuilder().append(createStyleEntry(leafletCss)).append('\n')
					.append(createStyleEntry(minimapCss)).append('\n').append(createScriptEntry(leafletUrl))
					.append('\n').append(createScriptEntry(providersUrl)).append('\n')
					.append(createScriptEntry(minimapUrl)).append('\n').append(createScriptEntry(markerClusterGroupUrl))
					.append('\n').append(createStyleEntry(markerClusterGroupCss)).append('\n')
					.append(createStyleEntry(markerClusterGroupDefaultCss)).append('\n')
					.append(createScriptEntry(zoomMapUrl)).toString();
		return scriptEntries;
	}

	@Override
	protected String createLatLng(double lat, double lon) {
		return NLS.bind("L.latLng({0},{1})", usformat.format(lat), usformat.format(lon)); //$NON-NLS-1$
	}

	@Override
	protected String createLatLngBounds(double swLat, double swLon, double neLat, double neLon) {
		return NLS.bind("L.latLngBounds({0},{1})", createLatLng(swLat, swLon), createLatLng(neLat, neLon)); //$NON-NLS-1$ ;
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

}
