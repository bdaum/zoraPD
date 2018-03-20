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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;

@SuppressWarnings("restriction")
public class BackupCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
		try {
			dialog.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CoreActivator activator = CoreActivator.getDefault();
					monitor.beginTask(Messages.BackupAction_creating_backup, IProgressMonitor.UNKNOWN);
					activator.getDbManager().performBackup(0L, -1L, false, activator.getBackupGenerations());
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			AcousticMessageDialog.openError(getShell(), Messages.BackupAction_error_during_backup,
					e.getCause().getMessage());
			return;
		} catch (InterruptedException e) {
			// should not happen
		}
	}

}
