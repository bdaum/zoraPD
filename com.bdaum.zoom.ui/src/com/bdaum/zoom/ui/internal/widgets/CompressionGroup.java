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
 * (c) 2012 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.ui.widgets.NumericControl;

public class CompressionGroup implements Listener {

	private static final String QUALITY = "jpegQuality"; //$NON-NLS-1$
	private static final String METHOD = "method"; //$NON-NLS-1$

	private static final double[] sizeTab = new double[] { 0.62, 0.62, 0.62, 0.62, 0.62, 0.63, 0.63, 0.63, 0.63, 0.63,
			0.64, 0.64, 0.64, 0.64, 0.64, 0.65, 0.65, 0.65, 0.65, 0.65, 0.66, 0.66, 0.66, 0.66, 0.66, 0.67, 0.67, 0.67,
			0.67, 0.67, 0.68, 0.68, 0.68, 0.68, 0.68, 0.69, 0.69, 0.69, 0.69, 0.69, 0.70, 0.70, 0.71, 0.71, 0.72, 0.72,
			0.73, 0.73, 0.74, 0.74, 0.75, 0.76, 0.78, 0.80, 0.82, 0.83, 0.85, 0.86, 0.88, 0.90, 0.91, 0.93, 0.96, 0.98,
			1.00, 1.01, 1.02, 1.03, 1.04, 1.05, 1.07, 1.11, 1.15, 1.19, 1.23, 1.27, 1.31, 1.36, 1.42, 1.48, 1.55, 1.75,
			1.95, 2.15, 2.35, 2.55, 3.10, 3.75, 4.40, 5.05, 5.77 };

	private NumericControl numControl;
	private Composite group;

	private CheckboxButton methodButton;
	private Label hintLabel;
	private boolean webP = false;

	public CompressionGroup(Composite parent, boolean method) {
		group = new Composite(parent, SWT.NONE);
		group.setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false, ((GridLayout) parent.getLayout()).numColumns, 1));
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		group.setLayout(layout);
		if (method) {
			methodButton = WidgetFactory.createCheckButton(group, Messages.CompressionGroup_use_webp,
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1),
					Messages.CompressionGroup_webp_tooltip);
			methodButton.addListener(SWT.Selection, this);
		}
		new Label(group, SWT.NONE).setText(Messages.CompressionGroup_jpegQuality);
		numControl = new NumericControl(group, SWT.NONE);
		numControl.setMinimum(1);
		numControl.setMaximum(100);
		numControl.setIncrement(5);
		numControl.setPageIncrement(25);
		hintLabel = new Label(group, SWT.NONE);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.horizontalIndent = 20;
		hintLabel.setLayoutData(data);
		hintLabel.setText(Messages.CompressionGroup_enter_0);
		hintLabel.setVisible(false);
	}

	public void setEnabled(boolean enabled) {
		numControl.setEnabled(enabled);
	}

	public void fillValues(IDialogSettings settings) {
		if (settings == null)
			numControl.setSelection(75);
		else {
			webP = settings.getBoolean(METHOD);
			if (methodButton != null)
				methodButton.setSelection(webP);
			try {
				int quality = settings.getInt(QUALITY);
				numControl.setSelection(quality <= 0 || quality > 100 ? 75 : quality);
			} catch (NumberFormatException e) {
				numControl.setSelection(75);
			}
		}
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(QUALITY, numControl.getSelection());
		settings.put(METHOD, webP);
	}

	public void fillValues(int quality, boolean useWebP) {
		webP = useWebP;
		numControl.setSelection(quality <= 0 || quality > 100 ? 75 : quality);
		if (methodButton != null)
			methodButton.setSelection(useWebP);
		updateLabel();
	}

	public int getJpegQuality() {
		return numControl.getSelection();
	}

	public boolean isEnabled() {
		return numControl.isEnabled();
	}

	public boolean isVisible() {
		return group.isVisible();
	}

	public void setVisible(boolean visible) {
		group.setVisible(visible);
	}

	public double getSizeFactor() {
		return sizeTab[getJpegQuality()];
	}

	public void addListener(int type, Listener listener) {
		numControl.addListener(type, listener);
	}

	public void removeListener(int type, Listener listener) {
		numControl.addListener(type, listener);
	}

	public boolean getUseWebp() {
		return methodButton != null && methodButton.getSelection();
	}

	public void setUseWebP(boolean b) {
		this.webP = b;
		if (methodButton != null)
			methodButton.setSelection(b);
		updateLabel();
	}

	@Override
	public void handleEvent(Event event) {
		webP = methodButton.getSelection();
		updateLabel();
	}

	private void updateLabel() {
		hintLabel.setVisible(webP);
	}

}
