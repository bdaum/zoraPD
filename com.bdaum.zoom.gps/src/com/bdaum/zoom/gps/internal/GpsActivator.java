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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.gps.internal;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

@SuppressWarnings("restriction")
public class GpsActivator extends ZUiPlugin {

	private static final String SEARCH_HISTORY = "searchHistory"; //$NON-NLS-1$

	private static final String MAP_COMPONENT = "mapComponent"; //$NON-NLS-1$

	private static final String[] EMPTY = new String[0];

	public static final String PLUGIN_ID = "com.bdaum.zoom.gps"; //$NON-NLS-1$

	private static GpsActivator plugin;

	private String fileLocation;

	private int filterIndex;

	private String[] searchHistory = EMPTY;

	private List<IGeocodingService> namingList;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IDialogSettings dialogSettings = getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(MAP_COMPONENT);
		if (section == null)
			section = dialogSettings.addNewSection(MAP_COMPONENT); 
		String[] hist = section.getArray(SEARCH_HISTORY);
		if (hist != null)
			searchHistory = hist;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (searchHistory != null) {
			IDialogSettings section = getDialogSettings().getSection(MAP_COMPONENT);
			if (section != null)
				section.put(SEARCH_HISTORY, searchHistory);
		}
		plugin = null;
		super.stop(context);
	}

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
		List<String> gpsFileExtensions = UiActivator.getDefault().getGpsFileExtensions();
		String[] result = new String[gpsFileExtensions.size() + 1];
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
		IPreferencesService preferencesService = Platform.getPreferencesService();
		int timeshift = preferencesService.getInt(PLUGIN_ID, PreferenceConstants.TIMESHIFT, 0, null);
		int tolerance = preferencesService.getInt(PLUGIN_ID, PreferenceConstants.TOLERANCE, 60, null);
		boolean edit = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.EDIT, false, null);
		boolean overwrite = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.OVERWRITE, false, null);
		boolean includecoords = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.INCLUDECOORDINATES, false,
				null);
		boolean includenames = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.INCLUDENAMES, true, null);
		boolean updatealtitude = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.UPDATEALTITUDE, false,
				null);
		boolean useWaypoints = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.USEWAYPOINTS, true, null);
		long interval = 3600000L / preferencesService.getInt(PLUGIN_ID, PreferenceConstants.HOURLYLIMIT, 40, null);
		return new GpsConfiguration(timeshift, tolerance, edit, overwrite, useWaypoints, includecoords, includenames,
				QueryField.getKeywordFilter(), updatealtitude, interval);
	}

	public EventTaggingConfiguration createEventTaggingConfiguration() {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		int timeshift = preferencesService.getInt(PLUGIN_ID, PreferenceConstants.TIMESHIFT, 0, null);
		boolean web = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.EVENTTAGGINGWEB, true, null);
		boolean cat = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.EVENTTAGGINGCAT, true, null);
		boolean keywords = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.EVENTTAGGINGKEYWORDS, true,
				null);
		return new EventTaggingConfiguration(timeshift, web, cat, QueryField.getKeywordFilter(), keywords);
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public static IMapComponent getMapComponent(IConfigurationElement conf) {
		if (conf != null) {
			try {
				IMapComponent mc = (IMapComponent) (conf.createExecutableExtension(MAP_COMPONENT));
				String maptype = Platform.getPreferencesService().getString(PLUGIN_ID,
						PreferenceConstants.MAPTYPE + '.' + conf.getAttribute("id"), //$NON-NLS-1$
						null, null);
				mc.setInitialMapType(maptype);
				return mc;
			} catch (CoreException e) {
				getDefault().logError(Messages.getString("GpsActivator.cannot_instantiate_mappingSystem"), e); //$NON-NLS-1$
			}
		}
		return null;
	}

	public static IConfigurationElement findCurrentMappingSystem() {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String id = preferencesService.getString(PLUGIN_ID, PreferenceConstants.MAPPINGSYSTEM, null, null);
		IConfigurationElement first = null;
		IConfigurationElement found = null;
		for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "mappingSystem") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : ext.getConfigurationElements()) {
				if (Boolean.parseBoolean(conf.getAttribute("default"))) //$NON-NLS-1$
					first = conf;
				if (id != null && id.equals(conf.getAttribute("id"))) { //$NON-NLS-1$
					found = conf;
					break;
				}
			}
		if (found == null)
			found = first;
		return found;
	}

	public static IConfigurationElement[] collectMappingSystems() {
		return collectConfigurationElements("mappingSystem"); //$NON-NLS-1$
	}

	private static IConfigurationElement[] collectConfigurationElements(String id) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, id);
		List<IConfigurationElement> list = new ArrayList<IConfigurationElement>();
		for (IExtension ext : extensionPoint.getExtensions())
			for (IConfigurationElement conf : ext.getConfigurationElements())
				list.add(conf);
		return list.toArray(new IConfigurationElement[list.size()]);
	}

	public static void setCurrentMappingSystem(IConfigurationElement conf) {
		BatchUtilities.putPreferences(InstanceScope.INSTANCE.getNode(PLUGIN_ID), PreferenceConstants.MAPPINGSYSTEM,
				conf.getAttribute("id")); //$NON-NLS-1$
	}

	public static void setCurrentMapType(IConfigurationElement conf, String maptype) {
		BatchUtilities.putPreferences(InstanceScope.INSTANCE.getNode(PLUGIN_ID),
				PreferenceConstants.MAPTYPE + '.' + conf.getAttribute("id"), maptype); //$NON-NLS-1$
	}

	public IGeocodingService getNamingService() {
		return getNamingServiceByName(null);
	}

	public IGeocodingService getNamingServiceByName(String name) {
		if (name == null)
			name = getPreferenceStore().getString(PreferenceConstants.NAMINGSERVICE);
		List<IGeocodingService> namingMap = getNamingList();
		int size = namingMap.size();
		for (int i = 0; i < size; i++) {
			IGeocodingService service = namingMap.get(i);
			if (service.getName().equals(name))
				return service;
		}
		for (int i = 0; i < size; i++) {
			IGeocodingService service = namingMap.get(i);
			if (service.isDefault())
				return service;
		}
		return null;
	}

	public IGeocodingService getNamingServiceById(String id) {
		if (id == null)
			id = getPreferenceStore().getString(PreferenceConstants.NAMINGSERVICE);
		List<IGeocodingService> namingMap = getNamingList();
		int size = namingMap.size();
		for (int i = 0; i < size; i++) {
			IGeocodingService service = namingMap.get(i);
			if (service.getId().equals(id))
				return service;
		}
		for (int i = 0; i < size; i++) {
			IGeocodingService service = namingMap.get(i);
			if (service.isDefault())
				return service;
		}
		return null;
	}

	public IGeocodingService[] getNamingServices() {
		Collection<IGeocodingService> values = getNamingList();
		return values.toArray(new IGeocodingService[values.size()]);
	}

	private List<IGeocodingService> getNamingList() {
		if (namingList == null) {
			namingList = new ArrayList<>(3);
			for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "geonamingService") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement conf : ext.getConfigurationElements()) {
					String name = conf.getAttribute("name"); //$NON-NLS-1$
					try {
						IGeocodingService service = (IGeocodingService) conf.createExecutableExtension("class"); //$NON-NLS-1$
						service.setId(conf.getAttribute("id")); //$NON-NLS-1$
						service.setName(name);
						service.setDefault(Boolean.parseBoolean(conf.getAttribute("default"))); //$NON-NLS-1$
						service.setLink(conf.getAttribute("link")); //$NON-NLS-1$
						service.setDescription(conf.getAttribute("description")); //$NON-NLS-1$
						for (IConfigurationElement child : conf.getChildren())
							service.addParameter(new IGeocodingService.Parameter(child.getAttribute("id"), //$NON-NLS-1$
									child.getAttribute("label"), child.getAttribute("reqMsg"), //$NON-NLS-1$ //$NON-NLS-2$
									child.getAttribute("hint"), child.getAttribute("tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
									child.getAttribute("explanation"))); //$NON-NLS-1$
						namingList.add(service);
					} catch (CoreException e) {
						logError(NLS.bind(Messages.getString("GpsActivator.cannot_create_geonaming_service"), name), e); //$NON-NLS-1$
					}
				}
		}
		return namingList;
	}

	public String[] getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(String[] searchHistory) {
		this.searchHistory = searchHistory;
	}

}
