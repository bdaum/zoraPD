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

package com.bdaum.juploadr.uploadapi.smugrest.authentification;

import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

public class SmugmugAuthEventDirector {
//	private static final String PERMS = "write"; //$NON-NLS-1$
//	private static final String LINK_BASE = "http://smugmug.com/services/auth/?api_key=" //$NON-NLS-1$
//			+ SmugmugRestApi.SMUGMUG_API_KEY + "&perms=" + PERMS; //$NON-NLS-1$
//	private static final String AUTHMESSAGE = Messages.SmugmugAuthEventDirector_your_authorization_is_required
//			+ Messages.SmugmugAuthEventDirector_you_must_be_connected;
//	public static final String SMUGMUG_SERVICES_AUTH = "smugmug.com/services/auth/"; //$NON-NLS-1$

//	private String frob;

	public SmugmugAuthEventDirector() {

	}

	public boolean execute(Session session) throws ProtocolException,
			CommunicationException {

		session.getAccount().setAuthenticated(true);
		new LoginWithPassword(session).execute();
		return true;

//
//		if (!info.isAuthenticated()) {
//
//			GetFrob getFrob = new GetFrob(info);
//			getFrob.execute();
//			frob = getFrob.getFrob();
//			info.addProperty("frob", frob); //$NON-NLS-1$
//			final String authLink = buildAuthLink();
//			if (!AuthDialog.show(authLink,
//					AUTHMESSAGE, SMUGMUG_SERVICES_AUTH, 850, 550))
//				return false;
//			GetToken getToken = new GetToken(frob, info);
//			getToken.execute();
//			// if we're here, everything is ok.
//			info.setToken(getToken.getToken());
//			info.setAuthenticated(getToken.getToken() != null);
//		}
//
//		if (info.isAuthenticated()) {
//			// sweet, we have a token, let's get the rest of the info...
//			GetUploadStatus gus = new GetUploadStatus(info);
//			gus.execute();
//		}
//		return true;
	}

//	private String buildAuthLink() {
//		StringBuffer link = new StringBuffer(LINK_BASE);
//		link.append("&frob="); //$NON-NLS-1$
//		link.append(frob);
//		link.append("&api_sig="); //$NON-NLS-1$
//		StringBuffer apiSig = new StringBuffer(
//				SmugmugRestApi.SMUGMUG_SHARED_SECRET);
//		apiSig.append("api_key"); //$NON-NLS-1$
//		apiSig.append(SmugmugRestApi.SMUGMUG_API_KEY);
//		apiSig.append("frob"); //$NON-NLS-1$
//		apiSig.append(frob);
//		apiSig.append("perms"); //$NON-NLS-1$
//		apiSig.append(PERMS);
//
//		link.append(StringSigner.md5(apiSig.toString()));
//		return link.toString();
//	}
}
