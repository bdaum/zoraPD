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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;

public class FilterField implements Listener {

	private static final String EMPTY = "empty"; //$NON-NLS-1$
	private static final String ENTER_FILTER_EXPRESSION = Messages.FilterField_enter_filter_expr;
	private Text filterField;
	private WildCardFilter filter;
	private ListenerList<Listener> listeners = new ListenerList<>();

	public FilterField(Composite parent) {
		filterField = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		filterField.setLayoutData(new GridData(300, SWT.DEFAULT));
		filterField.setText(ENTER_FILTER_EXPRESSION);
		filterField.setToolTipText(Messages.FilterField_expressions_entered);
		filterField.setData(EMPTY, Boolean.TRUE);
		filterField.addListener(SWT.Modify, this);
		filterField.addListener(SWT.FocusIn, this);
		filterField.addListener(SWT.FocusOut, this);
	}

	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.FocusIn:
			if (filterField.getData(EMPTY) != null) {
				filterField.removeListener(SWT.Modify, FilterField.this);
				filterField.setText(""); //$NON-NLS-1$
				filterField.addListener(SWT.Modify, FilterField.this);
			}
			return;
		case SWT.FocusOut:
			if (filterField.getData(EMPTY) != null) {
				filterField.removeListener(SWT.Modify, FilterField.this);
				filterField.setText(ENTER_FILTER_EXPRESSION);
				filterField.addListener(SWT.Modify, FilterField.this);
			}
			return;
		default:
			String s = filterField.getText();
			filterField.setData(EMPTY, s.isEmpty() ? Boolean.TRUE : null);
			filter = new WildCardFilter(s + '*', null);
			fireModifyText(e);
		}
	}

	protected void fireModifyText(Event e) {
		e.type = SWT.Modify;
		e.data = this;
		for (Listener l : listeners)
			l.handleEvent(e);
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

	public void setLayoutData(Object layoutData) {
		filterField.setLayoutData(layoutData);
	}

	public WildCardFilter getFilter() {
		return filter;
	}

}
