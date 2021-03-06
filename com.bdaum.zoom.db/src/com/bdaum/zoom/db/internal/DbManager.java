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
 * (c) 2009-2018 Berthold Daum  
 */
package com.bdaum.zoom.db.internal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.ArtworkOrObject_typeImpl;
import com.bdaum.zoom.cat.model.Asset_typeImpl;
import com.bdaum.zoom.cat.model.Category_typeImpl;
import com.bdaum.zoom.cat.model.ComposedTo_typeImpl;
import com.bdaum.zoom.cat.model.CreatorsContact_typeImpl;
import com.bdaum.zoom.cat.model.Criterion_typeImpl;
import com.bdaum.zoom.cat.model.DerivedBy_typeImpl;
import com.bdaum.zoom.cat.model.Exhibit_typeImpl;
import com.bdaum.zoom.cat.model.Exhibition_typeImpl;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.Group_typeImpl;
import com.bdaum.zoom.cat.model.LocationCreated_typeImpl;
import com.bdaum.zoom.cat.model.LocationShown_typeImpl;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.Region_typeImpl;
import com.bdaum.zoom.cat.model.SlideShow_typeImpl;
import com.bdaum.zoom.cat.model.Slide_typeImpl;
import com.bdaum.zoom.cat.model.SmartCollection_typeImpl;
import com.bdaum.zoom.cat.model.SortCriterion_typeImpl;
import com.bdaum.zoom.cat.model.TrackRecord_typeImpl;
import com.bdaum.zoom.cat.model.WebExhibit_typeImpl;
import com.bdaum.zoom.cat.model.WebGallery_typeImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.MetaImpl;
import com.bdaum.zoom.common.GeoMessages;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbListener;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.core.trash.FileLocationBackup;
import com.bdaum.zoom.core.trash.HistoryItem;
import com.bdaum.zoom.core.trash.StructBackup;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.db.internal.job.BackupJob;
import com.bdaum.zoom.db.internal.job.CleanupJob;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.BatchConstants;
import com.db4o.Db4o;
import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.CommonConfiguration;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import com.db4o.config.QueryEvaluationMode;
import com.db4o.config.encoding.StringEncodings;
import com.db4o.defragment.DefragmentConfig;
import com.db4o.diagnostic.Diagnostic;
import com.db4o.diagnostic.DiagnosticConfiguration;
import com.db4o.diagnostic.DiagnosticListener;
import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.ext.Db4oException;
import com.db4o.ext.Db4oIOException;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.ext.StoredClass;
import com.db4o.ext.SystemInfo;
import com.db4o.internal.caching.Cache4;
import com.db4o.internal.caching.CacheFactory;
import com.db4o.io.CachingStorage;
import com.db4o.io.FileStorage;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

@SuppressWarnings("restriction")
public class DbManager implements IDbManager, IAdaptable, IInputValidator {

	private static final String IMPL = "Impl"; //$NON-NLS-1$

	private static final String TYPEIMPL = "_typeImpl"; //$NON-NLS-1$

	private static final int MAXIDS = 256;

	private static final int VERSION = 16;

	private static final boolean DIAGNOSE = false;

	private static final int TIMEOUT = 30000;

	private static final String SAFE_TRANSACTION = "transaction"; //$NON-NLS-1$

	private static final String LUCENE_WRITE_LOCK = "write.lock"; //$NON-NLS-1$

	private static final long ONEDAY = 24 * 60 * 60 * 1000L;

	private Set<String> indexedFields;

	private ObjectContainer db;

	protected String fileName;

	private ObjectContainer trashCan;

	private Set<String> dirtyCollections = ConcurrentHashMap.newKeySet();

	protected Meta meta;

	private boolean transactionFailed;

	private boolean trashFailed;

	protected File file;

	protected boolean readOnly;

	protected File indexPath;

	private int dbErrorCounter;

	private boolean backupScheduled;

	protected DbFactory factory;

	private long previousDay = -1L;

	private String previousTimeline = Meta_type.timeline_no;

	private Set<String> lastFolderUris;

	private Set<String> previousLocationKeys;

	private boolean emergency;

	private EmbeddedConfiguration externalConfig;

	private File luceneLockFile;

	/**
	 * Constructor
	 *
	 * @param factory  - parent factory
	 * @param fileName - catalog file name
	 * @param newDb    - true if new catalog
	 * @param readOnly - true if opened in read only mode
	 */
	public DbManager(DbFactory factory, String fileName, boolean newDb, boolean readOnly) {
		this(factory, fileName, newDb, readOnly, null);
	}

	protected DbManager(DbFactory factory, String fileName, boolean newDb, boolean readOnly,
			EmbeddedConfiguration externalConfig) {
		this.factory = factory;
		this.fileName = fileName;
		this.externalConfig = externalConfig;
		this.file = new File(fileName);
		this.readOnly = readOnly;
		if (factory.getLireServiceVersion() >= 0) {
			String ip = fileName;
			int p = ip.lastIndexOf('.');
			if (p >= 0)
				ip = ip.substring(0, p);
			ip += Constants.INDEXEXTENSION;
			indexPath = new File(ip);
		}
		boolean reOpen = false;
		if (file.exists()) {
			if (newDb)
				file.delete();
			else {
				reOpen = true;
				this.readOnly |= !file.canWrite();
			}
		}
		// Reset lucene
		if (indexPath != null && indexPath.exists()) {
			luceneLockFile = new File(indexPath, LUCENE_WRITE_LOCK);
			if (reOpen)
				removeCrashedLuceneIndex();
			else
				Core.deleteFileOrFolder(indexPath);
		}
		// long time = System.currentTimeMillis();
		this.db = createDatabase(fileName, newDb);
		// System.out.println( System.currentTimeMillis()-time);
		// Reset trash
		new File(getTrashcanName()).delete();
	}

	public int getVersion() {
		return VERSION;
	}

	protected EmbeddedConfiguration createTrashcanConfiguration() {
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		configuration.file().reserveStorageSpace(1000000);
		configuration.file().readOnly(false);
		CommonConfiguration common = configuration.common();
		common.detectSchemaChanges(true); // Leave at true, false causes
											// problems
		common.callbacks(false);
		common.activationDepth(Integer.MAX_VALUE);
		common.stringEncoding(StringEncodings.utf8());
		ObjectClass trash = common.objectClass(Trash.class);
		trash.objectField("opId").indexed(true); //$NON-NLS-1$
		trash.objectField("name").indexed(true); //$NON-NLS-1$
		common.objectClass(HistoryItem.class).objectField("opId").indexed(true); //$NON-NLS-1$
		common.objectClass(FileLocationBackup.class).objectField("opId") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(StructBackup.class).objectField("opId") //$NON-NLS-1$
				.indexed(true);
		return configuration;
	}

	/*
	 * Deleting complex objects
	 *
	 * 1. Objects without backpointer to a parent are handeled with cascadeOnDelete.
	 * Example: GroupImpl 2. Objects with collections of primitive datatypes (String
	 * counts as primitive) are handled in the delete() method. Example: WallImpl 3.
	 * Objects with non-primitive members and backpointers are handled through
	 * specific deleteXXXX() methods. Example: deleteCollection()
	 */

