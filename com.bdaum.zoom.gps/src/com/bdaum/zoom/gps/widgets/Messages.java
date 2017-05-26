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

package com.bdaum.zoom.gps.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.gps.widgets.messages"; //$NON-NLS-1$
	public static String AbstractMapComponent_add_marker;
	public static String AbstractMapComponent_create_new_location;
	public static String AbstractMapComponent_drag_me;
	public static String AbstractMapComponent_embedded_map;
	public static String AbstractMapComponent_enable_JS;
	public static String AbstractMapComponent_error_search;
	public static String AbstractMapComponent_error_unpacking;
	public static String AbstractMapComponent_explanation;
	public static String AbstractMapComponent_Init_failed;
	public static String AbstractMapComponent_io_error_loading;
	public static String AbstractMapComponent_location_not_found;
	public static String AbstractMapComponent_location_search;
	public static String AbstractMapComponent_map_is_loading;
	public static String AbstractMapComponent_multiple_images;
	public static String AbstractMapComponent_next_page;
	public static String AbstractMapComponent_no_images;
	public static String AbstractMapComponent_no_tagging_possible;
	public static String AbstractMapComponent_not_found;
	public static String AbstractMapComponent_previous_page;
	public static String AbstractMapComponent_resource_missing;
	public static String AbstractMapComponent_search;
	public static String AbstractMapComponent_single_image;
	public static String AbstractMapComponent_web_service_error_search;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
