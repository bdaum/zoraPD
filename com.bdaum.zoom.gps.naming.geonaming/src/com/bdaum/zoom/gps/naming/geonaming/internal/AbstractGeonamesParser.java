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
 * (c) 2009-2011 Berthold Daum  
 */

package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.zoom.gps.geonames.AbstractParser;
import com.bdaum.zoom.gps.geonames.WebServiceException;

public abstract class AbstractGeonamesParser extends AbstractParser {

	private static final String[] ERRORTEXTS = new String[] {
			Messages.getString("AbstractGeonamesParser.e10"), // 10 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e11"), // 11 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e12"), // 12 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e13"), // 13 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e14"), // 14 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e15"), // 15 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e16"), // 16 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e17"), // 17 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e18"), // 18 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e19"), // 19 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e20"), // 20 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e21"), // 21 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e22"), // 22 //$NON-NLS-1$
			Messages.getString("AbstractGeonamesParser.e23"), // 23 //$NON-NLS-1$
	};

	private static final boolean[] aborting = new boolean[] { true, // 10
			false, // 11
			false, // 12
			false, // 13
			false, // 14
			false, // 15
			false, // 16
			false, // 17
			true, // 18
			true, // 19
			true, // 20
			false, // 21
			true, // 22
			true // 23
	};

	private static final String STATUS = "status";//$NON-NLS-1$
	protected static final String VALUE = "value"; //$NON-NLS-1$

	public AbstractGeonamesParser(InputStream in)
			throws ParserConfigurationException, SAXException {
		super(in);
		nf.setMaximumFractionDigits(5);
	}

	protected static void checkStatus(String tag, Attributes atts)
			throws WebServiceException {
		if (STATUS.equals(tag)) {
			String ecode = atts.getValue("", VALUE); //$NON-NLS-1$
			try {
				int n = Integer.parseInt(ecode);
				if (n >= 10 && n <= 23) {
					ecode += " (" + ERRORTEXTS[n - 10] + ")"; //$NON-NLS-1$//$NON-NLS-2$
					if (aborting[n - 10])
						throw new WebServiceException(
								NLS.bind(
										Messages.getString("AbstractGeonamesParser.Geonames_exception"), //$NON-NLS-1$
										ecode), new WebServiceException(ecode));
				}
			} catch (NumberFormatException e) {
				// do nothing
			}
			throw new WebServiceException(NLS.bind(
					Messages.getString("AbstractGeonamesParser.Geonames_exception"), //$NON-NLS-1$
					ecode));
		}
	}

}