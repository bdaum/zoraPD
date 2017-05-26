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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.css.internal.CssActivator;

public class ZTrayDialog extends TrayDialog {

	protected String helpId;

	public ZTrayDialog(Shell parent) {
		this(parent, null);
	}

	public ZTrayDialog(Shell parent, String helpId) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.helpId = helpId;
		setHelpAvailable(helpId != null);
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		CssActivator.getDefault().setColors(getShell());
	}

}
