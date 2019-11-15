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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.mtp;

import java.io.File;

import javax.swing.Icon;

public interface IRootManager {

	/**
	 * Returns the base volume name for a given file
	 *
	 * @param file
	 *            - given file
	 * @return - volume name
	 */
	String getVolumeForFile(File file);

	/**
	 * Converts root file to voume name
	 *
	 * @param root
	 *            - root file
	 * @return volume name
	 */
	String rootToVolume(File root);

	/**
	 * Converts volume name to root file
	 *
	 * @param volume
	 *            - volume name
	 * @return - root file
	 */
	File volumeToRoot(String volume);

	/**
	 * Tests if the specified volume is offline
	 *
	 * @param volume
	 *            - volume name
	 * @return true if volume is offline
	 */
	boolean isOffline(String volume);

	/**
	 * Returns the icon belonging to a given file
	 *
	 * @param path
	 *            - file path
	 * @return - associated icon
	 */
	Icon getFileIcon(String path);

	/**
	 * Extracts the root of a file path
	 *
	 * @param file
	 *            - given file
	 * @return - root file
	 */
	File getRootFile(File file);

}