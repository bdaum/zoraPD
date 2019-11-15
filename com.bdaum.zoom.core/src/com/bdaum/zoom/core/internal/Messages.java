/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.core.internal.messages"; //$NON-NLS-1$
	public static String ImportState_already_in_cat;
	public static String ImportState_File_exists;
	public static String ImportState_Bad_numeric_data;
	public static String ImportState_Bad_date_value;
	public static String ImportState_Internal_error_processing_field;
	public static String ImportState_Malformed_XMP;
	public static String ImportState_IO_error_accessing_XMP;
	public static String ImportState_IO_error_importing;
	public static String ImportState_Internal_error_assigning;
	public static String ImportState_Bad_XMP_URL;
	public static String ImportState_IO_Error_XMP;
	public static String ImportState_File_already_imported;
	public static String ImportState_disk_full_restoring_xmp;
	public static String ImportState_error_writing_thumbnail;
	public static String ImportState_internal_error_xmp;
	public static String ImportState_io_error_restoring_xmp;

	public static String CoreActivator_backing_up_cat;
	public static String CoreActivator_Cannot_execute_operation;
	public static String CoreActivator_Categories;
	public static String CoreActivator_Cleaning_up;
	public static String CoreActivator_cannot_create_recipe_processor;
	public static String CoreActivator_catalog_converted;
	public static String CoreActivator_converting_cat;
	public static String CoreActivator_converting_to_new_version;
	public static String CoreActivator_error_closing_database;
	public static String CoreActivator_error_instantiating_catalog_contributor;
	public static String CoreActivator_error_when_updating_to_version_n;
	public static String CoreActivator_Exhibitions;
	public static String CoreActivator_index_file_does_not_exist;
	public static String CoreActivator_internal_error_handler_instantiation;
	public static String CoreActivator_internal_error_instantiating_media_support;
	public static String CoreActivator_Not_rated;
	public static String CoreActivator_Ratings;
	public static String CoreActivator_session_closed;
	public static String CoreActivator_session_started;
	public static String CoreActivator_Slideshows;
	public static String CoreActivator_unsupported_version;
	public static String CoreActivator_use_newer_version;
	public static String CoreActivator_User_defined;
	public static String CoreActivator_web_galleries;
	public static String CoreActivator_workspace_locked;
	public static String CoreActivator_wrong_index_version;
	public static String FileWatchManager_cannot_instantiate_file_monitor;
	public static String FileWatchManager_internal_error_processing_file_change;
	public static String FileWatchManager_io_error;
	public static String FileWatchManager_process_file_changes;

	public static String HighresImageLoader_cleaning_temp;
	public static String HighresImageLoader_DCRAW_conversion;
	public static String HighresImageLoader_DCRAWconversion_failed;
	public static String HighresImageLoader_error_loading_image;
	public static String HighresImageLoader_image_format_not_supported;
	public static String HighresImageLoader_Loading_image;
	public static String HighresImageLoader_no_raw_converter;
	public static String HighresImageLoader_Not_enough_memory_to_open;

	public static String Locker_cannot_lock;

	public static String Utilities_cannot_jump;
	public static String Utilities_email;
	public static String Utilities_Error_initializing_categories;
	public static String Utilities_Error_initializing_keywords;
	public static String Utilities_Error_loading_property_file;
	public static String Utilities_Error_writing_categories;
	public static String Utilities_Error_writing_keywords;
	public static String Utilities_io_error_creating_categories;
	public static String Utilities_io_error_creating_keywords;
	public static String Utilities_last_background_imports;
	public static String Utilities_last_import;
	public static String Utilities_not_categorized;
	public static String Utilities_nxmpixel;
	public static String VolumeManager_error_compiling_pattern;
	public static String VolumeManager_initialization_failed;
	public static String VolumeManager_initialize;
	public static String VolumeManager_monitor_volumes;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
