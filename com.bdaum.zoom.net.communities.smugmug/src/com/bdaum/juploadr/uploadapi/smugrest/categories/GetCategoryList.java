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

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;

public class GetCategoryList extends SmugmugMethod {
    public GetCategoryList(Session session) {
		super(session);
	}

	private GetCategoryListHandler handler = new GetCategoryListHandler(this);

    
	@Override
	public DefaultSmugmugHandler getResponseHandler() {
        return handler;
    }

    
	@Override
	public boolean execute() throws ProtocolException, CommunicationException {
       return super.execute();
    }

    
	@Override
	public SortedMap<String, String> getParams() {
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "smugmug.categories.get");  //$NON-NLS-1$//$NON-NLS-2$
        return params;
    }

    public List<SmugmugCategory> getCategories() {
        return handler.getCategories();
    }

    
	@Override
	public boolean isAuthorized() {
        return true;
    }
}
