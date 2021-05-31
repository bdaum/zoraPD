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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.db.NullDbManager;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;

@SuppressWarnings("restriction")
public class NewCatalogCommand extends AbstractCatCommandHandler {

	@Override
	public void run() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(Messages.NewCatAction_Create_new_cat);
		if (catFile != null) {
			dialog.setFilterPath(catFile.getParent());
			dialog.setFileName(catFile.getName());
		}
		dialog.setFilterExtensions(UiActivator.getDefault().getCatFileExtensions());
		dialog.setFilterNames(UiActivator.getDefault().getSupportedCatFileNames());
		dialog.setOverwrite(true);
		dialog.setFileName("*" + Constants.CATALOGEXTENSION); //$NON-NLS-1$
		final String file = dialog.open();
		if (file != null) {
			IDbManager dbManager = CoreActivator.getDefault().getDbManager();
			if (new File(file).equals(dbManager.getFile())) {
				AcousticMessageDialog.openWarning(getShell(), Messages.NewCatAction_Create_new_cat,
						Messages.NewCatalogCommand_already_open);
				return;
			}
			final Meta meta = (dbManager instanceof NullDbManager) ? null : dbManager.getMeta(false);
			if (preCatClose(false))
				try {
					BusyIndicator.showWhile(getShell().getDisplay(), () -> {
						IDbManager db = CoreActivator.getDefault().openDatabase(file, true, null);
						postCatOpen(db.getFileName(), true);
						new EditMetaDialog(getShell(), getActiveWorkbenchWindow().getActivePage(), db, true, meta)
								.open();
						postCatInit(false);
					});
				} catch (IllegalStateException e) {
					AcousticMessageDialog.openError(getShell(), Messages.NewCatAction_Operations_running,
							e.getMessage());
				}
		}
	}

}
