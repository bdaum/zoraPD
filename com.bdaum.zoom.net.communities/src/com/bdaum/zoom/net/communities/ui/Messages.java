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
package com.bdaum.zoom.net.communities.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.net.communities.ui.messages"; //$NON-NLS-1$
	public static String AbstractCommunityExportWizard_auth_failed;
	public static String AbstractCommunityExportWizard_internal_error;
	public static String AuthDialog_enter_code_here;
	public static String AuthDialog_reload;
	public static String CommunityExportWizard_connection_failed;
	public static String CommunityExportWizard_export_n_images;
	public static String CommunityExportWizard_export_one_image;
	public static String CommunityExportWizard_export_to;
	public static String CommunityExportWizard_nothing_selected;
	public static String EditCommunityAccountDialog_account;
	public static String EditCommunityAccountDialog_account_details;
	public static String EditCommunityAccountDialog_account_type;
	public static String EditCommunityAccountDialog_all_categories;
	public static String EditCommunityAccountDialog_assign_keywords_as_tags;
	public static String EditCommunityAccountDialog_available_albums;
	public static String EditCommunityAccountDialog_available_bandwidth;
	public static String EditCommunityAccountDialog_bandwidth;
	public static String EditCommunityAccountDialog_Capacity;
	public static String EditCommunityAccountDialog_categories;
	public static String EditCommunityAccountDialog_connection_failed;
	public static String EditCommunityAccountDialog_create_photo_sets_for_albums;
	public static String EditCommunityAccountDialog_general;
	public static String EditCommunityAccountDialog_import_into_catalog;
	public static String EditCommunityAccountDialog_imported_albums;
	public static String EditCommunityAccountDialog_limit_bandwidth;
	public static String EditCommunityAccountDialog_maximum_file_size;
	public static String EditCommunityAccountDialog_metadata;
	public static String EditCommunityAccountDialog_name;
	public static String EditCommunityAccountDialog_password;
	public static String EditCommunityAccountDialog_photosets_albums;
	public static String EditCommunityAccountDialog_please_fill_in_details;
	public static String EditCommunityAccountDialog_please_specify_account_name;
	public static String EditCommunityAccountDialog_privacy;
	public static String EditCommunityAccountDialog_propagate_categories;
	public static String EditCommunityAccountDialog_restrictions;
	public static String EditCommunityAccountDialog_tags;
	public static String EditCommunityAccountDialog_test;
	public static String EditCommunityAccountDialog_track_exports;
	public static String EditCommunityAccountDialog_unknown;
	public static String EditCommunityAccountDialog_unlimited;
	public static String EditCommunityAccountDialog_used_albums;
	public static String EditCommunityAccountDialog_used_bandwidth;
	public static String EditCommunityAccountDialog_used_categories;
	public static String EditCommunityAccountDialog_web_url;
	public static String ExportToCommunityPage_account;
	public static String ExportToCommunityPage_cannot_send_orginals;
	public static String ExportToCommunityPage_create_new_account;
	public static String ExportToCommunityPage_edit;
	public static String ExportToCommunityPage_exporting_n_images;
	public static String ExportToCommunityPage_exporting_one_image;
	public static String ExportToCommunityPage_include_metadata;
	public static String ExportToCommunityPage_metadata;
	public static String ExportToCommunityPage_no_images_selected;
	public static String ExportToCommunityPage_nothing_to_export;
	public static String ExportToCommunityPage_please_select_an_account;
	public static String ExportToCommunityPage_show_descriptions;
	public static String ExportToCommunityPage_title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
