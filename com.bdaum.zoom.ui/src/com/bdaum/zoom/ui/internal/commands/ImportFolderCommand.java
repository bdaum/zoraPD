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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.ImportModeDialog;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

@SuppressWarnings("restriction")
public class ImportFolderCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		UiActivator activator = UiActivator.getDefault();
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
		dialog.setText(Messages.ImportFolderAction_Import_source);
		dialog.setMessage(Messages.ImportFolderAction_import_folder_message);
		dialog.setFilterPath(UiActivator.getDefault().getInputFolderLocation());
		String filename = dialog.open();
		if (filename != null) {
			boolean device = false;
			File file = new File(filename);
			File parent = file;
			while (parent != null) {
				String name = parent.getName();
				if ("DCIM".equals(name)) { //$NON-NLS-1$
					device = true;
					break;
				}
				parent = parent.getParentFile();
			}
			ICore core = Core.getCore();
			File catRootFile = core.getVolumeManager().getRootFile(core.getDbManager().getFile());
			boolean foreignFolders = catRootFile == null
					|| !catRootFile.equals(core.getVolumeManager().getRootFile(file));
			ImportModeDialog imDialog = new ImportModeDialog(getShell(), device || foreignFolders);
			if (imDialog.open() != ImportModeDialog.OK)
				return;
			if (imDialog.isNewStructure()) {
				ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(null, new File[] { file },
						device && foreignFolders, false);
				WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
				wizard.init(null, null);
				wizardDialog.open();
				return;
			}

			activator.setInputFolderLocation(dialog.getFilterPath());
			File folder = new File(filename);
			File[] members = folder.listFiles();
			if (members.length > 0)
				OperationJob.executeOperation(new ImportOperation(new FileInput(members, true),
						activator.createImportConfiguration(this), null, new File[] { folder }), this);
		}
	}

}
