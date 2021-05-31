package com.bdaum.zoom.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.preferences.messages"; //$NON-NLS-1$
	public static String AbstractPreferencePage_cannot_create_page_part;
	public static String PreferenceConstants_appearance;
	public static String PreferenceConstants_application;
	public static String PreferenceConstants_audio;
	public static String PreferenceConstants_import;
	public static String PreferenceConstants_metadata;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
