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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.ZViewerComparator;

public class FtpAccountSelectionDialog extends ZTitleAreaDialog implements ISelectionChangedListener {

	private TableViewer ftpViewer;
	private FtpAccount account;

	public FtpAccountSelectionDialog(Shell parentShell) {
		super(parentShell);
	}


	@Override
	public void create() {
		super.create();
		setMessage(Messages.FtpAccountSelectionDialog_please_select);
		updateButtons();
	}

	private void updateButtons() {
		getButton(IDialogConstants.OK_ID).setEnabled(!ftpViewer.getSelection().isEmpty());
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		ftpViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.SINGLE);
		ftpViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		ftpViewer.setContentProvider(ArrayContentProvider.getInstance());
		ftpViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FtpAccount)
					return ((FtpAccount) element).getName();
				return element.toString();
			}
		});
		ftpViewer.setComparator(ZViewerComparator.INSTANCE);
		ftpViewer.addSelectionChangedListener(this);
		List<FtpAccount> ftpAccounts = FtpAccount.getAllAccounts();
		ftpViewer.setInput(ftpAccounts);
		return area;
	}


	@Override
	protected void okPressed() {
		account = (FtpAccount) ftpViewer.getStructuredSelection().getFirstElement();
		super.okPressed();
	}

	public FtpAccount getResult() {
		return account;
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

}
