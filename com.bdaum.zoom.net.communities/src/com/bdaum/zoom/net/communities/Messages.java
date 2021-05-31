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
package com.bdaum.zoom.net.communities;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.net.communities.messages"; //$NON-NLS-1$
	public static String CommunitiesActivator_cannot_instantiate;
	public static String CommunitiesPreferencePage_communities;
	public static String CommunitiesPreferencePage_community_descr;
	public static String CommunitiesPreferencePage_community_tooltip;
	public static String CommunitiesPreferencePage_create_a_new_account;
	public static String CommunitiesPreferencePage_edit;
	public static String CommunitiesPreferencePage_edit_selected_account;
	public static String CommunitiesPreferencePage_new;
	public static String CommunitiesPreferencePage_photo_community_accounts;
	public static String CommunitiesPreferencePage_remove;
	public static String CommunitiesPreferencePage_remove_selected_account;
	public static String CommunityAccount_account_does_not_belong_to;
	public static String CommunityAccount_bad_url;
	public static String CommunityAccount_internal_protocol_error;
	public static String ImageUploadApi_communication_error;
	public static String PreferenceConstants_communities;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
