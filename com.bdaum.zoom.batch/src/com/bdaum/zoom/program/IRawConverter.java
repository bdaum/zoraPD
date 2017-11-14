/*******************************************************************************
 * Copyright (c) 2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.program;

import java.io.File;
import java.util.List;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.image.IRecipeProvider;
import com.bdaum.zoom.image.recipe.Recipe;

public interface IRawConverter extends IConverter, IRecipeProvider {

	class RawProperty {

		public class RawEnum {
			public final String id;
			public final String value;
			public final boolean recipe;

			public RawEnum(String id, String value, boolean recipe) {
				this.id = id;
				this.value = value;
				this.recipe = recipe;
			}
		}

		public String id;
		public String name;
		public String type;
		public String dflt;
		public String value;
		public String min;
		public String max;
		public List<RawEnum> enums;

		public RawProperty(String id, String name, String type, String dflt,
				String min, String max) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.dflt = dflt;
			this.min = min;
			this.max = max;
		}
	}

	public static final int HIGH = 0;
	public static final int MEDIUM = 1;
	public static final int THUMB = 2;

	public static final String NONE = "none"; //$NON-NLS-1$
	public static final String OPTIONAL = "optional"; //$NON-NLS-1$
	public static final String REQUIRED = "required"; //$NON-NLS-1$

	/**
	 * Set id of converter
	 *
	 * @param id
	 */
	void setId(String id);

	/**
	 * Retrieve id of converter
	 *
	 * @return id
	 */
	String getId();

	/**
	 * Set name of converter
	 *
	 * @param name
	 */
	void setName(String name);

	/**
	 * Retrieve name of converter
	 *
	 * @return name
	 */
	String getName();

	/**
	 * Set executable mode (NONE, OPTIONAL, REQUIRED)
	 *
	 * @param executable
	 *            mode
	 */
	void setExecutable(String executable);

	/**
	 * Retrieve executable mode
	 *
	 * @return executable mode
	 */
	String getExecutable();

	/**
	 * Set detector usage
	 *
	 * @param detectors
	 *            - true if converter uses detectors
	 */
	void setDetectors(boolean detectors);

	/**
	 * Get detector usage
	 *
	 * @return true if converter uses detectors
	 */
	boolean isDetectors();

	/**
	 * Adds a property to the list of propertis
	 *
	 * @param prop
	 *            - property to add
	 */
	void addProperty(RawProperty prop);

	/**
	 * Retrieve the list of properties
	 *
	 * @return - list of properties
	 */
	List<RawProperty> getProperties();

	/**
	 * Set the default flag
	 *
	 * @param isDefault
	 *            true if this is the default converter
	 */
	void setDefault(boolean isDefault);

	/**
	 * Retrieve default flag
	 *
	 * @return true if this is the default converter
	 */
	boolean isDefault();

	/**
	 * Retrieve a message with the converter version
	 *
	 * @return - message or null
	 */
	String getVersionMessage();

	/**
	 * Set the executable path id for storing the executable path in the
	 * preference store
	 *
	 * @param path
	 *            id - executable path id
	 */
	void setPathId(String pathId);

	/**
	 * Retrieve the executable path id
	 *
	 * @return executable path id
	 */
	String getPathId();

	/**
	 * Set the executable path
	 *
	 * @param path
	 *            - the executable path
	 */
	void setPath(String path);

	/**
	 * Retrieve the executable path
	 *
	 * @return the executable path
	 */
	String getPath();

	/**
	 * Compute the conversion options
	 * @param rawRecipe - recipe or null
	 * @param options - options (receiver)
	 * @param resolution - resolution (HIGH, MEDIUM, THUMB)
	 * @return scale factor
	 */
	int deriveOptions(Recipe rawRecipe, Options options, int resolution);

	/**
	 * Search for the executable in a folder hierarchy
	 * @param parentFile - root folder or null for unspecified root folder
	 * @return - executable file or null
	 */
	File findModule(File parentFile);

	/**
	 * Set a flag if the converter uses recipes
	 * @param flag - true if the converter uses recipes
	 */
	void setUsesRecipes(String flag);

	/**
	 * Retrieve if the converter uses recipes
	 * @return true if the converter uses recipes
	 */
	String getUsesRecipes();

	/**
	 * Tests if there is a valid executable
	 * @return true if there is a valid executable
	 */
	boolean isValid();

}
