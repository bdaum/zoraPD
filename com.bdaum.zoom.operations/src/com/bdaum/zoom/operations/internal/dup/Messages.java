package com.bdaum.zoom.operations.internal.dup;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.operations.internal.dup.messages"; //$NON-NLS-1$
	public static String FileNameDuplicatesProvider_duplicates_file_name;
	public static String SimilarityDuplicatesProvider_duplicates_similar;
	public static String CombinedDuplicatesProvider_duplicates_combined;
	public static String ExposureDataDuplicatesProvider_duplicates_exif;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
