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
package com.bdaum.zoom.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

/**
 * A container containing some object
 *
 */
public class Ticketbox {
	/**
	 * The object
	 */
	public Object ticket;
	private File tempFile;

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
	 * @param uri  - URI of source file
	 * @return - local or remote file
	 * @throws IOException
	 */
	public File obtainFile(URI uri) throws IOException {
		tempFile = null;
		if (Constants.FILESCHEME.equals(uri.getScheme()))
			return new File(uri);
		tempFile = download(uri);
		return tempFile;
	}

	/**
	 * Disposes of resources after file provision
	 */
	public void cleanup() {
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
	}

	/**
	 * End FTP session
	 */
	public void endSession() {
		Core.endSession(this);
	}

	public static String computeErrorMessage(List<String> errands, Set<String> volumes) {
		if (!errands.isEmpty()) {
			String msg;
			if (errands.size() == 1) {
				msg = NLS.bind(Messages.Ticketbox_file_offline, errands.get(0),
						volumes.toArray()[0]);
			} else {
				StringBuilder sb = new StringBuilder();
				for (String volume : volumes) {
					if (sb.length() > 0)
						sb.append(", "); //$NON-NLS-1$
					sb.append(volume);
				}
				msg = NLS.bind(Messages.Ticketbox_files_offline,
						errands.size(), sb.toString());

			}
			return msg;
		}
		return null;
	}

	/**
	 * Tests if a file provision was local
	 * @return - triu if local
	 */
	public boolean isLocal() {
		return tempFile == null;
	}
}
