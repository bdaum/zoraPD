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
 * (c) 2009-2015 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.IPreferenceUpdater;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.ui.internal.PreferencesUpdater;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class FolderWatchJob extends SynchronizeCatJob {

	private static final String XMPEXTENSION = ".xmp"; //$NON-NLS-1$
	private static final int XMPEXTLEN = XMPEXTENSION.length();
	private ObjectFilter filter;
	private WatchedFolder[] watchedFolders;
	private CoreActivator activator;

	public FolderWatchJob(WatchedFolder[] watchedFolders) {
		super(Messages.FolderWatchJob_watching_folders);
		this.watchedFolders = watchedFolders;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.internal.job.SynchronizeCatJob#belongsTo(java.lang.
	 * Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		return Constants.DAEMONS == family || Constants.FOLDERWATCH == family ? true : super.belongsTo(family);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		activator = CoreActivator.getDefault();
		filter = activator.getFilenameExtensionFilter();
		if (activator.getDbManager().getFile() != null) {
			FileWatchManager fileWatchManager = activator.getFileWatchManager();
			List<IRecipeDetector> activeRecipeDetectors = activator.getActiveRecipeDetectors();
			try {
				watchFolders(monitor, activeRecipeDetectors, fileWatchManager);
			} catch (OperationCanceledException e) {
				// ignore
			}
		}
		return Status.OK_STATUS;
	}

	private void watchFolders(IProgressMonitor monitor, List<IRecipeDetector> activeRecipeDetectors,
			FileWatchManager fileWatchManager) {
		IDbManager dbManager = activator.getDbManager();
		Meta meta = dbManager.getMeta(true);
		if (meta.getPauseFolderWatch())
			return;
		long lastScan = meta.getLastWatchedFolderScan();
		long startTime = System.currentTimeMillis();
		setYieldStart();
		if (watchedFolders == null) {
			List<String> folders = meta.getWatchedFolder();
			if (folders != null) {
				List<WatchedFolder> w = new ArrayList<WatchedFolder>(folders.size());
				for (String folderId : folders) {
					WatchedFolder f = activator.getObservedFolder(folderId);
					if (f != null)
						w.add(f);
				}
				watchedFolders = w.toArray(new WatchedFolder[w.size()]);
			}
		}
		if (watchedFolders != null && watchedFolders.length > 0) {
			monitor.beginTask(Messages.FolderWatchJob_watching_folders, watchedFolders.length * 1000000);
			IVolumeManager volumeManager = activator.getVolumeManager();
			for (WatchedFolder observedFolder : watchedFolders) {
				File folderFile = volumeManager.findExistingFile(observedFolder.getUri(), observedFolder.getVolume());
				if (folderFile != null) {
					FilterChain filterChain = null;
					if (!observedFolder.getTransfer()) {
						filterChain = new FilterChain(UiUtilities.getFilters(observedFolder), "-+_*", ";", true); //$NON-NLS-1$//$NON-NLS-2$
						filterChain.setBaseLength(folderFile.getAbsolutePath().length() + 1);
					}
					watchFolder(folderFile, monitor, 1000000, System.currentTimeMillis(), observedFolder, filterChain,
							activeRecipeDetectors, fileWatchManager, lastScan);
				}
				if (monitor.isCanceled())
					break;
			}
			monitor.done();
		}
		if (!monitor.isCanceled()) {
			activator.purgeObsoleteWatchedFolderEntries();
			meta.setLastWatchedFolderScan(startTime);
			dbManager.storeAndCommit(meta);
		}
	}

	private void watchFolder(File folder, IProgressMonitor monitor, int work, long timeOfUpdate,
			WatchedFolder observedFolder, FilterChain filterChain, List<IRecipeDetector> activeRecipeDetectors,
			FileWatchManager fileWatchManager, long lastScan) {
		try {
			File[] members = folder.listFiles();
			if (members != null && members.length > 0) {
				List<File> outdatedFiles = new ArrayList<File>();
				List<File> newFiles = new ArrayList<File>();
				int incr = work / members.length;
				monitor.subTask(folder.getPath());
				boolean initXmp = true;
				Map<String, File> xmpMap = null;
				for (File member : members) {
					if (monitor.isCanceled())
						break;
					if (member.isDirectory()) {
						if (observedFolder.getRecursive()
								&& (filterChain == null || filterChain.accept(member, true))) {
							WatchedFolderImpl observedMember = (WatchedFolderImpl) activator
									.getObservedSubfolder(observedFolder, member);
							if (observedMember != null)
								watchFolder(member, monitor, incr, System.currentTimeMillis(), observedMember,
										filterChain, activeRecipeDetectors, fileWatchManager, lastScan);
						}
					} else {
						monitor.worked(incr);
						if (filter.accept(member)
								&& (fileWatchManager == null || !fileWatchManager.isFileIgnored(member))
								&& (filterChain == null || filterChain.accept(member, false))) {
							yield();
							if (initXmp) {
								initXmp = false;
								for (File xmpCandidate : members)
									if (xmpCandidate.isFile()) {
										String xmpName = xmpCandidate.getName();
										if (xmpName.toLowerCase().endsWith(XMPEXTENSION)) {
											if (xmpMap == null)
												xmpMap = new HashMap<String, File>(members.length * 3 / 2);
											xmpMap.put(xmpName.substring(0, xmpName.length() - XMPEXTLEN),
													xmpCandidate);
										}
									}
							}
							if (observedFolder.getTransfer())
								newFiles.add(member);
							else
								activator.classifyFile(member, newFiles, outdatedFiles, xmpMap, activeRecipeDetectors,
										lastScan);
						}
					}
					work -= incr;
				}
				if (monitor.isCanceled())
					return;
				if (!newFiles.isEmpty() || !outdatedFiles.isEmpty()) {
					new ChangeProcessor(newFiles, outdatedFiles, null, observedFolder, timeOfUpdate, null,
							Constants.FOLDERWATCH, this).schedule(250);
					return;
				}
			}
			observedFolder.setLastObservation(timeOfUpdate);
		} catch (Throwable t) {
			UiActivator ui = UiActivator.getDefault();
			if (ui != null)
				ui.logError(Messages.FolderWatchJob_internal_error_synchronizing, t);
		} finally {
			if (!monitor.isCanceled()) {
				monitor.worked(work);
				yield();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (workbenchWindows.length > 0)
				return workbenchWindows[0].getShell();
		}
		if (IPreferenceUpdater.class.equals(adapter))
			return new PreferencesUpdater();
		return super.getAdapter(adapter);
	}

}
