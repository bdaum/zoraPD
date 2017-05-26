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
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRawConverter implements IRawConverter {

	private String id;
	private String name;
	private String executable;
	private boolean detectors;
	private boolean isDefault;
	protected List<RawProperty> props = new ArrayList<IRawConverter.RawProperty>(5);
	private String pathId;
	private String path;
	protected File rawFile;
	private String usesRecipes = ""; //$NON-NLS-1$

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public void setDetectors(boolean detectors) {
		this.detectors = detectors;
	}

	public void addProperty(RawProperty prop) {
		props.add(prop);
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return executable
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * @return detectors
	 */
	public boolean isDetectors() {
		return detectors;
	}

	/**
	 * @return isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	public List<RawProperty> getProperties() {
		return props;
	}

	public String getVersionMessage() {
		return null;
	}

	public void setPathId(String pathId) {
		this.pathId = pathId;
	}

	public String getPathId() {
		return pathId;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setUsesRecipes(String usesRecipes) {
		this.usesRecipes = usesRecipes;
	}

	public String getUsesRecipes() {
		return usesRecipes;
	}

	public boolean isValid() {
		String p = getPath();
		if (p == null || p.length() == 0)
			return false;
		return new File(p).exists();
	}

}