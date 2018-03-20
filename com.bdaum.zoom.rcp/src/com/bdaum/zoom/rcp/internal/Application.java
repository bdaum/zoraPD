/*******************************************************************************
 * Copyright (c) 2009-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
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

	private static final String ZOOM_INI = "/zoom.ini"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		RGB white = new RGB(255, 255, 255);
		RGB silver = new RGB(144, 144, 144);
		RGB grey = new RGB(128, 128, 128);
		RGB dark = new RGB(64, 64, 64);
		RGB black = new RGB(0, 0, 0);
		// Regions
		colorRegistry.put(Constants.APPCOLOR_REGION_FACE, new RGB(255, 160, 0));
		// Background
		colorRegistry.put("b", black); //$NON-NLS-1$
		colorRegistry.put("d", dark); //$NON-NLS-1$
		colorRegistry.put("g", new RGB(240, 240, 248)); //$NON-NLS-1$
		colorRegistry.put("w", white); //$NON-NLS-1$
		colorRegistry.put("p", display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB()); //$NON-NLS-1$
		// Widget text
		colorRegistry.put("b_", white); //$NON-NLS-1$
		colorRegistry.put("d_", white); //$NON-NLS-1$
		colorRegistry.put("g_", black); //$NON-NLS-1$
		colorRegistry.put("w_", black); //$NON-NLS-1$
		colorRegistry.put("p_", display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB()); //$NON-NLS-1$
		// Gallery item text
		colorRegistry.put("b-", grey); //$NON-NLS-1$
		colorRegistry.put("d-", silver); //$NON-NLS-1$
		colorRegistry.put("g-", new RGB(32, 32, 32)); //$NON-NLS-1$
		colorRegistry.put("w-", grey); //$NON-NLS-1$
		colorRegistry.put("p-", display.getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB()); //$NON-NLS-1$
		// Selected gallery item background
		colorRegistry.put("b#", grey); //$NON-NLS-1$
		colorRegistry.put("d#", silver); //$NON-NLS-1$
		colorRegistry.put("g#", dark); //$NON-NLS-1$
		colorRegistry.put("w#", silver); //$NON-NLS-1$
		colorRegistry.put("p#", display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB()); //$NON-NLS-1$
		// Selected gallery item text
		colorRegistry.put("b!", black); //$NON-NLS-1$
		colorRegistry.put("d!", dark); //$NON-NLS-1$
		colorRegistry.put("g!", grey); //$NON-NLS-1$
		colorRegistry.put("w!", white); //$NON-NLS-1$
		colorRegistry.put("p!", display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB()); //$NON-NLS-1$
		// Fonts are created by the ApplicationWorkbenchAdvisor
		
		URL iniUrl = FileLocator.find(RcpActivator.getDefault().getBundle(), new Path(ZOOM_INI), null);
		if (iniUrl != null)
			try (InputStream in = iniUrl.openStream()) {
				System.getProperties().load(in);
			}
		try {
			return (PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor()) == PlatformUI.RETURN_RESTART) ? IApplication.EXIT_RESTART
							: IApplication.EXIT_OK;
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
		if (workbench != null) {
			final Display display = workbench.getDisplay();
			if (!display.isDisposed())
				display.syncExec(() -> workbench.close());
		}
	}
}
