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
package com.bdaum.zoom.peer.internal.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import ch.ethz.iks.r_osgi.RemoteOSGiException;
import ch.ethz.iks.r_osgi.RemoteOSGiService;
import ch.ethz.iks.r_osgi.RemoteServiceReference;
import ch.ethz.iks.r_osgi.URI;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.peer.IPeerProvider;
import com.bdaum.zoom.peer.internal.IPeerListener;
import com.bdaum.zoom.peer.internal.PeerActivator;
import com.bdaum.zoom.peer.internal.model.PeerDefinition;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.widgets.CLink;

@SuppressWarnings("restriction")
public class ROSGiManager {

	private class PingJob extends Daemon {

		public PingJob() {
			super(Messages.ROSGiManager_ping, 10000);
		}

		@Override
		protected boolean mayRun() {
			return Ui.isWorkbenchActive();
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			for (PeerDefinition peer : activator.getPeers()) {
				try {
					RemoteServiceReference[] references = remote
							.getRemoteServiceReferences(new URI(PROTOCOL
									+ "://" + peer), //$NON-NLS-1$
									IPeerProvider.class.getName(), null);
					if (references == null || references.length == 0) {
						if (onlineMap.remove(peer.toString()) != null)
							firePeerStatusChanged(peer, false);
					} else if (onlineMap.put(peer.toString(), references[0]) != null)
						firePeerStatusChanged(peer, true);
				} catch (RemoteOSGiException e) {
					if (onlineMap.remove(peer.toString()) != null)
						firePeerStatusChanged(peer, false);
				}
				if (monitor.isCanceled())
					break;
			}
		}
	}

	public static final String PROTOCOL = "r-osgi"; //$NON-NLS-1$

	private ServiceTracker<Object, Object> remoteOSGiServiceTracker;
	private final BundleContext context;
	private ServiceRegistration<IPeerProvider> peerProviderRegistration;
	private int listeningPort;
	private Map<String, RemoteServiceReference> onlineMap = Collections
			.synchronizedMap(new HashMap<String, RemoteServiceReference>());
	private RemoteOSGiService remote;
	private ListenerList<IPeerListener> peerListeners = new ListenerList<IPeerListener>();
	private final String host;

	private PeerProvider ownPeerProvider;

	private String ownLocation;

	private final PeerActivator activator;

	private URI endpoint;

	public ROSGiManager(PeerActivator activator, BundleContext context,
			String host) {
		this.activator = activator;
		this.context = context;
		this.host = host;
		remote = getRemoteOSGiService();
		listeningPort = remote.getListeningPort(PROTOCOL);
		ownLocation = host + ":" + listeningPort; //$NON-NLS-1$
		try {
			endpoint = new URI(new StringBuilder().append(PROTOCOL)
					.append("://").append(ownLocation).toString()); //$NON-NLS-1$
			remote.connect(endpoint);
		} catch (RemoteOSGiException e) {
			activator.logError(Messages.ROSGiManager_framework_error, e);
		} catch (IOException e) {
			activator.logError(Messages.ROSGiManager_io_error, e);
		}
		new PingJob().schedule();
	}

