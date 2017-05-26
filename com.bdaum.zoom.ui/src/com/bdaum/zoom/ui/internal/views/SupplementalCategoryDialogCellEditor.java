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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.dialogs.SupplementalCategoryDialog;

public class SupplementalCategoryDialogCellEditor extends AbstractMixedBagDialogCellEditor {


	public SupplementalCategoryDialogCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		SupplementalCategoryDialog dialog = new SupplementalCategoryDialog(cellEditorWindow
				.getShell(), doGetValue(), assets != null && assets.size() > 1);
		if (dialog.open() == Window.OK)
			return dialog.getResult();
		return null;
	}

	@Override
	protected void updateContents(Object value) {
		if (value instanceof String[])
			value = Core.toStringList((String[]) value, ";"); //$NON-NLS-1$
		super.updateContents(value);
	}


}