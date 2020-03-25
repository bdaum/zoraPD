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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
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

import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.WebColorGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class EditWallDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.wallProperties"; //$NON-NLS-1$

	private static final String WALL_COLOR = "wallColor"; //$NON-NLS-1$

	private static final String WIDTH = "width"; //$NON-NLS-1$

	private static final String HEIGHT = "height"; //$NON-NLS-1$

	private WallImpl current;
	private String title;
	private Text nameField;
	
	private NumericControl widthField;

	private NumericControl heightField;

	private WebColorGroup selectColorGroup;

	private IDialogSettings settings;
	
	private final Listener listener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			updateButtons();
		}
	};


	public EditWallDialog(Shell parentShell, WallImpl current, String title) {
		super(parentShell, HelpContextIds.EDITWALL_DIALOG);
		this.current = current;
		this.title = title;
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.EditWallDialog_specify_wall_properties);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		new Label(comp, SWT.NONE).setText(Messages.EditWallDialog_location);

		nameField = new Text(comp, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addListener(SWT.Modify, listener);

		new Label(comp, SWT.NONE).setText(Messages.EditWallDialog_dimensions);

		final Composite parm = new Composite(comp, SWT.NONE);
		parm.setLayout(new GridLayout(4, false));

		new Label(parm, SWT.NONE).setText(Messages.EditWallDialog_width);
		widthField = new NumericControl(parm, SWT.BORDER);
		widthField.setDigits(2);
		widthField.setMinimum(50);
		widthField.setMaximum(3000);
		widthField.setIncrement(10);
		widthField.setPageIncrement(100);
		widthField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		widthField.addListener(SWT.Selection, listener);

		new Label(parm, SWT.NONE).setText(Messages.EditWallDialog_height);
		heightField = new NumericControl(parm, SWT.BORDER);
		heightField.setDigits(2);
		heightField.setMinimum(50);
		heightField.setMaximum(500);
		heightField.setIncrement(10);
		heightField.setPageIncrement(100);
		heightField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		heightField.addListener(SWT.Selection, listener);

		selectColorGroup = new WebColorGroup(comp, Messages.EditWallDialog_wall_color);
		initValues();
		fillValues();
		return area;
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled = validate() && !readonly;
			getShell().setModified(enabled);
			okButton.setEnabled(enabled);
		}
	}

	private boolean validate() {
		if (nameField.getText().isEmpty()) {
			setErrorMessage(Messages.EditWallDialog_specify_location_name);
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	private void initValues() {
		selectColorGroup.fillValues(settings, WALL_COLOR, 255, 255, 250);
		initNumericControl(widthField, WIDTH, 5d);
		initNumericControl(heightField, HEIGHT, 2.5d);
	}

	private void initNumericControl(NumericControl control, String key, double dflt) {
		try {
			control.setSelection((int) (settings.getDouble(key) * 100));
		} catch (NumberFormatException e) {
			control.setSelection((int) (dflt * 100));
		}
	}

	private void fillValues() {
		Rgb_type selectedColor = null;
		if (current != null) {
			nameField.setText(current.getLocation());
			selectedColor = current.getColor();
			widthField.setSelection((int) (current.getWidth() / 10d));
			heightField.setSelection((int) (current.getHeight() / 10d));
		} else
			nameField.setText(title);
		if (selectedColor != null)
			selectColorGroup.setRGB(selectedColor);
	}

	@Override
	protected void okPressed() {
		if (current == null)
			current = new WallImpl();
		current.setLocation(nameField.getText());
		current.setColor(selectColorGroup.getRGB());
		current.setWidth(widthField.getSelection() * 10);
		current.setHeight(heightField.getSelection() * 10);
		saveValues();
		super.okPressed();
	}

	private void saveValues() {
		selectColorGroup.saveSettings(settings, WALL_COLOR);
		settings.put(WIDTH, current.getWidth() / 1000d);
		settings.put(HEIGHT, current.getHeight() / 1000d);
	}

	public WallImpl getResult() {
		return current;
	}

}
