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
 * (c) 2009-2018 Berthold Daum  
 */
package com.bdaum.zoom.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

/**
 * A Ticketbox represents an FTP session. It can download files via URI. Tempfiles are cleaned up before each download and at the end of the session
 * Ticketbox can also compile an error message from the errands. 
 * Typical use:
 *    try (Ticketbox box = new Ticketbox()) {
 *    	...
 *    	File file1 = box.obtainFile(uri1);
 *    	...
 *     	File file2 = box.obtainFile(uri2);
 *    	...
 *   	String errorMessage = box.getErrorMessage();
 *    }
 *
 */
public class Ticketbox implements AutoCloseable {

	public Object ticket;
	private File tempFile;
	private Set<String> volumes;
	private String errand;
	private int errs = 0;

	public static String computeErrorMessage(int errs, String errand1, Set<String> volumes) {
		switch (errs) {
		case 0:
			return null;
		case 1:
			if (volumes == null)
				return NLS.bind(Messages.Ticketbox_file_delete_offline, errand1);
			return NLS.bind(Messages.Ticketbox_file_offline, errand1, volumes.toArray()[0]);
		default:
			if (volumes == null)
				return NLS.bind(Messages.Ticketbox_n_files_deleted_offline, errs);
			return NLS.bind(Messages.Ticketbox_files_offline, errs, Core.toStringList(volumes.toArray(), ", ")); //$NON-NLS-1$
		}
	}

	/**
	 * Download a file
	 *
	 * @param uri
	 *            - URI of source file
	 * @return downloaded file
	 * @throws IOException
	 */
	public File download(URI uri) throws IOException {
		return Core.download(uri, this);
	}

	/**
	 * Provides a local file or downloads a remote file
	 * 
	 * @param uri
	 *            - URI of source file
	 * @return - local or from remote downloaded file
	 * @throws IOException
	 */
	public File obtainFile(URI uri) throws IOException {
		cleanup();
		if (Constants.FILESCHEME.equals(uri.getScheme()))
			return new File(uri);
		return tempFile = download(uri);
	}

	public void addErrand(String errand, String volume) {
		if (errs++ == 0)
			this.errand = errand;
		if (volume != null && !volume.isEmpty()) {
			if (volumes == null)
				volumes = new HashSet<String>(7);
			volumes.add(volume);
		}
	}

	private void cleanup() {
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
	}

	public String getErrorMessage() {
		return computeErrorMessage(errs, errand, volumes);
	}

	/**
	 * Tests if a file provision was local
	 * 
	 * @return - true if local
	 */
	public boolean isLocal() {
		return tempFile == null;
	}

	@Override
	public void close() {
		cleanup();
		if (ticket != null)
			Core.endSession(this);
	}
}
