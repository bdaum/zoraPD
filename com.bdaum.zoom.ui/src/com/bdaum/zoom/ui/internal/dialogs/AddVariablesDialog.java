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
 * (c) 2009-2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class AddVariablesDialog extends ZDialog {
	String var = null;
	private List list;
	private String[] vlist;
	private final String title;
	private String[] vlabels;

	public AddVariablesDialog(Shell parentShell, String title, String[] vlist, String[] vlabels) {
		super(parentShell);
		this.title = title;
		this.vlist = vlist;
		this.vlabels = vlabels;
	}

	@Override
	public void create() {
		super.create();
		validate();
	}

	public String getResult() {
		return var;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		list = new List(area, SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = list.getSelectionIndex();
				var = i < 0 ? null : vlist[i];
				validate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (var != null)
					okPressed();
			}
		});
		list.setLayoutData(new GridData(250, 200));
		String[] compList = new String[vlist.length];
		for (int i = 0; i < vlist.length; i++) {
			String varName = vlist[i];
			if (varName.startsWith("{") && varName.endsWith("}")) { //$NON-NLS-1$//$NON-NLS-2$
				varName = varName.substring(1, varName.length() - 1);
				if (vlabels == null)
					compList[i] = vlist[i] + TemplateMessages.getString(TemplateMessages.PREFIX + varName);
				else
					compList[i] = vlist[i] + " - " + vlabels[i]; //$NON-NLS-1$
			}
		}
		list.setItems(compList);
		return area;
	}

	protected void validate() {
		getButton(OK).setEnabled(var != null);
	}
}