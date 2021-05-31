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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Font_typeImpl;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.dialogs.WebFontDialog;

public class WebFontGroup implements Listener {

	private static final String FAMILY2 = "_family"; //$NON-NLS-1$
	private static final String COLOR2 = "_color"; //$NON-NLS-1$
	private static final String VARIANT2 = "_variant"; //$NON-NLS-1$
	private static final String WEIGHT2 = "_weight"; //$NON-NLS-1$
	private static final String STYLE2 = "_style"; //$NON-NLS-1$
	private static final String SIZE2 = "_size"; //$NON-NLS-1$

	private Button button;
	private Label label;
	private Button colorButton;
	private Font_type fonttype;
	private Image image;
	private String text;

	public WebFontGroup(final Composite parent, final String text) {
		this.text = text;
		label = new Label(parent, SWT.NONE);
		label.setText(text);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		button = new Button(composite, SWT.PUSH | SWT.BORDER);
		final GridData gd_bgButton = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd_bgButton.widthHint = 270;
		gd_bgButton.heightHint = 28;
		button.setLayoutData(gd_bgButton);
		button.setText(Messages.WebFontGroup_select_font);
		button.addListener(SWT.Selection, this);
		colorButton = new Button(composite, SWT.PUSH | SWT.BORDER);
		final GridData gd_colorLabeln = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd_colorLabeln.widthHint = 14;
		gd_colorLabeln.heightHint = 28;
		colorButton.setImage(image = new Image(parent.getShell().getDisplay(), 14, 28));
		colorButton.setLayoutData(gd_colorLabeln);
		colorButton.addListener(SWT.Dispose, this);
		colorButton.addListener(SWT.Selection, this);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == colorButton) {
				if (fonttype != null) {
					ColorDialog dialog = new ColorDialog(((Control)e.widget).getShell());
					dialog.setText(text);
					Rgb_type color = fonttype.getColor();
					if (color != null)
						dialog.setRGB(new RGB(color.getR(), color.getG(), color.getB()));
					RGB rgb = dialog.open();
					if (rgb != null) {
						paintButton(colorButton, new Rgb_typeImpl(rgb.red, rgb.green, rgb.blue));
						fonttype.setColor(new Rgb_typeImpl(rgb.red, rgb.green, rgb.blue));
					}
				}
			} else {
				WebFontDialog dialog = new WebFontDialog(((Control)e.widget).getShell(), text, getFont());
				if (dialog.open() == Window.OK)
					setFont(dialog.getResult());
			}
			return;
		case SWT.Dispose:
			image.dispose();
		}
	}

	protected void paintButton(Button butt, Rgb_type rgb) {
		Image image = butt.getImage();
		Rectangle bounds = image.getBounds();
		GC gc = new GC(image);
		Color c = new Color(butt.getShell().getDisplay(), rgb.getR(), rgb.getG(), rgb.getB());
		gc.setBackground(c);
		gc.fillRectangle(bounds);
		c.dispose();
		gc.dispose();
		butt.setImage(image);
	}

	public void setFont(Font_type fonttype) {
		if (fonttype != null) {
			this.fonttype = fonttype;
			String[] family = fonttype.getFamily();
			StringBuilder sb = new StringBuilder(Core.toStringList(family, ",")); //$NON-NLS-1$
			int size = fonttype.getSize();
			int style = fonttype.getStyle();
			int weight = fonttype.getStyle();
			int variant = fonttype.getStyle();
			sb.append(';').append(size).append('%');
			if (style > 0)
				sb.append(';').append(WebFontDialog.STYLES[style]);
			if (weight > 0)
				sb.append(';').append(WebFontDialog.WEIGHT[weight]);
			if (variant > 0)
				sb.append(';').append(WebFontDialog.VARIANT[variant]);
			button.setText(sb.toString());
			button.setToolTipText(sb.toString());
			Rgb_type rgbtype = fonttype.getColor();
			if (rgbtype != null)
				paintButton(colorButton, rgbtype);
		}
	}

	public Font_type getFont() {
		return fonttype;
	}

	public Label getLabel() {
		return label;
	}

	public Font_type fillValues(IDialogSettings dialogSettings, String key, String[] family, int size, int style,
			int weight, int variant, Rgb_type rgb) {
		try {
			String s = dialogSettings.get(key + SIZE2);
			if (s != null)
				size = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			String s = dialogSettings.get(key + STYLE2);
			if (s != null)
				style = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			String s = dialogSettings.get(key + WEIGHT2);
			if (s != null)
				weight = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			String s = dialogSettings.get(key + VARIANT2);
			if (s != null)
				variant = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			String s = dialogSettings.get(key + COLOR2);
			if (s != null) {
				java.awt.Color color = new java.awt.Color(Integer.parseInt(s));
				rgb = new Rgb_typeImpl(color.getRed(), color.getGreen(), color.getBlue());
			}
		} catch (NumberFormatException e) {
			return null;
		}
		String[] array = dialogSettings.getArray(key + FAMILY2);
		if (array != null)
			family = array;
		Font_typeImpl font = new Font_typeImpl(size, style, weight, variant, rgb);
		font.setFamily(family);
		setFont(font);
		return font;
	}

	public void saveSettings(IDialogSettings dialogSettings, String key) {
		if (fonttype != null) {
			dialogSettings.put(key + SIZE2, fonttype.getSize());
			dialogSettings.put(key + STYLE2, fonttype.getStyle());
			dialogSettings.put(key + WEIGHT2, fonttype.getWeight());
			dialogSettings.put(key + VARIANT2, fonttype.getVariant());
			Rgb_type rgb = fonttype.getColor();
			if (rgb != null)
				dialogSettings.put(key + COLOR2, new java.awt.Color(rgb.getR(), rgb.getG(), rgb.getB()).getRGB());
			dialogSettings.put(key + FAMILY2, fonttype.getFamily());
		}
	}

	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
		colorButton.setEnabled(enabled);
	}

	public void setVisible(boolean visible) {
		button.setVisible(visible);
		colorButton.setVisible(visible);
	}
}
