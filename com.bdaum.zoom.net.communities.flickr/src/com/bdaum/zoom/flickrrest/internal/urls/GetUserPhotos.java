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

package com.bdaum.zoom.flickrrest.internal.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.FlickrMethod;
import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Permission;

public class GetUserPhotos extends FlickrMethod {
	public GetUserPhotos(Session session) {
		super(session);
	}

	private URL url;

	@Override
	protected boolean doExecute(CommunityAccount account)
			throws FlickrException {
		account = setPermission(Permission.READ);
		String userPhotos = RestFlickrApi.FLICKR.getUrlsInterface()
				.getUserPhotos(account.getUsername());
		try {
			url = new URL(userPhotos);
		} catch (MalformedURLException e) {
			throw new FlickrException(Messages.GetUserPhotos_bad_user_photo_url);
		}
		return true;
	}

	public URL getURL() {
		return url;
	}

}
