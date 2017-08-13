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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.peer.internal;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbListener;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerProvider;
import com.bdaum.zoom.peer.internal.model.PeerDefinition;
import com.bdaum.zoom.peer.internal.model.SharedCatalog;
import com.bdaum.zoom.peer.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.peer.internal.services.ROSGiManager;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class PeerActivator extends ZUiPlugin
		implements IPreferenceChangeListener, IDbListener, ServiceTrackerCustomizer<Object, Object> {

	private static final String DEFAULT_CONTAINER_TYPE = "ecf.r_osgi.peer"; //$NON-NLS-1$

	private String containerType = DEFAULT_CONTAINER_TYPE;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.peer"; //$NON-NLS-1$

	// The shared instance
	private static PeerActivator plugin;

	private BundleContext context;

	private ServiceTracker<?, ?> containerManagerServiceTracker;

	private IContainer container;

	private List<SharedCatalog> catalogs;

	private ServiceTracker<Object, Object> peerProviderServiceTracker;

	private List<PeerDefinition> peers;

	private String host;

	private String hostName = ""; //$NON-NLS-1$

	private ROSGiManager rosgiManager;

	private Map<String, PeerDefinition> incomingCalls;

	private Set<String> blockedNodes;

	private Daemon connectJob;

	private int definedPort;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		plugin = this;
		 // cut out internal platform events
		System.setProperty("ch.ethz.iks.r_osgi.topic.filter", "org/eclipse/*"); //$NON-NLS-1$ //$NON-NLS-2$
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		StringTokenizer st = new StringTokenizer(node.get(PreferenceConstants.RECEIVERS, ""), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		Map<String, PeerDefinition> incoming = getIncomingCalls();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int p = token.indexOf(' ');
			if (p > 0) {
				try {
					String receiver = token.substring(0, p);
					int q = token.indexOf(' ', p + 1);
					if (q > p)
						incoming.put(receiver, new PeerDefinition(receiver, Long.parseLong(token.substring(p + 1, q)),
								Integer.parseInt(token.substring(q + 1))));
				} catch (Exception e) {
					// do nothing
				}
			}
		}
		node.addPreferenceChangeListener(this);
		definedPort = getPreferenceStore().getInt(PreferenceConstants.PORT);
		System.setProperty("ch.ethz.iks.r_osgi.port", String.valueOf(definedPort)); //$NON-NLS-1$
		connectJob = new Daemon(Messages.PeerActivator_connect_osgi, -1) {
			@Override
			public boolean belongsTo(Object family) {
				return family == PeerActivator.this || super.belongsTo(family);
			}
			
			@Override
			protected void doRun(IProgressMonitor monitor) {
				rosgiManager = new ROSGiManager(PeerActivator.this, context, getHostOrHostname());
				rosgiManager.registerPeerProvider();
				try {
					IContainerManager containerManager = getContainerManagerService();
					IContainerFactory containerFactory = containerManager.getContainerFactory();
					container = containerFactory.createContainer(containerType);
					peerProviderServiceTracker = new ServiceTracker(context, IPeerProvider.class, PeerActivator.this);
					peerProviderServiceTracker.open();
					logInfo(Messages.PeerActivator_peer_service_connected);
				} catch (ContainerCreateException e) {
					logError(Messages.PeerActivator_cannot_create_container, e);
				}
			}
		};
		connectJob.schedule(500);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(context, IContainerManager.class.getName(), null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		try {
			if (rosgiManager != null)
				rosgiManager.dispose();
		} catch (Exception e) {
			// just to be safe
		}
		if (container != null) {
			container.disconnect();
			container = null;
		}
		if (containerManagerServiceTracker != null) {
			containerManagerServiceTracker.close();
			containerManagerServiceTracker = null;
		}
		if (peerProviderServiceTracker != null) {
			peerProviderServiceTracker.close();
			peerProviderServiceTracker = null;
		}
		InstanceScope.INSTANCE.getNode(PLUGIN_ID).removePreferenceChangeListener(this);
		writeIncomingCalls();
		writeBlockedNodes();
		if (catalogs != null)
			for (SharedCatalog sharedDb : catalogs)
				sharedDb.dispose();
		plugin = null;
		super.stop(context);
	}

	private ROSGiManager getROSGiManager() {
		int i = 20;
		while (rosgiManager == null && i-- > 0) {
			if (Job.getJobManager().find(connectJob).length == 0)
				break;
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				break;
			}
		}
		return rosgiManager;
	}

	public void writeIncomingCalls() {
		if (incomingCalls != null) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, PeerDefinition> entry : incomingCalls.entrySet()) {
				if (sb.length() > 0)
					sb.append('\n');
				PeerDefinition pd = entry.getValue();
				sb.append(entry.getKey()).append(' ').append(String.valueOf(pd.getLastAccess())).append(' ')
						.append(pd.getRights());
			}
			getPreferenceStore().putValue(PreferenceConstants.RECEIVERS, sb.toString());
		}
	}

	public void writeBlockedNodes() {
		if (blockedNodes != null) {
			StringBuilder sb = new StringBuilder();
			for (String node : blockedNodes) {
				if (sb.length() > 0)
					sb.append('\n');
				sb.append(node);
			}
			getPreferenceStore().putValue(PreferenceConstants.BLOCKEDNODES, sb.toString());
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PeerActivator getDefault() {
		return plugin;
	}

	public IPeerProvider[] getPeerProviders() {
		return getROSGiManager().getPeerProviders(getPeers());
	}

	public List<SharedCatalog> getSharedCatalogs() {
		if (catalogs == null) {
			catalogs = readSharedCatalogs();
			Core.getCore().getDbFactory().addDbListener(this);
		}
		return catalogs;
	}

	public Set<String> getBlockedNodes() {
		if (blockedNodes == null)
			blockedNodes = readBlockedNodes();
		return blockedNodes;
	}

	private Set<String> readBlockedNodes() {
		blockedNodes = Collections.synchronizedSet(new HashSet<String>());
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String s = preferencesService.getString(PLUGIN_ID, PreferenceConstants.BLOCKEDNODES, "", null); //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(s, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens())
			blockedNodes.add(st.nextToken());
		return blockedNodes;
	}

	private static List<SharedCatalog> readSharedCatalogs() {
		List<SharedCatalog> catalogs = new ArrayList<SharedCatalog>();
		StringTokenizer st = new StringTokenizer(
				Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.SHAREDCATALOGS, "", null), //$NON-NLS-1$
				"\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int i = 0;
			SharedCatalog cat = null;
			StringTokenizer sto = new StringTokenizer(token, "?"); //$NON-NLS-1$
			while (sto.hasMoreTokens()) {
				String t = sto.nextToken();
				switch (i++) {
				case 0:
					cat = new SharedCatalog(t, 0);
					break;
				case 1:
					try {
						if (cat != null)
							cat.setPrivacy(Integer.parseInt(t));
					} catch (NumberFormatException e) {
						// do nothing
					}
					break;
				default:
					if (cat != null) {
						int p = t.lastIndexOf('=');
						if (p >= 0) {
							try {
								new PeerDefinition(t.substring(0, p), -1, Integer.parseInt(t.substring(p + 1)))
										.setParent(cat);
							} catch (NumberFormatException e) {
								// do nothing
							}
						}
					}
					break;
				}
			}
			if (cat != null)
				catalogs.add(cat);
		}
		return catalogs;
	}

	public List<PeerDefinition> getPeers() {
		if (peers == null)
			peers = readPeers(
					Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.PEERS, "", null)); //$NON-NLS-1$
		return peers;
	}

	private static List<PeerDefinition> readPeers(String prefs) {
		List<PeerDefinition> peers = new ArrayList<PeerDefinition>();
		StringTokenizer st = new StringTokenizer(prefs, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens())
			peers.add(new PeerDefinition(st.nextToken(), -1, 0));
		return peers;
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		// No update of port, because restart is needed anyway
		if (PreferenceConstants.PORT.equals(event.getKey()))
			UiActivator.getDefault().restart();
		else if (PreferenceConstants.PEERS.equals(event.getKey()))
			peers = null;
		else if (PreferenceConstants.SHAREDCATALOGS.equals(event.getKey())) {
			List<SharedCatalog> newCats = readSharedCatalogs();
			for (SharedCatalog cat : newCats)
				if (!catalogs.contains(cat))
					catalogs.add(cat);
			if (catalogs != null) {
				Iterator<SharedCatalog> it = catalogs.iterator();
				while (it.hasNext()) {
					SharedCatalog sharedCatalog = it.next();
					if (!newCats.contains(sharedCatalog)) {
						sharedCatalog.dispose();
						it.remove();
					}
				}
			}
		}
	}

	public IDbManager databaseAboutToOpen(String filename, boolean primary) {
		File file = new File(filename);
		for (SharedCatalog cat : catalogs)
			if (file.equals(cat.getPath()) && cat.getDbEntry().getFile() != null)
				return cat.getDbEntry();
		return null;
	}

	public void databaseOpened(IDbManager manager, boolean primary) {
		File file = manager.getFile();
		if (file != null)
			for (SharedCatalog cat : catalogs)
				if (file.equals(cat.getPath()) && cat.getDbEntry().getFile() != null)
					cat.setDbManager(manager);
	}

	public boolean databaseAboutToClose(IDbManager manager) {
		if (container != null)
			for (SharedCatalog cat : catalogs)
				if (cat.getDbEntry() == manager)
					return false;
		return true;
	}

	public void databaseClosed(IDbManager manager, int mode) {
		for (SharedCatalog cat : catalogs)
			if (cat.getDbEntry() == manager)
				cat.setDbManager(null);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logInfo(java.lang.String)
	 */

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public static void logDebug(String message, Object parm) {
		if (CoreActivator.DEBUG)
			getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID,
					"Debug: " + (parm == null ? message : NLS.bind(message, parm)))); //$NON-NLS-1$
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object addingService(ServiceReference reference) {
		logInfo(NLS.bind(Messages.PeerActivator_peer_provider_added, getHost() + ":" //$NON-NLS-1$
				+ rosgiManager.getListeningPort()));
		return context.getService(reference);
	}

	@SuppressWarnings("rawtypes")
	public void modifiedService(ServiceReference reference, Object service) {
		// do nothing
	}

	@SuppressWarnings("rawtypes")
	public void removedService(ServiceReference reference, Object service) {
		logInfo(Messages.PeerActivator_peer_provider_removed);
	}

	/**
	 * @return host
	 */
	public String getHost() {
		if (host == null)
			try {
				host = InetAddress.getLocalHost().toString();
				int p = host.indexOf('/');
				if (p >= 0) {
					hostName = host.substring(0, p);
					host = host.substring(p + 1);
				}
			} catch (UnknownHostException e) {
				host = "localhost"; //$NON-NLS-1$
			}
		return host;
	}

	/**
	 * @return hostName
	 */
	public String getHostName() {
		getHost();
		return hostName;
	}

	/**
	 * @param peer
	 * @return
	 * @see com.bdaum.zoom.peer.internal.services.ROSGiManager#isOnline(com.bdaum.zoom.peer.internal.model.PeerDefinition)
	 */
	public boolean isOnline(PeerDefinition peer) {
		return getROSGiManager().isOnline(peer.toString());
	}

	/**
	 * @param listener
	 * @see com.bdaum.zoom.peer.internal.services.ROSGiManager#addPeerListener(com.bdaum.zoom.peer.internal.IPeerListener)
	 */
	public void addPeerListener(IPeerListener listener) {
		getROSGiManager().addPeerListener(listener);
	}

	/**
	 * @param listener
	 * @see com.bdaum.zoom.peer.internal.services.ROSGiManager#removePeerListener(com.bdaum.zoom.peer.internal.IPeerListener)
	 */
	public void removePeerListener(IPeerListener listener) {
		getROSGiManager().removePeerListener(listener);
	}

	public String getLocation() {
		return getHostOrHostname() + ":" + getROSGiManager().getListeningPort(); //$NON-NLS-1$
	}

	public String getHostOrHostname() {
		String name = getHostName();
		return name.isEmpty() ? getHost() : name;
	}

	public IPeerProvider getPeerProvider(String location) throws ConnectionLostException {
		IPeerProvider peerProvider = getROSGiManager().getPeerProvider(location);
		if (peerProvider == null)
			throw new ConnectionLostException(location);
		return peerProvider;
	}

	public boolean addIncomingCall(String receiver, int rights) {
		if (!receiver.equals(getROSGiManager().getOwnLocation())) {
			Map<String, PeerDefinition> calls = getIncomingCalls();
			PeerDefinition peerDefinition = calls.get(receiver);
			if (peerDefinition != null) {
				if (peerDefinition.isBlocked())
					return false;
				peerDefinition.setRights(rights);
				peerDefinition.setLastAccess(System.currentTimeMillis());
			} else
				calls.put(receiver, new PeerDefinition(receiver, System.currentTimeMillis(), rights));
		}
		return true;
	}

	public Map<String, PeerDefinition> getIncomingCalls() {
		if (incomingCalls == null)
			incomingCalls = Collections.synchronizedMap(new HashMap<String, PeerDefinition>());
		return incomingCalls;
	}

	public boolean hasPeerProviders() {
		return getOnlinePeerCount() != 0 || hasOtherCatalogs();
	}

	private boolean hasOtherCatalogs() {
		File currentCatFile = Core.getCore().getDbManager().getFile();
		if (currentCatFile != null)
			for (SharedCatalog cat : getSharedCatalogs())
				if (!cat.getPath().equals(currentCatFile))
					return true;
		return false;
	}

	public int getOnlinePeerCount() {
		getROSGiManager();
		return rosgiManager == null ? 0 : rosgiManager.getOnlinePeerCount();
	}

	public void checkListeningPort(IAdaptable adaptable) {
		getROSGiManager();
		if (rosgiManager != null)
			rosgiManager.checkListeningPort(definedPort, adaptable);
	}

	public void deactivate() {
		Job.getJobManager().cancel(this);
		logInfo(Messages.PeerActivator_deactivated);
	}

	public int getListeningPort() {
		return getROSGiManager().getListeningPort();
	}

}
