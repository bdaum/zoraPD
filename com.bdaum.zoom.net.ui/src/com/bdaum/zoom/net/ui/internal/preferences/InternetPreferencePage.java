/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.net.ui.internal.preferences;

import java.util.List;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.ui.internal.HelpContextIds;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.dialogs.EditFtpDialog;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class InternetPreferencePage extends AbstractPreferencePage implements Listener, ISelectionChangedListener {

	private IProxyService proxyService;
	private Text portField;
	private Text proxyField;
	private NumericControl timeoutField;
	private List<FtpAccount> ftpAccounts;
	private TableViewer ftpViewer;
	private Button editButton;
	private Button removeButton;
	private RadioButtonGroup proxyGroup;
	private Composite configGroup;

	public InternetPreferencePage() {
		setDescription(Messages.InternetPreferencePage_Common_Internet_settings);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */

	@Override
	public void init(IWorkbench wb) {
		this.workbench = wb;
		setPreferenceStore(NetActivator.getDefault().getPreferenceStore());
		proxyService = NetActivator.getDefault().getProxyService();
	}

	@Override
	public void dispose() {
		NetActivator.getDefault().ungetProxyService(proxyService);
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.INTERNET_PREFERENCE_PAGE);
		createTabFolder(composite, "Internet"); //$NON-NLS-1$
		UiUtilities.createTabItem(tabFolder, Messages.InternetPreferencePage_http_ftp,
				Messages.InternetPreferencePage_http_tooltip).setControl(createHttpGroup(tabFolder));
		createExtensions(tabFolder, "com.bdaum.zoom.net.preferences.InternetPreferencePage"); //$NON-NLS-1$
		fillValues();
		initTabFolder(0);
	}

	private Control createHttpGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		createProxyGroup(composite);
		createTimeoutGroup(composite);
		createFtpGroup(composite);
		return composite;
	}

	private void createFtpGroup(Composite parent) {
		CGroup group = UiUtilities.createGroup(parent, 2, Messages.InternetPreferencePage_ftp_accounts);
		ftpViewer = new TableViewer(group, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		ftpViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		ftpViewer.setContentProvider(ArrayContentProvider.getInstance());
		ftpViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FtpAccount)
					return ((FtpAccount) element).getName();
				return element.toString();
			}
		});
		ftpViewer.setComparator(ZViewerComparator.INSTANCE);
		Composite buttonGroup = new Composite(group, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonGroup.setLayout(new GridLayout(1, false));
		Button newButton = createPushButton(buttonGroup, Messages.InternetPreferencePage_new,
				Messages.InternetPreferencePage_new_tooltip);
		newButton.addListener(SWT.Selection, this);
		editButton = createPushButton(buttonGroup, Messages.InternetPreferencePage_edit,
				Messages.InternetPreferencePage_edit_tooltip);
		editButton.addListener(SWT.Selection, this);
		removeButton = createPushButton(buttonGroup, Messages.InternetPreferencePage_remove,
				Messages.InternetPreferencePage_remove_tooltip);
		removeButton.addListener(SWT.Selection, this);
		ftpViewer.addSelectionChangedListener(this);
		updateButtons();
	}

	@Override
	protected void doUpdateButtons() {
		boolean enabled = !ftpViewer.getSelection().isEmpty();
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	private static Button createPushButton(Composite parent, String lab, String tooltip) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(lab);
		button.setToolTipText(tooltip);
		return button;
	}

	@SuppressWarnings("unused")
	private void createProxyGroup(Composite parent) {
		CGroup group = UiUtilities.createGroup(parent, 2, Messages.InternetPreferencePage_http_proxy);
		proxyGroup = new RadioButtonGroup(group, null, SWT.NONE, Messages.InternetPreferencePage_System_proxy_config,
				Messages.InternetPreferencePage_Direct_connection, Messages.InternetPreferencePage_Manual_config);
		proxyGroup.addListener(SWT.Selection, this);
		new Label(group, SWT.NONE);
		configGroup = createConfigGroup(group);
	}

	@Override
	protected void fillValues() {
		if (proxyService == null) {
			proxyGroup.setSelection(1);
			proxyGroup.setEnabled(false);
		} else {
			proxyGroup.setEnabled(0, proxyService.hasSystemProxies());
			proxyGroup
					.setSelection(proxyService.isProxiesEnabled() ? proxyService.isSystemProxiesEnabled() ? 0 : 2 : 1);
			IProxyData proxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE);
			if (proxyData != null) {
				proxyField.setText(safeValue(proxyData.getHost()));
				int port = proxyData.getPort();
				portField.setText(port < 0 ? "" : String.valueOf(port)); //$NON-NLS-1$
			}
		}
		int timeout = getPreferenceStore().getInt(PreferenceConstants.TIMEOUT);
		timeoutField.setSelection(timeout);
		if (ftpAccounts == null) {
			ftpAccounts = FtpAccount.getAllAccounts();
			ftpViewer.setInput(ftpAccounts);
		}
		updateFields();
	}

	private static String safeValue(String text) {
		return text == null ? "" : text; //$NON-NLS-1$
	}

	private void updateFields() {
		configGroup.setVisible(proxyGroup.getSelection() == 2);
	}

	private Composite createConfigGroup(Composite comp) {
		Composite grp = new Composite(comp, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 15;
		grp.setLayoutData(data);
		grp.setLayout(new GridLayout(4, false));
		new Label(grp, SWT.NONE).setText(Messages.InternetPreferencePage_HTTP_Proxy);
		proxyField = new Text(grp, SWT.BORDER);
		proxyField.setLayoutData(new GridData(150, SWT.DEFAULT));
		new Label(grp, SWT.NONE).setText(Messages.InternetPreferencePage_Port);
		portField = new Text(grp, SWT.BORDER);
		portField.setLayoutData(new GridData(50, SWT.DEFAULT));
		portField.addListener(SWT.Verify, this);
		return grp;
	}

	private void createTimeoutGroup(Composite comp) {
		CGroup grp = UiUtilities.createGroup(comp, 2, Messages.InternetPreferencePage_connection_timeout);
		new Label(grp, SWT.NONE).setText(Messages.InternetPreferencePage_Timeout);
		timeoutField = new NumericControl(grp, SWT.NONE);
		timeoutField.setMinimum(1);
		timeoutField.setMaximum(120);
	}

	@Override
	protected void doPerformDefaults() {
		getPreferenceStore().setValue(PreferenceConstants.TIMEOUT,
				getPreferenceStore().getDefaultInt(PreferenceConstants.TIMEOUT));
	}

	@Override
	protected void doPerformOk() {
		if (proxyService != null) {
			int selection = proxyGroup.getSelection();
			if (selection == 2) {
				proxyService.setProxiesEnabled(true);
				proxyService.setSystemProxiesEnabled(false);
				IProxyData proxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE);
				if (proxyData != null) {
					String text = proxyField.getText();
					proxyData.setHost(text.isEmpty() ? null : text);
					text = portField.getText();
					proxyData.setPort(text.isEmpty() ? -1 : Integer.parseInt(text));
					try {
						proxyService.setProxyData(new IProxyData[] { proxyData });
					} catch (CoreException e) {
						NetActivator.getDefault().logError(Messages.InternetPreferencePage_error_setting_proxy, e);
					}
				}
			} else if (selection == 0) {
				proxyService.setProxiesEnabled(true);
				proxyService.setSystemProxiesEnabled(true);
			} else {
				proxyService.setProxiesEnabled(false);
				proxyService.setSystemProxiesEnabled(false);
			}
		}
		getPreferenceStore().setValue(PreferenceConstants.TIMEOUT, timeoutField.getSelection());
		FtpAccount.saveAccounts(ftpAccounts);
	}

	public void handleEvent(Event e) {
		if (e.type == SWT.Verify) {
			for (int i = 0; i < e.text.length(); i++)
				if (!Character.isDigit(e.text.charAt(i))) {
					e.doit = false;
					break;
				}
		} else if (e.widget == proxyGroup) {
			updateFields();
			if (proxyField.getEnabled())
				proxyField.setFocus();
		} else if (e.widget == editButton) {
			IStructuredSelection selection = ftpViewer.getStructuredSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof FtpAccount) {
				EditFtpDialog dialog = new EditFtpDialog(getShell(), (FtpAccount) firstElement, false, null);
				if (dialog.open() == Window.OK) {
					ftpViewer.update(dialog.getResult(), null);
				}
			}
		} else if (e.widget == removeButton) {
			Object firstElement = ftpViewer.getStructuredSelection().getFirstElement();
			if (firstElement instanceof FtpAccount) {
				int i = ftpAccounts.indexOf(firstElement);
				FtpAccount sibling = (i < ftpAccounts.size() - 1) ? ftpAccounts.get(i + 1)
						: (i > 0) ? ftpAccounts.get(i - 1) : null;
				ftpAccounts.remove(i);
				ftpViewer.setInput(ftpAccounts);
				if (sibling != null)
					ftpViewer.setSelection(new StructuredSelection(sibling));
			}
		} else {
			EditFtpDialog dialog = new EditFtpDialog(getShell(), new FtpAccount(), false, null);
			if (dialog.open() == Window.OK) {
				FtpAccount result = dialog.getResult();
				ftpAccounts.add(result);
				ftpViewer.setInput(ftpAccounts);
				ftpViewer.setSelection(new StructuredSelection(result));
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

}