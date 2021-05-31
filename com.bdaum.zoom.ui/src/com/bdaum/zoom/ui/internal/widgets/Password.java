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
 * (c) 2021 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.ui.internal.Icons;

public class Password extends Composite implements Listener {

	private StackLayout stackLayout;
	private Text htext;
	private Text vtext;
	private ListenerList<Listener> listenerList = new ListenerList<>();
	private Label modeButton;

	public Password(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = 0;
		setLayout(layout);
		Composite stack = new Composite(this, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		htext = new Text(stack, SWT.SINGLE | SWT.LEAD | SWT.PASSWORD | style);
		vtext = new Text(stack, SWT.SINGLE | SWT.LEAD | style);
		stackLayout.topControl = htext;
		modeButton = new Label(this, SWT.NONE);
		modeButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		modeButton.setImage(Icons.eye.getImage());
		modeButton.addListener(SWT.MouseUp, this);
		htext.addListener(SWT.Modify, this);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Modify:
			fireEvent(e);
			break;
		case SWT.MouseUp:
			flip();
		}
	}

	private void flip() {
		if (stackLayout.topControl == htext) {
			htext.removeListener(SWT.Modify, this);
			vtext.setText(htext.getText());
			vtext.addListener(SWT.Modify, this);
			stackLayout.topControl = vtext;
			modeButton.setImage(Icons.noeye.getImage());
		} else {
			vtext.removeListener(SWT.Modify, this);
			htext.setText(vtext.getText());
			htext.addListener(SWT.Modify, this);
			stackLayout.topControl = htext;
			modeButton.setImage(Icons.eye.getImage());
		}
		this.layout(true, true);
	}

	/**
	 * Shows or hides the content
	 * @param show true to reveal content
	 */
	public void showContent(boolean show) {
		if (show != (stackLayout.topControl == vtext))
			flip();
	}

	/* (nicht-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify) {
			checkWidget();
			if (listener == null)
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			listenerList.add(listener);
			return;
		}
		super.addListener(type, listener);
	}

	/* (nicht-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#removeListener(int, org.eclipse.swt.widgets.Listener)
	 */
	public void removeListener(int type, Listener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		listenerList.remove(listener);
		super.removeListener(type, listener);
	}

	private void fireEvent(Event e) {
		e.widget = this;
		for (Listener listener : listenerList)
			listener.handleEvent(e);
	}

	/**
	 * Sets the text of this control
	 * @param text
	 */
	public void setText(String text) {
		(stackLayout.topControl == htext ? htext : vtext).setText(text);
	}

	/**
	 * return the text of the control
	 * @return
	 */
	public String getText() {
		return (stackLayout.topControl == htext ? htext : vtext).getText();
	}
	
	/* (nicht-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String string) {
		htext.setToolTipText(string);
		vtext.setToolTipText(string);
		super.setToolTipText(string);
	}
	
	/* (nicht-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	@Override
	public boolean setFocus() {
		return (stackLayout.topControl == htext ? htext : vtext).setFocus();
	}
	
}
