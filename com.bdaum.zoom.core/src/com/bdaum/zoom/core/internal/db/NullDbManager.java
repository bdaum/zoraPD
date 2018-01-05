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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.db;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.MetaImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.image.ImageConstants;

public class NullDbManager implements IDbManager {

	private static final ArrayList<AssetImpl> EMPTYASSETS = new ArrayList<AssetImpl>(0);

	private static final ArrayList<Ghost_typeImpl> EMPTYGHOSTS = new ArrayList<Ghost_typeImpl>(0);

	private static final List<LocationImpl> EMPTYLOCATIONS = new ArrayList<LocationImpl>(0);

	private static final List<Trash> EMPTYTRASH = new ArrayList<Trash>(0);

	private Meta meta;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getMeta()
	 */

	public Meta getMeta(boolean create) {
		if (meta == null && create)
			meta = createMeta();
		return meta;
	}

	private static Meta createMeta() {
		Date creationDate = new Date();
		return new MetaImpl(-1, -1, creationDate, System.getProperty("user.name"), null, "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"", Meta_type.timeline_month, //$NON-NLS-1$
				Meta_type.locationFolders_no, new Date(0L), 0, 0, creationDate, null, "", creationDate, //$NON-NLS-1$
				Meta_type.thumbnailResolution_medium, false, null, null, true, 30, false, true, false,
				ImageConstants.SHARPEN_MEDIUM, null, Platform.getOS(), null, Constants.PICASASCANNERVERSION, false, false, 75,
				true, null, 0L);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainById(java.lang.Class,
	 * java.lang.String)
	 */

	public <T extends IIdentifiableObject> T obtainById(Class<T> clazz, String id) {
		return null;
	}

	@Override
	public boolean exists(Class<? extends IIdentifiableObject> clazz, String id) {
		return false;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.db.IDbManager#obtainStructByIds(java.lang.String,
	 * java.lang.Class, java.util.Collection)
	 */
	public <T extends IIdentifiableObject> List<T> obtainStructByIds(String assetId, Class<T> clazz, String[] ids) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainObjects(java.lang.Class)
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainStructForAsset(java.lang.Class,
	 * java.lang.String, boolean)
	 */

	public <T extends IIdentifiableObject> List<T> obtainStructForAsset(Class<T> clazz, String id, boolean multiple) {
		if (id == null)
			return null;
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.lang.String[])
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, String[] values) {
		return new ArrayList<T>(0);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * boolean, java.lang.String[])
	 */
	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<? extends IIdentifiableObject> clazz, boolean or,
			Object... namesValuesRelations) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.util.Collection)
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, Collection<String> ids) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainAssetsForFile(java.net.URI)
	 */

	public List<AssetImpl> obtainAssetsForFile(URI uri) {
		return EMPTYASSETS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainGhostsForFile(java.net.URI)
	 */

	public List<Ghost_typeImpl> obtainGhostsForFile(URI uri) {
		return EMPTYGHOSTS;
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainAsset(java.lang.String)
	 */
	public AssetImpl obtainAsset(String id) {
		return null;
	}

	public <T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, String[] ids) {
		return new ArrayList<T>(0);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainByIds(java.lang.Class,
	 * java.util.Collection)
	 */
	public <T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, Collection<String> ids) {
		return new ArrayList<T>(0);
	}

	public List<AssetImpl> obtainAssets() {
		return EMPTYASSETS;
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.lang.Object, int)
	 */
	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field1, Object v1, int rel1) {
		return new ArrayList<T>(0);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainStruct(java.lang.Class,
	 * java.lang.String, boolean, java.lang.String, java.lang.Object, boolean)
	 */
	public <T extends IIdentifiableObject> List<T> obtainStruct(Class<T> clazz, String assetId, boolean multipleAsset,
			String field, Object v, boolean multipleField) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.lang.Object, int, java.lang.String,
	 * java.lang.Object, int, boolean)
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<? extends IIdentifiableObject> clazz,
			String field1, Object v1, int rel1, String field2, Object v2, int rel2, boolean or) {
		return new ArrayList<T>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IDbManager#findLocation(com.bdaum.zoom.cat.model.
	 * location.LocationImpl)
	 */

	public List<LocationImpl> findLocation(final LocationImpl location) {
		return EMPTYLOCATIONS;

	}

	public <T extends IIdentifiableObject> List<T> queryByExample(T example) {
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#storeAndCommit(java.lang.Object)
	 */

	public void storeAndCommit(Object obj) {
		// no op
	}

	public void reconnectToDb(RuntimeException e) {
		// no op
	}

	public <T extends Object> List<T> getTrash(Class<T> clazz, String opId) {
		return new ArrayList<T>();
	}

	public List<Trash> obtainTrashToDelete(boolean withFiles) {
		return EMPTYTRASH;
	}

	public List<Trash> obtainTrash(boolean byName) {
		return EMPTYTRASH;
	}

	public List<Trash> obtainTrashForFile(URI uri) {
		return EMPTYTRASH;
	}

	public boolean hasTrash() {
		return false;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#close(int)
	 */

	public void close(int mode) {
		// no op
	}

	public boolean createFolderHierarchy(final Asset asset) {
		return false;
	}

	public boolean createFolderHierarchy(final Asset asset, final boolean win32) {
		return false;
	}

	public SmartCollection createDirectoryCollection(String name, SmartCollection parentColl, GroupImpl directories,
			String fieldname, String fieldvalue, int rel, boolean[] changeIndicator) {
		return null;
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.core.db.IDbManager#createLastImportCollection(java.util.
	 * Date, boolean, java.lang.String)
	 */
	public Date createLastImportCollection(Date importDate, boolean cumulate, String description) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getImportGroup()
	 */

	public GroupImpl getImportGroup() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IDbManager#createTimeLine(com.bdaum.zoom.cat.model
	 * .asset.Asset, java.lang.String)
	 */

	public boolean createTimeLine(final Asset asset, final String timeline) {
		return false;
	}

	public SmartCollectionImpl createTimelineCollection(String name, SmartCollection parentColl, GroupImpl timeLine,
			String id, GregorianCalendar from, GregorianCalendar to, boolean[] changeIndicator) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.db.IDbManager#createLocationFolders(com.bdaum.zoom
	 * .cat.model.asset.Asset, java.lang.String)
	 */

	public boolean createLocationFolders(Asset asset, String locationOption) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.db.IDbManager#createLocationFolders(com.bdaum.zoom
	 * .cat.model.location.Location, java.lang.String)
	 */

	public boolean createLocationFolders(final Location location, final String locationOption) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#checkDbSanity(boolean)
	 *
	 * May fail due to DB4O COR-2105
	 */

	public void checkDbSanity(final boolean force) {
		// no op
	}

	public boolean needsDefragmentation() {
		return false;
		// no op
	}

	public void pruneEmptySystemCollections() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IDbManager#markSystemCollectionsForPurge(com.bdaum
	 * .zoom.cat.model.asset.Asset)
	 */

	public void markSystemCollectionsForPurge(Asset asset) {
		// no op
	}

	public boolean markSystemCollectionsForPurge(Location loc) {
		return false;
	}

	public boolean pruneSystemCollection(final SmartCollectionImpl sm) {
		return false;
	}

	public void pruneEmptySystemCollections(IProgressMonitor monitor) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IDbManager#createCollectionProcessor(com.bdaum.zoom
	 * .cat.model.group.SmartCollection, com.bdaum.zoom.core.IAssetFilter,
	 * com.bdaum.zoom.cat.model.group.SortCriterion)
	 */

	public ICollectionProcessor createCollectionProcessor(SmartCollection sm, IAssetFilter[] filters,
			SortCriterion customSort) {
		return new NullCollectionProcessor();
	}

	public boolean addDirtyCollection(String id) {
		return false;
	}

	public boolean resetLuceneIndex() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long)
	 */

	public Job performBackup(long delay, long interval) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long, boolean)
	 */

	public Job performBackup(long delay, long interval, boolean block) {
		return performBackup(delay, interval, block, Integer.MAX_VALUE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long, boolean,
	 * int)
	 */

	public Job performBackup(long delay, long interval, boolean block, int generations) {
		return null;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#store(java.lang.Object)
	 */

	public void store(Object object) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#delete(java.lang.Object)
	 */

	public void delete(Object object) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#storeTrash(java.lang.Object)
	 */

	public void storeTrash(Object object) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#deleteTrash(java.lang.Object)
	 */

	public void deleteTrash(Object object) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#deleteAllTrash(java.util.Collection)
	 */

	public void deleteAllTrash(Collection<? extends Object> t) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#commit()
	 */

	public void commit() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#rollback()
	 */

	public void rollback() {
		// no op
	}

	public void backup(String filename) {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#setReadOnly(boolean)
	 */

	public void setReadOnly(boolean readOnly) {
		// no op
	}

	public void resetErrorCount() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#beginSafeTransaction()
	 */

	public synchronized boolean beginSafeTransaction() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#endSafeTransaction()
	 */

	public void endSafeTransaction() {
		// no op
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#safeTransaction(java.lang.Runnable)
	 */

	public boolean safeTransaction(Runnable runnable) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#safeTransaction(java.lang.Object,
	 * java.lang.Object)
	 */

	public boolean safeTransaction(Object toBeDeleted, Object toBeStored) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#isBackupScheduled()
	 */

	public boolean isBackupScheduled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getStatistics()
	 */

	public Map<String, Long> getStatistics() {
		return new HashMap<String, Long>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#isEmbedded()
	 */

	public boolean isEmbedded() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */

	public Object getAdapter(Class<?> adapter) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#repairCatalog()
	 */

	public void repairCatalog() {
		// no op
	}

	public int getVersion() {
		return -1;
	}

	public String getFileName() {
		return "<>"; //$NON-NLS-1$
	}

	public boolean isReadOnly() {
		return true;
	}

	public ICollectionProcessor createCollectionProcessor(SmartCollection sm) {
		return createCollectionProcessor(sm, null, null);
	}

	public void closeTrash() {
		// no op
	}

	public void rollbackTrash() {
		// no op
	}

	public void commitTrash() {
		// no op
	}

	public File getFile() {
		return null;
	}

	public File getIndexPath() {
		return null;
	}

}
