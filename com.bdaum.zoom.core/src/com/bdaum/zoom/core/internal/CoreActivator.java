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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.aoModeling.runtime.UUIDgenerator;
import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.core.internal.db.CatalogConverter;
import com.bdaum.zoom.core.internal.db.NullDbManager;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.core.trash.HistoryItem;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageStore;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.internal.ImageCache;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class CoreActivator extends Plugin implements ICore, IAdaptable {

	public static final NullDbManager NULLDBMANAGER = new NullDbManager();

	private static final String LOCK = ".lock"; //$NON-NLS-1$

	private static final String SHOW = ".show"; //$NON-NLS-1$

	private static final String CAT_OPID = "$$cat$$"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.core"; //$NON-NLS-1$

	public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.bdaum.zoom.debug")); //$NON-NLS-1$

	// The shared instance
	private static CoreActivator plugin;

	private IDbManager dbManager = NULLDBMANAGER;

	private ListenerList<CatalogListener> listeners = new ListenerList<CatalogListener>();

	private VolumeManager volumeManager;

	private IWorkbenchOperationSupport operationSupport;

	private IOperationHistory operationHistory;

	private int backupInterval = 7;

	private AssetProvider assetProvider;

	private ImageCache imageCache;

	private File catFile;

	private Map<String, WatchedFolder> observedFolders = Collections
			.synchronizedMap(new HashMap<String, WatchedFolder>());

	private LinkedList<CatLocation> recentCats = new LinkedList<CatLocation>();

	private IDbFactory dbFactory;

	private ServiceReference<?> dbfactoryRef;

	private List<IRecipeDetector> activeRecipeProcessors;

	private FileWatchManager fileWatchManager;

	private boolean noProgress;

	private int backupGenerations;

	private ArrayList<ICatalogContributor> catalogContributors;

	private HashMap<String, IMediaSupport> mediaSupportMap;

	private FileNameExtensionFilter filenameExtensionFilter;

	private IMediaSupport[] mediaSupports;

	private boolean processRecipes;

	private com.bdaum.zoom.core.internal.Locker locker;

	private File showfile;

	private HighresImageLoader highresImageLoader;

	private ServiceReference<IGeoService> geoServiceRef;

	private ServiceReference<IAiService> aiServiceRef;

	private int aiUsers;

	private Map<String, String> mediaMimeMap;

	private Map<String, Theme> themes;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IdentifiableObject.setIdentifierGenerator(new UUIDgenerator());
		IPath stateLocation = getStateLocation();
		showfile = stateLocation.append(SHOW).toFile();
		IPath lockfile = stateLocation.append(LOCK);
		File file = lockfile.toFile();
		locker = new Locker(file);
		try {
			if (!locker.lock()) {
				try {
					showfile.createNewFile();
					String[] commandLineArgs = Platform.getApplicationArgs();
					if (commandLineArgs.length > 0) {
						BufferedWriter writer = null;
						try {
							writer = new BufferedWriter(new FileWriter(showfile));
							writer.write(Core.toStringList(commandLineArgs, "\n")); //$NON-NLS-1$
						} finally {
							try {
								if (writer != null)
									writer.close();
							} catch (Exception e) {
								// ignore
							}
						}
					}
				} catch (Exception e) {
					// ignore
				}
				plugin = null;
				System.exit(0);
			}
		} catch (IOException e) {
			// don't lock if we can't
		}
		logInfo(NLS.bind(Messages.CoreActivator_session_started, getBundle().getVersion().toString()));
		if (Constants.WIN32)
			ImageActivator.getDefault().deleteFileAfterShutdown(file);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			logInfo(Messages.CoreActivator_session_closed);
			if (fileWatchManager != null)
				fileWatchManager.dispose();
			if (dbfactoryRef != null)
				getBundle().getBundleContext().ungetService(dbfactoryRef);
			if (aiServiceRef != null)
				context.ungetService(aiServiceRef);
			if (geoServiceRef != null)
				context.ungetService(geoServiceRef);
			if (imageCache != null) {
				imageCache.dispose();
				imageCache = null;
			}
			if (volumeManager != null)
				volumeManager.dispose();
			dbManager.close(CatalogListener.SHUTDOWN);
		} finally {
			if (locker != null)
				locker.release();
			plugin = null;
			super.stop(context);
		}
	}

	public void deleteTrashCan() {
		if (dbManager.hasTrash()) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					List<Trash> set = dbManager.obtainTrashToDelete(true);
					monitor.beginTask(Messages.CoreActivator_Cleaning_up, set.size() + 1);
					for (Trash t : set) {
						t.deleteFiles();
						monitor.worked(1);
					}
					dbManager.closeTrash();
					monitor.done();
				}
			};
			try {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (workbenchWindow != null)
					new ProgressMonitorDialog(workbenchWindow.getShell()).run(false, true, runnable);
				else
					runnable.run(new NullProgressMonitor());
			} catch (InvocationTargetException e) {
				// ignore
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CoreActivator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logError(java.lang.String,
	 * java.lang.Throwable)
	 */

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logWarning(java.lang.String,
	 * java.lang.Exception)
	 */

	public void logWarning(String message, Throwable e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#logInfo(java.lang.String)
	 */

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public static void logDebug(String message, Object parm) {
		if (DEBUG)
			getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID,
					"Debug: " + (parm == null ? message : NLS.bind(message, parm)))); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#openDatabase(java.lang.String, boolean)
	 */

	public IDbManager openDatabase(final String fileName) {
		return openDatabase(fileName, false, null);
	}

	public IDbManager openDatabase(final String fileName, final boolean newDb, final Meta meta) {
		BusyIndicator.showWhile(null, () -> doOpenDatabase(fileName, newDb, meta));
		return dbManager;
	}

	private void doOpenDatabase(String fileName, boolean newDb, Meta oldMeta) {
		closeDatabase();
		File cfile = new File(fileName);
		getFileWatchManager().ignore(cfile, CAT_OPID);
		if (newDb || cfile.exists()) {
			List<Object> toBeStored = new ArrayList<Object>();
			List<Object> toBeDeleted = new ArrayList<Object>();
			IDbFactory factory = getDbFactory();
			dbManager = factory.createDbManager(fileName, newDb, false, true);
			Meta newMeta = dbManager.getMeta(true);
			toBeStored.add(newMeta);
			if (newDb && oldMeta != null)
				copyMeta(oldMeta, cfile, toBeStored, newMeta);
			else if (newDb) {
				String property = System.getProperty("com.bdaum.zoom.userFields"); //$NON-NLS-1$
				if (property != null && !property.isEmpty()) {
					int p = property.indexOf(',');
					if (p >= 0) {
						newMeta.setUserFieldLabel1(property.substring(0, p).trim());
						newMeta.setUserFieldLabel2(property.substring(p + 1).trim());
					} else
						newMeta.setUserFieldLabel1(property.trim());
				}
			}
			if (!toBeStored.isEmpty() || !toBeDeleted.isEmpty())
				dbManager.safeTransaction(toBeDeleted, toBeStored);
			if (newDb) {
				Utilities.initSystemCollections(dbManager);
				dbManager.storeAndCommit(newMeta);
				fireStructureModified();
			} else if (newMeta != null && !dbManager.isReadOnly()) {
				if (dbManager.isEmbedded())
					convertDatabase(newMeta);
				resumeIndexing(newMeta);
				for (IResumeHandler handler : getResumeHandlers())
					handler.resumeWork(newMeta);
			}
		}
	}

	public void copyMeta(Meta oldMeta, File cfile, List<Object> toBeStored, Meta newMeta) {
		Utilities.copyMeta(oldMeta, newMeta);
		Date creationDate = new Date();
		newMeta.setCreationDate(creationDate);
		newMeta.setLastImport(new Date(0L));
		newMeta.setCategory(Utilities.cloneCategories(oldMeta.getCategory()));
		newMeta.setReadonly(false);
		newMeta.setLastSequenceNo(0);
		newMeta.setLastYearSequenceNo(0);
		newMeta.setLastBackup(creationDate);
		newMeta.setLastBackupFolder(null);
		String backupLocation = oldMeta.getBackupLocation();
		if (backupLocation != null) {
			int p = backupLocation.lastIndexOf(BatchConstants.CATEXTENSION);
			if (p >= 0) {
				int q = backupLocation.lastIndexOf('/', p);
				if (q < 0)
					q = backupLocation.lastIndexOf('\\', p);
				if (q >= 0)
					backupLocation = backupLocation.substring(0, q + 1) + cfile.getName()
							+ backupLocation.substring(p + BatchConstants.CATEXTENSION.length());
				newMeta.setBackupLocation(backupLocation);
			}
		}
		newMeta.setLastSessionEnd(creationDate);
		newMeta.setLastSelection(null);
		newMeta.setLastExpansion(new ArrayList<String>(0));
		newMeta.setLastCollection(null);
		newMeta.setPauseFolderWatch(false);
		newMeta.setCleaned(false);
		newMeta.setPostponed(new ArrayList<String>(0));
		newMeta.setPostponedNaming(new ArrayList<String>(0));
		newMeta.setReadonly(false);
		newMeta.setPlatform(Platform.getOS());
		newMeta.setLastPicasaScan(null);
		newMeta.setPicasaScannerVersion(Constants.PICASASCANNERVERSION);
		Map<String, LastDeviceImport> lastDeviceImports = newMeta.getLastDeviceImport();
		if (lastDeviceImports != null)
			for (LastDeviceImport deviceImport : lastDeviceImports.values()) {
				deviceImport.setTimestamp(-1);
				toBeStored.add(deviceImport);
			}
	}

	public void closeDatabase() {
		IDbManager old = dbManager;
		dbManager = NULLDBMANAGER;
		File file = old.getFile();
		if (file != null)
			try {
				if (fileWatchManager != null)
					fileWatchManager.ignore(file, null);
				old.close(CatalogListener.NORMAL);
			} catch (Exception e) {
				logError(Messages.CoreActivator_error_closing_database, e);
			}
	}

	public Set<String> getCbirAlgorithms() {
		Set<String> cbirAlgorithms = dbManager.getMeta(true).getCbirAlgorithms();
		if (cbirAlgorithms != null && !cbirAlgorithms.isEmpty())
			return cbirAlgorithms;
		cbirAlgorithms = new HashSet<String>();
		for (Algorithm algorithm : dbFactory.getLireService(true).getSupportedSimilarityAlgorithms())
			if (!(algorithm.isAi()) && algorithm.isEssential())
				cbirAlgorithms.add(algorithm.getName());
		return cbirAlgorithms;
	}

	public Algorithm getDefaultCbirAlgorithm() {
		Set<String> cbirAlgorithms = getCbirAlgorithms();
		for (Algorithm algo : dbFactory.getLireService(true).getSupportedSimilarityAlgorithms())
			if (cbirAlgorithms.contains(algo.getName()))
				return algo;
		return null;
	}

	public Set<String> getIndexedTextFields() {
		return getIndexedTextFields(getDbManager().getMeta(true));
	}

	public Set<String> getIndexedTextFields(Meta meta) {
		Set<String> fields = meta.getIndexedTextFields();
		if (fields != null && !fields.isEmpty())
			return fields;
		fields = new HashSet<String>();
		for (QueryField qfield : QueryField.getQueryFields())
			if (qfield.isFullTextBase())
				fields.add(qfield.getId());
		fields.add(ILuceneService.INDEX_SLIDE_TITLE);
		fields.add(ILuceneService.INDEX_SLIDE_DESCR);
		fields.add(ILuceneService.INDEX_EXH_TITLE);
		fields.add(ILuceneService.INDEX_EXH_DESCR);
		fields.add(ILuceneService.INDEX_EXH_CREDITS);
		fields.add(ILuceneService.INDEX_WEBGAL_TITLE);
		fields.add(ILuceneService.INDEX_WEBGAL_DESCR);
		fields.add(ILuceneService.INDEX_WEBGAL_ALT);
		fields.add(ILuceneService.INDEX_PERSON_SHOWN);
		fields.add(ILuceneService.INDEX_FILENAME);
		return fields;
	}

	private IResumeHandler[] getResumeHandlers() {
		List<IResumeHandler> result = new ArrayList<IResumeHandler>(3);
		for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "resumeHandler") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : ext.getConfigurationElements())
				try {
					result.add((IResumeHandler) conf.createExecutableExtension("class")); //$NON-NLS-1$
				} catch (CoreException e) {
					logError(NLS.bind(Messages.CoreActivator_internal_error_handler_instantiation,
							conf.getAttribute("name")), e); //$NON-NLS-1$
				}
		return result.toArray(new IResumeHandler[result.size()]);
	}

	private void resumeIndexing(Meta meta) {
		int lireServiceVersion = dbFactory.getLireServiceVersion();
		if (lireServiceVersion < 0 || meta.getNoIndex()) {
			meta.clearPostponed();
			dbManager.store(meta.getPostponed());
			dbManager.storeAndCommit(meta);
			return;
		}
		// Check if index exists
		File indexPath = dbManager.getIndexPath();
		if (indexPath != null) {
			Set<String> postponed = meta.getPostponed();
			Job job = null;
			if (indexPath.exists()) {
				if (meta.getRelevantLireVersion() != lireServiceVersion) {
					meta.setRelevantLireVersion(lireServiceVersion);
					IDbErrorHandler errorHandler = getErrorHandler();
					if (errorHandler != null)
						errorHandler.showInformation(Constants.APPLICATION_NAME,
								NLS.bind(Messages.CoreActivator_wrong_index_version, indexPath), this);
					deleteOutdatedIndexBackups(meta);
					job = dbFactory.getLireService(true).createIndexingJob();
				} else if (postponed != null && !postponed.isEmpty())
					job = dbFactory.getLireService(true)
							.createIndexingJob(postponed.toArray(new String[postponed.size()]));

			} else {
				IDbErrorHandler errorHandler = getErrorHandler();
				if (errorHandler != null)
					errorHandler.showInformation(Constants.APPLICATION_NAME,
							NLS.bind(Messages.CoreActivator_index_file_does_not_exist, indexPath), this);
				String lastBackupFolder = meta.getLastBackupFolder();
				if (lastBackupFolder != null) {
					File indexBackup = new File(lastBackupFolder, indexPath.getName());
					if (indexBackup.isDirectory() && !new File(indexBackup, "write.lock") //$NON-NLS-1$
							.exists())
						job = dbFactory.getLireService(true).createIndexingJob(indexBackup, meta.getLastBackup());
				}
				if (job == null)
					job = dbFactory.getLireService(true).createIndexingJob();
			}
			if (job != null) {
				if (postponed != null && !postponed.isEmpty()) {
					meta.clearPostponed();
					dbManager.store(postponed);
					dbManager.storeAndCommit(meta);
				}
				job.schedule();
			}
		}
	}

	private void deleteOutdatedIndexBackups(Meta meta) {
		String backupLocation = meta.getBackupLocation();
		String[] result = Utilities.computeBackupLocation(catFile, backupLocation);
		backupLocation = result[0];
		String generationFolder = result[1];
		if (generationFolder != null) {
			final String generationPattern = result[2];
			File gFolder = new File(generationFolder);
			if (gFolder.exists()) {
				File[] children = gFolder.listFiles(new FileFilter() {
					public boolean accept(File child) {
						return (child.isDirectory() && child.getName().matches(generationPattern));
					}
				});
				if (children != null) {
					File indexPath = dbManager.getIndexPath();
					if (indexPath != null) {
						String indexFolderName = indexPath.getName();
						for (File folder : children) {
							File indexFolder = new File(folder, indexFolderName);
							if (indexFolder.exists())
								BatchUtilities.deleteFileOrFolder(indexFolder);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	public IDbFactory getDbFactory() {
		if (dbFactory == null) {
			BundleContext bundleContext = getBundle().getBundleContext();
			dbfactoryRef = bundleContext.getServiceReference(IDbFactory.class.getName());
			if (dbfactoryRef != null)
				dbFactory = (IDbFactory) bundleContext.getService(dbfactoryRef);
		}
		return dbFactory;
	}

	public IDbErrorHandler getErrorHandler() {
		IDbFactory factory = getDbFactory();
		return factory == null ? null : factory.getErrorHandler();
	}

	public IMediaSupport getMediaSupport(String format) {
		return getMediaSupportMap().get(format);
	}

	public Set<String> getMediaFormats() {
		return getMediaMimeMap().keySet();
	}

	public Map<String, String> getMediaMimeMap() {
		if (mediaMimeMap == null) {
			mediaMimeMap = new HashMap<String, String>(30);
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"mediaSupport"); //$NON-NLS-1$
			for (IExtension extension : extensionPoint.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements()) {
					StringTokenizer st = new StringTokenizer(conf.getAttribute("formats")); //$NON-NLS-1$
					StringTokenizer stm = new StringTokenizer(conf.getAttribute("mimetypes")); //$NON-NLS-1$
					while (st.hasMoreTokens() && stm.hasMoreTokens())
						mediaMimeMap.put(st.nextToken(), stm.nextToken());
				}
		}
		return mediaMimeMap;
	}

	public Map<String, IMediaSupport> getMediaSupportMap() {
		if (mediaSupportMap == null) {
			mediaSupportMap = new HashMap<String, IMediaSupport>(5);
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"mediaSupport"); //$NON-NLS-1$
			for (IExtension extension : extensionPoint.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements()) {
					String name = conf.getAttribute("name"); //$NON-NLS-1$
					String plural = conf.getAttribute("plural"); //$NON-NLS-1$
					String collectionID = conf.getAttribute("collectionID"); //$NON-NLS-1$
					try {
						IMediaSupport mediaSupport = (IMediaSupport) conf.createExecutableExtension("class"); //$NON-NLS-1$
						mediaSupport.setName(name);
						mediaSupport.setPlural(plural);
						mediaSupport.setCollectionId(collectionID);
						Map<String, String> mimeMap = new HashMap<String, String>(30);
						StringTokenizer st = new StringTokenizer(conf.getAttribute("formats")); //$NON-NLS-1$
						StringTokenizer stm = new StringTokenizer(conf.getAttribute("mimetypes")); //$NON-NLS-1$
						while (st.hasMoreTokens() && stm.hasMoreTokens()) {
							String ext = st.nextToken();
							mimeMap.put(ext, stm.nextToken());
							mediaSupportMap.put(ext, mediaSupport);
						}
						mediaSupport.setMimeMap(mimeMap);

					} catch (CoreException e) {
						logError(NLS.bind(Messages.CoreActivator_internal_error_instantiating_media_support, name), e);
					}
				}
		}
		return mediaSupportMap;
	}

	private void convertDatabase(final Meta meta) {
		final IDbManager db = getDbManager();
		int catVersion = meta.getVersion();
		int supportedVersion = db.getVersion();
		if (catVersion > supportedVersion)
			Core.getCore().getDbFactory().getErrorHandler().fatalError(Messages.CoreActivator_unsupported_version,
					NLS.bind(Messages.CoreActivator_use_newer_version, Constants.APPLICATION_NAME), (IAdaptable) db);
		else if (catVersion < supportedVersion) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(Messages.CoreActivator_converting_cat, IProgressMonitor.UNKNOWN);
					monitor.subTask(Messages.CoreActivator_backing_up_cat);
					db.performBackup(0L, -1L, true);
					monitor.subTask(Messages.CoreActivator_converting_to_new_version);
					CatalogConverter.convert(db, monitor);
					logInfo(NLS.bind(Messages.CoreActivator_catalog_converted, db.getVersion()));
					monitor.done();
				}
			};
			Display current = Display.getCurrent();
			Shell shell = (current != null) ? current.getActiveShell() : null;
			if (shell != null) {
				try {
					new ProgressMonitorDialog(shell).run(false, false, runnable);
				} catch (InvocationTargetException e) {
					logError(NLS.bind(Messages.CoreActivator_error_when_updating_to_version_n, db.getVersion()), e);
				} catch (InterruptedException e) {
					// should never happen
				}
			} else
				try {
					runnable.run(new NullProgressMonitor());
				} catch (InvocationTargetException e) {
					logError(NLS.bind(Messages.CoreActivator_error_when_updating_to_version_n, db.getVersion()), e);
				} catch (InterruptedException e) {
					// should never happen
				}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#getDbManager()
	 */

	public IDbManager getDbManager() {
		return dbManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#getImageCache()
	 */

	public ImageStore getImageCache() {
		if (imageCache == null)
			imageCache = new ImageCache(new AssetImageProvider(), 257);
		return imageCache;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#getAssetProvider()
	 */

	public AssetProvider getAssetProvider() {
		if (assetProvider == null)
			assetProvider = new AssetProvider(dbManager);
		return assetProvider;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#addCatalogListener(com.bdaum.zoom.core.
	 * CatalogListener)
	 */

	public void addCatalogListener(CatalogListener l) {
		listeners.add(l);
		if (dbManager != NULLDBMANAGER) {
			l.catalogOpened(false);
			l.assetsModified(null, null);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#removeCatalogListener(com.bdaum.zoom.core.
	 * CatalogListener)
	 */

	public void removeCatalogListener(CatalogListener l) {
		listeners.remove(l);
	}

	public void fireCatalogClosed(int mode) {
		if (dbManager.getFile() != null) {
			if (mode != CatalogListener.EMERGENCY) {
				for (Object listener : listeners.getListeners())
					((CatalogListener) listener).catalogClosed(mode);
				resetInfrastructure();
				System.setProperty(Constants.PROP_CATACCESS, Constants.PROP_CATACCESS_NONE);
			}
			catFile = dbManager.getFile();
			dbManager.close(mode);
			dbManager = NULLDBMANAGER;
		}
		if (catFile != null && mode != CatalogListener.EMERGENCY) {
			if (fileWatchManager != null)
				fileWatchManager.ignore(catFile, null);
			addToRecentCats(catFile);
		}
	}

	private void addToRecentCats(File f) {
		CatLocation loc = new CatLocation(f);
		recentCats.remove(loc);
		if (recentCats.size() > 3)
			recentCats.remove(recentCats.size() - 1);
		recentCats.add(0, loc);
	}

	public void fireCatalogOpened(boolean newDb) {
		System.setProperty(Constants.PROP_CATACCESS,
				dbManager.isReadOnly() ? Constants.PROP_CATACCESS_READ : Constants.PROP_CATACCESS_WRITE);
		resetInfrastructure();
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).catalogOpened(newDb);
		fireAssetsModified(null, null);
	}

	private void resetInfrastructure() {
		assetProvider = null;
		if (imageCache != null) {
			imageCache.dispose();
			imageCache = null;
		}
	}

	public void fireApplyRules(Collection<? extends Asset> assets, QueryField node) {
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).applyRules(assets, node);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#fireAssetsModified(java.util.List,
	 * com.bdaum.zoom.core.QueryField)
	 */

	public void fireAssetsModified(BagChange<Asset> changes, QueryField node) {
		if (resetInfrastructure(assetProvider, changes == null ? null : changes.getChanged(), node))
			for (Object listener : listeners.getListeners())
				((CatalogListener) listener).assetsModified(null, null);
		else
			for (Object listener : listeners.getListeners())
				((CatalogListener) listener).assetsModified(changes, node);
	}

	public boolean resetInfrastructure(IAssetProvider provider, Collection<? extends Asset> assets, Object node) {
		if (provider != null) {
			if (node instanceof QueryField && provider.invalidate((QueryField) node))
				// forced redraw
				return true;
			if (node == null && imageCache != null)
				// image has changed
				imageCache.invalidateImages(assets);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#fireStructureModified()
	 */

	public void fireStructureModified() {
		if (assetProvider != null)
			assetProvider.resetProcessor();
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).structureModified();
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.ICore#fireCatalogSelection(org.eclipse.jface.viewers
	 * .IStructuredSelection)
	 */
	public void fireCatalogSelection(IStructuredSelection selection, boolean forceUpdate) {
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).setCatalogSelection(selection, forceUpdate);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#fireHierarchyModified()
	 */

	public void fireHierarchyModified() {
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).hierarchyModified();
	}

	public void fireBookmarksModified() {
		for (Object listener : listeners.getListeners())
			((CatalogListener) listener).bookmarksModified();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.ICore#performOperation(org.eclipse.core.commands.
	 * operations.IUndoableOperation, org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus performOperation(IUndoableOperation op, IProgressMonitor monitor, IAdaptable info) {
		if (operationHistory == null) {
			operationSupport = PlatformUI.getWorkbench().getOperationSupport();
			operationHistory = operationSupport.getOperationHistory();
			operationHistory.addOperationHistoryListener(new IOperationHistoryListener() {
				public void historyNotification(OperationHistoryEvent event) {
					if (event.getEventType() == OperationHistoryEvent.OPERATION_REMOVED
							&& event.getOperation() instanceof HistoryItem) {
						getDbManager().deleteAllTrash(
								dbManager.getTrash(HistoryItem.class, ((HistoryItem) event.getOperation()).getOpId()));
					}
				}
			});
		}
		op.addContext(operationSupport.getUndoContext());
		try {
			return operationHistory.execute(op, monitor, info);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, PLUGIN_ID,
					NLS.bind(Messages.CoreActivator_Cannot_execute_operation, op.getLabel()), e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICore#getVolumeManager()
	 */

	public IVolumeManager getVolumeManager() {
		if (volumeManager == null)
			volumeManager = new VolumeManager();
		return volumeManager;
	}

	public void putObservedFolder(WatchedFolder folder) {
		observedFolders.put(folder.getStringId(), folder);
	}

	public void removeObservedFolder(WatchedFolder folder) {
		observedFolders.remove(folder.getStringId());
	}

	public WatchedFolder getObservedFolder(String id) {
		WatchedFolder observedFolder = doGetObservedFolder(id);
		if (observedFolder == null) {
			WatchedFolder obj = dbManager.obtainById(WatchedFolderImpl.class, id);
			if (obj != null)
				putObservedFolder(observedFolder = obj);
		}
		return observedFolder;
	}

	private WatchedFolder doGetObservedFolder(String id) {
		return observedFolders.get(id);
	}

	public void purgeObsoleteWatchedFolderEntries() {
		List<Object> toBeDeleted = new ArrayList<Object>();
		for (WatchedFolderImpl wf : dbManager.obtainObjects(WatchedFolderImpl.class))
			if (!observedFolders.containsKey(wf.getStringId()))
				toBeDeleted.add(wf);
		dbManager.safeTransaction(toBeDeleted, null);
	}

	public WatchedFolder getObservedSubfolder(WatchedFolder observedFolder, File subFolder) {
		if (!observedFolder.getRecursive())
			return null;
		String volume = getVolumeManager().getVolumeForFile(subFolder);
		String id = Utilities.computeWatchedFolderId(subFolder, volume);
		WatchedFolderImpl observedMember = (WatchedFolderImpl) getObservedFolder(id);
		if (observedMember == null) {
			observedMember = new WatchedFolderImpl(subFolder.toURI().toString(), volume, 0L, true,
					observedFolder.getTransfer() ? null : getFileWatchManager().getDefaultFilters(),
					observedFolder.getTransfer(), observedFolder.getArtist(), observedFolder.getSkipDuplicates(),
					observedFolder.getSkipPolicy(), observedFolder.getTargetDir(), observedFolder.getSubfolderPolicy(),
					observedFolder.getSelectedTemplate(), observedFolder.getCue(), observedFolder.getFileSource());
			observedMember.setStringId(id);
			putObservedFolder(observedMember);
		}
		return observedMember;
	}

	public void setBackupInterval(int backupInterval) {
		this.backupInterval = backupInterval;
	}

	public void setCatFile(File catFile) {
		this.catFile = catFile;
	}

	public File getCatFile() {
		return catFile;
	}

	public void saveFolderStates() {
		dbManager.safeTransaction(null, observedFolders.values());
	}

	public int getBackupInterval() {
		return backupInterval;
	}

	public LinkedList<CatLocation> getRecentCats() {
		return recentCats;
	}

	public void setRecentCats(LinkedList<CatLocation> recentCats) {
		this.recentCats = recentCats;
	}

	public boolean isNetworked() {
		return getPeerService() != null;
	}

	public IPeerService getPeerService() {
		return getDbFactory().getPeerService();
	}

	public IAiService getAiService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		if (aiServiceRef == null)
			aiUsers = 0;
		if (aiUsers > 0) {
			++aiUsers;
			return bundleContext.getService(aiServiceRef);
		}
		aiServiceRef = bundleContext.getServiceReference(IAiService.class);
		if (aiServiceRef != null) {
			++aiUsers;
			return bundleContext.getService(aiServiceRef);
		}
		return null;
	}

	public void ungetAiService(IAiService service, String providerId) {
		--aiUsers;
		if (aiUsers <= 0 && aiServiceRef != null) {
			service.dispose(providerId);
			BundleContext bundleContext = getBundle().getBundleContext();
			bundleContext.ungetService(aiServiceRef);
			aiServiceRef = null;
		}
	}

	public List<IRecipeDetector> getRecipeDetectors() {
		List<IRecipeDetector> recipeProcessors = new ArrayList<IRecipeDetector>();
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "recipeDetector") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				String name = conf.getAttribute("name"); //$NON-NLS-1$
				try {
					IRecipeDetector processor = (IRecipeDetector) conf.createExecutableExtension("class"); //$NON-NLS-1$
					processor.setName(name);
					processor.setId(conf.getAttribute("id")); //$NON-NLS-1$
					for (IConfigurationElement parameter : conf.getChildren()) {
						IRecipeParameter parm = new AbstractRecipeDetector.RecipeParameter(
								parameter.getAttribute("name"), //$NON-NLS-1$
								parameter.getAttribute("id"), parameter.getAttribute("default")); //$NON-NLS-1$ //$NON-NLS-2$
						processor.addParameter(parm);
						for (IConfigurationElement value : parameter.getChildren())
							parm.addValueDescriptor(new AbstractRecipeDetector.RecipeParameter.RecipeParameterValue(
									value.getAttribute("label"), //$NON-NLS-1$
									value.getAttribute("id"))); //$NON-NLS-1$
					}
					recipeProcessors.add(processor);
				} catch (CoreException e) {
					logError(NLS.bind(Messages.CoreActivator_cannot_create_recipe_processor, name), e);
				}
			}
		return recipeProcessors;
	}

	public List<IRecipeDetector> getActiveRecipeDetectors() {
		return activeRecipeProcessors;
	}

	public void configureActiveRecipeDetectors(String[] detectorIds, boolean processRecipes) {
		this.processRecipes = processRecipes;
		List<IRecipeDetector> recipeDetectors = getRecipeDetectors();
		if (detectorIds != null) {
			activeRecipeProcessors = new ArrayList<IRecipeDetector>(detectorIds.length);
			for (String id : detectorIds)
				for (IRecipeDetector detector : recipeDetectors)
					if (id.equals(detector.getId()))
						activeRecipeProcessors.add(detector);
		} else
			activeRecipeProcessors = null;
	}

	public List<IRecipeDetector> getDetectors(String[] detectorIds) {
		if (detectorIds == null)
			return activeRecipeProcessors;
		if (!processRecipes)
			return null;
		List<IRecipeDetector> detectors = new ArrayList<IRecipeDetector>(detectorIds.length);
		for (IRecipeDetector detector : getRecipeDetectors())
			for (String id : detectorIds)
				if (id.equals(detector.getId())) {
					detectors.add(detector);
					break;
				}
		return detectors;
	}

	public FileWatchManager getFileWatchManager() {
		if (fileWatchManager == null)
			fileWatchManager = new FileWatchManager();
		return fileWatchManager;
	}

	public boolean containsRawImage(List<Asset> assets, boolean includeDng) {
		for (Asset asset : assets)
			if (ImageConstants.isRaw(asset.getUri(), includeDng))
				return true;
		return false;
	}

	public void setNoProgress(boolean noProgress) {
		this.noProgress = noProgress;
	}

	/**
	 * @return the noProgress
	 */
	public boolean isNoProgress() {
		return noProgress;
	}

	public void setBackupGenerations(int backupGenerations) {
		this.backupGenerations = backupGenerations;
	}

	/**
	 * @return the backUpGenerations
	 */
	public int getBackupGenerations() {
		return backupGenerations;
	}

	/**
	 * @return registered catalog contributors
	 */
	public Collection<ICatalogContributor> getCatalogContributors() {
		if (catalogContributors == null) {
			catalogContributors = new ArrayList<ICatalogContributor>(3);
			for (IExtension extension : Platform.getExtensionRegistry()
					.getExtensionPoint(PLUGIN_ID, "catalogContributor").getExtensions()) //$NON-NLS-1$
				for (IConfigurationElement conf : extension.getConfigurationElements())
					try {
						catalogContributors.add((ICatalogContributor) conf.createExecutableExtension("class")); //$NON-NLS-1$
					} catch (CoreException e) {
						logError(NLS.bind(Messages.CoreActivator_error_instantiating_catalog_contributor,
								conf.getAttribute("name")), e); //$NON-NLS-1$
					}
		}
		return catalogContributors;
	}

	/**
	 * Stops all indexing jobs and recreates the index from scratch
	 */
	public void recreateIndex() {
		Job.getJobManager().cancel(Constants.INDEXING);
		Core.waitOnJobCanceled(Constants.INDEXING);
		dbFactory.getLuceneService().releaseAllIndexReadersAndWriters();
		Job job = dbFactory.getLireService(true).createIndexingJob();
		if (job != null)
			job.schedule();
	}

	public FileNameExtensionFilter getFilenameExtensionFilter() {
		if (filenameExtensionFilter == null) {
			String[] imageExtensions = ImageConstants.getSupportedImageFileExtensionsGroups(true);
			Set<String> mediaFormats = getMediaFormats();
			String[] extensions = new String[imageExtensions.length + mediaFormats.size()];
			System.arraycopy(imageExtensions, 0, extensions, 0, imageExtensions.length);
			int i = imageExtensions.length;
			for (String f : mediaFormats)
				extensions[i++] = f;
			filenameExtensionFilter = new FileNameExtensionFilter(extensions, true);
		}
		return filenameExtensionFilter;
	}

	public IMediaSupport[] getMediaSupport() {
		if (mediaSupports == null) {
			Set<IMediaSupport> values = new HashSet<IMediaSupport>(getMediaSupportMap().values());
			mediaSupports = values.toArray(new IMediaSupport[values.size()]);
			Arrays.sort(mediaSupports, new Comparator<IMediaSupport>() {
				public int compare(IMediaSupport o1, IMediaSupport o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		return mediaSupports;
	}

	public String[] testOnShow() {
		if (showfile.exists()) {
			List<String> parms = new ArrayList<String>();
			try (BufferedReader reader = new BufferedReader(new FileReader(showfile))) {
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					parms.add(line);
				}
			} catch (IOException e) {
				// do nothing
			}
			showfile.delete();
			return parms.toArray(new String[parms.size()]);
		}
		return null;
	}

	public long getFreeSpace(File targetFile) {
		return getFileWatchManager().getFreeSpace(getVolumeManager().getRootFile(targetFile));
	}

	public void classifyFile(File member, List<File> newFiles, List<File> outdatedFiles, Map<String, File> xmpMap,
			List<IRecipeDetector> activeRecipeDetectors) {
		long filedate = member.lastModified();
		if (filedate == 0L)
			return;
		URI uri = member.toURI();
		List<AssetImpl> assets = dbManager.obtainAssetsForFile(uri);
		Iterator<AssetImpl> ait = assets.iterator();
		if (!ait.hasNext()) {
			List<Trash> t = dbManager.obtainTrashForFile(uri);
			if (t == null || t.isEmpty()) {
				List<Ghost_typeImpl> ghosts = dbManager.obtainGhostsForFile(uri);
				if (ghosts == null || ghosts.isEmpty())
					newFiles.add(member);
			}
			return;
		}
		long xmpdate = Long.MIN_VALUE;
		if (xmpMap != null) {
			String filename = member.getName();
			int p = filename.lastIndexOf('.');
			if (p >= 0)
				filename = filename.substring(0, p);
			File sidecar = xmpMap.get(filename);
			if (sidecar != null)
				xmpdate = sidecar.lastModified();
		}
		long lastmod = -1;
		AssetImpl asset = null;
		if (ait.hasNext()) {
			asset = ait.next();
			Date lastModification = asset.getLastModification();
			if (lastModification != null)
				lastmod = lastModification.getTime();
		}
		if (lastmod < filedate) {
			outdatedFiles.add(member);
			return;
		}
		if (asset != null) {
			Date xmpModifiedAt = asset.getXmpModifiedAt();
			if (xmpModifiedAt == null && lastmod < xmpdate
					|| xmpModifiedAt != null && xmpModifiedAt.getTime() < xmpdate) {
				outdatedFiles.add(member);
				return;
			}
		}
		if (activeRecipeDetectors != null) {
			if (lastmod < 0) {
				ait = assets.iterator();
				if (asset == null && ait.hasNext())
					asset = ait.next();
				if (asset != null) {
					Date lastModification = asset.getLastModification();
					if (lastModification != null)
						lastmod = lastModification.getTime();
				}
			}
			long recipeModified = -1;
			IRawConverter currentRawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
			long timestamp = currentRawConverter == null ? 0L
					: currentRawConverter.getLastRecipeModification(uri.toString(), 0L, null);
			if (timestamp > 0)
				recipeModified = timestamp;
			if (recipeModified > lastmod)
				outdatedFiles.add(member);
		}
	}

	public HighresImageLoader getHighresImageLoader() {
		if (highresImageLoader == null)
			highresImageLoader = new HighresImageLoader();
		return highresImageLoader;
	}

	public IGeoService getGeoService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		geoServiceRef = bundleContext.getServiceReference(IGeoService.class);
		return geoServiceRef != null ? bundleContext.getService(geoServiceRef) : null;
	}

	public Map<String, Theme> getThemes() {
		if (themes == null) {
			themes = new HashMap<>(6);
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"catalogTheme"); //$NON-NLS-1$
			for (IExtension iExtension : extensionPoint.getExtensions()) {
				for (IConfigurationElement conf : iExtension.getConfigurationElements()) {
					String id = conf.getAttribute("id"); //$NON-NLS-1$
					themes.put(id, new Theme(id, conf.getAttribute("name"), conf.getAttribute("keywords"), //$NON-NLS-1$ //$NON-NLS-2$
							conf.getAttribute("categories"), Boolean.parseBoolean(conf.getAttribute("default")))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return themes;
	}

	public Theme getCurrentTheme() {
		String themeID = dbManager.getMeta(true).getThemeID();
		Map<String, Theme> themes = getThemes();
		if (themeID == null || themeID.isEmpty())
			for (Theme theme : themes.values()) {
				if (theme.isDefault())
					return theme;
			}
		return themes.get(themeID);
	}

}
