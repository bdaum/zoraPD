package com.bdaum.zoom.ai.internal.translator;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.internal.translator.messages"; //$NON-NLS-1$
	public static String TranslatorClient_io_exception;
	public static String TranslatorClient_io_exception_languages;
	public static String TranslatorClient_no_endpoint;
	public static String TranslatorClient_no_key;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
