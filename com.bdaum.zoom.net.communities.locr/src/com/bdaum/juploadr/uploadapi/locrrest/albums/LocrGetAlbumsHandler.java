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
package com.bdaum.juploadr.uploadapi.locrrest.albums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scohen.juploadr.app.PhotoSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class LocrGetAlbumsHandler extends DefaultLocrHandler {

	private ArrayList<PhotoSet> photoSets;
	private PhotoSet photoSet;

	public LocrGetAlbumsHandler(Object parent) {
		super(parent);
		photoSets = new ArrayList<PhotoSet>();
	}

	
	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes atts) throws SAXException {
		super.startElement(uri, localName, qname, atts);
		if ("album".equals(qname)) { //$NON-NLS-1$
			if (photoSet != null) {
				photoSets.add(photoSet);
			}
			photoSet = new LocrPhotoSet();
			photoSet.setNew(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	
	@Override
	public void characters(char[] chars, int start, int end)
			throws SAXException {
		super.characters(chars, start, end);
		String cdata = new String(chars, start, end).trim();
		if (!cdata.isEmpty() && photoSet != null) {
			if ("name".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setTitle(cdata);
			} else if ("description".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setDescription(cdata);
			} else if ("id".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setId(cdata);
			} else if ("photos_count".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setNumberOfPhotos(Integer.parseInt(cdata));
			} else if ("thumbnail_id".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setPrimaryPhotoId(cdata);
			} else if ("user_url".equals(lastTag)) { //$NON-NLS-1$
				photoSet.setUrl(cdata);
			}
		}
	}

	
	@Override
	public void endDocument() throws SAXException {
		if (photoSet != null) {
			photoSets.add(photoSet);
		}
	}

	public List<PhotoSet> getPhotoSets() {
		Collections.sort(photoSets);
		return photoSets;
	}

}
