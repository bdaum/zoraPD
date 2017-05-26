/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * modified for multi lingual messages (bd)
 */
package com.sun.jna.platform.mac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;

import com.sun.jna.platform.FileUtils;

public class MacFileUtils extends FileUtils {

	public boolean hasTrash() {
		return true;
	}

	public void moveToTrash(File[] files) throws IOException {
		// TODO: use native API for moving to trash (if any)
		File home = new File(System.getProperty("user.home")); //$NON-NLS-1$
		File trash = new File(home, ".Trash"); //$NON-NLS-1$
		if (!trash.exists()) {
			throw new IOException(NLS.bind(Messages.MacFileUtils_trash_not_found, trash));
		}
		StringBuilder sb = new StringBuilder();
		List<File> failed = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			File src = files[i];
			File target = new File(trash, src.getName());
			if (!src.renameTo(target)) {
				failed.add(src);
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(src.getName());
			}
		}
		if (sb.length() > 0) {
			throw new IOException(NLS.bind(Messages.MacFileUtils_file_not_trashed, sb));
		}
	}
}
