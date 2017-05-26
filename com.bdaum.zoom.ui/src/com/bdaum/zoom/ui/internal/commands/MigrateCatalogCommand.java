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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.MigrationPolicy;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.operations.internal.MigrateOperation;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.MigrateDialog;
import com.bdaum.zoom.ui.internal.wizards.ExportPreferencesWizard;

@SuppressWarnings("restriction")
public class MigrateCatalogCommand extends AbstractCommandHandler {

	private IStatus status;
	private int severity;
	private String finalMessage;

	@Override
	public void run() {
		IDbManager dbManager = Core.getCore().getDbManager();
		File catFile = dbManager.getFile();
		if (catFile != null) {
			Job[] criticalJobs = Job.getJobManager().find(Constants.CRITICAL);
			if (criticalJobs.length > 0) {
				AcousticMessageDialog.openInformation(getShell(), Messages.MigrateCatActionDelegate_cat_migratiion,
						Messages.MigrateCatActionDelegate_cannot_execute);
				return;
			}
			MigrateDialog dialog = new MigrateDialog(getShell(), catFile);
			if (dialog.open() == MigrateDialog.OK) {
				final MigrationPolicy policy = dialog.getResult();
				File targetFile = new File(policy.getTargetCatalog());
				if (targetFile.exists() && !AcousticMessageDialog.openQuestion(getShell(),
						Messages.MigrateCatActionDelegate_cat_migratiion,
						NLS.bind(Messages.MigrateCatActionDelegate_target_exists, targetFile.getName())))
					return;
				targetFile.delete();
				IDbFactory dbFactory = Core.getCore().getDbFactory();
				final IDbManager newDbManager = dbFactory.createDbManager(policy.getTargetCatalog(), true, false,
						false);
				IRunnableWithProgress runnable1 = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						CoreActivator.getDefault().getFileWatchManager().setPaused(true, this.getClass().toString());
						Job.getJobManager().cancel(Constants.FOLDERWATCH);
						Job.getJobManager().cancel(Constants.SYNCPICASA);
						Core.waitOnJobCanceled(Constants.FOLDERWATCH, Constants.SYNCPICASA);
						MigrateOperation op = new MigrateOperation(newDbManager, policy);
						try {
							status = op.execute(monitor, MigrateCatalogCommand.this);
							MigrateCatalogCommand.this.finalMessage = op.getFinalMessage();
							MigrateCatalogCommand.this.severity = op.getSeverity();
						} catch (ExecutionException e) {
							status = new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
									Messages.MigrateCatActionDelegate_internal_error, e);
						}
					}
				};
				ZProgressMonitorDialog pm = new ZProgressMonitorDialog(getShell());
				try {
					pm.run(true, true, runnable1);
					if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.CANCEL) {
						if (status.getSeverity() == IStatus.ERROR) {
							AcousticMessageDialog.openError(getShell(),
									Messages.MigrateCatActionDelegate_cat_migratiion,
									NLS.bind(Messages.MigrateCatActionDelegate_finished_with_problems,
											status.toString()));
						}
						targetFile.delete();
						return;
					}
					if (finalMessage == null)
						finalMessage = ""; //$NON-NLS-1$
					if (status.getSeverity() == IStatus.WARNING) {
						String message = status.getMessage();
						finalMessage += '\n' + message;
						severity = MessageDialog.WARNING;
					}
					AcousticMessageDialog finalDialog = new AcousticMessageDialog(getShell(),
							Messages.MigrateCatActionDelegate_catalog_migration, null, finalMessage, severity,
							new String[] { Messages.MigrateCatActionDelegate_export_preferences,
									Messages.MigrateCatActionDelegate_done },
							1);
					if (finalDialog.open() == 0) {
						ExportPreferencesWizard wizard = new ExportPreferencesWizard();
						wizard.init(getActiveWorkbenchWindow().getWorkbench(), null);
						new WizardDialog(getShell(), wizard).open();
					}
				} catch (InvocationTargetException e) {
					UiActivator.getDefault().logError(Messages.MigrateCatActionDelegate_error_migrating, e);
					targetFile.delete();
				} catch (InterruptedException e) {
					targetFile.delete();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					UiActivator.getDefault().postCatInit(false);
					CoreActivator.getDefault().getFileWatchManager().setPaused(false, this.getClass().toString());
				}
			}
		}
	}

}
