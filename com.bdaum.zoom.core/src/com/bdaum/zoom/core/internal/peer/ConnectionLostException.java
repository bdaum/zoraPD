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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.core.internal.peer;

import org.eclipse.osgi.util.NLS;

public class ConnectionLostException extends RuntimeException {

	private static final long serialVersionUID = -2884180222935153085L;
	private final String location;

	public ConnectionLostException(String location) {
		super("offline"); //$NON-NLS-1$
		this.location = location;
	}

	@Override
	public String getLocalizedMessage() {
		return NLS.bind(Messages.ConnectionLostException_offline, location);
	}

}
