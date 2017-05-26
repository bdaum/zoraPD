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


package com.bdaum.zoom.ui.internal.views;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.core.QueryField;

public class ViewComboCellEditor extends ComboBoxCellEditor {

	private Object enumeration;

	public ViewComboCellEditor() {
	}

	public ViewComboCellEditor(Composite parent, QueryField qfield) {
		super(parent, qfield.getEnumLabels());
		enumeration = qfield.getEnumeration();
	}

	
	@Override
	protected Object doGetValue() {
		Integer v = (Integer) super.doGetValue();
		if (enumeration instanceof int[])
			return ((int[]) enumeration)[v];
		if (enumeration instanceof String[])
			return ((String[]) enumeration)[v];
		return getItems()[v];
	}

	
	@Override
	protected void doSetValue(Object value) {
		if (enumeration instanceof int[]) {
			int v = ((Integer) value).intValue();
			int[] intEnum = (int[]) enumeration;
			for (int i = 0; i < intEnum.length; i++) {
				if (v == intEnum[i]) {
					super.doSetValue(i);
					return;
				}
			}
			return;
		}
		String[] stringEnum = (enumeration instanceof String[]) ? ((String[]) enumeration)
				: getItems();
		String v = String.valueOf(value);
		for (int i = 0; i < stringEnum.length; i++) {
			if (v.equals(stringEnum[i])) {
				super.doSetValue(i);
				return;
			}
		}
	}
}