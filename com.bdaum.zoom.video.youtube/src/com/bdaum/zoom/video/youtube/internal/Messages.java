package com.bdaum.zoom.video.youtube.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.youtube.internal.messages"; //$NON-NLS-1$
	public static String YouTubeUploadClient_communication_error;
	public static String YouTubeUploadClient_execution_error;
	public static String YouTubeUploadClient_internal_protocol_error;
	public static String YouTubeUploadClient_timeout;
	public static String YouTubeUploadClient_unexpected_upload_status;
	public static String YouTubeUploadClient_upload_failed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
