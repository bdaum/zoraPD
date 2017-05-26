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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.gps.internal.gpx;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.ui.gps.IWaypointCollector;
import com.bdaum.zoom.ui.gps.RasterCoordinate;
import com.bdaum.zoom.ui.gps.Waypoint;
import com.bdaum.zoom.ui.gps.WaypointExtension;

public class WaypointCollector implements IWaypointCollector {

	private static final String GPXNS = "http://www.topografix.com/GPX/1/1"; //$NON-NLS-1$
	private static final String GPXEXTNS = "http://www.garmin.com/xmlschemas/GpxExtensions/v3"; //$NON-NLS-1$
	private static final String WAYPT = "wpt"; //$NON-NLS-1$
	private static final String LATITUDE = "lat"; //$NON-NLS-1$
	private static final String LONGITUDE = "lon"; //$NON-NLS-1$
	private static final String ELE = "ele"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String DESC = "desc"; //$NON-NLS-1$
	private static final String CAT = "Category"; //$NON-NLS-1$
	private static final String STREETADR = "StreetAddress"; //$NON-NLS-1$
	private static final String CITY = "City"; //$NON-NLS-1$
	private static final String POSTCODE = "PostalCode"; //$NON-NLS-1$
	private static final String PHONE = "PhoneNumber"; //$NON-NLS-1$

	public WaypointCollector() {
	}

	public void collect(InputStream in,
			final Map<RasterCoordinate, Waypoint> waypoints)
			throws IOException, ParseException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				private WaypointExtension waypnt;
				private List<String> categories = null;
				private StringBuilder text = new StringBuilder();

				@Override
				public void startElement(String namespaceURI, String localName,
						String qName, Attributes atts) throws SAXException {
					if (WAYPT.equals(qName) && GPXNS.equals(namespaceURI))
						waypnt = new WaypointExtension(
								parseDouble(atts.getValue("", LATITUDE)), //$NON-NLS-1$
								parseDouble(atts.getValue("", LONGITUDE))); //$NON-NLS-1$
					text.setLength(0);
				}

				@Override
				public void characters(char[] ch, int start, int length) {
					if (waypnt != null)
						text.append(ch, start, length);
				}

				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if (WAYPT.equals(localName) && GPXNS.equals(uri)) {
						if (waypnt != null) {
							if (categories != null)
								waypnt.setCategories(categories
										.toArray(new String[categories.size()]));
							waypoints.put(new RasterCoordinate(waypnt.getLat(),
									waypnt.getLon(), 2), waypnt);
							waypnt = null;
						}
					} else if (waypnt != null) {
						if (GPXNS.equals(uri)
								&& (NAME.equals(localName)
										|| ELE.equals(localName) || DESC
										.equals(localName))) {
							String s = text.toString();
							if (NAME.equals(localName))
								waypnt.setName(s);
							else if (DESC.equals(localName))
								waypnt.setDescription(s);
							else if (ELE.equals(localName))
								waypnt.setElevation(parseDouble(s));
						} else if (GPXEXTNS.equals(uri)
								&& (CAT.equals(localName)
										|| STREETADR.equals(localName)
										|| CITY.equals(localName)
										|| POSTCODE.equals(localName) || PHONE
										.equals(localName))) {
							String s = text.toString();
							if (CAT.equals(localName)) {
								if (categories == null)
									categories = new ArrayList<String>();
								categories.add(s);
							} else if (STREETADR.equals(localName)) {
								String n = null;
								int p = s.indexOf(',');
								if (p > 0) {
									n = s.substring(0, p).trim();
									if (n.length() > 0
											&& Character.isDigit(n.charAt(0)))
										s = s.substring(p + 1).trim();
									else
										n = null;
								}
								if (n == null) {
									p = s.lastIndexOf(' ');
									if (p >= 0) {
										n = s.substring(p + 1).trim();
										if (n.length() > 0
												&& Character.isDigit(n
														.charAt(0)))
											s = s.substring(0, p).trim();
										else
											n = null;
									}
								}
								waypnt.setStreetnumber(n);
								waypnt.setStreet(s);
							} else if (CITY.equals(localName))
								waypnt.setCity(s);
							else if (POSTCODE.equals(localName))
								waypnt.setPostalcode(s);
							else if (PHONE.equals(localName))
								waypnt.setPhone(s);
						}
					}
				}
			};
			saxParser.parse(in, handler);
		} catch (ParserConfigurationException e) {
			throw new IOException(e.toString());
		} catch (SAXException e) {
			throw new IOException(e.toString());
		}
	}

	private static double parseDouble(String text) throws SAXException {
		try {
			return Double.parseDouble(text);
		} catch (NumberFormatException e) {
			throw new SAXException(e);
		}
	}

}
