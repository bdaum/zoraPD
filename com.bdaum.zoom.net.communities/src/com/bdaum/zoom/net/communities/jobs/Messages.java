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
package com.bdaum.zoom.net.communities.jobs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.net.communities.jobs.messages"; //$NON-NLS-1$
	public static String ExportToCommunityJob_cancel;
	public static String ExportToCommunityJob_communication_error;
	public static String ExportToCommunityJob_error_when_exporting;
	public static String ExportToCommunityJob_export_to_report;
	public static String ExportToCommunityJob_export_was_canceled;
	public static String ExportToCommunityJob_exporting_to;
	public static String ExportToCommunityJob_image_already_uploaded;
	public static String ExportToCommunityJob_image_classified_as_unsafe;
	public static String ExportToCommunityJob_image_is_larger_than;
	public static String ExportToCommunityJob_image_too_large;
	public static String ExportToCommunityJob_image_uploaded_at;
	public static String ExportToCommunityJob_n_images_transferred;
	public static String ExportToCommunityJob_privacy_violated;
	public static String ExportToCommunityJob_replace;
	public static String ExportToCommunityJob_replace_all;
	public static String ExportToCommunityJob_skip_all;
	public static String ExportToCommunityJob_skp;
	public static String ExportToCommunityJob_the_traffic_limit_of;
	public static String ExportToCommunityJob_tracik_date_format;
	public static String ExportToCommunityJob_traffic_limit_exceeded;
	public static String ExportToCommunityJob_upload;
	public static String ExportToCommunityJob_upload_all;
	public static String UploadJob_communication_error;
	public static String UploadJob_export_report;
	public static String UploadJob_export_to;
	public static String UploadJob_file_upload;
	public static String UploadJob_internal_error;
	public static String UploadJob_upload_failed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