	public void checkListeningPort(final int port, IAdaptable adaptable) {
		if (listeningPort != port) {
			final Shell shell = adaptable.getAdapter(Shell.class);
			if (shell != null && !shell.isDisposed()) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						new AcousticMessageDialog(shell,
								Constants.APPLICATION_NAME, null, NLS.bind(
										Messages.ROSGiManager_port_occupied,
										port, listeningPort),
								AcousticMessageDialog.WARNING,
								new String[] { IDialogConstants.OK_LABEL }, 0) {
							@Override
							protected Control createCustomArea(Composite parent) {
								Composite composite = new Composite(parent,
										SWT.NONE);
								composite.setLayoutData(new GridData(SWT.FILL,
										SWT.FILL, true, true));
								composite.setLayout(new GridLayout());
								CLink link = new CLink(composite, SWT.NONE);
								link.setLayoutData(new GridData(SWT.CENTER,
										SWT.CENTER, true, false));
								link.setText(Messages.ROSGiManager_configure_network);
								link.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(
											org.eclipse.swt.events.SelectionEvent e) {
										close();
										PreferencesUtil
												.createPreferenceDialogOn(
														shell,
														"com.bdaum.zoom.peer.PeerPreferencePage", //$NON-NLS-1$
														new String[0], null).open();
									}
								});
								return composite;
							}
						}.open();
					}
				});
			}

		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RemoteOSGiService getRemoteOSGiService() {
		if (remoteOSGiServiceTracker == null) {
			remoteOSGiServiceTracker = new ServiceTracker(context,
					RemoteOSGiService.class.getName(), null);
			remoteOSGiServiceTracker.open();
		}
		return (RemoteOSGiService) remoteOSGiServiceTracker.getService();
	}

	public void registerPeerProvider() {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(RemoteOSGiService.R_OSGi_REGISTRATION, Boolean.TRUE);
		ownPeerProvider = new PeerProvider(host, listeningPort);
		peerProviderRegistration = context.registerService(IPeerProvider.class,
				ownPeerProvider, properties);
	}

	public void dispose() {
		// Unget all foreign provider services
		for (RemoteServiceReference ref : onlineMap.values())
			remote.ungetRemoteService(ref);
		// Retract our own provider
		try {
			if (peerProviderRegistration != null) {
				peerProviderRegistration.unregister();
				ownPeerProvider = null;
			}
		} catch (Exception e) {
			// ignore
		}
		// Close the R-OSGi tracker
		if (remoteOSGiServiceTracker != null) {
			remoteOSGiServiceTracker.close();
			remoteOSGiServiceTracker = null;
		}
		// disconnect
		try {
			remote.disconnect(endpoint);
		} catch (RemoteOSGiException e) {
			// ignore
		}
	}

	public IPeerProvider[] getPeerProviders(List<PeerDefinition> peersDefs) {

		List<IPeerProvider> result = new ArrayList<IPeerProvider>(
				peersDefs.size());
		for (PeerDefinition peer : peersDefs) {
			String location = peer.toString();
			IPeerProvider peerProvider = getPeerProvider(location);
			if (peerProvider != null)
				result.add(peerProvider);
			else if (onlineMap.remove(location) != null)
				firePeerStatusChanged(peer, false);
		}
		if (ownPeerProvider != null)
			result.add(ownPeerProvider);
		return result.toArray(new IPeerProvider[result.size()]);
	}

	public IPeerProvider getPeerProvider(String location) {
		if (location == null || location.equals(ownLocation))
			return ownPeerProvider;
		RemoteServiceReference ref = onlineMap.get(location);
		if (ref != null) {
			IPeerProvider remoteService = (IPeerProvider) remote
					.getRemoteService(ref);
			if (remoteService != null)
				return remoteService;
			activator.logError(Messages.ROSGiManager_service_not_found, null);
		}
		return null;
	}

	private void firePeerStatusChanged(PeerDefinition peer, boolean online) {
		for (Object listener : peerListeners.getListeners())
			((IPeerListener) listener).statusChanged(peer, online);
	}

	public void addPeerListener(IPeerListener listener) {
		peerListeners.add(listener);
	}

	public void removePeerListener(IPeerListener listener) {
		peerListeners.remove(listener);
	}

	/**
	 * @return listeningPort
	 */
	public int getListeningPort() {
		return listeningPort;
	}

	public boolean isOnline(String location) {
		return onlineMap.containsKey(location);
	}

	public int getOnlinePeerCount() {
		return onlineMap.size();
	}

	/**
	 * @return ownLocation
	 */
	public String getOwnLocation() {
		return ownLocation;
	}

}
