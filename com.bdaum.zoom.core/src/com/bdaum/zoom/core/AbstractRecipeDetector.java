/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
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
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;

/**
 * A convenience implementation of common recipe detector constructs
 */
public abstract class AbstractRecipeDetector implements IRecipeDetector {

	public static class RecipeParameter implements IRecipeParameter {

		/**
		 * Value descriptor for parameter values
		 *
		 */
		public static class RecipeParameterValue implements
				IRecipeParameterValue {

			private String label;

			private String id;

			/**
			 * Constructor
			 *
			 * @param label
			 *            - parameter value label
			 * @param id
			 *            - parameter value ID
			 */
			public RecipeParameterValue(String label, String id) {
				this.label = label;
				this.id = id;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter.
			 * IRecipeParameterValue#getLabel()
			 */
			public String getLabel() {
				return label != null ? label : id;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter.
			 * IRecipeParameterValue#getId()
			 */
			public String getId() {
				return id;
			}
		}

		private String name;

		private String id;

		private List<IRecipeParameterValue> values = new ArrayList<IRecipeDetector.IRecipeParameter.IRecipeParameterValue>(
				5);

		private String value;

		/**
		 * Constructor
		 *
		 * @param name
		 *            - parameter name
		 * @param id
		 *            - parameter ID
		 * @param dflt
		 *            - parameter default value
		 */
		public RecipeParameter(String name, String id, String dflt) {
			super();
			this.name = name;
			this.id = id;
			value = dflt;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#addValueDescriptor
		 * (com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter.
		 * IRecipeParameterValue)
		 */
		public void addValueDescriptor(IRecipeParameterValue v) {
			values.add(v);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#getValues()
		 */
		public List<IRecipeParameterValue> getValues() {
			return values;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#getLabel(java
		 * .lang.String)
		 */
		public String getLabel(String anId) {
			for (IRecipeParameterValue v : values)
				if (v.getId().equals(anId))
					return v.getLabel();
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#getValue()
		 */
		public String getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter#setValue(java
		 * .lang.String)
		 */
		public void setValue(String value) {
			this.value = value;
		}
	}

	protected static final double DIA35MM = Math.sqrt(36 * 36 + 24 * 24);

	private String name;

	private String id;

	private List<IRecipeParameter> parameters = new ArrayList<IRecipeDetector.IRecipeParameter>(
			2);

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.image.recipe.IRecipeDetector#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.image.recipe.IRecipeDetector#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.image.recipe.IRecipeDetector#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.image.recipe.IRecipeDetector#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRecipeDetector#addParameter(com.bdaum.zoom.core.
	 * IRecipeDetector.IRecipeParameter)
	 */
	public void addParameter(IRecipeParameter parameter) {
		parameters.add(parameter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRecipeDetector#getParameters()
	 */
	public List<IRecipeParameter> getParameters() {
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRecipeDetector#getParameter(java.lang.String)
	 */
	public IRecipeParameter getParameter(String anId) {
		for (IRecipeParameter par : parameters)
			if (par.getId().equals(anId))
				return par;
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRecipeDetector#getParameterValue(java.lang.String)
	 */
	public String getParameterValue(String anId) {
		IRecipeParameter parameter = getParameter(anId);
		return parameter == null ? null : parameter.getValue();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRecipeDetector#getIntegerParameterValue(java.lang
	 * .String)
	 */
	public int getIntegerParameterValue(String anId) {
		String v = getParameterValue(anId);
		if (v != null)
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException e) {
				// ignore
			}
		return -1;
	}

	/**
	 * Common utility method to compute additional folders to be watched and
	 * updates the map that associates watched folders with recipe detectors
	 *
	 * @param watchedFolders
	 *            - folders to be added or removed from the list of currently
	 *            watched folders
	 * @param detectorMap
	 *            - map describing the relationship between folders containing
	 *            metafiles and recipe detectors required to process those
	 *            metafiles when no subfolders are to be considered
	 * @param recursiveDetectorMap
	 *            - map describing the relationship between folders containing
	 *            metafiles and recipe detectors required to process those
	 *            metafiles when also subfolders are to be considered
	 * @param remove
	 *            - true if specified folders shall be removed from the list of
	 *            currently watched folders, false if specified folders shall be
	 *            added to the list of currently watched folders,
	 * @param subPaths
	 *            - an array of subpaths. Each combination of watchedFolder and
	 *            subpath is added to the maps
	 * @return list of folders or files that must be watched in addition to the
	 *         standard watched folders
	 */

	protected List<RecipeFolder> computeWatchedMetaFolders(
			WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap,
			Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean remove, String[] subPaths) {
		List<RecipeFolder> result = new ArrayList<RecipeFolder>();
		for (WatchedFolder wfolder : watchedFolders) {
			String uri = wfolder.getUri();
			if (wfolder.getRecursive())
				try {
					updateWatchedMetaFolders(recursiveDetectorMap, result,
							new File(new URI(uri)), this, remove, true);
				} catch (URISyntaxException e) {
					// dont watch this
				}
			else
				for (int i = 0; i < subPaths.length; i++)
					try {
						File subfolder = new File(new URI(
								subPaths[i].length() > 0 ? uri + '/'
										+ subPaths[i] : uri));
						updateWatchedMetaFolders(detectorMap, result,
								subfolder, this, remove, false);
					} catch (URISyntaxException e) {
						// dont watch this
					}
		}
		return result;
	}

	/**
	 * Utility method to add a detector to a detector map
	 *
	 * @param detectorMap
	 *            - map to update
	 * @param folders
	 *            - list of folders to update
	 * @param folder
	 *            - folder to be added or removed
	 * @param detector
	 *            - associated detector
	 * @param remove
	 *            - true if folder is removed
	 * @param recursive
	 *            - true if folder is watched recursively
	 */
	protected void updateWatchedMetaFolders(
			Map<File, List<IRecipeDetector>> detectorMap,
			List<RecipeFolder> folders, File folder, IRecipeDetector detector,
			boolean remove, boolean recursive) {
		folders.add(new RecipeFolder(folder, recursive));
		if (remove)
			detectorMap.remove(folder);
		else {
			List<IRecipeDetector> list = detectorMap.get(folder);
			if (list == null) {
				list = new ArrayList<IRecipeDetector>(2);
				detectorMap.put(folder, list);
			}
			list.add(detector);
		}
	}

	/**
	 * Utility method convert a string into a float
	 *
	 * @param value
	 *            - string value
	 * @return - float value
	 * @throws ParseException
	 */
	protected float parseFloat(String value) throws ParseException {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new ParseException(value, 0);
		}
	}

	/**
	 * Adds a struct field to the struc description
	 * @param sb - struct description
	 * @param qfield - field spec
	 * @param value - field value
	 */
	protected void addStructField(StringBuilder sb,
			QueryField qfield, Object value) {
		if (sb.length() > 0)
			sb.append(',');
		sb.append(qfield.getExifToolKey()).append('=');
		if (qfield.getCard() == 1)
			sb.append(value);
		else
			sb.append('[').append(value).append(']');
	}

	/**
	 * Completes a struct description and stores it in a map
	 * @param overlayMap - receiving map
	 * @param qfield - field spec
	 * @param sb - struct description
	 */
	protected void setStruct(Map<String, String> overlayMap,
			QueryField qfield, StringBuilder sb) {
		if (sb.length() > 0) {
			sb.insert(0, '{').append('}');
			overlayMap.put(qfield.getExifToolKey(), sb.toString());
		}
	}


}
