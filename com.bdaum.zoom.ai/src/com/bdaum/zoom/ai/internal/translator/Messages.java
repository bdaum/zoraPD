package com.bdaum.zoom.ai.internal.translator;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.internal.translator.messages"; //$NON-NLS-1$
	public static String TranslatorClient_configuration_error;
	public static String TranslatorClient_io_exception_access_token;
	public static String TranslatorClient_io_exception_languages;
	public static String TranslatorClient_io_exception_translating;
	public static String TranslatorClient_protocol_exception;
	public static String TranslatorClient_unknown_host;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
