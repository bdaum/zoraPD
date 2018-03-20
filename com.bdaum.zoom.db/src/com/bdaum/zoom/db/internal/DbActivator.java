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
 * (c) 2009-2013 Berthold Daum  
 */
package com.bdaum.zoom.db.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.core.internal.lire.NullLireService;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.lucene.NullLuceneService;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.program.BatchUtilities;

public class DbActivator extends Plugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.db"; //$NON-NLS-1$

	private static DbActivator plugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DbActivator getDefault() {
		return plugin;
	}

	private ServiceReference<?> peerServiceRef;

	private ServiceReference<ILireService> lireServiceRef;

	private ServiceReference<ILuceneService> luceneServiceRef;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		if (peerServiceRef != null)
			context.ungetService(peerServiceRef);
		if (lireServiceRef != null)
			context.ungetService(lireServiceRef);
		if (luceneServiceRef != null)
			context.ungetService(luceneServiceRef);
		plugin = null;
		super.stop(context);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logInfo(java.lang.String)
	 */
	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logError(java.lang.String,
	 * java.lang.Throwable)
	 */
	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logWarning(java.lang.String,
	 * java.lang.Exception)
	 */
	public void logWarning(String message, Exception e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

	public IPeerService getPeerService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		peerServiceRef = bundleContext.getServiceReference(IPeerService.class);
		if (peerServiceRef != null)
			return (IPeerService) bundleContext.getService(peerServiceRef);
		return null;
	}

	public int getLireServiceVersion() {
		BundleContext bundleContext = getBundle().getBundleContext();
		lireServiceRef = bundleContext.getServiceReference(ILireService.class);
		if (lireServiceRef != null) {
			Object version = lireServiceRef.getProperty(ILireService.VERSION);
			if (version instanceof Integer)
				return (Integer) version;
		}
		return -1;
	}

	public ILireService getLireService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		lireServiceRef = bundleContext.getServiceReference(ILireService.class);
		if (lireServiceRef != null)
			return bundleContext.getService(lireServiceRef);
		return new NullLireService();
	}

	public ILuceneService getLuceneService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		luceneServiceRef = bundleContext.getServiceReference(ILuceneService.class);
		if (luceneServiceRef != null)
			return bundleContext.getService(luceneServiceRef);
		return new NullLuceneService();
	}


	public boolean isVersionChange(String file) {
		String version = Platform.getProduct().getDefiningBundle().getVersion()
				.toString();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		if (!version.equals(node.get(file, null))) {
			BatchUtilities.putPreferences(node, file, version);
			return true;
		}
		return false;
	}

}
