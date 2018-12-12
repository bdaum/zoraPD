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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

public class ImportDeviceCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> doRun());
	}

	public void doRun() {
		StorageObject[] dcims;
		while (true) {
			dcims = Core.getCore().getVolumeManager().findDCIMs();
			if (dcims.length > 0)
				break;
			MessageDialog dialog = new AcousticMessageDialog(getShell(),
					Messages.ImportFromDeviceAction_Import_from_device, null,
					Messages.ImportFromDeviceAction_there_seems_no_suitable_device, MessageDialog.QUESTION,
					new String[] { IDialogConstants.RETRY_LABEL, IDialogConstants.CANCEL_LABEL }, 1);
			if (dialog.open() > 0)
				return;
		}
		ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(null, dcims, true, true, true, null, false);
		WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
		wizard.init(null, null);
		wizardDialog.open();
	}

}
