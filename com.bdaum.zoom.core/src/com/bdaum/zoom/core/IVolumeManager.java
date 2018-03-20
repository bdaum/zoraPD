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
package com.bdaum.zoom.core;

import java.io.File;
import java.net.URI;

import javax.swing.Icon;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.VolumeListener;

public interface IVolumeManager {

	public static final String FILE = Constants.FILESCHEME + ':';

//	public static final String MEDIA = "/media/"; //$NON-NLS-1$
//	public static final String RUNMEDIA = "/run/media/"; //$NON-NLS-1$


	// file status
	public static final int ONLINE = 0;
	public static final int REMOTE = 1;
	public static final int OFFLINE = 2;
	public static final int PEER = 3;

	/**
	 * Returns the real URI for a given asset
	 *
	 * @param asset
	 *            - asset or null
	 * @return - real URI or null if the asset was null
	 */
	URI findFile(Asset asset);

	/**
	 * Returns the real URI for a nominal URI and the real volume name
	 *
	 * @param uri
	 *            - nominal URI
	 * @param volume
	 *            - volume name
	 * @return real URI or null if the given URI was invalid
	 */
	URI findFile(String uri, String volume);

	/**
	 * Returns the real URI for a given asset and test for existence of the
	 * corresponding file
	 *
	 * @param asset
	 *            or null
	 * @param local
	 *            - true if file must exist on the local file system
	 * @return real URI or null if the given URI was invalid or the
	 *         corresponding file does not exist
	 */
	URI findExistingFile(Asset asset, boolean local);

	/**
	 * Returns the real URI for a nominal URI and the real volume name and test
	 * for existence of the corresponding file
	 *
	 * @param uri
	 *            - nominal URI
	 * @param volume
	 *            - volume name
	 * @return real URI or null if the given URI was invalid or the
	 *         corresponding file does not exist
	 */
	File findExistingFile(String uri, String volume);

	/**
	 * Determines the current image file state of an asset
	 *
	 * @param asset
	 *            - asset to be tested
	 * @return - ONLINE, REMOTE, OFFLINE, or PEER
	 */
	int determineFileState(Asset asset);

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
	 * Returns the URI of an associated voice file
	 *
	 * @param asset
	 *            - owning asset of the voice file
	 * @return voice file URI
	 */
	URI findVoiceFile(Asset asset);

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

	/**
	 * Adds a device insertion listener
	 *
	 * @param listener
	 *            - the device insertion listener
	 */
	public abstract void addDeviceInsertionListener(
			DeviceInsertionListener listener);

	/**
	 * Removes a device insertion listener
	 *
	 * @param listener
	 *            - the device insertion listener
	 */
	public abstract void removeDeviceInsertionListener(
			DeviceInsertionListener listener);

	/**
	 * Test if an asset is remote
	 *
	 * @param asset
	 *            - asset to test
	 * @return - true if asset belongs to another peer or if asset uri is not a
	 *         file uri.
	 */
	boolean isRemote(Asset asset);

	/**
	 * Removes a volume listener
	 * @param listener - the volume listener
	 */
	void removeVolumeListener(VolumeListener listener);

	/**
	 * Adds a volume listener
	 * @param listener - the volume listener
	 */
	void addVolumeListener(VolumeListener listener);

}