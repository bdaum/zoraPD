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

package com.bdaum.juploadr.uploadapi.smugrest.upload;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.scohen.juploadr.Messages;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.UploadImage;
import org.scohen.juploadr.event.ImageUploadResponse;
import org.scohen.juploadr.event.UploadCompleteEvent;
import org.scohen.juploadr.event.UploadEvent;
import org.scohen.juploadr.event.UploadStatusMonitor;
import org.scohen.juploadr.upload.HttpClientFactory;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.scohen.juploadr.uploadapi.UploadFailedException;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;

/**
 * @author bdaum
 */
public class SmugmugUpload extends SmugmugMethod {
	private static final String X_SMUG_FILE_NAME = "X-Smug-FileName"; //$NON-NLS-1$
	private static final String URL = "http://upload.smugmug.com/"; //$NON-NLS-1$
	public static final int STATUS_OK = 1;
	public static final int STATUS_FAILED = 0;

	private ImageAttributes image;
	private UploadStatusMonitor monitor;
	private SmugmugUploadResponseHandler handler;
	private final boolean replace;

	public SmugmugUpload(ImageAttributes img, Session session, boolean replace,
			UploadStatusMonitor monitor) {
		super(session);
		this.image = img;
		this.replace = replace;
		this.monitor = monitor;

		handler = new SmugmugUploadResponseHandler(this);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);

	}


	@Override
	public boolean execute() throws ProtocolException, CommunicationException {
		HttpClient client = HttpClientFactory.getHttpClient(session
				.getAccount());
		this.monitor.uploadStarted(new UploadEvent(image, 0, true, false));
		SortedMap<String, String> params = getParams();
		String name = params.get(X_SMUG_FILE_NAME);
		PutMethod put = new PutMethod(URL + name);
		for (Map.Entry<String, String> entry : params.entrySet())
			put.addRequestHeader(entry.getKey(), entry.getValue());
		File file = new File(image.getImagePath());
		Asset asset = image.getAsset();
		FileRequestEntity entity = new FileRequestEntity(file, asset
				.getMimeType());
		put.setRequestEntity(entity);
		try {
			int status = client.executeMethod(put);
			if (status == HttpStatus.SC_OK) {
				// deal with the response
				try {
					String response = put.getResponseBodyAsString();
					put.releaseConnection();
					boolean success = parseResponse(response);
					if (success) {
						image.setState(UploadImage.STATE_UPLOADED);
						ImageUploadResponse resp = new ImageUploadResponse(
								handler.getPhotoID(), handler.getKey(), handler
										.getUrl());
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


	@Override
	public DefaultSmugmugHandler getResponseHandler() {
		return handler;
	}


	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("X-Smug-SessionID", session.getSessionId()); //$NON-NLS-1$
		map.put("X-Smug-Version", "1.2.0"); //$NON-NLS-1$  //$NON-NLS-2$
		map.put("X-Smug-ResponseType", "REST"); //$NON-NLS-1$ //$NON-NLS-2$
		PhotoSet album = session.matchAlbum(image);
		if (replace)
			map.put("X-Smug-ImageID", image.getPhotoId()); //$NON-NLS-1$
		else
			map.put("X-Smug-AlbumID", album.getId()); //$NON-NLS-1$
		Asset asset = image.getAsset();
		map.put(X_SMUG_FILE_NAME, Core.getFileName(asset.getUri(), true));
		map.put("X-Smug-Caption", image.getTitle()); //$NON-NLS-1$
		String keywords = ((ImageUploadApi) session.getApi()).getTagParser().toRequestString(
				image.getTags());
		map.put("X-Smug-Keywords", keywords); //$NON-NLS-1$
		double gpsLatitude = asset.getGPSLatitude();
		if (!Double.isNaN(gpsLatitude))
			map.put("X-Smug-Latitude", String.valueOf(gpsLatitude)); //$NON-NLS-1$
		double gpsLongitude = asset.getGPSLongitude();
		if (!Double.isNaN(gpsLongitude))
			map.put("X-Smug-Longitude", String.valueOf(gpsLongitude)); //$NON-NLS-1$
		double gpsAltitude = asset.getGPSAltitude();
		if (!Double.isNaN(gpsAltitude))
			map.put("X-Smug-Altitude", String.valueOf(gpsAltitude)); //$NON-NLS-1$
		if (asset.getSafety() == QueryField.SAFETY_RESTRICTED)
			map.put("X-Smug-Hidden", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		return map;
	}


	@Override
	public boolean isAuthorized() {
		return false;
	}

}
