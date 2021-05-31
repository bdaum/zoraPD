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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.internal.preference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {
	public static final String ENABLE = "com.bdaum.zoom.ai.enable"; //$NON-NLS-1$
	public static final String ACTIVEPROVIDER  = "com.bdaum.zoom.ai.activeProvider"; //$NON-NLS-1$
	public static final String TRANSLATORKEY  = "com.bdaum.zoom.ai.translatorKey"; //$NON-NLS-1$
	public static final String LANGUAGE = "com.bdaum.zoom.ai.language"; //$NON-NLS-1$
	public static final String TRANSLATORENDPOINT = "com.bdaum.zoom.ai.translatorEndpoint"; //$NON-NLS-1$
	
	public static final Object AI = Messages.PreferenceConstants_ai;
	public static final String AIGENERAL = Messages.PreferenceConstants_general;
	public static final String TRANSLATOR = Messages.PreferenceConstants_translator;
	
	@Override
	public List<Object> getRootElements() {
		return Arrays.asList(AI);
	}
	
	@Override
	public List<Object> getChildren(Object group) {
		if (group == AI)
			return Arrays.asList(AIGENERAL, TRANSLATOR);
		if (group == AIGENERAL)
			return Arrays.asList(ENABLE, ACTIVEPROVIDER);
		if (group == TRANSLATOR)
			return Arrays.asList(TRANSLATORKEY, LANGUAGE, TRANSLATORENDPOINT);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(AiActivator.PLUGIN_ID);
	}

}
