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
package com.bdaum.zoom.peer.internal.model;

import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.peer.internal.PeerActivator;

public class PeerDefinition {

	private String host;
	private int port;
	private int rights;
	private SharedCatalog parent;
	private String address;
	private long lastAccess;
	private String nickname;

	public PeerDefinition(String address, long lastAccess, int rights) {
		this.address = address;
		this.lastAccess = lastAccess;
		this.rights = rights;
		int p = address.indexOf('\t');
		if (p >= 0) {
			nickname = address.substring(0,p);
			address = address.substring(p+1);
		}
		p = address.lastIndexOf(':');
		if (p >= 0) {
			try {
				port = Integer.parseInt(address.substring(p+1));
			} catch (NumberFormatException e) {
				// assume 0
			}
			host = address.substring(0,p);
		} else
			host = address;
	}


	public PeerDefinition(String nickname, String host, int port) {
		this.nickname = nickname;
		this.host = host;
		setPort(port);
	}

	/**
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            das zu setzende Objekt host
	 */
	public void setHost(String host) {
		this.host = host;
		address = port >= 0 ? host + ":" + port : host; //$NON-NLS-1$
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            das zu setzende Objekt port
	 */
	public void setPort(int port) {
		this.port = port;
		address = port >= 0 ? host + ":" + port : host; //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PeerDefinition)
			return host.equals(((PeerDefinition) obj).getHost())
					&& port == ((PeerDefinition) obj).getPort();
		return false;
	}

	@Override
	public int hashCode() {
		return host.hashCode() + port;
	}

	@Override
	public String toString() {
		return nickname != null && nickname.length() > 0 ? nickname + '\t' + address : address;
	}

	/**
	 * @return rights
	 */
	public int getRights() {
		return rights;
	}

	/**
	 * @param rights
	 *            das zu setzende Objekt rights
	 */
	public void setRights(int rights) {
		this.rights = rights;
	}

	public void setParent(SharedCatalog parent) {
		this.parent = parent;
		parent.addRestriction(this);
	}

	/**
	 * @return parent
	 */
	public SharedCatalog getParent() {
		return parent;
	}

	public String getRightsLabel() {
		if (isBlocked())
			return Messages.PeerDefinition_black_listed;
		StringBuilder sb = new StringBuilder();
		if ((rights & IPeerService.SEARCH) != 0)
			sb.append(Messages.PeerDefinition_search);
		if ((rights & IPeerService.VIEW) != 0) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(Messages.PeerDefinition_view);
		}
		if ((rights & IPeerService.VOICE) != 0) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(Messages.PeerDefinition_voice_notes);
		}
		if ((rights & IPeerService.COPY) != 0) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(Messages.PeerDefinition_copy);
		}
		if (sb.length() == 0)
			return Messages.PeerDefinition_none;
		return sb.toString();
	}

	public boolean isBlocked() {
		return PeerActivator.getDefault().getBlockedNodes().contains(host);
	}


	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}


	/**
	 * @return lastAccess
	 */
	public long getLastAccess() {
		return lastAccess;
	}

	public String getLocation() {
		return address;
	}


	public String getNickname() {
		return nickname;
	}


	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
