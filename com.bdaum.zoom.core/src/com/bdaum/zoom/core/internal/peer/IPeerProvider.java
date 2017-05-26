package com.bdaum.zoom.core.internal.peer;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.IAssetFilter;

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
public interface IPeerProvider {

	/**
	 * Runs a collection search against all shared catalogs
	 * @param scoll - collection definition
	 * @param assetFilters - additional asset filters
	 * @param receiver - receiver node location, used to check credentials
	 * @return - collection of found assets
	 */
	Collection<AssetSearchResult> select(SmartCollection scoll,
			IAssetFilter[] assetFilters, String receiver);

	/**
	 * Return information about the specified file
	 * @param uri - nominal file URI
	 * @param volume - file volume
	 * @return - FileInfo object
	 */
	FileInfo getFileInfo(String uri, String volume);

	/**
	 * Start file transfer
	 * @param info - FileInfo object
	 * @return session ticket
	 */
	String ftOpen(FileInfo info);

	/**
	 * Filetransfer - read chunk
	 * @param ticket -  session ticket
	 * @param size - number of bytes to be read
	 * @return - bytes read
	 */
	byte[] ftRead(String ticket, int size);

	/**
	 * Close file transfer session
	 * @param ticket -  session ticket
	 */
	void ftClose(String ticket);

	/**
	 * Check the credentials of inquiring peer node
	 * @param operation - operation type (see IPeerService.SEARCH/VIEW/VOICE/COPY)
	 * @param safety - requested safety level
	 * @param catFile - catalog to be accessed
	 * @param receiver - inquiring peer node location
	 * @return true if access is allowed
	 */
	boolean checkCredentials(int operation, int safety, File catFile, String receiver);

	/**
	 * Retrieves an asset subpart such as locations, contacts etc.
	 * @param catFile - catalog to be accessed
	 * @param clazz - Type of subpart
	 * @param assetId - ID of owning asset
	 * @param multipleAsset - true if multiple assets can own subpart
	 * @return - list of found subparts
	 */
	<T extends IIdentifiableObject> List<T> obtainStructForAsset(File catFile,
			Class<T> clazz, String assetId, boolean multipleAsset);

	/**
	 * Retrieves objects by ids
	 * @param catFile - catalog to be accessed
	 * @param clazz - Type of object
	 * @param field - object field containing ID
	 * @param ids - Array of IDs
	 * @return - list of found objects
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(File catFile,
			Class<T> clazz, String field, String[] ids);

	/**
	 * Retrieves a single object identified by the specified ID
	 * @param catFile - catalog to be accessed
	 * @param clazz - Type of object
	 * @param id - object ID
	 * @return - found object or null
	 */
	<T extends IIdentifiableObject> T obtainById(File catFile, Class<T> clazz, String id);

	/**
	 * Retrieves an asset by ID
	 * @param catFile - catalog to be accessed
	 * @param assetId - asset ID
	 * @param receiver - inquiring peer node location, used to check credentials
	 * @return found asset or null
	 * @throws SecurityException if credential check failed
	 */
	Asset obtainAsset(File catFile, String assetId, String receiver) throws SecurityException;

	/**
	 * Returns the location of this provider peer
	 * @return location of this provider peer
	 */
	String getLocation();

	/**
	 * Returns the host of this provider peer
	 * @return host of this provider peer
	 */
	String getHost();

	/**
	 * Checks if the specified similarity index is supported
	 * @param algoId - algorithm ID
	 * @return true if the specified similarity index is supported
	 */
	boolean supportsSimilarityAlgorithm(String algoId);

	/**
	 * Tests if the current request is from to local node
	 * @param sender - the sender of the request
	 * @return true if the current request is from to local node
	 */
	boolean isLocalRequest(String sender);

	/**
	 * Return all value proposals for the specified field of all published catalogs of this provider
	 *  Can also be used to obtain a tag cloud of the shared databases
	 * @param field - query field key - specify null for tag cloud
	 * @param subfield - query field subfield or number of occurrences for tag cloud
	 * @param location - the receiver of the result
	 * @return value proposals or tag cloud (alternating list of tag names and scores)
	 */
	Collection<? extends String> getValueProposals(String field,String subfield, String location);

}
