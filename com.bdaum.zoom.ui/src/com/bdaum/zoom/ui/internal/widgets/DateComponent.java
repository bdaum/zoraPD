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
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.ui.widgets.DateInput;

public class DateComponent extends Composite {

	private StackLayout stackLayout;
	private DateInput dateInput;
	private Label label;

	public DateComponent(Composite parent, int style) {
		super(parent, style);
		stackLayout = new StackLayout();
		setLayout(stackLayout);
		dateInput = new DateInput(this, style);
		label = new Label(this, SWT.BORDER);
		label.setAlignment(SWT.CENTER);
		stackLayout.topControl = dateInput;
	}

	/**
	 * Returns the selected date
	 * @return the selected Date
	 */
	public Date getSelection() {
		return dateInput == stackLayout.topControl ? dateInput.getDate() : null;
	}

	/**
	 * @param date
	 * @see com.bdaum.zoom.ui.widgets.DateInput#setDate(java.util.Date)
	 */
	public void setSelection(Object date) {
		if (date instanceof Date) {
			dateInput.setDate((Date) date);
			stackLayout.topControl = dateInput;
		} else {
			label.setText(date.toString());
			stackLayout.topControl = label;
		}
		layout(true,true);
	}

	public void addListener(int type, Listener listener) {
		dateInput.addListener(type, listener);
	}

	/**
	 * @param enabled
	 * @see com.bdaum.zoom.ui.widgets.DateInput#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		dateInput.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public void removeListener(int type, Listener listener) {
		dateInput.removeListener(type, listener);
	}

}
