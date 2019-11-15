package com.bdaum.zoom.mtp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.mtp.messages"; //$NON-NLS-1$
	public static String StorageObject_file_disk_full;
	public static String StorageObject_renaming_failed;
	public static String StorageObject_transfer_failed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
