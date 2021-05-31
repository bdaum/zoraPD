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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;


public class MailPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		node.putBoolean(PreferenceConstants.PLATFORMCLIENT, true);
		node.put(PreferenceConstants.HOSTURL, ""); //$NON-NLS-1$
		node.put(PreferenceConstants.USER, ""); //$NON-NLS-1$
		node.put(PreferenceConstants.PASSWORD, ""); //$NON-NLS-1$
		node.put(PreferenceConstants.SENDER, ""); //$NON-NLS-1$
		node.put(PreferenceConstants.SECURITY, PreferenceConstants.SSL); 
		node.putInt(PreferenceConstants.PORT, 465); 
		node.put(PreferenceConstants.SIGNATURE, ""); //$NON-NLS-1$
		node.put(PreferenceConstants.VCARD, ""); //$NON-NLS-1$
	}

}
