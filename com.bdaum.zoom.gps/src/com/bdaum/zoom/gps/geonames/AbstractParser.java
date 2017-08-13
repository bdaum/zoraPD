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
package com.bdaum.zoom.gps.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.ui.gps.WaypointArea;

public abstract class AbstractParser {

	protected final NumberFormat nf = NumberFormat.getInstance();
	protected final SAXParser saxParser = SAXParserFactory.newInstance()
			.newSAXParser();
	protected final InputStream in;
	protected final StringBuilder text = new StringBuilder();
	protected Place place;
	protected List<WaypointArea> pnts;

	public AbstractParser(InputStream in) throws ParserConfigurationException,
			SAXException {
		this.in = in;
	}

	protected double getDouble(String s) throws ParseException {
		s = s.trim();
		if (s.isEmpty())
			return Double.NaN;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return nf.parse(s).doubleValue();
		}
	}

	public void parse(Place p) throws SAXException, WebServiceException,
			IOException {
		this.place = p;
		saxParser.parse(in, getHandler());
	}

	public void parse(List<WaypointArea> pnts) throws SAXException,
			WebServiceException, IOException {
		this.pnts = pnts;
		saxParser.parse(in, getHandler());
	}

	protected abstract DefaultHandler getHandler();

}