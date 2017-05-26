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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.widgets.IInputAdvisor;

/**
 * This class adapts the Eclipse InputDialog
 *
 */
public class ZInputDialog extends InputDialog {

	/**
	 * true if the catalog is read-only
	 */
	protected boolean readonly;

	private final boolean alwaysEnabled;
	private final IInputAdvisor advisor;

	/**
	 * Creates an input dialog with OK and Cancel buttons. Note that the dialog
	 * will have no visual representation (no widgets) until it is told to open.
	 * <p>
	 * Note that the <code>open</code> method blocks for input dialogs.
	 * </p>
	 *
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level
	 *            shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message, or <code>null</code> if none
	 * @param initialValue
	 *            the initial input value, or <code>null</code> if none
	 *            (equivalent to the empty string)
	 * @param validator
	 *            an input validator, or <code>null</code> if none
	 * @param alwaysEnabled
	 *            - true if the OK button is always enabled by default, false if
	 *            it only enabled for writable catalogs
	 */

	public ZInputDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue,
			IInputValidator validator, boolean alwaysEnabled) {
		this(parentShell, dialogTitle, dialogMessage, initialValue, null,
				validator, alwaysEnabled);
	}

	/**
	 * Creates an input dialog with OK and Cancel buttons. Note that the dialog
	 * will have no visual representation (no widgets) until it is told to open.
	 * <p>
	 * Note that the <code>open</code> method blocks for input dialogs.
	 * </p>
	 *
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level
	 *            shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message, or <code>null</code> if none
	 * @param initialValue
	 *            the initial input value, or <code>null</code> if none
	 *            (equivalent to the empty string)
	 * @param advisor
	 *            in input adviosr, or <code>null</code> if none
	 * @param validator
	 *            an input validator, or <code>null</code> if none
	 * @param alwaysEnabled
	 *            - true if the OK button is always enabled by default, false if
	 *            it only enabled for writable catalogs
	 */
	public ZInputDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputAdvisor advisor,
			IInputValidator validator, boolean alwaysEnabled) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.advisor = advisor;
		this.alwaysEnabled = alwaysEnabled;
		readonly = Core.getCore().getDbManager().isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		CssActivator.getDefault().setColors(getShell());
		updateButtons();
	}

	private void updateButtons() {
		boolean enabled = alwaysEnabled || !readonly;
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.InputDialog#validateInput()
	 */
	@Override
	protected void validateInput() {
		String errorMessage = null;
		String text = getText().getText();
		if (getValidator() != null)
			errorMessage = getValidator().isValid(text);
		setErrorMessage(errorMessage);
		if (errorMessage == null) {
			setErrorMessage((advisor != null) ? advisor.getAdvice(text) : null);
			getShell().setModified(true);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

}
