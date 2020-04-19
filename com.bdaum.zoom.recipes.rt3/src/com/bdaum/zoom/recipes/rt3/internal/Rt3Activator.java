/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.rt3.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.program.BatchUtilities;

public class Rt3Activator extends Plugin {

	private static final String LOCATE = "locate"; //$NON-NLS-1$

	private static final String LOCALAPPDATA = "LOCALAPPDATA"; //$NON-NLS-1$

	private static final String RAW_THERAPEE = "RawTherapee"; //$NON-NLS-1$

	private static final String RAW_THERAPEE2 = "Raw Therapee"; //$NON-NLS-1$

	private File[] profiles;

	private static final String PROGRAM_FILES = "ProgramFiles"; //$NON-NLS-1$

	private static final String CACHEDPROFILES = "cache/profiles"; //$NON-NLS-1$

	private static final String LINUXPROFILES = "/.config/RawTherapee/" + CACHEDPROFILES; //$NON-NLS-1$

	private static final String HOME = "HOME"; //$NON-NLS-1$

	private static final String APPDATA = "APPDATA"; //$NON-NLS-1$

	protected static final String PP3 = ".pp3"; //$NON-NLS-1$
	private static char[] hexChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.recipes.rt3"; //$NON-NLS-1$

	private static final String[] LOCATERAWTHERAPEE = new String[] { LOCATE, RAW_THERAPEE };

	// The shared instance
	private static Rt3Activator plugin;

	private MessageDigest md5;
	private StringBuilder formatBuilder = new StringBuilder(32);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		List<File> profileList = new ArrayList<File>(3);
		String os = Platform.getOS();
		if (os.equals(Platform.OS_WIN32)) {
			collectCacheProfiles(profileList, APPDATA);
			collectCacheProfiles(profileList, LOCALAPPDATA);
			collectCacheProfiles(profileList, PROGRAM_FILES);
		} else if (os.equals(Platform.OS_LINUX)) {
			String home = System.getenv(HOME);
			File rtProfiles = new File(home + LINUXPROFILES);
			if (rtProfiles.exists())
				profileList.add(rtProfiles);
			try {
				String result = null;
				try {
					result = BatchUtilities.executeCommand(LOCATERAWTHERAPEE, null, Messages.Rt3Activator_locate_rt,
							IStatus.OK, IStatus.WARNING, -1, 1000L, "UTF-8", null); //$NON-NLS-1$
				} catch (IOException e) {
					// not found
				}
				if (result != null) {
					StringTokenizer st = new StringTokenizer(result, "\n"); //$NON-NLS-1$
					while (st.hasMoreTokens()) {
						String line = st.nextToken().trim();
						int p = line.lastIndexOf(RAW_THERAPEE);
						if (p >= 0) {
							int q = line.indexOf('/', p + 10);
							if (q < 0) {
								File rtSharedProfiles = new File(line, CACHEDPROFILES);
								if (rtSharedProfiles.exists() && !profileList.contains(rtSharedProfiles))
									profileList.add(rtSharedProfiles);
							}
						}
					}
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.indexOf("(1)") < 0 || message.indexOf(RAW_THERAPEE) < 0 //$NON-NLS-1$
						|| message.indexOf(LOCATE) < 0)
					logError(Messages.Rt3Activator_cannot_execute_locate, e);
			}
		}
		profiles = profileList.toArray(new File[profileList.size()]);
		md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
	}

	private static void collectCacheProfiles(List<File> profileList, String env) {
		String targetFolder = System.getenv(env);
		File target = new File(targetFolder);
		if (target.exists()) {
			File[] rtFiles = target.listFiles(new FileFilter() {
				public boolean accept(File f) {
					String name = f.getName();
					return name.startsWith(RAW_THERAPEE) || name.startsWith(RAW_THERAPEE2);
				}
			});
			for (File rtFile : rtFiles) {
				File profile = new File(rtFile, CACHEDPROFILES);
				if (profile.exists())
					profileList.add(profile);
			}
		}

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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logError(java.lang.String,
	 * java.lang.Throwable)
	 */

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Rt3Activator getDefault() {
		return plugin;
	}

	public File getMetafile(String uri, String priority) {
		try {
			File origFile = new File(new URI(uri));
			File sidecar = getSidecarFile(origFile);
			File cacheFile = getCacheFile(origFile, computeHashForFile(origFile));
			return (sidecar == null || cacheFile != null && RT3Detector.METADATA_CACHE.equals(priority)) ? cacheFile
					: sidecar;
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}

	protected File getSidecarFile(File origFile) {
		File sidecar = new File(new StringBuilder().append(origFile.getAbsolutePath()).append(PP3).toString());
		return sidecar.exists() ? sidecar : null;
	}

	protected File getCacheFile(File origFile, String hash) {
		String metafilename = new StringBuilder().append(origFile.getName()).append('.').append(hash).append(PP3)
				.toString();
		for (File profileFolder : profiles) {
			File metafile = new File(profileFolder, metafilename);
			if (metafile.exists())
				return metafile;
		}
		return null;
	}

	public String computeHashForFile(File origFile) throws Exception {
		return calculateHash(md5, new ByteArrayInputStream((origFile.getPath() + origFile.length()).getBytes()));
	}

	public synchronized String calculateHash(MessageDigest algorithm, InputStream in) throws Exception {
		formatBuilder.setLength(0);
		DigestInputStream dis = new DigestInputStream(in, algorithm);
		while (dis.read() != -1) {
			// do nothing
		}
		for (byte b : algorithm.digest())
			formatBuilder.append(hexChars[((b >>> 4) & 0xf)]).append(hexChars[(b & 0xf)]);
		return formatBuilder.toString();
	}

	public File[] getMetaFolders() {
		return profiles;
	}

}
