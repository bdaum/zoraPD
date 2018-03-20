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

package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.gps.geonames.WebServiceException;

public class AddressParser extends AbstractGeonamesParser {

	private static final String ADDRESS = "address"; //$NON-NLS-1$
	private static final String STREET = "street"; //$NON-NLS-1$
	private static final String STREETNUMBER = "streetNumber"; //$NON-NLS-1$
	private static final String POSTALCODE = "postalcode"; //$NON-NLS-1$
	private static final String PLACENAME = "placename"; //$NON-NLS-1$
	private static final String LAT = "lat"; //$NON-NLS-1$
	private static final String LNG = "lng"; //$NON-NLS-1$
	private static final String ADMINNAME1 = "adminName1"; //$NON-NLS-1$
	private static final String DISTANCE = "distance"; //$NON-NLS-1$

	public AddressParser(InputStream in) throws SAXException,
			ParserConfigurationException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean addr = false;
			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException, WebServiceException {
				checkStatus(qName, atts);
				if (ADDRESS.equals(qName))
					addr = true;
				text.setLength(0);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				text.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (ADDRESS.equals(qName))
					addr = false;
				else if (addr) {
					try {
						String s = text.toString();
						if (POSTALCODE.equals(qName))
							place.setPostalcode(s);
						else if (PLACENAME.equals(qName))
							place.setName(s);
						else if (LAT.equals(qName))
							place.setLat(getDouble(s));
						else if (LNG.equals(qName))
							place.setLon(getDouble(s));
						else if (ADMINNAME1.equals(qName))
							place.setState(s);
						else if (STREET.equals(qName))
							place.setStreet(s);
						else if (STREETNUMBER.equals(qName))
							place.setStreetnumber(s);
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
