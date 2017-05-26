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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.peer;

import java.io.File;
import java.io.Serializable;


public class AssetOrigin implements Serializable {

	private static final long serialVersionUID = 8697197735790991444L;
	private File catFile;
	private String location;

	public AssetOrigin(File catName,
			String location) {
		this.catFile = catName;
		this.location = location;
	}

	/**
	 * @return catName
	 */
	public File getCatFile() {
		return catFile;
	}

	/**
	 * @param catFile das zu setzende Objekt catName
	 */
	public void setCatFile(File catFile) {
		this.catFile = catFile;
	}

	/**
	 * @return location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location das zu setzende Objekt location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	public String getHost() {
		if (location != null) {
			int p = location.lastIndexOf(':');
			if (p >= 0)
				return location.substring(0,p);
		}
		return location;
	}


}
