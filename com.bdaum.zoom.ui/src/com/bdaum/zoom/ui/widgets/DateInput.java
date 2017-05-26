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
package com.bdaum.zoom.ui.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

/**
 * This class combines a date and a time input
 *
 */
public class DateInput extends Composite implements SelectionListener {

	private DateTime dateField;
	private DateTime timeField;

	private List<SelectionListener> listeners = new ArrayList<SelectionListener>(3);
	private static GregorianCalendar cal = new GregorianCalendar();

	/**
	 * Constructor
	 *
	 * @param parent
	 *            - parent container
	 * @param style
	 *            - style bits - SWT.DATE: show date input - SWT.TIME: show time
	 *            input - other bits: as in org.eclipse.swt.widgets.DateTime
	 */
	public DateInput(Composite parent, int style) {
		super(parent, style & ~(SWT.TIME | SWT.SHORT | SWT.DATE | SWT.MEDIUM | SWT.READ_ONLY | SWT.DROP_DOWN));
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 1;
		setLayout(layout);
		if ((style & SWT.DATE) == 0 && (style & SWT.TIME) == 0)
			style |= (SWT.DATE | SWT.TIME);
		if ((style & SWT.DATE) != 0) {
			dateField = new DateTime(this, style & ~(SWT.TIME | SWT.BORDER));
			dateField.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
		}
		if ((style & SWT.TIME) != 0) {
			timeField = new DateTime(this, ((style & SWT.LONG) != 0) ? SWT.TIME : (SWT.TIME | SWT.SHORT));
			timeField.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
		}
	}

	/**
	 * Returns the entered date
	 *
	 * @return - date
	 */
	public Date getDate() {
		cal.clear();
		if (dateField != null) {
			cal.set(Calendar.YEAR, dateField.getYear());
			cal.set(Calendar.MONTH, dateField.getMonth());
			cal.set(Calendar.DAY_OF_MONTH, dateField.getDay());
		}
		if (timeField != null) {
			cal.set(Calendar.HOUR_OF_DAY, timeField.getHours());
			cal.set(Calendar.MINUTE, timeField.getMinutes());
			if ((timeField.getStyle() & SWT.SHORT) == 0)
				cal.set(Calendar.SECOND, timeField.getSeconds());
		}
		return cal.getTime();
	}

	/**
	 * Sets a date as default
	 *
	 * @param date
	 *            - default date
	 */
	public void setDate(Date date) {
		cal.setTime(date);
		if (dateField != null)
			dateField.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		if (timeField != null)
			timeField.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	}

	/**
	 * Adds a selection listener
	 *
	 * @param listener
	 *            - selection listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			if (dateField != null)
				dateField.addSelectionListener(this);
			if (timeField != null)
				timeField.addSelectionListener(this);
		}
	}

	/**
	 * Remove selection listener
	 *
	 * @param listener
	 *            - selection listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		listeners.remove(listener);
		if (dateField != null)
			dateField.removeSelectionListener(this);
		if (timeField != null)
			timeField.removeSelectionListener(this);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.
	 * eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		e.widget = this;
		for (SelectionListener listener : listeners)
			listener.widgetDefaultSelected(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		e.widget = this;
		for (SelectionListener listener : listeners)
			listener.widgetSelected(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (dateField != null)
			dateField.setEnabled(enabled);
		if (timeField != null)
			timeField.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setBounds(org.eclipse.swt.graphics.
	 * Rectangle )
	 */
	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		layout();
	}

}
