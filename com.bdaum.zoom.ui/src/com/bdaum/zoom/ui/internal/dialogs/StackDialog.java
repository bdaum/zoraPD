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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class StackDialog extends ZTitleAreaDialog implements Listener {

	private static final char[] BADCHARS = new char[] { '#', '!', '=', '/', '[', ']', '{', '}', '"', ':', ';', ',', '?',
			'*', '\\', '<', '>', '|', '&' };
	private Combo nameField;
	private final String[] names;
	private String name;

	public StackDialog(Shell shell, String[] names) {
		super(shell);
		this.names = names;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.StackDialog_define_stack_name);
		setMessage(Messages.StackDialog_define_stack_name_msg);
		nameField.setVisibleItemCount(8);
		nameField.setItems(names);
		if (names.length > 0)
			nameField.setText(names[0]);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.StackDialog_stack_name);
		nameField = new Combo(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addListener(SWT.Modify, this);
		nameField.addListener(SWT.Selection, this);
		return area;
	}

	private boolean validate() {
		String errorMessage = null;
		String name = nameField.getText();
		if (name.isEmpty())
			errorMessage = Messages.StackDialog_please_define;
		else
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				for (char c2 : BADCHARS)
					if (c == c2) {
						errorMessage = NLS.bind(Messages.StackDialog_must_not_contain, String.valueOf(c));
						break;
					}
			}
		setErrorMessage(errorMessage);
		return errorMessage == null;
	}

	private void updateButtons() {
		getButton(OK).setEnabled(validate());
	}

	@Override
	protected void okPressed() {
		name = nameField.getText();
		super.okPressed();
	}

	public String getName() {
		return name;
	}

	@Override
	public void handleEvent(Event event) {
		updateButtons();
	}

}
