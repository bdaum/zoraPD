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
	private URL makimarkersUrl;
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
		makimarkersUrl = findUrl("/gmap/Leaflet.MakiMarkers.js"); //$NON-NLS-1$
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
		StringBuilder sb = new StringBuilder();
		sb.append("var selectedProvider = \"").append(mapProvider) //$NON-NLS-1$
				.append("\";\n"); //$NON-NLS-1$
		return sb.toString();
	}

	@Override
	protected String createScriptEntries() {
		StringBuilder sb = new StringBuilder();
		sb.append(createStyleEntry(leafletCss)).append('\n');
		sb.append(createStyleEntry(minimapCss)).append('\n');
		sb.append(createScriptEntry(leafletUrl)).append('\n');
		sb.append(createScriptEntry(makimarkersUrl)).append('\n');
		sb.append(createScriptEntry(providersUrl)).append('\n');
		sb.append(createScriptEntry(minimapUrl)).append('\n');
		sb.append(createScriptEntry(markerClusterGroupUrl)).append('\n');
		sb.append(createStyleEntry(markerClusterGroupCss)).append('\n');
		sb.append(createStyleEntry(markerClusterGroupDefaultCss)).append('\n');
		sb.append(createScriptEntry(zoomMapUrl));
		return sb.toString();
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
