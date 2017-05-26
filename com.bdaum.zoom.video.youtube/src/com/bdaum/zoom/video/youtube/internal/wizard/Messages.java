package com.bdaum.zoom.video.youtube.internal.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.youtube.internal.wizard.messages"; //$NON-NLS-1$
	public static String ExportToYouTubePage_check_account;
	public static String ExportToYouTubePage_communication_failed;
	public static String ExportToYouTubePage_error_fetching_category;
	public static String ExportToYouTubePage_exporting;
	public static String ExportToYouTubePage_exporting_n_video;
	public static String ExportToYouTubePage_exporting_one_video;
	public static String ExportToYouTubePage_include_geo;
	public static String ExportToYouTubePage_include_keywords;
	public static String ExportToYouTubePage_no_video_selected;
	public static String ExportToYouTubePage_no_video_to_export;
	public static String ExportToYouTubePage_settings;
	public static String ExportToYouTubePage_user_auth_failed;
	public static String ExportToYouTubePage_wrong_protocol;
	public static String ExportToYouTubePage_youtube_category;
	public static String YouTubeExportWizard_export_n_videos;
	public static String YouTubeExportWizard_export_one_video;
	public static String YouTubeExportWizard_export_to;
	public static String YouTubeExportWizard_no_video_selected;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
