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
 * Modifications (c) 2009 Berthold Daum  
 */

package com.bdaum.juploadr.uploadapi.smugrest;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class DefaultSmugmugHandler extends DefaultHandler {

	protected String lastTag;
	private String lastName;
	protected String failureReason;
	protected int errorCode = 0;
	private boolean fault;

	public DefaultSmugmugHandler(Object parent) {
		super();
	}

	
	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes atts) throws SAXException {
		lastTag = qname;
		if ("fault".equals(qname)) //$NON-NLS-1$
			fault = true;
		if ("err".equals(qname)) { //$NON-NLS-1$
			failureReason = atts.getValue("msg"); //$NON-NLS-1$
			errorCode = Integer.parseInt(atts.getValue("code")); //$NON-NLS-1$
		}
	}

	
	@Override
	public void endElement(String uri, String localName, String qname)
			throws SAXException {
		if ("fault".equals(qname)) //$NON-NLS-1$
			fault = false;
	}

	
	@Override
	public void characters(char[] chars, int start, int end)
			throws SAXException {
		if (fault) {
			String cdata = new String(chars, start, end).trim();
			if (!cdata.isEmpty()) {
				if ("name".equals(lastTag)) //$NON-NLS-1$
					lastName = cdata;
				else if ("int".equals(lastTag) && "faultCode".equals(lastName))  //$NON-NLS-1$//$NON-NLS-2$
					errorCode = Integer.parseInt(cdata);
				else if ("string".equals(lastTag) && "faultString".equals(lastName))  //$NON-NLS-1$//$NON-NLS-2$
					failureReason = cdata; 
			}
		}
	}

	public boolean isSuccessful() {
		return failureReason == null;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getFailureReason() {
		return failureReason;
	}

}
