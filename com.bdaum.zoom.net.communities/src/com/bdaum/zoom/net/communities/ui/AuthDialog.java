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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.net.communities.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class AuthDialog extends ZTitleAreaDialog {

	private static final String AUTHORIZE = "authorize."; //$NON-NLS-1$
	private static final int RELOAD = 9999;
	private Browser browser;
	private final String authLink;
	private final String message;
	private final int width;
	private final int height;
	private final boolean respond;
	private Text codeField;
	private String code = ""; //$NON-NLS-1$
	private Label codeLabel;

	public static String show(String authLink, String authmessage, int w,
			int h, boolean respond) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		final Shell shell = workbench.getWorkbenchWindows()[0].getShell();
		final AuthDialog dialog = new AuthDialog(shell, authLink, authmessage,
				w, h, respond);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		if (dialog.getReturnCode() == Window.OK)
			return dialog.getResult();
		return null;
	}

	public AuthDialog(Shell parentShell, String authLink, String message,
			int width, int height, boolean respond) {
		super(parentShell);
		this.authLink = authLink;
		this.message = message;
		this.width = width;
		this.height = height;
		this.respond = respond;
	}

	@Override
	public void create() {
		super.create();
		setMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		browser = new Browser(composite, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.widthHint = width;
		layoutData.heightHint = height;
		browser.setLayoutData(layoutData);
		if (respond) {
			codeLabel = new Label(composite, SWT.NONE);
			codeLabel.setFont(JFaceResources.getHeaderFont());
			codeLabel.setText(Messages.AuthDialog_enter_code_here);
			codeField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			codeField.setFont(JFaceResources.getHeaderFont());
			codeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			codeLabel.setVisible(false);
			codeField.setVisible(false);
			browser.addLocationListener(new LocationListener() {
				public void changing(LocationEvent event) {
					// do nothing
				}
				public void changed(LocationEvent event) {
					if (event.location.indexOf(AUTHORIZE) > 0) {
						codeLabel.setVisible(true);
						codeField.setVisible(true);
					}
				}
			});
		}
		browser.setUrl(authLink);
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, RELOAD, Messages.AuthDialog_reload, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == RELOAD)
			browser.refresh();
		else
			super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		if (codeField != null)
			code = codeField.getText().trim();
		super.okPressed();
	}

	public String getResult() {
		return code;
	}

}
