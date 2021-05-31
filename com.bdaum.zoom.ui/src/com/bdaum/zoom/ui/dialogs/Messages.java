package com.bdaum.zoom.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.dialogs.messages"; //$NON-NLS-1$
	public static String TimedMessageDialog_update_dialog;
	public static String WatchedFolderLabelProvider_no;
	public static String WatchedFolderLabelProvider_storage;
	public static String WatchedFolderLabelProvider_transfer;
	public static String WatchedFolderLabelProvider_yes;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
