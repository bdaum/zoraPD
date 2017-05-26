/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.lightzone.internal;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

@SuppressWarnings("restriction")
public class UpdateDerivedDialog extends AcousticMessageDialog {

	private CheckboxButton askButton;
	private boolean ask;

	public UpdateDerivedDialog(Shell parentShell, File file) {
		super(
				parentShell,
				Constants.APPLICATION_NAME,
				null,
				NLS.bind(
						Messages.UpdateDerivedDialog_keeping_consistent,
						file), AcousticMessageDialog.WARNING,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL }, 0);
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		askButton = WidgetFactory.createCheckButton(composite,
				Messages.UpdateDerivedDialog_dont_ask_again, new GridData(SWT.CENTER, SWT.CENTER, true,
						false));
		return composite;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		ask = askButton.getSelection();
		super.buttonPressed(buttonId);
	}

	public boolean getAsk() {
		return ask;
	}
}
