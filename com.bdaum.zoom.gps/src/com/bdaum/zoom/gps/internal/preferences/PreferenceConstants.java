/*******************************************************************************
 * Copyright (c) 2009-2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.gps.internal.GpsActivator;

public class PreferenceConstants implements IPreferenceConstants {
	public static final String TIMESHIFT = "com.bdaum.zoom.gps.timeshift"; //$NON-NLS-1$
	public static final String TOLERANCE = "com.bdaum.zoom.gps.tolerance"; //$NON-NLS-1$
	public static final String OVERWRITE = "com.bdaum.zoom.gps.overwrite"; //$NON-NLS-1$
	public static final String INCLUDENAMES = "com.bdaum.zoom.gps.includeNames"; //$NON-NLS-1$
	public static final String INCLUDECOORDINATES = "com.bdaum.zoom.gps.includeCoordinates"; //$NON-NLS-1$
	public static final String USEWAYPOINTS = "com.bdaum.zoom.gps.useWaypoints"; //$NON-NLS-1$
	public static final String UPDATEALTITUDE = "com.bdaum.zoom.gps.updateAltitude"; //$NON-NLS-1$
	public static final String MAPPINGSYSTEM = "com.bdaum.zoom.gps.mappingSystem"; //$NON-NLS-1$
	public static final String MAPTYPE = "com.bdaum.zoom.gps.mapType"; //$NON-NLS-1$
	public static final String NAMINGSERVICE = "com.bdaum.zoom.gps.namingService"; //$NON-NLS-1$
	public static final String GEONAMES = "GeoNames"; //$NON-NLS-1$
	public static final String GOOGLE = "Google"; //$NON-NLS-1$
	public static final String EVENTTAGGINGEMAIL = "com.bdaum.zoom.gps.eventTaggingEmail"; //$NON-NLS-1$
	public static final String EVENTTAGGINGWEB = "com.bdaum.zoom.gps.eventTaggingWeb"; //$NON-NLS-1$
	public static final String EVENTTAGGINGKEYWORDS = "com.bdaum.zoom.gps.eventTaggingKeywords"; //$NON-NLS-1$
	public static final String EVENTTAGGINGCAT = "com.bdaum.zoom.gps.eventTaggingCat"; //$NON-NLS-1$
	public static final String EDIT = "com.bdaum.zoom.gps.editTrackpoints"; //$NON-NLS-1$ ;
	public static final String HOURLYLIMIT = "com.bdaum.zoom.gps.hourlyLimit"; //$NON-NLS-1$ ;
	public static final String NOGO = "com.bdaum.zoom.gps.nogo"; //$NON-NLS-1$ ;
	public static final String GOOGLECLIENTID = "com.bdaum.zoom.gps.googleClientId"; //$NON-NLS-1$

	public static final String GEO = Messages.getString("PreferenceConstants.geo"); //$NON-NLS-1$

	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.PROCESSING)
			return Arrays.asList(GEO);
		if (group == GEO)
			return Arrays.asList(TIMESHIFT, TOLERANCE, OVERWRITE, INCLUDENAMES, INCLUDECOORDINATES, USEWAYPOINTS,
					UPDATEALTITUDE, MAPPINGSYSTEM, MAPTYPE, NAMINGSERVICE, GEONAMES, GOOGLE, EVENTTAGGINGEMAIL,
					EVENTTAGGINGWEB, EVENTTAGGINGKEYWORDS, EVENTTAGGINGCAT, EDIT, HOURLYLIMIT, NOGO, GOOGLECLIENTID);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(GpsActivator.PLUGIN_ID);
	}

}
