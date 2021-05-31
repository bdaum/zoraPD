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

package com.bdaum.zoom.ui.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
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
public class RangeControl extends Composite implements Listener {

	private static final String TOOLTIP = Messages.RangeControl_click_to_set;
	private Spinner lowSpinner;
	private Canvas canvas;
	private Point selection;
	private ListenerList<Listener> selectionListeners = new ListenerList<>();
	private Spinner highSpinner;
	private boolean lowDown = false;
	private boolean highDown = false;
	private boolean logarithmic;

	public RangeControl(Composite parent, int style) {
		super(parent, style & SWT.BORDER);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = layout.verticalSpacing = layout.horizontalSpacing = 0;
		setLayout(layout);
		lowSpinner = new Spinner(this, style | SWT.BORDER);
		lowSpinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		lowSpinner.addListener(SWT.Selection, this);
		lowSpinner.addListener(SWT.DefaultSelection, this);
		lowSpinner.addListener(SWT.Modify, this);
		lowSpinner.pack();
		Point size = lowSpinner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		canvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.heightHint = size.y;
		canvas.setLayoutData(layoutData);
		canvas.addListener(SWT.Paint, this);
		canvas.addListener(SWT.MouseDoubleClick, this);
		canvas.addListener(SWT.MouseDown, this);
		canvas.addListener(SWT.MouseUp, this);
		canvas.addListener(SWT.MouseMove, this);
		canvas.setToolTipText(TOOLTIP);
		highSpinner = new Spinner(this, style | SWT.BORDER);
		highSpinner.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		highSpinner.addListener(SWT.Selection, this);
		highSpinner.addListener(SWT.DefaultSelection, this);
		highSpinner.addListener(SWT.Modify, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.
	 * PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		Rectangle clientArea = canvas.getClientArea();
		double minimum = getMinimum();
		double maximum = getMaximum();
		double v1 = lowSpinner.getSelection();
		double v2 = highSpinner.getSelection();
		if (logarithmic) {
			maximum = toLog(maximum);
			minimum = toLog(minimum);
			v1 = toLog(v1);
			v2 = toLog(v2);
		}
		double range = maximum - minimum;
		int x1 = (int) (range <= 0 ? 0 : clientArea.width * (v1 - minimum) / range + 0.5d);
		int x2 = (int) (range <= 0 ? 0 : clientArea.width * (v2 - minimum) / range + 0.5d);
		GC gc = e.gc;
		if (x2 < clientArea.width) {
			gc.setBackground(getBackground());
			gc.fillRectangle(clientArea.x + x2, clientArea.y, clientArea.width - x2, clientArea.height);
		}
		if (x1 > 0) {
			gc.setBackground(getBackground());
			gc.fillRectangle(clientArea.x, clientArea.y, x1, clientArea.height);
		}
		if (x2 > x1) {
			gc.setBackground(getForeground());
			gc.fillRectangle(clientArea.x + x1, clientArea.y, x2 - x1, clientArea.height);
		}
	}

	private static double toLog(double v) {
		return v < 0 ? -Math.log10(-v - 1) : Math.log10(v + 1);
	}

	private static double toLin(double v) {
		return v < 0 ? -Math.pow(10, v) + 1 : Math.pow(10, v) - 1;
	}

	/**
	 * Adds a selection listener
	 * 
	 * @param listener
	 *            - selection listener
	 * @see org.eclipse.swt.widgets.Spinner#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void addListener(int type, Listener listener) {
		if (type == SWT.Selection)
			selectionListeners.add(listener);
	}

	/**
	 * Removes a selection listener
	 * 
	 * @param listener
	 *            - selection listener
	 * @see org.eclipse.swt.widgets.Spinner#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void removeListener(int type, Listener listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * Returns the number of fractional digits
	 * 
	 * @return - fractional digits
	 * @see org.eclipse.swt.widgets.Spinner#getDigits()
	 */
	public int getDigits() {
		return lowSpinner.getDigits();
	}

	/**
	 * Returns the spinner increment
	 * 
	 * @return - spinner increment
	 * @see org.eclipse.swt.widgets.Spinner#getIncrement()
	 */
	public int getIncrement() {
		return lowSpinner.getIncrement();
	}

	/**
	 * Returns the maximum value
	 * 
	 * @return - maximum value
	 * @see org.eclipse.swt.widgets.Spinner#getMaximum()
	 */
	public int getMaximum() {
		return lowSpinner.getMaximum();
	}

	/**
	 * Returns the minimum value
	 * 
	 * @return - minimum value
	 * @see org.eclipse.swt.widgets.Spinner#getMinimum()
	 */
	public int getMinimum() {
		return lowSpinner.getMinimum();
	}

	/**
	 * Returns the spinners page increment
	 * 
	 * @return - page increment
	 * @see org.eclipse.swt.widgets.Spinner#getPageIncrement()
	 */
	public int getPageIncrement() {
		return lowSpinner.getPageIncrement();
	}

	/**
	 * Returns the current value
	 * 
	 * @return - current value
	 * @see org.eclipse.swt.widgets.Spinner#getSelection()
	 */
	public Point getSelection() {
		return new Point(lowSpinner.getSelection(), highSpinner.getSelection());
	}

	/**
	 * Returns the spinner's text
	 * 
	 * @return - text of spinner control
	 * @see org.eclipse.swt.widgets.Spinner#getText()
	 */
	public String[] getText() {
		return new String[] { lowSpinner.getText(), highSpinner.getText() };
	}

	/**
	 * Returns the spinner's text limit
	 * 
	 * @return - text limit
	 * @see org.eclipse.swt.widgets.Spinner#getTextLimit()
	 */
	public Point getTextLimit() {
		return new Point(lowSpinner.getTextLimit(), highSpinner.getTextLimit());
	}

	/**
	 * Sets the number of fractional digits
	 * 
	 * @param value
	 *            - fractional digits
	 * @see org.eclipse.swt.widgets.Spinner#setDigits(int)
	 */
	public void setDigits(int value) {
		lowSpinner.setDigits(value);
		highSpinner.setDigits(value);
	}

	/**
	 * Sets the spinners increment
	 * 
	 * @param value
	 *            - increment
	 * @see org.eclipse.swt.widgets.Spinner#setIncrement(int)
	 */
	public void setIncrement(int value) {
		lowSpinner.setIncrement(value);
		highSpinner.setIncrement(value);
	}

	/**
	 * Sets the maximum value
	 * 
	 * @param value
	 *            - maximum value
	 * @see org.eclipse.swt.widgets.Spinner#setMaximum(int)
	 */
	public void setMaximum(int value) {
		lowSpinner.setMaximum(value);
		highSpinner.setMaximum(value);
		lowSpinner.getParent().pack();
		highSpinner.getParent().pack();
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
		lowSpinner.setMinimum(value);
		highSpinner.setMinimum(value);
		lowSpinner.getParent().pack();
		highSpinner.getParent().pack();
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
		lowSpinner.setPageIncrement(value);
		highSpinner.setPageIncrement(value);
	}

	/**
	 * Sets the default value
	 * 
	 * @param value
	 *            - default value
	 * @see org.eclipse.swt.widgets.Spinner#setSelection(int)
	 */
	public void setSelection(Point value) {
		this.selection = value;
		lowSpinner.setSelection(value.x);
		highSpinner.setSelection(value.y);
		lowSpinner.getParent().getParent().pack();
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
		lowSpinner.setToolTipText(string);
		highSpinner.setToolTipText(string);
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
		return lowSpinner.getEnabled();
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
		lowSpinner.setEnabled(enabled);
		highSpinner.setEnabled(enabled);
		canvas.setVisible(enabled);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MouseDown:
		case SWT.Paint:
			Rectangle clientArea = canvas.getClientArea();
			double minimum = getMinimum();
			double maximum = getMaximum();
			double v1 = lowSpinner.getSelection();
			double v2 = highSpinner.getSelection();
			if (logarithmic) {
				maximum = toLog(maximum);
				minimum = toLog(minimum);
				v1 = toLog(v1);
				v2 = toLog(v2);
			}
			double range = maximum - minimum;
			int x1 = (int) (range <= 0 ? 0 : clientArea.width * (v1 - minimum) / range + 0.5d);
			int x2 = (int) (range <= 0 ? 0 : clientArea.width * (v2 - minimum) / range + 0.5d);
			if (event.type == SWT.MouseDown) {
				int x = event.x - clientArea.x;
				int dist1 = Math.abs(x1 - x);
				int dist2 = Math.abs(x2 - x);
				if (dist1 < dist2)
					lowDown = true;
				else
					highDown = true;
				processMouseEvent(event);
			} else {
				GC gc = event.gc;
				if (x2 < clientArea.width) {
					gc.setBackground(getBackground());
					gc.fillRectangle(clientArea.x + x2, clientArea.y, clientArea.width - x2, clientArea.height);
				}
				if (x1 > 0) {
					gc.setBackground(getBackground());
					gc.fillRectangle(clientArea.x, clientArea.y, x1, clientArea.height);
				}
				if (x2 > x1) {
					gc.setBackground(getForeground());
					gc.fillRectangle(clientArea.x + x1, clientArea.y, x2 - x1, clientArea.height);
				}
			}
			return;
		case SWT.MouseUp:
			lowDown = false;
			highDown = false;
			return;
		case SWT.MouseDoubleClick:
			lowDown = false;
			highDown = false;
			lowSpinner.setSelection(selection.x);
			highSpinner.setSelection(selection.y);
			canvas.redraw();
			return;
		case SWT.MouseMove:
			if (lowDown || highDown)
				processMouseEvent(event);
			return;
		case SWT.Selection:
		case SWT.DefaultSelection:
		case SWT.Modify:
			fireEvent(event);
			canvas.redraw();
		}
	}

