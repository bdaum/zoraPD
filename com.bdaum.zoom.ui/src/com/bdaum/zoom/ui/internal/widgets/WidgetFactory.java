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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class WidgetFactory {

	public static CheckboxButton createCheckButton(Composite parent, String label, Object layoutData) {
		return createCheckButton(parent, label, layoutData, null);
	}

	public static CheckboxButton createCheckButton(Composite parent, String label, Object layoutData, String tooltip) {
		CheckboxButton button = new CheckboxButton(parent, label, SWT.NONE);
		if (layoutData != null)
			button.setLayoutData(layoutData);
		if (tooltip != null)
			 button.setToolTipText(tooltip);
		return button;
	}

	public static Button createPushButton(Composite parent, String label, int align) {
		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(align, SWT.CENTER, false, false));
		button.setText(label);
		return button;
	}

}
