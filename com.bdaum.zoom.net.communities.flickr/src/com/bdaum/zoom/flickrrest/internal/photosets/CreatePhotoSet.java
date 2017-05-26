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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.flickrrest.internal.photosets;

import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.PostUploadAction;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.FlickrMethod;
import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Permission;

public class CreatePhotoSet extends FlickrMethod implements PostUploadAction {
	private PhotoSet photoSet;

	public CreatePhotoSet(PhotoSet photoSet, Session session) {
		super(session);
		this.photoSet = photoSet;
	}

	@Override
	protected boolean doExecute(CommunityAccount account)
			throws FlickrException {
		setPermission(Permission.WRITE);
		RestFlickrApi.FLICKR.getPhotosetsInterface().create(
				photoSet.getTitle(), photoSet.getDescription(),
				photoSet.getPrimaryPhotoId());
		return true;
	}

	public String getErrorText() {
		return "create photoset " + photoSet.getTitle(); //$NON-NLS-1$
	}

}
