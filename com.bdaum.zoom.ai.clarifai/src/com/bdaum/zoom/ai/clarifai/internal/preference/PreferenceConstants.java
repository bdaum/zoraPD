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
package com.bdaum.zoom.ai.clarifai.internal.preference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.ai.clarifai.internal.ClarifaiActivator;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String MAXCONCEPTS = "com.bdaum.zoom.ai.clarifai.maxConcepts"; //$NON-NLS-1$
	public static final String MINCONFIDENCE = "com.bdaum.zoom.ai.clarifai.minConfidence"; //$NON-NLS-1$
	public static final String MARKABOVE = "com.bdaum.zoom.ai.clarifai.markAbove"; //$NON-NLS-1$
	public static final String MARKKNOWNONLY = "com.bdaum.zoom.ai.clarifai.markKnownOnly"; //$NON-NLS-1$
	public static final String ADULTCONTENTS = "com.bdaum.zoom.ai.clarifai.adultContents"; //$NON-NLS-1$
	public static final String TRANSLATE = "com.bdaum.zoom.ai.clarifai.translate"; //$NON-NLS-1$
	public static final String THEME = "com.bdaum.zoom.ai.clarifai.theme"; //$NON-NLS-1$
	public static final String FACES = "com.bdaum.zoom.ai.clarifai.faces"; //$NON-NLS-1$
	public static final String CELEBRITIES = "com.bdaum.zoom.ai.clarifai.celebrities"; //$NON-NLS-1$
	public static final String LANGUAGE = "com.bdaum.zoom.ai.clarifai.language"; //$NON-NLS-1$
	public static final String APIKEY = "com.bdaum.zoom.ai.clarifai.apikey"; //$NON-NLS-1$
	
	public static final String CLARIFAI = "Clarifai"; //$NON-NLS-1$

	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == com.bdaum.zoom.ai.internal.preference.PreferenceConstants.AI)
			return Arrays.asList(CLARIFAI);
		if (group == CLARIFAI)
			return Arrays.asList(MAXCONCEPTS, MINCONFIDENCE, MARKABOVE, MARKKNOWNONLY, ADULTCONTENTS, TRANSLATE, THEME,
					FACES, CELEBRITIES, LANGUAGE, APIKEY);
		return Collections.emptyList();
	}

	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(ClarifaiActivator.PLUGIN_ID);
	}

}
