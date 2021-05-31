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
package com.bdaum.zoom.gps.widgets;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.widgets.ZDialog;

@SuppressWarnings("restriction")
public class LocationSelectionDialog extends ZDialog implements Listener {

	private final Object[] items;
	protected String result;

	public LocationSelectionDialog(Shell parent, Object[] items) {
		super(parent);
		setShellStyle(SWT.NO_TRIM);
		this.items = items;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(1, false));
		ListViewer viewer = new ListViewer(area, SWT.V_SCROLL | SWT.SINGLE);
		viewer.add(items);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				result = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
				close();
			}
		});
		viewer.getControl().addListener(SWT.FocusOut, this);
		Shell shell = getShell();
		shell.pack();
		shell.layout();
		viewer.getControl().setFocus();
		return area;
	}

	@Override
	public void handleEvent(Event event) {
		result = null;
		close();
	}

	public String getResult() {
		return result;
	}
}
