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
package com.bdaum.zoom.css;

import org.akrogen.tkui.css.core.impl.engine.AbstractCSSEngine;
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
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

import com.bdaum.zoom.css.internal.CssActivator;

public abstract class ZColumnLabelProvider extends OwnerDrawLabelProvider implements ILabelProvider {

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
	private ColumnViewer viewer;
	private AbstractCSSEngine engine;
	private int maxWidth = 0;

	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		this.viewer = viewer;
		viewerControl = (Composite) viewer.getControl();
		engine = CssActivator.getDefault().getCssEngine(viewerControl.getDisplay());
		if (column instanceof TreeViewerColumn)
			colItem = ((TreeViewerColumn) column).getColumn();
		else if (column instanceof TableViewerColumn)
			colItem = ((TableViewerColumn) column).getColumn();
		super.initialize(viewer, column);
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
		Color color = getColor(element, "selected-background-color"); //$NON-NLS-1$
		return color != null ? color : viewerControl.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
	}

	private Color getColor(Object element, String propertyName) {
		return (Color) getResource(viewer.testFindItem(element), propertyName, Color.class);
	}

	private Object getResource(Widget widget, String propertyName, Object toType) {
		CSSValue value = getCSSValue(widget, propertyName);
		if (value != null) {
			try {
				return engine.convert(value, toType, widget.getDisplay());
			} catch (Exception e) {
				engine.handleExceptions(e);
			}
		}
		return null;
	}

	private CSSValue getCSSValue(Widget widget, String propertyName) {
		if (widget == null || engine == null)
			return null;
		Element elt = engine.getElement(widget);
		if (elt == null)
			return null;
		CSSStyleDeclaration styleDeclaration = engine.getViewCSS().getComputedStyle(elt, null);
		if (styleDeclaration == null)
			return null;
		return styleDeclaration.getPropertyCSSValue(propertyName);
	}

	protected Color getSelectedForeground(Object element) {
		Color color = getColor(element, "selected-color"); //$NON-NLS-1$
		return color != null ? color : viewerControl.getForeground();
	}

	protected Color getBackground(Object element) {
		Color color = getColor(element, "background-color"); //$NON-NLS-1$
		return color != null ? color : viewerControl.getBackground();
	}

	protected Color getForeground(Object element) {
		Color color = getColor(element, "color"); //$NON-NLS-1$
		return color != null ? color : viewerControl.getForeground();
	}

	@Override
	public Color getToolTipBackgroundColor(Object element) {
		return getColor(element, "tooltip-background-color"); //$NON-NLS-1$
	}

	@Override
	public Color getToolTipForegroundColor(Object element) {
		return getColor(element, "tooltip-color"); //$NON-NLS-1$
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
			int x = event.x + iconWidth + 4;
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
			String s = sb.append(textValue.substring(0, start)).append(ELLIPSIS)
					.append(textValue.substring(end, length)).toString();
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

}
