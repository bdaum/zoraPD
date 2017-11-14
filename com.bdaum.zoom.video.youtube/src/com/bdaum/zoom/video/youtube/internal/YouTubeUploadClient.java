/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bdaum.zoom.video.youtube.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.google.gdata.client.media.ResumableGDataFileUploader;
import com.google.gdata.client.uploader.ProgressListener;
import com.google.gdata.client.uploader.ResumableHttpFileUploader;
import com.google.gdata.client.uploader.ResumableHttpFileUploader.ResponseMessage;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaCopyright;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Demonstrates YouTube Data API operation to upload large media files.
 *
 *
 */
public class YouTubeUploadClient implements CommunityApi {

	/**
	 * The URL used to resumable upload
	 */
	/** Time interval at which upload task will notify about the progress */
	private static final int PROGRESS_UPDATE_INTERVAL = 1000;

	/** Max size for each upload chunk */
	private static final int DEFAULT_CHUNK_SIZE = 10000000;

	protected static final Object ATOMNS = "http://www.w3.org/2005/Atom"; //$NON-NLS-1$

	protected static final Object CATEGORY = "category"; //$NON-NLS-1$

	protected static final String TERM = "term"; //$NON-NLS-1$

	protected static final String LABEL = "label"; //$NON-NLS-1$

	private YouTubeService service;

	private String siteName;

	private boolean auth;

	private IErrorHandler handler;

	private String videoId;

	/**
	 * A {@link ProgressListener} implementation to track upload progress. The
	 * listener can track multiple uploads at the same time.
	 */
	private class FileUploadProgressListener implements ProgressListener {

		private final IProgressMonitor monitor;
		private int worked = 0;

		public FileUploadProgressListener(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public synchronized void progressChanged(ResumableHttpFileUploader uploader) {
			switch (uploader.getUploadState()) {
			case COMPLETE:
				monitor.worked(100 - worked);
				return;
			case IN_PROGRESS:
				int progress = (int) (uploader.getProgress() + 0.5d);
				monitor.worked(progress);
				worked += progress;
				return;
			default:
				return;
			}
		}
	}

	public YouTubeUploadClient() {
		super();
		service = new YouTubeService("org.photozora-" + Constants.APPNAME + '-' //$NON-NLS-1$
				+ Activator.getDefault().getBundle().getVersion(), YtConstants.DEVELOPERKEY);
	}