	protected EmbeddedConfiguration createDatabaseConfiguration(boolean allowUpgrade, String fName,
			long reservedSpace) {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		if (reservedSpace >= 0)
			config.file().reserveStorageSpace(reservedSpace);
		final CommonConfiguration common = config.common();
		if (DIAGNOSE)
			common.messageLevel(2);
		common.allowVersionUpdates(allowUpgrade);
		common.callConstructors(true);
		common.testConstructors(false);
		common.callbacks(false);
		common.queries().evaluationMode(QueryEvaluationMode.LAZY);
		common.activationDepth(Integer.MAX_VALUE);
		common.updateDepth(2);
		common.stringEncoding(StringEncodings.utf8());
		// General
		common.detectSchemaChanges(true); // Leave at true, false causes
											// problems and doesn't improve much
		common.objectClass(IdentifiableObject.class).objectField(Constants.OID).indexed(true);
		common.objectClass(AomObject.class).indexed(false);
		// Group
		common.objectClass(GroupImpl.class).cascadeOnDelete(true);
		common.objectClass(Group_typeImpl.class).indexed(false);
		// Collections
		common.objectClass(SmartCollectionImpl.class).objectField("group_rootCollection_parent").indexed(false); //$NON-NLS-1$
		common.objectClass(SmartCollectionImpl.class).objectField("lastAccessDate").indexed(true); //$NON-NLS-1$
		common.objectClass(SmartCollection_typeImpl.class).indexed(false);
		// Criteria
		common.objectClass(CriterionImpl.class).updateDepth(1);
		common.objectClass(Criterion_typeImpl.class).indexed(false);
		common.objectClass(Criterion_typeImpl.class).cascadeOnDelete(true);
		// SortCriteria
		common.objectClass(SortCriterionImpl.class).updateDepth(1);
		common.objectClass(SortCriterion_typeImpl.class).indexed(false);
		// Relations
		ObjectClass derivedBy = common.objectClass(DerivedByImpl.class);
		derivedBy.objectField("derivative") //$NON-NLS-1$
				.indexed(true);
		derivedBy.objectField("original") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(DerivedBy_typeImpl.class).indexed(false);
		ObjectClass composedTo = common.objectClass(ComposedToImpl.class);
		composedTo.objectField("composite") //$NON-NLS-1$
				.indexed(true);
		composedTo.objectField("component") //$NON-NLS-1$
				.indexed(true);
		composedTo.cascadeOnDelete(true);
		common.objectClass(ComposedTo_typeImpl.class).indexed(false);
		// Asset
		common.objectClass(AssetImpl.class).cascadeOnDelete(true);
		ObjectClass assetType = common.objectClass(Asset_typeImpl.class);
		assetType.indexed(false);
		indexedFields = factory.getIndexedFields();
		for (String key : indexedFields)
			assetType.objectField(key).indexed(true);
		// Track and Ghost
		common.objectClass(TrackRecordImpl.class).objectField("asset_track_parent") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(TrackRecord_typeImpl.class).indexed(false);
		common.objectClass(Ghost_typeImpl.class).objectField(QueryField.NAME.getKey()).indexed(true);
		common.objectClass(Ghost_typeImpl.class).indexed(true);
		// Region
		ObjectClass region = common.objectClass(RegionImpl.class);
		region.objectField("asset_person_parent").indexed(true); //$NON-NLS-1$
		region.objectField("album").indexed(true); //$NON-NLS-1$
		common.objectClass(Region_typeImpl.class).indexed(false);
		// Location, Artwork, Contacts
		ObjectClass locationCreated = common.objectClass(LocationCreatedImpl.class);
		locationCreated.cascadeOnDelete(true);
		locationCreated.objectField("asset").indexed(true); //$NON-NLS-1$
		locationCreated.objectField("location").indexed(true); //$NON-NLS-1$
		ObjectClass locationShown = common.objectClass(LocationShownImpl.class);
		locationShown.objectField("asset").indexed(true); //$NON-NLS-1$
		locationShown.objectField("location").indexed(true); //$NON-NLS-1$
		ObjectClass artworkOrObject = common.objectClass(ArtworkOrObjectShownImpl.class);
		artworkOrObject.objectField("asset").indexed(true); //$NON-NLS-1$
		artworkOrObject.objectField("artworkOrObject").indexed(true); //$NON-NLS-1$
		ObjectClass creatorsContact = common.objectClass(CreatorsContactImpl.class);
		creatorsContact.cascadeOnDelete(true);
		creatorsContact.objectField("asset").indexed(true); //$NON-NLS-1$
		common.objectClass(CreatorsContact_typeImpl.class).indexed(false);
		common.objectClass(LocationCreated_typeImpl.class).indexed(false);
		common.objectClass(LocationShown_typeImpl.class).indexed(false);
		common.objectClass(ArtworkOrObject_typeImpl.class).indexed(false);
		creatorsContact.objectField("contact").indexed(true); //$NON-NLS-1$
		ObjectClass location = common.objectClass(LocationImpl.class);
		location.objectField("worldRegion").indexed(true); //$NON-NLS-1$
		location.objectField("countryISOCode").indexed(true); //$NON-NLS-1$
		location.objectField("countryName").indexed(true); //$NON-NLS-1$
		location.objectField("provinceOrState").indexed(true); //$NON-NLS-1$
		location.objectField("city").indexed(true); //$NON-NLS-1$
		location.objectField("sublocation").indexed(true); //$NON-NLS-1$
		location.objectField("details").indexed(true); //$NON-NLS-1$
		// Presentations
		common.objectClass(SlideShowImpl.class).objectField("group_slideshow_parent") //$NON-NLS-1$
				.indexed(false);
		common.objectClass(SlideShowImpl.class).objectField("lastAccessDate").indexed(true); //$NON-NLS-1$

		common.objectClass(SlideShow_typeImpl.class).indexed(false);

		common.objectClass(SlideImpl.class).objectField("asset") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(Slide_typeImpl.class).indexed(false);

		common.objectClass(ExhibitionImpl.class).objectField("group_exhibition_parent") //$NON-NLS-1$
				.indexed(false);
		common.objectClass(ExhibitionImpl.class).objectField("lastAccessDate").indexed(true); //$NON-NLS-1$

		common.objectClass(Exhibition_typeImpl.class).indexed(false);

		common.objectClass(ExhibitImpl.class).objectField("asset") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(Exhibit_typeImpl.class).indexed(false);

		common.objectClass(WebGalleryImpl.class).objectField("group_webGallery_parent") //$NON-NLS-1$
				.indexed(false);
		common.objectClass(WebGalleryImpl.class).objectField("lastAccessDate").indexed(true); //$NON-NLS-1$

		common.objectClass(WebGallery_typeImpl.class).indexed(false);

		common.objectClass(WebExhibitImpl.class).objectField("asset") //$NON-NLS-1$
				.indexed(true);
		common.objectClass(WebExhibit_typeImpl.class).indexed(false);
		// Categories
		common.objectClass(Category_typeImpl.class).indexed(false);
		return config;
	}

	private ObjectContainer createDatabase(String fName, boolean newDb) {
		EmbeddedConfiguration config = externalConfig != null ? externalConfig
				: createDatabaseConfiguration(false, fName, newDb ? 2500000 : -1L);
		setupCache(config);
		try {
			final ObjectContainer container = Db4oEmbedded.openFile(config, fName);
			if (DIAGNOSE) {
				StoredClass[] storedClasses = container.ext().storedClasses();
				for (StoredClass sc : storedClasses) {
					try {
						System.out.println(Class.forName(sc.getName()).getName());
					} catch (ClassNotFoundException ex) {
						System.err.println(ex);
					}
				}
				DiagnosticConfiguration diagnostic = config.common().diagnostic();
				diagnostic.addListener(new DiagnosticListener() {
					public void onDiagnostic(Diagnostic d) {
						System.out.println(d);
					}
				});
			}
			return container;
		} catch (DatabaseFileLockedException e) {
			fatalError(NLS.bind(Messages.DbManager_Unable_to_open_catalog, fName));
		} catch (Exception e) {
			fatalError(NLS.bind(Messages.DbManager_Failed_to_open_catalog, fName, e));
		}
		return null;
	}

