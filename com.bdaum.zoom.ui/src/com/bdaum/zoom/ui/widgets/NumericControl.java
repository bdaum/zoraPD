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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

/**
 * A numeric input control similar to org.eclipse.swt.widgets.Spinner but with a
 * small synchronous slider below the spinner
 *
 */
public class NumericControl extends Composite implements Listener {

	public static final int LOGARITHMIC = 1 << 30;
	private static final String TOOLTIP = Messages.NumericControl_click_to_set;
	private Spinner spinner;
	private Canvas canvas;
	private int selection;
	private ListenerList<Listener> listeners = new ListenerList<>();
	private boolean mouseDown;
	private boolean logarithmic;

	public NumericControl(Composite parent, int style) {
		super(parent, style & SWT.BORDER);
		if ((style & LOGARITHMIC) != 0)
			logarithmic = true;
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = layout.verticalSpacing = 0;
		setLayout(layout);
		spinner = new Spinner(this, style & ~LOGARITHMIC | SWT.BORDER);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		spinner.addListener(SWT.Selection, this);
		spinner.addListener(SWT.DefaultSelection, this);
		spinner.addListener(SWT.Modify, this);
		Point size = spinner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		canvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.heightHint = 4;
		layoutData.widthHint = size.x;
		canvas.setLayoutData(layoutData);
		canvas.addListener(SWT.Paint, this);
		canvas.addListener(SWT.MouseDown, this);
		canvas.addListener(SWT.MouseUp, this);
		canvas.addListener(SWT.MouseDoubleClick, this);
		canvas.addListener(SWT.MouseMove, this);
		canvas.setToolTipText(TOOLTIP);
	}

	private static double toLog(double v) {
		return v < 0 ? -Math.log10(-v - 1) : Math.log10(v + 1);
	}

	private static double toLin(double v) {
		return v < 0 ? -Math.pow(10, v) + 1 : Math.pow(10, v) - 1;
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		spinner.addFocusListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		spinner.removeFocusListener(listener);
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Selection)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the number of fractional digits
	 * 
	 * @return - fractional digits
	 * @see org.eclipse.swt.widgets.Spinner#getDigits()
	 */
	public int getDigits() {
		return spinner.getDigits();
	}

	/**
	 * Returns the spinner increment
	 * 
	 * @return - spinner increment
	 * @see org.eclipse.swt.widgets.Spinner#getIncrement()
	 */
	public int getIncrement() {
		return spinner.getIncrement();
	}

	/**
	 * Returns the maximum value
	 * 
	 * @return - maximum value
	 * @see org.eclipse.swt.widgets.Spinner#getMaximum()
	 */
	public int getMaximum() {
		return spinner.getMaximum();
	}

	/**
	 * Returns the minimum value
	 * 
	 * @return - minimum value
	 * @see org.eclipse.swt.widgets.Spinner#getMinimum()
	 */
	public int getMinimum() {
		return spinner.getMinimum();
	}

	/**
	 * Returns the spinners page increment
	 * 
	 * @return - page increment
	 * @see org.eclipse.swt.widgets.Spinner#getPageIncrement()
	 */
	public int getPageIncrement() {
		return spinner.getPageIncrement();
	}

	/**
	 * Returns the current value
	 * 
	 * @return - current value
	 * @see org.eclipse.swt.widgets.Spinner#getSelection()
	 */
	public int getSelection() {
		return spinner.getSelection();
	}

	/**
	 * Returns the spinner's text
	 * 
	 * @return - text of spinner control
	 * @see org.eclipse.swt.widgets.Spinner#getText()
	 */
	public String getText() {
		return spinner.getText();
	}

	/**
	 * Returns the spinner's text limit
	 * 
	 * @return - text limit
	 * @see org.eclipse.swt.widgets.Spinner#getTextLimit()
	 */
	public int getTextLimit() {
		return spinner.getTextLimit();
	}

	/**
	 * Sets the number of fractional digits
	 * 
	 * @param value
	 *            - fractional digits
	 * @see org.eclipse.swt.widgets.Spinner#setDigits(int)
	 */
	public void setDigits(int value) {
		spinner.setDigits(value);
	}

	/**
	 * Sets the spinners increment
	 * 
	 * @param value
	 *            - increment
	 * @see org.eclipse.swt.widgets.Spinner#setIncrement(int)
	 */
	public void setIncrement(int value) {
		spinner.setIncrement(value);
	}

