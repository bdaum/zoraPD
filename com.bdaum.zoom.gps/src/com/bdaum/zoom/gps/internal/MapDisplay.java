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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.gps.internal;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.dialogs.MapDialog;
import com.bdaum.zoom.gps.internal.views.MapView;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.ILocationDisplay;

public class MapDisplay implements ILocationDisplay {

	public void display(Location loc) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				try {
					((MapView) activePage.showView(MapView.ID)).showLocation(loc);
				} catch (PartInitException e) {
					// do nothing
				}
			}
		}
	}

	public void display(AssetSelection selectedAssets) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				try {
					((MapView) activePage.showView(MapView.ID)).showLocations(selectedAssets);
				} catch (PartInitException e) {
					// do nothing
				}
			}
		}
	}


	public Location defineLocation(Location loc) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			MapDialog dialog = new MapDialog(activeWorkbenchWindow.getShell(),
					loc);
			if (dialog.open() == Window.OK) {
				final Location result = dialog.getResult();
				if (result != null && result.getLatitude() != null
						&& !Double.isNaN(result.getLatitude())
						&& result.getLongitude() != null
						&& !Double.isNaN(result.getLongitude())) {
					BusyIndicator.showWhile(activeWorkbenchWindow.getShell()
							.getDisplay(), new Runnable() {

						public void run() {
							try {
								Place place = GpsUtilities.fetchPlaceInfo(
										result.getLatitude(),
										result.getLongitude());
								if (place != null) {
									if (!Double.isNaN(place.getLat())
											&& !Double.isNaN(place.getLon())) {
										double elevation = GpsUtilities
												.fetchElevation(place.getLat(),
														place.getLon());
										if (!Double.isNaN(elevation))
											place.setElevation(elevation);
									}
									GpsUtilities.transferPlacedata(place,
											result);
								}
							} catch (SocketTimeoutException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.Naming_service_connection_timed_out"), //$NON-NLS-1$
												e);
							} catch (HttpException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.http_exception"), //$NON-NLS-1$
												e);
							} catch (IOException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.Error_when_parsing"), //$NON-NLS-1$
												e);
							} catch (SAXException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.XML_problem_when_parsing"), //$NON-NLS-1$
												e);
							} catch (WebServiceException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.Webservice_problem_when_naming_places"), //$NON-NLS-1$
												e);
							} catch (ParserConfigurationException e) {
								GpsActivator
										.getDefault()
										.logError(
												Messages.getString("MapDisplay.internal_error_configuring_sax"), //$NON-NLS-1$
												e);
							}
						}
					});
					return result;
				}
			}
		}
		return null;
	}

}
