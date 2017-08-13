/* Copyright 2009-2015 Berthold Daum

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.bdaum.zoom.net.core.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IFTPService;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.net.core.internal.Activator;
import com.bdaum.zoom.net.core.internal.Base64;
import com.bdaum.zoom.net.core.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.program.BatchUtilities;

public class FtpAccount {

	private static final String GUEST = "zoraPD@photozora.org"; //$NON-NLS-1$
	private static final String TRACK_EXPORT = "trackExport"; //$NON-NLS-1$
	private static final String WEB_HOST = "webHost"; //$NON-NLS-1$
	private static final String PREFIX = "prefix"; //$NON-NLS-1$
	private static final String PORT = "port"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String PASSIVE_MODE = "passiveMode"; //$NON-NLS-1$
	private static final String NOTES = "notes"; //$NON-NLS-1$
	private static final String LOGIN = "login"; //$NON-NLS-1$
	private static final String HOST = "host"; //$NON-NLS-1$
	private static final String DIRECTORY = "directory"; //$NON-NLS-1$
	private static final String ANONYMOUS = "anonymous"; //$NON-NLS-1$
	private static final String ACCOUNT = "account"; //$NON-NLS-1$
	private static final char FIELDSEP = '\02';
	private static final String FIELDSEPS = new String(new char[] { FIELDSEP });
	private static final char SEP = '\01';
	private static final String SEPS = new String(new char[] { SEP });
	private String name;
	private String host;
	private String login;
	private String password;
	private String subAccount;
	private boolean anonymous;
	private boolean passiveMode = true;
	private int port = 21;
	private String directory;
	private String webHost;
	private String prefix;
	private String notes;
	private boolean skipAll;
	private boolean replaceAll;
	private int filecount;
	private boolean trackExport = true;

	/**
	 * Finds an FTP account in the systems preference settings
	 *
	 * @param name
	 *            - account name
	 * @return account object
	 */
	public static FtpAccount findAccount(String name) {
		String ess = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.FTPACCOUNTS, "", null); //$NON-NLS-1$
		String search = name + FIELDSEP;
		StringTokenizer st = new StringTokenizer(ess, SEPS);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.startsWith(search))
				return new FtpAccount(s);
		}
		return null;
	}

	/**
	 * Obtains all FTP accounts defined in the systems preference settings
	 *
	 * @return list of account objects
	 */
	public static List<FtpAccount> getAllAccounts() {
		ArrayList<FtpAccount> ftpAccounts = new ArrayList<FtpAccount>();
		String s = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.FTPACCOUNTS, "", null); //$NON-NLS-1$
		if (s != null) {
			StringTokenizer st = new StringTokenizer(s, SEPS);
			while (st.hasMoreTokens())
				ftpAccounts.add(new FtpAccount(st.nextToken()));
		}
		return ftpAccounts;
	}

	/**
	 * Saves account objects to the systems preference settings
	 *
	 * @param ftpAccounts
	 *            - list of account objects
	 */
	public static void saveAccounts(List<FtpAccount> ftpAccounts) {
		StringBuilder sb = new StringBuilder(4096);
		for (FtpAccount ftpAccount : ftpAccounts)
			if (ftpAccount.getName() != null
					&& !ftpAccount.getName().isEmpty())
				sb.append(ftpAccount.toString()).append(SEP);
		BatchUtilities.putPreferences(InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID), PreferenceConstants.FTPACCOUNTS, sb.toString());
	}

	private FtpAccount(String s) {
		StringTokenizer st = new StringTokenizer(s, FIELDSEPS);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int p = token.indexOf('=');
			if (p < 0)
				name = token;
			else {
				String key = token.substring(0, p).intern();
				String value = token.substring(p + 1);
				if (key == ACCOUNT)
					subAccount = value;
				else if (key == ANONYMOUS)
					anonymous = Boolean.parseBoolean(value);
				else if (key == DIRECTORY)
					directory = value;
				else if (key == HOST)
					host = value;
				else if (key == LOGIN)
					login = value;
				else if (key == NOTES)
					notes = value;
				else if (key == PASSIVE_MODE)
					passiveMode = Boolean.parseBoolean(value);
				else if (key == TRACK_EXPORT)
					trackExport = Boolean.parseBoolean(value);
				else if (key == PASSWORD)
					password = value;
				else if (key == PORT)
					try {
						port = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						// Use default
					}
				else if (key == PREFIX)
					prefix = value;
				else if (key == WEB_HOST)
					webHost = value;
			}
		}
	}

	/**
	 * Constructor
	 */
	public FtpAccount() {
		// Empty account
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(name);
		appendString(sb, ACCOUNT, subAccount);
		appendString(sb, ANONYMOUS, String.valueOf(anonymous));
		appendString(sb, DIRECTORY, directory);
		appendString(sb, HOST, host);
		appendString(sb, LOGIN, login);
		appendString(sb, NOTES, notes);
		appendString(sb, PASSIVE_MODE, String.valueOf(passiveMode));
		appendString(sb, TRACK_EXPORT, String.valueOf(trackExport));
		appendString(sb, PASSWORD, password);
		appendString(sb, PORT, String.valueOf(port));
		appendString(sb, PREFIX, prefix);
		appendString(sb, WEB_HOST, webHost);
		return sb.toString();
	}

	private static void appendString(StringBuilder sb, String key, String value) {
		if (value != null && !value.isEmpty())
			sb.append(FIELDSEP).append(key).append('=').append(value);
	}

	/**
	 * Retrieves the FTP host
	 *
	 * @return FTP host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the FTP host
	 *
	 * @param host
	 *            - FTP host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Retrieves the login ID
	 *
	 * @return - login ID
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Sets the login ID
	 *
	 * @param login
	 *            - login ID
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * Retrieves the password
	 *
	 * @return - password
	 */
	public String getPassword() {
		return decode(password);
	}

	private static String decode(String pw) {
		if (pw != null)
			try {
				byte[] bytes = Base64.decode(pw);
				return new String(bytes);
			} catch (IOException e) {
				// should never happen
			}
		return pw;
	}

	/**
	 * Sets the password
	 *
	 * @param password
	 *            - account password
	 */
	public void setPassword(String password) {
		this.password = encode(password);
	}

	private static String encode(String pw) {
		return pw != null ? Base64.encodeBytes(pw.getBytes())
				: null;
	}

	/**
	 * Indicates if account is accessed anonymously
	 *
	 * @return true if accessed anonymously
	 */
	public boolean isAnonymous() {
		return anonymous;
	}

	/**
	 * Sets if account is accessed anonymously
	 *
	 * @param anonymous
	 *            - true if accessed anonymously
	 */
	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	/**
	 * Indicates if account is in passive mode
	 *
	 * @return true if account is in passive mode
	 */
	public boolean isPassiveMode() {
		return passiveMode;
	}

	/**
	 * Sets passive mode
	 *
	 * @param passiveMode
	 *            - true if account is set to passive mode
	 */
	public void setPassiveMode(boolean passiveMode) {
		this.passiveMode = passiveMode;
	}

	/**
	 * Indicates if uploads shall be tracked
	 *
	 * @return - true if uploads shall be tracked
	 */
	public boolean isTrackExport() {
		return trackExport;
	}

	/**
	 * Sets the track mode
	 *
	 * @param trackExport
	 *            - true if uploads shall be tracked
	 */
	public void setTrackExport(boolean trackExport) {
		this.trackExport = trackExport;
	}

	/**
	 * Retrieves port number
	 *
	 * @return - port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets port number
	 *
	 * @param port
	 *            - port number
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Retrieves target directory
	 *
	 * @return target directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Sets target directory
	 *
	 * @param directory
	 *            - target directory
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * Retrieves web host url
	 *
	 * @return web host url or null
	 */
	public String getWebHost() {
		return webHost;
	}

	/**
	 * Sets web host url
	 *
	 * @param webHost
	 *            - web host url
	 */
	public void setWebHost(String webHost) {
		this.webHost = webHost;
	}

	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the directory that is mapped to the web host The target directory
	 * must start with this prefix
	 *
	 * @param prefix
	 *            - directory prefix or null
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Sets the account name
	 *
	 * @param name
	 *            - account name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves the account notes
	 *
	 * @return account notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the account notes
	 *
	 * @param notes
	 *            - account notes
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Retrieves the sub account information
	 *
	 * @return sub account or null
	 */
	public String getSubAccount() {
		return subAccount;
	}

	/**
	 * Sets sub account information
	 *
	 * @param subAccount
	 *            - sub account
	 */
	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}

	/**
	 * Tests if the account is valid
	 *
	 * @param adaptable
	 *            - Adaptable object. Must at least provide a Shell instance
	 * @return - error message in case of failure, null in case of success
	 */
	public String test(IAdaptable adaptable) {
		try {
			transferFiles(null, new NullProgressMonitor(), adaptable, false);
		} catch (IOException e) {
			return e.getMessage();
		}
		return null;
	}

	/**
	 * Opens the specified Web URL in a browser
	 *
	 * @return - error message in case of failure, null in case of success
	 */
	public String testWebUrl() {
		String wh = getWebUrl();
		if (wh != null) {
			try {
				IWebBrowser browser = PlatformUI.getWorkbench()
						.getBrowserSupport().getExternalBrowser();
				browser.openURL(new URL(BatchUtilities.encodeBlanks(wh)));
			} catch (PartInitException e) {
				// ignore
			} catch (MalformedURLException e) {
				return NLS.bind(Messages.FtpAccount_bad_url, wh);
			}
		}
		return null;
	}

	/**
	 * Returns the web URL of the target directory
	 *
	 * @return WebURL of the target directory
	 */
	public String getWebUrl() {
		String wh = getWebHost();
		if (wh == null || wh.isEmpty())
			return null;
		wh = Core.furnishWebUrl(wh);
		String pfx = getPrefix();
		String dir = getDirectory();
		if (dir != null) {
			if (pfx != null && !pfx.isEmpty() && dir.startsWith(pfx))
				dir = dir.substring(pfx.length());
			dir = stripSlashes(dir);
			wh += '/' + dir;
		}
		return wh;
	}

	/**
	 * Returns the URL of the target directory
	 *
	 * @return URL of target directory
	 */
	public String getUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append("ftp://").append(getHost()).append(':').append( //$NON-NLS-1$
				String.valueOf(getPort())).append('/')
				.append(stripSlashes(getDirectory()));
		return sb.toString();
	}

	/**
	 * Computes the target location of an uploaded file
	 *
	 * @param file
	 *            - file to be uploaded
	 * @return - URI of target location
	 */
	public String computeTargetURI(File file) {
		String u;
		String wh = getWebUrl();
		if (wh != null) {
			u = wh + '/' + file.getName();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(IFTPService.FTPSCHEME)
					.append("://").append(getHost()).append('/').append( //$NON-NLS-1$
							getDirectory()).append('/').append(file.getName());
			u = sb.toString();
		}
		return BatchUtilities.encodeBlanks(u);
	}

	/**
	 * Login into a account
	 *
	 * @return FTPClient object or null
	 * @throws IOException
	 */
	public FTPClient login() throws IOException {
		int reply = 0;
		FTPClient ftp = new FTPClient();
		try {
			if (port != 0)
				ftp.connect(getHost(), getPort());
			else
				ftp.connect(getHost());
			if (isAnonymous())
				ftp.login(ANONYMOUS, GUEST);
			else if (getSubAccount() != null && !getSubAccount().isEmpty())
				ftp.login(getLogin(), getPassword(), getSubAccount());
			else
				ftp.login(getLogin(), getPassword());
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new IOException(NLS.bind(
						Messages.FtpAccount_ftp_server_refused,
						ftp.getReplyString()));
			if (isPassiveMode())
				ftp.enterLocalPassiveMode();
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
			throw e;
		}
		return ftp;
	}

	/**
	 * Transfers files to the FTP account
	 *
	 * @param files
	 *            - files to be transferred
	 * @param monitor
	 *            - progress monitor
	 * @param adaptable
	 *            - adaptable instance. Must at least provide a Shell instance
	 * @param deleteTransferred
	 *            - true if transferred files shall be deleted on the client
	 *            site
	 * @return number of transferred files or -1 in case of abort
	 * @throws IOException
	 */
	public int transferFiles(File[] files, IProgressMonitor monitor,
			IAdaptable adaptable, boolean deleteTransferred) throws IOException {
		filecount = countFiles(files);
		monitor.beginTask(Messages.FtpAccount_uploading_files, filecount + 1);
		FTPClient ftp = null;
		try {
			ftp = login();
			String dir = stripSlashes(getDirectory());
			int n = 0;
			boolean result = ftp.changeWorkingDirectory(Core
					.encodeUrlSegment(dir));
			if (!result) {
				if (monitor.isCanceled())
					return -1;
				boolean createDir = true;
				IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
				if (errorHandler != null)
					createDir = errorHandler.question(
							Messages.FtpAccount_directory_does_not_exist,
							Messages.FtpAccount_specified_dir_does_not_exist,
							adaptable);
				if (createDir) {
					StringTokenizer st = new StringTokenizer(dir, "/"); //$NON-NLS-1$
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						ftp.makeDirectory(token);
						result = ftp.changeWorkingDirectory(Core
								.encodeUrlSegment(token));
						if (!result)
							throw new IOException(
									Messages.FtpAccount_directory_creation_failed);
					}

				} else
					throw new IOException(
							Messages.FtpAccount_target_dir_does_not_exist);
			}
			if (files != null) {
				monitor.worked(1);
				skipAll = false;
				replaceAll = false;
				if (monitor.isCanceled())
					return -1;
				n = transferFiles(ftp, files, monitor, adaptable,
						deleteTransferred);
			}

			// transfer files
			if (isPassiveMode())
				ftp.enterLocalActiveMode();
			ftp.logout();
			return n;
		} finally {
			monitor.done();
			if (ftp != null && ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
		}
	}

	private static String stripSlashes(String dir) {
		while (dir.startsWith("/")) //$NON-NLS-1$
			dir = dir.substring(1);
		return dir;
	}

	private int countFiles(File[] files) {
		if (files != null) {
			int n = 0;
			for (File file : files) {
				if (file.isDirectory())
					n += countFiles(file.listFiles());
				else
					n++;
			}
			return n;
		}
		return 0;
	}

	@SuppressWarnings("fallthrough")
	private int transferFiles(FTPClient ftp, File[] files,
			IProgressMonitor monitor, IAdaptable adaptable,
			boolean deleteTransferred) throws IOException {
		if (monitor.isCanceled())
			return -1;
		FTPFile[] oldfiles = ftp.listFiles();
		if (monitor.isCanceled())
			return -1;
		Map<String, FTPFile> names = new HashMap<String, FTPFile>();
		for (FTPFile file : oldfiles)
			names.put(file.getName(), file);
		int n = 0;
		for (File file : files) {
			final String filename = file.getName();
			FTPFile ftpFile = names.get(filename);
			if (file.isDirectory()) {
				if (ftpFile != null) {
					if (!ftpFile.isDirectory())
						throw new IOException(
								NLS.bind(
										Messages.FtpAccount_cannot_replace_file_with_subdir,
										filename));
					boolean result = ftp.changeWorkingDirectory(Core
							.encodeUrlSegment(filename));
					if (!result)
						throw new IOException(
								NLS.bind(
										Messages.FtpAccount_cannot_change_to_working_dir,
										filename));
					// System.out.println(filename + " is new directory"); //$NON-NLS-1$
				} else {
					ftp.makeDirectory(filename);
					boolean result = ftp.changeWorkingDirectory(Core
							.encodeUrlSegment(filename));
					if (!result)
						throw new IOException(
								Messages.FtpAccount_creation_of_subdir_failed);
					// System.out.println(filename + " is new directory"); //$NON-NLS-1$
				}
				if (monitor.isCanceled())
					return -1;
				int c = transferFiles(ftp, file.listFiles(), monitor,
						adaptable, deleteTransferred);
				if (c < 0)
					return -1;
				n += c;
				ftp.changeToParentDirectory();
				// System.out.println("Returned to parent directory"); //$NON-NLS-1$
			} else {
				if (ftpFile != null) {
					if (ftpFile.isDirectory())
						throw new IOException(
								NLS.bind(
										Messages.FtpAccount_cannot_replace_subdir_with_file,
										filename));
					if (skipAll) {
						if (deleteTransferred)
							file.delete();
						continue;
					}
					if (!replaceAll) {
						if (monitor.isCanceled())
							return -1;
						int ret = 4;
						IDbErrorHandler errorHandler = Core.getCore()
								.getErrorHandler();
						if (errorHandler != null) {
							String[] buttons = (filecount > 1) ? new String[] {
									Messages.FtpAccount_overwrite,
									Messages.FtpAccount_overwrite_all,
									IDialogConstants.SKIP_LABEL,
									Messages.FtpAccount_skip_all,
									IDialogConstants.CANCEL_LABEL }
									: new String[] {
											Messages.FtpAccount_overwrite,
											IDialogConstants.SKIP_LABEL,
											IDialogConstants.CANCEL_LABEL };
							ret = errorHandler
									.showMessageDialog(
											Messages.FtpAccount_file_already_exists,
											null,
											NLS.bind(
													Messages.FtpAccount_file_exists_overwrite,
													filename),
											MessageDialog.QUESTION, buttons, 0,
											adaptable);
						}
						if (filecount > 1) {
							switch (ret) {
							case 0:
								break;
							case 1:
								replaceAll = true;
								break;
							case 3:
								skipAll = true;
								/* FALL-THROUGH */
							case 2:
								if (deleteTransferred)
									file.delete();
								continue;
							default:
								return -1;
							}
						} else {
							switch (ret) {
							case 0:
								break;
							case 1:
								if (deleteTransferred)
									file.delete();
								continue;
							default:
								return -1;
							}
						}
					}
					ftp.deleteFile(Core.encodeUrlSegment(filename));
					//					System.out.println(filename + " deleted"); //$NON-NLS-1$
				}
				try (BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file))) {
					ftp.storeFile(Core.encodeUrlSegment(filename), in);
					//					System.out.println(filename + " stored"); //$NON-NLS-1$
					n++;
				} finally {
					if (deleteTransferred)
						file.delete();
					monitor.worked(1);
				}
			}
		}
		return n;
	}

}
