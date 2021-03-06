/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.net.communities;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.scohen.juploadr.uploadapi.IErrorHandler;

import com.bdaum.zoom.ui.internal.ZUiPlugin;

@SuppressWarnings("restriction")
public class CommunitiesActivator extends ZUiPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.net.communities"; //$NON-NLS-1$

	// The shared instance
	private static CommunitiesActivator plugin;

	/**
	 * The constructor
	 */
	public CommunitiesActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static CommunitiesActivator getDefault() {
		return plugin;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public static CommunityApi getCommunitiesApi(IConfigurationElement config) {
		try {
			CommunityApi api = (CommunityApi) config.createExecutableExtension("api"); //$NON-NLS-1$
			try {
				api.setErrorHandler((IErrorHandler) config.createExecutableExtension("errorHandler")); //$NON-NLS-1$
			} catch (Exception e) {
				// do nothing
			}
			return api;
		} catch (CoreException e) {
			getDefault().logError(
					NLS.bind(Messages.CommunitiesActivator_cannot_instantiate,
							config.getAttribute("name")), e); //$NON-NLS-1$
			return null;
		}
	}


}
