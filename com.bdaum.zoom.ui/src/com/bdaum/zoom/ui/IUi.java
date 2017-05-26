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
 * (c) 2009-2013 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.gps.IGpsParser;
import com.bdaum.zoom.ui.gps.IWaypointCollector;

/**
 * Frontend for UI services and utilities
 * This interface should not be implemented.
 *
 */
public interface IUi {

	/**
	 * Returns a navigation history instance
	 *
	 * @param window
	 *            - workbench window that owns the history instance
	 * @return - navigation history instance
	 */
	INavigationHistory getNavigationHistory(IWorkbenchWindow window);

	/**
	 * Returns the dialog settings with the specified ID
	 *
	 * @param id
	 *            - settings ID
	 * @return - settings instance
	 */
	IDialogSettings getDialogSettings(String id);

	/**
	 * Returns the current display CMS settings see -
	 * com.bdaum.zoom.image.ImageConstants.NOCMS, -
	 * com.bdaum.zoom.image.ImageConstants.SRGB, -
	 * com.bdaum.zoom.image.ImageConstants.ARGB
	 *
	 * @return - current display CMS settings
	 */
	int getDisplayCMS();

	/**
	 * Returns a GPS file parser for the specified file
	 *
	 * @param file
	 *            - GPS file
	 * @return GPS parser or null if file format is not supported
	 */
	IGpsParser getGpsParser(File file);

	/**
	 * Returns a GPS waypoint collector for the specified file
	 *
	 * @param file
	 *            - GPS file
	 * @return GPS waypoint collector or null if no waypoint collector was supplied for the file format
	 */
	IWaypointCollector getWaypointCollector(File file);


	/**
	 * Adds a preference listener for UI preferences
	 *
	 * @param preferenceListener
	 */
	void addPreferenceChangeListener(
			IPreferenceChangeListener preferenceListener);

	/**
	 * Removes a preference listener for UI preferences
	 *
	 * @param preferenceListener
	 */
	void removePreferenceChangeListener(
			IPreferenceChangeListener preferenceListener);

	/**
	 * Plays the sound file with the specified name
	 *
	 * @param sound
	 *            - sound file name (without extension)
	 * @param prefKey
	 *            - a preference key that determines if a sound is played or
	 *            not, or null
	 */
	void playSound(String sound, String prefKey);

	/**
	 * Writes an error entry into the system log
	 * @param message - error message
	 * @param e - error cause or null
	 */
	void logError(String message, Throwable e);

	/**
	 * Returns a list of image assets derived from a the currently opened presentation editor
	 * (slideshow, exhibition, webgallery)
	 * @return list of image assets
	 */
	List<Asset> getAssetsFromPresentation();
	/**
	 * Returns the name of the currently opened presentation editor
	 * (slideshow, exhibition, webgallery)
	 * @return presentation name
	 */
	String getPresentationName();

	/**
	 * Returns a list of presentation items
	 * @return presentation items
	 */
	List<AomObject> getPresentationItems();

	/**
	 * Extracts assets from presentation items
	 * @param presentationItems - presentation items
	 * @param pruneNonAssets - if true, items not containing an asset are removed from the input collection
	 * @return - list of extracted assets
	 */
	List<Asset> getAssetsFromPresentationItems(Collection<AomObject> presentationItems,
			boolean pruneNonAssets);

	/**
	 * Tests if the applications workbench window is active
	 * @return true if the applications workbench window is active
	 * @deprecated - use static method Ui.isWorkbenchActive();
	 */
	@Deprecated
	boolean isWorkbenchActive();

}