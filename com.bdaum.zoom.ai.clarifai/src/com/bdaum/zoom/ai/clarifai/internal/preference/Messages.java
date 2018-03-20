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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.clarifai.internal.preference;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.clarifai.internal.preference.messages"; //$NON-NLS-1$
	public static String PagePart_access_failed;
	public static String PagePart_access_token;
	public static String PagePart_both_must_be_set;
	public static String PagePart_check_adult;
	public static String PagePart_check_credentials;
	public static String PagePart_client_id;
	public static String PagePart_credentials;
	public static String PagePart_detect_celebrities;
	public static String PagePart_detect_celebrities_tooltip;
	public static String PagePart_detect_faces;
	public static String PagePart_detect_faces_tooltip;
	public static String PagePart_language;
	public static String PagePart_limits;
	public static String PagePart_manage_clarifai_account;
	public static String PagePart_mark_above;
	public static String PagePart_mark_known;
	public static String PagePart_mark_known_tooltip;
	public static String PagePart_max_concepts;
	public static String PagePart_min_confidence;
	public static String PagePart_model;
	public static String PagePart_porno;
	public static String PagePart_secret;
	public static String PagePart_translate;
	public static String PagePart_translate_tooltip;
	public static String PagePart_verified;
	public static String PagePart_visit_account_page;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
