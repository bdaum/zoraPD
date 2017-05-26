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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.gps.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.gps.geonames.IGeonamingService;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.gps.widgets.IMapComponent;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class GpsActivator extends ZUiPlugin {

	private static final String[] EMPTY = new String[0];

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.gps"; //$NON-NLS-1$

	// The shared instance
	private static GpsActivator plugin;

	public static int MAXHISTORYLENGTH = 8;

	private String fileLocation;

	private int filterIndex;

	private String[] searchHistory = EMPTY;

	/**
	 * The constructor
	 */
	public GpsActivator() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GpsActivator getDefault() {
		return plugin;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public int getFilterIndex() {
		return filterIndex;
	}

	public String[] getSupportedGpsFileExtensions() {
		List<String> gpsFileExtensions = UiActivator.getDefault()
				.getGpsFileExtensions();
		String[] result = new String[gpsFileExtensions.size()+1];
		int i = 0;
		for (String ext : gpsFileExtensions)
			result[i++] = '.' + ext;
		result[i] = "*.*"; //$NON-NLS-1$
		return result;
	}

	public String[] getSupportedGpsFileNames() {
		List<String> gpsFileTypes = UiActivator.getDefault().getGpsFileTypes();
		gpsFileTypes.add(Messages.getString("GpsActivator.All_files")); //$NON-NLS-1$
		return gpsFileTypes.toArray(new String[gpsFileTypes.size()]);
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void setFilterIndex(int filterIndex) {
		this.filterIndex = filterIndex;
	}

	public GpsConfiguration createGpsConfiguration() {
		IPreferencesService preferencesService = Platform
				.getPreferencesService();
		int timeshift = preferencesService.getInt(PLUGIN_ID,
				PreferenceConstants.TIMESHIFT, 0, null);
		int tolerance = preferencesService.getInt(PLUGIN_ID,
				PreferenceConstants.TOLERANCE, 60, null);
		boolean edit = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.EDIT, false, null);
		boolean overwrite = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.OVERWRITE, false, null);
		boolean includecoords = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.INCLUDECOORDINATES, false, null);
		boolean includenames = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.INCLUDENAMES, true, null);
		boolean updatealtitude = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.UPDATEALTITUDE, false, null);
		boolean useWaypoints = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.USEWAYPOINTS, true, null);
		long interval = 3600000L / preferencesService.getInt(PLUGIN_ID,
				PreferenceConstants.HOURLYLIMIT, 40, null);
		return new GpsConfiguration(timeshift, tolerance, edit, overwrite,
				useWaypoints, includecoords, includenames,
				QueryField.getKeywordFilter(), updatealtitude, interval);
	}

	public EventTaggingConfiguration createEventTaggingConfiguration() {
		IPreferencesService preferencesService = Platform
				.getPreferencesService();
		int timeshift = preferencesService.getInt(PLUGIN_ID,
				PreferenceConstants.TIMESHIFT, 0, null);
		boolean web = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.EVENTTAGGINGWEB, true, null);
		boolean cat = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.EVENTTAGGINGCAT, true, null);
		boolean keywords = preferencesService.getBoolean(PLUGIN_ID,
				PreferenceConstants.EVENTTAGGINGKEYWORDS, true, null);
		return new EventTaggingConfiguration(timeshift, web, cat,
				QueryField.getKeywordFilter(), keywords);
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public static IMapComponent getMapComponent(IConfigurationElement conf) {
		if (conf != null) {
			try {
				IMapComponent mc = (IMapComponent) (conf
						.createExecutableExtension("mapComponent")); //$NON-NLS-1$
				String maptype = Platform.getPreferencesService().getString(
						PLUGIN_ID,
						PreferenceConstants.MAPTYPE + '.'
								+ conf.getAttribute("id"), //$NON-NLS-1$
						null, null);
				mc.setInitialMapType(maptype);
				return mc;
			} catch (CoreException e) {
				getDefault()
						.logError(
								Messages.getString("GpsActivator.cannot_instantiate_mappingSystem"), e); //$NON-NLS-1$
			}
		}
		return null;
	}

	public static IConfigurationElement findCurrentMappingSystem() {
		IPreferencesService preferencesService = Platform
				.getPreferencesService();
		String id = preferencesService.getString(PLUGIN_ID,
				PreferenceConstants.MAPPINGSYSTEM, null, null);
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(PLUGIN_ID, "mappingSystem"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		IConfigurationElement first = null;
		IConfigurationElement found = null;
		for (IExtension ext : extensions) {
			for (IConfigurationElement conf : ext.getConfigurationElements()) {
				if (Boolean.parseBoolean(conf.getAttribute("default"))) //$NON-NLS-1$
					first = conf;
				if (id != null && id.equals(conf.getAttribute("id"))) { //$NON-NLS-1$
					found = conf;
					break;
				}
			}
		}
		if (found == null)
			found = first;
		return found;
	}

	public static IConfigurationElement[] collectMappingSystems() {
		return collectConfigurationElements("mappingSystem"); //$NON-NLS-1$
	}

	private static IConfigurationElement[] collectConfigurationElements(
			String id) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(PLUGIN_ID, id);
		List<IConfigurationElement> list = new ArrayList<IConfigurationElement>();
		for (IExtension ext : extensionPoint.getExtensions())
			for (IConfigurationElement conf : ext.getConfigurationElements())
				list.add(conf);
		return list.toArray(new IConfigurationElement[list.size()]);
	}

	public static void setCurrentMappingSystem(IConfigurationElement conf) {
		BatchUtilities.putPreferences(
				InstanceScope.INSTANCE.getNode(PLUGIN_ID),
				PreferenceConstants.MAPPINGSYSTEM, conf.getAttribute("id")); //$NON-NLS-1$
	}

	public static void setCurrentMapType(IConfigurationElement conf,
			String maptype) {
		BatchUtilities
				.putPreferences(
						InstanceScope.INSTANCE.getNode(PLUGIN_ID),
						PreferenceConstants.MAPTYPE + '.'
								+ conf.getAttribute("id"), maptype); //$NON-NLS-1$
	}

	public IGeonamingService getNamingService() {
		String namingService = getPreferenceStore().getString(
				PreferenceConstants.NAMINGSERVICE);
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(PLUGIN_ID, "geonamingService"); //$NON-NLS-1$
		for (IExtension ext : extensionPoint.getExtensions())
			for (IConfigurationElement conf : ext.getConfigurationElements()) {
				if (conf.getAttribute("id").equals(namingService)) //$NON-NLS-1$
					try {
						return (IGeonamingService) conf
								.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						logError(
								Messages.getString("GpsActivator.cannot_create_geonaming_service"), e); //$NON-NLS-1$
					}
			}
		return null;
	}

	public static IConfigurationElement[] getPremiumServices() {
		return collectConfigurationElements("premiumService"); //$NON-NLS-1$
	}

	public static IConfigurationElement[] getNamingServices() {
		return collectConfigurationElements("geonamingService"); //$NON-NLS-1$
	}

	public String[] getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(String[] searchHistory) {
		this.searchHistory = searchHistory;
	}
}
