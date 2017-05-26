package com.bdaum.zoom.video.youtube.internal.job;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.youtube.internal.job.messages"; //$NON-NLS-1$
	public static String ExportToYouTubeJob_communication_error;
	public static String ExportToYouTubeJob_error_uploading;
	public static String ExportToYouTubeJob_export_report;
	public static String ExportToYouTubeJob_exporting_video;
	public static String ExportToYouTubeJob_io_error_upload;
	public static String ExportToYouTubeJob_service_error_upload;
	public static String ExportToYouTubeJob_upload;
	public static String ExportToYouTubeJob_upload_interrupted;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
