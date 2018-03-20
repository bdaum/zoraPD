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

package com.bdaum.juploadr.uploadapi.smugrest.tags;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.app.tags.Tag;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugRestApi;


public class GetListUser extends SmugmugMethod {
    private List<Tag> tags;
    
    public GetListUser(Session session) {
    	super(session);
        this.tags = new LinkedList<Tag>();
    }
    
    
	@Override
	public DefaultSmugmugHandler getResponseHandler() {
        return new GetListUserHandler(this);
    }

    
	@Override
	public SortedMap<String, String> getParams() {
        SortedMap<String, String> params = new TreeMap<String, String>();
       	params.put("method","smugmug.tags.getListUser"); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("api_key",SmugmugRestApi.SMUGMUG_API_KEY); //$NON-NLS-1$
        return params;
    }

    
	@Override
	public boolean isAuthorized() {
        return true;
    }
    
    public void addTag(String tag) {
       tags.add(new Tag(tag)); 
    }
    
    public List<Tag> getTags() {
        return tags;
    }

}
