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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;

/**
 * Implements a link similar to org.eclipse.swt.widgets.Link Better behavior in
 * regards to styling (link color can be changed)
 *
 */
public class CLink extends Composite implements Listener {

	private Label label;
	private ListenerList<Listener> listeners = new ListenerList<>();
	private Color inactiveColor;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            - parent container
	 * @param style
	 *            - style bits
	 */
	public CLink(Composite parent, int style) {
		super(parent, style & SWT.BORDER);
		setLayout(new GridLayout());
		label = new Label(this, style & (~SWT.BORDER));
		label.setData(CSSProperties.ID, CSSProperties.CLINK);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		label.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		label.addListener(SWT.MouseUp, this);
		label.addListener(SWT.MouseEnter, this);
		label.addListener(SWT.MouseExit, this);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		label.setData(CSSProperties.ID, enabled ? CSSProperties.CLINK : CSSProperties.DISABLED);
		CssActivator.getDefault().setColors(label);
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Selection) {
			checkWidget();
			if (listener == null)
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			listeners.add(listener);
		}
	}

	public void removeListener(int type, Listener listener) {
		if (type == SWT.Selection) {
			checkWidget();
			if (listener == null)
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			listeners.remove(listener);
		}
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseEnter:
			inactiveColor = label.getForeground();
			label.setForeground(getForeground());
			break;
		case SWT.MouseExit:
			label.setForeground(inactiveColor);
			inactiveColor = null;
			break;
		case SWT.MouseUp:
			if (e.button == 1)
				fireEvent(e);
			break;
		}
	}

	private void fireEvent(Event event) {
		event.widget = this;
		event.type = SWT.Selection;
		for (Listener listener : listeners)
			listener.handleEvent(event);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void mouseHover(MouseEvent e) {
		// do nothing
	}

}
