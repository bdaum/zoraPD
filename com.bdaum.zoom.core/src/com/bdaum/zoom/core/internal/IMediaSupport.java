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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.core.QueryField;

public interface IMediaSupport {
	/*** Property flags ***/
	/**
	 * Mediatype PHOTO
	 */
	int PHOTO = 1 << 9;
	/**
	 * Mediatype VIDEO
	 */
	int VIDEO = 1 << 10;
	/**
	 * Mediatype AUDIO
	 */
	int AUDIO = 1 << 11;
	/*** Other media types: 1<<12 ... 1<<20 ***/
	/**
	 * Media can appear in exhibitions
	 */
	int EXHIBITION = 1 << 24;
	/**
	 * Media can appear in web galleries
	 */
	int WEBGALLERY = 1 << 25;
	/**
	 * Media can appear in slideshows
	 */
	int SLIDESHOW = 1 << 26;
	/**
	 * Media is in PDF format
	 */
	int PDF = 1 << 27;
	/**
	 * Media is in KML format
	 */
	int KML = 1 << 28;

	int IMPORTWORKSTEPS = 6;

	/**
	 * Imports a file into the catalog
	 *
	 * @param file
	 *            - file to import
	 * @param extension
	 *            - file extension
	 * @param importState
	 *            - import state object
	 * @param aMonitor
	 *            - progress monitor
	 * @param remote
	 *            - remote URI
	 * @return - number of imported objects, negative sign if folder structure
	 *         has changed
	 * @throws Exception
	 */
	int importFile(File file, String extension, ImportState importState,
			IProgressMonitor aMonitor, URI remote) throws Exception;

	/**
	 * Returns the media specific property flags
	 *
	 * @return property flags
	 */
	int getPropertyFlags();

	/**
	 * Tests for the given property flag
	 *
	 * @return - true if the media has the specified property
	 */
	boolean testProperty(int flag);

	/**
	 * Returns a 40x40 media icon
	 *
	 * @return media icon
	 */
	Image getIcon40();

	/**
	 * Returns the media name
	 *
	 * @return media name
	 */
	String getName();

	/**
	 * Returns the plural form
	 *
	 * @return plural form
	 */
	String getPlural();

	/**
	 * Sets the media name
	 *
	 * @param name
	 *            - media name
	 */
	void setName(String name);

	/**
	 * Sets the media plural form
	 *
	 * @param plural
	 *            - media plural form
	 */
	void setPlural(String plural);

	/**
	 * Delivers all valid file extensions for this media type
	 *
	 * @return file extensions
	 */
	String[] getFileExtensions();

	/**
	 * Transfers a specfic media extension from one asset to the other
	 *
	 * @param sourceAsset
	 * @param targetAsset
	 */
	void transferExtension(Asset sourceAsset, Asset targetAsset);

	/**
	 * Resets a specific media extension
	 *
	 * @param asset
	 */
	void resetExtension(Asset asset);

	/**
	 * Sets a field value into the media specific extension
	 *
	 * @param qfield
	 *            - field descriptor
	 * @param asset
	 * @param value
	 * @return true in case of success
	 */
	boolean setFieldValue(QueryField qfield, Asset asset, Object value)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;

	/**
	 * Retrieves the media extension belonging to the given field key
	 *
	 * @param asset
	 *            - asset
	 * @return media extension or null
	 */
	MediaExtension getMediaExtension(Asset asset);

	/**
	 * Tests if the given key points into a specific media extension
	 *
	 * @param key
	 *            - field key
	 * @return true if the given key points into a specific media extension
	 */
	boolean handles(String key);

	/**
	 * Fetch field value from the given extension
	 *
	 * @param qfield
	 *            field descriptor
	 * @param ext
	 *            - extension object
	 * @return field value
	 */
	Object getFieldValue(QueryField qfield, MediaExtension ext)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;

	/**
	 * Returns the class object of the extension class
	 *
	 * @return - extension type
	 */
	Class<? extends MediaExtension> getExtensionType();

	/**
	 * Retrieves the field name from the given key
	 *
	 * @param key
	 *            - field key
	 * @return - field name
	 */
	String getFieldName(String key);

	/**
	 * Resets a bag field
	 *
	 * @param queryField
	 *            - bag field
	 * @param obj
	 *            - object to be modified
	 * @return - return in case of success
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	boolean resetBag(QueryField queryField, Asset obj)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;

	/**
	 * Obtain specific folder for this media type
	 *
	 * @param file
	 *            - DCIM folder
	 * @return - media folder
	 */
	File getMediaFolder(File file);

	/**
	 * Perform specific undo tasks for the given imported asset
	 * @param asset - imported asset
	 * @param toBeStored - database objects to be deleted
	 * @param toBeDeleted - database object to be stored
	 * @return (obsolete)
	 */
	boolean undoImport(Asset asset, Set<Object> toBeDeleted, List<Object> toBeStored);

	/**
	 * Sets the mime map derived from the media extension definition
	 * @param mimeMap
	 */
	void setMimeMap(Map<String, String> mimeMap);

	/**
	 * Sets the collection ID from the media extension definition
	 * @param collectionID
	 */
	void setCollectionId(String collectionID);

}
