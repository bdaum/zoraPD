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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

/**
 * Demonstrates YouTube Data API operation to upload large media files.
 *
 *
 */
public class YouTubeUploadClient implements CommunityApi {

	/**
	 * Define a global instance of a Youtube object, which will be used to make
	 * YouTube Data API requests.
	 */
	private static YouTube youtube;

	/**
	 * Define a global variable that specifies the MIME type of the video being
	 * uploaded.
	 */
	private static final String VIDEO_FILE_FORMAT = "video/*";

	/** Time interval at which upload task will notify about the progress */
	private static final int PROGRESS_UPDATE_INTERVAL = 1000;

	/** Max size for each upload chunk */
	private static final int DEFAULT_CHUNK_SIZE = 10000000;

	protected static final Object ATOMNS = "http://www.w3.org/2005/Atom"; //$NON-NLS-1$

	protected static final Object CATEGORY = "category"; //$NON-NLS-1$

	protected static final String TERM = "term"; //$NON-NLS-1$

	protected static final String LABEL = "label"; //$NON-NLS-1$

	// private YouTubeService service;

	private String siteName;

	private boolean auth;

	private IErrorHandler handler;

	private String videoId;

	/**
	 * A {@link ProgressListener} implementation to track upload progress. The
	 * listener can track multiple uploads at the same time.
	 */
	// private class FileUploadProgressListener implements ProgressListener {
	//
	// private final IProgressMonitor monitor;
	// private int worked = 0;
	//
	// public FileUploadProgressListener(IProgressMonitor monitor) {
	// this.monitor = monitor;
	// }
	//
	// public synchronized void progressChanged(ResumableHttpFileUploader uploader)
	// {
	// switch (uploader.getUploadState()) {
	// case COMPLETE:
	// monitor.worked(100 - worked);
	// return;
	// case IN_PROGRESS:
	// int progress = (int) (uploader.getProgress() + 0.5d);
	// monitor.worked(progress);
	// worked += progress;
	// return;
	// default:
	// return;
	// }
	// }
	// }

	public YouTubeUploadClient() {
		super();
		// service = new YouTubeService("org.photozora-" + Constants.APPNAME + '-'
		// //$NON-NLS-1$
		// + Activator.getDefault().getBundle().getVersion(), YtConstants.DEVELOPERKEY);
	}

