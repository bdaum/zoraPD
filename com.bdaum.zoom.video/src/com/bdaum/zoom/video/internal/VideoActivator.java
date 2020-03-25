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
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.image.IImportFilterFactory;
import com.bdaum.zoom.image.IVideoService;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

@SuppressWarnings("restriction")
public class VideoActivator extends ZUiPlugin implements IVideoService {

	public static final String PLUGIN_ID = "com.bdaum.zoom.video"; //$NON-NLS-1$
	
	private static VideoActivator plugin;

	private HashMap<String, IImportFilterFactory> importFilters;

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

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

	@Override
	public IVideoStreamer createVideoStreamer(String file, IFrameHandler frameHandler, int preferreWidth) {
		return new VideoStreamer(file, frameHandler, preferreWidth);
	}

}
