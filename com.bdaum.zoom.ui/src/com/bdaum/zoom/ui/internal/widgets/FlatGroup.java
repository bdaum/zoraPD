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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.ui.widgets.Messages;

public class FlatGroup extends Composite {

	private final String field;
	private final IDialogSettings settings;
	private RadioButtonGroup modeButtonGroup;

	public FlatGroup(Composite parent, int style, IDialogSettings settings, String field) {
		super(parent, style);
		this.settings = settings;
		this.field = field;
		setLayout(new FillLayout());
		modeButtonGroup = new RadioButtonGroup(this, null, SWT.HORIZONTAL, Messages.FlatGroup_flat,
				Messages.FlatGroup_hierarchical);
		modeButtonGroup.setSelection(settings.getBoolean(field) ? 1 : 0);
	}

	public void addSelectionListener(SelectionListener listener) {
		modeButtonGroup.addSelectionListener(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		modeButtonGroup.removeSelectionListener(listener);
	}

	public void saveSettings() {
		settings.put(field, modeButtonGroup.getSelection() == 1);
	}

	public boolean isFlat() {
		return modeButtonGroup.getSelection() == 0;
	}

}
