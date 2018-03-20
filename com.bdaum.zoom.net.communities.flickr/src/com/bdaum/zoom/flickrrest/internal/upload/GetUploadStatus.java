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
 * (c) 2015 Berthold Daum  
 */

package com.bdaum.zoom.flickrrest.internal.upload;

import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.FlickrMethod;
import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.people.User;

public class GetUploadStatus extends FlickrMethod {
	private static final String PRO = "pro"; //$NON-NLS-1$
	private static final String FREE = "free"; //$NON-NLS-1$

	public GetUploadStatus(Session session) {
		super(session);
	}

	@Override
	protected boolean doExecute(CommunityAccount account)
			throws FlickrException {
		account = setPermission(Permission.READ);
		User user = RestFlickrApi.FLICKR
				.getPeopleInterface().getUploadStatus();
		boolean pro = user.isPro();
		account.setAccountType(pro ? PRO : FREE);
		account.setCanReplace(pro);
		account.setSupportsRaw(false);
		account.setUnlimited(false);
		account.setTrafficLimit(user.getBandwidthMax());
		account.setUnlimited(user.isBandwidthUnlimited());
		account.setCurrentUploadUsed(user.getBandwidthUsed());
		account.setUsername(user.getUsername());
		account.setName(user.getUsername());
		account.setMaxFilesize(user.getFilesizeMax());
		try {
			account.setMaxVideosize(Long.parseLong(user.getVideosSizeMax()));
		} catch (NumberFormatException e) {
			account.setMaxFilesize(-1L);
		}
		return true;
	}

}
