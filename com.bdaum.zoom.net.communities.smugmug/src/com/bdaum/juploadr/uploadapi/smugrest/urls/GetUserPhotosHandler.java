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

package com.bdaum.juploadr.uploadapi.smugrest.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;

public class GetUserPhotosHandler extends DefaultSmugmugHandler {
    private GetUserPhotos gup;
 
    public GetUserPhotosHandler(Object parent) {
        super(parent);
        gup = (GetUserPhotos) parent;
    }

    
	@Override
	public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qname, atts);
        if("user".equals(qname)) { //$NON-NLS-1$
            try {
                gup.setURL(new URL(atts.getValue("url"))); //$NON-NLS-1$
            } catch (MalformedURLException e) {
               // let's hope that flickr doesn't send us malformed URLs.
            }
        }
    }

}
