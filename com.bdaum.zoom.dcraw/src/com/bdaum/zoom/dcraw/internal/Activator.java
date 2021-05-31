/*******************************************************************************
 * Copyright (c) 2009-2021 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.dcraw.internal;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.program.BatchConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.dcraw"; //$NON-NLS-1$

	public final static String DCRAW = BatchConstants.WIN32 ? "dcraw.exe" : "dcraw"; //$NON-NLS-1$//$NON-NLS-2$

	public final static String DCRAWEMU = BatchConstants.WIN32 ? "dcraw_emu.exe" : "dcraw_emu"; //$NON-NLS-1$//$NON-NLS-2$

	// The shared instance
	private static Activator plugin;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public File locateDCRAW() {
		File installFolder = new File(Platform.getInstallLocation().getURL().getPath());
		File dcrawemuFile = new File(new File(installFolder, BatchConstants.DROPINFOLDER), DCRAWEMU);
		if (dcrawemuFile.exists())
			return dcrawemuFile;
		dcrawemuFile = new File(new File(installFolder.getParent(), BatchConstants.DROPINFOLDER), DCRAW);
		if (dcrawemuFile.exists())
			return dcrawemuFile;
		File dcrawFile = new File(new File(installFolder, BatchConstants.DROPINFOLDER), DCRAW);
		if (dcrawFile.exists())
			return dcrawFile;
		dcrawFile = new File(new File(installFolder.getParent(), BatchConstants.DROPINFOLDER), DCRAW);
		if (dcrawFile.exists())
			return dcrawFile;
		if (!BatchConstants.LINUX) {
			try {
				return FileLocator.findFile(getBundle(), "/" + DCRAWEMU); //$NON-NLS-1$
			} catch (Exception e) {
				try {
					return FileLocator.findFile(getBundle(), "/" + DCRAW); //$NON-NLS-1$
				} catch (Exception e1) {
					logError(Messages.getString("Activator.Error_locating"), e1); //$NON-NLS-1$
				}
			}
		} else {
			File linuxFile = new File("/usr/bin/dcraw_emu"); //$NON-NLS-1$
			if (linuxFile.exists())
				return linuxFile;
			linuxFile = new File("/usr/bin/dcraw"); //$NON-NLS-1$
			if (linuxFile.exists())
				return linuxFile;
		}
		return null;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
