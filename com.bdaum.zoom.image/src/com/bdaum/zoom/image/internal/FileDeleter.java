/*******************************************************************************
 * Copyright (c) 2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.image.internal;

import java.io.File;

class FileDeleter extends Thread {
	private final File cp;
	private final File tempFolder;

	public FileDeleter(File cp, File tempFolder) {
		this.cp = cp;
		this.tempFolder = tempFolder;
	}

	@Override
	public void run() {
		try {
			Runtime.getRuntime()
					.exec(new String[] {
							System.getProperty("java.home") + "/bin/java", //$NON-NLS-1$//$NON-NLS-2$
							"-cp", cp.getPath(), //$NON-NLS-1$
							getClass().getName(), tempFolder.getAbsolutePath() });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final File tempFolder = new File(args[0]);
		if (tempFolder.exists()) {
			long start = System.currentTimeMillis();
			while (true) {
				if (deleteFileOrFolder(tempFolder))
					return;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// do nothing
				}
				if (System.currentTimeMillis() - start > 15000) {
					System.err.println("Could not remove temp files"); //$NON-NLS-1$
					return;
				}
			}
		}
	}

	private static boolean deleteFileOrFolder(File in) {
		if (in.isDirectory()) {
			File[] files = in.listFiles();
			if (files == null)
				return false;
			for (File member : files)
				deleteFileOrFolder(member);
		}
		return (in.delete() || !in.exists());
	}

}