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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.CaptionProcessor;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.webserver.PreferenceConstants;
import com.bdaum.zoom.webserver.internals.preferences.WebserverPreferencePage;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

@SuppressWarnings("restriction")
public class WebserverActivator extends ZUiPlugin implements IPreferenceChangeListener, IPropertyChangeListener {

	public static final String PLUGIN_ID = "com.bdaum.zoom.webserver"; //$NON-NLS-1$

	public static final int ERROR = -2;
	public static final int STOPPED = -1;
	public static final int STARTING = 0;
	public static final int RUNNING = 1;

	protected static final String PREFPAGEID = "com.bdaum.zoom.webserver.page1"; //$NON-NLS-1$

	private static WebserverActivator plugin;

	private int state = STOPPED;

	private HTTPServer server;
	private ListenerList<WebserverListener> listeners = new ListenerList<>();
	private QueryField[] webNodes;
	private final CaptionProcessor captionProcessor = new CaptionProcessor(Constants.TH_ALL);

	private IAction clickHandler = new Action() {
		public void run() {
			Shell activeShell = (Shell) getAdapter(Shell.class);
			if (activeShell != null)
				PreferencesUtil.createPreferenceDialogOn(activeShell, PREFPAGEID, new String[0],
						WebserverPreferencePage.GENERAL).open();
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		addPreferenceChangeListener(this);
		UiActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (com.bdaum.zoom.ui.preferences.PreferenceConstants.SHOWLABEL.equals(property)
				|| com.bdaum.zoom.ui.preferences.PreferenceConstants.THUMBNAILTEMPLATE.equals(property)
				|| com.bdaum.zoom.ui.preferences.PreferenceConstants.LABELALIGNMENT.equals(property)
				|| com.bdaum.zoom.ui.preferences.PreferenceConstants.LABELFONTSIZE.equals(property))
			captionProcessor.updateGlobalConfiguration();
	}

	public boolean startWebserver() {
		setState(STARTING);
		int port = getPreferenceStore().getInt(PreferenceConstants.PORT);
		String imagePath = getPath(PreferenceConstants.IMAGEPATH);
		String exhibitionsPath = getPath(PreferenceConstants.EXHIBITIONPATH);
		String galleryPath = getPath(PreferenceConstants.GALLERYPATH);
		String slideshowsPath = getPath(PreferenceConstants.SLIDESHOWSPATH);
		server = new ZHTTPServer(port);
		VirtualHost host = server.getVirtualHost(null);
		host.setDirectoryIndex(getPreferenceStore().getString(PreferenceConstants.STARTPAGE));
		host.addContext("/", new IndexContextHandler()); //$NON-NLS-1$
		AbstractLightboxContextHandler imagesContextHandler = new ImagesContextHandler();
		host.addContext(imagePath, imagesContextHandler, "GET", "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext("/search-similar", imagesContextHandler, "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext("/file-upload", imagesContextHandler, "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext(exhibitionsPath, new ExhibitionsContextHandler(), "GET", "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext(galleryPath, new GalleriesContextHandler(), "GET", "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext(slideshowsPath, new SlideshowsContextHandler(), "GET", "POST"); //$NON-NLS-1$ //$NON-NLS-2$
		host.addContext("/plugins", new PluginContextHandler(), "GET"); //$NON-NLS-1$ //$NON-NLS-2$
		URL filesUrl = findTemplate("files/"); //$NON-NLS-1$
		try {
			host.addContext("/files", new HTTPServer.FileContextHandler(new File(filesUrl.toURI()))); //$NON-NLS-1$
			server.start();
			setState(RUNNING);
			logInfo(Messages.WebserverActivator_server_started);
			return true;
		} catch (BindException e) {
			String message = e.getMessage();
			if (message.contains("Address already in use")) { //$NON-NLS-1$
				setState(STOPPED);
				handlePortInUse(port);
			} else
				setState(ERROR);
			logError(Messages.WebserverActivator_failed_to_start_server, e);
		} catch (IOException e) {
			logError(Messages.WebserverActivator_failed_to_start_server, e);
			setState(ERROR);
		} catch (URISyntaxException e) {
			// should never happen
		}
		return false;
	}

	private void handlePortInUse(int port) {
		Shell shell = (Shell) getAdapter(Shell.class);
		if (shell != null && !shell.isDisposed())
			shell.getDisplay().asyncExec(() -> {
				if (!shell.isDisposed())
					new AcousticMessageDialog(shell, Constants.APPLICATION_NAME, null,
							NLS.bind(Messages.WebserverActivator_port_alread_in_use, port),
							AcousticMessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0) {
						@Override
						protected Control createCustomArea(Composite parent) {
							Composite composite = new Composite(parent, SWT.NONE);
							composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
							composite.setLayout(new GridLayout());
							CLink link = new CLink(composite, SWT.NONE);
							link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
							link.setText(Messages.WebserverActivator_configure_webserver);
							link.addListener(new Listener() {
								@Override
								public void handleEvent(Event event) {
									close();
									PreferencesUtil.createPreferenceDialogOn(shell, PREFPAGEID, new String[0],
											WebserverPreferencePage.GENERAL).open();
								}
							});
							return composite;
						}
					}.open();
			});
	}

	private String getPath(String key) {
		String path = getPreferenceStore().getString(key);
		return path.startsWith("/") ? path : '/' + path; //$NON-NLS-1$
	}

	public URL findTemplate(String name) {
		return findUrl(getBundle(), "web/" + name); //$NON-NLS-1$
	}

	public URL findUrl(Bundle bundle, String path) {
		try {
			return FileLocator.findFileURL(bundle, "/$nl$/" + path, true); //$NON-NLS-1$
		} catch (IOException e) {
			logError(NLS.bind(Messages.WebserverActivator_error_unpacking, path), e);
		}
		return null;
	}

	public void stop(BundleContext context) throws Exception {
		stopWebserver();
		UiActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		removePreferenceChangeListener(this);
		plugin = null;
		super.stop(context);
	}

	public static WebserverActivator getDefault() {
		return plugin;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		int oldstate = this.state;
		this.state = state;
		for (WebserverListener listener : listeners)
			listener.stateChanged(oldstate, state);
		UiActivator.getDefault()
				.setServerMessage(state == RUNNING ? Messages.WebserverActivator_running
						: state == ERROR ? Messages.WebserverActivator_failed : Messages.WebserverActivator_stopped,
						clickHandler);
	}

	public void stopWebserver() {
		if (server != null) {
			server.stop();
			server = null;
			setState(STOPPED);
			logInfo(Messages.WebserverActivator_server_stopped);
		}
	}

	public void removeWebserverListener(WebserverListener listener) {
		listeners.remove(listener);
	}

	public void addWebserverListener(WebserverListener listener) {
		listeners.add(listener);
	}

	public QueryField[] getWebNodes() {
		if (webNodes == null) {
			List<QueryField> list = new ArrayList<QueryField>(100);
			StringTokenizer st = new StringTokenizer(
					Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.WEBMETADATA, "", null), //$NON-NLS-1$
					"\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				QueryField qfield = QueryField.findQueryField(st.nextToken());
				if (qfield != null && qfield.hasLabel() && qfield.getType() != QueryField.T_NONE)
					list.add(qfield);
			}
			webNodes = list.toArray(new QueryField[list.size()]);
			Arrays.sort(webNodes, new Comparator<QueryField>() {
				public int compare(QueryField q1, QueryField q2) {
					return q1.getLabel().compareTo(q2.getLabel());
				}
			});
		}
		return webNodes;
	}

	public void addPreferenceChangeListener(IPreferenceChangeListener preferenceListener) {
		InstanceScope.INSTANCE.getNode(PLUGIN_ID).addPreferenceChangeListener(preferenceListener);
	}

	public void removePreferenceChangeListener(IPreferenceChangeListener preferenceListener) {
		InstanceScope.INSTANCE.getNode(PLUGIN_ID).removePreferenceChangeListener(preferenceListener);
	}

	protected String compilePage(String name, Map<String, String> substitutions) {
		StringBuilder sb = new StringBuilder(getPreferenceStore().getString(name));
		for (Map.Entry<String, String> entry : substitutions.entrySet())
			replaceText(sb, entry.getKey(), entry.getValue());
		return sb.toString();
	}

	private static void replaceText(StringBuilder sb, String var, String text) {
		int p = sb.indexOf(var);
		while (p >= 0) {
			if (var.startsWith("{$$")) { //$NON-NLS-1$
				if (text == null) {
					sb.delete(p, p + var.length());
					int q = findClosing(sb, p + 1);
					if (q > p) {
						sb.delete(q, q + 4);
						p = sb.indexOf(var, q);
					} else
						p = sb.indexOf(var, p + 1);
				} else {
					int q = findClosing(sb, p + 1);
					if (q > p)
						sb.replace(p, q + 4, text);
					p = sb.indexOf(var, p + 1);
				}
			} else {
				if (text == null || text.isEmpty()) {
					sb.delete(p, p + var.length());
					p = sb.indexOf(var, p + 1);
				} else {
					sb.replace(p, p + var.length(), text);
					p = sb.indexOf(var, p + text.length());
				}
			}
		}
	}

	private static int findClosing(StringBuilder sb, int from) {
		int level = 0;
		int start = from;
		while (start < sb.length()) {
			int q = sb.indexOf("{$$", start); //$NON-NLS-1$
			if (q < 0)
				return q;
			if (q + 3 < sb.length() && sb.charAt(q + 3) == '}') {
				if (level-- == 0)
					return q;
				start = q + 4;
			} else {
				++level;
				start = q + 3;
			}
		}
		return -1;
	}

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, null));
	}

	public void logError(String message, Exception e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (PreferenceConstants.WEBMETADATA.equals(event.getKey()))
			webNodes = null;
		if (PreferenceConstants.STARTPAGE.equals(event.getKey()))
			server.getVirtualHost(null)
					.setDirectoryIndex(getPreferenceStore().getString(PreferenceConstants.STARTPAGE));
	}

	public CaptionProcessor getCaptionProcessor() {
		return captionProcessor;
	}

}
