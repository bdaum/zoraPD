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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class SortColumnManager {

	public SortColumnManager(ColumnViewer viewer, int[] dfltDir, int dfltCol) {
		Assert.isTrue(dfltDir != null, "dfltDir = null"); //$NON-NLS-1$
		if (viewer instanceof TableViewer) {
			Table table = ((TableViewer) viewer).getTable();
			int columnCount = table.getColumnCount();
			Assert.isTrue(columnCount == dfltDir.length, NLS.bind("dfltDir dimension != {0}", columnCount)); //$NON-NLS-1$
			SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (table.getSortColumn() == e.widget)
						table.setSortDirection(table.getSortDirection() == SWT.DOWN ? SWT.UP : SWT.DOWN);
					else {
						TableColumn newSortColumn = (TableColumn) e.widget;
						table.setSortColumn(newSortColumn);
						for (int i = 0; i < columnCount; i++)
							if (table.getColumn(i) == newSortColumn) {
								table.setSortDirection(dfltDir[i]);
								break;
							}
					}
					viewer.refresh();
					Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (firstElement != null)
						viewer.reveal(firstElement);
				}
			};
			for (int i = 0; i < columnCount; i++)
				if (dfltDir[i] != SWT.NONE)
					table.getColumn(i).addSelectionListener(selectionListener);
			if (dfltCol >= 0 && dfltCol < dfltDir.length && dfltDir[dfltCol] != SWT.NONE) {
				table.setSortColumn(table.getColumn(dfltCol));
				table.setSortDirection(dfltDir[dfltCol]);
			}
		} else if (viewer instanceof TreeViewer) {
			Tree tree = ((TreeViewer) viewer).getTree();
			int columnCount = tree.getColumnCount();
			Assert.isTrue(columnCount == dfltDir.length, NLS.bind("dfltDir dimension != {0}", columnCount)); //$NON-NLS-1$
			SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (tree.getSortColumn() == e.widget)
						tree.setSortDirection(tree.getSortDirection() == SWT.DOWN ? SWT.UP : SWT.DOWN);
					else {
						TreeColumn newSortColumn = (TreeColumn) e.widget;
						tree.setSortColumn(newSortColumn);
						for (int i = 0; i < columnCount; i++)
							if (tree.getColumn(i) == newSortColumn) {
								tree.setSortDirection(dfltDir[i]);
								break;
							}
					}
					Object[] expandedElements = ((TreeViewer) viewer).getExpandedElements();
					viewer.refresh();
					((TreeViewer) viewer).setExpandedElements(expandedElements);
					Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (firstElement != null)
						viewer.reveal(firstElement);
				}
			};
			for (int i = 0; i < columnCount; i++)
				if (dfltDir[i] != SWT.NONE)
					tree.getColumn(i).addSelectionListener(selectionListener);
			if (dfltCol >= 0 && dfltCol < dfltDir.length && dfltDir[dfltCol] != SWT.NONE) {
				tree.setSortColumn(tree.getColumn(dfltCol));
				tree.setSortDirection(dfltDir[dfltCol]);
			}
		}
	}

}
