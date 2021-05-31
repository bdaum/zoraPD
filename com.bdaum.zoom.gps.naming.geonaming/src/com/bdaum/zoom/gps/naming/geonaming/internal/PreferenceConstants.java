/*******************************************************************************
 * Copyright (c) 2014-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;

public class PreferenceConstants implements IPreferenceConstants {
	public static final String GEONAMESPARMS = "com.bdaum.zoom.gps.geoNamesParms"; //$NON-NLS-1$
	public static final String SEARCHPARAMETERS = "com.bdaum.zoom.gps.geoNamesSearchParms"; //$NON-NLS-1$
	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}
	@Override
	public List<Object> getChildren(Object group) {
		if (group == com.bdaum.zoom.gps.internal.preferences.PreferenceConstants.GEO)
			return Arrays.asList(GEONAMESPARMS, SEARCHPARAMETERS);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(GeonamingActivator.PLUGIN_ID);
	}

}