	/**
	 * Sets the maximum value
	 * 
	 * @param value
	 *            - maximum value
	 * @see org.eclipse.swt.widgets.Spinner#setMaximum(int)
	 */
	public void setMaximum(int value) {
		spinner.setMaximum(value);
		spinner.getParent().pack();
		canvas.redraw();
	}

	/**
	 * Sets the minimum value
	 * 
	 * @param value
	 *            - minimum value
	 * @see org.eclipse.swt.widgets.Spinner#setMinimum(int)
	 */
	public void setMinimum(int value) {
		spinner.setMinimum(value);
		spinner.getParent().pack();
		canvas.redraw();
	}

	/**
	 * Sets the page increment
	 * 
	 * @param value
	 *            - page increment
	 * @see org.eclipse.swt.widgets.Spinner#setPageIncrement(int)
	 */
	public void setPageIncrement(int value) {
		spinner.setPageIncrement(value);
	}

	/**
	 * Sets the default value
	 * 
	 * @param value
	 *            - default value
	 * @see org.eclipse.swt.widgets.Spinner#setSelection(int)
	 */
	public void setSelection(int value) {
		this.selection = value;
		spinner.setSelection(value);
		spinner.getParent().pack();
		canvas.redraw();
	}

	/**
	 * Sets the tooltip text
	 * 
	 * @param string
	 *            - tooltip text
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String string) {
		spinner.setToolTipText(string);
		canvas.setToolTipText(string);
	}

	/**
	 * Returns the enabled state
	 * 
	 * @return - enabled state
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
	@Override
	public boolean getEnabled() {
		return spinner.getEnabled();
	}

	/**
	 * Sets the enabled state
	 * 
	 * @param enabled
	 *            - enabled state
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		spinner.setEnabled(enabled);
		canvas.setVisible(enabled);
	}

	private void fireEvent(Event ev) {
		ev.widget = this;
		ev.type = SWT.Selection;
		for (Listener l : listeners)
			l.handleEvent(ev);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MouseDown:
			mouseDown = true;
			processMouseEvent(event);
			break;
		case SWT.MouseUp:
			mouseDown = false;
			break;
		case SWT.MouseDoubleClick:
			mouseDown = false;
			spinner.setSelection(selection);
			canvas.redraw();
			break;
		case SWT.MouseMove:
			if (mouseDown)
				processMouseEvent(event);
			break;
		case SWT.Selection:
		case SWT.DefaultSelection:
		case SWT.Modify:
			fireEvent(event);
			canvas.redraw();
			break;
		case SWT.Paint:
			Rectangle clientArea = canvas.getClientArea();
			double minimum = getMinimum();
			double maximum = getMaximum();
			double v1 = getSelection();
			if (logarithmic) {
				maximum = toLog(maximum);
				minimum = toLog(minimum);
				v1 = toLog(v1);
			}
			double range = maximum - minimum;
			int x = (int) (range <= 0 ? 0 : clientArea.width * (v1 - minimum) / range + 0.5d);
			GC gc = event.gc;
			if (x < clientArea.width) {
				gc.setBackground(getBackground());
				gc.fillRectangle(clientArea.x + x, clientArea.y, clientArea.width - x, clientArea.height);
			}
			if (x > 0) {
				gc.setBackground(getForeground());
				gc.fillRectangle(clientArea.x, clientArea.y, x, clientArea.height);
			}
			break;
		}
	}

	private void processMouseEvent(Event ev) {
		if (spinner.isEnabled()) {
			Rectangle clientArea = canvas.getClientArea();
			double minimum = getMinimum();
			double maximum = getMaximum();
			if (logarithmic) {
				maximum = toLog(maximum);
				minimum = toLog(minimum);
			}
			double range = maximum - minimum;
			int x = ev.x - clientArea.x;
			double value = range * x / clientArea.width + minimum;
			if (logarithmic)
				value = toLin(value);
			int v = (int) (value + 0.5d);
			if (v != spinner.getSelection()) {
				spinner.setSelection(v);
				canvas.redraw();
				ev.type = SWT.Selection;
				fireEvent(ev);
			}
		}
	}

	public void setLogrithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
		canvas.redraw();
	}

}
