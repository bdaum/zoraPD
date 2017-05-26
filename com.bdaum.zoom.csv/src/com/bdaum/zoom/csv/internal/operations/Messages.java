package com.bdaum.zoom.csv.internal.operations;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.csv.internal.operations.messages"; //$NON-NLS-1$
	public static String ExportCsvOperation_error_opening_csv;
	public static String ExportCsvOperation_error_writing_csv;
	public static String ExportCsvOperation_export_csv;
	public static String ExportCsvOperation_export_values;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
