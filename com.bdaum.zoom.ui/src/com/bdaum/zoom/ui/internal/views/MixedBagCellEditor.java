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
 * (c) 2009-2011 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;


import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AbstractListCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.MixedBagCellEditorDialog;

public class MixedBagCellEditor extends AbstractMixedBagDialogCellEditor {

	private Object value;
	private QueryField qfield;

	public MixedBagCellEditor(Composite parent, QueryField qfield) {
		super(parent);
		this.qfield = qfield;
	}

	@Override
	protected Object doGetValue() {
		return value;
	}

	@Override
	protected void doSetValue(Object v) {
		this.value = v;
		super.doSetValue(UiUtilities.csv(v, qfield.getType(), ", ")); //$NON-NLS-1$
	}


	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		AbstractListCellEditorDialog dialog = new MixedBagCellEditorDialog(cellEditorWindow.getShell(), value,
						qfield,  assets != null && assets.size() > 1);
		dialog.create();
		Point location = dialog.getShell().getLocation();
		location.x -= 25;
		location.y += 30;
		dialog.getShell().setLocation(location);
		if (dialog.open() == Window.OK) {
			value = dialog.getResult();
			return value;
		}
		return null;
	}

}
