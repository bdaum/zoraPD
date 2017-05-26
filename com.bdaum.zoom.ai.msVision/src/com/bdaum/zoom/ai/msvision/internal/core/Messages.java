package com.bdaum.zoom.ai.msvision.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.msvision.internal.core.messages"; //$NON-NLS-1$
	public static String MsVisionServiceProvider_ms_vision_exception;
	public static String MsVisionServiceProvider_ms_vision_io_error;
	public static String MsVisionServiceProvider_ms_vision_proposals;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
