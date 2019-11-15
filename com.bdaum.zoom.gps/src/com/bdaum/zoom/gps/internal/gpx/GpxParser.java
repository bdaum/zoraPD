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

package com.bdaum.zoom.gps.internal.gpx;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.ui.gps.IGpsParser;
import com.bdaum.zoom.ui.gps.Trackpoint;

public class GpxParser implements IGpsParser {

	private static final String SPED = "speed"; //$NON-NLS-1$
	private static final String ELE = "ele"; //$NON-NLS-1$
	private static final String TIM = "time"; //$NON-NLS-1$
	private static final String RTEPT = "rtept"; //$NON-NLS-1$
	private static final String WPT = "wpt"; //$NON-NLS-1$
	private static final String TRKPT = "trkpt"; //$NON-NLS-1$
	private static final String TRKSEG = "trkseg"; //$NON-NLS-1$
	private static final String LATITUDE = "lat"; //$NON-NLS-1$
	private static final String LONGITUDE = "lon"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.gpx.IGpsParser#parse(InputStream in)
	 */
	public void parse(InputStream in, final List<Trackpoint> points) throws IOException, ParseException {
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				private Trackpoint trkpnt;
				private StringBuilder text = new StringBuilder();
				private int segment = -1;

				@Override
				public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
						throws SAXException {
					if (TRKSEG.equals(qName))
						segment = 0;
					else if (TRKPT.equals(qName)) {
						if (segment >= 0)
							++segment;
						trkpnt = new Trackpoint(parseDouble(atts.getValue("", LATITUDE)), //$NON-NLS-1$
								parseDouble(atts.getValue("", LONGITUDE)), false); //$NON-NLS-1$
					} else if (WPT.equals(qName) || RTEPT.equals(qName)) {
						trkpnt = new Trackpoint(parseDouble(atts.getValue("", LATITUDE)), //$NON-NLS-1$
								parseDouble(atts.getValue("", LONGITUDE)), true); //$NON-NLS-1$
					}
					text.setLength(0);
				}

				@Override
				public void characters(char[] ch, int start, int length) {
					if (trkpnt != null)
						text.append(ch, start, length);
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (TRKSEG.equals(qName)) {
						if (segment > 0)
							points.get(points.size() - 1).setSegmentEnd(true);
						segment = -1;
					} else if (TRKPT.equals(qName) || WPT.equals(qName) || RTEPT.equals(qName)) {
						if (trkpnt != null) {
							if (trkpnt.getTime() >= 0)
								points.add(trkpnt);
							trkpnt = null;
						}
					} else if (trkpnt != null) {
						int p = qName.indexOf(':');
						String tagName = p < 0 ? qName : qName.substring(p + 1).trim();
						if (TIM.equals(tagName) || ELE.equals(tagName) || SPED.equals(tagName)) {
							String s = text.toString();
							if (TIM.equals(tagName))
								trkpnt.setTime(text2date(s).getTime());
							else if (SPED.equals(tagName))
								trkpnt.setSpeed(parseDouble(s));
							else if (ELE.equals(tagName))
								trkpnt.setAltitude(parseDouble(s));
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

	private static Date text2date(String text) throws SAXException {
		try {
			SimpleDateFormat df = Format.XML_DATE_TIME_FORMAT.get(); 
			df.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
			if (text.endsWith("Z")) //$NON-NLS-1$
				return df.parse(text.substring(0, text.length() - 1));
			String shift;
			boolean minus = false;
			int p = text.lastIndexOf('+');
			if (p >= 0)
				shift = text.substring(p + 1);
			else {
				p = text.lastIndexOf('-');
				if (p < 0)
					return df.parse(text);
				shift = text.substring(p + 1);
				minus = true;
			}
			Date date = df.parse(text.substring(0, p));
			p = shift.indexOf(':');
			int offset = (p >= 0)
					? Integer.parseInt(shift.substring(0, p)) * 60 + Integer.parseInt(shift.substring(p + 1))
					: Integer.parseInt(shift) * 60;
			long t = date.getTime();
			date.setTime(minus ? t - offset * 60000L : t + offset * 60000L);
			return date;
		} catch (ParseException e) {
			throw new SAXException(e);
		}
	}
}
