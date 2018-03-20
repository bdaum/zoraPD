package com.bdaum.zoom.video.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.internal.preferences.messages"; //$NON-NLS-1$
	public static String PagePart_video;
	public static String PagePart_video_tooltip;
	public static String VideoPreferencePage_download;
	public static String VideoPreferencePage_ex_location;
	public static String VideoPreferencePage_no_executable;
	public static String VideoPreferencePage_vlc_description;
	public static String VideoPreferencePage_vlc_key;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
