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

package com.bdaum.zoom.ui.internal.views;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class IncrementalNumberCellEditor extends TextCellEditor implements ICellEditorValidator {

	private final int increment;
	private Button minusButton;
	private Button plusButton;
	private NumberFormat nf;
	private int maximum = Integer.MAX_VALUE;

	public IncrementalNumberCellEditor(Composite parent, int digits, int increment) {
		super(parent);
		this.increment = increment;
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(digits);
		nf.setMinimumFractionDigits(digits);
		setValidator(this);
	}
	
	public String isValid(Object value) {
		if (value != null) {
			try {
				nf.parse(value.toString());
			} catch (ParseException e1) {
				return Messages.getString("IncrementalNumberCellEditor.bad_number"); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	protected void doSetValue(Object value) {
		if (value instanceof String)
			try {
				if (Integer.parseInt((String) value) > maximum)
					value = nf.format(maximum);
			} catch (NumberFormatException e) {
				// do nothing
			}
		super.doSetValue(value);
	}

	@Override
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		final Text txt = (Text) super.createControl(composite);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Verify:
					if (e.keyCode != 0 && e.character != '\b' && e.character != SWT.DEL
							&& !Character.isDigit(e.character)) {
						e.doit = false;
						return;
					}
					String t = txt.getText();
					String futureText = t.substring(0, e.start) + e.text + t.substring(e.end);
					if (!futureText.isEmpty() && Integer.parseInt(futureText) > maximum)
						e.doit = false;
					return;
				default:
					incrementValue(txt, e.widget == minusButton ? -increment : increment);
				}
			}
		};
		txt.addListener(SWT.Verify, listener);
		minusButton = new Button(composite, SWT.ARROW | SWT.DOWN);
		minusButton.addListener(SWT.Selection, listener);
		plusButton = new Button(composite, SWT.ARROW | SWT.UP);
		plusButton.addListener(SWT.Selection, listener);
		return composite;
	}

	@Override
	protected void focusLost() {
		final Control control = getControl();
		final Display display = control.getDisplay();
		display.timerExec(10, () -> {
			if (!control.isDisposed()) {
				Control focusControl = display.getFocusControl();
				if (focusControl == text || focusControl == minusButton || focusControl == plusButton)
					return;
				IncrementalNumberCellEditor.super.focusLost();
			}
		});
	}

	private void incrementValue(final Text txt, int incr) {
		try {
			double v = nf.parse(txt.getText()).doubleValue();
			v += incr;
			if (v < 0)
				v = 0;
			if (v > maximum)
				v = maximum;
			txt.setText(nf.format(v));
		} catch (ParseException e1) {
			// ignore
		}
	}

	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		switch (keyEvent.character) {
		case '+':
			incrementValue(text, increment);
			break;
		case '-':
			incrementValue(text, -increment);
			break;
		default:
			super.keyReleaseOccured(keyEvent);
			break;
		}
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

}
