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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

/**
 * Implements a link similar to org.eclipse.swt.widgets.Link
 * Better behavior in regards to styling (link color can be changed)
 *
 */
public class CLink extends Composite implements MouseListener,
		MouseTrackListener {

	private Label label;
	private ListenerList<SelectionListener> listeners = new ListenerList<SelectionListener>();
	private Color inactiveColor;

	/**
	 * Constructor
	 * @param parent - parent container
	 * @param style - style bits
	 */
	public CLink(Composite parent, int style) {
		super(parent, style & SWT.BORDER);
		setLayout(new GridLayout());
		label = new Label(this, style & (~SWT.BORDER));
		label.setData("id", "clink"); //$NON-NLS-1$ //$NON-NLS-2$
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		label.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		label.addMouseListener(this);
		label.addMouseTrackListener(this);
	}
	
	/**
	 * Adds a selection listener
	 * @param listener - selection listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		listeners.add(listener);
	}

	/**
	 * Removes a selection listener
	 * @param listener - selectiion listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		listeners.remove(listener);
	}

	/**
	 * Sets the link text
	 * @param text - link text
	 */
	public void setText(String text) {
		label.setText(text);
	}
	
	@Override
	public void setToolTipText(String text) {
		label.setToolTipText(text);
	}

	public String getText() {
		return label.getText();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
		// do nothing
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		// do nothing
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (e.button == 1) {
			Event ev = new Event();
			ev.time = e.time;
			ev.stateMask = e.stateMask;
			ev.widget = e.widget;
			ev.x = e.x;
			ev.y = e.y;
			ev.button = e.button;
			fireSelectionEvent(new SelectionEvent(ev));
		}
	}

	private void fireSelectionEvent(SelectionEvent event) {
		for (Object listener : listeners.getListeners()) 
			((SelectionListener) listener).widgetSelected(event);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
		inactiveColor = label.getForeground();
		label.setForeground(getForeground());
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
		label.setForeground(inactiveColor);
		inactiveColor = null;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseHover(MouseEvent e) {
		// do nothing
	}


}
