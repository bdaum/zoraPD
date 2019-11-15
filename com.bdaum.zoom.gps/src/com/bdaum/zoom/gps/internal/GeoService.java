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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.gps.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.IGeoService;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.views.MapView;
import com.bdaum.zoom.gps.internal.views.Mapdata;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class GeoService implements IGeoService {

	public void showLocation(Asset asset, boolean external) {
		if (external) {
			double lat = asset.getGPSLatitude();
			double lon = asset.getGPSLongitude();
			if (Double.isNaN(lat) || Double.isNaN(lon)) {
				IDbManager dbManager = Core.getCore().getDbManager();
				for (LocationShownImpl rel : dbManager.obtainStructForAsset(LocationShownImpl.class,
						asset.getStringId(), false)) {
					LocationImpl loc = dbManager.obtainById(LocationImpl.class, rel.getLocation());
					if (loc != null && !(Double.isNaN(loc.getLatitude()) || Double.isNaN(loc.getLongitude()))) {
						lat = loc.getLatitude();
						lon = loc.getLongitude();
						break;
					}
				}
			}
			if (!(Double.isNaN(lat) || Double.isNaN(lon)))
				try {
					showInWebbrowser(lat, lon, 12);
					return;
				} catch (Exception e) {
					// do nothing
				}
		}
		UiUtilities.showView(MapView.ID);
	}

	public static String obtainGeoLink(double lat, double lon, int zoom) {
		IConfigurationElement mappingSystem = GpsActivator.findCurrentMappingSystem();
		if (mappingSystem != null) {
			String query = mappingSystem.getAttribute("query"); //$NON-NLS-1$
			if (query != null && !query.isEmpty()) {
				NumberFormat nf = NumberFormat.getInstance(Locale.US);
				nf.setMaximumFractionDigits(5);
				nf.setGroupingUsed(false);
				return NLS.bind(query, new Object[] { nf.format(lat), nf.format(lon), zoom });
			}
		}
		return null;
	}

	public static void showInWebbrowser(double lat, double lon, int zoom)
			throws PartInitException, MalformedURLException {
		String geoLink = obtainGeoLink(lat, lon, zoom);
		if (geoLink != null) {
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
			if (browser != null)
				browser.openURL(new URL(geoLink));
		}
	}

	@Override
	public Control showTrack(Composite parent, AssetImpl[] assets, boolean withMarkers) {
		IMapComponent mapComponent = GpsActivator.getMapComponent(GpsActivator.findCurrentMappingSystem());
		if (mapComponent != null) {
			mapComponent.createComponent(parent, false);
			List<Trackpoint> trackpoints = new ArrayList<Trackpoint>(assets.length);
			List<Place> markers = new ArrayList<Place>(assets.length);
			Place lastPlace = null;
			for (Asset asset : assets) {
				double lat = asset.getGPSLatitude();
				double lon = asset.getGPSLongitude();
				if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
					trackpoints.add(new Trackpoint(lat, lon, true));
					if (lastPlace != null
							&& Core.distance(lat, lon, lastPlace.getLat(), lastPlace.getLon(), 'K') < 0.02d)
						continue;
					lastPlace = new Place(lat, lon);
					lastPlace.addImageAssetId(asset.getStringId());
					lastPlace.setImageName(UiUtilities.createSlideTitle(asset));
					if (withMarkers)
						markers.add(lastPlace);
				}
			}
			mapComponent.setInput(new Mapdata(null,  markers.toArray(new Place[markers.size()]), trackpoints.toArray(new Trackpoint[trackpoints.size()])), 8,
					IMapComponent.AREA);
			return mapComponent.getControl();
		}
		return null;
	}

	private static String getAdditionalVariables(IMapComponent mapComponent) {
		String vars = mapComponent.createAdditionalVariables();
		StringBuilder sb = vars != null ? new StringBuilder(vars) : new StringBuilder();
		String[] assignments = mapComponent.getPicAssignments();
		for (String assignment : assignments)
			sb.append(assignment).append("\n"); //$NON-NLS-1$
		return sb.toString();
	}

	@Override
	public String getMapContext(String root) {
		IMapComponent mapComponent = GpsActivator.getMapComponent(GpsActivator.findCurrentMappingSystem());
		if (mapComponent == null)
			return null;
		mapComponent.setRoot(root);
		StringBuilder sb = new StringBuilder();
		List<String> scriptUrls = mapComponent.getScriptUrls();
		if (scriptUrls != null) {
			sb.append("<script type=\"text/javascript\">\n\t") //$NON-NLS-1$
					.append("var root='").append(root).append("';\n"); //$NON-NLS-1$//$NON-NLS-2$
			String vars = getAdditionalVariables(mapComponent);
			if (vars != null)
				sb.append(vars);
			sb.append("</script>\n"); //$NON-NLS-1$
			for (String url : scriptUrls)
				if (url.endsWith(".css")) //$NON-NLS-1$
					sb.append("<link rel=\"stylesheet\" href=\"").append(url).append("\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
				else
					sb.append("<script src=\"").append(url).append("\" type=\"text/javascript\"></script>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sb.toString();
	}

}
