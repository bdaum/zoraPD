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

package com.bdaum.juploadr.uploadapi.smugrest.photosets;

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.PostUploadAction;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;

public class CreatePhotoSet extends SmugmugMethod implements PostUploadAction {
    private PhotoSet photoSet;

    public CreatePhotoSet(PhotoSet photoSet, Session session) {
    	super(session);
        this.photoSet = photoSet;
    }

    
	@Override
	public DefaultSmugmugHandler getResponseHandler() {
        return new CreatePhotoSetResponseHandler(photoSet);
    }

    
	@Override
	public SortedMap<String, String> getParams() {
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "smugmug.albums.create"); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("Title", photoSet.getTitle()); //$NON-NLS-1$
        params.put("CategoryID", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        if (photoSet.getDescription() != null
                && photoSet.getDescription().length() > 0) {
            params.put("Description", photoSet.getDescription()); //$NON-NLS-1$
        }
        params.put("SquareThumbs", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!photoSet.isPublicAlbum())
        	params.put("Public", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        return params;
    }

    
	@Override
	public boolean isAuthorized() {
        return true;
    }

    
	public String getErrorText() {
        return "create photoset " + photoSet.getTitle(); //$NON-NLS-1$
    }

}
