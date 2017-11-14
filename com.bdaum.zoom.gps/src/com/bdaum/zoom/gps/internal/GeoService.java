package com.bdaum.zoom.gps.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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

@SuppressWarnings("restriction")
public class GeoService implements IGeoService {

	public void showLocation(Asset asset, boolean external) {
		if (external) {
			double lat = asset.getGPSLatitude();
			double lon = asset.getGPSLongitude();
			if (Double.isNaN(lat) || Double.isNaN(lon)) {
				IDbManager dbManager = Core.getCore().getDbManager();
				List<LocationShownImpl> set = dbManager
						.obtainStructForAsset(LocationShownImpl.class, asset.getStringId(), false);
				for (LocationShownImpl rel : set) {
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
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null)
				try {
					activePage.showView(MapView.ID);
				} catch (PartInitException e1) {
					// do nothing
				}
		}
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
