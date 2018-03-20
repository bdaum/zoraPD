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

package com.bdaum.zoom.rcp.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

@SuppressWarnings("restriction")
public class RcpActivator extends ZUiPlugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.rcp"; //$NON-NLS-1$
	private static RcpActivator plugin;
	private boolean isNew;
	private boolean[] expansionState;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (Constants.WIN32)
			ImageActivator.getDefault().deleteTempFolderAfterShutdown();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static RcpActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setIntroExpansionState(boolean[] expansionState) {
		this.expansionState = expansionState;
	}

	public boolean[] getIntroExpansionState() {
		return expansionState;
	}

}
