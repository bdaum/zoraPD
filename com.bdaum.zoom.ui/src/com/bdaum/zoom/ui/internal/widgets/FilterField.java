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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;

public class FilterField {

	private static final String EMPTY = "empty"; //$NON-NLS-1$
	private static final String ENTER_FILTER_EXPRESSION = Messages.FilterField_enter_filter_expr;
	private Text filterField;
	private WildCardFilter filter;
	private ListenerList<ModifyListener> listeners = new ListenerList<ModifyListener>();

	public FilterField(Composite parent) {
		filterField = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		filterField.setLayoutData(new GridData(300, SWT.DEFAULT));
		filterField.setText(ENTER_FILTER_EXPRESSION);
		filterField
				.setToolTipText(Messages.FilterField_expressions_entered);
		filterField.setData(EMPTY, Boolean.TRUE);
		final ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String s = filterField.getText();
				if (s.isEmpty())
					filterField.setData(EMPTY, Boolean.TRUE);
				else
					filterField.setData(EMPTY, null);
				filter = new WildCardFilter(s + '*', null);
				fireModifyText(e);
			}
		};
		filterField.addModifyListener(modifyListener);
		filterField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (filterField.getData(EMPTY) != null) {
					filterField.removeModifyListener(modifyListener);
					filterField.setText(ENTER_FILTER_EXPRESSION);
					filterField.addModifyListener(modifyListener);
				}
			}

			public void focusGained(FocusEvent e) {
				if (filterField.getData(EMPTY) != null) {
					filterField.removeModifyListener(modifyListener);
					filterField.setText(""); //$NON-NLS-1$
					filterField.addModifyListener(modifyListener);
				}
			}
		});
	}

	protected void fireModifyText(ModifyEvent e) {
		for (Object l : listeners.getListeners())
			((ModifyListener) l).modifyText(e);
	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	public void setLayoutData(Object layoutData) {
		filterField.setLayoutData(layoutData);
	}

	public WildCardFilter getFilter() {
		return filter;
	}

}
