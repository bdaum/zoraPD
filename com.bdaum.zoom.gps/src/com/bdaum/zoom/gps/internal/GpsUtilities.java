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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.gps.geonames.IGeonamingService;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.gps.Waypoint;
import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.gps.WaypointExtension;

@SuppressWarnings("restriction")
public class GpsUtilities {

	public static void transferPlacedata(Waypoint wp, Location newLocation) {
		newLocation.setCity(wp.getName());
		newLocation.setLatitude(wp.getLat());
		newLocation.setLongitude(wp.getLon());
		newLocation.setAltitude(wp.getElevation());
		if (wp instanceof WaypointExtension) {
			WaypointExtension wpe = (WaypointExtension) wp;
			if (wpe instanceof Place) {
				Place p = (Place) wpe;
				String countryName = p.getCountryName();
				String countryCode = p.getCountryCode();
				newLocation.setCountryName(countryName);
				newLocation.setCountryISOCode(countryCode);
				String continent = p.getContinent();
				newLocation.setWorldRegion(continent);
			}
			newLocation.setProvinceOrState(wpe.getState());
			if (wpe.getStreet() != null) {
				String street = wpe.getStreet();
				if (wpe.getStreetnumber() != null)
					street = wpe.getStreetnumber() + ", " + street; //$NON-NLS-1$
				newLocation.setSublocation(street);
			}
			Utilities.completeLocation(Core.getCore().getDbManager(), newLocation);
		}
	}

	public static boolean isEmpty(LocationImpl location) {
		return isEmpty(location.getCity()) && isEmpty(location.getCountryName())
				&& isEmpty(location.getCountryISOCode()) && isEmpty(location.getProvinceOrState())
				&& isEmpty(location.getWorldRegion()) && isEmpty(location.getSublocation());
	}

	private static boolean isEmpty(String text) {
		return text == null || text.isEmpty();
	}

	public static Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException,
			WebServiceException, SAXException, ParserConfigurationException, UnknownHostException, HttpException {
		IGeonamingService service = GpsActivator.getDefault().getNamingService();
		return (service != null) ? service.fetchPlaceInfo(lat, lon) : null;
	}

	public static WaypointArea[] findLocation(String address) throws SocketTimeoutException, IOException,
			WebServiceException, SAXException, ParserConfigurationException, HttpException {
		IGeonamingService service = GpsActivator.getDefault().getNamingService();
		return (service != null) ? service.findLocation(address) : null;
	}

	public static double fetchElevation(double lat, double lon) throws IOException, HttpException {
		BufferedReader reader = null;
		try {
			NumberFormat usformat = NumberFormat.getInstance(Locale.US);
			usformat.setMaximumFractionDigits(5);
			reader = new BufferedReader(new InputStreamReader(
					NetActivator.getDefault().openStream(NLS.bind("http://ws.geonames.org/srtm3?lat={0}&lng={1}", //$NON-NLS-1$
							usformat.format(lat), usformat.format(lon)))));
			String readLine = reader.readLine();
			if (readLine == null)
				return Double.NaN;
			try {
				double v = Double.parseDouble(readLine);
				return (v < -32000) ? Double.NaN : v;
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

}
