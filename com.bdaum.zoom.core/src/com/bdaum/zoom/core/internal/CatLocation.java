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

import java.io.File;

public class CatLocation {

	private String uri;
	private String volume;

	public CatLocation(String uri, String volume) {
		this.uri = uri;
		this.volume = volume;
	}

	public CatLocation(String s) {
		int p = s.indexOf('\t');
		if (p >= 0) {
			volume = s.substring(0, p);
			uri = s.substring(p + 1);
		} else
			uri = s;
	}

	public CatLocation(File f) {
		volume = CoreActivator.getDefault().getVolumeManager().getVolumeForFile(f);
		uri = f.toURI().toString();
	}

	
	@Override
	public String toString() {
		return (volume == null || volume.isEmpty()) ? uri : volume + '\t'
				+ uri;
	}

	public File getFile() {
		return CoreActivator.getDefault().getVolumeManager().findExistingFile(uri, volume);
	}

	public String getVolume() {
		return volume;
	}

	public String getUri() {
		return uri;
	}

	public boolean exists() {
		return getFile() != null;
	}

	public String getLabel() {
		File file = getFile();
		return file == null ? uri : file.getAbsolutePath();
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CatLocation) {
			CatLocation other = (CatLocation) obj;
			if (!uri.equals(other.getUri()))
				return false;
			if (volume == null)
				return other.getVolume() == null;
			return volume.equals(other.getVolume());
		}
		return false;
	}

	
	@Override
	public int hashCode() {
		int h = uri.hashCode();
		if (volume != null)
			h += 31 * volume.hashCode();
		return h;
	}
}
