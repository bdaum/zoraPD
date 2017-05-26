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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.juploadr.uploadapi.locrrest;

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
import org.scohen.juploadr.app.photoset.PhotosetNotFoundException;
import org.scohen.juploadr.upload.FiletypeNotRecognizedException;
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

import com.bdaum.juploadr.uploadapi.locrrest.authentification.GetApiSignature;
import com.bdaum.zoom.net.communities.InvalidAuthTokenException;
import com.bdaum.zoom.net.communities.PhotoNotFoundException;
import com.bdaum.zoom.net.communities.ServiceUnavailableException;
import com.bdaum.zoom.net.communities.UserNotFoundException;
import com.bdaum.zoom.net.core.internal.Activator;

public abstract class LocrMethod implements Action {
	protected static final String URL = "http://de.locr.com/api/"; //$NON-NLS-1$
	protected HttpClient client;
	private URLCodec codec = new URLCodec();
	protected DefaultLocrHandler handler;
	private final Session session;

	// private static Log log = LogFactory.getLog(FlickrMethod.class);

	public LocrMethod(Session session) {
		this.session = session;
		client = HttpClientFactory.getHttpClient(session.getAccount());
	}

	public abstract DefaultLocrHandler getResponseHandler();

	public boolean execute() throws ProtocolException, CommunicationException {

		HttpMethodBase method = getMethod();

		boolean rv = false;
		try {
			int response = client.executeMethod(method);
			if (HttpStatus.SC_OK == response) {
				rv = parseResponse(method.getResponseBodyAsString());
				if (!rv) {
					throw defaultExceptionFor(handler.getErrorCode());
				}
			} else {
				throw new CommunicationException(Messages.getString(
						"juploadr.ui.error.bad.http.response", Activator.getStatusText(response))); //$NON-NLS-1$
			}
		} catch (InvalidAuthTokenException iat) {
			((RestLocrApi) session.getApi()).reauthAccount(session);
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

	private ProtocolException defaultExceptionFor(int errorCode) {
		ProtocolException ex = buildProtocolException(errorCode);
		if (ex == null) {
			// we still don't have an exception, this is very odd.
			ex = new ProtocolException();
		}
		return ex;
	}

	protected ProtocolException buildProtocolException(int errorCode) {
		switch (errorCode) {

		case 100:
			return new ServiceUnavailableException();
		case 101:
			return new AccessDeniedException();
		case 200:
			return new InvalidValueException();
		case 201:
			return new MissingParameterException();
		case 202:
			return new MissingValueException();
		case 203:
			return new ValueNotaNumberException();
		case 204:
			return new ValueOutOfRangeException();
		case 205:
			return new UserNotFoundException();
		case 206:
			return new InvalidAuthTokenException();
		case 207:
			return new PermissionDeniedException();
		case 208:
			return new ReadOnlyException();
		case 209:
			return new FiletypeNotRecognizedException();
		case 210:
			return new UploadLimitException();
		case 211:
			return new PhotoAlreadyExistsException();
		case 212:
			return new PhotoNotFoundException();
		case 213:
			return new AlbumAlreadyExistsException();
		case 214:
			return new NoMoreAlbumsAllowedException();
		case 215:
			return new PhotosetNotFoundException();
		default:
			return null;
		}
	}

	/**
	 * @param responseBodyAsString
	 * @return
	 * @throws AuthException
	 */
	public boolean parseResponse(String response) throws ProtocolException {
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			handler = getResponseHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(new StringReader(response)));
			if (!handler.isSuccessful()) {
				throw defaultExceptionFor(handler.getErrorCode());
			}
			return handler.isSuccessful();
		} catch (SAXException e) {
			throw new AuthException(
					Messages.getString("juploadr.ui.error.response.unreadable.noreason"), //$NON-NLS-1$
					e);
		} catch (IOException e) {
			// this can't happen
		}
		return false;
	}

	public String getQueryString(boolean signed) {
		StringBuffer queryString = new StringBuffer();
		Map<String, String> params = getParams();
		if (isAuthorized()) {
			authorize(params);
		}
		String method = params.remove("method"); //$NON-NLS-1$
		if (method != null)
			queryString.append(method);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			appendNVP(queryString, entry.getKey(), entry.getValue());
		}
		return queryString.toString();
	}

	private void authorize(Map<String, String> params) {
		if (isAuthorized()) {
			String signature = getApiSignature();
			String password = session.getAccount().getPasswordHash();
			StringBuffer sb = new StringBuffer();
			sb.append(password).append(signature);
			String auth_token = StringSigner.md5(sb.toString());
			// now iterate through all the params, and append them
			params.put("auth_user_name", session.getAccount().getName()); //$NON-NLS-1$
			params.put("api_signature", signature); //$NON-NLS-1$
			params.put("auth_token", auth_token); //$NON-NLS-1$
		}
	}

	protected String getApiSignature() {
		GetApiSignature getApiSignature = new GetApiSignature(session);
		try {
			getApiSignature.execute();
		} catch (ProtocolException e) {
			session.getApi().handleCommunicationError(e);
		} catch (CommunicationException e) {
			session.getApi().handleCommunicationError(e);
		}
		return getApiSignature.getApiSignature();
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
		StringBuffer query = new StringBuffer(URL);
		query.append(getQueryString(true));
		// log.info(query.toString());
		return new GetMethod(query.toString());
	}

	public Session getSession() {
		return session;
	}
}
