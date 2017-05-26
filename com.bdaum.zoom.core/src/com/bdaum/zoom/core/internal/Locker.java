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

package com.bdaum.zoom.core.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Internal class.
 */
public class Locker {
	private File lockFile;

	private FileLock fileLock;

	private RandomAccessFile raFile;

	public Locker(File lockFile) {
		this.lockFile = lockFile;
	}

	/**
	 * Tries to lock the application workspace
	 * @return true if the application workspace was already locked
	 * @throws IOException
	 */
	public synchronized boolean lock() throws IOException {
		raFile = new RandomAccessFile(lockFile, "rw"); //$NON-NLS-1$
		try {
			fileLock = raFile.getChannel().tryLock(0, 1, false);
		} catch (IOException ioe) {
			throw new IOException(Messages.Locker_cannot_lock);
		} catch (OverlappingFileLockException e) {
			// handle it as null result
			fileLock = null;
		} finally {
			if (fileLock != null) {
				lockFile.deleteOnExit();
				return true;
			}
			raFile.close();
			raFile = null;
		}
		return false;
	}

	/**
	 * Releases the application workspace lock
	 */
	public synchronized void release() {
		if (fileLock != null) {
			try {
				fileLock.release();
			} catch (IOException e) {
//				e.printStackTrace();
				//don't complain, we're making a best effort to clean up
			}
			fileLock = null;
		}
		if (raFile != null) {
			try {
				raFile.close();
				lockFile.delete();
			} catch (IOException e) {
//				e.printStackTrace();
				//don't complain, we're making a best effort to clean up
			}
			raFile = null;
		}
	}
}
