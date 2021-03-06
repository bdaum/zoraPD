/*******************************************************************************
 * Copyright (c) 2015-2021 Berthold Daum.
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

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.batch.internal.BatchActivator;

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
	private int secondaryId;

	public void setId(String id) {
		this.id = id;
	}
	
	public void setSecondaryId(int id) {
		this.secondaryId = id;
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

	public String getId() {
		return id;
	}
	
	public int getSecondaryId() {
		return secondaryId;
	}


	public String getName() {
		return name;
	}

	public String getExecutable() {
		return executable;
	}

	public boolean isDetectors() {
		return detectors;
	}

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

	public String isValid() {
		String p = getPath();
		return testExecutable(p != null && !p.isEmpty() ? new File(p) : null);
	}
	
	@Override
	public String testExecutable(File f) {
		if (f == null)
			return Messages.getString("AbstractRawConverter.no_converter_specified"); //$NON-NLS-1$
		if (!f.exists())
			return NLS.bind(Messages.getString("AbstractRawConverter.converter_does_not_exist"), f); //$NON-NLS-1$
		return null;
	}
	
	@Override
	public void unget() {
		BatchActivator.getDefault().ungetRawConverter(this);
	}
	
	@Override
	public String getLibPath() {
		return null;
	}

}