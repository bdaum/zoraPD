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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.video.internal.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.internal.views.messages"; //$NON-NLS-1$
	public static String VideoViewer_access_restricted;
	public static String VideoViewer_connection_lost;
	public static String VideoViewer_currently_not_available;
	public static String VideoViewer_enlarged_thumbnail;
	public static String VideoViewer_n_of_m;
	public static String VideoViewer_no_stream;
	public static String VideoViewer_rights_not_sufficient;
	public static String VideoViewer_video_viewer;
	public static String VlcPlayingThread_error_initializing;
	public static String VlcPlayingThread_error_playing;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
