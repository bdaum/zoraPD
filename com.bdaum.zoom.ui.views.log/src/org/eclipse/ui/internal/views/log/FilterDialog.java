/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *     bdaum - Adapted to ZoRaPD
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;

import com.bdaum.zoom.ui.internal.dialogs.ZTrayDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class FilterDialog extends ZTrayDialog implements Listener{

	Button okButton;

	// entries count limit
	private CheckboxButton limit;
	Text limitText;

	// entry types filter
	private CheckboxButton errorCheckbox;
	private CheckboxButton warningCheckbox;
	private CheckboxButton infoCheckbox;
	private CheckboxButton okCheckbox;
	private IMemento memento;

	private RadioButtonGroup sessionButtonGroup;

	public FilterDialog(Shell parentShell, IMemento memento) {
		super(parentShell);
		this.memento = memento;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createEventTypesGroup(container);
		createLimitSection(container);
		createSessionSection(container);

		Dialog.applyDialogFont(container);
		return container;
	}

	private void createEventTypesGroup(Composite parent) {
		CGroup group = new CGroup(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 275;
		group.setLayoutData(gd);
		group.setText(Messages.LogView_FilterDialog_eventTypes);

		okCheckbox = WidgetFactory.createCheckButton(group, Messages.LogView_FilterDialog_ok, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		okCheckbox.setSelection(memento.getString(LogView.P_LOG_OK).equals("true")); //$NON-NLS-1$

		infoCheckbox =  WidgetFactory.createCheckButton(group, Messages.LogView_FilterDialog_information, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		infoCheckbox.setSelection(memento.getString(LogView.P_LOG_INFO).equals("true")); //$NON-NLS-1$

		warningCheckbox =  WidgetFactory.createCheckButton(group, Messages.LogView_FilterDialog_warning, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		warningCheckbox.setSelection(memento.getString(LogView.P_LOG_WARNING).equals("true")); //$NON-NLS-1$

		errorCheckbox =  WidgetFactory.createCheckButton(group, Messages.LogView_FilterDialog_error, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		errorCheckbox.setSelection(memento.getString(LogView.P_LOG_ERROR).equals("true")); //$NON-NLS-1$
	}

	private void createLimitSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		limit =  WidgetFactory.createCheckButton(comp, Messages.LogView_FilterDialog_limitTo, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		limit.setSelection(memento.getString(LogView.P_USE_LIMIT).equals("true")); //$NON-NLS-1$
		limit.addListener(SWT.Selection, this);
		limitText = new Text(comp, SWT.BORDER);
		limitText.addListener(SWT.Verify, this);
		limitText.addListener(SWT.Modify, this);
		limitText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		limitText.setText(memento.getString(LogView.P_LOG_LIMIT));
		limitText.setEnabled(limit.getSelection());
	}
	
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			limitText.setEnabled(((Button) e.widget).getSelection());
			break;
		case SWT.Verify:
			if (Character.isLetter(e.character))
				e.doit = false;
			break;
		case SWT.Modify:
			try {
				if (okButton == null)
					return;
				Integer.parseInt(limitText.getText());
				okButton.setEnabled(true);
			} catch (NumberFormatException e1) {
				okButton.setEnabled(false);
			}
			break;
		}
	}

	private void createSessionSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.LogView_FilterDialog_eventsLogged);
		
		sessionButtonGroup = new RadioButtonGroup(container, null,  SWT.NONE, Messages.LogView_FilterDialog_allSessions, Messages.LogView_FilterDialog_recentSession);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalIndent = 20;
		sessionButtonGroup.setLayoutData(layoutData);

		if (memento.getString(LogView.P_SHOW_ALL_SESSIONS).equals("true")) //$NON-NLS-1$
			sessionButtonGroup.setSelection(0);
		else
			sessionButtonGroup.setSelection(1);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void okPressed() {
		memento.putString(LogView.P_LOG_OK, okCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_INFO, infoCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_WARNING, warningCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_ERROR, errorCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_LIMIT, limitText.getText());
		memento.putString(LogView.P_USE_LIMIT, limit.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_SHOW_ALL_SESSIONS, sessionButtonGroup.getSelection() == 0 ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

		super.okPressed();
	}

}
