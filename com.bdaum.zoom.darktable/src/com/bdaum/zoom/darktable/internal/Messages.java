package com.bdaum.zoom.darktable.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.darktable.internal.messages"; //$NON-NLS-1$
	public static String DtRawConverter_locate_darktable;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
