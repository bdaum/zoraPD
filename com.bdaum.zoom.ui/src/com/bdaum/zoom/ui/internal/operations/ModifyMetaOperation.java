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
 * (c) 2009-2016 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.MetaImpl;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.Messages;
import com.bdaum.zoom.operations.internal.StructModifyOperation;
import com.bdaum.zoom.ui.internal.job.FolderWatchJob;

@SuppressWarnings("restriction")
public class ModifyMetaOperation extends DbOperation {

	private Meta meta;
	private String owner;
	private String theme;
	private String description;
	private String userfield1;
	private String userfield2;
	private Collection<String> keywords;
	private Map<String, Category> categories;
	private String resolution;
	private final int sharpen;
	private boolean webpCompression;
	private int jpegQuality;
	private boolean fromPreview;
	private String backupFile;
	private String timeline;
	private final List<WatchedFolder> watchedFolders;
	private final boolean paused;
	private final boolean readOnly;
	private final boolean autoWatch;
	private final String language;
	private final int latency;
	private Set<String> cbirAlgorithms;
	private Set<String> indexedTextFields;

	// Undo
	private final List<WatchedFolder> oldWatchedFolders;
	private Set<String> oldCbirAlgorithms;
	private Set<String> oldIndexedTextFields;
	private final boolean cumulateImports;
	private final String locationOption;
	private boolean noIndex;
	// private Boolean importFaces;
	private Boolean addToKeywords;
	private final Map<String, Map<QueryField, Object>> structOverlayMap;
	private StructModifyOperation structOp;
	private Map<String, IIdentifiableObject> newObjects;
	private boolean onCreation;
	private MetaImpl backup = new MetaImpl();
	private List<String> categoryChanges;
	private List<String> oldAssetCats;
	private List<String> oldAssetSupCats;
	private List<String> vocabularies;

	public ModifyMetaOperation(Meta meta, boolean onCreation, Map<String, Map<QueryField, Object>> structOverlayMap,
			Map<String, IIdentifiableObject> newObjects, String backupFile, String owner, String theme, String description,
			String userfield1, String userfield2, boolean cumulateImports, String timeline, String locationOption,
			Collection<String> keywords, Map<String, Category> categories, String resolution, boolean fromPreview,
			List<WatchedFolder> folderBackup, List<WatchedFolder> watchedFolders, int latency, boolean paused,
			boolean readOnly, boolean autoWatch, int sharpen, boolean webpCompression, int jpegQuality, boolean noIndex,
			String language, Set<String> cbirAlgorithms, Set<String> indexedTextFields, boolean addToKeywords,
			List<String> categoryChanges, List<String> vocabularies) {
		super(Messages.getString("ModifyMetaOperation.Modify_meta")); //$NON-NLS-1$
		this.meta = meta;
		this.onCreation = onCreation;
		this.structOverlayMap = structOverlayMap;
		this.newObjects = newObjects;
		this.owner = owner;
		this.theme = theme;
		this.description = description;
		this.userfield1 = userfield1;
		this.userfield2 = userfield2;
		this.cumulateImports = cumulateImports;
		this.timeline = timeline;
		this.locationOption = locationOption;
		this.keywords = keywords;
		this.categories = categories;
		this.resolution = resolution;
		this.backupFile = backupFile;
		this.fromPreview = fromPreview;
		this.oldWatchedFolders = folderBackup;
		this.watchedFolders = watchedFolders;
		this.latency = latency;
		this.paused = paused;
		this.readOnly = readOnly;
		this.autoWatch = autoWatch;
		this.sharpen = sharpen;
		this.webpCompression = webpCompression;
		this.jpegQuality = jpegQuality;
		this.noIndex = noIndex;
		this.language = language;
		this.cbirAlgorithms = cbirAlgorithms;
		this.indexedTextFields = indexedTextFields;
		this.addToKeywords = addToKeywords;
		this.categoryChanges = categoryChanges;
		this.vocabularies = vocabularies;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, categoryChanges == null ? 2 : 2 + categoryChanges.size());
		List<Object> toBeStored = new ArrayList<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		oldCbirAlgorithms = meta.getCbirAlgorithms();
		oldIndexedTextFields = meta.getIndexedTextFields();
		Utilities.copyMeta(meta, backup);
		if (backupFile != null)
			meta.setBackupLocation(backupFile);
		meta.setPauseFolderWatch(readOnly ? true : paused);
		meta.setNoIndex(noIndex);
		meta.setCbirAlgorithms(cbirAlgorithms);
		meta.setIndexedTextFields(indexedTextFields);
		meta.setReadonly(readOnly);
		System.setProperty(Constants.PROP_CATACCESS,
				readOnly ? Constants.PROP_CATACCESS_READ : Constants.PROP_CATACCESS_WRITE);
		meta.setAutoWatch(readOnly ? false : autoWatch);
		if (noIndex && noIndex != backup.getNoIndex() || !cbirAlgorithms.equals(oldCbirAlgorithms)
				|| !indexedTextFields.equals(oldIndexedTextFields))
			reconfigureIndexing(info, noIndex, cbirAlgorithms, indexedTextFields);
		boolean pause = meta.getPauseFolderWatch();
		boolean pauseChanged = pause != backup.getPauseFolderWatch();
		boolean pauseStarts = pauseChanged && meta.getPauseFolderWatch();
		boolean pauseEnds = pauseChanged && !meta.getPauseFolderWatch();

