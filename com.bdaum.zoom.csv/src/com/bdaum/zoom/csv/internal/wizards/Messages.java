package com.bdaum.zoom.csv.internal.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.csv.internal.wizards.messages"; //$NON-NLS-1$
	public static String CsvExportWizard_cannot_create_file;
	public static String CsvExportWizard_n_images;
	public static String CsvExportWizard_nothing_selected;
	public static String CsvExportWizard_one_image;
	public static String CsvTargetFilePage_all_files;
	public static String CsvTargetFilePage_comma_separated_values;
	public static String CsvTargetFilePage_file_name_empty;
	public static String CsvTargetFilePage_first_line;
	public static String CsvTargetFilePage_no_image_selected;
	public static String CsvTargetFilePage_output_file;
	public static String CsvTargetFilePage_specify_location;
	public static String CsvTargetFilePage_specify_target_file;
	public static String CsvTargetFilePage_target_file;
	public static String RelabelPage_dflt_name;
	public static String RelabelPage_new_name;
	public static String RelabelPage_relabel;
	public static String RelabelPage_relabel_message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
