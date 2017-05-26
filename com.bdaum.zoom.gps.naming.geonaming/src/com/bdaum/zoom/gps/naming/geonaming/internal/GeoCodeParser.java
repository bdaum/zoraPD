package com.bdaum.zoom.gps.naming.geonaming.internal;

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

public class GeoCodeParser extends AbstractParser {

	protected static final String GEONAME = "geoname"; //$NON-NLS-1$
	protected static final String LAT = "lat"; //$NON-NLS-1$
	protected static final String LNG = "lng"; //$NON-NLS-1$
	protected static final String STATUS = "status"; //$NON-NLS-1$
	protected static final String MESSAGE = "message"; //$NON-NLS-1$
	protected static final String TOPONYMNAME = "toponymName"; //$NON-NLS-1$
	protected static final String COUNTRYNAME = "countryName"; //$NON-NLS-1$
	protected static final String COUNTRYCODE = "countryCode"; //$NON-NLS-1$
	protected static final String TOTALRESULTCOUNT = "totalResultsCount";//$NON-NLS-1$
	public static final int MAXRESULTS = 100;

	public GeoCodeParser(InputStream in) throws ParserConfigurationException,
			SAXException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean geo = false;
			private WaypointArea waypoint;
			private String toponymname;
			private String countrycode;
			private String countryname;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException {
				if (STATUS.equals(qName)) {
					String message = atts.getValue(MESSAGE);
					throw new WebServiceException(NLS.bind(
							Messages.getString("GeoCodeParser.web_service_exception"), message)); //$NON-NLS-1$
				} else if (GEONAME.equals(qName)) {
					geo = true;
					waypoint = null;
					toponymname = null;
					countrycode = null;
					countryname = null;
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
					if (GEONAME.equals(qName)) {
						geo = false;
						StringBuilder name = new StringBuilder();
						if (toponymname != null)
							name.append(toponymname);
						if (countrycode != null) {
							if (name.length() > 0)
								name.append(", "); //$NON-NLS-1$
							name.append(countrycode);
						}
						if (countryname != null) {
							if (name.length() > 0)
								name.append(", "); //$NON-NLS-1$
							name.append(countryname);
						}
						getWaypoint().setName(name.toString());
					} else {
						String s = text.toString();
						if (TOPONYMNAME.equals(qName)) {
							toponymname = s;
						} else if (COUNTRYCODE.equals(qName)) {
							countrycode = s;
						} else if (COUNTRYNAME.equals(qName)) {
							countryname = s;
						} else if (LAT.equals(qName)) {
							try {
								double lat = getDouble(text.toString());
								getWaypoint().setLat(lat);
							} catch (ParseException e) {
								throw new SAXException(e);
							}
						} else if (LNG.equals(qName)) {
							try {
								double lon = getDouble(text.toString());
								getWaypoint().setLon(lon);
							} catch (ParseException e) {
								throw new SAXException(e);
							}
						}
					}
				} else if (TOTALRESULTCOUNT.equals(qName)) {
					try {
						int totalResultCount = Integer.parseInt(text.toString());
						if (totalResultCount > MAXRESULTS)
							throw new WebServiceException(NLS.bind(Messages.getString("GeoCodeParser.too_many_results"), totalResultCount)); //$NON-NLS-1$
					} catch (NumberFormatException e) {
						// No check possible
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
