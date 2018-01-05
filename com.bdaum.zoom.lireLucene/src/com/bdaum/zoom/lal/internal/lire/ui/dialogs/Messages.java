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

package com.bdaum.zoom.lal.internal.lire.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.lal.internal.lire.ui.dialogs.messages"; //$NON-NLS-1$

	public static String ConfigureTextSearchDialog_configure_searchfield;
	public static String ConfigureTextSearchDialog_specify_parameters;

	public static String SearchSimilarDialog_additional_keywords;
	public static String SearchSimilarDialog_drawing;
	public static String SearchSimilarDialog_keywords;
	public static String SearchSimilarDialog_no_peer_support_for_algo;
	public static String SearchSimilarDialog_reference_image;
	public static String SearchSimilarDialog_similarit_search_coll_title;
	public static String SearchSimilarDialog_similarity_search;
	public static String SearchSimilarDialog_similarity_search_message;
	public static String SearchSimilarDialog_visual;

	public static String TextSearchDialog_bad_query_expression;
	public static String TextSearchDialog_indexing_in_progress;
	public static String TextSearchDialog_maxmin;
	public static String TextSearchDialog_please_enter_a_search_string;
	public static String TextSearchDialog_query_string;
	public static String TextSearchDialog_specify_a_search_string;
	public static String TextSearchDialog_text_search;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
