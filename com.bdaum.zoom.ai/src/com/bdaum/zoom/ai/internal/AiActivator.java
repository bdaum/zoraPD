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
package com.bdaum.zoom.ai.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.ai.internal.preference.PreferenceConstants;
import com.bdaum.zoom.ai.internal.services.IAiServiceProvider;
import com.bdaum.zoom.ai.internal.translator.TranslatorClient;
import com.bdaum.zoom.core.internal.lire.AiAlgorithm;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings({ "restriction" })
public class AiActivator extends ZUiPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.ai"; //$NON-NLS-1$

	// The shared instanceO
	private static AiActivator plugin;

	private Map<String, IAiServiceProvider> providerMap;

	private TranslatorClient client;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

	}

	public TranslatorClient getClient() {
		if (client == null) {
			client = new TranslatorClient();
			String translatorKey = getPreferenceStore().getString(PreferenceConstants.TRANSLATORKEY);
			if (translatorKey != null && !translatorKey.isEmpty())
				client.setKey(translatorKey);
		}
		return client;
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
	public static AiActivator getDefault() {
		return plugin;
	}

	public IAiServiceProvider getServiceProvider(String serviceId) {
		getAiServiceProviders();
		if (serviceId == null) {
			String label = getPreferenceStore().getString(PreferenceConstants.ACTIVEPROVIDER);
			if (label != null)
				for (IAiServiceProvider provider : providerMap.values())
					if (label.equals(provider.getName()))
						return provider;
			serviceId = "*"; //$NON-NLS-1$
		}
		IAiServiceProvider provider = providerMap.get(serviceId);
		if (provider == null && !providerMap.isEmpty())
			for (IAiServiceProvider p : providerMap.values())
				return p;
		return provider;
	}

	public IAiServiceProvider[] getServiceProviders() {
		getAiServiceProviders();
		Collection<IAiServiceProvider> values = providerMap.values();
		return values.toArray(new IAiServiceProvider[values.size()]);
	}

	private void getAiServiceProviders() {
		if (providerMap == null) {
			providerMap = new HashMap<>();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"serviceProvider"); //$NON-NLS-1$
			for (IExtension ext : extensionPoint.getExtensions()) {
				String namespaceIdentifier = ext.getNamespaceIdentifier();
				for (IConfigurationElement config : ext.getConfigurationElements()) {
					String id = config.getAttribute("id"); //$NON-NLS-1$
					String name = config.getAttribute("name"); //$NON-NLS-1$
					try {
						AbstractAiServiceProvider provider = (AbstractAiServiceProvider) config
								.createExecutableExtension("class"); //$NON-NLS-1$
						provider.setId(id);
						provider.setName(name);
						provider.setLatency(getInt(config, "latency", 3000)); //$NON-NLS-1$
						providerMap.put(id, provider);
						List<String> modelIds = new ArrayList<>(5);
						List<String> modelLabels = new ArrayList<>(5);
						for (IConfigurationElement ratingModel : config.getChildren("ratingModel")) { //$NON-NLS-1$
							modelIds.add(ratingModel.getAttribute("id")); //$NON-NLS-1$
							modelLabels.add(ratingModel.getAttribute("label")); //$NON-NLS-1$
						}
						provider.setRatingModelIds(modelIds.toArray(new String[modelIds.size()]));
						provider.setRatingModelLabels(modelLabels.toArray(new String[modelIds.size()]));
						List<AiAlgorithm> algorithms = new ArrayList<>(5);
						for (IConfigurationElement feature : config.getChildren("feature")) //$NON-NLS-1$
							algorithms.add(new AiAlgorithm(getInt(feature, "id", -1), feature.getAttribute("name"), //$NON-NLS-1$ //$NON-NLS-2$
									feature.getAttribute("label"), feature.getAttribute("description"),  //$NON-NLS-1$//$NON-NLS-2$
									Boolean.parseBoolean(feature.getAttribute("essential")), namespaceIdentifier)); //$NON-NLS-1$
						provider.setFeatures(algorithms.toArray(new AiAlgorithm[algorithms.size()]));
					} catch (CoreException e) {
						logError(NLS.bind(Messages.AiActivator_error_loading_provider, name), e);
					}
				}
			}
		}
	}

	protected int getInt(IConfigurationElement config, String att, int dflt) {
		try {
			String attribute = config.getAttribute(att);
			if (attribute != null && !attribute.isEmpty())
				return Integer.parseInt(attribute);
		} catch (NumberFormatException e) {
			// fall through
		}
		return dflt;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public void disposeClient() {
		client = null;
	}

}
