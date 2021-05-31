package com.bdaum.zoom.webserver;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.webserver.messages"; //$NON-NLS-1$
	public static String PreferenceConstants_webserver;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
