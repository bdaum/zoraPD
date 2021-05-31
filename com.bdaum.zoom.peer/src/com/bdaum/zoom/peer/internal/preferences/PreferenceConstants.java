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
package com.bdaum.zoom.peer.internal.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.peer.internal.PeerActivator;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String SHAREDCATALOGS = "sharedCatalogs"; //$NON-NLS-1$
	public static final String PORT = "port"; //$NON-NLS-1$
	public static final String PEERS = "peers"; //$NON-NLS-1$
	public static final String RECEIVERS = "receivers"; //$NON-NLS-1$
	public static final String BLOCKEDNODES = "blockedNodes"; //$NON-NLS-1$

	public static final String NETWORK = Messages.PreferenceConstants_network;
	public static final String CATALOGS = Messages.PreferenceConstants_cat;
	public static final String PEER = Messages.PreferenceConstants_peer;

	@Override
	public List<Object> getRootElements() {
		return Arrays.asList(NETWORK);
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == NETWORK)
			return Arrays.asList(CATALOGS, PEER);
		if (group == CATALOGS)
			return Arrays.asList(SHAREDCATALOGS);
		if (group == PEER)
			return Arrays.asList(RECEIVERS, BLOCKEDNODES);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(PeerActivator.PLUGIN_ID);
	}


}
