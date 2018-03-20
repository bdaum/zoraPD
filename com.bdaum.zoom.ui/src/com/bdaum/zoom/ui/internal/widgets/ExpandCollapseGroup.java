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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.bdaum.zoom.ui.internal.Icons;

public class ExpandCollapseGroup {

	private TreeViewer treeViewer;
	private ToolBar toolBar;

	public ExpandCollapseGroup(Composite parent, int style) {
		this(parent, style, null);
	}
	
	public ExpandCollapseGroup(Composite parent, int style, Object layoutData) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolBar.setLayoutData(layoutData != null ? layoutData : new GridData(SWT.END, SWT.BEGINNING, true, false));
		ToolItem collapseItem = new ToolItem(toolBar, SWT.PUSH);
		collapseItem.setImage(Icons.collapseAll.getImage());
		collapseItem.setToolTipText(Messages.ExpandCollapseGroup_collapseall);
		collapseItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (treeViewer != null)
					treeViewer.collapseAll();
			}
		});
		ToolItem expandItem = new ToolItem(toolBar, SWT.PUSH);
		expandItem.setImage(Icons.expandAll.getImage());
		expandItem.setToolTipText(Messages.ExpandCollapseGroup_expandall);
		expandItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (treeViewer != null)
					treeViewer.expandAll();
			}
		});
	}

	public void setViewer(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	public void setLayoutData(Object layoutData) {
		toolBar.setLayoutData(layoutData);
	}

	public void setVisible(boolean visible) {
		toolBar.setVisible(visible);
	}

}
