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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.cat.foreign.bibble.internal;

import java.io.File;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.bdaum.zoom.cat.foreign.internal.ImportForeignCatOperation;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.commands.AbstractCommandHandler;

@SuppressWarnings("restriction")
public class BibbleImportHandler extends AbstractCommandHandler {

	private String previousName;

	public void run() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		dialog.setFilterPath(previousName);
		dialog.setText(Messages.BibbleImportHandler_import);
		dialog.setMessage(Messages.BibbleImportHandler_select_folder);
		String folderName = dialog.open();
		if (folderName != null) {
			File file = new File(folderName, "base"); //$NON-NLS-1$
			if (file.exists()) {
				previousName = folderName;
				OperationJob.executeOperation(new ImportForeignCatOperation(
						file.getAbsolutePath(), new BibbleCatHandler(),
						UiActivator.getDefault().createImportConfiguration(this)),
						this);
			} else
				AcousticMessageDialog.openError(null, Messages.BibbleImportHandler_import_cat, NLS.bind(Messages.BibbleImportHandler_cat_not_valid, file));
		}
	}


}
