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
 * (c) 2009-2015 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class ColorCodeDialog extends ZDialog implements Listener {

	public static final int SELECTABORT = -2;
	private int code;
	private ColorCodeGroup colorCodeGroup;

	public ColorCodeDialog(Shell parentShell, int code) {
		super(parentShell);
		setShellStyle(SWT.NO_TRIM);
		this.code = code;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Shell shell = getShell();
		colorCodeGroup = new ColorCodeGroup(parent, SWT.NONE, code);
		colorCodeGroup.addListener(SWT.FocusOut, this);
		colorCodeGroup.addListener(this);
		shell.pack();
		shell.layout();
		colorCodeGroup.setFocus();
		return colorCodeGroup;
	}

	@Override
	public int open() {
		setReturnCode(CANCEL);
		return (super.open() != CANCEL) ? code : SELECTABORT;
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.FocusOut:
			code = SELECTABORT;
			close();
			return;
		default:
			setReturnCode(OK);
			Shell shell = getShell();
			code = colorCodeGroup.getCode();
			shell.getDisplay().timerExec(100, () -> {
				if (!shell.isDisposed())
					close();
			});
		}

	}
}
