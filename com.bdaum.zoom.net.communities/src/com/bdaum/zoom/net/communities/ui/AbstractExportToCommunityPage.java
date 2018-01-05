package com.bdaum.zoom.net.communities.ui;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public abstract class AbstractExportToCommunityPage extends ColoredWizardPage {

	protected static final String SELECTED_ACCOUNT = "selectedAccount"; //$NON-NLS-1$
	protected final IConfigurationElement configElement;
	protected List<Asset> assets;
	protected ComboViewer accountViewer;
	protected List<CommunityAccount> communityAccounts;
	protected final String id;
	protected IConfigurationElement accountConfig;
	protected String msg;
	protected Button editButton;

	public AbstractExportToCommunityPage(IConfigurationElement configElement, List<Asset> assets, String id,
			String title, ImageDescriptor titleImage) {
		super("main", title, titleImage); //$NON-NLS-1$
		this.configElement = configElement;
		this.assets = assets;
		this.id = id;
		this.accountConfig = configElement.getChildren("account")[0]; //$NON-NLS-1$
	}

	protected void createAccountGroup(Composite parent) {
		communityAccounts = CommunityAccount.loadAllAccounts(id, accountConfig);
		communityAccounts.add(0, new CommunityAccount(accountConfig));
		Composite group = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.minimumWidth = 0;
		group.setLayoutData(layoutData);
		group.setLayout(new GridLayout(3, false));
		final Label accountLabel = new Label(group, SWT.NONE);
		accountLabel.setText(Messages.ExportToCommunityPage_account);
		accountViewer = new ComboViewer(group, SWT.BORDER | SWT.READ_ONLY);
		accountViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		accountViewer.setContentProvider(ArrayContentProvider.getInstance());
		accountViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommunityAccount) {
					String name = ((CommunityAccount) element).getName();
					return name == null ? Messages.ExportToCommunityPage_create_new_account : name;
				}
				return element.toString();
			}
		});
		editButton = new Button(group, SWT.PUSH);
		editButton.setText(Messages.ExportToCommunityPage_edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object el = ((IStructuredSelection) accountViewer.getSelection()).getFirstElement();
				if (el instanceof CommunityAccount) {
					final CommunityApi api = ((AbstractCommunityExportWizard) getWizard()).getApi();
					final CommunityAccount account = (CommunityAccount) el;
					BusyIndicator.showWhile(e.display, () -> {
						EditCommunityAccountDialog dialog = new EditCommunityAccountDialog(editButton.getShell(),
								account, api);
						if (dialog.open() == Window.OK) {
							CommunityAccount result = dialog.getResult();
							if (account.isNullAccount()) {
								communityAccounts.add(0, new CommunityAccount(accountConfig));
								accountViewer.setInput(communityAccounts);
								accountViewer.setSelection(new StructuredSelection(result));
							} else
								accountViewer.update(result, null);
							CommunityAccount.saveAllAccounts(id, communityAccounts);
						}
					});
					validatePage();
				}
			}
		});
		accountViewer.setInput(communityAccounts);
		if (communityAccounts.size() == 1)
			accountViewer.setSelection(new StructuredSelection(communityAccounts.get(0)));
		accountViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateFields();
				validatePage();
			}
		});
	}

	protected String checkAccount() {
		CommunityAccount acc = (CommunityAccount) ((IStructuredSelection) accountViewer.getSelection())
				.getFirstElement();
		if (acc == null || acc.getName() == null)
			return Messages.ExportToCommunityPage_please_select_an_account;
		return null;
	}

	protected void updateFields() {
		editButton.setEnabled(!accountViewer.getSelection().isEmpty());
	}

	protected void fillValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String accName = settings.get(SELECTED_ACCOUNT);
			if (accName != null)
				for (CommunityAccount acc : communityAccounts)
					if (accName.equals(acc.getName())) {
						accountViewer.setSelection(new StructuredSelection(acc));
						break;
					}

		}
	}

	protected void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		Object firstElement = ((IStructuredSelection) accountViewer.getSelection()).getFirstElement();
		if (firstElement instanceof CommunityAccount && settings != null)
			settings.put(SELECTED_ACCOUNT, ((CommunityAccount) firstElement).getName());
	}

	public boolean finish() throws CommunicationException, AuthException {
		saveSettings();
		Object firstElement = ((IStructuredSelection) accountViewer.getSelection()).getFirstElement();
		if (firstElement instanceof CommunityAccount)
			return doFinish((CommunityAccount) firstElement);
		return true;
	}

	protected abstract boolean doFinish(CommunityAccount acc) throws CommunicationException, AuthException;

}