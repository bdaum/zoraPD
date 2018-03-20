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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;

public class ImportPreferencesCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText(Messages.ImportPreferencesAction_import_user_preferences);
		dialog.setFilterExtensions(new String[] { "*.zpf" }); //$NON-NLS-1$
		dialog.setFilterNames(new String[] { Messages.ImportPreferencesAction_uesr_preferences });
		String file = dialog.open();
		if (file != null) {
			if (AcousticMessageDialog.openConfirm(getShell(), Messages.ImportPreferencesAction_import_user_preferences,
					Messages.ImportPreferencesAction_do_you_really_want_to_overwrite)) {
				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					Platform.getPreferencesService().importPreferences(in);
				} catch (FileNotFoundException e) {
					// should never happen
				} catch (CoreException e) {
					AcousticMessageDialog.openError(getShell(),
							Messages.ImportPreferencesAction_import_user_preferences,
							NLS.bind(Messages.ImportPreferencesAction_error_during_import, e.getMessage()));
				} catch (IOException e) {
					AcousticMessageDialog.openError(getShell(),
							Messages.ImportPreferencesAction_import_user_preferences,
							NLS.bind(Messages.ImportPreferencesAction_io_error, e.getMessage()));
				}
			}
		}
	}

}
