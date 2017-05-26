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

package com.bdaum.zoom.flickrrest.internal.info;

import org.scohen.juploadr.uploadapi.ImageInfo;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.FlickrMethod;
import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.tags.Tag;

public class FlickrInfo extends FlickrMethod {

	private final String photoId;
	private FlickrImageInfo imageInfo = new FlickrImageInfo();

	public FlickrInfo(String photoId, Session session) {
		super(session);
		this.photoId = photoId;
	}

	@Override
	protected boolean doExecute(CommunityAccount account)
			throws FlickrException {
		account = setPermission(Permission.READ);
		Photo info = RestFlickrApi.FLICKR.getPhotosInterface().getInfo(photoId,
				null);
		imageInfo.setPublic(info.isPublicFlag());
		imageInfo.setFamily(info.isFamilyFlag());
		imageInfo.setFriends(info.isFriendFlag());
		for (Tag tag : info.getTags())
			imageInfo.addTag(tag.getValue());
		imageInfo.setTitle(info.getTitle());
		imageInfo.setDescription(info.getDescription());
		for (String url : info.getUrls())
			imageInfo.addUrl(url);
		return true;
	}

	public ImageInfo getImageInfo() {
		return imageInfo;
	}

}
