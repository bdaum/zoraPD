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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;

public class FileInput {

	private List<StorageObject> entries = new ArrayList<StorageObject>();
	private StorageObject first;

	public FileInput(StorageObject[] files, boolean skipHiddenFiles) {
		try {
			StorageObject.collectFilteredFiles(files, entries, null, skipHiddenFiles, null);
			for (StorageObject file : files)
				if (!file.isHidden()) {
					first = file;
					break;
				}
		} catch (IOException e) {
			// should never happen, FileInput is not applied to mobile device files
		}
	}

	public FileInput(List<StorageObject> files, boolean skipHiddenFiles) {
		this(files.toArray(new StorageObject[files.size()]), skipHiddenFiles);
	}

	public int size() {
		return entries.size();
	}

	public String getName() {
		return first == null ? "" : first.getName(); //$NON-NLS-1$
	}

	public void getObjects(ObjectFilter filenameFilter,
			List<StorageObject> resultList) {
		for (StorageObject entry : entries)
			if (filenameFilter.accept(entry))
				resultList.add(entry);
	}

	public String getAbsolutePath() {
		return first == null ? "" : first.getAbsolutePath(); //$NON-NLS-1$
	}

	public String getVolume() {
		return first == null ? null : first.getVolume(); 
	}
}
