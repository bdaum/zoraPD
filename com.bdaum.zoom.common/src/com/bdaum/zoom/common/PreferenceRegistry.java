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
 * (c) 2021 Berthold Daum  
 */
package com.bdaum.zoom.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.BundleContext;

public class PreferenceRegistry extends Plugin {

	public interface IPreferenceConstants {

		/**
		 * Obtain list of root groups
		 * 
		 * @return list of root groups
		 */
		List<Object> getRootElements();

		/**
		 * Obtain child elements. The preference tree has 3 levels: 1. root groups 2.
		 * subgroups 3. keys
		 * 
		 * @param group
		 *            - parent group or subgroup
		 * @return list of child subgroups or keys
		 */
		List<Object> getChildren(Object group);

		/**
		 * @return preference instance node of the respective plugin
		 */
		IEclipsePreferences getNode();
	}

	public static final String PLUGIN_ID = "com.bdaum.zoom.common"; //$NON-NLS-1$

	public static final String UI = Messages.PreferenceRegistry_user_interface;
	public static final Object PROCESSING = Messages.PreferenceRegistry_processing;
	public static final String EXTERNAL_GROUP = Messages.PreferenceRegistry_externals;
	public static final Object INTERNET = Messages.PreferenceRegistry_internet;

	private static PreferenceRegistry plugin;

	private List<IPreferenceConstants> constants;

	public static PreferenceRegistry getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return all root groups
	 */
	public Object[] getRootElements() {
		List<Object> elements = new ArrayList<Object>();
		for (IPreferenceConstants pc : getConstants())
			elements.addAll(pc.getRootElements());
		return elements.toArray();
	}

	/**
	 * @param group - group or subgroup
	 * @return - all subgroups or keys belonging to the specified group or subgroup
	 */
	public Object[] getChildren(Object group) {
		List<Object> elements = new ArrayList<Object>();
		for (IPreferenceConstants pc : getConstants())
			elements.addAll(pc.getChildren(group));
		return elements.toArray();
	}

	/**
	 * @param item - key or subgroup
	 * @return parent group for the specified item
	 */
	public Object getParent(Object item) {
		for (Object rootElement : getRootElements())
			for (Object child : getChildren(rootElement)) {
				if (child.equals(item))
					return rootElement;
				for (Object grandChild : getChildren(child))
					if (grandChild.equals(item))
						return child;
			}
		return null;
	}

	/**
	 * @return all registered IPreferenceConstants modules
	 */
	public List<IPreferenceConstants> getConstants() {
		if (constants == null) {
			IExtension[] extensions = Platform.getExtensionRegistry()
					.getExtensionPoint(PLUGIN_ID, "preferenceConstants") //$NON-NLS-1$
					.getExtensions();
			constants = new ArrayList<IPreferenceConstants>(extensions.length);
			for (IExtension ext : extensions)
				for (IConfigurationElement conf : ext.getConfigurationElements())
					try {
						constants.add((IPreferenceConstants) conf.createExecutableExtension("class")); //$NON-NLS-1$
					} catch (CoreException e) {
						e.printStackTrace();
					}
		}
		return constants;
	}

}
