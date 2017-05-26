/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.juploadr.uploadapi.smugrest.authentification;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;

public class LoginWithPasswordHandler extends DefaultSmugmugHandler {
	private LoginWithPassword parent;

	public LoginWithPasswordHandler(Object parent) {
		super(parent);
		this.parent = (LoginWithPassword) parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.scohen.juploadr.uploadapi.rest.DefaultFlickrHandler#startElement(
	 * java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	
	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes atts) throws SAXException {
		super.startElement(uri, localName, qname, atts);
		lastTag = qname;
		if ("Session".equals(qname)) { //$NON-NLS-1$
			parent.setSessionId(atts.getValue("id")); //$NON-NLS-1$
		} else if ("Login".equals(qname)) { //$NON-NLS-1$
			String value = atts.getValue("AccountType"); //$NON-NLS-1$
			if (value.equals("Pro")) //$NON-NLS-1$
				parent.setAccountType(LoginWithPassword.PRO);
			else if (value.equals("Power")) //$NON-NLS-1$
				parent.setAccountType(LoginWithPassword.POWER);
			else
				parent.setAccountType(LoginWithPassword.STANDARD);
			parent.setMaxFileSize(Long
					.parseLong(atts.getValue("FileSizeLimit"))); //$NON-NLS-1$
		}
	}

}
