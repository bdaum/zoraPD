package com.bdaum.zoom.gps.naming.google.internal;

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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

import com.bdaum.zoom.batch.internal.DoneParsingException;
import com.bdaum.zoom.gps.geonames.IGeonamingService;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.gps.WaypointArea;

@SuppressWarnings("restriction")
public class GoogleGeonamingService implements IGeonamingService {

	private static final String COM_BDAUM_ZOOM_GPS = "com.bdaum.zoom.gps"; //$NON-NLS-1$
	public static final NumberFormat usformat = NumberFormat.getInstance(Locale.US);
	static {
		usformat.setMaximumFractionDigits(5);
	}

	public Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException, UnknownHostException {
		String lang = Locale.getDefault().getLanguage();
		String latitude = usformat.format(lat);
		String longitude = usformat.format(lon);
		String[] parms = new String[] { latitude, longitude, lang };
		return fetchPlaceInfoFromGoogle(parms, lat, lon);
	}

	private static Place fetchPlaceInfoFromGoogle(String[] parms, double lat, double lon)
			throws IOException, SAXException, ParserConfigurationException, UnknownHostException {
		Place place = new Place();
		place.setLat(lat);
		place.setLon(lon);
		try (InputStream in = openGoogleService(
				NLS.bind("http://maps.google.com/maps/api/geocode/xml?latlng={0},{1}&sensor=false&language={2}", //$NON-NLS-1$
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
		String privateKey = preferencesService.getString(COM_BDAUM_ZOOM_GPS, PreferenceConstants.GOOGLEPRIVATEKEY, "", //$NON-NLS-1$
				null);
		if (clientId != null && !clientId.isEmpty() && privateKey != null && !privateKey.isEmpty()) {
			String cand = query + (NLS.bind("&client={0}", clientId)); //$NON-NLS-1$
			try {
				query += (NLS.bind("&client={0}&signature={0}", clientId, //$NON-NLS-1$
						UrlSigner.computeSignature(cand, privateKey)));
			} catch (Exception e) {
				GoogleNamingActivator.getDefault().logError(Messages.GoogleGeonamingService_error_signing, e);
			}
		}
		return NetActivator.getDefault().openStream(query);
	}

	public WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException, ParserConfigurationException {
		List<WaypointArea> pnts = new ArrayList<WaypointArea>();
		Locale locale = Locale.getDefault();
		try (InputStream in = openGoogleService(NLS.bind(
				"http://maps.googleapis.com/maps/api/geocode/xml?address={0}&region={1}&language={2}", //$NON-NLS-1$
				new Object[] { URLEncoder.encode(address, "UTF-8"), locale.getCountry(), locale.getLanguage() }))) { //$NON-NLS-1$
			new GoogleGeoCodeParser(in).parse(pnts);
		} catch (DoneParsingException e) {
			// everything okay
		}
		return pnts.toArray(new WaypointArea[pnts.size()]);
	}

}
