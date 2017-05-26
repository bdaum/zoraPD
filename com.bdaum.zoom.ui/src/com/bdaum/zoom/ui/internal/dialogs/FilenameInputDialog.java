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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.dialogs.ZInputDialog;

public class FilenameInputDialog extends ZInputDialog {

	public FilenameInputDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, final String initialValue) {
		super(parentShell, dialogTitle, dialogMessage, initialValue,
				new IInputValidator() {
					public String isValid(String newText) {
						if (newText.equals(initialValue))
							return Messages.FilenameInputDialog_old_name;
						char c = BatchUtilities.checkFilename(newText);
						return (c >= 0) ? NLS.bind(
								Messages.FilenameInputDialog_bad_characters, c)
								: null;
					}
				}, true);
	}

}
