package com.bdaum.zoom.gps.naming.google.internal;

import java.io.InputStream;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.batch.internal.DoneParsingException;
import com.bdaum.zoom.common.GeoMessages;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.LocationConstants;
import com.bdaum.zoom.gps.geonames.AbstractParser;
import com.bdaum.zoom.gps.geonames.WebServiceException;

@SuppressWarnings("restriction")
public class GooglePlaceParser extends AbstractParser {
	private static final String RESULT = "result"; //$NON-NLS-1$
	private static final String LNG = "lng"; //$NON-NLS-1$
	private static final String LAT = "lat"; //$NON-NLS-1$
	private static final String LOCATION = "location"; //$NON-NLS-1$
	private static final String GEOMETRY = "geometry"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	private static final String SHORT_NAME = "short_name"; //$NON-NLS-1$
	private static final String LONG_NAME = "long_name"; //$NON-NLS-1$
	private static final String ADDRESS_COMPONENT = "address_component"; //$NON-NLS-1$
	private static final String GEOCODE_RESPONSE = "GeocodeResponse"; //$NON-NLS-1$
	private static final String STATUS = "status";//$NON-NLS-1$
	private static final String STATUS_OK = "OK"; //$NON-NLS-1$
	//	private static final String STATUS_NORESULTS = "ZERO_RESULTS"; //$NON-NLS-1$
	private static final String STATUS_QUERYLIMIT = "OVER_QUERY_LIMIT"; //$NON-NLS-1$
	//	private static final String STATUS_DENIED = "REQUEST_DENIED"; //$NON-NLS-1$
	//	private static final String STATUS_INVALID = "INVALID_REQUEST"; //$NON-NLS-1$

	public GooglePlaceParser(InputStream in)
			throws ParserConfigurationException, SAXException {
		super(in);
		nf.setMaximumFractionDigits(7);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean geo = false;
			private boolean acomp;
			private String longName;
			private Set<String> types = new HashSet<String>();
			private String shortName;
			private boolean geometry;
			private boolean location;
			private double lat = Double.NaN;
			private double lng = Double.NaN;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException {
				if (GEOCODE_RESPONSE.equals(qName))
					geo = true;
				else if (geo) {
					if (ADDRESS_COMPONENT.equals(qName)) {
						acomp = true;
						types.clear();
						longName = null;
						shortName = null;
					} else if (GEOMETRY.equals(qName))
						geometry = true;
					else if (geometry && LOCATION.equals(qName))
						location = true;
				}
				text.setLength(0);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				text.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (geo) {
					String s = text.toString();
					if (STATUS.equals(qName) && !STATUS_OK.equals(s)) {
						if (STATUS_QUERYLIMIT.equals(s))
							throw new WebServiceException(
									NLS.bind(
											Messages.GooglePlaceParser_google_web_service_exception,
											text), new WebServiceException(s));
						throw new WebServiceException(
								NLS.bind(
										Messages.GooglePlaceParser_google_web_service_exception,
										s));
					} else if (ADDRESS_COMPONENT.equals(qName)) {
						if (types.contains("country")) { //$NON-NLS-1$
							place.setCountryName(longName);
							place.setCountryCode(shortName);
							String continentCode = LocationConstants.countryToContinent
									.get(shortName);
							if (continentCode != null)
								place.setContinent(GeoMessages
										.getString(GeoMessages.PREFIX
												+ continentCode));
						} else if (types.contains("postal_code")) //$NON-NLS-1$
							place.setPostalcode(longName);
						else if (types.contains("locality")) //$NON-NLS-1$
							place.setName(longName);
						else if (types.contains("route")) //$NON-NLS-1$
							place.setStreet(longName);
						else if (types.contains("street_number")) //$NON-NLS-1$
							place.setStreetnumber(longName);
						else if (types.contains("administrative_area_level_1")) //$NON-NLS-1$
							place.setState(longName);
						acomp = false;
					}
					if (acomp) {
						if (LONG_NAME.equals(qName))
							longName = s;
						else if (SHORT_NAME.equals(qName))
							shortName = s;
						else if (TYPE.equals(qName))
							types.add(s);
					} else if (geometry && GEOMETRY.equals(qName))
						geometry = false;
					else if (location) {
						if (LOCATION.equals(qName)) {
							location = false;
						} else if (LAT.equals(qName)) {
							try {
								lat = getDouble(text.toString());
							} catch (ParseException e) {
								throw new SAXException(e);
							}
						} else if (LNG.equals(qName)) {
							try {
								lng = getDouble(text.toString());
							} catch (ParseException e) {
								throw new SAXException(e);
							}
						}
					}
					if (RESULT.equals(qName)) {
						if (!Double.isNaN(lat) && !Double.isNaN(lng))
							place.setDistance(Core.distance(place.getLat(),
									place.getLon(), lat, lng, 'k'));
						place.setLat(lat);
						place.setLon(lng);
						throw new DoneParsingException();
					}
				}
			}
		};
		return handler;
	}

}
