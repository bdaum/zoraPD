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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ai.clarifai.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.ai.clarifai.internal.preference.PreferenceConstants;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Theme;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import okhttp3.OkHttpClient;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class ClarifaiActivator extends ZUiPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.ai.clarifai"; //$NON-NLS-1$

	public static final String[] LANGUAGES = new String[] { "da", "de", "en", "es", "fi", "fr", "hu", "it", "nl", "no", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
			"pl", "pt", "ru", "sv", "tr" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	};

	// The shared instance
	private static ClarifaiActivator plugin;

	private String clientId;

	private String clientSecret;

	private ClarifaiClient client;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		clientId = getPreferenceStore().getString(PreferenceConstants.CLIENTID);
		clientSecret = getPreferenceStore().getString(PreferenceConstants.CLIENTSECRET);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
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
	public static ClarifaiActivator getDefault() {
		return plugin;
	}

	public void setAccountCredentials(String clientId, String clientSecret) {
		if (!clientId.equals(this.clientId) || !clientSecret.equals(this.clientSecret)) {
			this.clientId = clientId;
			this.clientSecret = clientSecret;
			disposeClient();
		}
	}

	public ClarifaiClient getClient() {
		if (client == null && clientId != null && !clientId.isEmpty() && clientSecret != null
				&& !clientSecret.isEmpty())
			client = new ClarifaiBuilder(clientId, clientSecret).client(new OkHttpClient()).buildSync();
		return client;
	}

	public void disposeClient() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	public void logInfo(String msg) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, msg));
	}

	public void logError(String msg, Throwable t) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, t));
	}

	public Theme getTheme() {
		Theme theme = null;
		String s = getPreferenceStore().getString(PreferenceConstants.THEME);
		if (s != null)
			theme = CoreActivator.getDefault().getThemes().get(s);
		if (theme == null)
			theme = CoreActivator.getDefault().getCurrentTheme();
		return theme;
	}

	public String getModelId() {
		String themeId = getTheme().getId();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "model"); //$NON-NLS-1$
		for (IExtension extension : extensionPoint.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements())
				if (themeId.equals(conf.getAttribute("themeId"))) //$NON-NLS-1$
					return conf.getAttribute("id"); //$NON-NLS-1$
		return null;
	}

	public boolean isMultilingual(String modelId) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "model"); //$NON-NLS-1$
		for (IExtension extension : extensionPoint.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements())
				if (modelId.equals(conf.getAttribute("themeId"))) //$NON-NLS-1$
					return Boolean.parseBoolean(conf.getAttribute("multilingual")); //$NON-NLS-1$
		return false;
	}

}
