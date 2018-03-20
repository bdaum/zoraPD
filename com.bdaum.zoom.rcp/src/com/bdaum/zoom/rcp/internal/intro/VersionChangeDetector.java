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
package com.bdaum.zoom.rcp.internal.intro;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IntroContentDetector;
import org.osgi.framework.Version;

import com.bdaum.zoom.rcp.internal.RcpActivator;
import com.bdaum.zoom.rcp.internal.PreferenceConstants;

public class VersionChangeDetector extends IntroContentDetector {

	@Override
	public boolean isNewContentAvailable() {
		boolean versionChange = isVersionChange();
		if (versionChange) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
				if (page != null) {
					IPerspectiveDescriptor currentPerspective = page.getPerspective();
					for (IPerspectiveDescriptor perspective : page.getOpenPerspectives()) {
						page.setPerspective(perspective);
						page.resetPerspective();
					}
					page.setPerspective(currentPerspective);
				}
			}
		}
		return versionChange;
	}

	private static boolean isVersionChange() {
		IPreferenceStore preferenceStore = RcpActivator.getDefault().getPreferenceStore();
		String v = preferenceStore.getString(PreferenceConstants.PREVIOUSVERSION);
		if (v == null || v.isEmpty()) {
			preferenceStore.putValue(PreferenceConstants.PREVIOUSVERSION,
					Platform.getProduct().getDefiningBundle().getVersion().toString());
			return true;
		}
		Version oldVersion = new Version(v);
		Version version = Platform.getProduct().getDefiningBundle().getVersion();
		if (version.getMajor() > oldVersion.getMajor()
				|| (version.getMajor() == oldVersion.getMajor() && version.getMinor() > oldVersion.getMinor())) {
			preferenceStore.putValue(PreferenceConstants.PREVIOUSVERSION, version.toString());
			return true;
		}
		return false;
	}
}
