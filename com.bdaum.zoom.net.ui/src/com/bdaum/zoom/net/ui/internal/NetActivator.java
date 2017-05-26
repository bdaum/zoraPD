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

package com.bdaum.zoom.net.ui.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.core.IFTPService;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.internal.Activator;
import com.bdaum.zoom.net.ui.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.bdaum.zoom.ui.internal.dialogs.EditFtpDialog;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class NetActivator extends ZUiPlugin implements IFTPService {

	public class FTPSession {

		private Map<String, FTPClient> connectionMap = new HashMap<String, FTPClient>();

		public void close() {
			for (FTPClient client : connectionMap.values())
				disposeClient(client);
			connectionMap.clear();
		}

		public FTPClient authenticate(URL url) {
			String host = url.getHost();
			int port = url.getPort();
			String userInfo = url.getUserInfo();
			if (userInfo != null && userInfo.length() > 0) {
				disposeClient(connectionMap.remove(host));
				FtpAccount acc = new FtpAccount();
				acc.setHost(host);
				if (port > 0)
					acc.setPort(port);
				acc.setDirectory(url.getPath());
				int p = userInfo.indexOf(':');
				if (p > 0) {
					acc.setLogin(userInfo.substring(0, p));
					acc.setPassword(userInfo.substring(p + 1));
				} else {
					acc.setLogin(userInfo);
					acc.setPassword(""); //$NON-NLS-1$
				}
				try {
					FTPClient client = acc.login();
					if (client != null && client.isConnected()) {
						connectionMap.put(host, client);
						return client;
					}
				} catch (IOException e) {
					// do nothing
				}
			}
			FTPClient client = connectionMap.get(host);
			if (client != null) {
				if (client.isConnected())
					return client;
				connectionMap.remove(host);
			}
			FtpAccount acc = accountMap.get(host);
			if (acc != null) {
				try {
					client = acc.login();
					if (client != null && client.isConnected()) {
						connectionMap.put(host, client);
						return client;
					}
				} catch (IOException e) {
					// do nothing
				}
				accountMap.remove(host);
			}
			List<FtpAccount> ftpAccounts = FtpAccount.getAllAccounts();
			for (FtpAccount ftpAccount : ftpAccounts) {
				if (ftpAccount.getHost().equals(host)
						&& (port < 0 || port == ftpAccount.getPort())) {
					try {
						client = ftpAccount.login();
						if (client != null && client.isConnected()) {
							connectionMap.put(host, client);
							accountMap.put(host, ftpAccount);
							return client;
						}
					} catch (IOException e) {
						// do nothing
					}
				}
			}
			FtpAccount ftpAccount = new FtpAccount();
			ftpAccount.setHost(host);
			if (port > 0)
				ftpAccount.setPort(port);
			ftpAccount.setDirectory(url.getPath());
			String errorMessage = null;
			while (true) {
				Shell activeShell = PlatformUI.getWorkbench().getDisplay()
						.getActiveShell();
				EditFtpDialog accDialog = new EditFtpDialog(activeShell,
						ftpAccount, true, errorMessage);
				if (accDialog.open() != Window.OK)
					break;
				ftpAccount = accDialog.getResult();
				try {
					client = ftpAccount.login();
					if (client != null && client.isConnected()) {
						connectionMap.put(host, client);
						accountMap.put(host, ftpAccount);
						return client;
					}
				} catch (IOException e) {
					// do nothing
				}
				errorMessage = Messages.NetActivator_connection_failed;
			}
			return null;
		}

		private void disposeClient(FTPClient client) {
			if (client != null && client.isConnected()) {
				try {
					client.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
		}

	}

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.net.ui"; //$NON-NLS-1$

	// The shared instance
	private static NetActivator plugin;

	private Map<Object, FTPSession> sessionMap = new HashMap<Object, FTPSession>();
	private Map<Object, ServiceReference<?>> serviceMap = new HashMap<Object, ServiceReference<?>>(
			5);
	Map<String, FtpAccount> accountMap = new HashMap<String, FtpAccount>();

	/**
	 * The constructor
	 */
	public NetActivator() {
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
		for (FTPSession session : sessionMap.values())
			session.close();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static NetActivator getDefault() {
		return plugin;
	}

	public IProxyService getProxyService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		ServiceReference<?> ref = bundleContext
				.getServiceReference(IProxyService.class.getName());
		if (ref != null) {
			IProxyService service = (IProxyService) bundleContext
					.getService(ref);
			if (service != null)
				serviceMap.put(service, ref);
			return service;
		}
		return null;
	}

	public InputStream openStream(String url) throws IOException {
		return Activator
				.openStream(url,
						getPreferenceStore()
								.getInt(PreferenceConstants.TIMEOUT) * 1000);
	}

	public InputStream retrieveFile(Object ticket, URL url) throws IOException {
		FTPClient client = getClient(ticket, url);
		String path = url.getPath();
		int p = path.lastIndexOf('/');
		if (p >= 0) {
			String dir = path.substring(0, p);
			if (!client.changeWorkingDirectory(dir))
				throw new FileNotFoundException(dir);
			path = path.substring(p + 1);
		}
		return client.retrieveFileStream(path);
	}

	public FTPClient getClient(Object ticket, URL url) {
		FTPSession ftpSession = sessionMap.get(ticket);
		if (ftpSession == null) {
			ftpSession = new FTPSession();
			sessionMap.put(ticket, ftpSession);
		}
		return ftpSession.authenticate(url);
	}

	public void endSession(Object ticket) {
		synchronized (sessionMap) {
			FTPSession session = sessionMap.remove(ticket);
			if (session != null)
				session.close();
		}
	}

	public Object startSession() {
		synchronized (sessionMap) {
			Object ticket = new Object();
			FTPSession session = new FTPSession();
			sessionMap.put(ticket, session);
			return ticket;
		}
	}

	public void ungetProxyService(IProxyService service) {
		if (service != null) {
			ServiceReference<?> ref = serviceMap.remove(service);
			if (ref != null)
				getBundle().getBundleContext().ungetService(ref);
		}
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));

	}

}
