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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.QueryField;

public class ViewTextCellEditor extends TextCellEditor {

	public ViewTextCellEditor() {
	}

	public ViewTextCellEditor(Composite parent, final QueryField qfield, final Asset asset) {
		super(parent);
		setValidator(new ICellEditorValidator() {
			public String isValid(Object value) {
				if (value != null) {
					IFormatter formatter = qfield.getFormatter();
					if (formatter != null) {
						try {
							formatter.fromString(value.toString());
							return null;
						} catch (ParseException e) {
							return Messages.getString("ViewTextCellEditor.bad_field_format"); //$NON-NLS-1$
						}
					}
					int maxLen = qfield.getMaxlength();
					if (value.toString().length() > maxLen)
						return NLS.bind(Messages.getString("ViewTextCellEditor.string_too_long"), //$NON-NLS-1$
								maxLen);
					return qfield.isValid(value, asset);
				}
				return null;
			}
		});
	}

}