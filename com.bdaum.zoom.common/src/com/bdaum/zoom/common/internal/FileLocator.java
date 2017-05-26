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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.common.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.bdaum.zoom.common.CommonUtilities;

public class FileLocator {

	private FileLocator() {
		// prevent instantiation
	}

	/**
	 * Find the URL for a given local path
	 *
	 * @param bundle
	 *            - bundle
	 * @param path
	 *            - local path
	 * @param encodeBlanks
	 *            - true if blanks are to be encoded
	 * @return URL or null
	 * @throws IOException
	 */
	public static URL findFileURL(Bundle bundle, String path,
			boolean encodeBlanks) throws IOException {
		URL url = org.eclipse.core.runtime.FileLocator.find(bundle, new Path(
				path), null);
		return url == null ? null : toFileURL(url, encodeBlanks);
	}

	/**
	 * Find the file for a given local path
	 *
	 * @param bundle
	 *            - bundle
	 * @param path
	 *            - local path
	 * @return File or null
	 * @throws IOException, URISyntaxException
	 */
	public static File findFile(Bundle bundle, String path)
			throws URISyntaxException, IOException {
		URL url = findFileURL(bundle, path, true);
		return url == null ? null : new File(url.toURI());
	}

	/**
	 * Find the file for a given local path
	 *
	 * @param bundle
	 *            - bundle
	 * @param path
	 *            - local path
	 * @return File or null
	 * @throws IOException
	 */
	public static String findAbsolutePath(Bundle bundle, String path)
			throws IOException {
		URL url = findFileURL(bundle, path, false);
		return url == null ? null : url.getPath();
	}

	/**
	 * Converts an Eclipse internal URL into a standard file URL and copies the
	 * file to a working area when necessary
	 *
	 * @param url
	 *            - input URL
	 * @param encodeBlanks
	 *            - true if blanks are to be encoded
	 * @return - output URL
	 * @throws IOException
	 */
	public static URL toFileURL(URL url, boolean encodeBlanks)
			throws IOException {
		try {
			url = org.eclipse.core.runtime.FileLocator.toFileURL(url);
			if (encodeBlanks)
				return new URL(CommonUtilities.encodeBlanks(url.toString()));
		} catch (MalformedURLException e) {
			// do nothing
		}
		return url;
	}

}
