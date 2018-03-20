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

package com.bdaum.zoom.operations.internal.gen;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.operations.internal.gen.messages"; //$NON-NLS-1$
	public static String AbstractGalleryGenerator_disk_full;
	public static String AbstractGalleryGenerator_generating_web_gallery;
	public static String AbstractGalleryGenerator_io_error_copying_original;
	public static String AbstractGalleryGenerator_io_error_for_image_n;
	public static String AbstractGalleryGenerator_io_error_updating_web_gallery;
	public static String AbstractGalleryGenerator_io_error_when_copying_web_resource;
	public static String AbstractGalleryGenerator_io_error_when_processing_template;
	public static String AbstractGalleryGenerator_xmp_error;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
