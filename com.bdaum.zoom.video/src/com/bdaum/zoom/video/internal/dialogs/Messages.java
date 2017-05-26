package com.bdaum.zoom.video.internal.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.internal.dialogs.messages"; //$NON-NLS-1$
	public static String VLCDialog_assign_correct_version;
	public static String VLCDialog_does_not_exist;
	public static String VLCDialog_download;
	public static String VLCDialog_executable;
	public static String VLCDialog_no_vlc;
	public static String VLCDialog_not_specified;
	public static String VLCDialog_not_suitable;
	public static String VLCDialog_vlckey;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
