package com.bdaum.zoom.gps.internal.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.gps.internal.wizards.messages"; //$NON-NLS-1$
	public static String AbstractGeoExportWizard_cannot_create;
	public static String AbstractGeoExportWizard_multiple_images;
	public static String AbstractGeoExportWizard_no_media;
	public static String AbstractGeoExportWizard_one_image;
	public static String ExportPage_all_files;
	public static String ExportPage_file_name_empty;
	public static String ExportPage_no_geocoded_image;
	public static String ExportPage_output_file;
	public static String ExportPage_specify_location;
	public static String ExportPage_specify_target_file;
	public static String ExportPage_target_file;
	public static String ExportPage_x_files;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
