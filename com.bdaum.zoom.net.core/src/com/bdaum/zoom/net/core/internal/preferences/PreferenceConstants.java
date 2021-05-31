/* Copyright 2009 Berthold Daum

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.bdaum.zoom.net.core.internal.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.net.core.internal.Activator;

public class PreferenceConstants implements IPreferenceConstants {

	public static final String FTPACCOUNTS = "com.bdaum.zoom.ftpAccounts"; //$NON-NLS-1$
	
	public static final String FTP = "FTP"; //$NON-NLS-1$

	@Override
	public List<Object> getRootElements() {
		return Arrays.asList(PreferenceRegistry.INTERNET);
	}

	@Override
	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.INTERNET)
			return Arrays.asList(FTP);
		if (group == FTP)
			return Arrays.asList(FTPACCOUNTS);
		return Collections.emptyList();
	}
	
	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}


}
