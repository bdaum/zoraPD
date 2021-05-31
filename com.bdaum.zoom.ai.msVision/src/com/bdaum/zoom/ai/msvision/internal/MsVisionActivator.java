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
package com.bdaum.zoom.ai.msvision.internal;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.msvision.internal.preference.PreferenceConstants;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class MsVisionActivator extends ZUiPlugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.ai.msVision"; //$NON-NLS-1$

	private static MsVisionActivator plugin;

	private String key;

	private String endpoint;

	private ComputerVisionClient client;

	public ComputerVisionClient getClient() {
		if (client == null && key != null && !key.isEmpty())
			client = ComputerVisionManager.authenticate(key).withEndpoint(endpoint);
		return client;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		key = getPreferenceStore().getString(PreferenceConstants.KEY);
		endpoint = getPreferenceStore().getString(PreferenceConstants.ENDPOINT);
		InstanceScope.INSTANCE.getNode(AiActivator.PLUGIN_ID)
				.addPreferenceChangeListener(new IEclipsePreferences.IPreferenceChangeListener() {
					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						if (com.bdaum.zoom.ai.internal.preference.PreferenceConstants.ENABLE.equals(event.getKey()))
							disposeClient();
					}
				});
	}

	public void stop(BundleContext context) throws Exception {
		disposeClient();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MsVisionActivator getDefault() {
		return plugin;
	}

	public void setAccountCredentials(String key, String endpoint) {
		this.key = key;
		this.endpoint = endpoint;
	}

	public void disposeClient() {
		client = null;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public String getKey() {
		return key;
	}

}
