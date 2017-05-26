package com.bdaum.zoom.jai;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.jai.messages"; //$NON-NLS-1$
	public static String JAIReader_corrupt_file;
	public static String JAIReader_error_decoding_image;
	public static String JAIReader_image_file_has_no_pages;
	public static String JAIReader_unsupported_color_space;
	public static String JAIReader_unsupported_file_format;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
