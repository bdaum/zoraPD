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

package com.bdaum.zoom.ui.internal.job;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.job.messages"; //$NON-NLS-1$
	public static String ChangeProcessor_deletion_failed;
	public static String CheckForUpdateJob_cannot_access;
	public static String CheckForUpdateJob_check_for_updates;
	public static String CheckForUpdateJob_new_version_available;
	public static String CheckForUpdateJob_there_is_a_new_version;
	public static String CheckForUpdateJob_up_to_date;
	public static String CheckForUpdateJob_update;
	public static String ExportfolderJob_copied;
	public static String ExportfolderJob_download_failed;
	public static String ExportfolderJob_downsampled;
	public static String ExportfolderJob_error_when_exporting_to_ftp;
	public static String ExportfolderJob_export_report;
	public static String ExportfolderJob_export_was_cancelled;
	public static String ExportfolderJob_exporting_to_folder;
	public static String ExportfolderJob_path_too_long;
	public static String ExportfolderJob_stars;
	public static String ExportfolderJob_undefined;
	public static String FindDuplicatesJob_duplicates;
	public static String FindDuplicatesJob_find_duplicates;
	public static String FindDuplicatesJob_internal_error;
	public static String FindSeriesJob_find_series;
	public static String FindSeriesJob_internal_error;
	public static String FindSeriesJob_series;
	public static String FolderWatchJob_folder_update_report;
	public static String FolderWatchJob_import_of_new_images_failed;
	public static String FolderWatchJob_internal_error_synchronizing;
	public static String FolderWatchJob_processing_folder_changes;
	public static String FolderWatchJob_updating_of_images_failed;
	public static String FolderWatchJob_watching_folders;
	public static String GalleryDecorateJob_decorate_gallery;
	public static String PrintJob_printing;
	public static String PrintJob_printing_pages;
	public static String SpellCheckingJob_check_spelling;
	public static String Updater_bad_hash_key;
	public static String Updater_checking;
	public static String Updater_computation_sha1_failed;
	public static String Updater_download_failed;
	public static String Updater_download_from_update_site_failed;
	public static String Updater_download_of_new_version_failed;
	public static String Updater_download_sha1_failed;
	public static String Updater_download_successful;
	public static String Updater_downloading;
	public static String Updater_illegal_sha1;
	public static String Updater_illegal_uri_during_update;
	public static String Updater_installer_package_successfully_downloaded;
	public static String Updater_key_does_not_match;
	public static String Updater_no;
	public static String Updater_update;
	public static String Updater_updater;
	public static String Updater_updating;
	public static String Updater_yes;
	public static String UpdateRawImagesJob_updating_raw_images;
	public static String SyncPicasaJob_error_configuring_SAX;
	public static String SyncPicasaJob_error_parsing_file;
	public static String SyncPicasaJob_error_reading_file;
	public static String SyncPicasaJob_internal_error;
	public static String SyncPicasaJob_io_error_reading_ini;
	public static String SyncPicasaJob_scanning_face_data;
	public static String SyncPicasaJob_scanning_picasa;
	public static String SyncPicasaJob_synchronize;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
