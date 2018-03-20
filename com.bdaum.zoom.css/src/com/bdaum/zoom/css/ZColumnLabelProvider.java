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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.css;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.css.internal.IColumnLabelColorModel;
import com.bdaum.zoom.css.internal.IThemeListener;

public abstract class ZColumnLabelProvider extends OwnerDrawLabelProvider
		implements ILabelProvider, IColumnLabelColorModel, IThemeListener {

	private static ZColumnLabelProvider instance;

	public static ZColumnLabelProvider getDefaultInstance() {
		if (instance == null)
			instance = new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return element.toString();
				}

			};
		return instance;
	}

	private static final String ELLIPSIS = "..."; //$NON-NLS-1$
	private static final int MSECPERCHAR = 50;
	private static final int MINTOOLTIPTIME = 500;
	private Item colItem;
	private Composite viewerControl;
	private String tooltip;
	private int maxWidth = 0;
	private Color disabledForegroundColor;
	private Color toolTipBackgroundColor;
	private Color toolTipForegroundColor;
	private Color selectedBackgroundColor;
	private Color selectedForegroundColor;

	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		viewerControl = (Composite) viewer.getControl();
		CssActivator activator = CssActivator.getDefault();
		activator.applyExtendedStyle(viewerControl, this);
		activator.addThemeListener(this);
		if (column instanceof TreeViewerColumn)
			colItem = ((TreeViewerColumn) column).getColumn();
		else if (column instanceof TableViewerColumn)
			colItem = ((TableViewerColumn) column).getColumn();
		super.initialize(viewer, column);
	}
	
	@Override
	public void dispose() {
		CssActivator.getDefault().removeThemeListener(this);
		super.dispose();
	}

	@Override
	protected void measure(Event event, Object element) {
		Rectangle size = getIconBounds();
		event.height = size == null ? 16 : size.height;
		event.width = getColumnWidth();
	}

	private int getColumnWidth() {
		if (colItem instanceof TreeColumn)
			return ((TreeColumn) colItem).getWidth();
		if (colItem instanceof TableColumn)
			return ((TableColumn) colItem).getWidth();
		return viewerControl.getClientArea().width;
	}

	protected Rectangle getIconBounds() {
		return null;
	}

	@Override
	protected void erase(Event event, Object element) {
		if (maxWidth < event.width)
			maxWidth = event.width;
		event.gc.setBackground(
				(event.detail & SWT.SELECTED) != 0 ? getSelectedBackground(element) : getBackground(element));
		event.gc.fillRectangle(event.x, event.y, maxWidth, event.height);
	}

	protected Color getSelectedBackground(Object element) {
		return selectedBackgroundColor;
	}

	protected Color getSelectedForeground(Object element) {
		return selectedForegroundColor;
	}

	protected Color getDisabledForeground(Object element) {
		return disabledForegroundColor;
	}

	protected Color getBackground(Object element) {
		return viewerControl.getBackground();
	}

	protected Color getForeground(Object element) {
		return viewerControl.getForeground();
	}

	@Override
	public Color getToolTipBackgroundColor(Object element) {
		return toolTipBackgroundColor;
	}

	@Override
	public Color getToolTipForegroundColor(Object element) {
		return toolTipForegroundColor;
	}

	protected void paint(Event event, Object element) {
		GC gc = event.gc;
		tooltip = null;
		Rectangle iconSize = getIconBounds();
		Image image = getImage(element);
		int iconWidth = iconSize != null ? iconSize.width : -1;
		int iconHeight = iconSize != null ? iconSize.height : -1;
		int rowHeight = Math.max(iconHeight, event.height);
		double fac = 1d;
		if (image != null) {
			if (iconSize == null) {
				iconSize = image.getBounds();
				iconWidth = iconSize.width;
				iconHeight = iconSize.height;
				rowHeight = Math.max(iconHeight, event.height);
			} else {
				Rectangle bounds = image.getBounds();
				fac = Math.min(((double) iconWidth) / bounds.width, ((double) iconHeight) / bounds.height);
				iconSize = bounds;
				iconWidth = iconSize.width;
				iconHeight = iconSize.height;
				rowHeight = Math.max(iconHeight, event.height);
			}
		}
		String text = getText(element);
		if (text != null) {
			Font customFont;
			Color foreground, background;
			if ((event.detail & SWT.SELECTED) != 0) {
				customFont = getSelectionFont(element);
				foreground = getSelectedForeground(element);
				background = getSelectedBackground(element);
			} else {
				customFont = getFont(element);
				foreground = getForeground(element);
				background = getBackground(element);
			}
			if (customFont != null)
				gc.setFont(customFont);
			else if ((event.detail & SWT.SELECTED) != 0)
				gc.setFont(JFaceResources.getFontRegistry().get("com.bdaum.zoom.selectionFont")); //$NON-NLS-1$
			else
				gc.setFont(JFaceResources.getDefaultFont());
			gc.setForeground(foreground);
			gc.setBackground(background);
			Point textExtent = gc.textExtent(text);
			if (iconHeight < 0)
				iconHeight = event.height;
			int x = (int) (event.x + iconWidth * fac + 4);
			int mxWidth = getColumnWidth() - x + getColumnXOrigin();
			String shortened = shortenText(element, text, textExtent.x, gc, mxWidth);
			if (shortened.length() < text.length())
				tooltip = text;
			int ty = event.y + (rowHeight - textExtent.y) / 2;
			if (element instanceof ITitle) {
				Point tx = gc.stringExtent(shortened);
				int len = tx.x;
				if (len < mxWidth)
					x += (mxWidth - len) / 2;
				int th = ty + tx.y / 2;
				gc.drawLine(0, th, mxWidth, th);
				gc.drawLine(0, th + 2, mxWidth, th + 2);
			}
			gc.drawText(shortened, x, ty, (event.detail & SWT.SELECTED) != 0);
		}
		if (image != null) {
			if (iconSize == null)
				gc.drawImage(image, event.x, (rowHeight - iconHeight) / 2);
			else {
				int w = (int) (iconWidth * fac);
				int h = (int) (iconHeight * fac);
				gc.drawImage(image, 0, 0, iconWidth, iconHeight, event.x + (iconWidth - w) / 2,
						event.y + (rowHeight - h) / 2, w, h);
			}
		}
		viewerControl.setToolTipText(tooltip);
	}

	protected Font getSelectionFont(Object element) {
		return null;
	}

	private int getColumnXOrigin() {
		int x = 0;
		if (viewerControl instanceof Table) {
			Table table = (Table) viewerControl;
			int[] columnOrder = table.getColumnOrder();
			for (int colIndex : columnOrder) {
				TableColumn column = table.getColumn(colIndex);
				if (column == colItem)
					return x;
				int width = column.getWidth();
				x += width;
			}
		} else if (viewerControl instanceof Tree) {
			Tree tree = (Tree) viewerControl;
			int[] columnOrder = tree.getColumnOrder();
			for (int colIndex : columnOrder) {
				TreeColumn column = tree.getColumn(colIndex);
				if (column == colItem)
					return x;
				int width = column.getWidth();
				x += width;
			}
		}
		return x;
	}

	protected String shortenText(Object element, String textValue, int maxExtent, GC gc, int maxWidth) {
		if (maxExtent < maxWidth)
			return textValue;
		int length = textValue.length();
		int charsToClip = Math.min(length - 6, Math.round(0.95f * length * (1 - ((float) maxWidth / maxExtent))));
		int pivot = length / 2;
		int start = pivot - (charsToClip / 2);
		int end = pivot + (charsToClip / 2) + 1;
		StringBuilder sb = new StringBuilder();
		while (start >= 0 && end < length) {
			String s = sb.append(textValue, 0, start).append(ELLIPSIS).append(textValue, end, length).toString();
			if (gc.textExtent(s).x < maxWidth)
				return s;
			sb.setLength(0);
			start--;
			end++;
		}
		return textValue;
	}

	protected Font getFont(Object element) {
		if (element instanceof ITitle)
			return JFaceResources.getBannerFont();
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		return tooltip;
	}

	@Override
	public int getToolTipTimeDisplayed(Object element) {
		String text = getToolTipText(element);
		return text == null ? 0 : Math.max(MINTOOLTIPTIME, text.length() * MSECPERCHAR);
	}

	public String getText(Object element) {
		return null;
	}

	public Image getImage(Object element) {
		return null;
	}

	@Override
	public void setDisabledForegroundColor(Color c) {
		disabledForegroundColor = c != null ? c : viewerControl.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
	}

	@Override
	public void setToolTipBackgroundColor(Color c) {
		toolTipBackgroundColor = c;
	}

	@Override
	public void setToolTipForegroundColor(Color c) {
		toolTipForegroundColor = c;
	}

	@Override
	public void setSelectedBackgroundColor(Color c) {
		selectedBackgroundColor = c != null ? c : viewerControl.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);

	}

	@Override
	public void setSelectedForegroundColor(Color c) {
		selectedForegroundColor = c != null ? c : viewerControl.getForeground();
	}

	@Override
	public boolean applyColorsTo(Object element) {
		return element instanceof Tree || element instanceof Table;
	}
	

	@Override
	public void themeChanged() {
		CssActivator.getDefault().applyExtendedStyle(viewerControl, this);
	}


}
