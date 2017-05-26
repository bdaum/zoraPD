package com.bdaum.zoom.ui.internal.hover;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.hover.messages"; //$NON-NLS-1$
	public static String HoverInfo_origin;
	public static String HoveringController_hover_control;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