		if (oldWatchedFolders != null && watchedFolders != null) {
			List<String> ids = updateWatchedFolders(oldWatchedFolders, readOnly ? oldWatchedFolders : watchedFolders,
					toBeDeleted, toBeStored, pause, pauseStarts, pauseEnds);
			if (ids != null)
				meta.setWatchedFolder(ids);
		}
		boolean changed = false;
		if (!readOnly) {
			if (owner != null)
				meta.setOwner(owner);
			if (theme != null)
				meta.setThemeID(theme);
			if (description != null)
				meta.setDescription(description);
			if (userfield1 != null)
				meta.setUserFieldLabel1(userfield1);
			if (userfield2 != null)
				meta.setUserFieldLabel2(userfield2);
			if (timeline != null)
				meta.setTimeline(timeline);
			if (locationOption != null)
				meta.setLocationFolders(locationOption);
			if (keywords != null)
				meta.setKeywords(keywords);
			if (categories != null) {
				changed = Utilities.updateCategories(dbManager, meta.getCategory(), categories, toBeDeleted,
						toBeStored);
				meta.setCategory(categories);
			}
			if (resolution != null)
				meta.setThumbnailResolution(resolution);
			meta.setSharpen(sharpen);
			meta.setWebpCompression(webpCompression);
			meta.setJpegQuality(jpegQuality);
			meta.setLocale(language);
			meta.setPersonsToKeywords(addToKeywords);
			meta.setCumulateImports(cumulateImports);
			meta.setThumbnailFromPreview(fromPreview);
			meta.setFolderWatchLatency(latency);
			meta.setVocabularies(vocabularies);
		}
		toBeStored.add(meta);
		dbManager.setReadOnly(false);
		storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
		if (!readOnly && structOverlayMap != null && !structOverlayMap.isEmpty()) {
			structOp = new StructModifyOperation(getLabel(), structOverlayMap, newObjects);
			structOp.execute(SubMonitor.convert(monitor, 1), info);
		}
		aMonitor.worked(1);
		if (!readOnly && categoryChanges != null && !categoryChanges.isEmpty()) {
			oldAssetCats = new ArrayList<>();
			oldAssetSupCats = new ArrayList<>();
			int i = 0;
			Iterator<String> it = categoryChanges.iterator();
			while (it.hasNext()) {
				String oldLabel = it.next();
				if (!it.hasNext())
					break;
				String newLabel = it.next();
				List<AssetImpl> assets = dbManager.obtainObjects(AssetImpl.class, QueryField.IPTC_CATEGORY.getKey(),
						oldLabel, QueryField.EQUALS);
				for (AssetImpl asset : assets) {
					asset.setCategory(newLabel);
					dbManager.store(asset);
					oldAssetCats.add(asset.getStringId());
					oldAssetCats.add(asset.getCategory());
					if (++i % 100 == 0)
						dbManager.commit();
				}
				assets = dbManager.obtainObjects(AssetImpl.class, QueryField.IPTC_SUPPLEMENTALCATEGORIES.getKey(),
						oldLabel, QueryField.EQUALS);
				for (AssetImpl asset : assets) {
					String[] supplementalCats = asset.getSupplementalCats();
					for (int j = 0; j < supplementalCats.length; j++) {
						if (oldLabel.equals(supplementalCats[i])) {
							oldAssetSupCats.add(asset.getStringId());
							oldAssetSupCats.add(newLabel);
							oldAssetSupCats.add(oldLabel);
							supplementalCats[i] = newLabel;
							break;
						}
					}
					dbManager.store(asset);
					if (++i % 100 == 0)
						dbManager.commit();
				}
				aMonitor.worked(1);
			}
			if (i % 100 > 0)
				dbManager.commit();
		}
		dbManager.setReadOnly(readOnly);
		if (changed)
			fireStructureModified();
		return close(info);
	}

	private void reconfigureIndexing(IAdaptable info, boolean noix, Set<String> cbir, Set<String> fields) {
		IDbFactory dbFactory = Core.getCore().getDbFactory();
		dbFactory.getLireService(true).configureCBIR(cbir);
		dbFactory.getLuceneService().configureTextIndex(fields);
		if (onCreation || dbFactory.getErrorHandler().question(Messages.getString("ModifyMetaOperation.index_change"), //$NON-NLS-1$
				noix ? Messages.getString("ModifyMetaOperation.delete_index_files_now") //$NON-NLS-1$
						: Messages.getString("ModifyMetaOperation.recreate_index_now"), //$NON-NLS-1$
				info)) {
			CoreActivator.getDefault().recreateIndex();
		}
	}

	private List<String> updateWatchedFolders(List<WatchedFolder> oldFolders, List<WatchedFolder> newFolders,
			Set<Object> toBeDeleted, List<Object> toBeStored, boolean pause, boolean pauseStarts, boolean pauseEnds) {
		List<String> ids = null;
		if (oldFolders == null || !oldFolders.equals(newFolders)) {
			CoreActivator activator = CoreActivator.getDefault();
			FileWatchManager fileWatchManager = activator.getFileWatchManager();
			ids = new ArrayList<String>();
			if (oldFolders != null)
				for (WatchedFolder folder : oldFolders) {
					deleteFolderTree(folder, toBeDeleted);
					fileWatchManager.removeImageFolder(folder);
				}
			if (newFolders != null)
				for (WatchedFolder folder : newFolders) {
					toBeStored.add(folder);
					ids.add(folder.getStringId());
					activator.putObservedFolder(folder);
					fileWatchManager.addImageFolder(folder);
				}
		}
		if (pauseStarts) {
			Job.getJobManager().cancel(Constants.FOLDERWATCH);
		} else if (pauseEnds && !newFolders.isEmpty()) {
			new FolderWatchJob(null).schedule(500);
		} else if (!pause && ids != null) {
			Job.getJobManager().cancel(Constants.FOLDERWATCH);
			if (!ids.isEmpty())
				new FolderWatchJob(null).schedule(500);
		}
		return ids;
	}

	private void deleteFolderTree(WatchedFolder folder, Set<Object> toBeDeleted) {
		toBeDeleted.add(folder);
		CoreActivator activator = CoreActivator.getDefault();
		activator.removeObservedFolder(folder);
		IVolumeManager volumeManager = activator.getVolumeManager();
		File folderFile = volumeManager.findExistingFile(folder.getUri(), folder.getVolume());
		if (folderFile != null) {
			File[] members = folderFile.listFiles();
			if (members != null && members.length > 0) {
				IDbManager dbManager = activator.getDbManager();
				for (File member : members) {
					if (member.isDirectory()) {
						String volume = volumeManager.getVolumeForFile(member);
						String id = Utilities.computeWatchedFolderId(member, volume);
						WatchedFolderImpl wf = dbManager.obtainById(WatchedFolderImpl.class, id);
						if (wf != null) {
							toBeDeleted.add(wf);
							deleteFolderTree(wf, toBeDeleted);
						}
					}
				}
			}
		}

	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		int work = 20;
		if (oldAssetCats != null)
			work += oldAssetCats.size();
		if (oldAssetSupCats != null)
			work += oldAssetSupCats.size();
		initUndo(aMonitor, work);
		if (structOp != null)
			structOp.undo(SubMonitor.convert(monitor, 10), info);
		List<Object> toBeStored = new ArrayList<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		boolean changed = Utilities.updateCategories(dbManager, meta.getCategory(), backup.getCategory(), toBeDeleted,
				toBeStored);
		boolean pause = backup.getPauseFolderWatch();
		boolean pauseChanged = pause != meta.getPauseFolderWatch();
		boolean pauseStarts = pauseChanged && backup.getPauseFolderWatch();
		boolean pauseEnds = pauseChanged && !backup.getPauseFolderWatch();

		Utilities.copyMeta(backup, meta);
		List<String> ids = updateWatchedFolders(watchedFolders, oldWatchedFolders, toBeDeleted, toBeStored, pause,
				pauseStarts, pauseEnds);
		if (ids != null)
			meta.setWatchedFolder(ids);
		dbManager.setReadOnly(false);
		toBeStored.add(meta);
		storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
		dbManager.setReadOnly(backup.getReadonly());
		if (noIndex && noIndex != backup.getNoIndex() || !cbirAlgorithms.equals(oldCbirAlgorithms)
				|| !indexedTextFields.equals(oldIndexedTextFields))
			reconfigureIndexing(info, backup.getNoIndex(), oldCbirAlgorithms, oldIndexedTextFields);
		aMonitor.worked(10);
		int i = 0;
		if (oldAssetCats != null) {
			Iterator<String> it = oldAssetCats.iterator();
			while (it.hasNext()) {
				String id = it.next();
				if (!it.hasNext())
					break;
				String label = it.next();
				AssetImpl asset = dbManager.obtainAsset(id);
				if (asset != null) {
					asset.setCategory(label);
					dbManager.store(asset);
					if (++i % 100 == 0)
						dbManager.commit();
				}
				aMonitor.worked(1);
			}
		}
		if (oldAssetSupCats != null) {
			Iterator<String> it = oldAssetSupCats.iterator();
			while (it.hasNext()) {
				String id = it.next();
				if (!it.hasNext())
					break;
				String oldLabel = it.next();
				if (!it.hasNext())
					break;
				String newLabel = it.next();
				AssetImpl asset = dbManager.obtainAsset(id);
				if (asset != null) {
					String[] supplementalCats = asset.getSupplementalCats();
					for (int j = 0; j < supplementalCats.length; j++) {
						if (oldLabel.equals(supplementalCats[i])) {
							supplementalCats[i] = newLabel;
							break;
						}
					}
					dbManager.store(asset);
					if (++i % 100 == 0)
						dbManager.commit();
				}
				aMonitor.worked(1);
			}
		}
		if (i % 100 > 0)
			dbManager.commit();
		if (changed)
			fireStructureModified();
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.META;
	}

	public int getUndoProfile() {
		return IProfiledOperation.META;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		// do nothing
	}

}
