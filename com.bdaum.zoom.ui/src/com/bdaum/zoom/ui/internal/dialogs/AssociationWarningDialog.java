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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.preferences.FileAssociationsPreferencePage;

public class AssociationWarningDialog extends AcousticMessageDialog {

	private final String extension;

	public AssociationWarningDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String extension) {
		super(parentShell, dialogTitle, null, dialogMessage, WARNING,
				new String[] {
						Messages.AssociationWarningDialog_Configure_file_assos,
						IDialogConstants.CANCEL_LABEL }, 0);
		this.extension = extension;
	}

	
	@Override
	public int open() {
		int ret = super.open();
		if (ret == 0) {
			close();
			PreferencesUtil.createPreferenceDialogOn(getShell(),
					FileAssociationsPreferencePage.ID, new String[0],
					extension).open();
		}
		return ret;
	}
}
