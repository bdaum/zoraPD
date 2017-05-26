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

package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.gps.geonames.WebServiceException;

public class PlaceParser extends AbstractGeonamesParser {

	private static final String GEONAME = "geoname"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String LAT = "lat"; //$NON-NLS-1$
	private static final String LNG = "lng"; //$NON-NLS-1$
	private static final String ID = "geonameId"; //$NON-NLS-1$
	private static final String COUNDTRYCODE = "countryCode"; //$NON-NLS-1$
	private static final String COUNTRYNAME = "countryName"; //$NON-NLS-1$
	private static final String DISTANCE = "distance"; //$NON-NLS-1$

	public PlaceParser(InputStream in) throws SAXException,
			ParserConfigurationException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean geo = false;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException, WebServiceException {
				checkStatus(qName, atts);
				if (GEONAME.equals(qName))
					geo = true;
				text.setLength(0);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				text.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (GEONAME.equals(qName))
					geo = false;
				else if (geo) {
					try {
						String s = text.toString();
						if (NAME.equals(qName))
							place.setName(s);
						else if (LAT.equals(qName))
							place.setLat(getDouble(s));
						else if (LNG.equals(qName))
							place.setLon(getDouble(s));
						else if (ID.equals(qName))
							place.setId(s);
						else if (COUNDTRYCODE.equals(qName))
							place.setCountryCode(s);
						else if (COUNTRYNAME.equals(qName))
							place.setCountryName(s);
						else if (DISTANCE.equals(qName))
							place.setDistance(getDouble(s));
					} catch (ParseException e) {
						throw new SAXException(e);
					}
				}
			}
		};
		return handler;
	}

}
