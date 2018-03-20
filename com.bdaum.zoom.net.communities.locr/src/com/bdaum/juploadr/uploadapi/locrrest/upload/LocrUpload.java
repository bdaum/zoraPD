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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.juploadr.uploadapi.locrrest.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.scohen.juploadr.Messages;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.StringSigner;
import org.scohen.juploadr.app.UploadImage;
import org.scohen.juploadr.app.geo.GeoLocation;
import org.scohen.juploadr.event.ImageUploadResponse;
import org.scohen.juploadr.event.MonitorFilePart;
import org.scohen.juploadr.event.UploadCompleteEvent;
import org.scohen.juploadr.event.UploadEvent;
import org.scohen.juploadr.event.UploadStatusMonitor;
import org.scohen.juploadr.upload.HttpClientFactory;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.scohen.juploadr.uploadapi.UploadFailedException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;
import com.bdaum.zoom.core.Core;

public class LocrUpload extends LocrMethod {

	public static final int STATUS_OK = 1;
	public static final int STATUS_FAILED = 0;

	private static final String POSTURL = "http://de.locr.com/api/post_photo_xml.php"; //$NON-NLS-1$
	private ImageAttributes image;
	private UploadStatusMonitor monitor;
	private SortedMap<String, String> params = new TreeMap<String, String>();

	public LocrUpload(ImageAttributes img, Session session,
			UploadStatusMonitor monitor) {
		super(session);
		this.image = img;
		this.monitor = monitor;

		handler = new LocrUploadResponseHandler(this);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
	}


