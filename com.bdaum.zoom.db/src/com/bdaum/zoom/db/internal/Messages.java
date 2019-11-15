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

package com.bdaum.zoom.db.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.db.internal.messages"; //$NON-NLS-1$
	public static String CollectionProcessor_distance;
	public static String CollectionProcessor_io_error_similarity;
	public static String CollectionProcessor_io_error_text_search;
	public static String CollectionProcessor_parser_error_text_search;
	public static String CollectionProcessor_score;
	public static String DbActivator_internal_error_query_extension;
	public static String DbFactory_db_service_started;
	public static String DbManager_backing_up_cat;
	public static String DbManager_backup_impossible;
	public static String DbManager_Catalog_error;
	public static String DbManager_Catalog_maintenance;
	public static String DbManager_Defrag_error;
	public static String DbManager_Defrag_failed;
	public static String DbManager_Defragmenting_;
	public static String DbManager_database_converted_to_version_n;
	public static String DbManager_defrag_cat_2;
	public static String DbManager_defragmentation_failed;
	public static String DbManager_defragmentation_successfiul;
	public static String DbManager_defragmenting_cat;
	public static String DbManager_error_checking_sanity;
	public static String DbManager_Failed_to_open_catalog;
	public static String DbManager_pruning_empty;
	public static String DbManager_The_cat_seems_to_be_fragmented;
	public static String DbManager_Unable_to_open_catalog;
	public static String DbManager_unknown_city;
	public static String DbManager_unknown_country;
	public static String DbManager_unknown_state;
	public static String DbManager_unknown_world_region;
	public static String DbManager_updating_database_version;
	public static String DbManager_User_field_1;
	public static String DbManager_User_field_2;
	public static String DbManager_file_missing;
	public static String DbManager_imports;
	public static String DbManager_internal_error_cleaning;
	public static String DbManager_internal_error_configurator_extension;
	public static String DbManager_internal_error_creation_locations;
	public static String DbManager_internal_error_folder_hierarchy;
	public static String DbManager_internal_error_pruning;
	public static String DbManager_internal_error_timeline;
	public static String DbManager_last_import;
	public static String DbManager_operation_was_aborted;
	public static String DbManager_tethered;
	public static String DbManager_time_out;
	public static String DbManager_locations;
	public static String DbManager_lost_connection;
	public static String DbManager_recent_bg_imports;
	public static String DbManager_recent_imports;
	public static String DbManager_reinsert_media;
	public static String DbManager_Directories;
	public static String DbManager_not_enough_disc_space;
	public static String DbManager_Timeline;


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
