package com.bdaum.zoom.ai.internal.services;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.internal.services.messages"; //$NON-NLS-1$
	public static String AiService_deactivated;
	public static String AiService_provider_not_found;
	public static String AiService_service_activated;
	public static String AiService_service_deactivated;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
