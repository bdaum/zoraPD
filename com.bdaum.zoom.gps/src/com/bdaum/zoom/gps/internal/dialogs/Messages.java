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

package com.bdaum.zoom.gps.internal.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.gps.internal.dialogs.messages"; //$NON-NLS-1$
	public static String DirPinDialog_direction;
	public static String DirPinDialog_location;
	public static String DirPinDialog_select;
	public static String FindUntaggedDialog_find_untagged;
	public static String FindUntaggedDialog_find_untagged_message;
	public static String FindUntaggedDialog_untagged_images;
	public static String TrackpointDialog_duration;
	public static String TrackpointDialog_edit;
	public static String TrackpointDialog_edit_subtrack;
	public static String TrackpointDialog_edit_trackpoints;
	public static String TrackpointDialog_end;
	public static String TrackpointDialog_end_after_start;
	public static String TrackpointDialog_end_before_x;
	public static String TrackpointDialog_initial_message;
	public static String TrackpointDialog_join;
	public static String TrackpointDialog_modify_start_end;
	public static String TrackpointDialog_remove;
	public static String TrackpointDialog_spli_subtrack;
	public static String TrackpointDialog_split;
	public static String TrackpointDialog_split_subtrack_msg;
	public static String TrackpointDialog_start;
	public static String TrackpointDialog_start_after_x;
	public static String TrackpointDialog_subtrack_overlaps;
	public static String TrackpointDialog_tracktimeformat;
	public static String TrackpointDialog_wrong_gap_end_value;
	public static String TrackpointDialog_wrong_gap_start_value;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