	@Override
	public boolean execute() throws ProtocolException, CommunicationException {
		HttpClient httpClient = HttpClientFactory.getHttpClient(getSession().getAccount());

		// checkAuthorization(client);
		this.monitor.uploadStarted(new UploadEvent(image, 0, true, false));

		PostMethod post = new PostMethod(POSTURL);
		List<Part> parts = getParts();
		MultipartRequestEntity entity = new MultipartRequestEntity(parts
				.toArray(new Part[parts.size()]), post.getParams());
		post.setRequestEntity(entity);

		try {

			int status = httpClient.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				// deal with the response
				try {
					String response = post.getResponseBodyAsString();
					post.releaseConnection();
					boolean success = parseResponse(response);
					if (success) {
						image.setState(UploadImage.STATE_UPLOADED);
						ImageUploadResponse resp = new ImageUploadResponse(
								((LocrUploadResponseHandler) handler)
										.getPhotoID());
						this.monitor.uploadFinished(new UploadCompleteEvent(
								resp, image));

					} else {
						throw new UploadFailedException(Messages
								.getString("juploadr.ui.error.status")); //$NON-NLS-1$
					}

				} catch (IOException e) {
					// TODO: Is it safe to assume the upload failed here?
					this.fail(Messages
							.getString("juploadr.ui.error.response.unreadable") //$NON-NLS-1$
							+ e.getMessage(), e);
				}
			} else {
				this.fail(Messages.getString(
						"juploadr.ui.error.bad.http.response", status), null); //$NON-NLS-1$
			}
		} catch (ConnectException ce) {
			this
					.fail(
							Messages
									.getString("juploadr.ui.error.unable.to.connect"), ce); //$NON-NLS-1$
		} catch (NoRouteToHostException route) {
			this.fail(
					Messages.getString("juploadr.ui.error.no.internet"), route); //$NON-NLS-1$
		} catch (UnknownHostException uhe) {
			this
					.fail(
							Messages
									.getString("juploadr.ui.error.unknown.host"), uhe); //$NON-NLS-1$

		} catch (HttpException e) {
			this
					.fail(
							Messages
									.getString("juploadr.ui.error.http.exception") + e, e); //$NON-NLS-1$
		} catch (IOException e) {
			this
					.fail(
							Messages
									.getString("juploadr.ui.error.simple.ioexception") + e.getMessage() + "" //$NON-NLS-1$ //$NON-NLS-2$
									+ e, e);
		}
		return true;
	}

	/**
	 * Notifies the monitor of failure and throws UploadFailedException.
	 *
	 * @param message
	 * @param cause
	 * @throws UploadFailedException
	 */
	private void fail(String message, Throwable cause)
			throws CommunicationException {
		this.monitor.uploadFailed(new UploadEvent(image, 0, true, false,
				message));
		if (cause != null) {
			throw new CommunicationException(message, cause);
		}
		throw new CommunicationException(message);
	}

	private List<Part> getParts() throws UploadFailedException {
		LinkedList<Part> parts = new LinkedList<Part>();
		try {
			String signature = getApiSignature();
			String password = getSession().getAccount().getPasswordHash();
			StringBuffer sb = new StringBuffer();
			sb.append(password).append(signature);
			String auth_token = StringSigner.md5(sb.toString());

			addNonNullStringPart(parts, "auth_user_name", getSession().getAccount().getName()); //$NON-NLS-1$
			addNonNullStringPart(parts, "api_signature", signature); //$NON-NLS-1$
			addNonNullStringPart(parts, "auth_token", auth_token); //$NON-NLS-1$
			addNonNullStringPart(parts, "caption", image.getTitle()); //$NON-NLS-1$
			addNonNullStringPart(
					parts,
					"tags", ((ImageUploadApi) getSession().getApi()).getTagParser().toRequestString(image.getTags())); //$NON-NLS-1$
			addNonNullStringPart(parts, "description", image.getDescription()); //$NON-NLS-1$
			GeoLocation location = image.getLocation();
			if (location != null) {
				addNonNullStringPart(parts,
						"latitude", String.valueOf(location.getLatitude())); //$NON-NLS-1$
				addNonNullStringPart(parts,
						"longitude", String.valueOf(location.getLongitude())); //$NON-NLS-1$
			}
			GeoLocation objectLocation = image.getObjectLocation();
			if (objectLocation != null) {
				addNonNullStringPart(
						parts,
						"object_latitude", String.valueOf(objectLocation.getLatitude())); //$NON-NLS-1$
				addNonNullStringPart(
						parts,
						"object_longitude", String.valueOf(objectLocation.getLongitude())); //$NON-NLS-1$
				if (location != null) {
					double bearing = Core.bearing(location.getLatitude(),
							location.getLongitude(), objectLocation
									.getLatitude(), objectLocation
									.getLongitude());
					addNonNullStringPart(parts,
							"direction", String.valueOf(bearing)); //$NON-NLS-1$
				}
			}

			int privacy = 0;
			if (!image.isPubliclyVisible()) {
				if (image.isFriendViewable())
					privacy = 500;
				else if (image.isFamilyViewable())
					privacy = 800;
				else
					privacy = 1000;
			}
			addNonNullStringPart(parts, "privacy", String.valueOf(privacy)); //$NON-NLS-1$
			addNonNullStringPart(parts, "photo_conv_bg", "true"); //$NON-NLS-1$ //$NON-NLS-2$

			MonitorFilePart file = new MonitorFilePart("file", new File(image //$NON-NLS-1$
					.getImagePath()), image);
			file.addStateChangeListener(monitor);
			parts.add(file);

		} catch (FileNotFoundException e) {
			throw new UploadFailedException(Messages.getString(
					"juploadr.ui.error.file.notfound", image.getImagePath())); //$NON-NLS-1$
		}

		return parts;
	}

	/**
	 * @param parts
	 * @param string
	 * @param title
	 */
	private void addNonNullStringPart(List<Part> parts, String paramName,
			String paramValue) {
		if (paramValue != null) {
			StringPart part = new StringPart(paramName, paramValue);
			part.setCharSet("UTF-8"); //$NON-NLS-1$
			parts.add(part);
			params.put(paramName, paramValue);
		}
	}


	@Override
	public DefaultLocrHandler getResponseHandler() {
		return handler;
	}


	@Override
	public SortedMap<String, String> getParams() {
		return params;
	}


	@Override
	public boolean isAuthorized() {
		return false;
	}

}
