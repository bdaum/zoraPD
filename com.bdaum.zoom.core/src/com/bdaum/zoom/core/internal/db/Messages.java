package com.bdaum.zoom.core.internal.db;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.core.internal.db.messages"; //$NON-NLS-1$
	public static String AssetEnsemble_bad_face_data;
	public static String AssetEnsemble_drawing_present;
	public static String AssetEnsemble_persons;
	public static String CatalogConverter_consolidate_album_count;
	public static String CatalogConverter_duplicated_relationships;
	public static String CatalogConverter_fast_album_access;
	public static String CatalogConverter_fix_raw_file_properties;
	public static String CatalogConverter_fix_watched_folder_ids;
	public static String CatalogConverter_full_text;
	public static String CatalogConverter_image_ID;
	public static String CatalogConverter_iptc_dateCreated;
	public static String CatalogConverter_overlapping_timeline;
	public static String CatalogConverter_prune_system_collections;
	public static String CatalogConverter_restore_directory_entries;
	public static String CatalogConverter_world_region;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
