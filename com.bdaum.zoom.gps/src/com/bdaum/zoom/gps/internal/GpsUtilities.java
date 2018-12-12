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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.gps.Waypoint;
import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.gps.WaypointExtension;
import com.google.openlocationcode.OpenLocationCode;
import com.google.openlocationcode.OpenLocationCode.CodeArea;

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
				&& isEmpty(location.getWorldRegion()) && isEmpty(location.getSublocation())
				&& isEmpty(location.getPlusCode());
	}

	private static boolean isEmpty(String text) {
		return text == null || text.isEmpty();
	}

	public static Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException,
			WebServiceException, SAXException, ParserConfigurationException, UnknownHostException, HttpException {
		IGeocodingService service = GpsActivator.getDefault().getNamingService();
		return (service != null) ? service.fetchPlaceInfo(lat, lon) : null;
	}

	public static WaypointArea[] findLocation(String address) throws SocketTimeoutException, IOException,
			WebServiceException, SAXException, ParserConfigurationException, HttpException {
		address = address.trim();
		char sep = address.indexOf('°') >= 0 ? ' ' : ',';
		int p = address.indexOf(sep);
		if (p > 0 && p < address.length() - 1) {
			try {
				return new WaypointArea[] { new WaypointArea(address,
						(double) Format.longitudeFormatter.fromString(address.substring(p + 1)),
						(double) Format.latitudeFormatter.fromString(address.substring(0, p))) };
			} catch (ParseException e) {
				// fall through
			}
		}
		try {
			CodeArea codeArea = OpenLocationCode.decode(address);
			return new WaypointArea[] {
					new WaypointArea(address, codeArea.getCenterLongitude(), codeArea.getCenterLatitude()) };
		} catch (Exception e) {
			// fall through
		}
		IGeocodingService service = GpsActivator.getDefault().getNamingService();
		return (service != null) ? service.findLocation(address) : null;
	}

	public static double fetchElevation(double lat, double lon) throws IOException, HttpException {
		IGeocodingService service = GpsActivator.getDefault().getNamingServiceById("GeoNames"); //$NON-NLS-1$
		return service == null ? Double.NaN : service.getElevation(lat, lon);
	}

	public static void getGeoAreas(IPreferenceStore preferenceStore, Collection<GeoArea> areas) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(8);
		String nogo = preferenceStore.getString(PreferenceConstants.NOGO);
		if (nogo != null) {
			int i = 0;
			double lat = 0, lon = 0, km = 0;
			String name = null;
			StringTokenizer st = new StringTokenizer(nogo, ";,", true); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (";".equals(s)) { //$NON-NLS-1$
					areas.add(new GeoArea(name, lat, lon, km));
					i = 0;
				} else if (",".equals(s)) //$NON-NLS-1$
					++i;
				else
					try {
						switch (i) {
						case 0:
							name = s;
							break;
						case 1:
							lat = nf.parse(s).doubleValue();
							break;
						case 2:
							lon = nf.parse(s).doubleValue();
							break;
						case 3:
							km = nf.parse(s).doubleValue();
							break;
						}
					} catch (ParseException e) {
						// do nothing
					}
			}
		}
	}

}
