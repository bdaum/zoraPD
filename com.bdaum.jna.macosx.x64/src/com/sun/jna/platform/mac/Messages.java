package com.sun.jna.platform.mac;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.sun.jna.platform.mac.messages"; //$NON-NLS-1$
	public static String MacFileUtils_file_not_trashed;
	public static String MacFileUtils_trash_not_found;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
