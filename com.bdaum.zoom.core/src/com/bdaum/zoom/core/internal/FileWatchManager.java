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
 * (c) 2009-2011 Berthold Daum  
 */
package com.bdaum.zoom.core.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.batch.internal.SerializingDaemon;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.IRecipeDetector.RecipeFolder;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.fileMonitor.internal.watcher.FileMonitor;
import com.bdaum.zoom.fileMonitor.internal.watcher.FileWatchListener;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class FileWatchManager implements IFileWatcher {
	private SerializingDaemon fileChangedDaemon = new SerializingDaemon(
			Messages.FileWatchManager_process_file_changes) {
		@Override
		protected void doRun(IProgressMonitor monitor) {
			processFileChanges(monitor);
		}
	};

	private FileMonitor fileMonitor;
	private List<String> paused = new ArrayList<String>(3);
	private Map<File, String> ignoredFiles = new Hashtable<File, String>(257);
	private Map<File, List<IRecipeDetector>> observedMetaFiles = new Hashtable<File, List<IRecipeDetector>>(15);
	private Map<File, List<IRecipeDetector>> observedRecursiveMetaFolders = new Hashtable<File, List<IRecipeDetector>>(
			15);
	private Map<String, FilterChain> filters = new Hashtable<String, FilterChain>(5);
	ListenerList<IFileWatchListener> listeners = new ListenerList<IFileWatchListener>();
	private ObjectFilter mediaFilter;
	private int activeWindows = 0;
	private File todo;
	private int todoKind;
	private Map<File, WatchedFolder> observedFolders = new Hashtable<File, WatchedFolder>(5);
	private String defaultWatchFilters;
	protected Set<File> previousFiles = ConcurrentHashMap.newKeySet(3);
	protected IRecipeDetector previousDetector;
	protected int previousKind;

	private FileMonitor getFileMonitor() {
		if (mediaFilter == null) {
			List<String> mediaExtensions = new ArrayList<String>();
			mediaExtensions.addAll(ImageConstants.getAllFormats());
			mediaExtensions.addAll(CoreActivator.getDefault().getMediaFormats());
			mediaFilter = new FileNameExtensionFilter(mediaExtensions.toArray(new String[mediaExtensions.size()]));
			try {
				fileMonitor = new FileMonitor();
			} catch (IOException e1) {
				CoreActivator.getDefault().logError(Messages.FileWatchManager_cannot_instantiate_file_monitor, e1);
				return null;
			}
			fileMonitor.addFileListener(new FileWatchListener() {
				public synchronized void fileChanged(File file, int kind) {
					try {
						if (!paused.isEmpty() || isFileIgnored(file))
							return;
						fileChangedDaemon.cancel();
						Set<File> imageFiles = ConcurrentHashMap.newKeySet(3);
						IRecipeDetector recipeDetector = findImageFiles(findRecipeDetectors(file), file, imageFiles);
						if (kind == previousKind || previousKind == CREATED && kind == MODIFIED)
							kind = previousKind;
						previousFiles.removeAll(imageFiles);
						processFileChanges(null);
						previousKind = kind;
						previousFiles = imageFiles;
						previousDetector = recipeDetector;
						fileChangedDaemon.schedule(1500L);
					} catch (Exception e) {
						// Just in case. An uncaught exception would cause the
						// fileMonitor thread to abort
						CoreActivator.getDefault()
								.logError(Messages.FileWatchManager_internal_error_processing_file_change, e);
					}
				}
			});
		}
		return fileMonitor;
	}

	protected List<IRecipeDetector> findRecipeDetectors(File file) {
		File folder = file.getParentFile();
		List<IRecipeDetector> recipeDetectors = observedMetaFiles.get(file);
		if (recipeDetectors == null) {
			recipeDetectors = observedMetaFiles.get(folder);
			if (recipeDetectors == null)
				while (folder != null) {
					recipeDetectors = observedRecursiveMetaFolders.get(folder);
					if (recipeDetectors != null)
						break;
					folder = folder.getParentFile();
				}
		}
		return recipeDetectors;
	}

	protected IRecipeDetector findImageFiles(List<IRecipeDetector> recipeDetectors, File file, Set<File> result) {
		String filename = file.getName();
		if (filename.toLowerCase().endsWith(".xmp")) { //$NON-NLS-1$
			filename = filename.substring(0, filename.length() - 4);
			File folder = file.getParentFile();
			File[] members = folder.listFiles(mediaFilter);
			if (members != null)
				for (File member : members)
					if (member.isFile()) {
						String mname = member.getName();
						if (mname.startsWith(filename)
								&& (filename.length() == mname.length() || mname.lastIndexOf('.') == filename.length()))
							result.add(member);
					}
			return null;
		}
		if (recipeDetectors != null) {
			List<WatchedFolder> watchedFolders = getWatchedFolders();
			WatchedFolder[] wf = watchedFolders.toArray(new WatchedFolder[watchedFolders.size()]);
			for (IRecipeDetector recipeDetector : recipeDetectors) {
				File imageFile = recipeDetector.getChangedImageFile(file, wf);
				if (imageFile != null) {
					result.add(imageFile);
					return recipeDetector;
				}
			}
		}
		result.add(file);
		return null;
	}

	public boolean isFileIgnored(File file) {
		return (ignoredFiles.containsKey(file));
	}

	protected void processFileChanges(IProgressMonitor monitor) {
		IDbManager dbManager = CoreActivator.getDefault().getDbManager();
		if (!dbManager.getMeta(true).getPauseFolderWatch() || Core.getCore().isTetheredShootingActive())
			for (File file : previousFiles) {
				if (monitor != null && monitor.isCanceled())
					return;
				if (!isFileIgnored(file) && (file.exists() || previousKind == FileWatchListener.DELETED))
					fireFileModificationEvent(previousDetector, file, previousKind);
			}
		previousFiles.clear();
		previousDetector = null;
		previousKind = FileWatchListener.NOOP;
	}

	private void fireFileModificationEvent(IRecipeDetector recipeDetector, File file, int kind) {
		boolean incremental = recipeDetector == null || kind == FileWatchListener.DELETED ? false
				: recipeDetector.usesIncrementalUpdate();
		if (activeWindows <= 0 && incremental) {
			if (!file.equals(todo)) {
				if (todo != null)
					doFireFileModificationEvent(todo, todoKind);
				todo = file;
				todoKind = kind;
			}
			return;
		}
		doFireFileModificationEvent(file, kind);
	}

	private void doFireFileModificationEvent(File file, int kind) {
		todo = null;
		if (!isFileIgnored(file) && mediaFilter.accept(file) && testFile(file)) {
			if (kind == FileWatchListener.CREATED)
				kind = checkFileAgainstCatalog(file);
			if (kind != FileWatchListener.NOOP)
				for (IFileWatchListener listener : listeners)
					switch (kind) {
					case FileWatchListener.CREATED:
						listener.fileCreated(file);
						break;
					case FileWatchListener.DELETED:
						listener.fileDeleted(file);
						break;
					default:
						listener.fileModified(file);
					}
		}
	}

	private static int checkFileAgainstCatalog(File file) {
		URI uri = file.toURI();
		IDbManager dbManager = CoreActivator.getDefault().getDbManager();
		List<AssetImpl> assets = dbManager.obtainAssetsForFile(uri);
		if ((assets == null || assets.isEmpty())) {
			List<Trash> t = dbManager.obtainTrashForFile(uri);
			if ((t == null || t.isEmpty()) && !dbManager.obtainGhostsForFile(uri).isEmpty())
				return FileWatchListener.NOOP;
			return FileWatchListener.CREATED;
		}
		return FileWatchListener.MODIFIED;
	}

	public void addFileWatchListener(IFileWatchListener listener) {
		listeners.add(listener);
	}

	public void removeFileWatchListener(IFileWatchListener listener) {
		listeners.remove(listener);
	}

	private void updateRecipeFolders(WatchedFolder[] watchedFolders, boolean add, boolean update) {
		List<IRecipeDetector> recipeDetectors = CoreActivator.getDefault().getActiveRecipeDetectors();
		if (recipeDetectors != null)
			for (IRecipeDetector recipeDetector : recipeDetectors) {
				List<RecipeFolder> additionalFolders = recipeDetector.computeWatchedMetaFilesOrFolders(watchedFolders,
						observedMetaFiles, observedRecursiveMetaFolders, update, !add);
				if (additionalFolders != null)
					for (RecipeFolder folder : additionalFolders)
						if (!observedFolders.containsKey(folder.file)) {
							if (add)
								addWatch(getFileMonitor(), folder.file, folder.recursive, null);
							else if (fileMonitor != null)
								removeWatch(fileMonitor, folder.file);
						}
			}
	}

	private static boolean addWatch(FileMonitor monitor, File file, boolean recursive, FilterChain filterChain) {
		if (monitor != null && file != null && file.exists())
			try {
				synchronized (monitor) {
					monitor.addWatch(file, recursive, filterChain);
					return true;
				}
			} catch (IOException e) {
				CoreActivator.getDefault().logError(NLS.bind(Messages.FileWatchManager_io_error, file), e);
			}
		return false;
	}

	private static void removeWatch(FileMonitor monitor, File file) {
		synchronized (monitor) {
			monitor.removeWatch(file);
		}
	}

	public void addImageFolder(WatchedFolder observedFolder) {
		doAddImageFolder(observedFolder);
		updateRecipeFolders(new WatchedFolder[] { observedFolder }, true, true);
	}

	private void doAddImageFolder(WatchedFolder observedFolder) {
		File folderFile = CoreActivator.getDefault().getVolumeManager().findExistingFile(observedFolder.getUri(),
				observedFolder.getVolume());
		if (addWatch(getFileMonitor(), folderFile, observedFolder.getRecursive(), getFilterChain(observedFolder)))
			observedFolders.put(folderFile, observedFolder);
	}

	public void removeImageFolder(WatchedFolder observedFolder) {
		doRemoveImageFolder(observedFolder);
		updateRecipeFolders(new WatchedFolder[] { observedFolder }, false, true);
	}

	private void doRemoveImageFolder(WatchedFolder observedFolder) {
		File folderFile = CoreActivator.getDefault().getVolumeManager().findExistingFile(observedFolder.getUri(),
				observedFolder.getVolume());
		if (folderFile != null && fileMonitor != null)
			removeWatch(fileMonitor, folderFile);
	}

	public void ignore(File newFile, String opId) {
		if (newFile != null)
			if (opId != null)
				ignoredFiles.put(newFile, opId);
			else
				ignoredFiles.remove(newFile);
	}

	public void stopIgnoring(String opId) {
		fileChangedDaemon.cancel();
		if (!previousFiles.isEmpty())
			processFileChanges(null);
		Set<Entry<File, String>> entrySet = ignoredFiles.entrySet();
		@SuppressWarnings("unchecked")
		Map.Entry<File, String>[] entries = entrySet.toArray(new Map.Entry[entrySet.size()]);
		for (Entry<File, String> entry : entries)
			if (entry.getValue().equals(opId))
				ignoredFiles.remove(entry.getKey());
	}

	public void updateWatchedMetaFolders() {
		if (fileMonitor != null) {
			for (File file : observedMetaFiles.keySet())
				fileMonitor.removeWatch(file);
			for (File file : observedRecursiveMetaFolders.keySet())
				fileMonitor.removeWatch(file);
		}
		observedMetaFiles.clear();
		observedRecursiveMetaFolders.clear();
		configureMonitor(false);
	}

	public void configureMonitor(boolean init) {
		List<WatchedFolder> watchedFolders = getWatchedFolders();
		if (init)
			for (WatchedFolder watchedFolder : watchedFolders)
				doAddImageFolder(watchedFolder);
		updateRecipeFolders(watchedFolders.toArray(new WatchedFolder[watchedFolders.size()]), true, false);
	}

	private static List<WatchedFolder> getWatchedFolders() {
		CoreActivator activator = CoreActivator.getDefault();
		List<WatchedFolder> folders = new ArrayList<WatchedFolder>();
		List<String> ids = activator.getDbManager().getMeta(true).getWatchedFolder();
		if (ids != null)
			for (String folderId : ids) {
				WatchedFolder observedFolder = activator.getObservedFolder(folderId);
				if (observedFolder != null)
					folders.add(observedFolder);
			}
		return folders;
	}

	public void moveFileSilently(File source, File target, String opId, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		ignore(target, opId);
		BatchUtilities.moveFile(source, target, monitor);
	}

	public void copyFileSilently(StorageObject source, File target, long lastModified, String opId,
			IProgressMonitor monitor) throws IOException, DiskFullException {
		ignore(target, opId);
		source.copy(target, monitor);
	}

	public void dispose() {
		fileChangedDaemon.cancel();
		if (fileMonitor != null)
			fileMonitor.dispose();
	}

	public void setActiveWindows(boolean activated) {
		this.activeWindows += activated ? 1 : -1;
		if (this.activeWindows > 0 && todo != null) {
			doFireFileModificationEvent(todo, todoKind);
			todo = null;
		}
	}

	private boolean testFile(File file) {
		File folder = file.getParentFile();
		while (folder != null) {
			WatchedFolder watchedFolder = observedFolders.get(folder);
			if (watchedFolder != null) {
				FilterChain filterChain = getFilterChain(watchedFolder);
				return (filterChain == null || filterChain.accept(file, false)) ? true : false;
			}
			folder = folder.getParentFile();
		}
		return false;
	}

	private FilterChain getFilterChain(WatchedFolder watchedFolder) {
		String spec = watchedFolder.getFilters();
		if (spec == null)
			spec = defaultWatchFilters;
		if (spec == null)
			return null;
		FilterChain filterChain = filters.get(spec);
		if (filterChain == null) {
			filterChain = new FilterChain(spec, "-+_*", ";", true); //$NON-NLS-1$//$NON-NLS-2$
			File folderFile = CoreActivator.getDefault().getVolumeManager().findExistingFile(watchedFolder.getUri(),
					watchedFolder.getVolume());
			filterChain.setBaseLength(folderFile == null ? 0 : folderFile.getAbsolutePath().length() + 1);
			filters.put(spec, filterChain);
		}
		return filterChain;
	}

	public void setDefaultFilters(String defaultWatchFilters) {
		this.defaultWatchFilters = defaultWatchFilters;
	}

	public long getFreeSpace(File rootFile) {
		return getFileMonitor().getFreeSpace(rootFile);
	}

	public String getDefaultFilters() {
		return defaultWatchFilters;
	}

	public void setPaused(boolean paused, String opid) {
		if (paused)
			this.paused.add(opid);
		else
			this.paused.remove(opid);

	}

}
