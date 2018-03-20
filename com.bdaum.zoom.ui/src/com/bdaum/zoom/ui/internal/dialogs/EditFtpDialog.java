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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

public class EditFtpDialog extends ZTitleAreaDialog implements IAdaptable {

	private FtpAccount account;
	private Text nameField;
	private Text hostField;
	private Text passwordField;
	private Text loginField;
	private CheckboxButton anonymousButton;
	private CheckboxButton passiveButton;
	private Text targetDirField;
	private Text webHostField;
	private Text prefixField;
	private CheckedText notesField;
	private Spinner portField;
	private Button testFtpButton;
	private Text accountField;
	private Button testUrlButton;
	private CheckboxButton trackField;
	private final boolean adhoc;
	private final String errorMessage;

	public EditFtpDialog(Shell parentShell, FtpAccount account, boolean adhoc, String errorMessage) {
		super(parentShell, HelpContextIds.EDIT_FTP_DIALOG);
		this.account = account;
		this.adhoc = adhoc;
		this.errorMessage = errorMessage;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.EditFtpDialog_ftp_account_details);
		fillValues();
		updateButtons();
		setErrorMessage(errorMessage);
	}

	private void fillValues() {
		if (account != null) {
			setCondText(nameField, account.getName());
			setCondText(hostField, account.getHost());
			setCondText(passwordField, account.getPassword());
			setCondText(loginField, account.getLogin());
			setCondText(accountField, account.getSubAccount());
			setCondText(targetDirField, account.getDirectory());
			setCondText(webHostField, account.getWebHost());
			setCondText(prefixField, account.getPrefix());
			if (notesField != null)
				notesField.setText(account.getNotes());
			if (trackField != null)
				trackField.setSelection(account.isTrackExport());
			anonymousButton.setSelection(account.isAnonymous());
			passiveButton.setSelection(account.isPassiveMode());
			portField.setSelection(account.getPort());
		}
	}

	private void updateButtons() {
		Button okbutton = getButton(IDialogConstants.OK_ID);
		boolean valid = validate();
		testFtpButton.setEnabled(valid);
		boolean validUrl = true;
		if (testUrlButton != null && webHostField != null) {
			validUrl = validateUrl();
			testUrlButton.setEnabled(!webHostField.getText().isEmpty() && validUrl);
		}
		boolean enabled = valid && validUrl;
		getShell().setModified(enabled);
		okbutton.setEnabled(enabled);
		boolean login = !anonymousButton.getSelection();
		loginField.setEnabled(login);
		passwordField.setEnabled(login);
		if (valid && validUrl)
			setErrorMessage(null);
	}

	@SuppressWarnings("unused")
	private boolean validateUrl() {
		String wh = webHostField.getText();
		if (!wh.isEmpty()) {
			wh = Core.furnishWebUrl(wh);
			String pfx = prefixField.getText();
			String dir = targetDirField.getText();
			if (!pfx.isEmpty() && dir.startsWith(pfx))
				dir = dir.substring(pfx.length());
			while (dir.startsWith("/")) //$NON-NLS-1$
				dir = dir.substring(1);
			wh += "/" + dir; //$NON-NLS-1$

			try {
				new URL(wh);
			} catch (MalformedURLException e) {
				setErrorMessage(NLS.bind(Messages.EditFtpDialog_bad_url, wh));
				return false;
			}
		}
		return true;
	}

	private boolean validate() {
		setMessage(Messages.EditFtpDialog_please_fill_in_details);
		String msg = null;
		if (nameField != null && nameField.getText().isEmpty())
			msg = Messages.EditFtpDialog_please_specify_an_account_name;
		else if (hostField.getText().isEmpty())
			msg = Messages.EditFtpDialog_please_specify_a_host_name;
		else if ((loginField.getText().isEmpty() || passwordField.getText().isEmpty())
				&& !anonymousButton.getSelection())
			msg = Messages.EditFtpDialog_please_specify_login_name;
		else if (targetDirField.getText().isEmpty())
			msg = Messages.EditFtpDialog_please_specify_target_dir;
		else if (prefixField != null && !prefixValid())
			msg = Messages.EditFtpDialog_target_dir_does_not_start_with_prefix;
		if (msg != null)
			setErrorMessage(msg);
		return msg == null;
	}

	private boolean prefixValid() {
		String pfx = prefixField.getText();
		if (pfx.isEmpty())
			return true;
		String dir = targetDirField.getText();
		if (!dir.startsWith(pfx))
			return false;
		if (pfx.length() == dir.length())
			return true;
		return dir.charAt(pfx.length()) == '/';
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		CTabFolder tabFolder = new CTabFolder(composite, SWT.TOP);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOverviewGroup(UiUtilities.createTabPage(tabFolder, Messages.EditFtpDialog_general, null));
		createConnectionGroup(UiUtilities.createTabPage(tabFolder, Messages.EditFtpDialog_connection, null));
		if (!adhoc)
			createAdvancedGroup(UiUtilities.createTabPage(tabFolder, Messages.EditFtpDialog_web, null));
		return area;
	}

	@SuppressWarnings("unused")
	private void createOverviewGroup(Composite comp) {
		comp.setLayout(new GridLayout(1, false));
		CGroup group1 = UiUtilities.createGroup(comp, 2, Messages.EditFtpDialog_main_details);
		int style = SWT.READ_ONLY;
		if (!adhoc) {
			nameField = createTextField(group1, Messages.EditFtpDialog_account_name, 150, SWT.NONE);
			style = SWT.NONE;
		}
		hostField = createTextField(group1, Messages.EditFtpDialog_host, -1, style);
		targetDirField = createTextField(group1, Messages.EditFtpDialog_target_dir, -1, style);
		targetDirField.addVerifyListener(fileVerifyListener);
		if (!adhoc)
			trackField = WidgetFactory.createCheckButton(group1, Messages.EditFtpDialog_track_exports,
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		CGroup group2 = UiUtilities.createGroup(comp, 3, Messages.EditFtpDialog_access_id);
		loginField = createTextField(group2, Messages.EditFtpDialog_login, -1, SWT.NONE);
		new Label(group2, SWT.NONE);
		passwordField = createTextField(group2, Messages.EditFtpDialog_password, -1, SWT.PASSWORD);
		new Label(group2, SWT.NONE);
		accountField = createTextField(group2, Messages.EditFtpDialog_account, 150, SWT.NONE);
		anonymousButton = WidgetFactory.createCheckButton(group2, Messages.EditFtpDialog_anonymous,
				new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		anonymousButton.addSelectionListener(selectionListener);
		testFtpButton = new Button(group2, SWT.PUSH);
		testFtpButton.setText(Messages.EditFtpDialog_test);
		testFtpButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 3, 1));
		testFtpButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				testFtp();
			}
		});
	}

	protected void testFtp() {
		createAccount();
		String msg = account.test(this);
		if (msg == null) {
			setMessage(Messages.EditFtpDialog_ftp_account_tested);
			setErrorMessage(null);
		} else
			setErrorMessage(msg);

	}

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};

	private VerifyListener fileVerifyListener = new VerifyListener() {

		public void verifyText(VerifyEvent e) {
			e.doit = "\"*:<>?\\|[]".indexOf(e.character) < 0; //$NON-NLS-1$
		}
	};

	private SelectionListener selectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			updateButtons();
		}
	};

	private Text createTextField(Composite g, String text, int width, int style) {
		Label label = new Label(g, SWT.NONE);
		label.setText(text);
		Text textField = new Text(g, SWT.SINGLE | SWT.LEAD | SWT.BORDER | style);
		if (width <= 0)
			textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		else
			textField.setLayoutData(new GridData(width, -1));
		textField.addModifyListener(modifyListener);
		return textField;
	}

	private void createConnectionGroup(Composite comp) {
		comp.setLayout(new GridLayout(2, false));
		passiveButton = WidgetFactory.createCheckButton(comp, Messages.EditFtpDialog_passive_mode,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		passiveButton.addSelectionListener(selectionListener);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.EditFtpDialog_port);
		portField = new Spinner(comp, SWT.BORDER);
		portField.setMaximum(9999);
		portField.addSelectionListener(selectionListener);
		portField.setEnabled(!adhoc);
	}

	private void createAdvancedGroup(Composite comp) {
		comp.setLayout(new GridLayout(1, false));
		CGroup group1 = UiUtilities.createGroup(comp, 2, Messages.EditFtpDialog_web_host);
		webHostField = createTextField(group1, Messages.EditFtpDialog_host_web, -1, SWT.NONE);
		prefixField = createTextField(group1, Messages.EditFtpDialog_prefix, -1, SWT.NONE);
		prefixField.addVerifyListener(fileVerifyListener);
		testUrlButton = new Button(group1, SWT.PUSH);
		testUrlButton.setText(Messages.EditFtpDialog_test_url);
		testUrlButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
		testUrlButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testUrl();
			}
		});

		CGroup group2 = UiUtilities.createGroup(comp, 1, Messages.EditFtpDialog_notes);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 70;
		notesField = new CheckedText(group2, SWT.MULTI | SWT.LEAD);
		notesField.setLayoutData(layoutData);
	}

	protected void testUrl() {
		createAccount();
		setErrorMessage(account.testWebUrl());
	}

	@Override
	protected void okPressed() {
		createAccount();
		super.okPressed();
	}

	private void createAccount() {
		if (account == null)
			account = new FtpAccount();
		if (nameField != null)
			account.setName(nameField.getText());
		account.setHost(hostField.getText());
		account.setPassword(passwordField.getText());
		account.setLogin(loginField.getText());
		account.setSubAccount(accountField.getText());
		account.setDirectory(targetDirField.getText());
		if (webHostField != null)
			account.setWebHost(webHostField.getText());
		if (prefixField != null)
			account.setPrefix(prefixField.getText());
		if (notesField != null)
			account.setNotes(notesField.getText());
		account.setAnonymous(anonymousButton.getSelection());
		account.setPassiveMode(passiveButton.getSelection());
		account.setPort(portField.getSelection());
		if (trackField != null)
			account.setTrackExport(trackField.getSelection());
	}

	public FtpAccount getResult() {
		return account;
	}

}
