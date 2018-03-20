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
 * Modifications (c) 2009 Berthold Daum  
 */

package org.scohen.juploadr.uploadapi;

import org.eclipse.core.runtime.Assert;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;

import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.net.communities.Messages;

public class Session {
	private String sessionId;
	private CommunityAccount account;
	private final CommunityApi api;
	private PhotoSet defaultAlbum;
	private int albumPolicy = -1;
	private Object accessToken;

	public Session(CommunityApi api, CommunityAccount account) {
		Assert.isNotNull(account);
		this.api = api;
		this.account = account;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public CommunityAccount getAccount() {
		return account;
	}

	public void setAccount(CommunityAccount account) {
		this.account = account;
	}

	public void init() throws CommunicationException, AuthException {
		if (!account.isNullAccount() && api != null) {
			authenticate();
			if (account.isAuthenticated())
				api.initialize(this);
		}
	}

	private void authenticate() throws CommunicationException, AuthException {
		try {
			api.authenticate(this);
		} catch (AuthException e) {
			throw e;
		} catch (ProtocolException e) {
			CommunitiesActivator.getDefault().logError(
					Messages.CommunityAccount_internal_protocol_error, e);
		}
	}

	public void close() {
		if (sessionId != null)
			try {
				api.setErrorHandler(null);
				api.logout(sessionId);
				sessionId = null;
			} catch (ProtocolException e) {
				CommunitiesActivator.getDefault().logError(
						Messages.CommunityAccount_internal_protocol_error, e);
			} catch (CommunicationException e) {
				// lost connection - session closed
			}
	}

	public CommunityApi getApi() {
		return api;
	}

	public String getPassword() {
		return api == null ? null : api.decodePassword(account.getPasswordHash());
	}

	public void setAlbumPolicy(int albumPolicy) {
		this.albumPolicy = albumPolicy;
	}

	public void setDefaultAlbum(PhotoSet defaultAlbum) {
		this.defaultAlbum = defaultAlbum;
	}

	public PhotoSet getDefaultAlbum() {
		return defaultAlbum;
	}

	public int getAlbumPolicy() {
		return albumPolicy;
	}

	public PhotoSet matchAlbum(ImageAttributes image) {
		PhotoSet found = null;
		int mx = -1;
		for (PhotoSet photoSet : account.getPhotosets()) {
			int score = photoSet.match(image, defaultAlbum != null && !defaultAlbum.isPublicAlbum());
			if (score > mx) {
				mx = score;
				found = photoSet;
			}
		}
		return found == null ? defaultAlbum : found;
	}

	/**
	 * @return accessToken
	 */
	public Object getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken das zu setzende Objekt accessToken
	 */
	public void setAccessToken(Object accessToken) {
		this.accessToken = accessToken;
	}

}
