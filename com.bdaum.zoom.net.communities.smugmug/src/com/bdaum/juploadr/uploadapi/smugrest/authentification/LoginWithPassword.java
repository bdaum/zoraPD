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

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugRestApi;

public class LoginWithPassword extends SmugmugMethod {
	protected static final String PRO = "pro"; //$NON-NLS-1$
	protected static final String POWER = "power"; //$NON-NLS-1$
	protected static final String STANDARD = "standard"; //$NON-NLS-1$
	private static final String URL = "https://api.smugmug.com/hack/rest/1.2.0/"; //$NON-NLS-1$

	public LoginWithPassword(Session session) {
		super(session);
	}

	/**
	 * @return Returns the maxBandwidth.
	 */
	public long getMaxBandwidth() {
		return session.getAccount().getTrafficLimit();
	}

	/**
	 * @param maxBandwidth
	 *            The maxBandwidth to set.
	 */
//	public void setMaxBandwidth(long maxBandwidth) {
//		session.getAccount().setTrafficLimit(maxBandwidth);
//	}

	/**
	 * @return Returns the pro.
	 */
	public boolean isPro() {
		return PRO.equals(session.getAccount().getAccountType());
	}

	/**
	 * @param pro
	 *            The pro to set.
	 */
	public void setAccountType(String type) {
		session.getAccount().setAccountType(type);
		session.getAccount().setCanReplace(true);
		session.getAccount().setSupportsRaw(false); 
		session.getAccount().setUnlimited(true);
//		session.getAccount().setMaxFilesize(Integer.MAX_VALUE);
		session.getAccount().setTrafficLimit(Long.MAX_VALUE);
		session.getAccount().setCurrentUploadUsed(0);
	}

	/**
	 * @return Returns the usedBandwidth.
	 */
	public long getUsedBandwidth() {
		return session.getAccount().getCurrentUploadUsed();
	}

	/**
	 * @param usedBandwidth
	 *            The usedBandwidth to set.
	 */
//	public void setUsedBandwidth(long usedBandwidth) {
//		session.getAccount().setCurrentUploadUsed(usedBandwidth);
//	}

	public void setUsername(String username) {
		session.getAccount().setUsername(username);
		session.getAccount().setName(username);
	}

	public String getUsername() {
		return session.getAccount().getUsername();
	}

	
	@Override
	public DefaultSmugmugHandler getResponseHandler() {
		return new LoginWithPasswordHandler(this);
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("method", "smugmug.login.withPassword"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("APIKey", SmugmugRestApi.SMUGMUG_API_KEY); //$NON-NLS-1$
		map.put("EmailAddress", session.getAccount().getName()); //$NON-NLS-1$
		map.put("Password", session.getApi().decodePassword(session.getAccount().getPasswordHash())); //$NON-NLS-1$
		return map;
	}

	
	@Override
	public boolean isAuthorized() {
		return true;
	}

	
	@Override
	public boolean execute() throws ProtocolException, CommunicationException {
		boolean success = super.execute();
		session.getAccount().save();
		return success;
	}

	public void setSessionId(String id) {
		session.setSessionId(id);
	}

	public void setMaxFileSize(long s) {
		session.getAccount().setMaxFilesize(s);
	}
	
	
	@Override
	public String getUrl() {
		return URL;
	}

}
