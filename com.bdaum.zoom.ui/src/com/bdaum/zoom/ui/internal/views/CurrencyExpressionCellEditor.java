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

package com.bdaum.zoom.ui.internal.views;

import java.text.ParseException;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;

public class CurrencyExpressionCellEditor extends TextCellEditor {

	IFormatter formatter = Format.currencyExpressionFormatter;

	public CurrencyExpressionCellEditor(Composite parent) {
		super(parent);
		setValidator(new ICellEditorValidator() {
			public String isValid(Object value) {
				if (value != null) {
					try {
						formatter.fromString(value.toString());
					} catch (ParseException e1) {
						return Messages
								.getString("ExpressionCellEditor.bad_arithmetic_expr"); //$NON-NLS-1$
					}
				}
				return null;
			}
		});
	}

	@Override
	protected Object doGetValue() {
		Object v = super.doGetValue();
		try {
			return v == null ? null : formatter.fromString(v.toString());
		} catch (ParseException e) {
			// should never happen
			return 0d;
		}
	}

	@Override
	protected void doSetValue(Object value) {
		super.doSetValue(formatter.toString(value));
	}
}
