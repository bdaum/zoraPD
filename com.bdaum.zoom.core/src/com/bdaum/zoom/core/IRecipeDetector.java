/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
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
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.DiskFullException;

/**
 * This interface describes recipe detectors that are able to detect and load
 * recipes as produced by various RAW converters
 *
 */
public interface IRecipeDetector {

	/**
	 * ID for XMP priority parameter
	 */
	String XMPEMBEDDED = "com.bdaum.zoom.recipe.xmpEmbedded"; //$NON-NLS-1$

	/**
	 * Descriptor for watched recipe folders
	 *
	 */
	public class RecipeFolder {
		public File file;
		public boolean recursive;

		/**
		 * Constructor
		 *
		 * @param file
		 *            - folder file
		 * @param recursive
		 *            - true if also subfolders are watched
		 */
		public RecipeFolder(File file, boolean recursive) {
			super();
			this.file = file;
			this.recursive = recursive;
		}
	}

	/**
	 * Optional recipe parameter
	 *
	 */
	public interface IRecipeParameter {

		/**
		 * Value descriptor for parameter values
		 *
		 */
		public interface IRecipeParameterValue {
			/**
			 * Returns the label of a parameter value
			 *
			 * @return label
			 */
			String getLabel();

			/**
			 * Returns the ID of a parameter value
			 *
			 * @return ID
			 */
			String getId();
		}

		/**
		 * Returns the name of a parameter
		 *
		 * @return
		 */
		String getName();

		/**
		 * Returns the ID of a parameter
		 *
		 * @return ID
		 */
		String getId();

		/**
		 * Returns the value of the parameter
		 *
		 * @return value
		 */
		String getValue();

		/**
		 * Sets the value of the parameter
		 *
		 * @param value
		 *            - value to set
		 */
		void setValue(String value);

		/**
		 * Adds a descriptor for a possible value to the parameter
		 *
		 * @param value
		 *            - value descriptor
		 */
		void addValueDescriptor(IRecipeParameterValue value);

		/**
		 * Returns a list of all registered value descriptors
		 *
		 * @return - list of value descriptors
		 */
		List<IRecipeParameterValue> getValues();

		/**
		 * Returns the value of the parameter description with the specified ID
		 * or null
		 *
		 * @param id
		 *            - parameter description ID
		 * @return - value of parameter description or null
		 */
		String getLabel(String id);
	}

	/**
	 * Checks if the proprietary recipe is contained in an XMP sidecar
	 *
	 * @param uri
	 *            - URI of image to which the recipe belongs
	 *
	 * @return -1 if the proprietary recipe is not contained in an XMP sidecar
	 *         positive number if the proprietary recipe is contained in an XMP
	 *         sidecar detectors with smaller numbers win over detectors with
	 *         larger numbers
	 */
	int isRecipeXMPembbedded(String uri);

	/**
	 * Checks if the proprietary recipe is contained in the image file
	 *
	 * @param uri
	 *            - URI of image to which the recipe belongs
	 *
	 * @return -1 if the proprietary recipe is not contained in an XMP sidecar
	 *         positive number if the proprietary recipe is contained in an XMP
	 *         sidecar detectors with smaller numbers win over detectors with
	 *         larger numbers
	 */
	int isRecipeEmbbedded(String uri);

	/**
	 * Detect the modification of a proprietary recipe for a given image
	 *
	 * @param uri
	 *            - URI of image to which the recipe belongs
	 * @return - timestamp of recipe belonging to the image with the given URI
	 */
	long getRecipeModificationTimestamp(String uri);

	/**
	 * Load a proprietary recipe format and deliver it as a normalized IRecipe
	 * implementation
	 *
	 * @param uri
	 *            - URI of image to which the recipe belongs
	 * @param highres
	 *            - true if used for a high resolution image, false for a
	 *            thumbnail
	 * @param focalLengthProvider
	 *            - provides the 35 mm focal length
	 * @param overlayMap
	 *            - a map in which EXIF and IPTC values provided by the recipe
	 *            can be put. Maybe null.
	 * @return - normalized recipe or null if no recipe exists
	 */
	Recipe loadRecipeForImage(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap);

