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
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;

public class RadioButtonGroup extends Composite implements Listener {

	private Button[] buttons;
	private Label[] buttonlabels;

	private ListenerList<Listener> listeners = new ListenerList<>();

	private boolean horizontal;

	private int indent = 0;
	private int columns;

	/**
	 * Creates a horizontal, vertical, or multi-column radio group
	 * 
	 * @param parent
	 *            - parent composite
	 * @param title
	 *            - title or null
	 * @param style
	 *            - SWT.BORDER; SWT.HORIZONTAL or SWT.VERTICAL or number of columns
	 *            (<=15)
	 * @param labels
	 *            - Labels for radio buttons
	 */
	public RadioButtonGroup(Composite parent, String title, int style, String... labels) {
		super(parent, style & SWT.BORDER);
		horizontal = (style & SWT.HORIZONTAL) != 0;
		columns = style & 0xf;
		int col = columns == 0 ? horizontal ? labels.length * 2 + (title != null ? 2 : 0) : 2 : 2 * columns;
		GridLayout layout = new GridLayout(col, false);
		if (title != null) {
			Label titleLabel = new Label(this, SWT.NONE);
			titleLabel.setLayoutData(
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, columns == 0 ? 2 : columns * 2, 1));
			titleLabel.setText(title);
			indent = 10;
		}
		layout.horizontalSpacing = layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		buttons = new Button[labels.length];
		buttonlabels = new Label[labels.length];
		for (int i = 0; i < labels.length; i++)
			setButton(i, labels[i]);
	}

	public void addButton(String label) {
		int length = buttons.length;
		Button[] newButtons = new Button[length + 1];
		System.arraycopy(buttons, 0, newButtons, 0, length);
		buttons = newButtons;
		Label[] newLabels = new Label[length + 1];
		System.arraycopy(buttonlabels, 0, newLabels, 0, length);
		buttonlabels = newLabels;
		setButton(length, label);
		if (horizontal)
			((GridLayout) getLayout()).numColumns += 2;
	}

	private void setButton(int i, String label) {
		buttons[i] = new Button(this, SWT.RADIO);
		buttons[i].setText(""); //$NON-NLS-1$
		buttonlabels[i] = new Label(this, SWT.NONE);
		buttonlabels[i].setText(label);
		buttons[i].pack();
		Rectangle bounds = buttons[i].getBounds();
		GridData data = new GridData();
		data.widthHint = bounds.height + 4;
		data.horizontalIndent = columns == 0 || i % columns == 0 ? indent : 5;
		if (horizontal)
			indent = 5;
		buttons[i].setLayoutData(data);
		buttonlabels[i].addListener(SWT.MouseDown, this);
		buttons[i].addListener(SWT.Selection, this);
	}

	public void setToolTipText(int i, String tooltip) {
		if (i >= 0 && i < buttons.length) {
			buttons[i].setToolTipText(tooltip);
			buttonlabels[i].setToolTipText(tooltip);
		}
	}

	public Object getData(int i, String key) {
		if (i >= 0 && i < buttons.length)
			return buttons[i].getData(key);
		return null;
	}

	public void setData(int i, String key, Object data) {
		if (i >= 0 && i < buttons.length)
			buttons[i].setData(key, data);
	}

	public void setText(int i, String text) {
		if (i >= 0 && i < buttons.length)
			buttonlabels[i].setText(text);
	}

	public void setEnabled(int i, boolean enabled) {
		if (i >= 0 && i < buttons.length) {
			buttons[i].setEnabled(enabled);
			buttonlabels[i].setData(CSSProperties.ID, enabled ? null : CSSProperties.DISABLED);
			CssActivator.getDefault().setColors(buttonlabels[i]);
		}
	}

	public void setEnabled(boolean enabled) {
		for (int i = 0; i < buttons.length; i++)
			setEnabled(i, enabled);
	}

	public void setVisible(int i, boolean visible) {
		if (i >= 0 && i < buttons.length) {
			buttons[i].setVisible(visible);
			buttonlabels[i].setVisible(visible);
		}
	}

	@Override
	public void handleEvent(Event event) {
		Widget widget = event.widget;
		event.widget = this;
		if (event.type == SWT.MouseDown)
			for (int i = 0; i < buttonlabels.length; i++) {
				if (widget == buttonlabels[i]) {
					setSelection(i);
					event.detail = i;
					event.doit = true;
					fireEvent(event);
					break;
				}
			}
		else {
			event.detail = -1;
			for (int i = 0; i < buttons.length; i++)
				if (buttons[i] == widget) {
					event.detail = i;
					break;
				}
			fireEvent(event);
		}
	}

	private void fireEvent(Event event) {
		for (Listener listener : listeners)
			listener.handleEvent(event);
	}

	public void setSelection(int index) {
		for (int i = 0; i < buttons.length; i++)
			buttons[i].setSelection(index == i);
	}

	public int getSelection() {
		for (int i = 0; i < buttons.length; i++)
			if (buttons[i].getSelection())
				return i;
		return -1;
	}

	public boolean isEnabled(int i) {
		return (i >= 0 && i < buttons.length) && buttons[i].getEnabled();
	}

	public boolean isEnabled() {
		if (super.isEnabled())
			for (int i = 0; i < buttons.length; i++)
				if (buttons[i].getEnabled())
					return true;
		return false;
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public int size() {
		return buttons.length;
	}

}
