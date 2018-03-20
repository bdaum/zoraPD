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

package com.bdaum.juploadr.uploadapi.smugrest.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scohen.juploadr.app.Category;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;

public class GetCategoryListHandler extends DefaultSmugmugHandler {
    private SmugmugCategory category;
    private List<SmugmugCategory> categories;

    public GetCategoryListHandler(Object parent) {
        super(parent);
        categories = new ArrayList<SmugmugCategory>();

    }

    
	@Override
	public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
    	if ("err".equals(qname)) { //$NON-NLS-1$
    		if ("15".equals(atts.getValue("code")))  //$NON-NLS-1$ //$NON-NLS-2$
    			return;
    	}
        super.startElement(uri, localName, qname, atts);
        if ("Category".equals(qname)) { //$NON-NLS-1$
            if (category != null) {
                categories.add(category);
            }
            category = new SmugmugCategory();
            category.setId(atts.getValue("id")); //$NON-NLS-1$
            category.setTitle(atts.getValue("Title")); //$NON-NLS-1$
        }
    }


    
	@Override
	public void endDocument() throws SAXException {
        if (category != null) {
            categories.add(category);
        }
    }

    public List<SmugmugCategory> getCategories() {
    	Category ps = new SmugmugCategory();
        Collections.sort(categories, ps.new TitleComparator());
        return categories;
    }

}