	/**
	 * Load a proprietary recipe and deliver it as a normalized IRecipe
	 * implementation
	 *
	 * @param uri
	 *            - URI of recipe file
	 * @param highres
	 *            - true if used for a high resolution image, false for a
	 *            thumbnail
	 * @param focalLengthProvider
	 *            - provides the 35 mm focal length
	 * @param overlayMap
	 *            - a map in which EXIF and IPTC values provided by the recipe
	 *            can be put. Maybe null.
	 * @return - normalized recipe or null if no recipe exists
	 */
	Recipe loadRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap);

	/**
	 * Returns the name of the processor
	 *
	 * @return name of processor
	 */
	String getName();

	/**
	 * Sets the name of the processor
	 *
	 * @param name
	 *            - name of the processor
	 */
	void setName(String name);

	/**
	 * Sets the ID of the processor
	 *
	 * @param id
	 *            - ID of the processor
	 */
	void setId(String id);

	/**
	 * Returns the ID of the processor
	 *
	 * @return ID of processor
	 */
	String getId();

	/**
	 * Computes additional folders to be watched and updates the map that
	 * associates watched folders with recipe detectors
	 *
	 * @param watchedFolders
	 *            - folders to be added or removed from the list of currently
	 *            watched folders
	 * @param detectorMap
	 *            - map describing the relationship between folders containing
	 *            metafiles and recipe detectors required to process those
	 *            metafiles (not required for XMP metafiles) when no subfolders
	 *            are to be considered
	 * @param recursiveDetectorMap
	 *            - map describing the relationship between folders containing
	 *            metafiles and recipe detectors required to process those
	 *            metafiles (not required for XMP metafiles) when also
	 *            subfolders are to be considered
	 * @param update
	 *            - true if folders are only updated
	 * @param remove
	 *            true if specified folders shall be removed from the list of
	 *            currently watched folders, false if specified folders shall be
	 *            added to the list of currently watched folders,
	 * @return list of folders or files that must be watched in addition to the
	 *         standard watched folders (For XMP metafiles only required in case
	 *         of incremental update)
	 */
	List<RecipeFolder> computeWatchedMetaFilesOrFolders(
			WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap,
			Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean update, boolean remove);

	/**
	 * Returns the image file belonging to the supplied meta file
	 *
	 * @param metaFile
	 *            - meta file
	 * @param watchedFolders
	 *            - watched folders that may contain the image file
	 * @return - associated image file or null
	 */
	File getChangedImageFile(File metaFile, WatchedFolder[] watchedFolders);

	/**
	 * Return true if meta files are updated incrementally by the supported
	 * third party application
	 *
	 * @return - true in case of incremental update
	 */
	boolean usesIncrementalUpdate();

	/**
	 * Adds an optional parameter
	 *
	 * @param parameter
	 *            - new parameter
	 */
	void addParameter(IRecipeParameter parameter);

	/**
	 * Returns the list of registered parameters
	 *
	 * @return - list of parameters
	 */
	List<IRecipeParameter> getParameters();

	/**
	 * Returns the parameter with the specified ID or null
	 *
	 * @param id
	 *            - parameter ID
	 * @return parameter or null
	 */
	IRecipeParameter getParameter(String id);

	/**
	 * Returns value of the parameter with the specified ID or null
	 *
	 * @param id
	 *            - parameter ID
	 * @return parameter value or null
	 */
	String getParameterValue(String id);

	/**
	 * Returns the integer value of the parameter with the specified ID or -1
	 *
	 * @param id
	 *            - parameter ID
	 * @return integer parameter value or -1
	 */
	int getIntegerParameterValue(String id);

	/**
	 * Retrieve active recipe files for given image URI
	 *
	 * @param uri
	 *            - image uri
	 * @return - active recipe files
	 */
	public abstract File[] getMetafiles(String uri);

	/**
	 * Copies all relevant recipes to the target folder
	 *
	 * @param targetFolder
	 *            - target folder
	 * @param oldUri
	 *            - old image URI
	 * @param newUri
	 *            - new image URI
	 * @param readOnly
	 *            - true if copied recipes shall be set to readonly
	 * @throws DiskFullException
	 * @throws IOException
	 */
	void archiveRecipes(File targetFolder, String oldUri, String newUri,
			boolean readOnly) throws IOException, DiskFullException;

}
