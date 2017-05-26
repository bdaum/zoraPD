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
package com.bdaum.juploadr.uploadapi.locrrest.upload;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class LocrUploadResponseHandler extends DefaultLocrHandler {

	private String photoID;
	private String status;
	private String verbose;

	public LocrUploadResponseHandler(Object parent) {
		super(parent);
	}

	
	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes atts) throws SAXException {
		if ("post_photo".equals(qname)) { //$NON-NLS-1$
			status = atts.getValue("status"); //$NON-NLS-1$
		}
		super.startElement(uri, localName, qname, atts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	
	@Override
	public void characters(char[] chars, int start, int end)
			throws SAXException {
		super.characters(chars, start, end);

		String cdata = new String(chars, start, end).trim();
		if (cdata.length() > 0) {
			if ("id".equals(lastTag)) { //$NON-NLS-1$
				photoID = cdata;
			} 
		}
	}

	/**
	 * @param errorCode
	 *            The errorCode to set.
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return Returns the photoID.
	 */
	public String getPhotoID() {
		return photoID;
	}

	/**
	 * @param photoID
	 *            The photoID to set.
	 */
	public void setPhotoID(String photoID) {
		this.photoID = photoID;
	}

	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return Returns the verbose.
	 */
	public String getVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            The verbose to set.
	 */
	public void setVerbose(String verbose) {
		this.verbose = verbose;
	}

}
