package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

import com.bdaum.zoom.gps.geonames.IGeonamingService;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.gps.WaypointArea;

@SuppressWarnings("restriction")
public class GeoNamesService implements IGeonamingService {

	private static final String COM_BDAUM_ZOOM_GPS_NAMING_GEONAMING = "com.bdaum.zoom.gps.naming.geonaming"; //$NON-NLS-1$
	public static final NumberFormat usformat = NumberFormat.getInstance(Locale.US);
	private static final String USERNAME = "photozora"; //$NON-NLS-1$
	static {
		usformat.setMaximumFractionDigits(5);
	}

	public Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException, UnknownHostException, HttpException {
		String lang = Locale.getDefault().getLanguage();
		String latitude = usformat.format(lat);
		String longitude = usformat.format(lon);
		String[] parms = new String[] { latitude, longitude, lang };
		return fetchPlaceInfoFromGeoNames(lang, parms);
	}

	private static Place fetchPlaceInfoFromGeoNames(String lang, String[] parms) throws IOException, SAXException,
			ParserConfigurationException, WebServiceException, UnknownHostException, HttpException {
		Place place = new Place();
		try (InputStream in = openGeonamesService(
				NLS.bind("http://ws.geonames.org/findNearbyPlaceName?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
						parms))) {
			new PlaceParser(in).parse(place);
		}
		if (place.getId() != null) {
			try (InputStream in = openGeonamesService(
					NLS.bind("http://ws.geonames.org/hierarchy?geonameId={0}&lang={1}", //$NON-NLS-1$
							place.getId(), lang))) {
				new HierarchyParser(in).parse(place);
			}
		}
		if ("US".equals(place.getCountryCode())) { //$NON-NLS-1$
			try (InputStream in = openGeonamesService(
					NLS.bind("http://ws.geonames.org/findNearestAddress?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
							parms))) {
				new AddressParser(in).parse(place);
				return (place.getName() != null) ? place : null;
			}
		}
		try (InputStream in = openGeonamesService(
				NLS.bind("http://ws.geonames.org/findNearbyPostalCodes?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
						parms))) {
			new PostcodeParser(in).parse(place);
			return (place.getName() != null) ? place : null;
		}
	}

	private static InputStream openGeonamesService(String query)
			throws IOException, UnknownHostException, HttpException {
		String parms = Platform.getPreferencesService().getString(COM_BDAUM_ZOOM_GPS_NAMING_GEONAMING,
				PreferenceConstants.GEONAMESPARMS, "", null); //$NON-NLS-1$
		if (parms != null && parms.length() > 0) {
			if (parms.startsWith("&")) //$NON-NLS-1$
				query += parms;
			else
				query += ('&' + parms);
		} else
			query += ("&username=") + USERNAME; //$NON-NLS-1$
		return NetActivator.getDefault().openStream(query);
	}

	public WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException, ParserConfigurationException, HttpException {
		List<WaypointArea> pnts = new ArrayList<WaypointArea>();
		Locale locale = Locale.getDefault();
		try (InputStream in = openGeonamesService(NLS.bind("http://ws.geonames.org/search?q={0}&lang={2}&maxRows={3}", //$NON-NLS-1$
				new Object[] { URLEncoder.encode(address, "UTF-8"), locale.getCountry(), //$NON-NLS-1$
						locale.getLanguage(), GeoCodeParser.MAXRESULTS }))) {
			new GeoCodeParser(in).parse(pnts);
			return pnts.toArray(new WaypointArea[pnts.size()]);
		}
	}

}
