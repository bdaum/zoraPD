package com.bdaum.zoom.rawtherapee.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.rawtherapee.internal.messages"; //$NON-NLS-1$
	public static String RTRawConverter_locate_rawtherapee;
	public static String RTRawConverter_use_cli;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
