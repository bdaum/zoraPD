package com.bdaum.zoom.web.galleria;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.web.galleria.messages"; //$NON-NLS-1$
	public static String GalleriaGenerator_missing_resources;
	public static String GalleriaGenerator_please_install_theme;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
