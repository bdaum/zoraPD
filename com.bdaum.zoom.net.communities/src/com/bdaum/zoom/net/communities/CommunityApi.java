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
 * (c) 2012-2015 Berthold Daum  
 */
package com.bdaum.zoom.net.communities;

import org.eclipse.core.runtime.MultiStatus;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

public interface CommunityApi {

	/**
	 * Returns the friendly name of the upload site. Like "Flickr" or "Zooomr"
	 *
	 * @return the name of the site.
	 */
	String getSiteName();

	/**
	 * Do any initialization stuff that needs to be done. this method does not
	 * throw exceptions, as it's called when jUploadr is started, and we don't
	 * want to have it crash on startup. This is not guaranteed to be the first
	 * method called on your newly created object. In fact, authenticate() will
	 * probably be called first.
	 *
	 */
	void initialize(Session session);

	/**
	 * Authenticate with the new flickr auth api.
	 *
	 * @return true if OK was clicked, false if cancel was clicked
	 * @throws ProtocolException
	 * @throws CommunicationException
	 */
	boolean authenticate(Session session)
			throws ProtocolException, CommunicationException;

	/**
	 * Checks if the account is authenticated
	 *
	 * @param account
	 *            - community account object
	 * @return - true if authenticated
	 */
	boolean isAccountAuthenticated(CommunityAccount account);

	/**
	 * Decodes encoded password
	 *
	 * @param passwordHash
	 *            - encoded password
	 * @return - decoded password
	 */
	String decodePassword(String passwordHash);

	/**
	 * @param password
	 *            - password
	 * @return - encoded password
	 */
	String encodePassword(String password);

	/**
	 * Error handling
	 *
	 * @param e
	 *            - exception
	 */
	void handleCommunicationError(Exception e);

	/**
	 * @param handler
	 *            - sets an error handler
	 */
	void setErrorHandler(IErrorHandler handler);

	/**
	 * Logout from session
	 *
	 * @param sessionId
	 *            - session ID
	 * @throws ProtocolException
	 * @throws CommunicationException
	 */
	void logout(String sessionId) throws ProtocolException,
			CommunicationException;

	boolean isSupportsGeocoding();

	boolean isSetSubcategories();

	boolean isSetCategories();

	boolean isSinglePhotosets();

	boolean isSupportsSubcategories();

	boolean isSupportsCategories();

	boolean isSupportsPhotosets();

	boolean isSupportsTagging();

	Session getSession(CommunityAccount account)
			throws AuthException, CommunicationException;

	void setStatus(MultiStatus status);

	IErrorHandler getErrorHandler();

}