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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class StatusDialog extends ZDialog {

	public static final int ABORT = -1;

	private int status;

	public StatusDialog(Shell parentShell, int status) {
		super(parentShell);
		setShellStyle(SWT.NO_TRIM);
		this.status = status;
	}

	@Override
	public void create() {
		super.create();
		Shell shell = getShell();
		shell.layout();
		shell.pack();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new FillLayout());
		RadioButtonGroup stateButtonGroup = new RadioButtonGroup(area, null, SWT.BORDER);
		final int[] states = (int[]) QueryField.STATUS.getEnumeration();
		String[] labels = QueryField.STATUS.getEnumLabels();
		int k = 0;
		for (int i = 0; i < states.length; i++) {
			if (states[i] < 0)
				continue;
			final int index = i;
			stateButtonGroup.addButton(labels[i]);
			if (status == states[i])
				stateButtonGroup.setSelection(k);
			stateButtonGroup.setData(k++, "index", index); //$NON-NLS-1$
		}
		stateButtonGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer index = (Integer) stateButtonGroup.getData(e.detail, "index"); //$NON-NLS-1$
				status = states[index];
			}
		});
		return area;
	}

	@Override
	protected void okPressed() {
		// do nothing
	}

	@Override
	public int open() {
		int open = super.open();
		if (open == CANCEL)
			return ABORT;
		return status;
	}

}
