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
package com.bdaum.zoom.peer.internal.services;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.peer.internal.services.messages"; //$NON-NLS-1$
	public static String PeerService_disk_full;
	public static String PeerService_fetch;
	public static String PeerService_file_transfer;
	public static String PeerService_io_error_creating;
	public static String PeerService_io_error_reading;
	public static String PeerService_peer_service_activated;
	public static String PeerService_transfer_file;
	public static String ROSGiManager_configure_network;
	public static String ROSGiManager_framework_error;
	public static String ROSGiManager_io_error;
	public static String ROSGiManager_ping;
	public static String ROSGiManager_port_occupied;
	public static String ROSGiManager_service_not_found;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
