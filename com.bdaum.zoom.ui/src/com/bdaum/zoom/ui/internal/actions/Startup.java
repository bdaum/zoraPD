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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.batch.internal.SerializingDaemon;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.IFileWatchListener;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.commands.OpenCatalogCommand;
import com.bdaum.zoom.ui.internal.dialogs.StartupDialog;
import com.bdaum.zoom.ui.internal.job.ChangeProcessor;
import com.bdaum.zoom.ui.internal.job.CheckForUpdateJob;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class Startup implements IStartup, IAdaptable {

	public static final int MAXRECENTCREATIONS = 8;

	private final class FileWatchListener implements IFileWatchListener {

		private final class DelayedCreationDaemon extends SerializingDaemon {
			private final File file;

			public DelayedCreationDaemon(File file) {
				super(Messages.Startup_process_file_creation);
				this.file = file;
			}

			@Override
			public boolean belongsTo(Object family) {
				if (file.equals(family))
					return true;
				return super.belongsTo(family);
			}

			@Override
			protected void doRun(IProgressMonitor monitor) {
				UiActivator activator = UiActivator.getDefault();
				if (activator != null && !activator.isClosing())
					doCreateFile(file);
			}
		}

		private File lastFileCreated = null;
		private List<File> filesCreated = new ArrayList<File>(1);
		private List<File> filesModified = new ArrayList<File>(1);
		private List<File> filesDeleted = new ArrayList<File>(1);
		private LinkedList<File> recentCreations = new LinkedList<File>();

		public void fileCreated(File file) {
			if (file != null)
				Job.getJobManager().cancel(file);
			if (lastFileCreated == null)
				lastFileCreated = file;
			else if (file != null && !file.equals(lastFileCreated)) {
				doCreateFile(lastFileCreated);
				lastFileCreated = file;
			}
			Meta meta = Core.getCore().getDbManager().getMeta(true);
			int folderWatchLatency = meta.getFolderWatchLatency();
			new DelayedCreationDaemon(lastFileCreated)
					.schedule(folderWatchLatency == 0 ? 30000L : folderWatchLatency * 1000L);
			lastFileCreated = null;
		}

		public void fileModified(File file) {
			if (file != null) {
				if (Job.getJobManager().find(file).length > 0)
					fileCreated(file);
				else {
					filesModified.clear();
					filesModified.add(file);
					new ChangeProcessor(null, filesModified, null, computeObservedFolder(file), -1L, null,
							Constants.FOLDERWATCH, Startup.this).schedule(100);
				}
			}
		}

		public void fileDeleted(File file) {
			if (file != null) {
				Job.getJobManager().cancel(file);
				if (recentCreations.remove(file)) {
					filesDeleted.clear();
					filesDeleted.add(file);
					new ChangeProcessor(null, null, filesDeleted, computeObservedFolder(file), -1L, null,
							Constants.FOLDERWATCH, Startup.this).schedule(100);
				}
			}
		}

		private void doCreateFile(File file) {
			if (file != null) {
				filesCreated.clear();
				filesModified.clear();
				CoreActivator.getDefault().classifyFile(file, filesCreated, filesModified, null, null);
				recentCreations.addAll(filesCreated);
				while (recentCreations.size() > MAXRECENTCREATIONS)
					recentCreations.pollFirst();
				if (!filesCreated.isEmpty() || !filesModified.isEmpty())
					new ChangeProcessor(filesCreated, filesModified, null, computeObservedFolder(file), -1L, null,
							Constants.FOLDERWATCH, Startup.this).schedule(100);
			}
		}

		private WatchedFolderImpl computeObservedFolder(File file) {
			CoreActivator activator = CoreActivator.getDefault();
			File parentFile = file.getParentFile();
			return (WatchedFolderImpl) activator.getObservedFolder(Utilities.computeWatchedFolderId(parentFile,
					activator.getVolumeManager().getVolumeForFile(parentFile)));
		}
	}

	private IFileWatchListener fileWatchListener = new FileWatchListener();

	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Shell shell = (Shell) getAdapter(Shell.class);
		String version = System.getProperty("java.runtime.version"); //$NON-NLS-1$
		int p = version.indexOf('_');
		if (p < 0)
			p = version.indexOf('-');
		if (p >= 0)
			version = version.substring(0, p);
		if (Constants.REQUIRED_JAVA_VERSIONS.compareTo(version) > 0) {
			if (shell != null) {
				final String v = version;
				shell.getDisplay().syncExec(() -> AcousticMessageDialog.openError(shell, Messages.Startup_wrong_java_version, NLS.bind(
						Messages.Startup_wrong_java_version_expl,
						new Object[] { System.getProperty("java.home"), Constants.REQUIRED_JAVA_VERSIONS, v })));
			}
		}
		if (ensureDbOpen(workbench)) {
			IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
			workbench.getDisplay().syncExec(() -> {
				for (IWorkbenchWindow window : workbenchWindows)
					window.getShell().setText(Constants.APPLICATION_NAME + " - " //$NON-NLS-1$
							+ Core.getCore().getDbManager().getFileName());
			});

			for (IWorkbenchWindow window : workbenchWindows)
				Ui.getUi().getNavigationHistory(window);
			boolean traymode = Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
					PreferenceConstants.TRAY_MODE, false, null);

			if (traymode && shell != null)
				shell.getDisplay().syncExec(() -> shell.setVisible(false));
			CoreActivator coreActivator = CoreActivator.getDefault();
			try {
				FileWatchManager manager = coreActivator.getFileWatchManager();
				manager.setDefaultFilters(UiActivator.getDefault().getDefaultWatchFilters());
				manager.configureMonitor(true);
				manager.addFileWatchListener(fileWatchListener);
			} catch (Exception e) {
				coreActivator.logError(Messages.Startup_cannot_access_file_manager, e);
			}
			coreActivator.addCatalogListener(new CatalogAdapter() {
				@Override
				public void catalogOpened(boolean newDb) {
					if (!newDb)
						UiActivator.getDefault().postCatInit(true);
				}

				@Override
				public void catalogClosed(int mode) {
					Job.getJobManager().cancel(Constants.CATALOG);
				}

			});
			processDroppedImages(Platform.getApplicationArgs());
			IPeerService peerService = Core.getCore().getDbFactory().getPeerService();
			if (peerService != null)
				peerService.checkListeningPort(this);
			UiActivator.getDefault().setStarted();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (workbenchWindows.length > 0)
				return workbenchWindows[0].getShell();
		}
		return null;
	}

	private boolean ensureDbOpen(final IWorkbench workbench) {
		final IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
		final CoreActivator coreActivator = CoreActivator.getDefault();
		final File catFile = coreActivator.getCatFile();
		if (catFile != null) {
			CoreActivator.logDebug("Open file {0}", catFile); //$NON-NLS-1$
			IDbManager db = coreActivator.openDatabase(catFile.getAbsolutePath());
			CoreActivator.logDebug("Database created", null); //$NON-NLS-1$
			String updatePolicy = Platform.getPreferencesService().getString(UiActivator.PLUGIN_ID,
					PreferenceConstants.UPDATEPOLICY, PreferenceConstants.UPDATEPOLICY_WITHBACKUP, null);
			if (PreferenceConstants.UPDATEPOLICY_ONSTART.equals(updatePolicy)
					|| (PreferenceConstants.UPDATEPOLICY_WITHBACKUP.equals(updatePolicy) && db.isBackupScheduled()))
				new CheckForUpdateJob(this, true).schedule(3000L);
			coreActivator.fireCatalogOpened(false);
		}
		String previousCatUri = UiActivator.getDefault().getPreviousCatUri();
		File previousCatFile = null;
		if (previousCatUri != null) {
			try {
				previousCatFile = new File(new URI(previousCatUri));
			} catch (URISyntaxException e) {
				// do nothing
			}
		}
		IDbManager dbManager = coreActivator.getDbManager();
		if (dbManager.getFile() != null) {
			workbench.getDisplay().syncExec(() -> OpenCatalogCommand.checkPausedFolderWatch(workbench.getDisplay().getActiveShell(), dbManager));
			return true;
		}
		final File file = previousCatFile;
		int[] ret = new int[1];
		while (coreActivator.getDbManager().getFile() == null) {
			workbench.getDisplay().syncExec(() -> {
				try {
					IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow() == null
							? workbenchWindows[0] : workbench.getActiveWorkbenchWindow();
					ret[0] = new StartupDialog(activeWorkbenchWindow, file).open();
				} catch (Exception e) {
					// ignore initial E4 problems
				}
			});
			if (ret[0] == Dialog.CANCEL)
				return false;
		}
		return true;
	}

	public void processDroppedImages(String[] parms) {
		List<File> images = new ArrayList<File>(parms.length);
		List<File> folders = new ArrayList<File>(parms.length);
		for (int i = 0; i < parms.length; i++) {
			String arg = parms[i];
			if (arg.startsWith("-")) //$NON-NLS-1$
				i++;
			else {
				String[] fileNames = new String[] { arg };
				Utilities.collectImages(fileNames, images);
				Utilities.collectFolders(fileNames, folders);
			}
		}
		if (!images.isEmpty())
			OperationJob.executeOperation(new ImportOperation(new FileInput(images, false),
					UiActivator.getDefault().createImportConfiguration(this), null,
					folders.toArray(new File[folders.size()])), this);
	}
}
