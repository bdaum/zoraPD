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

package com.bdaum.zoom.net.ui.internal.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.net.ui.internal.NetActivator;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String TIMEOUT = "com.bdaum.zoom.timeout"; //$NON-NLS-1$
	
	public static final String GENERAL = Messages.PreferenceConstants_general; 

	@Override
	public List<Object> getRootElements() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.INTERNET)
			return Arrays.asList(GENERAL);
		if (group == GENERAL)
			return Arrays.asList(TIMEOUT);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(NetActivator.PLUGIN_ID);
	}

}
