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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class SpinnerComponent extends Composite {

	private StackLayout stackLayout;
	private Spinner spinner;
	private Label label;

	public SpinnerComponent(Composite parent, int style) {
		super(parent, style);
		stackLayout = new StackLayout();
		setLayout(stackLayout);
		spinner = new Spinner(this, style);
		label = new Label(this, SWT.BORDER);
		label.setAlignment(SWT.CENTER);
		stackLayout.topControl = spinner;
	}

	/**
	 * @return
	 * @see com.bdaum.zoom.ui.widgets.DateInput#getDate()
	 */
	public Integer getSelection() {
		return spinner == stackLayout.topControl ? spinner.getSelection() : null;
	}

	/**
	 * @param value
	 * @see com.bdaum.zoom.ui.widgets.DateInput#setDate(java.util.Date)
	 */
	public void setSelection(Object value) {
		if (value instanceof Integer) {
			spinner.setSelection((Integer) value);
			stackLayout.topControl = spinner;
		} else {
			label.setText(value.toString());
			stackLayout.topControl = label;
		}
		layout(true,true);
	}

	/**
	 * @param listener
	 * @see com.bdaum.zoom.ui.widgets.DateInput#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		spinner.addSelectionListener(listener);
	}

	/**
	 * @param enabled
	 * @see com.bdaum.zoom.ui.widgets.DateInput#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		spinner.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		spinner.addKeyListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Spinner#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		spinner.removeSelectionListener(listener);
	}

	/**
	 * @param value
	 * @see org.eclipse.swt.widgets.Spinner#setDigits(int)
	 */
	public void setDigits(int value) {
		spinner.setDigits(value);
	}

	/**
	 * @param value
	 * @see org.eclipse.swt.widgets.Spinner#setIncrement(int)
	 */
	public void setIncrement(int value) {
		spinner.setIncrement(value);
	}

	/**
	 * @param value
	 * @see org.eclipse.swt.widgets.Spinner#setMaximum(int)
	 */
	public void setMaximum(int value) {
		spinner.setMaximum(value);
	}

	/**
	 * @param value
	 * @see org.eclipse.swt.widgets.Spinner#setMinimum(int)
	 */
	public void setMinimum(int value) {
		spinner.setMinimum(value);
	}

	/**
	 * @param value
	 * @see org.eclipse.swt.widgets.Spinner#setPageIncrement(int)
	 */
	public void setPageIncrement(int value) {
		spinner.setPageIncrement(value);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		spinner.removeKeyListener(listener);
	}

}
