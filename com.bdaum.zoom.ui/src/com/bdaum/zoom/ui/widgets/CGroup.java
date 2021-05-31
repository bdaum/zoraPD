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

package com.bdaum.zoom.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Implements a group container similar to org.eclipse.swt.widgets.Group Better
 * behavior in regards to styling (title color can be changed)
 *
 */
public class CGroup extends Composite implements Listener {

	private static final int archHeight = 5;
	private static final int arcWidth = 10;
	private static final int indent = 8;
	private final static int inset = 1;
	private final static int margins = 4;
	private String text;
	private int th = 0;
	private int tw;
	
	public static CGroup create(Composite parent, int cols, String text) {
		CGroup cGroup = new CGroup(parent, SWT.NONE);
		cGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, cols, 1));
		cGroup.setLayout(new GridLayout(2, false));
		cGroup.setText(text);
		return cGroup;
	}

	/**
	 * Constructor
	 *
	 * @param parent
	 *            - parent container
	 * @param style
	 *            - style bits
	 */
	public CGroup(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.Paint, this);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		for (Control control : getChildren())
			control.setVisible(visible);
	}

	/**
	 * Set the title text
	 *
	 * @param text
	 *            - title text
	 */
	public void setText(String text) {
		this.text = text;
		computeTopTrim(getFont());
		super.setToolTipText(text);
		redraw();
	}

	private void computeTopTrim(Font font) {
		if (text != null && !text.isEmpty()) {
			GC gc = new GC(this);
			gc.setFont(font);
			Point textExtent = gc.textExtent(text);
			th = textExtent.y;
			tw = textExtent.x;
			gc.dispose();
		} else {
			th = 0;
			tw = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		computeTopTrim(font);
		super.setFont(font);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events
	 * .PaintEvent)
	 */
	public void handleEvent(Event e) {
		if (isVisible()) {
			Rectangle clientArea = super.getClientArea();
			GC gc = e.gc;
			Color fg = getForeground();
			if (fg != null) {
				gc.setForeground(fg);
				int yoff = (th != 0) ? th / 2 : inset;
				gc.drawRoundRectangle(clientArea.x + inset,
						clientArea.y + yoff, clientArea.width - 2 * inset,
						clientArea.height - 2 * inset - yoff, arcWidth,
						archHeight);
				if (th != 0) {
					int xoff = (tw + 2 * arcWidth + indent > clientArea.width) ? 0
							: indent;
					int space = (clientArea.width - 2 * arcWidth - xoff) * 2 / 3;
					String s = text;
					if (tw > space)
						s = text.substring(0, Math.max(3, text.length() * space / tw)) + '…';
					gc.setFont(getFont());
					gc.drawText(s, clientArea.x + arcWidth + xoff, clientArea.y);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Scrollable#computeTrim(int, int, int, int)
	 */
	@Override
	public Rectangle computeTrim(int x, int y, int width, int height) {
		int ymargin = (th != 0) ? th : margins;
		Rectangle r = super.computeTrim(x, y, width, height);
		r.x -= margins;
		r.y -= ymargin;
		r.width += 2 * margins;
		r.height += margins + ymargin;
		return r;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Scrollable#getClientArea()
	 */
	@Override
	public Rectangle getClientArea() {
		int ymargin = (th != 0) ? th : margins;
		Rectangle clientArea = super.getClientArea();
		clientArea.x += margins;
		clientArea.y += ymargin;
		clientArea.width -= 2 * margins;
		clientArea.height -= (margins + ymargin);
		return clientArea;
	}

	public String getText() {
		return text;
	}

}
