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
 * (c) 2020 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;

public class ColorFilterDialog extends ZTitleAreaDialog implements Listener {

	private static final RGB DFLTCOLOR = new RGB(255, 255, 255);

	private static final String[] filterDescr = new String[] {

			"Warming85 #EC8A00", //$NON-NLS-1$

			"WarmingLBA #FA9600", //$NON-NLS-1$

			"Warming81 #EBB113", //$NON-NLS-1$

			"Coolling80 #006DFF", //$NON-NLS-1$

			"CoolingLBB #005DFF", //$NON-NLS-1$

			"Cooling82 #00B5FF", //$NON-NLS-1$

			"Red #EA1A1A", //$NON-NLS-1$

			"Orange #F38417", //$NON-NLS-1$

			"Yellow #F9E31C", //$NON-NLS-1$

			"Green #19C919", //$NON-NLS-1$

			"Cyan #1DCBEA", //$NON-NLS-1$

			"Blue #1D35EA", //$NON-NLS-1$

			"Violet #9B1DEA", //$NON-NLS-1$

			"Magenta #E318E3", //$NON-NLS-1$

			"Sepia #AC7A33", //$NON-NLS-1$

			"DeepRed #FF0000", //$NON-NLS-1$

			"DeepBlue #0022CD", //$NON-NLS-1$

			"DeepEmerald #008C00", //$NON-NLS-1$

			"DeepYellow #FFD500", //$NON-NLS-1$

			"Underwater #00C1B1" }; //$NON-NLS-1$

	private static final String[] names = new String[filterDescr.length];
	private static final RGB[] rgbs = new RGB[filterDescr.length];

	private static final String CUSTOMCOLOR = "customcolor"; //$NON-NLS-1$
	private final Image[] images = new Image[filterDescr.length];

	private RGB rgb;

	private String title;

	private Label lastLabel;

	private Button[] customButton = new Button[4];

	private Image[] customImage = new Image[4];

	private RGB[] customRgb = new RGB[4];

	private IDialogSettings dialogSettings;

	static {
		for (int i = 0; i < filterDescr.length; i++) {
			String fd = filterDescr[i];
			int p = fd.lastIndexOf('#');
			names[i] = ColorMessages.getString(fd.substring(0, p).trim());
			java.awt.Color c = java.awt.Color.decode(fd.substring(p));
			rgbs[i] = new RGB(c.getRed(), c.getGreen(), c.getBlue());
		}
	}

	public ColorFilterDialog(Shell parentShell) {
		super(parentShell);
		Display display = parentShell.getDisplay();
		for (int i = 0; i < rgbs.length; i++) {
			images[i] = new Image(display, 16, 16);
			GC gc = new GC(images[i]);
			Color c = new Color(display, rgbs[i]);
			gc.setBackground(c);
			gc.fillRectangle(0, 0, 16, 16);
			c.dispose();
			gc.dispose();
		}
		dialogSettings = getDialogSettings(UiActivator.getDefault(), "colorFilterDialog"); //$NON-NLS-1$
		for (int i = 0; i < 4; i++)
			customRgb[i] = StringConverter.asRGB(dialogSettings.get(CUSTOMCOLOR + i), DFLTCOLOR);
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.ColorFilterDialog_filter_message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Display display = parent.getDisplay();
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(8, false));
		for (int i = 0; i < images.length; i++) {
			Button button = new Button(composite, SWT.PUSH);
			button.setImage(images[i]);
			button.setData(rgbs[i]);
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText(names[i]);
			Font font;
			if (lastLabel == null && rgbs[i].equals(rgb)) {
				font = JFaceResources.getFont(UiConstants.SELECTIONFONT);
				lastLabel = label;
			} else
				font = JFaceResources.getDefaultFont();
			label.setFont(font);
			button.setData("label", label); //$NON-NLS-1$
			button.addListener(SWT.Selection, this);
		}
		Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1));
		for (int i = 0; i < 4; i++) {
			customButton[i] = new Button(composite, SWT.PUSH);
			customImage[i] = new Image(display, 16, 16);
			GC gc = new GC(customImage[i]);
			Color c = new Color(display, customRgb[i]);
			gc.setBackground(c);
			gc.fillRectangle(0, 0, 16, 16);
			c.dispose();
			gc.dispose();
			customButton[i].setImage(customImage[i]);
			customButton[i].setData(customRgb[i]);
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			Font font;
			if (lastLabel == null && customRgb[i].equals(rgb)) {
				font = JFaceResources.getFont(UiConstants.SELECTIONFONT);
				lastLabel = label;
			} else
				font = JFaceResources.getDefaultFont();
			label.setFont(font);
			label.setText(Messages.ColorFilterDialog_custom_color + (i + 1));
			customButton[i].setData("label", label); //$NON-NLS-1$
			customButton[i].addListener(SWT.Selection, this);
		}
		return area;
	}

	public void setRGB(RGB rgb) {
		this.rgb = rgb;
	}

	public void setText(String title) {
		this.title = title;
	}

	public RGB getRgb() {
		return rgb;
	}

	@Override
	public void handleEvent(Event event) {
		if (lastLabel != null)
			lastLabel.setFont(JFaceResources.getDefaultFont());
		Button button = (Button) event.widget;
		lastLabel = (Label) button.getData("label"); //$NON-NLS-1$
		lastLabel.setFont(JFaceResources.getFont(UiConstants.SELECTIONFONT));
		rgb = (RGB) button.getData();
		for (int j = 0; j < customButton.length; j++)
			if (button == customButton[j]) {
				ColorDialog dialog = new ColorDialog(getShell());
				dialog.setText(((Label) button.getData("label")).getText()); //$NON-NLS-1$
				dialog.setRGB(rgb);
				RGB newRgb = dialog.open();
				if (newRgb != null) {
					button.setData(rgb = customRgb[j] = newRgb);
					GC gc = new GC(customImage[j]);
					Color c = new Color(gc.getDevice(), rgb);
					gc.setBackground(c);
					gc.fillRectangle(0, 0, 16, 16);
					c.dispose();
					gc.dispose();
					button.setImage(customImage[j]);
				}
				return;
			}
	}

	@Override
	protected void okPressed() {
		saveValues();
		super.okPressed();
	}

	private void saveValues() {
		for (int i = 0; i < 4; i++)
			dialogSettings.put(CUSTOMCOLOR + i, StringConverter.asString(customRgb[i]));
	}

	@Override
	public boolean close() {
		for (Image image : images)
			image.dispose();
		for (Image image : customImage)
			image.dispose();
		return super.close();
	}

}
