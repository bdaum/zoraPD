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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.bdaum.zoom.ai.clarifai.internal.ClarifaiActivator;


public class PreferenceInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE
				.getNode(ClarifaiActivator.PLUGIN_ID);
		node.putInt(PreferenceConstants.MAXCONCEPTS, 10);
		node.putInt(PreferenceConstants.MINCONFIDENCE, 90);
		node.putInt(PreferenceConstants.MARKABOVE, 99);
		node.putBoolean(PreferenceConstants.TRANSLATE, true);
		node.putBoolean(PreferenceConstants.MARKKNOWNONLY, true);
		node.putBoolean(PreferenceConstants.ADULTCONTENTS, false);
		node.putBoolean(PreferenceConstants.FACES, false);
		node.putBoolean(PreferenceConstants.CELEBRITIES, false);
		node.put(PreferenceConstants.LANGUAGE, "en"); //$NON-NLS-1$
	}

}
