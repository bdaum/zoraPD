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
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.video.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.IImportFilterFactory;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.bdaum.zoom.video.internal.dialogs.VLCDialog;
import com.bdaum.zoom.video.internal.preferences.PreferenceConstants;
import com.sun.jna.NativeLibrary;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class VideoActivator extends ZUiPlugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.video"; //$NON-NLS-1$

	private static VideoActivator plugin;

	private HashMap<String, IImportFilterFactory> importFilters;

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative
	 * path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		String location = getVlcLocation();
		if (location != null) {
			File loc = new File(location);
			if (loc.exists())
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), loc.getParent());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static VideoActivator getDefault() {
		return plugin;
	}

	public Map<String, IImportFilterFactory> getImportFilters() {
		if (importFilters == null) {
			importFilters = new HashMap<String, IImportFilterFactory>(5);
			for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "importFilter") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements())
					try {
						IImportFilterFactory filter = (IImportFilterFactory) conf.createExecutableExtension("class"); //$NON-NLS-1$
						StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
						while (st.hasMoreTokens())
							importFilters.put(st.nextToken().toLowerCase(), filter);
					} catch (CoreException e) {
						logError(NLS.bind(Messages.Activator_cannot_create_filter, conf.getAttribute("name")), e); //$NON-NLS-1$
					}
		}
		return importFilters;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public MediaPlayerFactory vlcCheck(Shell shell, String[] vlcArgs) {
		File locat = null;
		while (true) {
			String vlcLocation = getVlcLocation();
			locat = (vlcLocation == null || vlcLocation.isEmpty()) ? null : new File(vlcLocation);
			if (locat == null || !locat.exists()) {
				final VLCDialog dialog = new VLCDialog(shell, locat, null);
				shell.getDisplay().syncExec(() -> dialog.open());
				locat = dialog.getResult();
				if (locat != null)
					setVlcLocation(locat);
				else
					break;
			} else {
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), locat.getParent());
				try {
					return new MediaPlayerFactory(vlcArgs);
				} catch (Exception e) {
					final VLCDialog dialog = new VLCDialog(shell, locat,
							NLS.bind(Platform.getOSArch().indexOf("64") >= 0 ? Messages.Activator_invalid_libary //$NON-NLS-1$
									: Messages.VideoActivator_invalid_library_64, Constants.APPNAME));
					shell.getDisplay().syncExec(() -> dialog.open());
					locat = dialog.getResult();
					if (locat != null)
						setVlcLocation(locat);
					else
						break;
				}
			}
		}
		return null;
	}

	public void setVlcLocation(File locat) {
		getPreferenceStore().setValue(PreferenceConstants.VLCLOCATION, locat.getAbsolutePath());
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), locat.getParent());
	}

	public String getVlcLocation() {
		return getPreferenceStore().getString(PreferenceConstants.VLCLOCATION);
	}
}
