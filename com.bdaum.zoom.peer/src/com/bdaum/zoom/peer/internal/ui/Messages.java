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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.peer.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.peer.internal.ui.messages"; //$NON-NLS-1$
	public static String PeerDefinitionDialog_own_location_as_peer;
	public static String PeerDefinitionDialog_access_restrictions;
	public static String PeerDefinitionDialog_computer_name;
	public static String PeerDefinitionDialog_copy;
	public static String PeerDefinitionDialog_copy_tooltip;
	public static String PeerDefinitionDialog_define_computer_name;
	public static String PeerDefinitionDialog_define_name_and_rights;
	public static String PeerDefinitionDialog_nickname;
	public static String PeerDefinitionDialog_peer_location;
	public static String PeerDefinitionDialog_peerDef_already_exists;
	public static String PeerDefinitionDialog_port;
	public static String PeerDefinitionDialog_rights;
	public static String PeerDefinitionDialog_search;
	public static String PeerDefinitionDialog_search_tooltip;
	public static String PeerDefinitionDialog_specify_operations;
	public static String PeerDefinitionDialog_specify_valid_ip;
	public static String PeerDefinitionDialog_view;
	public static String PeerDefinitionDialog_view_tooltip;
	public static String PeerDefinitionDialog_voice_notes;
	public static String PeerDefinitionDialog_voice_notes_tooltip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
