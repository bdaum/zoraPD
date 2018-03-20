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

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.WebColorGroup;

public class EditWallDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.wallProperties"; //$NON-NLS-1$

	private static final String WALL_COLOR = "wallColor"; //$NON-NLS-1$

	private static final String WIDTH = "width"; //$NON-NLS-1$

	private static final String HEIGHT = "height"; //$NON-NLS-1$

	private static NumberFormat af = (NumberFormat.getNumberInstance());

	private WallImpl current;
	private String title;
	private Text nameField;

	private IDialogSettings settings;

	public EditWallDialog(Shell parentShell, WallImpl current, String title) {
		super(parentShell, HelpContextIds.EDITWALL_DIALOG);
		this.current = current;
		this.title = title;
		settings = getDialogSettings(UiActivator.getDefault(),SETTINGSID);
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.EditWallDialog_specify_wall_properties);
		updateButtons();
	}

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};

	private Text widthField;

	private Text heightField;

	private WebColorGroup selectColorGroup;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2,false));
		new Label(comp, SWT.NONE).setText(Messages.EditWallDialog_location);

		nameField = new Text(comp, SWT.BORDER);
		nameField
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(modifyListener);

		new Label(comp, SWT.NONE).setText(Messages.EditWallDialog_dimensions);

		final Composite parm = new Composite(comp, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 4;
		parm.setLayout(gridLayout_1);

		new Label(parm, SWT.NONE).setText(Messages.EditWallDialog_width);

		widthField = new Text(parm, SWT.BORDER);
		final GridData gd_widthField = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		gd_widthField.widthHint = 50;
		widthField.setLayoutData(gd_widthField);
		widthField.addModifyListener(modifyListener);
		new Label(parm, SWT.NONE).setText(Messages.EditWallDialog_height);

		heightField = new Text(parm, SWT.BORDER);
		final GridData gd_heightField = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_heightField.widthHint = 50;
		heightField.setLayoutData(gd_heightField);
		heightField.addModifyListener(modifyListener);
		selectColorGroup = new WebColorGroup(comp,
				Messages.EditWallDialog_wall_color);
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
		if (!validDouble(widthField, Messages.EditWallDialog_width_error, 0.5d,
				30d))
			return false;
		if (!validDouble(heightField, Messages.EditWallDialog_height_error,
				0.5d, 5d))
			return false;
		setErrorMessage(null);
		return true;
	}

	private boolean validDouble(Text field, String label, double min, double max) {
		af.setMaximumFractionDigits(3);
		String s = field.getText();
		try {
			double v = af.parse(s).doubleValue();
			if (v > max) {
				setErrorMessage(NLS.bind(
						Messages.EditWallDialog_value_must_not_be_larger,
						label, min));
			} else if (v >= min)
				return true;
			setErrorMessage(NLS.bind(
					Messages.EditWallDialog_value_must_be_larger_equal, label,
					min));
		} catch (ParseException e) {
			setErrorMessage(NLS.bind(
					Messages.EditWallDialog_not_a_valid_number, label));
		}
		return false;
	}

	private void initValues() {
		selectColorGroup.fillValues(settings, WALL_COLOR, 255, 255, 250);
		initNumericControl(widthField, WIDTH, 5d);
		initNumericControl(heightField, HEIGHT, 2.5d);
	}

	private void initNumericControl(Text control, String key, double dflt) {
		af.setMaximumFractionDigits(2);
		try {
			control.setText(af.format(settings.getDouble(key)));
		} catch (NumberFormatException e) {
			control.setText(af.format(dflt));
		}
	}

	private void fillValues() {
		af.setMaximumFractionDigits(2);
		Rgb_type selectedColor = null;
		if (current != null) {
			nameField.setText(current.getLocation());
			selectedColor = current.getColor();
			widthField.setText(af.format(current.getWidth() / 1000d));
			heightField.setText(af.format(current.getHeight() / 1000d));
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
		current.setWidth(stringToMm(widthField));
		current.setHeight(stringToMm(heightField));
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

	private static int stringToMm(Text field) {
		af.setMaximumFractionDigits(3);
		try {
			return (int) (1000 * af.parse(field.getText()).doubleValue());
		} catch (ParseException e) {
			// should not happen
			return 0;
		}
	}

}
