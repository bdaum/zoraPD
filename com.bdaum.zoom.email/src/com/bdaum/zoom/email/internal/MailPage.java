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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.email.internal.job.EmailJob;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class MailPage extends ColoredWizardPage implements Listener {

	private CheckedText subjectField, messageField;
	private Combo toField, ccField, bccField;

	public MailPage() {
		super("mail", Messages.MailPage_mail_props, null); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean platformClient = preferenceStore.getBoolean(PreferenceConstants.PLATFORMCLIENT);
		if (!platformClient) {
			CGroup recipGroup = UiUtilities.createGroup(composite, 1, Messages.MailPage_recipients);
			toField = createCombo(recipGroup, Messages.MailPage_to, "to"); //$NON-NLS-1$
			ccField = createCombo(recipGroup, Messages.MailPage_cc, "cc"); //$NON-NLS-1$
			bccField = createCombo(recipGroup, Messages.MailPage_bcc, "bcc"); //$NON-NLS-1$
		}
		CGroup textGroup = UiUtilities.createGroup(composite, 1, Messages.SendEmailPage_Message);
		new Label(textGroup, SWT.NONE).setText(Messages.SendEmailPage_Subject);
		subjectField = new CheckedText(textGroup, SWT.BORDER);
		subjectField.setSpellingOptions(8, ISpellCheckingService.TITLEOPTIONS);
		subjectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		subjectField.addListener(SWT.Modify, this);
		final Label messageLabel = new Label(textGroup, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		messageLabel.setText(Messages.MailPage_text);
		messageField = new CheckedText(textGroup, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 200;
		messageField.setLayoutData(data);
		setControl(composite);

		String imageList = ((IMailWizard) getWizard()).getImageList();
		StringBuilder sb = new StringBuilder(200);
		sb.append(imageList);
		String vcard = preferenceStore.getString(PreferenceConstants.VCARD);
		if (!vcard.isEmpty()) {
			File file = new File(vcard);
			if (file.exists())
				sb.append("\n ").append(file.getName()); //$NON-NLS-1$
		}
		String signature = preferenceStore.getString(PreferenceConstants.SIGNATURE);
		if (!signature.isEmpty())
			sb.append("\n\n\n").append(signature); //$NON-NLS-1$
		messageField.setText(sb.toString());
		super.createControl(parent);
	}

	private Combo createCombo(CGroup textGroup, String label, String key) {
		new Label(textGroup, SWT.NONE).setText(label);
		Combo field = new Combo(textGroup, SWT.NONE);
		field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		field.addListener(SWT.Modify, this);
		IDialogSettings settings = getDialogSettings();
		String[] items = settings.getArray(key);
		if (items != null) {
			field.setItems(items);
			if (items.length > 0)
				field.setText(items[0]);
		}
		field.setVisibleItemCount(8);
		field.addListener(SWT.Modify, this);
		field.addListener(SWT.Selection, this);
		return field;
	}

	public void completeEmailData(EmailData data) {
		if (toField != null) {
			data.setTo(toField.getText());
			data.setCc(ccField.getText());
			data.setBcc(bccField.getText());
		}
		data.setSubject(subjectField.getText());
		data.setMessage(messageField.getText());
	}

	public void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		if (toField != null) {
			saveHistory(settings, "to", toField); //$NON-NLS-1$
			saveHistory(settings, "cc", ccField); //$NON-NLS-1$
			saveHistory(settings, "bcc", bccField); //$NON-NLS-1$
		}

	}

	private void saveHistory(IDialogSettings settings, String key, Combo field) {
		String[] items = field.getItems();
		String text = field.getText();
		List<String> newItems = new ArrayList<String>(items.length + 1);
		newItems.add(text);
		for (int i = 0; i < items.length; i++)
			if (!text.equals(items[i])) {
				newItems.add(items[i]);
				if (newItems.size() >= 8)
					break;
			}
		settings.put(key, newItems.toArray(new String[newItems.size()]));
	}

	public void handleEvent(Event event) {
		validatePage();
	}

	@Override
	protected String validate() {
		if (toField != null) {
			String text = toField.getText();
			if (text.trim().isEmpty())
				return Messages.MailPage_receiver_address;
			String errMsg = checkAdr(text, Messages.MailPage_bad_to_address);
			if (errMsg != null)
				return errMsg;
			if (ccField != null) {
				errMsg = checkAdr(ccField.getText(), Messages.MailPage_bad_cc_address);
				if (errMsg != null)
					return errMsg;
			}
			if (bccField != null) {
				errMsg = checkAdr(bccField.getText(), Messages.MailPage_bad_bcc_address);
				if (errMsg != null)
					return errMsg;
			}
			if (subjectField != null && subjectField.getText().trim().isEmpty())
				return Messages.MailPage_no_subject;
		}
		return null;
	}

	private String checkAdr(String text, String msg) {
		if (!msg.trim().isEmpty()) {
			StringTokenizer st = new StringTokenizer(text, ";"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				try {
					new InternetAddress(token).validate();
				} catch (AddressException ex) {
					return NLS.bind(msg, token);
				}
			}
		}
		return null;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && subjectField != null && subjectField.getText().length() == 0
				&& getWizard() instanceof EmailPDFWizard)
			subjectField.setText(((EmailPDFWizard) getWizard()).getTitle());
		super.setVisible(visible);
	}

	public boolean finish() {
		IMailWizard wizard = (IMailWizard) getWizard();
		EmailData emailData = wizard.getEmailData();
		List<String> to = emailData.getTo();
		if (to == null || to.isEmpty()) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				SmartCollectionImpl coll = Ui.getUi().getNavigationHistory(activeWorkbenchWindow)
						.getSelectedCollection();
				if (coll.getAlbum() && coll.getSystem()) {
					String description = coll.getDescription();
					int p = description.indexOf('\n');
					if (p > 0) {
						int q = description.indexOf(": ", p); //$NON-NLS-1$
						if (q >= 0) {
							int r = description.indexOf(';', q + 2);
							String email = r > 0 ? description.substring(q + 2, r) : description.substring(q + 2);
							emailData.setTo(email.trim());
						}
					}
				}
			}
		}
		new EmailJob(emailData, this).schedule();
		return true;
	}

}
