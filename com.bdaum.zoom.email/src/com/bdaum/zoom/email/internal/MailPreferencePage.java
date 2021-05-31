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
 * (c) 2021 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.util.Collections;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.Password;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class MailPreferencePage extends AbstractPreferencePage implements Listener {

	private CheckboxButton clientButton;
	private Text hostUrlField, userIdField, senderField, signatureField;
	private Password passwordField;
	private RadioButtonGroup securityGroup, portGroup;
	private FileEditor vcardField;
	private CGroup hostGroup, authGroup, senderGroup;
	private Button testButton;

	public MailPreferencePage() {
		setDescription(NLS.bind("Setup for {0}s own mail client", Constants.APPNAME)); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createPageContents(Composite parent) {
		setHelp(HelpContextIds.MAIL_PREFERENCES);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		clientButton = WidgetFactory.createCheckButton(composite, Messages.MailPreferencePage_use_platform, null);
		clientButton.addListener(SWT.Selection, this);
		createPropertiesGroup(composite);
		fillValues();
		updateFields();
	}

	private void createPropertiesGroup(Composite composite) {
		hostGroup = CGroup.create(composite, 1, Messages.MailPreferencePage_smtp_host);
		new Label(hostGroup, SWT.NONE).setText(Messages.MailPreferencePage_host_address);
		hostUrlField = new Text(hostGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		hostUrlField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostUrlField.addListener(SWT.Modify, this);
		new Label(hostGroup, SWT.NONE).setText(Messages.MailPreferencePage_port);
		portGroup = new RadioButtonGroup(hostGroup, null, SWT.HORIZONTAL, "587", "465", "2525", "25"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		new Label(hostGroup, SWT.NONE).setText(Messages.MailPreferencePage_security);
		securityGroup = new RadioButtonGroup(hostGroup, null, SWT.HORIZONTAL, Messages.MailPreferencePage_none, "SSL", //$NON-NLS-1$
				"StartTls"); //$NON-NLS-1$
		authGroup = CGroup.create(composite, 1, Messages.MailPreferencePage_authentif);
		new Label(authGroup, SWT.NONE).setText(Messages.MailPreferencePage_user_id);
		userIdField = new Text(authGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		userIdField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		userIdField.addListener(SWT.Modify, this);
		new Label(authGroup, SWT.NONE).setText(Messages.MailPreferencePage_password);
		passwordField = new Password(authGroup, SWT.NONE);
		passwordField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		passwordField.addListener(SWT.Modify, this);
		senderGroup = CGroup.create(composite, 1, Messages.MailPreferencePage_sender);
		new Label(senderGroup, SWT.NONE).setText(Messages.MailPreferencePage_email_adr);
		senderField = new Text(senderGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		senderField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		senderField.addListener(SWT.Modify, this);
		Label label = new Label(senderGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		label.setText(Messages.MailPreferencePage_signature);
		signatureField = new Text(senderGroup, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 50;
		signatureField.setLayoutData(layoutData);
		new Label(senderGroup, SWT.NONE).setText("vCard"); //$NON-NLS-1$
		vcardField = new FileEditor(senderGroup, SWT.OPEN, Messages.MailPreferencePage_select_vcard, false,
				new String[] { "*.vcf;*.VCF" }, //$NON-NLS-1$
				new String[] { Messages.MailPreferencePage_vcard_files }, null, null, false, null);
		vcardField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		testButton = new Button(composite, SWT.PUSH);
		testButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		testButton.setText(Messages.MailPreferencePage_test);
		testButton.addListener(SWT.Selection, this);

	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		clientButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.PLATFORMCLIENT));
		hostUrlField.setText(preferenceStore.getString(PreferenceConstants.HOSTURL));
		int port = preferenceStore.getInt(PreferenceConstants.PORT);
		portGroup.setSelection(port == 465 ? 1 : port == 2525 ? 2 : port == 25 ? 3 : 0);
		String sec = preferenceStore.getString(PreferenceConstants.SECURITY);
		securityGroup.setSelection(
				PreferenceConstants.SSL.equals(sec) ? 1 : PreferenceConstants.STARTTLS.equals(sec) ? 2 : 0);
		userIdField.setText(preferenceStore.getString(PreferenceConstants.USER));
		String decode = CommonUtilities.decode(preferenceStore.getString(PreferenceConstants.PASSWORD));
		if (decode != null)
			passwordField.setText(decode);
		senderField.setText(preferenceStore.getString(PreferenceConstants.SENDER));
		signatureField.setText(preferenceStore.getString(PreferenceConstants.SIGNATURE));
		vcardField.setText(preferenceStore.getString(PreferenceConstants.VCARD));
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.PLATFORMCLIENT,
				preferenceStore.getDefaultBoolean(PreferenceConstants.PLATFORMCLIENT));
		preferenceStore.setValue(PreferenceConstants.HOSTURL,
				preferenceStore.getDefaultString(PreferenceConstants.HOSTURL));
		preferenceStore.setValue(PreferenceConstants.USER, preferenceStore.getDefaultString(PreferenceConstants.USER));
		preferenceStore.setValue(PreferenceConstants.PASSWORD,
				preferenceStore.getDefaultString(PreferenceConstants.PASSWORD));
		preferenceStore.setValue(PreferenceConstants.SENDER,
				preferenceStore.getDefaultString(PreferenceConstants.SENDER));
		preferenceStore.setValue(PreferenceConstants.PORT, preferenceStore.getDefaultInt(PreferenceConstants.PORT));
		preferenceStore.setValue(PreferenceConstants.SECURITY,
				preferenceStore.getDefaultString(PreferenceConstants.SECURITY));
		preferenceStore.setValue(PreferenceConstants.SIGNATURE,
				preferenceStore.getDefaultString(PreferenceConstants.SIGNATURE));
		preferenceStore.setValue(PreferenceConstants.VCARD,
				preferenceStore.getDefaultString(PreferenceConstants.VCARD));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean useClient = clientButton.getSelection();
		preferenceStore.setValue(PreferenceConstants.PLATFORMCLIENT, useClient);
		if (!useClient) {
			preferenceStore.setValue(PreferenceConstants.HOSTURL, hostUrlField.getText());
			switch (portGroup.getSelection()) {
			case 1:
				preferenceStore.setValue(PreferenceConstants.PORT, 465);
				break;
			case 2:
				preferenceStore.setValue(PreferenceConstants.PORT, 2525);
				break;
			case 3:
				preferenceStore.setValue(PreferenceConstants.PORT, 25);
				break;
			default:
				preferenceStore.setValue(PreferenceConstants.PORT, 587);
			}
			switch (securityGroup.getSelection()) {
			case 1:
				preferenceStore.setValue(PreferenceConstants.SECURITY, PreferenceConstants.SSL);
				break;
			case 2:
				preferenceStore.setValue(PreferenceConstants.SECURITY, PreferenceConstants.STARTTLS);
				break;
			default:
				preferenceStore.setValue(PreferenceConstants.SECURITY, ""); //$NON-NLS-1$
			}
			preferenceStore.setValue(PreferenceConstants.USER, userIdField.getText());
			String encode = CommonUtilities.encode(passwordField.getText());
			if (encode != null)
				preferenceStore.setValue(PreferenceConstants.PASSWORD, encode);
			preferenceStore.setValue(PreferenceConstants.SENDER, senderField.getText());
			preferenceStore.setValue(PreferenceConstants.SIGNATURE, signatureField.getText());
			preferenceStore.setValue(PreferenceConstants.VCARD, vcardField.getText());
		}
	}

	@Override
	protected String doValidate() {
		if (!clientButton.getSelection()) {
			if (hostUrlField.getText().isEmpty())
				return Messages.MailPreferencePage_no_host;
			if (userIdField.getText().isEmpty())
				return Messages.MailPreferencePage_no_user;
			if (passwordField.getText().isEmpty())
				return Messages.MailPreferencePage_no_password;
			if (senderField.getText().isEmpty())
				return Messages.MailPreferencePage_no_sender;
			try {
				new InternetAddress(senderField.getText()).validate();
			} catch (AddressException ex) {
				return Messages.MailPreferencePage_bad_sender;
			}
		}
		return null;
	}

	private void updateFields() {
		boolean visible = !clientButton.getSelection();
		hostGroup.setVisible(visible);
		authGroup.setVisible(visible);
		senderGroup.setVisible(visible);
		testButton.setVisible(visible);
	}

	public void handleEvent(Event event) {
		if (event.widget == testButton) {
			BusyIndicator.showWhile(getShell().getDisplay(), () -> sendTestMessage());
			return;
		}
		validate();
		updateFields();
	}

	private void sendTestMessage() {
		IStatus status = Activator.getDefault().sendMail(Collections.singletonList(senderField.getText()),
				Collections.emptyList(), Collections.emptyList(), Messages.MailPreferencePage_test,
				NLS.bind(Messages.MailPreferencePage_test_msg, Constants.APPLICATION_NAME), null);
		if (status.isOK()) {
			AcousticMessageDialog.openInformation(getShell(), Messages.MailPreferencePage_email_sent,
					Messages.MailPreferencePage_sent_msg);
			return;
		}
		String msg = status.getMessage();
		if (status.isMultiStatus()) {
			StringBuilder sb = new StringBuilder();
			IStatus[] children = ((MultiStatus) status).getChildren();
			for (IStatus child : children) {
				if (sb.length() > 0)
					sb.append("; "); //$NON-NLS-1$
				sb.append(child.getMessage());
				Throwable exception = child.getException();
				if (exception != null) {
					String message = exception.getLocalizedMessage();
					if (message != null)
						sb.append(": ").append(message); //$NON-NLS-1$
				}
			}
			msg = sb.toString();
		}
		AcousticMessageDialog.openError(getShell(), Messages.MailPreferencePage_email_failed, msg);
	}

	@Override
	public void setValid(boolean valid) {
		testButton.setEnabled(valid);
		super.setValid(valid);
	}

}
