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
 * (c) 2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.dialogs.CodesDialog;

public class TopicTextCellEditor extends DialogCellEditor {

	private Object value;
	private QueryField qfield;

	public TopicTextCellEditor(Composite parent, QueryField qfield) {
		super(parent);
		this.qfield = qfield;
	}

	@Override
	protected Object doGetValue() {
		return value;
	}

	@Override
	protected void doSetValue(Object v) {
		super.doSetValue(this.value = v);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		CodesDialog dialog = new CodesDialog(cellEditorWindow.getShell(), qfield, null, null);
		if (dialog.open() == Window.OK)
			return value = dialog.getResult();
		return null;
	}

}