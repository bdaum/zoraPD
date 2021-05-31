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
 * (c) 2021 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String PLATFORMCLIENT = "com.bdaum.zoom.email.platform"; //$NON-NLS-1$
	public static final String HOSTURL = "com.bdaum.zoom.email.hostUrl"; //$NON-NLS-1$
	public static final String USER = "com.bdaum.zoom.email.user"; //$NON-NLS-1$
	public static final String PASSWORD = "com.bdaum.zoom.email.password"; //$NON-NLS-1$
	public static final String SENDER = "com.bdaum.zoom.email.sender"; //$NON-NLS-1$
	public static final String PORT = "com.bdaum.zoom.email.port"; //$NON-NLS-1$
	public static final String SSL = "ssl"; //$NON-NLS-1$
	public static final String STARTTLS = "starttls"; //$NON-NLS-1$
	public static final String SECURITY = "com.bdaum.zoom.email.security"; //$NON-NLS-1$
	public static final String SIGNATURE = "com.bdaum.zoom.email.signature"; //$NON-NLS-1$
	public static final String VCARD = "com.bdaum.zoom.email.vcard"; //$NON-NLS-1$

	public static final String EMAIL = Messages.PreferenceConstants_email;

	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.INTERNET)
			return Arrays.asList(EMAIL);
		if (group == EMAIL)
			return Arrays.asList(PLATFORMCLIENT, HOSTURL, USER, PASSWORD, SENDER, PORT, SECURITY, SIGNATURE, VCARD);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

}
