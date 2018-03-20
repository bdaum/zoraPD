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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.core.internal.peer;

import java.io.File;
import java.io.Serializable;

/**
 * @author bdaum
 *
 */
public class FileInfo  implements Serializable {

	private static final long serialVersionUID = -4084176825822211996L;
	private long size;
	private File file;

	public FileInfo(long fileSize, File file) {
		this.size = fileSize;
		this.file = file;
	}

	/**
	 * @return size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size das zu setzende Objekt size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file das zu setzende Objekt file
	 */
	public void setFile(File file) {
		this.file = file;
	}

}
