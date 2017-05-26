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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.Icons;

public class ColorCodeGroup extends Composite implements PaintListener {

	private static int n = QueryField.COLORCODE.getEnumLabels().length;
	private static int width = 16;
	private static int height = 16;
	private static int cwidth = n * width+2;
	private static int cheight = height+13;

	private Canvas canvas;
	private int code;

	private ListenerList<SelectionListener> listeners = new ListenerList<SelectionListener>();

	public ColorCodeGroup(Composite parent, int style, int code) {
		super(parent, style);
		this.code = code;
		canvas = new Canvas(this, SWT.DOUBLE_BUFFERED | SWT.BORDER);
		canvas.setBounds(0, 0, cwidth, cheight);
		setBounds(0, 0, cwidth, cheight);
		canvas.addPaintListener(this);
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				ColorCodeGroup.this.code = e.x / width - 1;
				canvas.redraw();
				fireSelection(e);
			}
		});
		canvas.redraw();
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		canvas.addFocusListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		canvas.removeFocusListener(listener);
	}

	@Override
	public boolean setFocus() {
		return canvas.setFocus();
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireSelection(MouseEvent e) {
		Event e2 = new Event();
		e2.display = e.display;
		e2.item = this;
		e2.widget = this;
		e2.x = e.x;
		e2.y = e.y;
		e2.stateMask = e.stateMask;
		SelectionEvent ev = new SelectionEvent(e2);
		for (Object listener : listeners.getListeners())
			((SelectionListener) listener).widgetSelected(ev);
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, width, height);
		gc.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
		for (int i = 0; i < width; i += 4)
			for (int j = 0; j < height; j += 4)
				if ((i + j) % 8 == 0)
					gc.fillRectangle(i, j, 4, 4);
		for (int i = 0; i < n; i++)
			if (i > 0) {
				Image image = Icons.toSwtColors(i - 1);
				Rectangle ibounds = image.getBounds();
				gc.drawImage(image, ibounds.x, ibounds.y, ibounds.width,
						ibounds.height, i * width, 0, width, height);
			}
		gc.drawImage(Icons.tri.getImage(), width * (code + 1), height);
	}

	/**
	 * @return code
	 */
	public int getCode() {
		return code;
	}

}
