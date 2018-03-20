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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.bdaum.zoom.ai.msvision.internal.MsVisionActivator;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE
				.getNode(MsVisionActivator.PLUGIN_ID);
		node.putInt(PreferenceConstants.MAXCONCEPTS, 10);
		node.putInt(PreferenceConstants.MINCONFIDENCE, 80);
		node.putBoolean(PreferenceConstants.ADULTCONTENTS, true);
		node.putBoolean(PreferenceConstants.FACES, true);
		node.putBoolean(PreferenceConstants.DESCRIPTION, true);
		node.putBoolean(PreferenceConstants.CELEBRITIES, false);
		node.putBoolean(PreferenceConstants.TRANSLATE_CATEGORIES, true);
		node.putBoolean(PreferenceConstants.TRANSLATE_DESCRIPTION, true);
		node.putBoolean(PreferenceConstants.TRANSLATE_TAGS, true);
		node.putInt(PreferenceConstants.MARKABOVE, 95);
		node.putBoolean(PreferenceConstants.MARKKNOWNONLY, true);
	}

}
