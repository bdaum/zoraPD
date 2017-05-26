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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.messages"; //$NON-NLS-1$
	public static String NavigationHistory_internal_error;
	public static String UiActivator_access_restriction;
	public static String UiActivator_cancel_all_operations;
	public static String UiActivator_cannot_create_relation_detector;
	public static String UiActivator_cannot_instantiate_image_viewer;
	public static String UiActivator_cannot_instantiate_parser;
	public static String UiActivator_cannot_instantiate_waypoint_collector;
	public static String UiActivator_connection_lost;
	public static String UiActivator_disk_full_playing_remote;
	public static String UiActivator_error_instantiating_action;
	public static String UiActivator_InternalErrorDropinHandler;
	public static String UiActivator_io_error_playing_remote;
	public static String UiActivator_please_wait;
	public static String UiActivator_restart_later;
	public static String UiActivator_restart_msg;
	public static String UiActivator_restart_now;
	public static String UiActivator_restart_required;
	public static String UiActivator_return_to_workbench;
	public static String UiActivator_unsufficient_right_voice_note;
	public static String UiActivator_ZoomCatalog;
	public static String UiConstants_false;
	public static String UiConstants_infinite;
	public static String UiConstants_true;
	public static String UiUtilities_remote_image;
	public static String UiUtilities_text_search;
	public static String UiUtilities_text_search2;
	public static String UiUtilities_the_image_is_remote;
	public static String UiUtilities_time_search;
	public static String UiUtilities_time_search2;
	public static String UiUtilities_All;
	public static String UiUtilities_and;
	public static String UiUtilities_ascending;
	public static String UiUtilities_based_on_similarity;
	public static String UiUtilities_date_format;
	public static String UiUtilities_date_format_compact;
	public static String UiUtilities_descending;
	public static String UiUtilities_File_does_not_exist;
	public static String UiUtilities_File_does_not_exist_delete_entry;
	public static String UiUtilities_File_is_offline;
	public static String UiUtilities_File_is_offline_mount_volume;
	public static String UiUtilities_Files_missing;
	public static String UiUtilities_user_defined;

	public static String UiUtilities_not_a_valid_fp;
	public static String UiUtilities_not_a_valid_integer;
	public static String UiUtilities_or;
	public static String UiUtilities_value_must_be_greater_0;
	public static String UiUtilities_sorted_by;
	public static String UiUtilities_string_too_long;
	public static String VocabManager_cannot_change_level;

	public static String VocabManager_error_in_vocab;
	public static String VocabManager_error_reading;
	public static String VocabManager_file_not_exist;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
