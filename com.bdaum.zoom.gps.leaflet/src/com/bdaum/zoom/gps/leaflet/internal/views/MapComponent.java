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
 * (c) 2017 Berthold Daum  
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

	private URL leafletUrl;
	private URL zoomMapUrl;
	private URL leafletCss;
	private URL providersUrl;
	private URL minimapUrl;
	private URL minimapCss;
	private String mapProvider;
	private URL markerClusterGroupUrl;
	private URL markerClusterGroupCss;
	private URL markerClusterGroupDefaultCss;

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		if (data instanceof String)
			mapProvider = (String) data;
	}

	@Override
	protected void findResources() {
		leafletUrl = findUrl("/gmap/leaflet-src.js"); //$NON-NLS-1$
		providersUrl = findUrl("/gmap/leaflet-providers.js"); //$NON-NLS-1$
		leafletCss = findUrl("/gmap/leaflet.css"); //$NON-NLS-1$
		zoomMapUrl = findUrl("/gmap/zoomMap.js"); //$NON-NLS-1$
		minimapUrl = findUrl("/gmap/Control.MiniMap.js"); //$NON-NLS-1$
		minimapCss = findUrl("/gmap/Control.MiniMap.css"); //$NON-NLS-1$
		markerClusterGroupUrl = findUrl("/gmap/leaflet.markercluster.js"); //$NON-NLS-1$
		markerClusterGroupCss = findUrl("/gmap/MarkerCluster.css"); //$NON-NLS-1$
		markerClusterGroupDefaultCss = findUrl("/gmap/MarkerCluster.Default.css"); //$NON-NLS-1$
	}

	@Override
	protected String createSetPosDetailScript(HistoryItem item) {
		return NLS.bind("map.setZoom({0});\nmap.panTo({1});", (int) item.getDetail(), //$NON-NLS-1$
				createLatLng(item.getLatitude(), item.getLongitude()));
	}

	private URL findUrl(String path) {
		return super.findUrl(LeafletActivator.getDefault().getBundle(), path);
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
		return new StringBuilder().append("var selectedProvider = \"").append(mapProvider) //$NON-NLS-1$
				.append("\";\n") //$NON-NLS-1$
				.toString();
	}

	@Override
	protected String createScriptEntries() {
		return new StringBuilder().append(createStyleEntry(leafletCss)).append('\n')
				.append(createStyleEntry(minimapCss)).append('\n').append(createScriptEntry(leafletUrl)).append('\n')
				.append(createScriptEntry(providersUrl))
				.append('\n').append(createScriptEntry(minimapUrl)).append('\n')
				.append(createScriptEntry(markerClusterGroupUrl)).append('\n')
				.append(createStyleEntry(markerClusterGroupCss)).append('\n')
				.append(createStyleEntry(markerClusterGroupDefaultCss)).append('\n')
				.append(createScriptEntry(zoomMapUrl)).toString();
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