	private static void setupCache(EmbeddedConfiguration config) {
		final int cachePageCount = 64;
		final int cachePageSize = 4096;
		config.file().storage(new CachingStorage(new FileStorage(), cachePageCount, cachePageSize) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected Cache4 newCache() {
				return CacheFactory.new2QXCache(cachePageCount);
			}
		});
	}

	private void fatalError(String msg) {
		if (!emergency)
			factory.getErrorHandler().fatalError(Messages.DbManager_Catalog_error, msg, this);
	}

	public ObjectContainer getDatabase() {
		return db;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getMeta()
	 */

	public Meta getMeta(boolean create) {
		if (meta == null) {
			meta = obtainMeta();
			if (create) {
				if (meta == null)
					meta = createMeta();
				if (!meta.getCleaned())
					new CleanupJob().schedule(5000L);
			}
		}
		return meta;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainMeta()
	 */
	private Meta obtainMeta() {
		try {
			MetaImpl metaImpl = obtainObjects(MetaImpl.class).get(0);
			if (metaImpl.getPlatform() == null)
				metaImpl.setPlatform(Platform.getOS());
			return metaImpl;
		} catch (Exception e) {
			return null;
		}
	}

	protected Meta createMeta() {
		Date creationDate = new Date();
		StringBuilder sb = new StringBuilder();
		String fName = file.getName();
		if (fName.endsWith(BatchConstants.CATEXTENSION))
			fName = fName.substring(0, fName.length() - BatchConstants.CATEXTENSION.length());
		sb.append(Constants.LOCVAR).append("/backups/").append(fName).append('.').append(Constants.DATEVAR).append( //$NON-NLS-1$
				'.' + Constants.BACKUPEXT);
		return new MetaImpl(VERSION, factory.getLireServiceVersion(), creationDate, System.getProperty("user.name"), //$NON-NLS-1$
				null, "", Messages.DbManager_User_field_1, Messages.DbManager_User_field_2, Meta_type.timeline_month, //$NON-NLS-1$
				Meta_type.locationFolders_no, new Date(0L), 0, 0, creationDate, null, sb.toString(), creationDate,
				Meta_type.thumbnailResolution_medium, false, null, null, false, 30, false, readOnly, false,
				ImageConstants.SHARPEN_MEDIUM, null, Platform.getOS(), null, Constants.PICASASCANNERVERSION, false,
				false, 75, false, null, 0L);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainById(java.lang.Class,
	 * java.lang.String)
	 */

	public <T extends IIdentifiableObject> T obtainById(Class<T> clazz, String id) {
		while (id != null && db != null)
			try {
				Query query = db.query();
				query.constrain(clazz);
				query.descend(Constants.OID).constrain(id);
				ObjectSet<T> set = query.<T>execute();
				T obj = set.hasNext() ? set.next() : null;
				if (obj != null && !readOnly && !(obj instanceof RegionImpl))
					while (set.hasNext())
						delete(set.next()); // Delete duplicates with next commit
				dbErrorCounter = 0;
				return obj;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectToDb(e);
			} catch (Db4oException e) {
				reconnectToDb(e);
			}
		return null;
	}

	@Override
	public boolean exists(Class<? extends IIdentifiableObject> clazz, String id) {
		while (id != null && db != null)
			try {
				Query query = db.query();
				query.constrain(clazz);
				query.descend(Constants.OID).constrain(id);
				boolean hasNext = query.<IIdentifiableObject>execute().hasNext();
				dbErrorCounter = 0;
				return hasNext;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectToDb(e);
			} catch (Db4oException e) {
				reconnectToDb(e);
			}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainByIds(java.lang.Class,
	 * java.lang.String[])
	 */
	public <T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, String[] ids) {
		return obtainObjects(clazz, Constants.OID, ids);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainByIds(java.lang.Class,
	 * java.util.Collection)
	 */
	public <T extends IIdentifiableObject> List<T> obtainByIds(Class<T> clazz, Collection<String> ids) {
		return obtainObjects(clazz, Constants.OID, ids);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainStructByIds(java.lang.String,
	 * java.lang.Class, java.util.Collection)
	 */
	public <T extends IIdentifiableObject> List<T> obtainStructByIds(String assetId, Class<T> clazz, String[] ids) {
		if (ids == null || ids.length == 0)
			return new ArrayList<>(0);
		IPeerService peerService = factory.getPeerService();
		if (peerService != null) {
			AssetOrigin assetOrigin = peerService.getAssetOrigin(assetId);
			if (assetOrigin != null)
				try {
					return peerService.obtainObjects(assetOrigin, clazz, Constants.OID, ids);
				} catch (ConnectionLostException e) {
					return new ArrayList<T>(0);
				}
		}
		return obtainObjects(clazz, Constants.OID, ids);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainAsset(java.lang.String)
	 */

	public AssetImpl obtainAsset(final String id) {
		return obtainById(AssetImpl.class, id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainAssets()
	 */

	public List<AssetImpl> obtainAssets() {
		return obtainObjects(AssetImpl.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainObjects(java.lang.Class)
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz) {
		while (db != null)
			try {
				ObjectSet<T> set = db.query(clazz);
				dbErrorCounter = 0;
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectToDb(e);
			}
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
			return new ArrayList<T>(0);
		IPeerService peerService = factory.getPeerService();
		if (peerService != null) {
			AssetOrigin assetOrigin = peerService.getAssetOrigin(id);
			if (assetOrigin != null)
				try {
					return peerService.obtainStructForAsset(assetOrigin, clazz, id, multiple);
				} catch (ConnectionLostException e) {
					return new ArrayList<T>(0);
				}
		}
		return obtainStruct(clazz, id, multiple, null, null, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainStruct(java.lang.Class,
	 * java.lang.String, boolean, java.lang.String, java.lang.Object, boolean)
	 */

	public <T extends IIdentifiableObject> List<T> obtainStruct(Class<T> clazz, String assetId, boolean multipleAsset,
			String field, Object v, boolean multipleField) {
		return obtainObjects(clazz, false, "asset", assetId, //$NON-NLS-1$
				multipleAsset ? QueryField.CONTAINS : QueryField.EQUALS, field, v,
				multipleField ? QueryField.CONTAINS : QueryField.EQUALS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.lang.Object, int)
	 */

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field1, Object v1, int rel1) {
		while (db != null) {
			try {
				Query query = db.query();
				query.constrain(clazz);
				if (field1 != null)
					applyRelation(rel1, query.descend(field1).constrain(v1));
				ObjectSet<T> set = query.execute();
				dbErrorCounter = 0;
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectToDb(e);
			}
		}
		return new ArrayList<T>();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.lang.String[])
	 */
	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, String[] values) {
		while (values != null && db != null && values.length > 0)
			try {
				int count = values.length;
				int n = ImageConstants.NPROCESSORS;
				if (n > 1 && count > 1) {
					final List<T> set = Collections.synchronizedList(new ArrayList<T>(count));
					final int inc = (count + n - 1) / n;
					IntStream.range(0, n).parallel().forEach(
							p -> fetchByIds(clazz, field, values, p * inc, Math.min(count, (p + 1) * inc), set));
					dbErrorCounter = 0;
					return set;
				}
				List<T> set = new ArrayList<T>(count);
				fetchByIds(clazz, field, values, 0, count, set);
				dbErrorCounter = 0;
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectToDb(e);
			}
		return new ArrayList<T>(0);
	}

	public <T extends IIdentifiableObject> List<T> obtainParentObjects(Class<T> clazz, String field,
			Collection<? extends IIdentifiableObject> children) {
		if (children != null && db != null && !children.isEmpty()) {
			String[] ids = new String[children.size()];
			int i = 0;
			for (IIdentifiableObject child : children)
				ids[i++] = child.getStringId();
			return obtainObjects(clazz, field, ids);
		}
		return new ArrayList<T>(0);
	}

	private <T> void fetchByIds(Class<T> clazz, String field, String[] ids, int lower, int upper, Collection<T> set) {
		if (upper - lower > MAXIDS) {
			int segments = (upper - lower + MAXIDS - 1) / MAXIDS;
			int inc = (upper - lower + segments - 1) / segments;
			for (int i = lower; i < upper; i += inc)
				fetchByIds(clazz, field, ids, i, Math.min(upper, i + inc), set);
		} else if (upper > lower) {
			Query query = db.query();
			query.constrain(clazz);
			Constraint or = query.descend(field).constrain(ids[lower]);
			for (int i = lower + 1; i < upper; i++)
				or = query.descend(field).constrain(ids[i]).or(or);
			set.addAll(query.execute());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#obtainObjects(java.lang.Class,
	 * java.lang.String, java.util.Collection)
	 */
	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field, Collection<String> ids) {
		if (ids != null && db != null && !ids.isEmpty())
			return obtainObjects(clazz, field, ids.toArray(new String[ids.size()]));
		return new ArrayList<T>(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainAssetsForFile(java.net.URI)
	 */

	public List<AssetImpl> obtainAssetsForFile(URI uri) {
		while (uri != null && db != null)
			try {
				Query query = db.query();
				query.constrain(AssetImpl.class);
				query.descend(QueryField.NAME.getKey()).constrain(Core.getFileName(uri, false));
				ObjectSet<AssetImpl> set = query.execute();
				if (!set.hasNext()) {
					dbErrorCounter = 0;
					return set;
				}
				List<AssetImpl> matching = new ArrayList<AssetImpl>(set.size());
				IVolumeManager vm = Core.getCore().getVolumeManager();
				for (AssetImpl asset : set)
					if (uri.equals(vm.findFile(asset)))
						matching.add(asset);
				dbErrorCounter = 0;
				return matching;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectToDb(e);
			} catch (Db4oException e) {
				reconnectToDb(e);
			}
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#obtainGhostsForFile(java.net.URI)
	 */

	public List<Ghost_typeImpl> obtainGhostsForFile(URI uri) {
		while (uri != null && db != null)
			try {
				Query query = db.query();
				query.constrain(Ghost_typeImpl.class);
				query.descend(QueryField.NAME.getKey()).constrain(Core.getFileName(uri, false));
				ObjectSet<Ghost_typeImpl> set = query.execute();
				if (!set.hasNext()) {
					dbErrorCounter = 0;
					return set;
				}
				List<Ghost_typeImpl> matching = new ArrayList<Ghost_typeImpl>(set.size());
				IVolumeManager vm = Core.getCore().getVolumeManager();
				for (Ghost_typeImpl ghost : set)
					if (uri.equals(vm.findFile(ghost.getUri(), ghost.getVolume())))
						matching.add(ghost);
				dbErrorCounter = 0;
				return matching;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectToDb(e);
			} catch (Db4oException e) {
				reconnectToDb(e);
			}
		return Collections.emptyList();
	}

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, String field1, Object v1, int rel1,
			String field2, Object v2, int rel2, boolean or) {
		return obtainObjects(clazz, or, field1, v1, rel1, field2, v2, rel2);
	}

	private static void applyRelation(int rel1, Constraint constraint1) {
		switch (rel1) {
		case QueryField.STARTSWITH:
			constraint1.startsWith(false);
			break;
		case QueryField.CONTAINS:
			constraint1.contains();
			break;
		case QueryField.ENDSWITH:
			constraint1.endsWith(false);
			break;
		case QueryField.GREATER:
			constraint1.greater();
			break;
		case QueryField.SMALLER:
			constraint1.smaller();
			break;
		case QueryField.NOTGREATER:
			constraint1.greater().not();
			break;
		case QueryField.NOTSMALLER:
			constraint1.smaller().not();
			break;
		case QueryField.NOTEQUAL:
			constraint1.not();
			break;
		case QueryField.DOESNOTCONTAIN:
			constraint1.contains().not();
			break;
		case QueryField.DOESNOTSTARTWITH:
			constraint1.startsWith(false).not();
			break;
		case QueryField.DOESNOTENDWITH:
			constraint1.endsWith(false).not();
			break;
		}
	}

	public <T extends IIdentifiableObject> List<T> obtainObjects(Class<T> clazz, boolean or,
			Object... namesValuesRelations) {
		while (db != null)
			try {
				Query query = db.query();
				query.constrain(clazz);
				Constraint constraint1 = null;
				for (int i = 0; i < namesValuesRelations.length - 2; i += 3) {
					Object field = namesValuesRelations[i];
					Object rel = namesValuesRelations[i + 2];
					Constraint constraint2 = null;
					if (field instanceof String && rel instanceof Integer) {
						constraint2 = query.descend((String) field).constrain(namesValuesRelations[i + 1]);
						applyRelation((Integer) rel, constraint2);
						constraint1 = constraint1 != null
								? or ? constraint1.or(constraint2) : constraint1.and(constraint2)
								: constraint2;
					}
				}
				ObjectSet<T> set = query.execute();
				dbErrorCounter = 0;
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectToDb(e);
			}
		return new ArrayList<T>();
	}

	public List<LocationImpl> findLocation(final LocationImpl location) {
		while (location != null && db != null)
			try {
				Query query = db.query();
				query.constrain(LocationImpl.class);
				Constraint c = null;
				if (location.getCity() != null)
					c = query.descend("city").constrain(location.getCity()); //$NON-NLS-1$
				if (location.getCountryISOCode() != null) {
					Constraint c2 = query.descend("countryISOCode").constrain( //$NON-NLS-1$
							location.getCountryISOCode());
					c = c != null ? c2.and(c) : c2;
				} else if (location.getCountryName() != null) {
					Constraint c2 = query.descend("countryName").constrain( //$NON-NLS-1$
							location.getCountryName());
					c = c != null ? c2.and(c) : c2;
				}
				if (location.getProvinceOrState() != null) {
					Constraint c2 = query.descend("provinceOrState").constrain( //$NON-NLS-1$
							location.getProvinceOrState());
					c = c != null ? c2.and(c) : c2;
				}
				if (location.getSublocation() != null) {
					Constraint c2 = query.descend("sublocation").constrain( //$NON-NLS-1$
							location.getSublocation());
					c = c != null ? c2.and(c) : c2;
				}
				if (location.getDetails() != null) {
					Constraint c2 = query.descend("details").constrain( //$NON-NLS-1$
							location.getDetails());
					c = c != null ? c2.and(c) : c2;
				}
				ObjectSet<LocationImpl> set = query.execute();
				dbErrorCounter = 0;
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectToDb(e);
			} catch (Db4oException e) {
				reconnectToDb(e);
			}
		return Collections.emptyList();

	}

	public <T extends IIdentifiableObject> List<T> queryByExample(T example) {
		if (example != null) {
			String id = example.getStringId();
			example.setStringId(null);
			try {
				while (db != null) {
					try {
						ObjectSet<T> set = db.queryByExample(example);
						dbErrorCounter = 0;
						return set;
					} catch (DatabaseClosedException e) {
						break;
					} catch (IllegalStateException e) {
						reconnectToDb(e);
					} catch (Db4oException e) {
						reconnectToDb(e);
					}
				}
			} finally {
				example.setStringId(id);
			}
		}
		return new ArrayList<T>(0);
	}

	public void storeAndCommit(Object obj) {
		store(obj);
		commit();
	}

	public void reconnectToDb(RuntimeException e) {
		// Reset lucene
		if (++dbErrorCounter > 100 || emergency)
			throw e;
		endSafeTransaction();
		removeLuceneWriteLock();
		while (!file.exists())
			promptForReconnect();
		try {
			db.close();
		} catch (Exception e2) {
			// ignore
		}
		db = createDatabase(fileName, false);
	}

	private boolean removeLuceneWriteLock() {
		return luceneLockFile == null ? false : luceneLockFile.delete();
	}

	public boolean isLuceneLocked() {
		return luceneLockFile != null && luceneLockFile.exists();
	}

	public File getIndexPath() {
		return indexPath;
	}

	public <T extends Object> List<T> getTrash(Class<T> clazz, String opId) {
		while (trashCan != null)
			try {
				Query query = trashCan.query();
				query.constrain(clazz);
				if (opId != null)
					query.descend("opId").constrain(opId); //$NON-NLS-1$
				return query.execute();
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				reconnectTrashcanToDb(e);
			} catch (Db4oException e) {
				reconnectTrashcanToDb(e);
			}
		return new ArrayList<T>();
	}

	public List<Trash> obtainTrashToDelete(boolean withFiles) {
		while (trashCan != null)
			try {
				ObjectSet<Trash> trash;
				if (withFiles) {
					Query query = trashCan.query();
					query.constrain(Trash.class);
					query.descend("files").constrain(true); //$NON-NLS-1$
					trash = query.execute();
				} else
					trash = trashCan.query(Trash.class);
				dbErrorCounter = 0;
				return trash;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectTrashcanToDb(e);
			}
		return Collections.emptyList();
	}

	public List<Trash> obtainTrash(boolean byName) {
		while (trashCan != null)
			try {
				Query query = trashCan.query();
				query.constrain(Trash.class);
				if (byName)
					query.descend("name").orderAscending(); //$NON-NLS-1$
				else
					query.descend("date").orderDescending(); //$NON-NLS-1$
				ObjectSet<Trash> trash = query.execute();
				dbErrorCounter = 0;
				return trash;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectTrashcanToDb(e);
			}
		return Collections.emptyList();
	}

	public List<Trash> obtainTrashForFile(URI uri) {
		while (uri != null && trashCan != null)
			try {
				Query query = trashCan.query();
				query.constrain(Trash.class);
				query.descend("name").constrain(Core.getFileName(uri, false)); //$NON-NLS-1$
				ObjectSet<Trash> set = query.execute();
				dbErrorCounter = 0;
				if (!set.hasNext())
					return set;
				List<Trash> matching = new ArrayList<Trash>(set.size());
				IVolumeManager vm = Core.getCore().getVolumeManager();
				for (Trash t : set)
					if (uri.equals(vm.findFile(t.getUri(), t.getVolume())))
						matching.add(t);
				return matching;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException | Db4oException e) {
				reconnectTrashcanToDb(e);
			}
		return Collections.emptyList();
	}

	private void reconnectTrashcanToDb(RuntimeException e) {
		if (++dbErrorCounter > 100 || emergency)
			throw e;
		String trashcanName = getTrashcanName();
		while (!new File(trashcanName).exists())
			promptForReconnect();
		try {
			trashCan.close();
		} catch (Exception e2) {
			// ignore
		}
		trashCan = Db4oEmbedded.openFile(createTrashcanConfiguration(), trashcanName);
	}

	private void promptForReconnect() {
		factory.getErrorHandler().promptForReconnect(Messages.DbManager_lost_connection,
				Messages.DbManager_reinsert_media, this, this);
	}
	
	public String isValid(String newText) {
		return file.exists() ? null : Messages.DbManager_file_missing;
	}

	public String getTrashcanName() {
		return fileName + ".history"; //$NON-NLS-1$
	}

	public ObjectContainer getTrashCan() {
		if (trashCan == null)
			trashCan = Db4oEmbedded.openFile(createTrashcanConfiguration(), getTrashcanName());
		return trashCan;
	}

	public boolean hasTrash() {
		return trashCan != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getFileName()
	 */

	public String getFileName() {
		return fileName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getFile()
	 */

	public File getFile() {
		return file;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#close(int)
	 */
	public void close(int mode) {
		if (mode == CatalogListener.NORMAL)
			for (IDbListener listener : factory.getListeners())
				try {
					if (!listener.databaseAboutToClose(this))
						return;
				} catch (Exception e) {
					// ignore
				}
		else if (mode == CatalogListener.EMERGENCY)
			emergency = true;
		resetSystemCollectionCache();
		if (db != null) {
			if (!emergency) // db4o has its own shutdown hook
				try {
					db.close();
				} catch (Db4oIOException e) {
					// ignore
				} catch (IllegalStateException e) {
					// ignore
				}
			db = null;
			meta = null;
			dirtyCollections.clear();
			transactionFailed = false;
		}
		for (Object listener : factory.getListeners())
			try {
				((IDbListener) listener).databaseClosed(this, mode);
			} catch (Exception e) {
				// ignore
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#closeTrash()
	 */

	public void closeTrash() {
		if (hasTrash()) {
			try {
				trashCan.close();
			} catch (Db4oIOException e) {
				// ignore
			} catch (IllegalStateException e) {
				// ignore
			}
			trashCan = null;
			trashFailed = false;
			new File(getTrashcanName()).delete();
		}
	}

	public boolean createFolderHierarchy(final Asset asset) {
		return createFolderHierarchy(asset, Constants.WIN32);
	}

	public boolean createFolderHierarchy(final Asset asset, final boolean win32) {
		final boolean[] changeIndicator = new boolean[] { false };
		safeTransaction(() -> {
			try {
				String path = asset.getUri();
				int p = path.lastIndexOf('/');
				if (p > 0) {
					String folderUri = path.substring(0, p);
					if (lastFolderUris == null)
						lastFolderUris = new HashSet<>();
					else if (lastFolderUris.contains(folderUri))
						return;
					lastFolderUris.add(folderUri);
				}
				GroupImpl directories = getCollectionGroup(Constants.GROUP_ID_FOLDERSTRUCTURE,
						Messages.DbManager_Directories, true);
				SmartCollection parentColl = null;
				int pathFrom = 0;
				StringBuilder sb = new StringBuilder();
				String protocol = ""; //$NON-NLS-1$
				String pname = ":"; //$NON-NLS-1$
				String device = ""; //$NON-NLS-1$
				p = path.indexOf(':', pathFrom);
				if (p >= pathFrom) {
					sb.append(protocol = path.substring(pathFrom, p)).append(':');
					pname = sb.toString();
					pathFrom = p;
				}
				while (path.charAt(++pathFrom) == '/')
					sb.append('/');
				String prot = sb.toString();
				if (!Constants.FILESCHEME.equals(protocol))
					parentColl = createDirectoryCollection(pname, parentColl, directories, QueryField.URI.getKey(),
							prot, QueryField.STARTSWITH, changeIndicator);
				else if (win32) {
					p = path.indexOf(':', pathFrom);
					if (p >= pathFrom) {
						sb.append(device = path.substring(pathFrom, p + 1));
						pathFrom = p;
						while (path.charAt(++pathFrom) == '/')
							sb.append('/');
						String volume = asset.getVolume();
						if (volume == null || volume.isEmpty())
							volume = device;
						parentColl = createDirectoryCollection(volume, parentColl, directories,
								QueryField.VOLUME.getKey(), volume, QueryField.EQUALS, changeIndicator);
					}
				}
				StringTokenizer tokenizer = new StringTokenizer(path.substring(pathFrom), "/", true); //$NON-NLS-1$
				String previousToken = null;
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					sb.append(token);
					if ("/".equals(token)) { //$NON-NLS-1$
						if (previousToken != null) {
							try {
								previousToken = URLDecoder.decode(previousToken, "UTF-8"); //$NON-NLS-1$
							} catch (UnsupportedEncodingException e1) {
								// do nothing
							}
							parentColl = createDirectoryCollection(previousToken, parentColl, directories,
									QueryField.URI.getKey(), sb.toString(), QueryField.STARTSWITH, changeIndicator);
							previousToken = null;
						}
					} else
						previousToken = token;
				}
			} catch (RuntimeException e2) {
				DbActivator.getDefault().logError(Messages.DbManager_internal_error_folder_hierarchy, e2);
			}

		});
		return changeIndicator[0];
	}

	public SmartCollection createDirectoryCollection(String name, SmartCollection parentColl, GroupImpl group,
			String fieldname, String fieldvalue, int rel, boolean[] changeIndicator) {
		String key = new StringBuilder(fieldname).append('=').append(fieldvalue).toString();
		List<SmartCollectionImpl> set = obtainObjects(SmartCollectionImpl.class, Constants.OID, key, QueryField.EQUALS);
		SmartCollectionImpl coll1 = null;
		if (set.isEmpty()) {
			coll1 = new SmartCollectionImpl(name, true, false, false, false, null, 0, null, 0, null,
					Constants.INHERIT_LABEL, null, 0, 1, null);
			coll1.setStringId(key);
			Criterion crit = new CriterionImpl(fieldname, null, fieldvalue, null, rel, false);
			coll1.addCriterion(crit);
			SortCriterion sortCrit = new SortCriterionImpl(fieldname, null, false);
			coll1.addSortCriterion(sortCrit);
			if (parentColl != null) {
				parentColl.addSubSelection(coll1);
				store(parentColl);
			} else {
				group.addRootCollection(key);
				coll1.setGroup_rootCollection_parent(group.getStringId());
				store(group);
			}
			store(coll1);
			store(crit);
			store(sortCrit);
			changeIndicator[0] = true;
		} else {
			for (SmartCollectionImpl sm : set) {
				if (coll1 == null)
					coll1 = sm;
				else {
					// remove duplicates
					changeIndicator[0] = true;
					transferChildren(sm, coll1);
					SmartCollection psm = sm.getSmartCollection_subSelection_parent();
					if (psm != null && psm != parentColl) {
						psm.removeSubSelection(sm);
						store(psm);
					}
					String gsmid = sm.getGroup_rootCollection_parent();
					if (gsmid != null && !gsmid.equals(group.getStringId())) {
						GroupImpl gsm = obtainById(GroupImpl.class, gsmid);
						if (gsm != null && gsm != group) {
							gsm.removeRootCollection(sm.getStringId());
							store(gsm);
						}
					}
					deleteCollection(sm);
				}
				if (parentColl != null)
					parentColl.removeSubSelection(sm);
				else
					group.removeRootCollection(sm.getStringId());
			}
			if (parentColl != null) {
				parentColl.addSubSelection(coll1);
				coll1.setSmartCollection_subSelection_parent(parentColl);
				store(parentColl);
			} else {
				group.addRootCollection(coll1.getStringId());
				coll1.setGroup_rootCollection_parent(group.getStringId());
				store(group);
			}
			store(coll1);
		}
		return coll1;
	}

	private void transferChildren(SmartCollection source, SmartCollection target) {
		SmartCollection found = null;
		for (Iterator<SmartCollection> it = source.getSubSelection().iterator(); it.hasNext();) {
			SmartCollection subcol = it.next();
			String id = subcol.getStringId();
			for (SmartCollection cand : target.getSubSelection())
				if (cand.getStringId().equals(id)) {
					found = cand;
					if (subcol == cand)
						it.remove();
					else
						transferChildren(subcol, found);
					break;
				}
			if (found == null) {
				target.addSubSelection(subcol);
				subcol.setSmartCollection_subSelection_parent(target);
				it.remove();
			}
		}
	}

	private void deleteCollection(SmartCollection collection) {
		if (collection != null) {
			AomList<SmartCollection> subSelections = collection.getSubSelection();
			if (subSelections != null)
				for (SmartCollection sub : subSelections)
					if (sub.getSmartCollection_subSelection_parent() == null
							|| sub.getSmartCollection_subSelection_parent() == collection)
						deleteCollection(sub);
			AomList<Criterion> criteria = collection.getCriterion();
			if (criteria != null)
				for (Criterion crit : criteria)
					delete(crit);
			AomList<SortCriterion> sortCriteria = collection.getSortCriterion();
			if (sortCriteria != null)
				for (SortCriterion crit : sortCriteria)
					delete(crit);
			delete(collection.getPostProcessor());
			delete(collection);
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#createLastImportCollection(java.util
	 * .Date, boolean, java.lang.String)
	 */
	public Date createLastImportCollection(Date importDate, boolean cumulate, String description, boolean tethered) {
		Date previousImport = null;
		GroupImpl group = getImportGroup();
		GroupImpl subgroup = null;
		int showLabel = Constants.INHERIT_LABEL;
		String labelTemplate = null;
		SmartCollectionImpl coll = obtainById(SmartCollectionImpl.class, Constants.LAST_IMPORT_ID);
		if (coll != null) {
			Criterion criterion = coll.getCriterion().isEmpty() ? null : coll.getCriterion(0);
			if (criterion != null) {
				Object value = criterion.getValue();
				Object to = criterion.getTo();
				if (to != null) {
					previousImport = (Date) to;
					if (criterion.getAnd() && tethered
							|| cumulate && (description == null || description.equals(coll.getDescription()))) {
						criterion.setTo(importDate);
						store(criterion);
						return previousImport;
					}
				} else
					previousImport = (Date) value;
				group.removeRootCollection(Constants.LAST_IMPORT_ID);
				subgroup = getRecentImportsSubgroup(group);
				subgroup.addRootCollection(Utilities.setImportKeyAndLabel(coll, value, to));
				coll.setGroup_rootCollection_parent(subgroup.getStringId());
				store(coll);
			} else {
				group.removeRootCollection(Constants.LAST_IMPORT_ID);
				delete(coll);
			}
			showLabel = coll.getShowLabel();
			labelTemplate = coll.getLabelTemplate();
		}
		SmartCollectionImpl newcoll = new SmartCollectionImpl(
				tethered ? Messages.DbManager_tethered
						: cumulate ? Messages.DbManager_recent_bg_imports : Messages.DbManager_last_import,
				true, false, false, false, description, 0, null, 0, null, showLabel, labelTemplate, 0, 1, null);
		newcoll.setStringId(Constants.LAST_IMPORT_ID);
		Criterion criterion = new CriterionImpl(QueryField.IMPORTDATE.getKey(), null, "", //$NON-NLS-1$
				null, cumulate ? QueryField.BETWEEN : QueryField.DATEEQUALS, false);
		newcoll.addCriterion(criterion);
		SortCriterion sortCrit = new SortCriterionImpl(QueryField.NAME.getKey(), null, false);
		newcoll.addSortCriterion(sortCrit);
		newcoll.setGroup_rootCollection_parent(group.getStringId());
		group.addRootCollection(Constants.LAST_IMPORT_ID);
		criterion.setValue(importDate);
		criterion.setAnd(tethered);
		criterion.setTo(tethered || cumulate ? importDate : null);
		store(criterion);
		store(sortCrit);
		store(newcoll);
		store(group);
		if (subgroup != null)
			store(subgroup);
		return previousImport;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getImportGroup()
	 */

	private GroupImpl getRecentImportsSubgroup(GroupImpl group) {
		GroupImpl subgroup = obtainById(GroupImpl.class, Constants.GROUP_ID_RECENTIMPORTS);
		if (subgroup == null) {
			subgroup = new GroupImpl(Messages.DbManager_recent_imports, true, Constants.INHERIT_LABEL, null, 0, 1,
					null);
			subgroup.setStringId(Constants.GROUP_ID_RECENTIMPORTS);
			subgroup.setGroup_subgroup_parent(group);
			group.addSubgroup(subgroup);
			for (Iterator<String> it = group.getRootCollection().iterator(); it.hasNext();) {
				String id = it.next();
				if (!id.equals(Constants.LAST_IMPORT_ID)) {
					SmartCollectionImpl coll = obtainById(SmartCollectionImpl.class, id);
					if (coll != null) {
						coll.setGroup_rootCollection_parent(Constants.GROUP_ID_RECENTIMPORTS);
						store(coll);
						subgroup.addRootCollection(id);
					}
					it.remove();
				}
			}
		}
		return subgroup;
	}

	public GroupImpl getImportGroup() {
		return getCollectionGroup(Constants.GROUP_ID_IMPORTS, Messages.DbManager_imports, true);
	}

	private GroupImpl getCollectionGroup(String id, String label, boolean system) {
		GroupImpl group = obtainById(GroupImpl.class, id);
		if (group == null) {
			group = new GroupImpl(label, system, Constants.INHERIT_LABEL, null, 0, 1, null);
			group.setStringId(id);
		}
		return group;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#createTimeLine(com.bdaum.zoom.cat.model
	 * .asset.Asset, java.lang.String)
	 */

	public boolean createTimeLine(final Asset asset, final String timeline) {
		if (timeline == null || timeline.equals(Meta_type.timeline_no))
			return false;
		Date dateCreated = asset.getDateCreated();
		if (dateCreated == null)
			dateCreated = asset.getDateTimeOriginal();
		if (dateCreated == null)
			return false;
		long day = dateCreated.getTime() / ONEDAY;
		if (previousDay == day && previousTimeline == timeline)
			return false;
		previousDay = day;
		previousTimeline = timeline;
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dateCreated);
		final boolean[] changeIndicator = new boolean[] { false };
		safeTransaction(() -> {
			try {
				GroupImpl timeLineGroup = getCollectionGroup(Constants.GROUP_ID_TIMELINE, Messages.DbManager_Timeline,
						true);
				int year = cal.get(Calendar.YEAR);
				StringBuilder ksb = new StringBuilder(DATETIMEKEY);
				ksb.append(year);
				SmartCollectionImpl parentColl = createTimelineCollection(String.valueOf(year), null, timeLineGroup,
						ksb.toString(), new GregorianCalendar(year, 0, 1),
						new GregorianCalendar(year, 11, 31, 23, 59, 59), changeIndicator);
				store(timeLineGroup);
				store(parentColl);
				if (!timeline.equals(Meta_type.timeline_year)) {
					if (timeline.equals(Meta_type.timeline_week) || timeline.equals(Meta_type.timeline_weekAndDay)) {
						int week = cal.get(Calendar.WEEK_OF_YEAR);
						GregorianCalendar from1 = new GregorianCalendar(year, 0, 1);
						from1.set(GregorianCalendar.WEEK_OF_YEAR, week);
						from1.set(GregorianCalendar.DAY_OF_WEEK, 0);
						ksb.append("-W").append(week); //$NON-NLS-1$
						GregorianCalendar to = new GregorianCalendar(year, 0, 1);
						to.set(Calendar.WEEK_OF_YEAR, week);
						to.set(Calendar.DAY_OF_WEEK, 6);
						to.set(Calendar.HOUR_OF_DAY, 23);
						to.set(Calendar.MINUTE, 59);
						to.set(Calendar.SECOND, 59);
						parentColl = createTimelineCollection(Format.WEEK.get().format(from1.getTime()), parentColl,
								timeLineGroup, ksb.toString(), from1, to, changeIndicator);
						if (!timeline.equals(Meta_type.timeline_week)) {
							int day11 = cal.get(Calendar.DAY_OF_WEEK);
							ksb.append('-').append(day11);
							from1 = new GregorianCalendar(year, 0, 1);
							from1.set(Calendar.WEEK_OF_YEAR, week);
							from1.set(Calendar.DAY_OF_WEEK, day11);
							to = new GregorianCalendar(year, 0, 1);
							to.set(Calendar.WEEK_OF_YEAR, week);
							to.set(Calendar.DAY_OF_WEEK, day11);
							to.set(Calendar.HOUR_OF_DAY, 23);
							to.set(Calendar.MINUTE, 59);
							to.set(Calendar.SECOND, 59);
							createTimelineCollection(Format.WEEKDAY_FORMAT.get().format(from1.getTime()), parentColl,
									timeLineGroup, ksb.toString(), from1, to, changeIndicator);
						}
					} else {
						int month = cal.get(Calendar.MONTH);
						GregorianCalendar from2 = new GregorianCalendar(year, month, 1);
						ksb.append('-').append(month);
						parentColl = createTimelineCollection(Format.MONTH_FORMAT.get().format(from2.getTime()),
								parentColl, timeLineGroup, ksb.toString(), from2, new GregorianCalendar(year, month,
										from2.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59),
								changeIndicator);
						if (!timeline.equals(Meta_type.timeline_month)) {
							int day12 = cal.get(Calendar.DAY_OF_MONTH);
							ksb.append('-').append(day12);
							createTimelineCollection(String.valueOf(day12), parentColl, timeLineGroup, ksb.toString(),
									new GregorianCalendar(year, month, day12),
									new GregorianCalendar(year, month, day12, 23, 59, 59), changeIndicator);
						}
					}
				}
			} catch (RuntimeException e) {
				DbActivator.getDefault().logError(Messages.DbManager_internal_error_timeline, e);
			}
		});
		return changeIndicator[0];
	}

	public SmartCollectionImpl createTimelineCollection(String name, SmartCollection parentColl, GroupImpl timeLine,
			String id, GregorianCalendar from, GregorianCalendar to, boolean[] changeIndicator) {
		SmartCollectionImpl coll = obtainById(SmartCollectionImpl.class, id);
		if (coll == null) {
			coll = new SmartCollectionImpl(name, true, false, false, false, null, 0, null, 0, null,
					Constants.INHERIT_LABEL, null, 0, 1, null);
			coll.setStringId(id);
			Criterion exifCrit = new CriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, from.getTime(),
					to.getTime(), QueryField.BETWEEN, false);
			coll.addCriterion(exifCrit);
			SortCriterion sortCrit1 = new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, false);
			coll.addSortCriterion(sortCrit1);
			if (parentColl != null) {
				parentColl.addSubSelection(coll);
				store(parentColl);
			} else {
				coll.setGroup_rootCollection_parent(timeLine.getStringId());
				timeLine.addRootCollection(coll.getStringId());
			}
			store(coll);
			store(exifCrit);
			store(sortCrit1);
			changeIndicator[0] = true;
		}
		return coll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#createLocationFolders(com.bdaum.zoom
	 * .cat.model.asset.Asset, java.lang.String)
	 */
	public boolean createLocationFolders(Asset asset, String locationOption) {
		if (locationOption != null && !locationOption.equals(Meta_type.locationFolders_no))
			for (LocationCreatedImpl locationCreated : new ArrayList<LocationCreatedImpl>(
					obtainStructForAsset(LocationCreatedImpl.class, asset.getStringId(), true))) {
				LocationImpl loc = obtainById(LocationImpl.class, locationCreated.getLocation());
				if (loc != null)
					return createLocationFolders(loc, locationOption);
			}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#createLocationFolders(com.bdaum.zoom
	 * .cat.model.location.Location, java.lang.String)
	 */
	public boolean createLocationFolders(final Location location, final String locationOption) {
		if (locationOption == null || locationOption.equals(Meta_type.locationFolders_no))
			return false;
		final StringBuilder ksb = new StringBuilder(96);
		final String regionCode = location.getWorldRegionCode();
		final String countryCode = location.getCountryISOCode();
		final String state = location.getProvinceOrState();
		final String city = location.getCity();
		ksb.append(LOCATIONKEY).append(regionCode).append('|').append(countryCode).append('|').append(state).append('|')
				.append(city);
		final String locationKey = ksb.toString();
		if (previousLocationKeys == null)
			previousLocationKeys = new HashSet<>();
		else if (previousLocationKeys.contains(locationKey))
			return false;
		previousLocationKeys.add(locationKey);
		ksb.setLength(LOCATIONKEY.length());
		final boolean[] changeIndicator = new boolean[] { false };
		safeTransaction(() -> {
			try {
				GroupImpl locationGroup = getCollectionGroup(Constants.GROUP_ID_LOCATIONS, Messages.DbManager_locations,
						true);
				// Continent
				ksb.append(regionCode);
				String name = regionCode != null ? GeoMessages.getString(GeoMessages.PREFIX + regionCode)
						: Messages.DbManager_unknown_world_region;
				Criterion crit = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
						QueryField.LOCATION_WORLDREGIONCODE.getKey(), regionCode, null,
						regionCode == null || regionCode.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS, true);
				SmartCollectionImpl parentColl = createLocationCollection(name, null, locationGroup, ksb.toString(),
						changeIndicator, crit);
				store(locationGroup);
				store(parentColl);
				// Country
				ksb.append('|').append(countryCode);
				name = location.getCountryName();
				if (name == null)
					name = Messages.DbManager_unknown_country;
				crit = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
						QueryField.LOCATION_COUNTRYCODE.getKey(), countryCode, null,
						countryCode == null || countryCode.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS, true);
				parentColl = createLocationCollection(name, parentColl, null, ksb.toString(), changeIndicator, crit);
				if (!locationOption.equals(Meta_type.locationFolders_country)) {
					// State
					ksb.append('|').append(state);
					name = state;
					if (name == null)
						name = Messages.DbManager_unknown_state;
					crit = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
							QueryField.LOCATION_STATE.getKey(), state, null,
							state == null || state.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS, true);
					CriterionImpl crit2 = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
							QueryField.LOCATION_COUNTRYCODE.getKey(), countryCode, null,
							countryCode == null || countryCode.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS,
							true);
					parentColl = createLocationCollection(name, parentColl, null, ksb.toString(), changeIndicator, crit,
							crit2);
					if (!locationOption.equals(Meta_type.locationFolders_state)) {
						// City
						name = city;
						if (name == null)
							name = Messages.DbManager_unknown_city;
						crit = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
								QueryField.LOCATION_CITY.getKey(), city, null,
								city == null || city.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS, true);

						crit2 = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
								QueryField.LOCATION_STATE.getKey(), state, null,
								state == null || state.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS, true);
						CriterionImpl crit3 = new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(),
								QueryField.LOCATION_COUNTRYCODE.getKey(), countryCode, null,
								countryCode == null || countryCode.isEmpty() ? QueryField.UNDEFINED : QueryField.EQUALS,
								true);
						parentColl = createLocationCollection(name, parentColl, null, locationKey, changeIndicator,
								crit, crit2, crit3);
					}
				}
			} catch (RuntimeException e) {
				DbActivator.getDefault().logError(Messages.DbManager_internal_error_creation_locations, e);
			}

		});
		return changeIndicator[0];
	}

	protected SmartCollectionImpl createLocationCollection(String name, SmartCollectionImpl parentColl,
			GroupImpl locationGroup, String id, boolean[] changeIndicator, Criterion... criteria) {
		SmartCollectionImpl coll = obtainById(SmartCollectionImpl.class, id);
		if (coll == null) {
			coll = new SmartCollectionImpl(name, true, false, false, false, null, 0, null, 0, null,
					Constants.INHERIT_LABEL, null, 0, 1, null);
			coll.setStringId(id);
			for (Criterion criterion : criteria)
				coll.addCriterion(criterion);
			SortCriterion sortCrit1 = new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, false);
			coll.addSortCriterion(sortCrit1);
			if (parentColl != null) {
				parentColl.addSubSelection(coll);
				store(parentColl);
			} else {
				coll.setGroup_rootCollection_parent(locationGroup.getStringId());
				locationGroup.addRootCollection(coll.getStringId());
			}
			store(coll);
			for (Criterion criterion : criteria)
				store(criterion);
			store(sortCrit1);
			changeIndicator[0] = true;
		}
		return coll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#checkDbSanity(boolean)
	 *
	 * May fail due to DB4O COR-2105
	 */
	public void checkDbSanity(final boolean force) {
		// MessageDialog.openInformation(null, "", systemInfo.freespaceSize()
		// + ", " + systemInfo.totalSize() + "; "
		// + systemInfo.freespaceEntryCount());
		if (!emergency)
			try {
				if (force || needsDefragmentation()) {
					Meta meta1 = getMeta(false);
					boolean hasBackups = meta1 == null
							|| meta1.getBackupLocation() != null && !meta1.getBackupLocation().isEmpty();
					if (force || hasBackups
							|| factory.getErrorHandler().question(Messages.DbManager_Catalog_maintenance,
									Messages.DbManager_The_cat_seems_to_be_fragmented, this)) {
						SystemInfo systemInfo = getSystemInfo();
						long totalSize = systemInfo.totalSize();
						long freespaceSize = systemInfo.freespaceSize();
						long occupiedspaceSize = totalSize - freespaceSize;
						final long reserve = isReadOnly() ? occupiedspaceSize / 100 : occupiedspaceSize / 20;
						final long defragSpaceSize = occupiedspaceSize + reserve;
						long requiredSpace = defragSpaceSize + (force ? 0 : totalSize);
						long availableSpace = file.getUsableSpace();
						if (availableSpace < requiredSpace) {
							factory.getErrorHandler().showError(Messages.DbManager_Catalog_maintenance,
									NLS.bind(Messages.DbManager_not_enough_disc_space, requiredSpace, availableSpace),
									this);
							return;
						}
						final String backupPath = fileName + ".defrag"; //$NON-NLS-1$
						File backup = new File(backupPath);
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
						try {
							dialog.run(true, false, new IRunnableWithProgress() {

								public void run(IProgressMonitor monitor)
										throws InvocationTargetException, InterruptedException {
									try {
										monitor.beginTask(NLS.bind(Messages.DbManager_Defragmenting_, fileName),
												IProgressMonitor.UNKNOWN);
										if (!force) {
											monitor.subTask(Messages.DbManager_backing_up_cat);
											performBackup(0L, -1L, true);
										}

										db.close();
										db = null;
										monitor.subTask(Messages.DbManager_defragmenting_cat);

										DefragmentConfig config = new DefragmentConfig(fileName, backupPath);
										boolean fileNeedsUpgrade = config.fileNeedsUpgrade();
										EmbeddedConfiguration ef = createDatabaseConfiguration(fileNeedsUpgrade,
												fileName, defragSpaceSize);
										try {
											SystemInfo systemInfo = getSystemInfo();
											long totalSize = systemInfo.totalSize();
											defragment(fileName, backupPath, ef);
											ef = createDatabaseConfiguration(false, fileName, -1L);
											db = Db4oEmbedded.openFile(ef, fileName);
											systemInfo = getSystemInfo();
											long newFreespaceSize = systemInfo.freespaceSize();
											long newTotalSize = systemInfo.totalSize();
											db.close();
											db = null;
											if (newFreespaceSize > 3 * reserve || newTotalSize > totalSize) {
												monitor.subTask(Messages.DbManager_defrag_cat_2);
												long newOccupiedspaceSize = newTotalSize - newFreespaceSize;
												long newReserve = isReadOnly() ? newOccupiedspaceSize / 100
														: newOccupiedspaceSize / 20;
												long newDefragSpaceSize = newOccupiedspaceSize + newReserve;
												ef = createDatabaseConfiguration(fileNeedsUpgrade, fileName,
														newDefragSpaceSize);
												defragment(fileName, backupPath, ef);
											}
										} catch (Exception e) {
											File target = new File(fileName);
											target.delete();
											new File(backupPath).renameTo(target);
											throw new InvocationTargetException(e);
										}
										if (fileNeedsUpgrade) {
											monitor.subTask(Messages.DbManager_updating_database_version);
											db = Db4oEmbedded.openFile(ef, fileName);
											db.close();
											db = null;
											DbActivator.getDefault().logInfo(
													NLS.bind(Messages.DbManager_database_converted_to_version_n,
															Db4o.version()));
										}
									} finally {
										if (db == null)
											db = createDatabase(fileName, false);
									}
								}
							});
						} catch (InvocationTargetException e1) {
							factory.getErrorHandler().showError(Messages.DbManager_Defrag_error,
									NLS.bind(Messages.DbManager_Defrag_failed, e1.getCause()), this);
							DbActivator.getDefault().logError(Messages.DbManager_Defrag_error, e1);
							return;
						} catch (InterruptedException e1) {
							// should never happen
						} finally {
							if (!file.exists()) {
								if (backup.exists())
									backup.renameTo(file);
								DbActivator.getDefault().logError(Messages.DbManager_defragmentation_failed, null);
								return;
							}
						}
						backup.delete();
						DbActivator.getDefault().logInfo(Messages.DbManager_defragmentation_successfiul);
					}
				}
			} catch (Throwable e) {
				DbActivator.getDefault().logError(Messages.DbManager_error_checking_sanity, e);
			}
	}

	protected void defragment(String fName, String backupPath, EmbeddedConfiguration config) {
		if (db != null) {
			db.close();
			db = null;
		}
		File backupFile = new File(backupPath);
		File newFile = new File(fName);
		if (newFile.exists()) {
			backupFile.delete();
			newFile.renameTo(backupFile);
		}
		EmbeddedConfiguration sourceConfig = createDatabaseConfiguration(false, backupPath, -1);
		final ObjectContainer sourceContainer = Db4oEmbedded.openFile(sourceConfig, backupPath);
		final ObjectContainer targetContainer = Db4oEmbedded.openFile(config, fName);
		try {
			StoredClass[] storedClasses = sourceContainer.ext().storedClasses();
			Map<String, Class<?>> typeMap = new HashMap<>();
			for (StoredClass sc : storedClasses) {
				Class<?> c = null;
				try {
					c = Class.forName(sc.getName());
				} catch (ClassNotFoundException ex) {
					// ignore entries from unknown packages
					// media extension are covered via MediaExtensionImpl
				}
				if (c != null) {
					String simpleName = c.getSimpleName();
					if (simpleName.endsWith(TYPEIMPL)) {
						String shortName = simpleName.substring(0, simpleName.length() - TYPEIMPL.length());
						if (!typeMap.containsKey(shortName))
							typeMap.put(shortName, c);
					} else if (simpleName.endsWith(IMPL))
						typeMap.put(simpleName.substring(0, simpleName.length() - IMPL.length()), c);
				}
			}
			int i = 0;
			for (Class<?> clazz : typeMap.values()) {
				for (Object object : sourceContainer.query(clazz)) {
					if (object instanceof IIdentifiableObject) {
						Query query = targetContainer.query();
						query.constrain(clazz);
						query.descend(Constants.OID).constrain(((IIdentifiableObject) object).getStringId());
						if (query.<IIdentifiableObject>execute().hasNext())
							continue;
					}
					targetContainer.store(object);
					if (++i >= 500) {
						targetContainer.commit();
						i = 0;
					}
				}
			}
			targetContainer.commit();
		} finally {
			sourceContainer.close();
			targetContainer.close();
		}
	}

	public boolean needsDefragmentation() {
		SystemInfo systemInfo = getSystemInfo();
		long totalSize = systemInfo.totalSize();
		return totalSize > 5000000 && systemInfo.freespaceSize() * 10 > totalSize
				|| systemInfo.freespaceEntryCount() > 10000;
	}

	public void pruneEmptySystemCollections() {
		if (!readOnly && hasDirtyCollection()) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					pruneEmptySystemCollections(monitor, false);
				}
			};
			try {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(workbenchWindow.getShell());
					dialog.create();
					dialog.getShell().setText(Constants.APPLICATION_NAME);
					dialog.run(false, true, runnable);
				} else
					runnable.run(new NullProgressMonitor());
			} catch (InvocationTargetException | InterruptedException e) {
				// ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#markSystemCollectionsForPurge(com.bdaum
	 * .zoom.cat.model.asset.Asset)
	 */

	public void markSystemCollectionsForPurge(Asset asset) {
		Meta meta = getMeta(true);
		boolean cleaned = meta.getCleaned();
		if (!cleaned || beginSafeTransactionWithReport())
			try {
				boolean notEmpty = hasDirtyCollection();
				boolean added = false;
				String uri = asset.getUri();
				StringBuilder sb = new StringBuilder();
				int p = uri.lastIndexOf('/', uri.length() - 2);
				while (p >= 0) {
					uri = uri.substring(0, p + 1);
					int from = uri.length() - 2;
					p = uri.lastIndexOf('/', from);
					if (p < 0)
						break;
					sb.setLength(0);
					if (uri.lastIndexOf(':', from) > p) {
						String volume = asset.getVolume();
						if (volume != null && !volume.isEmpty()) {
							added |= addDirtyCollection(sb.append(VOLUMEKEY).append(volume).toString());
							sb.setLength(0);
						}
					}
					added |= addDirtyCollection(sb.append(URIKEY).append(uri).toString());
				}
				Date dateCreated = asset.getDateCreated();
				if (dateCreated == null)
					dateCreated = asset.getDateTimeOriginal();
				if (dateCreated != null) {
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(dateCreated);
					StringBuilder ksb = new StringBuilder(DATETIMEKEY);
					added |= addDirtyCollection(ksb.append(String.valueOf(cal.get(Calendar.YEAR))).toString());
					int l = ksb.length();
					added |= addDirtyCollection(
							ksb.append('-').append(String.valueOf(cal.get(Calendar.MONTH))).toString())
							| addDirtyCollection(
									ksb.append('-').append(String.valueOf(Calendar.DAY_OF_MONTH)).toString());
					ksb.setLength(l);
					added |= addDirtyCollection(
							ksb.append("-W").append(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR))).toString()) //$NON-NLS-1$
							| addDirtyCollection(
									ksb.append('-').append(String.valueOf(Calendar.DAY_OF_WEEK)).toString());
				}
				LocationCreatedImpl rel = obtainById(LocationCreatedImpl.class, asset.getLocationCreated_parent());
				LocationImpl loc = obtainById(LocationImpl.class, rel == null ? null : rel.getLocation());
				if (loc != null)
					added |= markSystemCollectionsForPurge(loc);
				Date importDate = asset.getImportDate();
				if (importDate != null)
					added |= addDirtyCollection(IMPORTKEY + Format.DATE_TIME_HYPHEN_FORMAT.get().format(importDate));
				if (added && !notEmpty && cleaned) {
					meta.setCleaned(false);
					storeAndCommit(meta);
				}
			} catch (Exception e) {
				rollback();
				DbActivator.getDefault().logError(Messages.DbManager_internal_error_cleaning, e);
			} finally {
				if (cleaned)
					endSafeTransaction();
			}
	}

	public boolean markSystemCollectionsForPurge(Location loc) {
		if (loc != null) {
			StringBuilder ksb = new StringBuilder(LOCATIONKEY);
			return addDirtyCollection(ksb.append(loc.getWorldRegionCode()).toString())
					| addDirtyCollection(ksb.append('|').append(loc.getCountryISOCode()).toString())
					| addDirtyCollection(ksb.append('|').append(loc.getProvinceOrState()).toString())
					| addDirtyCollection(ksb.append('|').append(loc.getCity()).toString());
		}
		return false;
	}

	public boolean pruneSystemCollection(final SmartCollection sm) {
		if (!readOnly && (sm.getCriterion() == null || sm.getCriterion().isEmpty() || sm.getCriterion().get(0) == null
				|| createCollectionProcessor(sm, null, null).isEmpty()) && beginSafeTransactionWithReport())
			try {
				SmartCollection parent = sm.getSmartCollection_subSelection_parent();
				if (parent != null) {
					parent.removeSubSelection(sm);
					store(parent);
				} else {
					GroupImpl group = obtainById(GroupImpl.class, sm.getGroup_rootCollection_parent());
					if (group != null) {
						group.removeRootCollection(sm.getStringId());
						store(group);
					}
				}
				deleteCollection(sm);
				commit();
				return true;
			} catch (Exception e) {
				rollback();
				DbActivator.getDefault().logError(Messages.DbManager_internal_error_pruning, e);
			} finally {
				endSafeTransaction();
			}
		return false;
	}

	public void pruneEmptySystemCollections(IProgressMonitor monitor, boolean all) {
		synchronized (dirtyCollections) {
			// long time = System.currentTimeMillis();
			if (!readOnly) {
				if (all)
					for (SmartCollectionImpl sm : obtainObjects(SmartCollectionImpl.class))
						if (sm.getSystem()) {
							String id = sm.getStringId();
							if (id.startsWith(VOLUMEKEY) || id.startsWith(URIKEY) || id.startsWith(DATETIMEKEY)
									|| id.startsWith(LOCATIONKEY) || id.startsWith(IMPORTKEY))
								dirtyCollections.add(id);
						}
				if (hasDirtyCollection()) {
					Group importGroup = getImportGroup();
					List<Group> subgroups = importGroup.getSubgroup();
					if (!subgroups.isEmpty())
						importGroup = subgroups.get(0);
					List<String> importIds = new ArrayList<String>(importGroup.getRootCollection());
					monitor.beginTask(Messages.DbManager_pruning_empty, dirtyCollections.size() + 2 + importIds.size());
					LinkedList<String> sorted = new LinkedList<String>(dirtyCollections);
					Collections.sort(sorted, new Comparator<String>() {
						public int compare(String o1, String o2) {
							return o2.length() - o1.length();
						}
					});
					monitor.worked(1);
					if (db != null) {
						int maxImports = factory.getMaxImports() - 1;
						while (!sorted.isEmpty()) {
							String id = sorted.poll();
							monitor.worked(1);
							if (id.startsWith(IDbManager.IMPORTKEY))
								continue;
							SmartCollectionImpl sm = obtainById(SmartCollectionImpl.class, id);
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// ignore
							}
							if (sm != null && !pruneSystemCollection(sm))
								for (Iterator<String> it = sorted.iterator(); it.hasNext();)
									if (id.startsWith(it.next())) {
										it.remove();
										monitor.worked(1);
									}
						}
						importIds.remove(Constants.LAST_IMPORT_ID);
						Collections.sort(importIds, new Comparator<String>() {
							@Override
							public int compare(String o1, String o2) {
								return o2.compareTo(o1);
							}
						});
						List<Object> toBeDeleted = new ArrayList<>(importIds.size());
						int i = 0;
						for (String id : importIds) {
							SmartCollectionImpl sm = obtainById(SmartCollectionImpl.class, id);
							if (sm != null && sm.getSubSelection().isEmpty()) {
								if (i < maxImports && !Utilities.isImportEmpty(this, sm))
									++i;
								else {
									importGroup.removeRootCollection(id);
									toBeDeleted.add(sm);
								}
							}
							monitor.worked(1);
						}
						if (!toBeDeleted.isEmpty())
							safeTransaction(toBeDeleted, importGroup);
						monitor.worked(1);
						Meta meta = getMeta(true);
						meta.setCleaned(true);
						safeTransaction(null, meta);
						dirtyCollections.clear();
					}
					monitor.done();
				}
				// System.out.println(System.currentTimeMillis() - time);
			}
		}
	}

	public ICollectionProcessor createCollectionProcessor(SmartCollection sm, IAssetFilter[] filters,
			SortCriterion customSort) {
		return new CollectionProcessor(factory, this, sm, filters, customSort);
	}

	public boolean addDirtyCollection(String id) {
		return dirtyCollections.add(id);
	}

	private boolean hasDirtyCollection() {
		return !dirtyCollections.isEmpty();
	}

	private boolean removeCrashedLuceneIndex() {
		if (isLuceneLocked())
			return resetLuceneIndex();
		return false;
	}

	public boolean resetLuceneIndex() {
		if (indexPath == null || !indexPath.exists())
			return true;
		boolean success = Core.deleteFileOrFolder(indexPath);
		if (success && !readOnly) {
			Meta meta = obtainMeta();
			if (meta != null) {
				Set<String> postponed = meta.getPostponed();
				if (postponed != null && !postponed.isEmpty()) {
					meta.clearPostponed();
					store(postponed);
					storeAndCommit(meta);
				}
			}
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long)
	 */
	public BackupJob performBackup(long delay, long interval) {
		return performBackup(delay, interval, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long, boolean)
	 */
	public BackupJob performBackup(long delay, long interval, boolean block) {
		return performBackup(delay, interval, block, Integer.MAX_VALUE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#performBackup(long, long, boolean,
	 * int)
	 */
	public BackupJob performBackup(long delay, long interval, boolean block, int generations) {
		Meta meta = getMeta(true);
		String backupLocation = meta.getBackupLocation();
		if (backupLocation != null && !backupLocation.isEmpty()) {
			if (checkAvailableSpace(backupLocation, getFile(), getIndexPath())) {
				Date date = meta.getLastBackup();
				Date lastSessionEnd = meta.getLastSessionEnd();
				long lastBackup = date.getTime();
				long lastSession = lastSessionEnd == null ? Long.MAX_VALUE : lastSessionEnd.getTime();
				long now = System.currentTimeMillis();
				if (interval < 0 || now - lastBackup > interval && lastSession > lastBackup) {
					date.setTime(now);
					meta.setLastBackupFolder(backupLocation);
					storeAndCommit(meta);
					BackupJob job = new BackupJob(backupLocation, getFile(), getIndexPath(), generations);
					job.schedule(delay);
					backupScheduled = true;
					if (block)
						try {
							job.join();
						} catch (InterruptedException e) {
							// should never happen
						}
					return job;
				}
			}
		}
		return null;
	}

	private boolean checkAvailableSpace(String backupLocation, File catFile, File indexPath) {
		File backupFile = new File(backupLocation);
		long freeSpace = CoreActivator.getDefault().getFreeSpace(backupFile);
		if (freeSpace > 0L
				&& catFile.length() + computeSpace(indexPath, 4096L) - computeSpace(backupFile, 0L) > freeSpace) {
			factory.getErrorHandler().showWarning(Constants.APPLICATION_NAME, Messages.DbManager_backup_impossible,
					null);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#store(java.lang.Object)
	 */

	private long computeSpace(File file, long foldersize) {
		if (file == null)
			return 0L;
		if (file.isDirectory()) {
			long filesize = foldersize;
			File[] children = file.listFiles();
			if (children != null)
				for (File child : children)
					filesize += computeSpace(child, foldersize);
			return filesize;
		}
		return file.length();
	}

	public void store(Object object) {
		if (!readOnly && !transactionFailed)
			try {
				db.store(object);
			} catch (Exception e) {
				transactionFailed = true;
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#delete(java.lang.Object)
	 */

	public void delete(Object object) {
		if (!readOnly && !transactionFailed && object != null)
			try {
				if (object instanceof SmartCollection) {
					deleteMember(((SmartCollection) object).getCriterion());
					deleteMember(((SmartCollection) object).getSortCriterion());
					deleteMember(((SmartCollection) object).getSubSelection());
					resetSystemCollectionCache();
				} else if (object instanceof SlideShow)
					deleteMember(((SlideShow) object).getEntry());
				else if (object instanceof Wall)
					deleteMember(((Wall) object).getExhibit());
				else if (object instanceof Storyboard)
					deleteMember(((Storyboard) object).getExhibit());
				db.delete(object);
				if (DIAGNOSE)
					DbActivator.getDefault().logInfo("Deleted " + object); //$NON-NLS-1$
			} catch (Exception e) {
				transactionFailed = true;
			}
	}

	private void resetSystemCollectionCache() {
		if (lastFolderUris != null)
			lastFolderUris.clear();
		if (previousLocationKeys != null)
			previousLocationKeys.clear();
		previousDay = -1L;
		previousTimeline = Meta_type.timeline_no;
	}

	private void deleteMember(Object object) {
		if (object != null)
			db.delete(object);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#storeTrash(java.lang.Object)
	 */

	public void storeTrash(Object object) {
		if (!trashFailed)
			try {
				getTrashCan().store(object);
			} catch (Exception e) {
				trashFailed = true;
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#deleteTrash(java.lang.Object)
	 */

	public void deleteTrash(Object object) {
		if (trashCan != null && !trashFailed)
			try {
				trashCan.delete(object);
			} catch (Exception e) {
				trashFailed = true;
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#deleteAllTrash(java.util.Collection)
	 */

	public void deleteAllTrash(Collection<? extends Object> t) {
		if (trashCan != null)
			try {
				for (Object o : t)
					trashCan.delete(o);
				trashCan.commit();
			} catch (Exception e) {
				trashCan.rollback();
				trashFailed = true;
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#commit()
	 */

	public void commit() {
		if (!readOnly) {
			if (transactionFailed)
				rollbackError();
			else
				try {
					db.commit();
				} catch (Exception e) {
					rollbackError();
				}
		}
	}

	private void rollbackError() {
		rollbackTrash();
		rollback();
		connectionLostWarning();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#rollback()
	 */

	public void rollback() {
		if (!readOnly)
			try {
				db.rollback();
				transactionFailed = false;
			} catch (Exception e) {
				// do nothing
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#commitTrash()
	 */

	public void commitTrash() {
		if (trashCan != null)
			if (trashFailed)
				rollbackError();
			else
				try {
					trashCan.commit();
				} catch (Exception e) {
					rollbackError();
				}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#rollbackTrash()
	 */

	public void rollbackTrash() {
		if (trashCan != null)
			try {
				trashCan.rollback();
				trashFailed = false;
			} catch (Exception e) {
				// do nothing
			}
	}

	private void connectionLostWarning() {
		if (!emergency)
			factory.getErrorHandler().connectionLostWarning(Messages.DbManager_lost_connection,
					Messages.DbManager_operation_was_aborted, this);
	}

	public void backup(String filename) {
		if (db != null)
			db.ext().backup(filename);
	}

	protected SystemInfo getSystemInfo() {
		return ((ExtObjectContainer) db).systemInfo();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#isReadOnly()
	 */

	public boolean isReadOnly() {
		return readOnly;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#setReadOnly(boolean)
	 */

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly |= !file.canWrite();
	}

	public void resetErrorCount() {
		dbErrorCounter = 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#beginSafeTransaction()
	 */

	public synchronized boolean beginSafeTransaction() {
		if (db != null)
			for (int i = 0; i < TIMEOUT; i += 90)
				try {
					if (db.ext().setSemaphore(SAFE_TRANSACTION, TIMEOUT))
						return true;
					Thread.sleep(100);
				} catch (DatabaseClosedException e1) {
					connectionLostWarning();
					return false;
				} catch (InterruptedException e) {
					// ignore
				}
		return false;
	}

	private boolean beginSafeTransactionWithReport() {
		if (beginSafeTransaction())
			return true;
		DbActivator.getDefault().logError(Messages.DbManager_time_out, null);
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#endSafeTransaction()
	 */

	public void endSafeTransaction() {
		if (db != null)
			try {
				db.ext().releaseSemaphore(SAFE_TRANSACTION);
			} catch (Exception e) {
				// ignore
			}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#safeTransaction(java.lang.Runnable)
	 */

	public boolean safeTransaction(Runnable runnable) {
		if (beginSafeTransactionWithReport())
			try {
				runnable.run();
				commit();
				return true;
			} catch (RuntimeException e) {
				rollback();
				throw e;
			} finally {
				endSafeTransaction();
			}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#safeTransaction(java.lang.Object,
	 * java.lang.Object)
	 */
	public boolean safeTransaction(Object toBeDeleted, Object toBeStored) {
		if (beginSafeTransactionWithReport())
			try {
				if (toBeDeleted != null) {
					if (toBeDeleted instanceof Collection<?>)
						for (Object o : (Collection<?>) toBeDeleted) {
							if (o != null)
								delete(o);
						}
					else
						delete(toBeDeleted);
				}
				if (toBeStored != null) {
					if (toBeStored instanceof Collection<?>)
						for (Object o : (Collection<?>) toBeStored) {
							if (o != null)
								store(o);
						}
					else
						store(toBeStored);
				}
				commit();
				return true;
			} catch (RuntimeException e) {
				rollback();
				throw e;
			} finally {
				endSafeTransaction();
			}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#isBackupScheduled()
	 */

	public boolean isBackupScheduled() {
		return backupScheduled;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#createCollectionProcessor(com.bdaum.zoom
	 * .cat.model.group.SmartCollection)
	 */

	public ICollectionProcessor createCollectionProcessor(SmartCollection sm) {
		return createCollectionProcessor(sm, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IDbManager#getStatistics()
	 */

	public Map<String, Long> getStatistics() {
		Map<String, Long> results = new HashMap<String, Long>(5);
		SystemInfo systemInfo = getSystemInfo();
		results.put(IDbManager.TOTALSIZE, systemInfo.totalSize());
		results.put(IDbManager.FREESPACE, systemInfo.freespaceSize());
		results.put(IDbManager.FREESPACEENTRIES, (long) systemInfo.freespaceEntryCount());
		return results;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbManager#repairCatalog()
	 */
	public void repairCatalog() {
		Set<Object> toBeStored = new HashSet<Object>();
		List<Object> toBeDeleted = new ArrayList<Object>();
		List<SmartCollectionImpl> collections = obtainObjects(SmartCollectionImpl.class);
		for (SmartCollectionImpl sm : collections) {
			AomList<Criterion> crits = sm.getCriterion();
			if (crits == null)
				removeCollection(toBeStored, toBeDeleted, sm);
			else {
				for (Iterator<Criterion> it = crits.iterator(); it.hasNext();)
					if (it.next() == null)
						it.remove();
				if (crits.isEmpty())
					removeCollection(toBeStored, toBeDeleted, sm);
			}
		}
		safeTransaction(toBeDeleted, toBeStored);
	}

	private void removeCollection(Set<Object> toBeStored, List<Object> toBeDeleted, SmartCollectionImpl sm) {
		SmartCollection parent = sm.getSmartCollection_subSelection_parent();
		if (parent != null) {
			parent.removeSubSelection(sm);
			toBeStored.add(parent);
		}
		String groupId = sm.getGroup_rootCollection_parent();
		if (groupId != null) {
			GroupImpl group = obtainById(GroupImpl.class, groupId);
			if (group != null) {
				group.removeRootCollection(sm.getStringId());
				toBeStored.add(group);
			}
		}
		toBeDeleted.add(sm);
	}

	protected boolean isIndexed(String field) {
		return indexedFields.contains(field);
	}

}
