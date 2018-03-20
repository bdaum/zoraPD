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
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.program.DiskFullException;

public interface IPeerService {

	/*** Operations **/
	int SEARCH = 1;
	int VIEW = 2;
	int COPY = 4;
	int VOICE = 8;

	/**
	 * Performs a collection search across all connected peers Returns
	 * immediately after starting the search jobs Result are obtained through
	 * method getSelect();
	 *
	 * @param scoll
	 *            - collection definition
	 * @param assetFilters
	 *            - additional asset filters
	 * @return session ticket or null if no peer providers are connected
	 */
	String select(SmartCollection scoll, IAssetFilter[] assetFilters);

	/**
	 * Returns the resulting asset collection belonging to the specified ticket
	 *
	 * @param ticket
	 *            - session ticket
	 * @return asset collection
	 */
	Collection<? extends Asset> getSelect(String ticket);

	/**
	 * Returns true if peers are connected or if the own node has other catalogs than the current one
	 *
	 * @return true if peers are connected or if the own node has other catalogs than the current one
	 */
	boolean hasPeerPeerProviders();

	/**
	 * Returns true if asset was obtained by a peer
	 *
	 * @param assetId
	 *            - ID of asset
	 * @return true if asset was obtained by a peer
	 */
	boolean isOwnedByPeer(String assetId);

	/**
	 * Returns asset origin information about the specified asset
	 *
	 * @param assetId
	 *            - ID of asset
	 * @return asset origin information or null if the asset was not obtained
	 *         from a peer
	 */
	AssetOrigin getAssetOrigin(String assetId);

	/**
	 * Checks the credentials for performing certain asset operations
	 *
	 * @param operation
	 *            SEARCH, VIEW, VOICE, COPY
	 * @param safety
	 *            - safety level for the asset on which the operations are
	 *            performed
	 * @param assetOrigin
	 *            - asset origin information of the asset on which the
	 *            operations are performed
	 * @return true if operation is possible
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	boolean checkCredentials(int operation, int safety, AssetOrigin assetOrigin)
			throws ConnectionLostException;

	/**
	 * Starts a file transfer job on image file
	 *
	 * @param asset
	 *            - owning asset
	 * @param assetOrigin
	 *            - asset origin information
	 * @return job object
	 */
	Job scheduleTransferJob(Asset asset, AssetOrigin assetOrigin);

	/**
	 * Retrieve asset with the specified ID
	 *
	 * @param location
	 *            - location of owning peer
	 * @param catFile
	 *            - catalog to be searched
	 * @param assetId
	 *            - asset ID
	 * @return asset or null
	 * @throws SecurityException
	 *             if the asking node does not have sufficient rights to access
	 *             the asset
	 * @throws ConnectionLostException
	 *             if peer node is offline
	 */
	Asset obtainAsset(String location, File catFile, String assetId)
			throws SecurityException, ConnectionLostException;

	/**
	 * Performs a file transfer
	 *
	 * @param monitor
	 *            - progress monitor
	 * @param assetOrigin
	 *            - origin of owning asset
	 * @param info
	 *            - file information about the file to be transferred
	 * @param outFile
	 *            - output file
	 * @return true in case of success
	 * @throws IOException
	 * @throws DiskFullException
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	boolean transferRemoteFile(IProgressMonitor monitor,
			AssetOrigin assetOrigin, FileInfo info, File outFile)
			throws IOException, DiskFullException, ConnectionLostException;

	/**
	 * Returns the host of the current node
	 *
	 * @return - host of the current node
	 */
	String getHost();

	/**
	 * Find all peers not supporting the specified similarity algorithm
	 *
	 * @param algoId
	 *            - algorithm ID
	 * @return Array of all peers not supporting the specified similarity
	 *         algorithm
	 */
	String[] findWeakSimilarityPeers(String algoId);

	/**
	 * Tests if the specified peer is local
	 *
	 * @param peer
	 *            - peer location
	 * @return - true if the specified peer is local
	 */
	boolean isLocal(String peer);

	/**
	 * Return information about the specified remote file
	 *
	 * @param assetOrigin
	 *            - owning asset
	 * @param uri
	 *            - file URI
	 * @param volume
	 *            - file volume
	 * @return - FileInfo object or null if file does not exist.
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	FileInfo getFileInfo(AssetOrigin assetOrigin, String uri, String volume)
			throws ConnectionLostException;

	/**
	 * Fetch a subpart of an asset
	 *
	 * @param assetOrigin
	 *            - origin of owning asset
	 * @param clazz
	 *            - subpart type
	 * @param assetId
	 *            - asset ID
	 * @param multiAsset
	 *            - true if subpart can have multiple assets as parents
	 * @return list of found subparts
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	<T extends IIdentifiableObject> List<T> obtainStructForAsset(
			AssetOrigin assetOrigin, Class<T> clazz, String assetId,
			boolean multiAsset) throws ConnectionLostException;

	/**
	 * Find an object by ID
	 *
	 * @param assetOrigin
	 *            - origin of context asset
	 * @param clazz
	 *            - type of object
	 * @param id
	 *            of object
	 * @return found object or null
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	<T extends IIdentifiableObject> T obtainById(AssetOrigin assetOrigin,
			Class<T> clazz, String id) throws ConnectionLostException;

	/**
	 * Find objects by field values
	 *
	 * @param assetOrigin
	 *            - origin of context asset
	 * @param clazz
	 *            - type of object
	 * @param field
	 *            - field containing the asked values
	 * @param values
	 *            - values
	 * @return list of found objects
	 * @throws ConnectionLostException
	 *             - if peer has gone offline
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(
			AssetOrigin assetOrigin, Class<T> clazz, String field,
			String[] values) throws ConnectionLostException;

	/**
	 * Returns the number of remote peers that are online
	 * @return number of remote peers that are online
	 */
	int getOnlinePeerCount();

	/**
	 * Asks for value proposals for the specified field
	 *   Can also be used to obtain a tag cloud of the shared databases
	 * @param field - query field key - specify null for tag cloud
	 * @param subfield - query field subfield or number of occurrences for tag cloud
	 * @return task ticket
	 */
	public abstract String askForValueProposals(String field, String subfield);

	/**
	 * Returns for value proposals for the specified field
	 * @param ticket - task ticket
	 * @return ticket
	 */
	public abstract Set<String> getProposals(String ticket);

	/**
	 * Discards the former query for proposals
	 * @param ticket - task ticket
	 * @return value proposals
	 */
	void discardTask(String ticket);

	/**
	 * Checks if the listening port has been changed during startup
	 * @param adaptable - Adaptable that can provide a Shell instance
	 */
	void checkListeningPort(IAdaptable adaptable);

	/**
	 * Asks for a tag cloud with the specified number of occurrences
	 * @param occurrences - max number of tags to be returned
	 * @return ticket
	 */
	String askForTagCloud(int occurrences);

	/**
	 * Returns the tag cloud
	 * @param ticket - task ticket
	 * @return tag cloud map
	 */
	Map<String, List<ScoredString>> getTagCloud(String ticket);

}