	private void fireEvent(Event e) {
		for (Listener l : selectionListeners)
			l.handleEvent(e);
	}

	private void processMouseEvent(Event e) {
		if (lowSpinner.isEnabled()) {
			Rectangle clientArea = canvas.getClientArea();
			double minimum = getMinimum();
			double maximum = getMaximum();
			if (logarithmic) {
				maximum = toLog(maximum);
				minimum = toLog(minimum);
			}
			double range = maximum - minimum;
			int x = e.x - clientArea.x;
			double value = range * x / clientArea.width + minimum;
			if (logarithmic)
				value = toLin(value);
			if (lowDown) {
				int v = (int) (Math.min(value, highSpinner.getSelection() - 1) - 0.5d);
				if (value != lowSpinner.getSelection()) {
					lowSpinner.setSelection(v);
					fireSelectionEvent(e);
				}
			} else if (highDown) {
				int v = (int) (Math.max(value, lowSpinner.getSelection() + 1) + 0.5d);
				if (v != highSpinner.getSelection()) {
					highSpinner.setSelection(v);
					fireSelectionEvent(e);
				}
			}
		}
	}

	protected void fireSelectionEvent(Event ev) {
		canvas.redraw();
		fireEvent(ev);
	}

	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
		canvas.redraw();
	}

}
