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

package com.bdaum.juploadr.uploadapi.smugrest.upload;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;

/**
 * @author steve
 * 
 */
public class SmugmugUploadResponseHandler extends DefaultSmugmugHandler {
	private String photoID;
	private String key;
	private String url;

	public SmugmugUploadResponseHandler(Object parent) {
		super(parent);
	}

	
	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes atts) throws SAXException {
		super.startElement(uri, localName, qname, atts);
		if ("Image".equals(qname)) { //$NON-NLS-1$
			photoID = atts.getValue("id");//$NON-NLS-1$
			key = atts.getValue("Key");//$NON-NLS-1$
			url = atts.getValue("URL");//$NON-NLS-1$
		}
	}

	/**
	 * @return Returns the photoID.
	 */
	public String getPhotoID() {
		return photoID;
	}

	public String getKey() {
		return key;
	}

	public String getUrl() {
		return url;
	}



}
