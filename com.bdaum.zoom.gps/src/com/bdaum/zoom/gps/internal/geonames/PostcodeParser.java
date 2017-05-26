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

package com.bdaum.zoom.gps.internal.geonames;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.gps.geonames.WebServiceException;

public class PostcodeParser extends AbstractGeonamesParser {

	private static final String CODE = "code"; //$NON-NLS-1$
	private static final String POSTALCODE = "postalcode"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String COUNDTRYCODE = "countryCode"; //$NON-NLS-1$
	private static final String ADMINNAME1 = "adminName1"; //$NON-NLS-1$
	private static final String DISTANCE = "distance"; //$NON-NLS-1$
	private static final String LAT = "lat"; //$NON-NLS-1$
	private static final String LNG = "lng"; //$NON-NLS-1$

	public PostcodeParser(InputStream in) throws SAXException,
			ParserConfigurationException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean cod = false;
			String adminName1;
			String countryCode;
			String name;
			String postalcode;
			double distance;
			double lat;
			double lon;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException, WebServiceException {
				checkStatus(qName, atts);
				if (CODE.equals(qName)) {
					cod = true;
					adminName1 = null;
					countryCode = null;
					name = null;
					postalcode = null;
					distance = Double.NaN;
					lat = Double.NaN;
					lon = Double.NaN;
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
				if (CODE.equals(qName)) {
					cod = false;
					if (place.getName() == null || place.getName().equals(name)) {
						if (place.getName() == null) {
							place.setName(name);
							place.setDistance(distance);
							place.setLat(lat);
							place.setLon(lon);
						}
						if (place.getCountryCode() == null)
							place.setCountryCode(countryCode);
						if (place.getState() == null)
							place.setState(adminName1);
						place.setPostalcode(postalcode);
					}
				} else if (cod) {
					try {
						String s = text.toString();
						if (POSTALCODE.equals(qName))
							postalcode = s;
						else if (NAME.equals(qName))
							name = s;
						else if (ADMINNAME1.equals(qName))
							adminName1 = s;
						else if (COUNDTRYCODE.equals(qName))
							countryCode = s;
						else if (DISTANCE.equals(qName))
							distance = getDouble(s);
						else if (LAT.equals(qName))
							lat = getDouble(s);
						else if (LNG.equals(qName))
							lon = getDouble(s);
					} catch (ParseException e) {
						throw new SAXException(e);
					}
				}
			}
		};
		return handler;
	}

}
