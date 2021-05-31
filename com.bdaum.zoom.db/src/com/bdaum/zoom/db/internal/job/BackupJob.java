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

package com.bdaum.zoom.db.internal.job;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.db.internal.DbManager;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class BackupJob extends CustomJob {

	private String backupLocation;
	private final File catFile;
	private final File indexPath;
	private final int generations;
	private String generationFolder;
	private String generationPattern;

	public BackupJob(String backupLocation, File catFile, File indexPath, int generations) {
		super(Messages.getString("BackupJob.Catalog_Backup")); //$NON-NLS-1$
		this.catFile = catFile;
		this.indexPath = indexPath;
		this.generations = generations;
		setPriority(DECORATE);
		String[] result = Utilities.computeBackupLocation(catFile, backupLocation);
		this.backupLocation = result[0];
		generationFolder = result[1];
		generationPattern = result[2];
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		ICore core = Core.getCore();
		DbManager dbManager = (DbManager) core.getDbManager();
		File folder = new File(backupLocation);
		if (folder.exists())
			Core.deleteFileOrFolder(folder);
		if (!folder.mkdirs()) {
			core.getDbFactory().getErrorHandler().showError(Messages.getString("BackupJob.backup_error"), //$NON-NLS-1$
					Messages.getString("BackupJob.folder_creation_failed"), //$NON-NLS-1$
					null);
			return Status.CANCEL_STATUS;
		}
		try {
			File catBackupFile = new File(folder, catFile.getName());
			dbManager.backup(catBackupFile.getAbsolutePath());
			try {
				if (indexPath != null)
					BatchUtilities.copyFolder(indexPath, new File(folder, indexPath.getName()), monitor);
			} catch (IOException e) {
				core.logError(Messages.getString("BackupJob.io_error_index_backup"), e); //$NON-NLS-1$
			}
			BatchUtilities.exportPreferences(new File(folder, BatchConstants.APP_PREFERENCES), null);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			int deleted = -1;
			if (generations < Integer.MAX_VALUE && generationFolder != null) {
				File gFolder = new File(generationFolder);
				if (gFolder.exists()) {
					File[] children = gFolder.listFiles(new FileFilter() {
						public boolean accept(File child) {
							return (child.isDirectory() && child.getName().matches(generationPattern));
						}
					});
					if (children != null && children.length > 0) {
						deleted = children.length - generations - 1;
						if (deleted > 0) {
							Arrays.sort(children);
							for (int i = 0; i < deleted; i++)
								BatchUtilities.deleteFileOrFolder(children[i]);
						}
					}
				}
			}
			core.logInfo(
					deleted > 0 ? NLS.bind(Messages.getString("BackupJob.backup_performed_with_deletions"), deleted) //$NON-NLS-1$
							: Messages.getString("BackupJob.catalog_backup")); //$NON-NLS-1$
		} catch (Exception e) {
			core.logError(Messages.getString("BackupJob.Backup_failed"), e); //$NON-NLS-1$
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

}
