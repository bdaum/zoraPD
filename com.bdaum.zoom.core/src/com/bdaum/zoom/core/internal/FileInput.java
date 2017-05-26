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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.bdaum.zoom.core.Core;

public class FileInput {

	private static class Entry {
		private File file;
		private long size;
		private long lastModified;

		Entry(File file) {
			this.file = file;
			this.size = file.length();
			this.lastModified = file.lastModified();
		}

		File getFile() {
			return (file.exists() && file.lastModified() == lastModified && file
					.length() == size) ? file : null;
		}
	}

	private List<Entry> entries = new ArrayList<Entry>();
	private File first;

	public FileInput(File[] files, boolean skipHiddenFiles) {
		listFiles(files, skipHiddenFiles);
	}

	public FileInput(List<File> files, boolean skipHiddenFiles) {
		this(files.toArray(new File[files.size()]), skipHiddenFiles);
	}

	private void listFiles(File[] files, boolean skipHiddenFiles) {
		if (files != null)
			for (File file : files) {
				if (file == null || skipHiddenFiles && file.isHidden())
					continue;
				if (first == null)
					first = file;
				if (file.isDirectory())
					listFiles(file.listFiles(), skipHiddenFiles);
				else
					entries.add(new Entry(file));
			}
	}

	public int size() {
		return entries.size();
	}

	public String getName() {
		return first == null ? "" : first.getName(); //$NON-NLS-1$
	}

	public void getURIs(FileNameExtensionFilter filenameFilter,
			List<URI> resultList) {
		for (Entry entry : entries) {
			File file = entry.getFile();
			if (filenameFilter.accept(file))
				resultList.add(file.toURI());
		}
	}

	public String getAbsolutePath() {
		return first == null ? "" : first.getAbsolutePath(); //$NON-NLS-1$
	}

	public String getVolume() {
		return first == null ? null : Core.getCore().getVolumeManager().getVolumeForFile(first);
	}
}
