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
 * (c) 2012-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.vr.internal;

import java.io.File;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.common.internal.FileLocator;

public class VrActivator extends Plugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.vr"; //$NON-NLS-1$
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private static VrActivator plugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static VrActivator getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext ctx) throws Exception {
		super.start(ctx);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */

	@Override
	public void stop(BundleContext ctx) throws Exception {
		plugin = null;
		super.stop(ctx);
	}

	public File locateResource(String path) {
		try {
			return FileLocator.findFile(getBundle(), path);
		} catch (Exception e) {
			return null;
		}
	}

}
