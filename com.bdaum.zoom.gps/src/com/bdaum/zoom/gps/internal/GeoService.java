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
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.IGeoService;
import com.bdaum.zoom.gps.internal.views.MapView;
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

	public static void showInWebbrowser(double lat, double lon, int zoom)
			throws PartInitException, MalformedURLException {
		IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
		IConfigurationElement mappingSystem = GpsActivator.findCurrentMappingSystem();
		if (mappingSystem != null) {
			String query = mappingSystem.getAttribute("query"); //$NON-NLS-1$
			if (query != null && !query.isEmpty()) {
				NumberFormat nf = NumberFormat.getInstance(Locale.US);
				nf.setMaximumFractionDigits(5);
				browser.openURL(new URL(NLS.bind(query, new Object[] { nf.format(lat), nf.format(lon), zoom })));
			}
		}
	}

}
