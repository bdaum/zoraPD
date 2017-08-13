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

package com.bdaum.zoom.rcp.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		// Regions
		colorRegistry.put(Constants.APPCOLOR_REGION_FACE, new RGB(255, 160, 0));
		// Background
		colorRegistry.put("b", new RGB(0, 0, 0)); //$NON-NLS-1$
		colorRegistry.put("d", new RGB(64, 64, 64)); //$NON-NLS-1$
		colorRegistry.put("g", new RGB(240, 240, 248)); //$NON-NLS-1$
		colorRegistry.put("w", new RGB(255, 255, 255)); //$NON-NLS-1$
		colorRegistry.put("p", display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB()); //$NON-NLS-1$
		// Widget text
		colorRegistry.put("b_", new RGB(255, 255, 255)); //$NON-NLS-1$
		colorRegistry.put("d_", new RGB(255, 255, 255)); //$NON-NLS-1$
		colorRegistry.put("g_", new RGB(0, 0, 0)); //$NON-NLS-1$
		colorRegistry.put("w_", new RGB(0, 0, 0)); //$NON-NLS-1$
		colorRegistry.put("p_", display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB()); //$NON-NLS-1$
		// Gallery item text
		colorRegistry.put("b-", new RGB(128, 128, 128)); //$NON-NLS-1$
		colorRegistry.put("d-", new RGB(144, 144, 144)); //$NON-NLS-1$
		colorRegistry.put("g-", new RGB(32, 32, 32)); //$NON-NLS-1$
		colorRegistry.put("w-", new RGB(128, 128, 128)); //$NON-NLS-1$
		colorRegistry.put("p-", display.getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB()); //$NON-NLS-1$
		// Selected gallery item background
		colorRegistry.put("b#", new RGB(128, 128, 128)); //$NON-NLS-1$
		colorRegistry.put("d#", new RGB(144, 144, 144)); //$NON-NLS-1$
		colorRegistry.put("g#", new RGB(64, 64, 64)); //$NON-NLS-1$
		colorRegistry.put("w#", new RGB(144, 144, 144)); //$NON-NLS-1$
		colorRegistry.put("p#", display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB()); //$NON-NLS-1$
		// Selected gallery item text
		colorRegistry.put("b!", new RGB(0, 0, 0)); //$NON-NLS-1$
		colorRegistry.put("d!", new RGB(64, 64, 64)); //$NON-NLS-1$
		colorRegistry.put("g!", new RGB(128, 128, 128)); //$NON-NLS-1$
		colorRegistry.put("w!", new RGB(255, 255, 255)); //$NON-NLS-1$
		colorRegistry.put("p!", display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB()); //$NON-NLS-1$

		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			return IApplication.EXIT_OK;
		} finally {
			try {
				display.dispose();
			} catch (Exception e) {
				// catched only because of trayitem bug in osx implemention
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		if (!display.isDisposed())
			display.syncExec(() -> workbench.close());
	}
}
