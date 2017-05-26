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

	// The shared instance
	private static Activator plugin;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public File locateDCRAW() {
		String dcraw = BatchConstants.WIN32 ? "dcraw.exe" : "dcraw"; //$NON-NLS-1$ //$NON-NLS-2$
		File installFolder = new File(Platform.getInstallLocation().getURL()
				.getPath());
		File dcrawFile = new File(new File(installFolder,
				BatchConstants.DROPINFOLDER), dcraw);
		if (dcrawFile.exists())
			return dcrawFile;
		dcrawFile = new File(new File(installFolder.getParent(),
				BatchConstants.DROPINFOLDER), dcraw);
		if (dcrawFile.exists())
			return dcrawFile;
		if (!BatchConstants.LINUX) {
			try {
				return FileLocator.findFile(getBundle(), "/" + dcraw); //$NON-NLS-1$
			} catch (Exception e) {
				logError(Messages.getString("Activator.Error_locating"), e); //$NON-NLS-1$
			}
		} else {
			File linuxFile = new File("/usr/bin/dcraw"); //$NON-NLS-1$
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
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
