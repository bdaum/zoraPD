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

package com.bdaum.zoom.flickrrest.internal;

import org.scohen.juploadr.uploadapi.Action;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.net.communities.CommunityAccount;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;

public abstract class FlickrMethod implements Action {
	protected final Session session;
	private String errorCode;
	private String errorMessage;

	public FlickrMethod(Session session) {
		this.session = session;
	}

	protected CommunityAccount setPermission(Permission perm) {
		CommunityAccount account = session.getAccount();
		Auth auth = new Auth();
		auth.setPermission(perm);
		auth.setToken(account.getToken());
		auth.setTokenSecret(account.getSecret());
		RequestContext.getRequestContext().setAuth(auth);
		return account;
	}

	public boolean execute() throws ProtocolException, CommunicationException {
		try {
			return doExecute(session.getAccount());
		} catch (FlickrException e) {
			errorCode = e.getErrorCode();
			errorMessage = e.getErrorMessage();
			throw new CommunicationException(e);
		}
	}

	protected abstract boolean doExecute(CommunityAccount account) throws FlickrException;

	/**
	 * @return errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @return errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
