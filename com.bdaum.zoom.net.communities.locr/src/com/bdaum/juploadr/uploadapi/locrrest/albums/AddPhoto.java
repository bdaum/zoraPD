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

package com.bdaum.juploadr.uploadapi.locrrest.albums;

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;

public class AddPhoto extends LocrMethod {

	private final PhotoSet toAddTo;
	private String photoId;
	private ImageAttributes image;

	public AddPhoto(PhotoSet toAddTo, ImageAttributes img, Session session) {
		super(session);
		this.toAddTo = toAddTo;
		this.image = img;
		this.photoId = img.getPhotoId();
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		 SortedMap<String, String> params = new TreeMap<String, String>();
	        params.put("method", "update_photo_xml.php?"); //$NON-NLS-1$ //$NON-NLS-2$
	        params.put("add_to_album_id", toAddTo.getId()); //$NON-NLS-1$
	        params.put("photo_id", photoId); //$NON-NLS-1$
	        return params;
	}

	
	@Override
	public DefaultLocrHandler getResponseHandler() {
		return new AddPhotoHandler(this);
	}

    
	@Override
	public boolean isAuthorized() {
        return true;
    }

    public String getErrorText() {
        return "add " + image.getTitle() + " to album " + toAddTo.getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
