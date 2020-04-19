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

package com.bdaum.zoom.ui.internal.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.FtpAccountSelectionDialog;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class RemoteImportPage extends ColoredWizardPage {

	private static final String HISTORY_ITEMS = "historyItems"; //$NON-NLS-1$
	private Combo combo;
	public RemoteImportPage() {
		super("main", Messages.RemoteImportPage_import_remote_images, null); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 3);
		setControl(composite);
		setHelp(HelpContextIds.IMPORTREMOTE);
		new Label(composite, SWT.NONE).setText(Messages.RemoteImportPage_URL);
		combo = new Combo(composite, SWT.NONE);
		String[] items = getDialogSettings().getArray(HISTORY_ITEMS);
		if (items != null) {
			combo.setItems(items);
			if (combo.getItemCount() > 0)
				combo.setText(combo.getItem(0));
		}
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		gd_combo.widthHint = 350;
		combo.setLayoutData(gd_combo);
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Messages.RemoteImportPage_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FtpAccountSelectionDialog dialog = new FtpAccountSelectionDialog(getShell());
				if (dialog.open() == Window.OK) {
					combo.setText(dialog.getResult().getUrl());
					validatePage();
				}
			}
		});
		setTitle(Messages.RemoteImportPage_title);
		setMessage(Messages.RemoteImportPage_enter_a_valid_url);
		super.createControl(parent);
	}

	@SuppressWarnings("unused")
	@Override
	protected String validate() {
		String url = combo.getText();
		if (url.isEmpty())
			return Messages.RemoteImportPage_url_must_not_be_empty;
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return Messages.RemoteImportPage_invalid_url;
		}
		return null;
	}

	public URL getUrl() {
		try {
			return new URL(combo.getText());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void saveSettings() {
		String[] hist = UiUtilities.updateComboHistory(combo);
		getDialogSettings().put(HISTORY_ITEMS, hist);
	}

}
