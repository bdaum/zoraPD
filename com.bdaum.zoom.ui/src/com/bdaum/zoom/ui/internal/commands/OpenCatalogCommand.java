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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;

@SuppressWarnings("restriction")
public class OpenCatalogCommand extends AbstractCatCommandHandler {

	@Override
	public void run() {
		try {
			if (catFile == null) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.OpenCatAction_Open_existing);
				UiActivator activator = UiActivator.getDefault();
				dialog.setFilterExtensions(activator.getCatFileExtensions());
				dialog.setFilterNames(activator.getSupportedCatFileNames());
				String path = dialog.open();
				if (path != null) {
					catFile = new File(path);
					File currentFile =  CoreActivator.getDefault().getDbManager().getFile();
					if (currentFile != null && currentFile.equals(catFile)) {
						AcousticMessageDialog.openWarning(getShell(), Messages.OpenCatAction_Open_existing,
								Messages.OpenCatalogCommand_already_open);
						return;
					}
				}
			}
			if (catFile != null) {
				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						if (preCatClose(false))
							try {
								IDbManager db = CoreActivator.getDefault().openDatabase(catFile.getAbsolutePath());
								postCatOpen(db.getFileName(), false);
								postCatInit(false);
							} catch (IllegalStateException e) {
								AcousticMessageDialog.openError(getShell(), Messages.OpenCatAction_Operations_running,
										e.getMessage());
							}
					}
				});
			}
		} finally {
			catFile = null;
		}

	}

}
