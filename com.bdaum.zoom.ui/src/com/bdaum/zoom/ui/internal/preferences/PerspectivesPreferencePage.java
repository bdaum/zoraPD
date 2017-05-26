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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;

/**
 * The Workbench / Perspectives preference page.
 */
public class PerspectivesPreferencePage extends AbstractPreferencePage {
	IPerspectiveRegistry perspectiveRegistry;

	private ArrayList<IPerspectiveDescriptor> perspectives;

	private ArrayList<IPerspectiveDescriptor> perspToDelete = new ArrayList<IPerspectiveDescriptor>();

	private Table perspectivesTable;

	private Button deleteButton;

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

		// Font font = parent.getFont();

		// define container & its gridding
		Composite perspectivesComponent = new Composite(parent, SWT.NONE);
		perspectivesComponent.setLayoutData(new GridData(GridData.FILL_BOTH));
		perspectivesComponent.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		perspectivesComponent.setLayout(layout);
		// Message
		Label description = new Label(perspectivesComponent, SWT.WRAP);
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false, 2, 1));
		description.setText(Messages
				.getString("PerspectivesPreferencePage.perspectives_organize")); //$NON-NLS-1$
		Label space = new Label(perspectivesComponent, SWT.NONE);
		space.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false, 2, 1));

		// Add the label
		Label label = new Label(perspectivesComponent, SWT.LEFT);
		label.setText(Messages
				.getString("PerspectivesPreferencePage.available_perspectives")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		// label.setFont(font);

		// Add perspectivesTable.
		perspectivesTable = new Table(perspectivesComponent, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		perspectivesTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		// perspectivesTable.setFont(font);

		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		perspectivesTable.setLayoutData(data);

		// Populate the perspectivesTable
		IPerspectiveDescriptor[] persps = perspectiveRegistry.getPerspectives();
		perspectives = new ArrayList<IPerspectiveDescriptor>(persps.length);
		for (int i = 0; i < persps.length; i++) {
			perspectives.add(i, persps[i]);
		}
		Collections.sort(perspectives,
				new Comparator<IPerspectiveDescriptor>() {

					public int compare(IPerspectiveDescriptor o1,
							IPerspectiveDescriptor o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}
				});
		updatePerspectivesTable();

		// Create vertical button bar.
		Composite buttonBar = (Composite) createVerticalButtonBar(perspectivesComponent);
		data = new GridData(GridData.FILL_VERTICAL);
		buttonBar.setLayoutData(data);

		return perspectivesComponent;
	}

	/**
	 * Creates a new vertical button with the given id.
	 * <p>
	 * The default implementation of this framework method creates a standard
	 * push button, registers for selection events including button presses and
	 * help requests, and registers default buttons with its shell. The button
	 * id is stored as the buttons client data.
	 * </p>
	 *
	 * @param parent
	 *            the parent composite
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button,
	 *            and <code>false</code> otherwise
	 * @return Button The created button.
	 */
	protected Button createVerticalButton(Composite parent, String label,
			boolean defaultButton) {
		Button button = new Button(parent, SWT.PUSH);

		button.setText(label);

		GridData data = setButtonLayoutData(button);
		data.horizontalAlignment = GridData.FILL;

		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				verticalButtonPressed(event.widget);
			}
		});
		button.setToolTipText(label);
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		button.setFont(parent.getFont());
		return button;
	}

	/**
	 * Creates and returns the vertical button bar.
	 *
	 * @param parent
	 *            the parent composite to contain the button bar
	 * @return the button bar control
	 */
	protected Control createVerticalButtonBar(Composite parent) {
		// Create composite.
		Composite composite = new Composite(parent, SWT.NULL);

		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		// Add the buttons to the button bar.

		deleteButton = createVerticalButton(composite,
				Messages.getString("PerspectivesPreferencePage.delete"), false); //$NON-NLS-1$
		deleteButton.setToolTipText(Messages
				.getString("PerspectivesPreferencePage.delete_tooltip")); //$NON-NLS-1$
		updateButtons();

		return composite;
	}

	/**
	 * The default button has been pressed.
	 */

	@Override
	protected void doPerformDefaults() {
		// Project perspective preferences
		updatePerspectivesTable();
	}

	/**
	 * Deletes the perspectives selected by the user if there is no opened
	 * instance of that perspective.
	 *
	 * @return boolean <code>true</code> if all of the perspectives could be
	 *         deleted.
	 */
	private boolean findOpenInstance(IPerspectiveDescriptor desc) {
		IWorkbenchWindow windows[] = workbench.getWorkbenchWindows();

		// find all active perspectives currently
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage pages[] = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				IPerspectiveDescriptor perspective = pages[j].getPerspective();
				if (desc.equals(perspective)) {
					if (!MessageDialog
							.openQuestion(
									getShell(),
									Messages.getString("PerspectivesPreferencePage.delete_perspective"), //$NON-NLS-1$
									NLS.bind(
											Messages.getString("PerspectivesPreferencePage.are_you_sure"), //$NON-NLS-1$
											desc.getLabel()))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Apply the user's changes if any
	 */

	@Override
	protected void doPerformOk() {

		// Delete the perspective
		if (perspectives.size() < perspectiveRegistry.getPerspectives().length) {
			// close any perspectives that are about to be deleted
			for (IWorkbenchWindow workbenchWindow : workbench
					.getWorkbenchWindows()) {
				for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
					IPerspectiveDescriptor perspective = workbenchPage
							.getPerspective();
					for (IPerspectiveDescriptor desc : perspToDelete)
						if (desc.equals(perspective))
							workbenchPage.closePerspective(perspective, true,
									true);
				}
			}
			for (IPerspectiveDescriptor desc : perspToDelete)
				perspectiveRegistry.deletePerspective(desc);
		}
	}

	/**
	 * Update the button enablement state.
	 */
	@Override
	protected void doUpdateButtons() {
		// Get selection.
		int index = perspectivesTable.getSelectionIndex();

		// Map it to the perspective descriptor
		IPerspectiveDescriptor desc = null;
		if (index > -1) {
			desc = perspectives.get(index);
		}

		// Do enable.
		if (desc != null) {
			deleteButton.setEnabled(!isPredefined(desc));
			// CssActivator.getDefault().setColors(deleteButton, null);
		} else {
			deleteButton.setEnabled(false);
		}
	}

	private static boolean isPredefined(IPerspectiveDescriptor desc) {
		if (desc instanceof IPluginContribution)
			return ((IPluginContribution) desc).getPluginId() != null;
		return false;
	}

	/**
	 * Update the perspectivesTable.
	 */
	protected void updatePerspectivesTable() {
		// Populate the table with the items
		perspectivesTable.removeAll();
		for (int i = 0; i < perspectives.size(); i++)
			newPerspectivesTableItem(perspectives.get(i), i, false);
	}

	/**
	 * Create a new tableItem using given perspective, and set image for the new
	 * item.
	 */
	protected TableItem newPerspectivesTableItem(IPerspectiveDescriptor persp,
			int index, boolean selected) {

		ImageDescriptor image = persp.getImageDescriptor();

		TableItem item = new TableItem(perspectivesTable, SWT.NULL, index);
		if (image != null) {
			final Image img = image.createImage();
			if (img != null) {
				item.setImage(img);
				item.addDisposeListener(new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						img.dispose();
					}
				});
			}
		}
		item.setFont(isPredefined(persp) ? JFaceResources.getDefaultFont()
				: JFaceResources.getBannerFont());
		String label = persp.getLabel();
		item.setText(label);
		item.setData(persp);
		if (selected) {
			perspectivesTable.setSelection(index);
		}

		return item;
	}

	/**
	 * Notifies that this page's button with the given id has been pressed.
	 *
	 * @param button
	 *            the button that was pressed
	 */
	protected void verticalButtonPressed(Widget button) {
		// Get selection.
		int index = perspectivesTable.getSelectionIndex();

		// Map it to the perspective descriptor
		IPerspectiveDescriptor desc = null;
		if (index > -1) {
			desc = perspectives.get(index);
		} else {
			return;
		}

		// Take action.
		if (button == deleteButton) {
			if (!isPredefined(desc) && !perspToDelete.contains(desc)) {
				if (!findOpenInstance(desc)) {
					perspToDelete.add(desc);
					perspectives.remove(desc);
					updatePerspectivesTable();
				}

			}
		}
		updateButtons();
	}

}
