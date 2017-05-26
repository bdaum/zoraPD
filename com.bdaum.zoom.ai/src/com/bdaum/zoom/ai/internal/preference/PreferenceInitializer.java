package com.bdaum.zoom.ai.internal.preference;

import java.util.Locale;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.bdaum.zoom.ai.internal.AiActivator;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE
				.getNode(AiActivator.PLUGIN_ID);
		node.putBoolean(PreferenceConstants.ENABLE, false);
		node.put(PreferenceConstants.LANGUAGE, Locale.getDefault().getLanguage());
	}

}
