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

package com.bdaum.zoom.flickrrest.internal.authentication;

import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import com.bdaum.zoom.flickrrest.internal.RestFlickrApi;
import com.bdaum.zoom.flickrrest.internal.upload.GetUploadStatus;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.ui.AuthDialog;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;

public class FlickrAuthEventDirector {
	private static final String AUTHMESSAGE = Messages.FlickrAuthEventDirector_your_authorization_is_required
			+ Messages.FlickrAuthEventDirector_you_must_be_connected;
	private static final int MAXATTEMPTS = 5;
	protected int ret;

	public boolean execute(Session session) throws ProtocolException,
			CommunicationException {

		CommunityAccount account = session.getAccount();
		account.setAuthenticated(account.isAuthenticated()
				&& account.getToken() != null && account.getSecret() != null);
		boolean retry = false;
		while (true) {
			if (!account.isAuthenticated()) {
				AuthInterface authInterface = RestFlickrApi.FLICKR
						.getAuthInterface();
				Token token = authInterface.getRequestToken();
				String authLink = authInterface.getAuthorizationUrl(token,
						Permission.WRITE);
				String code = AuthDialog.show(authLink, AUTHMESSAGE, 850, 800,
						true);
				if (code == null)
					return false;
				int attempts = MAXATTEMPTS;
				while (true) {
					Token accessToken = authInterface.getAccessToken(token,
							new Verifier(code));
					try {
						authInterface.checkToken(accessToken);
						account.setToken(accessToken.getToken());
						account.setSecret(accessToken.getSecret());
						break;
					} catch (FlickrException e) {
						if (--attempts >= 0)
							continue;
						throw new CommunicationException(e);
					}
				}
			}
			account.setAuthenticated(account.getToken() != null
					&& account.getSecret() != null);
			if (account.isAuthenticated())
				try {
					new GetUploadStatus(session).execute();
					break;
				} catch (CommunicationException e) {
					if (retry)
						throw e;
					retry = true;
					account.setAuthenticated(false);
				}
		}
		return true;
	}

}
