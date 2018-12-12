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

package com.bdaum.zoom.core.internal;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;

public class FileNameExtensionFilter implements ObjectFilter {

	private Set<String> lowerCaseExtensions;
	private final boolean acceptDir;
	private boolean hasEmptyExtension = false;

	/**
	 * Constructor
	 * 
	 * @param extensions
	 *            - Array of extensions
	 */
	public FileNameExtensionFilter(String[] extensions) {
		acceptDir = true;
		lowerCaseExtensions = new HashSet<String>(extensions.length * 3 / 2);
		for (String ex : extensions) {
			int p = ex.lastIndexOf('.');
			if (p > 0 && p < ex.length() - 1)
				ex = ex.substring(p + 1);
			if (!"*".equals(ex)) { //$NON-NLS-1$
				hasEmptyExtension = ex.isEmpty();
				lowerCaseExtensions.add(ex.toLowerCase(Locale.ENGLISH));
			}
		}
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
		lowerCaseExtensions = new HashSet<String>(extensions.length * 3);
		for (int i = 0; i < extensions.length; i++) {
			StringTokenizer st = new StringTokenizer(extensions[i], ";"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String ex = st.nextToken();
				int p = ex.lastIndexOf('.');
				if (p > 0 && p < ex.length() - 1)
					ex = ex.substring(p + 1);
				if (!"*".equals(ex)) { //$NON-NLS-1$
					hasEmptyExtension = ex.isEmpty();
					lowerCaseExtensions.add(ex.toLowerCase(Locale.ENGLISH));
				}
			}
		}
	}

	public boolean accept(File f) {
		return f != null && f.isDirectory() ? acceptDir : accept(f.getName());
	}
	
	public boolean accept(StorageObject f) {
		return f != null && f.isDirectory() ? acceptDir : accept(f.getName());
	}

	@Override
	public boolean accept(String fileName) {
		int i = fileName.lastIndexOf('.');
		if (i <= 0)
			return hasEmptyExtension;
		return i < fileName.length() - 1
				&& lowerCaseExtensions.contains(fileName.substring(i + 1).toLowerCase(Locale.ENGLISH));
	}

}
