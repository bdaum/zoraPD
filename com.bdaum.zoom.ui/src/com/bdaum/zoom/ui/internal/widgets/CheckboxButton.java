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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;

public class CheckboxButton extends Composite implements Listener {

	private ListenerList<Listener> listeners = new ListenerList<>();

	private Button button;

	private Label buttonlabel;

	public CheckboxButton(Composite parent, String text, int style) {
		super(parent, style & SWT.BORDER);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		button = new Button(this, SWT.CHECK);
		button.setText(""); //$NON-NLS-1$
		buttonlabel = new Label(this, SWT.NONE);
		buttonlabel.setText(text);
		button.pack();
		GridData data = new GridData();
		data.widthHint = button.getBounds().height + 4;
		button.setLayoutData(data);
		buttonlabel.addListener(SWT.MouseDown, this);
		button.addListener(SWT.Selection, this);
	}

	public void setToolTipText(String tooltip) {
		button.setToolTipText(tooltip);
		buttonlabel.setToolTipText(tooltip);
	}

	public void setText(String text) {
		buttonlabel.setText(text);
	}

	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
		buttonlabel.setData(CSSProperties.ID, enabled ? null : CSSProperties.DISABLED);
		CssActivator.getDefault().setColors(buttonlabel);
	}

	@Override
	public void handleEvent(Event event) {
		event.widget = this;
		event.type = SWT.Selection;
		if (event.type == SWT.MouseDown)
			setSelection(!getSelection());
		fireEvent(event);
	}

	private void fireEvent(Event event) {
		event.widget = this;
		event.type = SWT.Selection;
		for (Listener listener : listeners)
			listener.handleEvent(event);
	}

	public void setSelection(boolean selected) {
		button.setSelection(selected);
	}

	public boolean getSelection() {
		return button.getSelection();
	}

	public boolean isEnabled() {
		return button.getEnabled() && super.isEnabled();
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Selection)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

}
