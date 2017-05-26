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
package com.bdaum.zoom.core.db;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.trash.Trash;

public interface IDbManager {

	/** System collection keys **/
	String CATKEY = QueryField.IPTC_CATEGORY.getKey() + '=';
	String URIKEY = QueryField.URI.getKey() + '=';
	String VOLUMEKEY = QueryField.VOLUME.getKey() + '=';
	String IMPORTKEY = QueryField.IMPORTDATE.getKey() + '=';
	String RATINGKEY = QueryField.RATING.getKey() + '=';
	String DATETIMEKEY = QueryField.EXIF_DATETIMEORIGINAL.getKey() + '=';
	String LOCATIONKEY = QueryField.IPTC_LOCATIONCREATED.getKey() + '=';

	/** Statistics slots **/
	String TOTALSIZE = "totalSize"; //$NON-NLS-1$
	String FREESPACE = "freeSpace"; //$NON-NLS-1$
	String FREESPACEENTRIES = "freeSpaceEntries"; //$NON-NLS-1$

	/**
	 * Returns a map with statistics values
	 *
	 * @return - map with statistics values
	 */
	Map<String, Long> getStatistics();

	/**
	 * Returns db manager version
	 *
	 * @return - db manager version
	 */
	int getVersion();

	/**
	 * Test if we run with an embedded database
	 *
	 * @return - true if embedded
	 */
	boolean isEmbedded();

	/**
	 * Returns an instance of the catalog properties. Optionally Creates a
	 * default set of properties if none exist
	 *
	 * @param create
	 *            - true if a default set of properties should be created if
	 *            none exists
	 *
	 * @return - catalog properties
	 */
	Meta getMeta(boolean create);

	/**
	 * Reads an object from the database by its string ID
	 *
	 * @param clazz
	 *            - type of object to be read
	 * @param id
	 *            - ID of object to be read
	 * @return - the object or null if not found
	 */
	<T extends IIdentifiableObject> T obtainById(Class<T> clazz, String id);

	/**
	 * Convenience method for obtainById(clazz, id) != null
	 * but does not activate objects
	 *
	 * @param clazz
	 *            - type of object to be read
	 * @param id
	 *            - ID of object to be read
	 * @return - true if the object was found
	 */
	boolean exists(Class<? extends IIdentifiableObject> clazz, String id);

	/**
	 * Convenience method for obtainsById(AssetImpl.class, id)
	 *
	 * @param id
	 *            - ID of asset to be read
	 * @return - the asset or null if not found
	 */
	AssetImpl obtainAsset(String id);

