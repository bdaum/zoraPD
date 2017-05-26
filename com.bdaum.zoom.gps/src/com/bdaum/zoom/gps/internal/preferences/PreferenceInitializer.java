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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.bdaum.zoom.gps.internal.GpsActivator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE
				.getNode(GpsActivator.PLUGIN_ID);
		// get local time shift to UTC
		SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s"); //$NON-NLS-1$
		Date now = new Date();
		String today = DF.format(now);
		DF.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
		try {
			Date utc = DF.parse(today);
			long diff = now.getTime() - utc.getTime();
			diff += diff < 0 ? -30000L : 30000L;
			node.putInt(PreferenceConstants.TIMESHIFT, (int) (diff / 60000L));
		} catch (ParseException e) {
			// should never happen
		}

		node.putInt(PreferenceConstants.TOLERANCE, 60);
		node.putBoolean(PreferenceConstants.UPDATEALTITUDE, true);
		node.putBoolean(PreferenceConstants.INCLUDENAMES, true);
		node.putBoolean(PreferenceConstants.USEWAYPOINTS, true);
		node.put(PreferenceConstants.NAMINGSERVICE,
				PreferenceConstants.GEONAMES);
		node.putBoolean(PreferenceConstants.EVENTTAGGINGCAT, true);
		node.putBoolean(PreferenceConstants.EVENTTAGGINGWEB, true);
		node.putBoolean(PreferenceConstants.EVENTTAGGINGKEYWORDS, true);
		node.putInt(PreferenceConstants.HOURLYLIMIT, 40);
	}

}
