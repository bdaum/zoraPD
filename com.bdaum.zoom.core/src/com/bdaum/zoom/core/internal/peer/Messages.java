package com.bdaum.zoom.core.internal.peer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.core.internal.peer.messages"; //$NON-NLS-1$
	public static String ConnectionLostException_offline;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
