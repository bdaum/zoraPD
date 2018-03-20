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
 * (c) 2011 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public abstract class AbstractListCellEditorDialog extends ZTitleAreaDialog {

	protected Object value;
	protected QueryField qfield;

	public AbstractListCellEditorDialog(Shell parentShell, Object value, QueryField qfield) {
		super(parentShell);
		this.value = value;
		this.qfield = qfield;
	}

	@Override
	public void create() {
		super.create();
		setTitle(qfield.getLabelWithUnit());
	}

	public Object getResult() {
		return value;
	}
}