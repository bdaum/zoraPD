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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.peer.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.peer.internal.preferences.messages"; //$NON-NLS-1$
	public static String PeerPreferencePage_add;
	public static String PeerPreferencePage_add_current;
	public static String PeerPreferencePage_add_restriction;
	public static String PeerPreferencePage_all;
	public static String PeerPreferencePage_automatic_update;
	public static String PeerPreferencePage_calling_peer;
	public static String PeerPreferencePage_catalog_does_not_exist;
	public static String PeerPreferencePage_cats_tooltip;
	public static String PeerPreferencePage_clear;
	public static String PeerPreferencePage_computer_name;
	public static String PeerPreferencePage_dateformat;
	public static String PeerPreferencePage_defaultport;
	public static String PeerPreferencePage_incoming_calls;
	public static String PeerPreferencePage_incoming_msg;
	public static String PeerPreferencePage_incoming_tooltip;
	public static String PeerPreferencePage_last_access;
	public static String PeerPreferencePage_last_op;
	public static String PeerPreferencePage_local;
	public static String PeerPreferencePage_medium;
	public static String PeerPreferencePage_network_geography;
	public static String PeerPreferencePage_nickname;
	public static String PeerPreferencePage_offline;
	public static String PeerPreferencePage_online;
	public static String PeerPreferencePage_own_location;
	public static String PeerPreferencePage_path;
	public static String PeerPreferencePage_peer_and_shared_cat_definition;
	public static String PeerPreferencePage_peer_location;
	public static String PeerPreferencePage_peer_nodes;
	public static String PeerPreferencePage_peer_tooltip;
	public static String PeerPreferencePage_port;
	public static String PeerPreferencePage_privacy;
	public static String PeerPreferencePage_public_items;
	public static String PeerPreferencePage_remove;
	public static String PeerPreferencePage_restriction_obsolete;
	public static String PeerPreferencePage_shared_cats;
	public static String PeerPreferencePage_shared_cats_msg;
	public static String PeerPreferencePage_status;
	public static String PeerPreferencePage_toggle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
