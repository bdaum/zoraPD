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

package com.bdaum.juploadr.uploadapi.smugrest.photosets;

import org.scohen.juploadr.app.AbstractPhotoSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;

public class CreatePhotoSetResponseHandler extends DefaultSmugmugHandler {
    private AbstractPhotoSet set;

    public CreatePhotoSetResponseHandler(Object parent) {
        super(parent);
        set = (AbstractPhotoSet) parent;
    }

    
	@Override
	public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
        super.startElement(uri, localName, qname, atts);
        if ("Album".equals(qname)) { //$NON-NLS-1$
            set.setId(atts.getValue("id")); //$NON-NLS-1$
            set.setSecret(atts.getValue("Key")); //$NON-NLS-1$
        }
    }

}
