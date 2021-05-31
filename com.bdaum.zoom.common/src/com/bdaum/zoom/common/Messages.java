package com.bdaum.zoom.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.common.messages"; //$NON-NLS-1$
	public static String PreferenceRegistry_externals;
	public static String PreferenceRegistry_internet;
	public static String PreferenceRegistry_processing;
	public static String PreferenceRegistry_user_interface;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
