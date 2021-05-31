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
package com.bdaum.zoom.net.communities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String COMMUNITYACCOUNTS = "com.bdaum.zoom.communityAccounts."; //$NON-NLS-1$
	public static final String COMMUNITIES = Messages.PreferenceConstants_communities; 

	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.INTERNET) 
			return Arrays.asList(COMMUNITIES);
		if (group == COMMUNITIES) {
			List<Object> result = new ArrayList<>();
			for (IExtension ext : Platform.getExtensionRegistry()
					.getExtensionPoint(CommunitiesActivator.PLUGIN_ID, "community").getExtensions()) //$NON-NLS-1$
				for (IConfigurationElement conf : ext.getConfigurationElements())
					result.add(COMMUNITYACCOUNTS + conf.getAttribute("id")); //$NON-NLS-1$
			return result;
		}
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(CommunitiesActivator.PLUGIN_ID);
	}

}
