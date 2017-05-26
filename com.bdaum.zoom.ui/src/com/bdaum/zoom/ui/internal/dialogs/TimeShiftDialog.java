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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;

public class TimeShiftDialog extends ZTitleAreaDialog implements SelectionListener {

	private long shift = 0L;
	private Spinner hourField;
	private Spinner minuteField;
	private Spinner secondField;

	public TimeShiftDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.TIMEHSHIFT_DIALOG);
	}

	
	@Override
	public void create() {
		super.create();
		setTitle(Messages.TimeShiftDialog_apply_time_shift);
		setMessage(Messages.TimeShiftDialog_specify_a_shift_value);
		updateButtons();
	}

	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(2, false);
		comp.setLayout(gridLayout);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.TimeShiftDialog_hours);
		hourField = new Spinner(comp, SWT.BORDER);
		hourField.setMinimum(-876600);
		hourField.setMaximum(876600);
		hourField.addSelectionListener(this);
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.TimeShiftDialog_minutes);
		minuteField = new Spinner(comp, SWT.BORDER);
		minuteField.setMinimum(-59);
		minuteField.setMaximum(59);
		minuteField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		minuteField.addSelectionListener(this);
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.TimeShiftDialog_seconds);
		secondField = new Spinner(comp, SWT.BORDER);
		secondField.setMinimum(-59);
		secondField.setMaximum(59);
		secondField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		secondField.addSelectionListener(this);
		return area;
	}

	
	@Override
	protected void okPressed() {
		shift = getTimeShift();
		super.okPressed();
	}

	private long getTimeShift() {
		return ((hourField.getSelection() * 60 + minuteField.getSelection()) * 60 + secondField
				.getSelection()) * 1000L;
	}

	public long getResult() {
		return shift;
	}

	
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	
	public void widgetSelected(SelectionEvent e) {
		updateButtons();
	}

	private void updateButtons() {
		boolean enabled = getTimeShift() != 0;
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

}