	/**
	 * Reads objects from the database by a list of string ID
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param ids
	 *            - IDs of objects to be read
	 * @return - list of resulting objects, null if database is not open
	 */
	<T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, String[] ids);

	/**
	 * Reads objects from the database by a list of string ID
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param ids
	 *            - IDs of objects to be read
	 * @return - list of resulting objects, null if database is not open
	 */
	<T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, Collection<String> ids);

	/**
	 * Reads asset subobjects from the database by a list of string ID
	 *
	 * @param assetId
	 *            - asset ID
	 * @param class1
	 *            - type of objects to be read
	 * @param ids
	 *            - IDs of objects to be read
	 * @return
	 */
	<T extends IIdentifiableObject> List<T> obtainStructByIds(String assetId, Class<T> clazz, String[] ids);

	/**
	 * Returns a list of all objects with the specified type
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @return - list of resulting objects, null if database is not open
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz);

	/**
	 * Convenience method for obtainObjects(AssetImpl.class)
	 *
	 * @return - list of resulting assets, null if database is not open
	 */
	List<AssetImpl> obtainAssets();

	/**
	 * Finds assets with specified criteria
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param field1
	 *            - first constraint field name
	 * @param v1
	 *            - first constraint value
	 * @param rel1
	 *            - first constraint relation as defined in QueryField
	 * @param field2
	 *            - second constraint field name
	 * @param v2
	 *            - second constraint value
	 * @param rel2
	 *            - second constraint relation as defined in QueryField
	 * @param or
	 *            - true if both constraints should be connected by OR instead
	 *            of AND
	 * @return the list of objects found
	 * @deprecated - use obtainObjects(clazz, or, namesValuesRelations)
	 */
	@Deprecated
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<? extends IIdentifiableObject> clazz, String field1,
			Object v1, int rel1, String field2, Object v2, int rel2, boolean or);

	/**
	 * Finds objects with specified criteria
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param field1
	 *            - first constraint field name
	 * @param v1
	 *            - first constraint value
	 * @param rel1
	 *            - first constraint relation as defined in QueryField
	 * @return the list of objects found
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field1, Object v1, int rel1);

	/**
	 * Finds objects that references on of the specified IDs
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param field
	 *            - field containing an ID
	 * @param values
	 *            - one or several IDs
	 * @return the list of objects that match at least one of the specified IDs
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, String[] values);

	/**
	 * Finds objects that references on of the specified IDs
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param field
	 *            - field containing an ID
	 * @param ids
	 *            - one or several IDs
	 * @return the list of objects that match at least one of the specified IDs
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, Collection<String> ids);

	/**
	 * Finds assets with specified criteria
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param or
	 *            - true if both constraints should be connected by OR instead
	 *            of AND
	 * @param namesValuesRelations
	 *            - an array of alternating field names, field values, and
	 *            relations
	 * @return the list of objects that matches the search criteria
	 */
	<T extends IIdentifiableObject> List<T> obtainObjects(Class<? extends IIdentifiableObject> clazz, boolean or,
			Object... namesValuesRelations);

	/**
	 * Finds structured components of assets such as Locations, Contacts, or
	 * Artwork
	 *
	 * @param clazz
	 *            - type of objects to be read
	 * @param assetId
	 *            - ID of asset to which these components relate
	 * @param multipleAsset
	 *            - true if a component can belong to multiple assets
	 * @param field
	 *            - constraint field name
	 * @param v
	 *            - constraint value
	 * @param multipleField
	 *            - true if the field allows multiple values
	 * @return the list of objects found
	 */
	<T extends IIdentifiableObject> List<T> obtainStruct(Class<T> clazz, String assetId, boolean multipleAsset,
			String field, Object v, boolean multipleField);

	<T extends IIdentifiableObject> List<T> obtainStructForAsset(Class<T> clazz, String id, boolean multiple);

	/**
	 * Optimized access method for finding assets by uri
	 *
	 * @param uri
	 *            - uri of asset
	 * @return - list of matching assets
	 */
	List<AssetImpl> obtainAssetsForFile(URI uri);

	/**
	 * Finds locations matching the provided example location
	 *
	 * @param location
	 *            - example location. Variable fields must be set to null.
	 * @return list of matching location
	 */
	List<LocationImpl> findLocation(final LocationImpl location);

	/**
	 * Store a single object and commits
	 *
	 * @param obj
	 *            - the object to store
	 */
	void storeAndCommit(Object obj);

	/**
	 * Store a single object
	 *
	 * @param obj
	 *            - the object to store
	 */
	void store(Object obj);

	/**
	 * Deletes an object
	 *
	 * @param obj
	 *            - the object to delete
	 */
	void delete(Object obj);

	/**
	 * Commits the database
	 */
	void commit();

	/**
	 * Rolls the database back
	 */
	void rollback();

	/**
	 * Returns the name of the catalog file
	 *
	 * @return name of the catalog file
	 */
	String getFileName();

	/**
	 * Modifies the read-only status
	 *
	 * @param readOnly
	 *            - true if database is in read-only state
	 */
	void setReadOnly(boolean readOnly);

	/**
	 * Tests if catalog is write-protected
	 *
	 * @return true if catalog is write-protected
	 */
	boolean isReadOnly();

	/**
	 * Performs the run() method of the runnable and commits
	 *
	 * @param runnable
	 *            - a Runnable performing database operations
	 * @return true in case of success, false in case of failure
	 */
	boolean safeTransaction(Runnable runnable);

	/**
	 * Deletes and stores the specified objects and commits
	 *
	 * @param toBeDeleted
	 *            - Single object or Collection of objects to be deleted or null
	 * @param toBeStored
	 *            - Single object or Collection of objects to be stored or null
	 * @return true in case of success, false in case of failure
	 * @since 1.2
	 */
	boolean safeTransaction(Object toBeDeleted, Object toBeStored);

	/**
	 * Creates a processor for the specified collection
	 *
	 * @param sm
	 *            - collection to be processed
	 * @param assetFilters
	 *            - additional filters or null
	 * @param customSort
	 *            - custom sort criterion or null
	 * @return the collection processor
	 */
	ICollectionProcessor createCollectionProcessor(SmartCollection sm, IAssetFilter[] assetFilters,
			SortCriterion customSort);

	/**
	 * Convenience method for createCollectionProcessor(SmartCollection sm,
	 * null, null)
	 *
	 * @param sm
	 *            - collection to be processed
	 * @return the collection processor
	 */
	ICollectionProcessor createCollectionProcessor(SmartCollection sm);

	/**
	 * Creates new directory system collections as necessary
	 *
	 * @param asset
	 *            - new or moved asset
	 * @return - true when new collections were created
	 */
	boolean createFolderHierarchy(Asset asset);

	/**
	 * Creates new directory system collections as necessary
	 *
	 * @param asset
	 *            - new or moved asset
	 * @param win32
	 *            - true if asset as located on a win32 platform
	 * @return - true when new collections were created
	 */
	boolean createFolderHierarchy(Asset asset, boolean win32);

	/**
	 * Creates new timeline system collections as necessary
	 *
	 * @param asset
	 *            - new or updated asset
	 * @param timeline
	 *            - timeline type (see Meta_type.timeline_xxx)
	 * @return - true when new collections were created
	 */
	boolean createTimeLine(Asset asset, String timeline);

	/**
	 * Creates new location collections as necessary
	 *
	 * @param location
	 *            - new or updated location
	 * @param locationOption
	 *            - location folder type (see Meta_type.locationFolders_xxx)
	 * @return - true when new collections were created
	 */
	boolean createLocationFolders(Location location, String locationOption);

	/**
	 * * Creates new location collections as necessary
	 *
	 * @param asset
	 *            - new or updated asset
	 * @param locationOption
	 *            - location folder type (see Meta_type.locationFolders_xxx)
	 * @return - true when new collections were created
	 */
	boolean createLocationFolders(Asset asset, String locationOption);

	/**
	 * Queries the database by the supplied example. Null values are regarded as
	 * empty slots
	 *
	 * @param example
	 *            - the example object
	 * @return - matching objects
	 */
	<T extends IIdentifiableObject> List<T> queryByExample(T example);

	/**
	 * Tests if the database contains trash objects
	 *
	 * @return - true if the database contains trash objects
	 */
	boolean hasTrash();

	/**
	 * Fetches all trash objects associated with the specified image URI
	 *
	 * @param uri
	 *            - image URI
	 * @return - list of trash objects or null
	 */
	List<Trash> obtainTrashForFile(URI uri);

	/**
	 * Stores a trash object
	 *
	 * @param object
	 *            - trash object to store
	 */
	public void storeTrash(Object object);

	/**
	 * Closes the database
	 *
	 * @param mode
	 *            closing mode - NORMAL, SHUTDOWN, EMERGENY (@see
	 *            com.bdaum.zoom.core.CatalogListener)
	 */
	void close(int mode);

	/**
	 * Finds trash objects of the specified class with the specified operation
	 * ID
	 *
	 * @param clazz
	 *            - type of trash object
	 * @param opId
	 *            - operation ID
	 * @return list of trash objects
	 */
	<T extends Object> List<T> getTrash(Class<T> clazz, String opId);

	/**
	 * Searches for objects of type Trash
	 *
	 * @param withFiles
	 *            - return only trash objects where also the image file is to be
	 *            deleted
	 * @return - trash objects
	 */
	List<Trash> obtainTrashToDelete(boolean withFiles);

	/**
	 * Closes the trash storage
	 */
	void closeTrash();

	/**
	 * Rolls trash database operations back
	 */
	void rollbackTrash();

	/**
	 * Commits all trash operations
	 */
	void commitTrash();

	/**
	 * Return database file (if any)
	 *
	 * @return - database file or null
	 */
	File getFile();

	/**
	 * Creates a back-up of the database if a back-up is due or is forced
	 *
	 * @param delay
	 *            - delay in msec before back-up job is started
	 * @param interval
	 *            - interval between backups in msec, -1 to force immediate
	 *            backup
	 * @return - scheduled backup job
	 */
	Job performBackup(long delay, long interval);

	/**
	 * Creates a back-up of the database if a back-up is due or is forced
	 *
	 * @param delay
	 *            - delay in msec before back-up job is started
	 * @param interval
	 *            - interval between backups in msec, -1 to force immediate
	 *            backup
	 * @param block
	 *            - true if method should wait for job finishing
	 * @return - scheduled backup job
	 */
	Job performBackup(long delay, long interval, boolean block);

	/**
	 * Creates a back-up of the database if a back-up is due or is forced
	 *
	 * @param delay
	 *            - delay in msec before back-up job is started
	 * @param interval
	 *            - interval between backups in msec, -1 to force immediate
	 *            backup
	 * @param block
	 *            - true if method should wait for job finishing
	 * @param generations
	 *            - true maximum number of backup generations to keep
	 * @return - scheduled backup job
	 */
	Job performBackup(long delay, long interval, boolean block, int generations);

	/**
	 * Returns the path of a local Lucene index or null
	 *
	 * @return local Lucene index or null
	 */
	File getIndexPath();

	/**
	 * Resets the local Lucene index if any
	 *
	 * @return true if there was something to reset
	 */
	boolean resetLuceneIndex();

	/**
	 * Deletes a trash object
	 *
	 * @param obj
	 *            - object to delete
	 */
	void deleteTrash(Object obj);

	/**
	 * Deletes all trash item
	 *
	 * @param set
	 *            - trash items
	 */
	void deleteAllTrash(Collection<? extends Object> set);

	/**
	 * Retrieves all trash objects
	 *
	 * @param byName
	 *            - true if sorted by name
	 * @return trash objects
	 */
	List<Trash> obtainTrash(boolean byName);

	/**
	 * Retrieves all Ghost objects for a given image URI
	 *
	 * @param uri
	 *            - image URI
	 * @return - ghost objects
	 */
	List<Ghost_typeImpl> obtainGhostsForFile(URI uri);

	/**
	 * Creates a collection for the most recent imports
	 *
	 * @param date
	 *            - date stamp
	 * @param cumulate
	 *            - true if import collection cumulates several background
	 *            imports
	 * @param description
	 *            - description text of the new collection
	 * @return - date stamp of previous recent imports collection or null
	 */
	Date createLastImportCollection(Date importDate, boolean cumulate, String description);

	/**
	 * Removes the given collection from the database if it is empty
	 *
	 * @param sm
	 *            - collection to be checked and removed
	 * @return - true if the collection was removed
	 */
	boolean pruneSystemCollection(SmartCollectionImpl sm);

	/**
	 * Removes all empty system defined collections Handles progress management
	 * by itself
	 */
	void pruneEmptySystemCollections();

	/**
	 * Removes all empty system defined collections
	 *
	 * @param monitor
	 *            - progress monitor
	 */
	void pruneEmptySystemCollections(IProgressMonitor monitor);

	/**
	 * Performs backups and defragmentation if necessary
	 *
	 * @param force
	 *            - true if defragmentation is forced, no backup in this case
	 */
	void checkDbSanity(boolean force);

	/**
	 * Tests if a backup is scheduled
	 *
	 * @return true if a backup is currently scheduled
	 */
	boolean isBackupScheduled();

	/**
	 * Starts an isolated transaction
	 *
	 * @return - true if transaction was successfully started
	 */
	boolean beginSafeTransaction();

	/**
	 * Ends a transaction that was started with beginSafeTransaction()
	 */
	void endSafeTransaction();

	/**
	 * Marks system collections as purge candidates when an asset is deleted or
	 * moved
	 *
	 * @param asset
	 *            - deleted or moved asset
	 */
	void markSystemCollectionsForPurge(Asset asset);

	/**
	 * Marks system collections as purge candidates when a location is deleted
	 * or modified
	 *
	 * @param loc
	 *            - location object
	 * @return true if the collection was not already marked
	 */
	boolean markSystemCollectionsForPurge(Location loc);

	/**
	 * Adds a collection to the set of purge candidates
	 *
	 * @param id
	 *            - collection id
	 * @return - true if the collection id was not already in the set
	 */
	boolean addDirtyCollection(String id);

	/**
	 * Retrieves the import group or creates one if it does not exist
	 *
	 * @return - import group
	 */
	GroupImpl getImportGroup();

	/**
	 * Repairs defects in the catalog
	 */
	void repairCatalog();

	/**
	 * Tests if the database needs defragmentation
	 *
	 * @return true if the database needs defragmentation
	 */
	boolean needsDefragmentation();

	/**
	 * Make a copy of the current catalog to the given destination
	 *
	 * @param destination
	 */
	void backup(String destination);

}