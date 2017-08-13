package com.bdaum.zoom.lal.internal.lucene;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.lal.internal.lucene.messages"; //$NON-NLS-1$
	public static String Lucene_lucene_service_started;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
