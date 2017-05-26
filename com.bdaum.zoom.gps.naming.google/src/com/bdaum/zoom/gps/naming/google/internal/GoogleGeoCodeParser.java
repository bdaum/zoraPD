package com.bdaum.zoom.gps.naming.google.internal;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.gps.geonames.AbstractParser;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.ui.gps.WaypointArea;

public class GoogleGeoCodeParser extends AbstractParser {
	private static final String RESULT = "result"; //$NON-NLS-1$
	private static final String ADDRESS = "formatted_address"; //$NON-NLS-1$
	private static final String LNG = "lng"; //$NON-NLS-1$
	private static final String LAT = "lat"; //$NON-NLS-1$
	private static final String LOCATION = "location"; //$NON-NLS-1$
	private static final String GEOMETRY = "geometry"; //$NON-NLS-1$
	private static final String VIEWPORT = "viewport"; //$NON-NLS-1$
	private static final String SOUTHWEST = "southwest"; //$NON-NLS-1$
	private static final String NORTHEAST = "northeast"; //$NON-NLS-1$
	private static final String STATUS = "status";//$NON-NLS-1$
	private static final String STATUS_OK = "OK"; //$NON-NLS-1$
	private static final String STATUS_QUERYLIMIT = "OVER_QUERY_LIMIT"; //$NON-NLS-1$
	protected boolean northeast;

	public GoogleGeoCodeParser(InputStream in)
			throws ParserConfigurationException, SAXException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean geo = false;
			private WaypointArea waypoint;
			private boolean geometry;
			private boolean location;
			private boolean viewport;
			private boolean southwest;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException {
				if (RESULT.equals(qName)) {
					geo = true;
					waypoint = null;
				} else if (geo) {
					if (GEOMETRY.equals(qName))
						geometry = true;
					else if (geometry && LOCATION.equals(qName))
						location = true;
					else if (geometry && VIEWPORT.equals(qName))
						viewport = true;
					else if (viewport && SOUTHWEST.equals(qName))
						southwest = true;
					else if (viewport && NORTHEAST.equals(qName))
						northeast = true;
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
					} else if (ADDRESS.equals(qName)) {
						getWaypoint().setName(s);
					}
					if (LAT.equals(qName)) {
						try {
							double lat = getDouble(text.toString());
							if (location)
								getWaypoint().setLat(lat);
							else if (southwest)
								getWaypoint().setSWlat(lat);
							else if (northeast)
								getWaypoint().setNElat(lat);
						} catch (ParseException e) {
							throw new SAXException(e);
						}
					} else if (LNG.equals(qName)) {
						try {
							double lon = getDouble(text.toString());
							if (location)
								getWaypoint().setLon(lon);
							else if (southwest)
								getWaypoint().setSWlon(lon);
							else if (northeast)
								getWaypoint().setNElon(lon);
						} catch (ParseException e) {
							throw new SAXException(e);
						}
					}
					if (location && LOCATION.equals(qName))
						location = false;
					if (viewport) {
						if (VIEWPORT.equals(qName))
							viewport = false;
						else if (SOUTHWEST.equals(qName))
							southwest = false;
						else if (NORTHEAST.equals(qName))
							northeast = false;
					}
				}
			}

			public WaypointArea getWaypoint() {
				if (waypoint == null) {
					waypoint = new WaypointArea();
					pnts.add(waypoint);
				}
				return waypoint;
			}
		};
		return handler;
	}

}
