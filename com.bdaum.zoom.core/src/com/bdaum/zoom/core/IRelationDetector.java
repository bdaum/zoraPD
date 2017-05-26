/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.core;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;

public interface IRelationDetector {

	/**
	 * Sets the unique ID of the detector
	 * @param id - unique ID
	 */
	void setId(String id);

	/**
	 * Sets the user readable name of the detector
	 * @param name - user readable name
	 */
	void setName(String name);

	/**
	 * Sets the detectors description
	 * @param description
	 */
	void setDescription(String description);

	/**
	 * Returns the detectors description
	 * @return description
	 */
	String getDescription();

	/**
	 * Returns the detectors name
	 * @return - name
	 */
	String getName();

	/**
	 * Returns the detectors unique ID
	 * @return ID
	 */
	String getId();

	/**
	 * Detects if there is an original file from which the given file is derived.
	 * @param uri - URI of given file
	 * @param isDng - true if the file is a DNG file
	 * @param isRaw - true if the file is a RAW file
	 * @param ensemble - Asset ensemble belonging to the given file
	 * @param toBeDeleted - add objects to be deleted from the database here
	 * @param toBeStored - add objects to be stored in the database here
	 * @return URI of found original file or null
	 */
	URI detectRelation(String uri, boolean isDng, boolean isRaw,
			AssetEnsemble ensemble, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored);

	/**
	 * Resets the detector prior to a series of calls.
	 */
	void reset();

	/**
	 * A helper method to adapt derived or original files when an image is renamed
	 * @param asset - asset to be renamed
	 * @param source - source location
	 * @param target - target location
	 * @param adaptable - An adaptable delivering at least a Shell
	 * @param opId - ID of calling operation, can be used for silencing the file monitor
	 * @return - true if all derived or original images have been updated
	 */
	boolean renameAsset(Asset asset, File source, File target,
			IAdaptable adaptable, String opId);

	/**
	 * A helper method to adapt derived or original files when an image is moved to another location
	 * @param asset - asset to be moved
	 * @param source - source location
	 * @param target - target location
	 * @param adaptable - An adaptable delivering at least a Shell
	 * @param opId - ID of calling operation, can be used for silencing the file monitor
	 * @return - true if all derived or original images have been updated
	 */
	boolean moveAsset(Asset asset, File source, File target,
			IAdaptable adaptable, String opId);

	/**
	 * A helper method to adapt derived or original files when an image is imported into a new folder structure
	 * Note: the method cannot rely on established relation records. It must keep track of imported images.
	 * @param source - source location
	 * @param target - target location
	 * @param adaptable - An adaptable delivering at least a Shell
	 * @param first - true if this is the very first call within a task
	 * @param opId - ID of calling operation, can be used for silencing the file monitor
	 * @return - true if all derived or original images have been updated
	 */
	void transferFile(File source, File target, boolean first, IAdaptable info, String opId);

	/**
	 * Returns a collapse pattern for collapsing images that are related
	 * @return collapse pattern
	 */
	String getCollapsePattern();

}
