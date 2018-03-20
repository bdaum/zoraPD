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
 * Modifications (c) 2009 Berthold Daum  
 */

package com.bdaum.juploadr.uploadapi.smugrest;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.scohen.juploadr.Messages;
import org.scohen.juploadr.app.StringSigner;
import org.scohen.juploadr.upload.HttpClientFactory;
import org.scohen.juploadr.uploadapi.Action;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bdaum.zoom.net.core.internal.Activator;

public abstract class SmugmugMethod implements Action {
	private static final String URL = "http://api.smugmug.com/hack/rest/1.2.0/"; //$NON-NLS-1$
	protected HttpClient client;
	private URLCodec codec = new URLCodec();
	private DefaultSmugmugHandler handler;
	protected final Session session;

	public SmugmugMethod(Session session) {
		this.session = session;
		client = HttpClientFactory.getHttpClient(session.getAccount());
	}

	public abstract DefaultSmugmugHandler getResponseHandler();

	public boolean execute() throws ProtocolException, CommunicationException {

		HttpMethodBase method = getMethod();

		boolean rv = false;
		try {
			int response = client.executeMethod(method);
			if (HttpStatus.SC_OK == response) {
				rv = parseResponse(method.getResponseBodyAsString());
			} else {
				throw new CommunicationException(Messages.getString(
						"juploadr.ui.error.bad.http.response", Activator.getStatusText(response))); //$NON-NLS-1$
			}
		} catch (HttpException e) {
			throw new CommunicationException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage(), e);
		} finally {
			method.releaseConnection();
		}
		return rv;

	}

	public int getErrorCode() {
		return handler.getErrorCode();
	}

	/**
	 * @param responseBodyAsString
	 * @return
	 * @throws AuthException
	 */
	public boolean parseResponse(String response) throws ProtocolException {
		try {
			// System.setProperty("org.xml.sax.driver",
			// "com.bluecast.xml.Piccolo");
			// log.info(response);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			handler = getResponseHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(new StringReader(response)));
			if (!handler.isSuccessful()) {
				throw new ProtocolException(handler.getFailureReason());
			}
			return handler.isSuccessful();
		} catch (SAXException e) {
			throw new AuthException(
					Messages.getString("juploadr.ui.error.response.unreadable.noreason"), e); //$NON-NLS-1$
		} catch (IOException e) {
			// this can't happen
		}
		return false;
	}

	public final String getSignature() {
		StringBuffer sb = new StringBuffer(SmugmugRestApi.SMUGMUG_SHARED_SECRET);
		// now iterate through all the params, and append them
		Map<String, String> params = getParams();
		if (isAuthorized()) {
			String sessionId = session.getSessionId();
			if (sessionId != null) {
				params.put("SessionID", sessionId); //$NON-NLS-1$
			}
		}
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey()).append(entry.getValue());
		}
		return StringSigner.md5(sb.toString());
	}

	public String getQueryString(boolean signed) {
		StringBuffer queryString = new StringBuffer();
		Map<String, String> params = getParams();
		if (isAuthorized()) {
			String sessionId = session.getSessionId();
			if (sessionId != null) {
				params.put("SessionID", sessionId); //$NON-NLS-1$
			}
			//			params.put("auth_token", session.getAccount().getToken()); //$NON-NLS-1$
		}
		for (Map.Entry<String, String> entry : params.entrySet()) {
			appendNVP(queryString, entry.getKey(), entry.getValue());
		}

		if (signed) {
			appendNVP(queryString, "api_sig", getSignature()); //$NON-NLS-1$
		}

		return queryString.toString();
	}

	private void appendNVP(StringBuffer url, String name, String value) {
		if (url.length() == 0) {
			url.append("?"); //$NON-NLS-1$
		}
		char lastChar = url.charAt(url.length() - 1);
		if (lastChar != '&' && lastChar != '?') {
			url.append("&"); //$NON-NLS-1$
		}
		url.append(name);
		url.append("="); //$NON-NLS-1$
		url.append(encode(value));
	}

	protected String encode(String toEnc) {
		try {
			return codec.encode(toEnc);
		} catch (EncoderException e) {
			// do nothing
		}
		return null;
	}

	public abstract SortedMap<String, String> getParams();

	public abstract boolean isAuthorized();

	public HttpMethodBase getMethod() {
		StringBuffer query = new StringBuffer(getUrl());
		query.append(getQueryString(true));
		// log.info(query.toString());
		GetMethod get = new GetMethod(query.toString());

		return get;
	}

	public String getUrl() {
		return URL;
	}
}
