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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.codes;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.codes.messages"; //$NON-NLS-1$
	public static String CodeParser_cat_not_found;
	public static String CodeParser_code_catalog_not_found;
	public static String CodeParser_error_creating_code_parser;
	public static String CodeParser_io_exception;
	public static String CodeParser_revision;
	public static String CodeParser_sax_parsing_exceptiion;
	public static String CodeParser_scene_codes;
	public static String CodeParser_scene_codes_msg;
	public static String CodeParser_subject_codes;
	public static String CodeParser_subject_codes_msg;
	public static String CodeParser_unknown;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
