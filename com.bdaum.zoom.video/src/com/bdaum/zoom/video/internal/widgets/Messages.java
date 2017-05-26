package com.bdaum.zoom.video.internal.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.internal.widgets.messages"; //$NON-NLS-1$
	public static String VideoControl_continue;
	public static String VideoControl_snapshot;
	public static String VideoControl_stop;
	public static String VideoControl_toggle_sound;
	public static String VideoControl_volume;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
