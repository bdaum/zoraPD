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
package com.bdaum.zoom.ai.msvision.internal.preference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.ai.msvision.internal.MsVisionActivator;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String MAXCONCEPTS = "com.bdaum.zoom.ai.msvision.maxConcepts"; //$NON-NLS-1$
	public static final String MINCONFIDENCE = "com.bdaum.zoom.ai.msvision.minConfidence"; //$NON-NLS-1$
	public static final String ADULTCONTENTS = "com.bdaum.zoom.ai.msvision.adultContents"; //$NON-NLS-1$
	public static final String KEY = "com.bdaum.zoom.ai.msvision.key1"; //$NON-NLS-1$
	public static final String FACES = "com.bdaum.zoom.ai.msvision.faces"; //$NON-NLS-1$
	public static final String DESCRIPTION = "com.bdaum.zoom.ai.msvision.description"; //$NON-NLS-1$
	public static final String CELEBRITIES = "com.bdaum.zoom.ai.msvision.celebrities"; //$NON-NLS-1$
	public static final String TRANSLATE_DESCRIPTION = "com.bdaum.zoom.ai.msvision.translateDescription"; //$NON-NLS-1$
	public static final String TRANSLATE_CATEGORIES = "com.bdaum.zoom.ai.msvision.translateCats"; //$NON-NLS-1$
	public static final String TRANSLATE_TAGS = "com.bdaum.zoom.ai.msvision.translateTags"; //$NON-NLS-1$
	public static final String MARKABOVE = "com.bdaum.zoom.ai.msvision.markAbove"; //$NON-NLS-1$
	public static final String MARKKNOWNONLY = "com.bdaum.zoom.ai.msvision.knownOnly"; //$NON-NLS-1$
	public static final String ENDPOINT = "com.bdaum.zoom.ai.msvision.endpoint"; //$NON-NLS-1$
	
	public static final String MSVISION = "Microsoft Vision"; //$NON-NLS-1$

	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == com.bdaum.zoom.ai.internal.preference.PreferenceConstants.AI)
			return Arrays.asList(MSVISION);
		if (group == MSVISION)
			return Arrays.asList(MAXCONCEPTS, MINCONFIDENCE, ADULTCONTENTS, KEY, FACES, DESCRIPTION, CELEBRITIES,
					TRANSLATE_DESCRIPTION, TRANSLATE_CATEGORIES, TRANSLATE_TAGS, MARKABOVE, MARKKNOWNONLY, ENDPOINT);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(MsVisionActivator.PLUGIN_ID);
	}
}