	public String uploadVideo(IProgressMonitor monitor, Session session, URI uri, String mimeType, String videoTitle,
			Date creationDate, String[] keywords, String category, String copyright, double latitude, double longitude,
			String location, boolean makePrivate) throws IOException, ServiceException, InterruptedException {
		videoId = null;
		File videoFile = new File(uri);
		if (!videoFile.exists())
			throw new FileNotFoundException(videoFile.toString());

		VideoEntry newEntry = new VideoEntry();
		YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
		mg.setTitle(new MediaTitle());
		mg.getTitle().setPlainTextContent(videoTitle);
		mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, category));
		mg.setCopyright(new MediaCopyright());
		mg.getCopyright().setContent(copyright == null ? "" : copyright); //$NON-NLS-1$
		mg.setPrivate(makePrivate);
		MediaKeywords mediaKeywords = new MediaKeywords();
		mg.setKeywords(mediaKeywords);
		if (keywords != null)
			for (String kw : keywords)
				mediaKeywords.addKeyword(kw);
		mg.setDescription(new MediaDescription());
		mg.getDescription().setPlainTextContent(videoTitle);
		mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, Constants.APPNAME));
		if (!Double.isNaN(latitude) && !Double.isNaN(longitude))
			newEntry.setGeoCoordinates(new GeoRssWhere(latitude, longitude));
		else if (location != null && location.length() > 0)
			newEntry.setLocation(location);
		if (creationDate != null) {
			DateTime dateTime = new DateTime(creationDate);
			dateTime.setDateOnly(true);
			newEntry.setRecorded(dateTime);
		}
		MediaFileSource ms = new MediaFileSource(videoFile, mimeType);
		newEntry.setMediaSource(ms);

		// String uploadUrl =
		// "http://uploads.gdata.youtube.com/feeds/api/users/default/uploads";
		// VideoEntry createdEntry = service.insert(new URL(uploadUrl),
		// newEntry);
		// if (createdEntry.isDraft()) {
		// System.out.println("Video is not live");
		// YtPublicationState pubState = createdEntry.getPublicationState();
		// if (pubState.getState() == YtPublicationState.State.PROCESSING) {
		// System.out.println("Video is still being processed.");
		// } else if (pubState.getState() == YtPublicationState.State.REJECTED)
		// {
		// System.out.print("Video has been rejected because: ");
		// System.out.println(pubState.getDescription());
		// System.out.print("For help visit: ");
		// System.out.println(pubState.getHelpUrl());
		// } else if (pubState.getState() == YtPublicationState.State.FAILED) {
		// System.out.print("Video failed uploading because: ");
		// System.out.println(pubState.getDescription());
		// System.out.print("For help visit: ");
		// System.out.println(pubState.getHelpUrl());
		// }
		// }
		// return null;

		FileUploadProgressListener listener = new FileUploadProgressListener(monitor);
		ResumableGDataFileUploader uploader = new ResumableGDataFileUploader.Builder(service,
				new URL(YtConstants.RESUMABLE_UPLOAD_URL), ms, newEntry).title(videoTitle)
						.trackProgress(listener, PROGRESS_UPDATE_INTERVAL).chunkSize(DEFAULT_CHUNK_SIZE).build();

		uploader.start();
		while (!uploader.isDone()) {
			Thread.sleep(PROGRESS_UPDATE_INTERVAL);
		}
		ResponseMessage response = uploader.getResponse();
		try {
			if (response != null) {
				String receiveMessage = response.receiveMessage(3000);
				if (receiveMessage != null) {
					// <id>tag:youtube.com,2008:video:Zd_PLRmRNA0</id>
					int p = receiveMessage.indexOf("<id>") + 4; //$NON-NLS-1$
					if (p >= 4) {
						int q = receiveMessage.indexOf("</id>", p); //$NON-NLS-1$
						if (q > p) {
							int r = receiveMessage.indexOf("video:", p); //$NON-NLS-1$
							if (r >= p && r < q)
								videoId = receiveMessage.substring(r + 6, q).trim();
						}
					}
				}
			}
		} catch (ExecutionException e) {
			return Messages.YouTubeUploadClient_execution_error;
		} catch (TimeoutException e) {
			return Messages.YouTubeUploadClient_timeout;
		}
		switch (uploader.getUploadState()) {
		case COMPLETE:
			return null;
		case CLIENT_ERROR:
			return Messages.YouTubeUploadClient_upload_failed;
		default:
			return Messages.YouTubeUploadClient_unexpected_upload_status;
		}
	}

	public void initialize(Session session) {
		IConfigurationElement conf = (IConfigurationElement) session.getAccount().getConfiguration().getParent();
		siteName = conf.getAttribute("name"); //$NON-NLS-1$
	}

	public String getSiteName() {
		return siteName;
	}

	public boolean authenticate(Session session) throws ProtocolException, CommunicationException {
		if (service != null) {
			CommunityAccount account = session.getAccount();
			String passwordHash = account.getPasswordHash();
			String password = decodePassword(passwordHash);
			try {
				service.setUserCredentials(account.getName(), password);
				auth = true;
				return true;
			} catch (AuthenticationException e) {
				// return false
			}
		}
		return false;
	}

	public boolean isAccountAuthenticated(CommunityAccount account) {
		return auth;
	}

	public String decodePassword(String encrypted) {
		if (encrypted == null)
			return ""; //$NON-NLS-1$
		char[] charArray = encrypted.toCharArray();
		int o = 7;
		for (int i = 0; i < charArray.length; i++) {
			charArray[i] -= (o % 31);
			o *= 37;
		}
		return new String(charArray);
	}

	public String encodePassword(String text) {
		if (text == null)
			return ""; //$NON-NLS-1$
		char[] charArray = text.toCharArray();
		int o = 7;
		for (int i = 0; i < charArray.length; i++) {
			charArray[i] += (o % 31);
			o *= 37;
		}
		return new String(charArray);
	}

	public void handleCommunicationError(Exception e) {
		if (e instanceof ProtocolException)
			Activator.getDefault().logError(Messages.YouTubeUploadClient_internal_protocol_error, e);
		else if (handler != null)
			handler.handleError(this, e);
		else
			Activator.getDefault().logError(Messages.YouTubeUploadClient_communication_error, e);
	}

	public void setErrorHandler(IErrorHandler handler) {
		this.handler = handler;
	}

	public void logout(String sessionId) throws ProtocolException, CommunicationException {
		// do nothing
	}

	public boolean isSupportsGeocoding() {
		return false;
	}

	public boolean isSetSubcategories() {
		return false;
	}

	public boolean isSetCategories() {
		return false;
	}

	public boolean isSinglePhotosets() {
		return false;
	}

	public boolean isSupportsSubcategories() {
		return false;
	}

	public boolean isSupportsCategories() {
		return false;
	}

	public boolean isSupportsPhotosets() {
		return false;
	}

	public boolean isSupportsTagging() {
		return false;
	}

	/**
	 * @return videoId
	 */
	public String getVideoId() {
		return videoId;
	}

	public Map<String, String> getCategories() throws IOException {
		final Map<String, String> cats = new HashMap<String, String>(50);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				@Override
				public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
						throws SAXException {
					if (CATEGORY.equals(localName) && ATOMNS.equals(namespaceURI)) {
						String term = atts.getValue(TERM);
						String label = atts.getValue(LABEL);
						cats.put(term, label);
					}
				}
			};
			try (InputStream in = new URL(YtConstants.YT_CATEGORIES).openStream()) {
				saxParser.parse(in, handler);
			}
		} catch (ParserConfigurationException e) {
			throw new IOException(e.toString());
		} catch (SAXException e) {
			throw new IOException(e.toString());
		}
		return cats;
	}

	public Session getSession(CommunityAccount account) throws AuthException, CommunicationException {
		return new Session(this, account);
	}

	public void setStatus(MultiStatus status) {
		// do nothing
	}

	public IErrorHandler getErrorHandler() {
		return handler;
	}

}
