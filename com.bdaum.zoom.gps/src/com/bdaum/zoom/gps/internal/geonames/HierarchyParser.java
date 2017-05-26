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

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.gps.geonames.WebServiceException;

public class HierarchyParser extends AbstractGeonamesParser {

	private static final String ADM1 = "ADM1"; //$NON-NLS-1$
	private static final String CONT = "CONT"; //$NON-NLS-1$
	private static final String GEONAME = "geoname"; //$NON-NLS-1$
	private static final String FCODE = "fcode"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$

	public HierarchyParser(InputStream in) throws SAXException,
			ParserConfigurationException {
		super(in);
	}

	@Override
	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean geo = false;
			String name;
			String fcode;

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException, WebServiceException {
				checkStatus(qName, atts);
				if (GEONAME.equals(qName)) {
					geo = true;
					name = null;
					fcode = null;
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
				if (GEONAME.equals(qName)) {
					geo = false;
					if (CONT.equals(fcode))
						place.setContinent(name);
					if (ADM1.equals(fcode))
						place.setState(name);
				} else if (geo) {
					if (NAME.equals(qName))
						name = text.toString();
					else if (FCODE.equals(qName))
						fcode = text.toString();
				}
			}
		};
		return handler;
	}

}
