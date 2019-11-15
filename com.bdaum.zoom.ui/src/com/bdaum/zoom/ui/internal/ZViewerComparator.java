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

import java.util.Comparator;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.bdaum.zoom.css.ZColumnLabelProvider;

public class ZViewerComparator extends ViewerComparator {

	private Comparator<String> comparator;

	public ZViewerComparator() {
		this(UiUtilities.stringComparator);
	}

	public ZViewerComparator(Comparator<String> comparator) {
		super(comparator);
		this.comparator = comparator;
	}

	public static final ViewerComparator INSTANCE = new ZViewerComparator();

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof ColumnViewer) {
			int colIndex = 0;
			int sortDir = SWT.UP;
			ColumnViewer columnViewer = (ColumnViewer) viewer;
			if (columnViewer instanceof TableViewer) {
				Table table = ((TableViewer) columnViewer).getTable();
				TableColumn sortColumn = table.getSortColumn();
				if (sortColumn != null) {
					sortDir = table.getSortDirection();
					for (int i = 0; i < table.getColumnCount(); i++) {
						if (table.getColumn(i) == sortColumn) {
							colIndex = i;
							break;
						}
					}
				}
			} else if (columnViewer instanceof TreeViewer) {
				Tree tree = ((TreeViewer) columnViewer).getTree();
				TreeColumn sortColumn = tree.getSortColumn();
				if (sortColumn != null) {
					sortDir = tree.getSortDirection();
					for (int i = 0; i < tree.getColumnCount(); i++) {
						if (tree.getColumn(i) == sortColumn) {
							colIndex = i;
							break;
						}
					}
				}
			}
			int dir = sortDir == SWT.DOWN ? -1 : 1;
			CellLabelProvider labelProvider = columnViewer.getLabelProvider(colIndex);
			if (labelProvider instanceof ColumnLabelProvider)
				return dir * comparator.compare(((ColumnLabelProvider) labelProvider).getText(e1),
						((ColumnLabelProvider) labelProvider).getText(e2));
			if (labelProvider instanceof ZColumnLabelProvider)
				return dir * comparator.compare(((ZColumnLabelProvider) labelProvider).getText(e1),
						((ZColumnLabelProvider) labelProvider).getText(e2));
		}
		return super.compare(viewer, e1, e2);
	}
}