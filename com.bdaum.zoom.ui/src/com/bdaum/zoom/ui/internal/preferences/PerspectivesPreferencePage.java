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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;

public class PerspectivesPreferencePage extends AbstractPreferencePage {
	IPerspectiveRegistry perspectiveRegistry;

	private ArrayList<IPerspectiveDescriptor> perspectives;

	private ArrayList<IPerspectiveDescriptor> perspToDelete = new ArrayList<IPerspectiveDescriptor>();

	private Button deleteButton;

	private TableViewer viewer;

	@Override
	public void init(IWorkbench aWorkbench) {
		super.init(aWorkbench);
		this.perspectiveRegistry = workbench.getPerspectiveRegistry();
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.PERSPECTIVE_PREFERENCE_PAGE);
		noDefaultAndApplyButton();
		createCustomizePerspective(composite);
	}

	/**
	 * Create a table of 3 buttons to enable the user to manage customized
	 * perspectives.
	 *
	 * @param parent
	 *            the parent for the button parent
	 * @return Composite that the buttons are created in.
	 */
	protected Composite createCustomizePerspective(Composite parent) {
		Composite perspectivesComponent = new Composite(parent, SWT.NONE);
		perspectivesComponent.setLayoutData(new GridData(GridData.FILL_BOTH));
		perspectivesComponent.setLayout(new GridLayout(2, false));
		Label description = new Label(perspectivesComponent, SWT.WRAP);
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		description.setText(Messages.getString("PerspectivesPreferencePage.perspectives_organize")); //$NON-NLS-1$
		new Label(perspectivesComponent, SWT.NONE)
				.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

		CGroup perspGroup = UiUtilities.createGroup(perspectivesComponent, 2,
				Messages.getString("PerspectivesPreferencePage.available_perspectives")); //$NON-NLS-1$
		viewer = new TableViewer(perspGroup, SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(ZViewerComparator.INSTANCE);
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(180);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPerspectiveDescriptor)
					return ((IPerspectiveDescriptor) element).getLabel();
				return element.toString();
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(120);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPerspectiveDescriptor)
					return isPredefined((IPerspectiveDescriptor) element)
							? Messages.getString("PerspectivesPreferencePage.predefined") //$NON-NLS-1$
							: Messages.getString("PerspectivesPreferencePage.user_defined"); //$NON-NLS-1$
				return element.toString();
			}
		});
		IPerspectiveDescriptor[] persps = perspectiveRegistry.getPerspectives();
		perspectives = new ArrayList<IPerspectiveDescriptor>(persps.length);
		for (int i = 0; i < persps.length; i++)
			perspectives.add(i, persps[i]);
		viewer.setInput(perspectives);
		createVerticalButtonBar(perspGroup);
		return perspectivesComponent;
	}

	/**
	 * Creates and returns the vertical button bar.
	 *
	 * @param parent
	 *            the parent composite to contain the button bar
	 * @return the button bar control
	 */
	protected Control createVerticalButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		composite.setLayout(layout);
		deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText(Messages.getString("PerspectivesPreferencePage.delete")); //$NON-NLS-1$
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IPerspectiveDescriptor desc = (IPerspectiveDescriptor) viewer.getStructuredSelection()
						.getFirstElement();
				if (desc != null) {
					if (event.widget == deleteButton && !isPredefined(desc) && !perspToDelete.contains(desc)
							&& !findOpenInstance(desc)) {
						perspToDelete.add(desc);
						perspectives.remove(desc);
						viewer.remove(desc);
					}
					updateButtons();
				}
			}
		});
		deleteButton.setToolTipText(Messages.getString("PerspectivesPreferencePage.delete_tooltip")); //$NON-NLS-1$
		updateButtons();
		return composite;
	}

	/**
	 * Deletes the perspectives selected by the user if there is no opened instance
	 * of that perspective.
	 *
	 * @return boolean <code>true</code> if all of the perspectives could be
	 *         deleted.
	 */
	private boolean findOpenInstance(IPerspectiveDescriptor desc) {
		IWorkbenchWindow windows[] = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage pages[] = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				IPerspectiveDescriptor perspective = pages[j].getPerspective();
				if (desc.equals(perspective))
					if (!MessageDialog.openQuestion(getShell(),
							Messages.getString("PerspectivesPreferencePage.delete_perspective"), //$NON-NLS-1$
							NLS.bind(Messages.getString("PerspectivesPreferencePage.are_you_sure"), //$NON-NLS-1$
									desc.getLabel()))) {
						return true;
					}
			}
		}
		return false;
	}

	@Override
	protected void doPerformOk() {
		if (perspectives.size() < perspectiveRegistry.getPerspectives().length) {
			for (IWorkbenchWindow workbenchWindow : workbench.getWorkbenchWindows())
				for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
					IPerspectiveDescriptor perspective = workbenchPage.getPerspective();
					for (IPerspectiveDescriptor desc : perspToDelete)
						if (desc.equals(perspective))
							workbenchPage.closePerspective(perspective, true, true);
				}
			for (IPerspectiveDescriptor desc : perspToDelete)
				perspectiveRegistry.deletePerspective(desc);
		}
	}

	@Override
	protected void doUpdateButtons() {
		IStructuredSelection sel = viewer.getStructuredSelection();
		deleteButton.setEnabled(!sel.isEmpty() && !isPredefined((IPerspectiveDescriptor) sel.getFirstElement()));
	}

	private static boolean isPredefined(IPerspectiveDescriptor desc) {
		if (desc instanceof IPluginContribution)
			return !desc.getId().endsWith('.' + desc.getLabel());
		return false;
	}

}
