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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.job;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.job.ProfiledSchedulingRule;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.DeleteOperation;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class ChangeProcessor extends Job {

	private final List<File> outdatedFiles;
	private final List<File> newFiles;
	private final WatchedFolder observedFolder;
	private final long timeOfUpdate;
	private ImportConfiguration config;
	private final IAdaptable adaptable;
	private final List<File> deletedFiles;
	private final String parentFamily;

	public ChangeProcessor(List<File> newFiles, List<File> outdatedFiles,
			List<File> deletedFiles, WatchedFolder observedFolder,
			long timeOfUpdate, ImportConfiguration config, String parentFamily,
			IAdaptable adaptable) {
		super(Messages.FolderWatchJob_processing_folder_changes);
		this.parentFamily = parentFamily;
		this.adaptable = adaptable;
		this.config = (config != null) ? config : UiActivator.getDefault()
				.createImportConfiguration(adaptable, true, true, false, true,
						false, false, true, true, true);
		setRule(new ProfiledSchedulingRule(
				ImportOperation.class,
				config != null
						&& config.rawOptions.equals(Constants.RAWIMPORT_BOTH) ? IProfiledOperation.CONTENT
						| IProfiledOperation.SYNCHRONIZE
						| IProfiledOperation.FILE
						: IProfiledOperation.CONTENT
								| IProfiledOperation.SYNCHRONIZE));
		setPriority(Job.DECORATE);
		setSystem(true);
		this.newFiles = newFiles;
		this.outdatedFiles = outdatedFiles;
		this.deletedFiles = deletedFiles;
		this.observedFolder = observedFolder;
		this.timeOfUpdate = timeOfUpdate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 *
	 * Framework method - no public API
	 */

	@Override
	public boolean belongsTo(Object family) {
		return Constants.DAEMONS == family
				|| Constants.OPERATIONJOBFAMILY == family
				|| parentFamily == family;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		MultiStatus mstatus = new MultiStatus(UiActivator.PLUGIN_ID, 0,
				Messages.FolderWatchJob_folder_update_report, null);
		int completeWork = (outdatedFiles != null ? outdatedFiles.size() : 0)
				+ (newFiles != null ? newFiles.size() : 0)
				+ (deletedFiles != null ? deletedFiles.size() : 0);
		if (completeWork > 0) {
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			if (outdatedFiles != null && !outdatedFiles.isEmpty()) {
				try {
					mstatus.addAll(new ImportOperation(new FileInput(
							outdatedFiles, false), config, null, null).execute(
							progress.newChild(outdatedFiles.size() * 100
									/ completeWork), adaptable));
				} catch (ExecutionException e) {
					mstatus.add(new Status(IStatus.ERROR,
							UiActivator.PLUGIN_ID,
							Messages.FolderWatchJob_updating_of_images_failed,
							e));
				}
			}
			if (newFiles != null && !newFiles.isEmpty()
					&& !monitor.isCanceled()) {
				try {
					ImportOperation op = null;
					config.isSynchronize = false;
					config.isResetStatus = true;
					config.isResetIptc = true;
					config.isResetGps = true;
					config.isResetFaceData = true;
					config.silent = true;
					if (observedFolder != null && observedFolder.getTransfer()) {
						ImportFromDeviceData deviceData = new ImportFromDeviceData(
								newFiles.toArray(new File[newFiles.size()]),
								false, observedFolder);
						deviceData.setArtist(observedFolder.getArtist());
						deviceData.setCue(observedFolder.getCue());
						deviceData.setDetectDuplicates(observedFolder
								.getSkipDuplicates());
						deviceData.setFileInput(new FileInput(newFiles, true));
						deviceData.setRenamingTemplate(observedFolder
								.getSelectedTemplate());
						deviceData
								.setSkipPolicy(observedFolder.getSkipPolicy());
						deviceData.setSubfolderPolicy(observedFolder
								.getSubfolderPolicy());
						deviceData.setTargetDir(observedFolder.getTargetDir());
						if (!monitor.isCanceled())
							op = new ImportOperation(deviceData, config,
									observedFolder.getFileSource());
					} else if (!monitor.isCanceled())
						op = new ImportOperation(
								new FileInput(newFiles, false), config, null,
								null);
					if (op != null)
						try {
							mstatus.addAll(op.execute(
									progress.newChild(newFiles.size() * 100
											/ completeWork), adaptable));
						} catch (ExecutionException e) {
							mstatus.add(new Status(
									IStatus.ERROR,
									UiActivator.PLUGIN_ID,
									Messages.FolderWatchJob_import_of_new_images_failed,
									e));
						}
				} catch (RuntimeException e) {
					// ignore
				}
			}
			if (deletedFiles != null && !deletedFiles.isEmpty()) {
				List<Asset> assets = new ArrayList<Asset>();
				IDbManager dbManager = Core.getCore().getDbManager();
				for (File file : deletedFiles)
					assets.addAll(dbManager.obtainAssetsForFile(file.toURI()));
				try {
					mstatus.addAll(new DeleteOperation(assets, false, null,
							null, null, config).execute(
							progress.newChild(outdatedFiles.size() * 100
									/ completeWork), adaptable));
				} catch (ExecutionException e) {
					mstatus.add(new Status(IStatus.ERROR,
							UiActivator.PLUGIN_ID,
							Messages.ChangeProcessor_deletion_failed, e));
				}
			}
		}
		if (timeOfUpdate > 0 && observedFolder != null && !monitor.isCanceled()
				&& mstatus.isOK())
			observedFolder.setLastObservation(timeOfUpdate);
		return mstatus;
	}

}