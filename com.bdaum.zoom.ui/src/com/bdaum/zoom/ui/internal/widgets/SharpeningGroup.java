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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class SharpeningGroup {

	private static final String RADIUS = "radius"; //$NON-NLS-1$
	private static final String AMOUNT = "amount"; //$NON-NLS-1$
	private static final String THRESHOLD = "threshold"; //$NON-NLS-1$
	private static final String APPLYSHARPENING = "applySharpening"; //$NON-NLS-1$

	private NumericControl radiusField;
	private NumericControl amountField;
	private NumericControl threshholdField;
	private CheckboxButton applyButton;
	private Composite group;

	public SharpeningGroup(Composite parent) {
		group = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = (GridLayout) parent.getLayout();
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false,
				gridLayout.numColumns, 1));
		GridLayout layout = new GridLayout(6, false);
		layout.horizontalSpacing = 15;
		layout.marginWidth = 0;
		layout.marginHeight = 3;
		group.setLayout(layout);
		applyButton = WidgetFactory.createCheckButton(group,
				Messages.SharpeningGroup_apply_sharpening, new GridData(
						SWT.BEGINNING, SWT.CENTER, true, false, 6, 1));
		applyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateControls();
				if (radiusField.getEnabled())
					radiusField.setFocus();
			}
		});
		Label radiusLabel = new Label(group, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = 15;
		radiusLabel.setLayoutData(layoutData);
		radiusLabel.setText(Messages.SharpeningGroup_radius);
		radiusField = new NumericControl(group, SWT.NONE);
		radiusField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		radiusField.setMinimum(5);
		radiusField.setMaximum(50);
		radiusField.setIncrement(1);
		radiusField.setDigits(1);
		new Label(group, SWT.NONE).setText(Messages.SharpeningGroup_amount);
		amountField = new NumericControl(group, SWT.NONE);
		amountField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		amountField.setMinimum(0);
		amountField.setMaximum(200);
		amountField.setIncrement(10);
		amountField.setDigits(2);
		new Label(group, SWT.NONE).setText(Messages.SharpeningGroup_threshhold);
		threshholdField = new NumericControl(group, SWT.NONE);
		threshholdField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		threshholdField.setMinimum(0);
		threshholdField.setMaximum(25);
		threshholdField.setIncrement(1);
	}

	public void updateControls() {
		boolean sharpenEnabled = applyButton.getEnabled()
				&& applyButton.getSelection();
		radiusField.setEnabled(sharpenEnabled);
		amountField.setEnabled(sharpenEnabled);
		threshholdField.setEnabled(sharpenEnabled);
		if (sharpenEnabled)
			radiusField.setFocus();
	}

	public void setEnabled(boolean sharpenEnabled) {
		applyButton.setEnabled(sharpenEnabled);
		updateControls();
	}

	public void fillValues(IDialogSettings settings) {
		if (settings == null) {
			radiusField.setSelection(15);
			amountField.setSelection(26);
			threshholdField.setSelection(2);
		} else {
			try {
				radiusField.setSelection(settings.getInt(RADIUS));
			} catch (NumberFormatException e) {
				radiusField.setSelection(15);
			}
			try {
				amountField.setSelection(settings.getInt(AMOUNT));
			} catch (NumberFormatException e) {
				amountField.setSelection(26);
			}
			try {
				threshholdField.setSelection(settings.getInt(THRESHOLD));
			} catch (NumberFormatException e) {
				threshholdField.setSelection(2);
			}
			applyButton.setSelection(settings.getBoolean(APPLYSHARPENING));
		}
		updateControls();
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(RADIUS, radiusField.getSelection());
		settings.put(AMOUNT, amountField.getSelection());
		settings.put(THRESHOLD, threshholdField.getSelection());
		settings.put(APPLYSHARPENING, applyButton.getSelection());
	}

	public UnsharpMask getUnsharpMask() {
		return applyButton.getSelection() ? ImageActivator.getDefault()
				.computeUnsharpMask(radiusField.getSelection() / 10f,
						amountField.getSelection() / 100f,
						threshholdField.getSelection()) : null;
	}

	public void fillValues(boolean applySharpening, float radius, float amount,
			int threshold) {
		applyButton.setSelection(applySharpening);
		radiusField.setSelection((int) (radius * 10));
		amountField.setSelection((int) (amount * 100));
		threshholdField.setSelection(threshold);
		updateControls();
	}

	public boolean getApplySharpening() {
		return applyButton.getSelection();
	}

	public float getRadius() {
		return radiusField.getSelection() / 10f;
	}

	public float getAmount() {
		return amountField.getSelection() / 100f;
	}

	public int getThreshold() {
		return threshholdField.getSelection();
	}

	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Control#isEnabled()
	 */
	public boolean isEnabled() {
		return applyButton.isEnabled();
	}

	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Control#isVisible()
	 */
	public boolean isVisible() {
		return group.isVisible();
	}

	/**
	 * @param visible
	 * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		group.setVisible(visible);
	}

}
