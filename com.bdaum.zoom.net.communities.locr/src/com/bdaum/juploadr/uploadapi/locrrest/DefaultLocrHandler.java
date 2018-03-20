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
package com.bdaum.juploadr.uploadapi.locrrest;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class DefaultLocrHandler extends DefaultHandler {

    protected String lastTag;
    protected String failureReason;
    protected int errorCode = 0;

    public DefaultLocrHandler(Object par) {
    	// super();
    }

    
	@Override
	public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
        lastTag = qname;
        if ("error".equals(qname)) { //$NON-NLS-1$
            failureReason = atts.getValue("message"); //$NON-NLS-1$
            errorCode = Integer.parseInt(atts.getValue("code")); //$NON-NLS-1$
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
