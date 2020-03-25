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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.ui.internal.UiConstants;

public class WebColorGroup implements Listener {
	private Button button;
	private Rgb_type rgb;
	private ListenerList<Listener> listeners = new ListenerList<>();
	private String text;
	private Image image;

	public WebColorGroup(final Composite parent, final String text) {
		this.text = text;
		Label label = null;
		if (text != null) {
			label = new Label(parent, SWT.NONE);
			label.setText(text);
		}
		button = new Button(parent, SWT.PUSH | SWT.BORDER);
		button.setData(UiConstants.LABEL, label);
		final GridData gd_bgButton = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd_bgButton.widthHint = 20;
		gd_bgButton.heightHint = 20;
		button.setLayoutData(gd_bgButton);
		button.setImage(image = new Image(parent.getShell().getDisplay(), 20, 20));
		button.addListener(SWT.Selection, this);
		button.addListener(SWT.Dispose, this);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			ColorDialog dialog = new ColorDialog(((Control) e.widget).getShell());
			dialog.setText(text);
			if (rgb != null)
				dialog.setRGB(new RGB(rgb.getR(), rgb.getG(), rgb.getB()));
			RGB rgb1 = dialog.open();
			if (rgb1 != null)
				setRGB(new Rgb_typeImpl(rgb1.red, rgb1.green, rgb1.blue));
			break;
		case SWT.Dispose:
			image.dispose();
			break;
		}
	}

	public Rgb_type setRGB(Rgb_type rgb) {
		this.rgb = rgb;
		Image image = button.getImage();
		GC gc = new GC(image);
		Color c = new Color(button.getShell().getDisplay(), rgb.getR(), rgb.getG(), rgb.getB());
		gc.setBackground(c);
		gc.fillRectangle(image.getBounds());
		c.dispose();
		gc.dispose();
		button.setImage(image);
		Event e = new Event();
		e.type = SWT.Modify;
		e.widget = button;
		e.data = this;
		for (Listener listener : listeners)
			listener.handleEvent(e);
		return rgb;
	}

	public Rgb_type getRGB() {
		return rgb;
	}

	public Rgb_type fillValues(IDialogSettings dialogSettings, String key, int r, int g, int b) {
		try {
			String colorSpec = dialogSettings.get(key);
			if (colorSpec != null) {
				java.awt.Color color = new java.awt.Color(Integer.parseInt(colorSpec));
				return setRGB(new Rgb_typeImpl(color.getRed(), color.getGreen(), color.getBlue()));
			}
		} catch (NumberFormatException e) {
			// do nothing
		}
		return setRGB(new Rgb_typeImpl(r, g, b));
	}

	public void saveSettings(IDialogSettings dialogSettings, String key) {
		if (rgb != null)
			dialogSettings.put(key, new java.awt.Color(rgb.getR(), rgb.getG(), rgb.getB()).getRGB());
	}

	public Control getControl() {
		return button;
	}

	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	public void setToolTipText(String text) {
		button.setToolTipText(text);
		Object data = button.getData(UiConstants.LABEL);
		if (data instanceof Label)
			((Label) data).setToolTipText(text);
	}

	/**
	 * Add listener. Tranmitted event maybe null
	 * 
	 * @param listener
	 */
	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

}
