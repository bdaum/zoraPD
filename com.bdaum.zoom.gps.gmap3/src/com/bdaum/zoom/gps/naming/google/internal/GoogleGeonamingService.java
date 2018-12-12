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
package com.bdaum.zoom.gps.naming.google.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.xml.sax.SAXException;

import com.bdaum.zoom.batch.internal.DoneParsingException;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.AbstractGeocodingService;
import com.bdaum.zoom.gps.internal.preferences.GpsPreferencePage;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class GoogleGeonamingService extends AbstractGeocodingService {

	private static final String COM_BDAUM_ZOOM_GPS = "com.bdaum.zoom.gps"; //$NON-NLS-1$
	private static int answers = 0;

	public Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException, UnknownHostException {
		return fetchPlaceInfoFromGoogle(
				new String[] { usformat.format(lat), usformat.format(lon), Locale.getDefault().getLanguage() }, lat,
				lon);
	}

	private static Place fetchPlaceInfoFromGoogle(String[] parms, double lat, double lon)
			throws IOException, SAXException, ParserConfigurationException, UnknownHostException, WebServiceException {
		Place place = new Place();
		place.setLat(lat);
		place.setLon(lon);
		try (InputStream in = openGoogleService(
				NLS.bind("https://maps.google.com/maps/api/geocode/xml?latlng={0},{1}&sensor=false&language={2}", //$NON-NLS-1$
						parms))) {
			new GooglePlaceParser(in).parse(place);
		} catch (DoneParsingException e) {
			// everything okay
		}
		return (place.getName() != null) ? place : null;
	}

	private static InputStream openGoogleService(String query) throws IOException, UnknownHostException {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String clientId = preferencesService.getString(COM_BDAUM_ZOOM_GPS, PreferenceConstants.GOOGLECLIENTID, "", //$NON-NLS-1$
				null);
		if (clientId == null || clientId.trim().isEmpty()) {
			if (answers < 3) {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					PreferencesUtil.createPreferenceDialogOn(activeWorkbenchWindow.getShell(), GpsPreferencePage.ID,
							null, GpsPreferencePage.ACCOUNTS).open();
					++answers;
					clientId = preferencesService.getString(COM_BDAUM_ZOOM_GPS, PreferenceConstants.GOOGLECLIENTID, "", //$NON-NLS-1$
							null);
				}
			}
			if (clientId == null || clientId.trim().isEmpty())
				throw new WebServiceException(Messages.GoogleGeonamingService_api_key_required);
		}
		query += (NLS.bind("&key={0}", clientId.trim()));//$NON-NLS-1$
		return NetActivator.getDefault().openStream(query);
	}

	public WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException, ParserConfigurationException {
		List<WaypointArea> pnts = new ArrayList<WaypointArea>();
		Locale locale = Locale.getDefault();
		try (InputStream in = openGoogleService(NLS.bind(
				"https://maps.googleapis.com/maps/api/geocode/xml?address={0}&region={1}&language={2}", //$NON-NLS-1$
				new Object[] { URLEncoder.encode(address, "UTF-8"), locale.getCountry(), locale.getLanguage() }))) { //$NON-NLS-1$
			new GoogleGeoCodeParser(in).parse(pnts);
		} catch (DoneParsingException e) {
			// everything okay
		}
		return pnts.toArray(new WaypointArea[pnts.size()]);
	}

	@Override
	public Control createParameterGroup(CGroup parmGroup) {
		Composite composite = new Composite(parmGroup, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.GoogleGeonamingService_no_parameters);
		return composite;
	}

	@Override
	public double getElevation(double lat, double lon) throws UnknownHostException, HttpException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveSearchParameters() {
		// nothing to do
	}

}
