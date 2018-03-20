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
package com.bdaum.zoom.gps.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.dialogs.ZTrayDialog;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;

@SuppressWarnings("restriction")
public class DirPinDialog extends ZTrayDialog {
	
	public static final int DIRECTION = 0;
	public static final int LOCATIONSHOWN = 1;

	private RadioButtonGroup buttonGroup;
	private int result;

	public DirPinDialog(Shell parent) {
		super(parent);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(comp, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(1, false));
		buttonGroup = new RadioButtonGroup(area, Messages.DirPinDialog_select, SWT.VERTICAL,
				Messages.DirPinDialog_direction, Messages.DirPinDialog_location);
		buttonGroup.setSelection(DIRECTION);
		return comp;
	}
	
	@Override
	protected void okPressed() {
		result = buttonGroup.getSelection();
		super.okPressed();
	}
	
	public int getResult() {
		return result;
	}

}
