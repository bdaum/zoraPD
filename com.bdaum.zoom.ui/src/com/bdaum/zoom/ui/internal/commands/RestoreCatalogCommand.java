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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;

@SuppressWarnings("restriction")
public class RestoreCatalogCommand extends AbstractCatCommandHandler {

	@Override
	public void run() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		dialog.setText(Messages.RestoreCatActionDelegate_select_backup);
		final CoreActivator coreActivator = CoreActivator.getDefault();
		IDbManager dbManager = coreActivator.getDbManager();
		File catFile = dbManager.getFile();
		if (catFile != null) {
			String backupLocation = dbManager.getMeta(true).getBackupLocation();
			if (backupLocation != null && !backupLocation.isEmpty()) {
				File generationFolder = new File(Utilities.computeBackupLocation(catFile, backupLocation)[1]);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(new String[] { Constants.BACKUPEXT },
						false);
				File[] backups = generationFolder.listFiles();
				if (backups != null && backups.length > 0) {
					Arrays.sort(backups, new Comparator<File>() {
						public int compare(File f1, File f2) {
							long m1 = f1.lastModified();
							long m2 = f2.lastModified();
							return m1 == m2 ? 0 : m1 > m2 ? -1 : 1;
						}
					});
					String path = backups[0].getAbsolutePath();
					for (File file : backups)
						if (file.isDirectory() && filter.accept(file.getName())) {
							path = file.getAbsolutePath();
							break;
						}
					dialog.setFilterPath(path);
				}
			}
		}
		String backupDir = dialog.open();
		if (backupDir != null) {
			File sourceFolder = new File(backupDir);
			String sourceName = null;
			String[] list = sourceFolder.list();
			if (list != null)
				for (String name : list)
					if (name.endsWith(BatchConstants.CATEXTENSION)) {
						sourceName = name;
						break;
					}
			if (catFile != null && !catFile.getName().equals(sourceName) && !AcousticMessageDialog.openQuestion(
					getShell(), Messages.RestoreCatActionDelegate_restore_cat,
					NLS.bind(Messages.RestoreCatActionDelegate_not_a_backup_of_current, sourceName, catFile.getName())))
				return;
			DirectoryDialog targetDialog = new DirectoryDialog(getShell(), SWT.SAVE);
			targetDialog.setText(Messages.RestoreCatActionDelegate_target_dir);
			if (catFile != null)
				targetDialog.setFilterPath(catFile.getParent());
			String target = targetDialog.open();
			if (target != null) {
				File targetFolder = new File(target);
				final File targetFile = sourceName == null ? null : new File(targetFolder, sourceName);
				final File sourceFile = sourceName == null ? null : new File(sourceFolder, sourceName);
				if (targetFile != null && sourceFile != null) {
					if (targetFile.exists() && !AcousticMessageDialog.openQuestion(getShell(),
							Messages.RestoreCatActionDelegate_restore_cat,
							NLS.bind(Messages.RestoreCatActionDelegate_already_exists, sourceName)))
						return;
					String absolutePath = sourceFile.getAbsolutePath();
					final File sourceIndexFolder = new File(
							absolutePath.substring(0, absolutePath.length() - BatchConstants.CATEXTENSION.length())
									+ Constants.INDEXEXTENSION);
					absolutePath = targetFile.getAbsolutePath();
					final File targetIndexFolder = new File(
							absolutePath.substring(0, absolutePath.length() - BatchConstants.CATEXTENSION.length())
									+ Constants.INDEXEXTENSION);
					coreActivator.closeDatabase();
					BusyIndicator.showWhile(getShell().getDisplay(), () -> {
						targetFile.delete();
						try {
							BatchUtilities.copyFile(sourceFile, targetFile, null);
							if (targetIndexFolder.exists())
								BatchUtilities.deleteFileOrFolder(targetIndexFolder);
							if (sourceIndexFolder.exists())
								BatchUtilities.copyFolder(sourceIndexFolder, targetIndexFolder, null);
							coreActivator.openDatabase(targetFile.getAbsolutePath());
							CoreActivator.logDebug("Database created", null); //$NON-NLS-1$
							getShell().setText(Constants.APPLICATION_NAME + " - " //$NON-NLS-1$
									+ targetFile);
							coreActivator.fireCatalogOpened(false);
						} catch (IOException e1) {
							AcousticMessageDialog.openError(getShell(), Messages.RestoreCatActionDelegate_restore_cat,
									NLS.bind(Messages.RestoreCatActionDelegate_io_error, e1, targetFile));
							BatchActivator.setFastExit(true);
							getActiveWorkbenchWindow().getWorkbench().close();
						} catch (DiskFullException e2) {
							AcousticMessageDialog.openError(getShell(), Messages.RestoreCatActionDelegate_restore_cat,
									NLS.bind(Messages.RestoreCatActionDelegate_disk_full, targetFile));
							BatchActivator.setFastExit(true);
							getActiveWorkbenchWindow().getWorkbench().close();
						}

					});
				}
			}
		}
	}
}
