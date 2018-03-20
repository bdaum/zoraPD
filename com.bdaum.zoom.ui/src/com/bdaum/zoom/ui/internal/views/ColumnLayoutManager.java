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
package com.bdaum.zoom.ui.internal.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

public class ColumnLayoutManager extends ControlAdapter {

	private final int[] columnWidths;
	private final int[] columnMaxWidths;

	public ColumnLayoutManager(ColumnViewer viewer, int[] columnWidths,
			int[] columnMaxWidths) {
		this.columnWidths = columnWidths;
		this.columnMaxWidths = columnMaxWidths;
		viewer.getControl().addControlListener(this);
	}

	@Override
	public void controlResized(ControlEvent e) {
		ScrollBar verticalBar = null;
		TreeColumn[] treeColumns = null;
		TableColumn[] tableColumns = null;
		Widget widget = e.widget;
		Rectangle area = ((Control) widget).getBounds();
		int available = area.width;
		if (widget instanceof Tree) {
			treeColumns = ((Tree) widget).getColumns();
			verticalBar = ((Tree) widget).getVerticalBar();
		} else if (widget instanceof Table) {
			tableColumns = ((Table) widget).getColumns();
			verticalBar = ((Table) widget).getVerticalBar();
		}
		if (verticalBar != null && verticalBar.isVisible())
			available -= verticalBar.getSize().x;
		int w = 0;
		for (int cw : columnWidths)
			w += cw;
		double fac = (double) available / w;
		int[] widths = new int[columnWidths.length];
		int remainder = 0;
		int w2 = 0;
		for (int i = 0; i < widths.length; i++) {
			int cw = (int) (columnWidths[i] * fac);
			if (columnMaxWidths != null && cw > columnMaxWidths[i]) {
				remainder += cw - columnMaxWidths[i];
				cw = columnMaxWidths[i];
			} else
				w2 += columnWidths[i];
			widths[i] = cw;
		}
		while (remainder > 0 && w2 > 0) {
			fac = (double) remainder / w2;
			int remainder2 = 0;
			w2 = 0;
			for (int i = 0; i < widths.length; i++)
				if (columnMaxWidths == null || widths[i] < columnMaxWidths[i]) {
					int cw = (int) (columnWidths[i] * fac);
					if (columnMaxWidths != null && cw > columnMaxWidths[i]) {
						remainder2 += cw - columnMaxWidths[i];
						cw = columnMaxWidths[i];
					} else
						w2 += columnWidths[i];
					widths[i] += cw;
				}
			if (remainder2 == remainder)
				break;
			remainder = remainder2;
		}
		for (int i = 0; i < widths.length; i++)
			if (treeColumns != null)
				treeColumns[i].setWidth(widths[i]);
			else if (tableColumns != null)
				tableColumns[i].setWidth(widths[i]);
	}

}
