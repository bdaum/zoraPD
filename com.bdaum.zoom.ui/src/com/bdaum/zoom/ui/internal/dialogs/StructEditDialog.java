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
 * (c) 2009-2013 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.creatorsContact.Contact;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.internal.StructModifyOperation;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.FlatGroup;

@SuppressWarnings("restriction")
public class StructEditDialog extends ZTitleAreaDialog implements IAdaptable {

	private static final String SETTINGSID = "com.bdaum.zoom.ui.structEditDialog"; //$NON-NLS-1$
	private static final String HIERARCHICAL_STRUCT = "hierarchicalStruct"; //$NON-NLS-1$
	private static final int EMAILBUTTONID = 997;
	private static final int SHOWBUTTONID = 998;
	private static final int ADDBUTTONID = 999;

	private AomObject value;
	private QueryField qfield;

	private StructComponent structComponent;
	private FlatGroup radioGroup;
	private IDialogSettings settings;

	public StructEditDialog(Shell parentShell, AomObject value, QueryField qfield) {
		super(parentShell);
		this.value = value;
		this.qfield = qfield;
		this.settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void create() {
		super.create();
		setTitle(qfield.getLabel());
		switch (qfield.getType()) {
		case QueryField.T_LOCATION:
			setMessage(Messages.StructEditDialog_select_existing_location_or_add);
			break;
		case QueryField.T_CONTACT:
			setMessage(Messages.StructEditDialog_select_existing_contact_or_add);
			break;
		case QueryField.T_OBJECT:
			setMessage(Messages.StructEditDialog_select_existing_artwork_or_add);
			break;
		}
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		switch (qfield.getType()) {
		case QueryField.T_LOCATION:
			label.setText(Messages.StructEditDialog_locations);
			break;
		case QueryField.T_CONTACT:
			label.setText(Messages.StructEditDialog_contacts);
			break;
		case QueryField.T_OBJECT:
			label.setText(Messages.StructEditDialog_artworks);
			break;
		}
		radioGroup = new FlatGroup(composite, SWT.NONE, settings, HIERARCHICAL_STRUCT + qfield.getType());
		radioGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		radioGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				structComponent.update();
			}
		});
		structComponent = new StructComponent(dbManager, composite, value, qfield.getType(), true, null, radioGroup,
				null, 0, settings);
		structComponent.fillValues();
		structComponent.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				value = (AomObject) ((IStructuredSelection) event.getSelection()).getFirstElement();
				updateButtons();
			}
		});
		return area;
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null)
			okButton.setEnabled(value != null);
		Button button = getButton(SHOWBUTTONID);
		if (button != null) {
			boolean enabled = value instanceof Location;
			getShell().setModified(enabled);
			button.setEnabled(enabled);
		}
		button = getButton(EMAILBUTTONID);
		if (button != null) {
			boolean enabled = value instanceof Contact && ((Contact) value).getEmail() != null
					&& ((Contact) value).getEmail().length > 0;
			getShell().setModified(enabled);
			button.setEnabled(enabled);
		}
	}

	public Object getResult() {
		return value;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ADDBUTTONID, Messages.StructEditDialog_add, false);
		if (qfield.getType() == QueryField.T_LOCATION && UiActivator.getDefault().getLocationDisplay() != null)
			createButton(parent, SHOWBUTTONID, Messages.StructEditDialog_show_in_map, false);
		else if (qfield.getType() == QueryField.T_CONTACT)
			createButton(parent, EMAILBUTTONID, Messages.StructEditDialog_email, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case ADDBUTTONID:
			int type = qfield.getType();
			Map<String, Map<QueryField, Object>> structOverlayMap = new HashMap<String, Map<QueryField, Object>>();
			EditStructDialog dialog = new EditStructDialog(getShell(), null, type, -1, structOverlayMap,
					Messages.StructEditDialog_add_n);
			dialog.create();
			Point location = dialog.getShell().getLocation();
			location.x -= 25;
			location.y += 30;
			dialog.getShell().setLocation(location);
			if (dialog.open() == OK) {
				IdentifiableObject result = dialog.getResult();
				Map<String, IIdentifiableObject> newObjects = new HashMap<String, IIdentifiableObject>(1);
				newObjects.put(result.getStringId(), result);
				Core.getCore().performOperation(
						new StructModifyOperation(dialog.getTitle(), structOverlayMap, newObjects),
						new NullProgressMonitor(), this);
				structComponent.add(result);
			}
			break;
		case SHOWBUTTONID:
			if (value instanceof Location) {
				ILocationDisplay service = UiActivator.getDefault().getLocationDisplay();
				if (service != null)
					service.display((Location) value);
			}
			break;
		case EMAILBUTTONID:
			if (value instanceof Contact) {
				String[] email = ((Contact) value).getEmail();
				if (email != null && email.length > 0)
					UiActivator.getDefault().sendMail(Arrays.asList(email));
			}
			break;
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		radioGroup.saveSettings();
		structComponent.saveSettings();
		super.okPressed();
	}
}