	public String uploadVideo(IProgressMonitor monitor, Session session, URI uri, String mimeType, String videoTitle,
			Date creationDate, String[] keywords, String category, String copyright, double latitude, double longitude,
			String location, boolean makePrivate) throws IOException, InterruptedException {

		File videoFile = new File(uri);
		if (!videoFile.exists())
			throw new FileNotFoundException(videoFile.toString());

		System.out.println("Uploading: " + videoFile);

		// Add extra information to the video before uploading.
		Video videoObjectDefiningMetadata = new Video();

		// Set the video to be publicly visible. This is the default
		// setting. Other supporting settings are "unlisted" and "private."
		VideoStatus status = new VideoStatus();
		status.setPrivacyStatus(makePrivate ? "private" : "public");
		videoObjectDefiningMetadata.setStatus(status);

		// Most of the video's metadata is set on the VideoSnippet object.
		VideoSnippet snippet = new VideoSnippet();

		// This code uses a Calendar instance to create a unique name and
		// description for test purposes so that you can easily upload
		// multiple files. You should remove this code from your project
		// and use your own standard names instead.
		snippet.setTitle(videoTitle);
		snippet.setDescription("Video uploaded by ZoRa PhotoDirector");

		// Set the keyword tags that you want to associate with the video.
		List<String> tags = Arrays.asList(keywords);
		snippet.setTags(tags);
		// Add the completed snippet object to the video resource.
		videoObjectDefiningMetadata.setSnippet(snippet);

		try (InputStream videoStream = new FileInputStream(videoFile)) {

			InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,
					videoStream);

			// Insert the video. The command sends three arguments. The first
			// specifies which information the API request is setting and which
			// information the API response should return. The second argument
			// is the video resource that contains metadata about the new video.
			// The third argument is the actual video content.
			YouTube.Videos.Insert videoInsert = youtube.videos().insert("snippet,statistics,status",
					videoObjectDefiningMetadata, mediaContent);

			// Set the upload type and add an event listener.
			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

			// Indicate whether direct media upload is enabled. A value of
			// "True" indicates that direct media upload is enabled and that
			// the entire media content will be uploaded in a single request.
			// A value of "False," which is the default, indicates that the
			// request will use the resumable media upload protocol, which
			// supports the ability to resume an upload operation after a
			// network interruption or other transmission failure, saving
			// time and bandwidth in the event of network failures.
			uploader.setDirectUploadEnabled(false);

			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				public void progressChanged(MediaHttpUploader uploader) throws IOException {
					switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						System.out.println("Initiation Started");
						break;
					case INITIATION_COMPLETE:
						System.out.println("Initiation Completed");
						break;
					case MEDIA_IN_PROGRESS:
						System.out.println("Upload in progress");
						System.out.println("Upload percentage: " + uploader.getProgress());
						break;
					case MEDIA_COMPLETE:
						System.out.println("Upload Completed!");
						break;
					case NOT_STARTED:
						System.out.println("Upload Not Started!");
						break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			// Call the API and upload the video.
			Video returnedVideo = videoInsert.execute();

			// Print data about the newly inserted video from the API response.
			System.out.println("\n================== Returned Video ==================\n");
			System.out.println("  - Id: " + returnedVideo.getId());
			System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
			System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
			System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
			System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
		}
		return null;

		// videoId = null;
		// VideoEntry newEntry = new VideoEntry();
		// YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
		// mg.setTitle(new MediaTitle());
		// mg.getTitle().setPlainTextContent(videoTitle);
		// mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
		// category));
		// mg.setCopyright(new MediaCopyright());
		// mg.getCopyright().setContent(copyright == null ? "" : copyright);
		// //$NON-NLS-1$
		// mg.setPrivate(makePrivate);
		// MediaKeywords mediaKeywords = new MediaKeywords();
		// mg.setKeywords(mediaKeywords);
		// if (keywords != null)
		// for (String kw : keywords)
		// mediaKeywords.addKeyword(kw);
		// mg.setDescription(new MediaDescription());
		// mg.getDescription().setPlainTextContent(videoTitle);
		// mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME,
		// Constants.APPNAME));
		// if (!Double.isNaN(latitude) && !Double.isNaN(longitude))
		// newEntry.setGeoCoordinates(new GeoRssWhere(latitude, longitude));
		// else if (location != null && location.length() > 0)
		// newEntry.setLocation(location);
		// if (creationDate != null) {
		// DateTime dateTime = new DateTime(creationDate);
		// dateTime.setDateOnly(true);
		// newEntry.setRecorded(dateTime);
		// }
		// MediaFileSource ms = new MediaFileSource(videoFile, mimeType);
		// newEntry.setMediaSource(ms);

		// FileUploadProgressListener listener = new
		// FileUploadProgressListener(monitor);
		// ResumableGDataFileUploader uploader = new
		// ResumableGDataFileUploader.Builder(service,
		// new URL(YtConstants.RESUMABLE_UPLOAD_URL), ms, newEntry).title(videoTitle)
		// .trackProgress(listener,
		// PROGRESS_UPDATE_INTERVAL).chunkSize(DEFAULT_CHUNK_SIZE).build();
		//
		// uploader.start();
		// while (!uploader.isDone()) {
		// Thread.sleep(PROGRESS_UPDATE_INTERVAL);
		// }
		// ResponseMessage response = uploader.getResponse();
		// try {
		// if (response != null) {
		// String receiveMessage = response.receiveMessage(3000);
		// if (receiveMessage != null) {
		// // <id>tag:youtube.com,2008:video:Zd_PLRmRNA0</id>
		// int p = receiveMessage.indexOf("<id>") + 4; //$NON-NLS-1$
		// if (p >= 4) {
		// int q = receiveMessage.indexOf("</id>", p); //$NON-NLS-1$
		// if (q > p) {
		// int r = receiveMessage.indexOf("video:", p); //$NON-NLS-1$
		// if (r >= p && r < q)
		// videoId = receiveMessage.substring(r + 6, q).trim();
		// }
		// }
		// }
		// }
		// } catch (ExecutionException e) {
		// return Messages.YouTubeUploadClient_execution_error;
		// } catch (TimeoutException e) {
		// return Messages.YouTubeUploadClient_timeout;
		// }
		// switch (uploader.getUploadState()) {
		// case COMPLETE:
		// return null;
		// case CLIENT_ERROR:
		// return Messages.YouTubeUploadClient_upload_failed;
		// default:
		// return Messages.YouTubeUploadClient_unexpected_upload_status;
		// }
	}

	public void initialize(Session session) {
		IConfigurationElement conf = (IConfigurationElement) session.getAccount().getConfiguration().getParent();
		siteName = conf.getAttribute("name"); //$NON-NLS-1$
	}

	public String getSiteName() {
		return siteName;
	}

	public boolean authenticate(Session session) throws ProtocolException, CommunicationException {
		if (youtube != null) {
			// This OAuth 2.0 access scope allows an application to upload files
			// to the authenticated user's YouTube channel, but doesn't allow
			// other types of access.
			List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");

			// Authorize the request.
			Credential credential;
			try {
				credential = Auth.authorize(scopes, "uploadvideo");
				// This object is used to make YouTube Data API requests.
				youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
						.setApplicationName("youtube-cmdline-uploadvideo-sample").build();
			} catch (IOException e) {
				// TODO Automatisch generierter Erfassungsblock
				e.printStackTrace();
			}

			// CommunityAccount account = session.getAccount();
			// String passwordHash = account.getPasswordHash();
			// String password = decodePassword(passwordHash);
			// try {
			// service.setUserCredentials(account.getName(), password);
			// auth = true;
			// return true;
			// } catch (AuthenticationException e) {
			// // return false
			// }
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
