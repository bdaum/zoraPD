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
 * (c) 2015-2018 Berthold Daum  
 */

package com.bdaum.zoom.flickrrest.internal.photosets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.FlickrMethod;
import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photosets.Photoset;

public class GetPhotosetList extends FlickrMethod {

	private List<PhotoSet> photosets;

	public GetPhotosetList(Session session) {
		super(session);
	}

	@Override
	protected boolean doExecute(CommunityAccount account) throws FlickrException {
		account = setPermission(Permission.READ);
		photosets = new ArrayList<PhotoSet>();
		for (Photoset ps : RestFlickrApi.FLICKR.getPhotosetsInterface().getList(session.getSessionId())
				.getPhotosets()) {
			FlickrPhotoSet photoSet = new FlickrPhotoSet();
			photoSet.setId(ps.getId());
			photoSet.setTitle(ps.getTitle());
			photoSet.setDescription(ps.getDescription());
			photoSet.setPrimaryPhotoId(ps.getPrimaryPhoto().getId());
			photoSet.setNumberOfPhotos(ps.getPhotoCount());
			photoSet.setUrl(ps.getUrl());
			photoSet.setSecret(ps.getSecret());
			photoSet.setServer(ps.getServer());
			photoSet.setNew(false);
			photosets.add(photoSet);
		}
		Collections.sort(photosets);
		return true;
	}

	public List<PhotoSet> getPhotoSets() {
		return photosets;
	}

}
