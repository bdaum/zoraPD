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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.webserver.internals.messages"; //$NON-NLS-1$
	public static String ImagesContextHandler_bad_query;
	public static String ImagesContextHandler_cmp_error;
	public static String ImagesContextHandler_xmp_io_error;
	public static String LightboxContextHandler_maxmin;
	public static String LightboxContextHandler_report;
	public static String PluginContextHandler_cannot_load_resrce;
	public static String PluginContextHandler_error_unpacking_rsrce;
	public static String WebserverActivator_configure_webserver;
	public static String WebserverActivator_error_unpacking;
	public static String WebserverActivator_failed;
	public static String WebserverActivator_failed_to_start_server;
	public static String WebserverActivator_port_alread_in_use;
	public static String WebserverActivator_running;
	public static String WebserverActivator_server_started;
	public static String WebserverActivator_server_stopped;
	public static String WebserverActivator_stopped;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
