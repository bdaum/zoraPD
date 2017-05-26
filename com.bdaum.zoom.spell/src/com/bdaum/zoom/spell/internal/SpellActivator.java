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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.spell.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.stibocatalog.hunspell.Hunspell;

@SuppressWarnings("restriction")
public class SpellActivator extends ZUiPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.spell"; //$NON-NLS-1$

	private static SpellActivator plugin;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		URL resource = FileLocator.find(getBundle(),
				new Path('/' + Hunspell.libName()), null);
		try {
			resource = FileLocator.toFileURL(resource);
		} catch (IOException e) {
			// should never happen
		}
		String hunPath = new File(resource.getPath()).getParent().toString();
		String libPath = System.getProperty("jna.library.path", ""); //$NON-NLS-1$//$NON-NLS-2$
		if (libPath.indexOf(hunPath) < 0)
			System.setProperty(
					"jna.library.path", libPath.length() > 0 ? hunPath + ';' + libPath : hunPath); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SpellActivator getDefault() {
		return plugin;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logWarning(String message, Throwable e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

}
