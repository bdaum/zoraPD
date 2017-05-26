/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum - adapted to JDIC, improved diagnostics
 *******************************************************************************/
package org.jdesktop.jdic.init;

import java.io.*;

import org.eclipse.osgi.util.NLS;

public class Library {

	static final String SEPARATOR;
	static StringBuilder cause = new StringBuilder(); // bd

	/* 64-bit support */
	static/* final */boolean IS_64 = 0x1FFFFFFFFL == (int /* long */) 0x1FFFFFFFFL;
	static final String SUFFIX_64 = "-64"; //$NON-NLS-1$
	static final String JDICDIR_32 = "jdiclib-32"; //$NON-NLS-1$
	static final String JDICDIR_64 = "jdiclib-64"; //$NON-NLS-1$

	static {
		SEPARATOR = System.getProperty("file.separator");
	}

	static boolean extract(String fileName, String mappedName) {
		FileOutputStream os = null;
		InputStream is = null;
		File file = new File(fileName);
		boolean extracted = false;
		try {
			if (!file.exists()) {
				is = Library.class.getResourceAsStream("/" + mappedName); //$NON-NLS-1$
				if (is != null) {
					extracted = true;
					int read;
					byte[] buffer = new byte[4096];
					os = new FileOutputStream(fileName);
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
					}
					os.close();
					is.close();
					if (!JdicManager.getPlatform().equals("windows")) { //$NON-NLS-1$
						try {
							Runtime
									.getRuntime()
									.exec(
											new String[] {
													"chmod", "755", fileName }).waitFor(); //$NON-NLS-1$ //$NON-NLS-2$
						} catch (Throwable e) {
						}
					}
				}
			}
			if (load(fileName))
				return true;
		} catch (Throwable e) {
			try {
				if (os != null)
					os.close();
			} catch (IOException e1) {
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e1) {
			}
			if (extracted && file.exists())
				file.delete();
		}
		return false;
	}

	static boolean load(String libName) {
		try {
			if (libName.indexOf(SEPARATOR) != -1) {
				System.load(libName);
			} else {
				System.loadLibrary(libName);
			}
			return true;
		} catch (UnsatisfiedLinkError e) {
			if (cause.length() > 0)
				cause.append("; ");
			cause.append(e);
		}
		return false;
	}

	public static void loadLibrary(String name) {
		loadLibrary(name, true);
	}

	public static void loadLibrary(String name, boolean mapName) {
		cause.setLength(0); // bd
		String prop = System.getProperty("sun.arch.data.model"); //$NON-NLS-1$
		if (prop == null)
			prop = System.getProperty("com.ibm.vm.bitmode"); //$NON-NLS-1$
		if (prop != null) {
			if ("32".equals(prop) && IS_64) { //$NON-NLS-1$
				throw new UnsatisfiedLinkError(
						"Cannot load 64-bit JDIC libraries on 32-bit JVM"); //$NON-NLS-1$
			}
			if ("64".equals(prop) && !IS_64) { //$NON-NLS-1$
				throw new UnsatisfiedLinkError(
						"Cannot load 32-bit JDIC libraries on 64-bit JVM"); //$NON-NLS-1$
			}
		}

		/* Compute the library name and mapped name */
//		String libName1, libName2, mappedName1, mappedName2;  // bd
		// if (mapName) {
		//		libName1 = name + "-" + Platform.PLATFORM + "-" + version;  //$NON-NLS-1$ //$NON-NLS-2$
		//		libName2 = name + "-" + Platform.PLATFORM;  //$NON-NLS-1$
		// mappedName1 = mapLibraryName (libName1);
		// mappedName2 = mapLibraryName (libName2);
		// } else {
//		libName1 = libName2 = mappedName1 = mappedName2 = name;
		// }
		String libName1, mappedName1;  // bd
		libName1 = mappedName1 = name;
		 if (mapName) 
			 mappedName1 = mapLibraryName (libName1);


		/* Try loading library from jdic library path */
		String path = System.getProperty("jdic.library.path"); //$NON-NLS-1$
		if (path != null) {
			path = new File(path).getAbsolutePath();
			if (load(path + SEPARATOR + mappedName1))
				return;
//			if (mapName && load(path + SEPARATOR + mappedName2)) // bd
//				return;
		}

		/* Try loading library from java library path */
		if (load(libName1))  // bd
			return;
//		if (mapName && load(libName2))
//			return;

		/*
		 * Try loading library from the tmp directory if jdic library path is not
		 * specified
		 */
		String fileName1 = mappedName1;
//		String fileName2 = mappedName2;  // bd
		if (path == null) {
			path = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
			File dir = new File(path, IS_64 ? JDICDIR_64 : JDICDIR_32);
			boolean make = false;
			if ((dir.exists() && dir.isDirectory()) || (make = dir.mkdir())) {
				path = dir.getAbsolutePath();
				if (make && !JdicManager.getPlatform().equals("windows")) { //$NON-NLS-1$
					try {
						Runtime
								.getRuntime()
								.exec(new String[] { "chmod", "777", path }).waitFor(); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (Throwable e) {
					}
				}
			} else {
				/* fall back to using the tmp directory */
				if (IS_64) {
					fileName1 = mapLibraryName(libName1 + SUFFIX_64);
//					fileName2 = mapLibraryName(libName2 + SUFFIX_64); // bd
				}
			}
			if (load(path + SEPARATOR + fileName1))
				return;
//			if (mapName && load(path + SEPARATOR + fileName2))  // bd
//				return;
		}

		/* Try extracting and loading library from jar */
		if (path != null) {
			if (extract(path + SEPARATOR + fileName1, mappedName1))
				return;
//			if (mapName && extract(path + SEPARATOR + fileName2, mappedName2))  // bd
//				return;
		}

		/* Failed to find the library */
		String msg = NLS.bind("no {0} in jdic.library.path, java.library.path or the jar file", libName1);
		if (cause.length() > 0)
			msg += ": " + cause;
		throw new UnsatisfiedLinkError(
//				"no "	+ libName1 + " or " + libName2 + " in jdic.library.path, java.library.path or the jar file"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				msg);
	}

	static String mapLibraryName(String libName) {
		/*
		 * JDIC libraries in the Macintosh use the extension .jnilib but the some
		 * VMs map to .dylib.
		 */
		libName = System.mapLibraryName(libName);
		String ext = ".dylib"; //$NON-NLS-1$
		if (libName.endsWith(ext)) {
			libName = libName.substring(0, libName.length() - ext.length())
					+ ".jnilib"; //$NON-NLS-1$
		}
		return libName;
	}

}
