/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZListDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public class FileExtensionDialog extends ZTitleAreaDialog implements Listener {

	private static final String DIALOG_SETTINGS_SECTION = "FileExtensionDialogSettings"; //$NON-NLS-1$

	private String filename = ""; //$NON-NLS-1$

	private String initialValue, title, headerTitle, message, label;

	private Text filenameField;

	private Button okButton;

	/**
	 * Constructs a new file extension dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	public FileExtensionDialog(Shell parentShell) {
		this(parentShell, Messages.getString("FileExtensionDialog.Add_file_type"), //$NON-NLS-1$
				Messages.getString("FileExtensionDialog.Define_new_file_type"), //$NON-NLS-1$
				Messages.getString("FileExtensionDialog.Enter_file_suffixes"), //$NON-NLS-1$
				Messages.getString("FileExtensionDialog.File_suffixes")); //$NON-NLS-1$
	}

	/**
	 * Constructs a new file extension dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param title
	 *            the dialog title
	 * @param headerTitle
	 *            the dialog header
	 * @param message
	 *            the dialog message
	 * @param label
	 *            the label for the "file type" field
	 * @since 3.4
	 */
	public FileExtensionDialog(Shell parentShell, String title, String headerTitle, String message, String label) {
		super(parentShell);
		this.title = title;
		this.headerTitle = headerTitle;
		this.message = message;
		this.label = label;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(parentComposite, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setTitle(headerTitle);
		setMessage(message);
		new Label(contents, SWT.LEFT).setText(label);
		filenameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
		if (initialValue != null)
			filenameField.setText(initialValue);
		filenameField.addListener(SWT.Modify, this);
		filenameField.setFocus();

		Button browseButton = new Button(contents, SWT.PUSH);
		browseButton.setText(Messages.getString("FileExtensionDialog.Browse")); //$NON-NLS-1$
		browseButton.addListener(SWT.Selection, this);
		Dialog.applyDialogFont(parentComposite);
		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(3).margins(defaultMargins.x, defaultMargins.y)
				.generateLayout(contents);
		return contents;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private boolean validateFileType() {
		//TODO We need kernel api to validate the extension or a filename
		// check for empty name and extension
		if (filename.isEmpty()) {
			setErrorMessage(null);
			return false;
		}
		StringTokenizer st = new StringTokenizer(filename, ";"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			// check for empty extension if there is no name
			int index = token.lastIndexOf('.');
			if (index == token.length() - 1 && (index == 0 || (index == 1 && token.charAt(0) == '*'))) {
				setErrorMessage(Messages.getString("FileExtensionDialog.File_extensions_must_not_be_empty")); //$NON-NLS-1$
				return false;
			}

			// check for characters before *
			// or no other characters
			// or next chatacter not '.'
			// or another *
			index = token.indexOf('*');
			if (index > -1) {
				if (token.length() == 1) {
					setErrorMessage(Messages.getString("FileExtensionDialog.File_extensions_must_not_be_empty")); //$NON-NLS-1$
					return false;
				}
				if (index != 0 || token.charAt(1) != '.') {
					setErrorMessage(Messages.getString("FileExtensionDialog.File_names_can_only")); //$NON-NLS-1$
					return false;
				}
				if (token.length() > index && token.indexOf('*', index + 1) != -1) {
					setErrorMessage(Messages.getString("FileExtensionDialog.File_names_can_only")); //$NON-NLS-1$
					return false;
				}
			}
		}
		setErrorMessage(null);
		return true;
	}

	/**
	 * Get the extension.
	 *
	 * @return the extension
	 */
	public String[] getExtensions() {
		List<String> sb = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(filename, ";"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int index = token.lastIndexOf('.');
			if (index >= 0 && index < token.length()) {
				sb.add(token.substring(index + 1));
			}
		}
		return sb.toArray(new String[sb.size()]);
	}

	/**
	 * Sets the initial value that should be prepopulated in this dialog.
	 *
	 * @param initialValue
	 *            the value to be displayed to the user
	 * @since 3.4
	 */
	public void setInitialValue(String initialValue) {
		this.initialValue = initialValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return UiActivator.getDefault().getDialogSettings(DIALOG_SETTINGS_SECTION);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.Modify) {
			filename = filenameField.getText().trim();
			okButton.setEnabled(validateFileType());
		} else {
			ListDialog dialog = new ZListDialog(getShell(), SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			dialog.setTitle(Messages.getString("FileExtensionDialog.registered_file_types"));//$NON-NLS-1$
			dialog.setContentProvider(ArrayContentProvider.getInstance());
			dialog.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
			String[] extensions = Program.getExtensions();
			List<String> list = new ArrayList<String>(extensions.length);
			for (String s : extensions)
				if (s.indexOf(' ') < 0)
					list.add(s);
			extensions = list.toArray(new String[list.size()]);
			dialog.setInput(extensions);
			dialog.setMessage(Messages.getString("FileExtensionDialog.Select_registered_file_types")); //$NON-NLS-1$
			if (dialog.open() == Window.OK) {
				Object[] result = dialog.getResult();
				if (result != null && result.length > 0) {
					StringBuilder sb = new StringBuilder(filenameField.getText());
					for (Object object : result) {
						if (sb.length() > 0)
							sb.append(';');
						sb.append('*').append(object);
					}
					filenameField.setText(sb.toString());
				}
			}
		}
	}
}
