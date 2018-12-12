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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.ImportModeDialog;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

public class ImportFolderCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		UiActivator activator = UiActivator.getDefault();
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
		dialog.setText(Messages.ImportFolderAction_Import_source);
		dialog.setMessage(Messages.ImportFolderAction_import_folder_message);
		dialog.setFilterPath(UiActivator.getDefault().getInputFolderLocation());
		String foldername = dialog.open();
		if (foldername != null) {
			File folder = new File(foldername);
			File[] members = folder.listFiles();
			if (members != null && members.length > 0) {
				File parent = folder;
				boolean device = false;
				while (parent != null) {
					String name = parent.getName();
					if ("DCIM".equals(name)) { //$NON-NLS-1$
						device = true;
						break;
					}
					parent = parent.getParentFile();
				}
				activator.setInputFolderLocation(dialog.getFilterPath());
				ICore core = Core.getCore();
				File catRootFile = core.getVolumeManager().getRootFile(core.getDbManager().getFile());
				boolean foreignFolders = catRootFile == null
						|| !catRootFile.equals(core.getVolumeManager().getRootFile(folder));
				ImportModeDialog imDialog = new ImportModeDialog(getShell(), device || foreignFolders);
				if (imDialog.open() == ImportModeDialog.OK) {
					ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(null,
							new StorageObject[] { new StorageObject(folder) }, device && foreignFolders, false,
							imDialog.isNewStructure(), null, false);
					WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
					wizard.init(null, null);
					wizardDialog.open();
				}
			} else
				AcousticMessageDialog.openInformation(getShell(), Messages.ImportFolderCommand_import_folder,
						Messages.ImportFolderCommand_folder_empty);
		}
	}

}
