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
 * (c) 2012-2014 Berthold Daum  
 */
package com.bdaum.zoom.vr.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.vr.internal.messages"; //$NON-NLS-1$
	public static String ExhibitionJob_disk_full_copying;
	public static String ExhibitionJob_disk_full_web_resource;
	public static String ExhibitionJob_download_image_failed;
	public static String ExhibitionJob_error_creating_image_file;
	public static String ExhibitionJob_error_generating;
	public static String ExhibitionJob_generate_exhibition;
	public static String ExhibitionJob_generating;
	public static String ExhibitionJob_generator;
	public static String ExhibitionJob_io_error;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
