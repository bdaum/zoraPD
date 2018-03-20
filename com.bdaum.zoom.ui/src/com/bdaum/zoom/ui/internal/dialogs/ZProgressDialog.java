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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public abstract class ZProgressDialog extends ZTitleAreaDialog {

	protected ProgressBar progressBar;

	public ZProgressDialog(Shell parentShell) {
		super(parentShell);
	}

	public ZProgressDialog(Shell parentShell, String helpId) {
		super(parentShell, helpId);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createCustomArea(composite);
		progressBar = new ProgressBar(composite, SWT.HORIZONTAL);
		GridData data = new GridData(SWT.FILL, SWT.END, true, false);
		data.heightHint = 5;
		progressBar.setLayoutData(data);
		return area;
	}

	protected abstract void createCustomArea(Composite composite) ;

	protected void setMinMax(int min, int max) {
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);
		progressBar.setVisible(true);
	}


	/**
	 * @return progressBar
	 */
	protected ProgressBar getProgressBar() {
		return progressBar;
	}

}