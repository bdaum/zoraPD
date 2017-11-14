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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.FilenameInputDialog;

@SuppressWarnings("restriction")
public class RenameCatalogCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		CoreActivator activator = CoreActivator.getDefault();
		Shell activeShell = getShell();
		IDbManager dbManager = activator.getDbManager();
		File file = dbManager.getFile();
		if (file == null)
			return;
		String fileName = file.getName();
		int p = fileName.lastIndexOf('.');
		final String name = p >= 0 ? fileName.substring(0, p) : fileName;
		FilenameInputDialog dialog = new FilenameInputDialog(activeShell, Messages.RenameCatActionDelegate_rename_cat,
				Messages.RenameCatActionDelegate_specify_new_name, name);
		if (dialog.open() == InputDialog.OK) {
			String newName = dialog.getValue();
			activator.fireCatalogClosed(CatalogListener.NORMAL);
			String folder = file.getParent();
			File newFile = new File(folder, newName + Constants.CATALOGEXTENSION);
			try {
				file.renameTo(newFile);
			} catch (Exception e1) {
				activator.logError(Messages.RenameCatActionDelegate_cannot_rename_cat, e1);
				return;
			}
			File indexFile = new File(folder, name + Constants.INDEXEXTENSION);
			if (indexFile.exists())
				try {
					indexFile.renameTo(new File(folder, newName + Constants.INDEXEXTENSION));
				} catch (Exception e) {
					activator.logError(Messages.RenameCatActionDelegate_cannot_rename_index, e);
					newFile.renameTo(file);
					return;
				}
			dbManager = activator.openDatabase(newFile.getAbsolutePath());
			activeShell.setText(Constants.APPLICATION_NAME + " - " //$NON-NLS-1$
					+ dbManager.getFileName());
			activator.setCatFile(newFile);
			activator.fireCatalogOpened(false);
		}
	}

}
