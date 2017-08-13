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

package com.bdaum.zoom.core.internal;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class FileNameExtensionFilter implements FileFilter {

	// Cached ext
	private String[] lowerCaseExtensions;
	private final boolean acceptDir;

	/**
	 * Constructor
	 * 
	 * @param extensions
	 *            - Array of extensions
	 */
	public FileNameExtensionFilter(String[] extensions) {
		acceptDir = true;
		List<String> list = new ArrayList<String>(extensions.length);
		for (String ex : extensions) {
			int p = ex.lastIndexOf('.');
			if (p > 0 && p < ex.length() - 1)
				ex = ex.substring(p + 1);
			if (!"*".equals(ex)) //$NON-NLS-1$
				list.add(ex.toLowerCase(Locale.ENGLISH));
		}
		lowerCaseExtensions = list.toArray(new String[list.size()]);
	}

	/**
	 * Constructor
	 * 
	 * @param extensions
	 *            - Array of extensions or extension groups
	 * @param acceptDir
	 */
	public FileNameExtensionFilter(String[] extensions, boolean acceptDir) {
		this.acceptDir = acceptDir;
		List<String> list = new ArrayList<String>(extensions.length * 2);
		for (int i = 0; i < extensions.length; i++) {
			StringTokenizer st = new StringTokenizer(extensions[i], ";"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String ex = st.nextToken();
				int p = ex.lastIndexOf('.');
				if (p > 0 && p < ex.length() - 1)
					ex = ex.substring(p + 1);
				if (!"*".equals(ex)) //$NON-NLS-1$
					list.add(ex.toLowerCase(Locale.ENGLISH));
			}
		}
		lowerCaseExtensions = list.toArray(new String[list.size()]);
	}

	public boolean accept(File f) {
		return (f == null) ? false : (f.isDirectory()) ? acceptDir : accept(f.getName());
	}

	public boolean accept(String fileName) {
		int i = fileName.lastIndexOf('.');
		if (i <= 0) {
			for (String extension : lowerCaseExtensions)
				if (extension.isEmpty())
					return true;
			return false;
		}
		if (i < fileName.length() - 1) {
			String desiredExtension = fileName.substring(i + 1).toLowerCase(Locale.ENGLISH);
			for (String extension : lowerCaseExtensions)
				if (desiredExtension.equals(extension))
					return true;
		}
		return false;
	}

}
