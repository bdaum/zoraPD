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
package com.bdaum.zoom.net.communities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.net.communities.ui.EditCommunityAccountDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class CommunitiesPreferencePage extends AbstractPreferencePagePart {

	static final Object[] EMPTY = new Object[0];
	private TreeViewer accountViewer;
	private Button editButton;
	private Button removeButton;
	private Map<String, List<CommunityAccount>> accounts = new HashMap<String, List<CommunityAccount>>(7);
	private Set<String> changed = new HashSet<String>(7);
	private Button newButton;

	@SuppressWarnings("unused")
	public Control createPageContents(final Composite parent, AbstractPreferencePage parentPage) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		new Label(composite, SWT.NONE).setText(Messages.CommunitiesPreferencePage_community_descr);
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
		CGroup group = createGroup(composite, 2, Messages.CommunitiesPreferencePage_photo_community_accounts);
		accountViewer = new TreeViewer(group, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		expandCollapseGroup.setViewer(accountViewer);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 200;
		accountViewer.getControl().setLayoutData(layoutData);
		accountViewer.setContentProvider(new AccountContentProvider(accounts));
		accountViewer.setLabelProvider(new AccountLabelProvider(accountViewer));
		accountViewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(accountViewer);
		Composite buttonGroup = new Composite(group, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonGroup.setLayout(new GridLayout(1, false));
		newButton = createPushButton(buttonGroup, Messages.CommunitiesPreferencePage_new,
				Messages.CommunitiesPreferencePage_create_a_new_account);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) accountViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof IConfigurationElement) {
					IConfigurationElement conf = (IConfigurationElement) firstElement;
					CommunityApi communitiesApi = CommunitiesActivator.getCommunitiesApi(conf);
					String id = conf.getAttribute("id"); //$NON-NLS-1$
					List<CommunityAccount> list = accounts.get(id);
					IConfigurationElement config = conf.getChildren()[0];
					EditCommunityAccountDialog dialog = new EditCommunityAccountDialog(parent.getShell(),
							new CommunityAccount(config), communitiesApi);
					if (dialog.open() == Window.OK) {
						CommunityAccount result = dialog.getResult();
						list.add(result);
						setViewerInput();
						accountViewer.setSelection(new StructuredSelection(result));
					}
				}
			}
		});

		editButton = createPushButton(buttonGroup, Messages.CommunitiesPreferencePage_edit,
				Messages.CommunitiesPreferencePage_edit_selected_account);
		editButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) accountViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof CommunityAccount) {
					final CommunityAccount account = (CommunityAccount) firstElement;
					IConfigurationElement conf = (IConfigurationElement) account.getConfiguration().getParent();
					final CommunityApi communitiesApi = CommunitiesActivator.getCommunitiesApi(conf);
					BusyIndicator.showWhile(e.display, () -> {
						EditCommunityAccountDialog dialog = new EditCommunityAccountDialog(parent.getShell(), account,
								communitiesApi);
						if (dialog.open() == Window.OK) {
							CommunityAccount result = dialog.getResult();
							accountViewer.update(result, null);
							changed.add(result.getCommunityId());
						}
					});
				}
			}
		});
		new Label(buttonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		removeButton = createPushButton(buttonGroup, Messages.CommunitiesPreferencePage_remove,
				Messages.CommunitiesPreferencePage_remove_selected_account);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) accountViewer.getSelection();

				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof CommunityAccount) {
					CommunityAccount communityAccount = (CommunityAccount) firstElement;
					List<CommunityAccount> acc = accounts.get(communityAccount.getCommunityId());
					if (acc != null) {
						int i = acc.indexOf(communityAccount);
						CommunityAccount sibling = (i < acc.size() - 1) ? acc.get(i + 1)
								: (i > 0) ? acc.get(i - 1) : null;
						acc.remove(i);
						changed.add(communityAccount.getCommunityId());
						setViewerInput();
						if (sibling != null)
							accountViewer.setSelection(new StructuredSelection(sibling));
					}
				}
			}
		});
		accountViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		setViewerInput();
		return composite;
	}

	@Override
	public void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) accountViewer.getSelection();
		boolean enabled = (selection.getFirstElement() instanceof CommunityAccount);
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		boolean newenabled = (selection.getFirstElement() instanceof IConfigurationElement);
		newButton.setEnabled(newenabled);
	}

	private static Button createPushButton(Composite parent, String lab, String tooltip) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(lab);
		button.setToolTipText(tooltip);
		return button;
	}

	private void setViewerInput() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(CommunitiesActivator.PLUGIN_ID, "community"); //$NON-NLS-1$
		accountViewer.setInput(extensionPoint);
		accountViewer.expandAll();
	}

	@Override
	public void performOk() {
		for (Map.Entry<String, List<CommunityAccount>> entry : accounts.entrySet()) {
			if (changed.contains(entry.getKey()))
				CommunityAccount.saveAllAccounts(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String getLabel() {
		return Messages.CommunitiesPreferencePage_communities;
	}
	
	@Override
	public String getTooltip() {
		return Messages.CommunitiesPreferencePage_community_tooltip;
	}

	@Override
	public void performCancel() {
		// do nothing
	}

}
