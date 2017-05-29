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

package com.bdaum.zoom.ui.internal.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public class UpdateJob extends AbstractUpdateJob {

	private static final String ALGORITHM = "SHA-512"; //$NON-NLS-1$
	private static final String UPDATE = Messages.Updater_update;
	private static final String VERSION = "{version}"; //$NON-NLS-1$
	private File downloadFile;
	private List<String> packages = new ArrayList<String>(4);
	private final Version publishedVersion;
	MessageDigest sha1;
	protected int result;

	public UpdateJob(Version publishedVersion, File downloadFile) {
		super(Messages.Updater_updater);
		this.publishedVersion = publishedVersion;
		this.downloadFile = downloadFile;
		String os = Platform.getOS();
		String ws = Platform.getWS();
		String osArch = Platform.getOSArch();
		String prefix = "ZoRa_" + publishedVersion + '_' + os + '.'; //$NON-NLS-1$
		if (Platform.OS_WIN32.equals(os)) {
			if (Platform.WS_WPF.equals(ws)) {
				packages.add(prefix + ws + '.' + Platform.ARCH_X86);
				packages.add(prefix + Platform.WS_WIN32 + '.' + Platform.ARCH_X86);
			} else if (Platform.WS_WIN32.equals(ws)) {
				if (Platform.ARCH_X86_64.equals(osArch)) {
					packages.add(prefix + ws + '.' + osArch);
					packages.add(prefix + ws + '.' + Platform.ARCH_X86);
				} else
					packages.add(prefix + ws + '.' + osArch);
			}
		} else if (Platform.OS_LINUX.equals(os)) {
			prefix = os + '.';
			if (Platform.WS_GTK.equals(ws) || Platform.WS_MOTIF.equals(ws)) {
				if (Platform.ARCH_X86_64.equals(osArch)) {
					packages.add(prefix + ws + '.' + osArch);
					packages.add(prefix + ws + '.' + Platform.ARCH_X86);
				} else
					packages.add(prefix + ws + '.' + osArch);
			} else if (Platform.WS_MOTIF.equals(ws))
				packages.add(prefix + ws + '.' + Platform.ARCH_X86);
		} else if (Platform.OS_MACOSX.equals(os)) {
			prefix = os + '.';
			if (Platform.WS_CARBON.equals(ws))
				packages.add(prefix + ws + '.' + Platform.ARCH_X86);
			else if (Platform.WS_COCOA.equals(ws)) {
				if (Platform.ARCH_X86_64.equals(osArch)) {
					packages.add(prefix + ws + '.' + osArch);
					packages.add(prefix + ws + '.' + Platform.ARCH_X86);
					packages.add(prefix + Platform.WS_CARBON + '.' + osArch);
					packages.add(prefix + Platform.WS_CARBON + '.' + Platform.ARCH_X86);
				} else {
					packages.add(prefix + ws + '.' + osArch);
					packages.add(prefix + Platform.WS_CARBON + '.' + osArch);
				}
			}
		}
	}

	private File download(String pack, IProgressMonitor monitor) {
		if (sha1 == null)
			try {
				sha1 = MessageDigest.getInstance(ALGORITHM); 
			} catch (NoSuchAlgorithmException e) {
				// should never happen
			}
		String filename = pack + "_install.jar"; //$NON-NLS-1$
		String sha1name = filename + ".sha-1.txt"; //$NON-NLS-1$
		File file = null;
		StringTokenizer st = new StringTokenizer(System.getProperty("com.bdaum.zoom.mirrors"), ","); //$NON-NLS-1$ //$NON-NLS-2$
		while (st.hasMoreTokens() && !monitor.isCanceled()) {
			String token = st.nextToken();
			token = replaceVersion(token, publishedVersion);
			String uri = token + filename;
			try {
				monitor.subTask(Messages.Updater_downloading);
				file = File.createTempFile(Constants.APPNAME + "Installer.JAR", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
				URLConnection uc = new URI(uri).toURL().openConnection();
				int contentLength = uc.getContentLength();
				if (!"application/x-java-archive".equals(uc.getContentType()) || contentLength == -1) //$NON-NLS-1$
					continue;
				if (monitor.isCanceled())
					break;
				InputStream bin = new BufferedInputStream(uc.getInputStream());
				int len = Core.copyBytesToFile(bin, file, monitor);
				if (len <= 0 || len != contentLength) {
					UiActivator.getDefault().logError(
							NLS.bind(Messages.Updater_download_failed, new Object[] { file, contentLength, len }),
							null);
					continue;
				}
				if (monitor.isCanceled())
					break;
				UiActivator.getDefault().logInfo(NLS.bind(Messages.Updater_download_successful, file));
				if (sha1 != null) {
					monitor.subTask(Messages.Updater_checking);
					String filepath = file.getAbsolutePath();
					try {
						String hash = calculateHash(sha1, filepath);
						if (monitor.isCanceled())
							break;
						String property = System.getProperty("com.bdaum.zoom.hash"); //$NON-NLS-1$
						property = replaceVersion(property, publishedVersion);
						try (Reader reader = new InputStreamReader(new URI(property + sha1name).toURL().openStream())) {
							char[] cbuf = new char[40];
							int read = reader.read(cbuf);
							if (read < 40) {
								UiActivator.getDefault().logError(Messages.Updater_bad_hash_key, null);
								return null;
							}
							if (monitor.isCanceled())
								break;
							if (!new String(cbuf).equals(hash)) {
								UiActivator.getDefault()
										.logError(NLS.bind(Messages.Updater_key_does_not_match, filepath), null);
								continue;
							}
							return file;
						} catch (URISyntaxException e) {
							UiActivator.getDefault().logError(NLS.bind(Messages.Updater_illegal_sha1, uri), e);
							return null;
						} catch (IOException e) {
							UiActivator.getDefault().logError(NLS.bind(Messages.Updater_download_sha1_failed, property),
									e);
							return null;
						}
					} catch (Exception e) {
						UiActivator.getDefault().logError(NLS.bind(Messages.Updater_computation_sha1_failed, filepath),
								e);
						continue;
					}
				}
				break;
			} catch (IOException e1) {
				UiActivator.getDefault().logError(NLS.bind(Messages.Updater_download_from_update_site_failed, token),
						e1);
			} catch (URISyntaxException e1) {
				UiActivator.getDefault().logError(NLS.bind(Messages.Updater_illegal_uri_during_update, uri), e1);
			}
		}
		return null;
	}

	private static String replaceVersion(String property, Version version) {
		int p = property.indexOf(VERSION);
		if (p < 0)
			return property;
		return property.substring(0, p) + version.toString() + property.substring(p + VERSION.length());
	}

	public static String calculateHash(MessageDigest algorithm, String fileName) throws Exception {
		try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(fileName)),
				algorithm)) {
			// read the file and update the hash calculation
			while (dis.read() != -1) {
				// do nothing
			}
			return byteArray2Hex(algorithm.digest());
		}
	}

	private static String byteArray2Hex(byte[] hash) {
		try (Formatter formatter = new Formatter()) {
			for (byte b : hash)
				formatter.format("%02x", b); //$NON-NLS-1$
			return formatter.toString();
		}
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		monitor.beginTask(Messages.Updater_updating, IProgressMonitor.UNKNOWN);
		if (downloadFile == null)
			for (String pack : packages) {
				if (monitor.isCanceled())
					break;
				downloadFile = download(pack, monitor);
				if (downloadFile != null)
					break;
			}
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		Display display = Display.getDefault();
		if (display.isDisposed())
			return Status.CANCEL_STATUS;
		if (downloadFile == null) {
			display.syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, Constants.APPNAME + UPDATE,
							Messages.Updater_download_of_new_version_failed);
				}
			});
			return Status.CANCEL_STATUS;
		}
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		display.syncExec(new Runnable() {
			public void run() {
				AcousticMessageDialog dialog = new AcousticMessageDialog(null, Constants.APPNAME + UPDATE, null,
						NLS.bind(Messages.Updater_installer_package_successfully_downloaded, downloadFile.getName()),
						MessageDialog.QUESTION, new String[] { Messages.Updater_yes, Messages.Updater_no }, 0);
				result = dialog.open();
			}
		});
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		switch (result) {
		case 0:
			Location installLocation = Platform.getInstallLocation();
			URL url = installLocation == null ? null : installLocation.getURL();
			URI uri = null;
			if (url != null)
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					// ignore
				}
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			String[] cmdarray = null;
			if (url != null) {
				String instancepath = Platform.getInstanceLocation().getURL().getPath();
				String installpath = Platform.getInstallLocation().getURL().getPath();
				String installType = (instancepath.startsWith(installpath)) ? "install" : "user"; //$NON-NLS-1$ //$NON-NLS-2$
				Properties props = new Properties();
				props.put("INSTALL_PATH", new File(uri).getParentFile().getPath()); //$NON-NLS-1$
				props.put("configuration.installFor", installType); //$NON-NLS-1$
				try {
					File propertyFile = File.createTempFile(Constants.APPNAME + "Installer.Properties", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
					try (FileOutputStream out = new FileOutputStream(propertyFile)) {
						props.store(out, null);
					}
					cmdarray = new String[] { downloadFile.getAbsolutePath(), "-options", propertyFile.getPath() };//$NON-NLS-1$
				} catch (IOException e) {
					// do nothing
				}
			}
			if (cmdarray == null)
				cmdarray = new String[] { downloadFile.getAbsolutePath() };
			UiActivator.getDefault().setUpdaterCommand(cmdarray);
			return Status.OK_STATUS;
		default:
			return Status.CANCEL_STATUS;
		}
	}

}
