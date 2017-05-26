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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.ui.widgets.NumericControl;

public class CompressionGroup {

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

	public CompressionGroup(Composite parent, boolean method) {
		group = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = (GridLayout) parent.getLayout();
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, gridLayout.numColumns, 1));
		group.setLayout(new GridLayout(2, false));
		if (method)
			methodButton = WidgetFactory.createCheckButton(group, Messages.CompressionGroup_use_webp,
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1),
					Messages.CompressionGroup_webp_tooltip);
		new Label(group, SWT.NONE).setText(Messages.CompressionGroup_jpegQuality);
		numControl = new NumericControl(group, SWT.NONE);
		numControl.setMinimum(1);
		numControl.setMaximum(100);
		numControl.setIncrement(5);
		numControl.setPageIncrement(25);
	}

	public void setEnabled(boolean enabled) {
		numControl.setEnabled(enabled);
	}

	public void fillValues(IDialogSettings settings) {
		if (settings == null)
			numControl.setSelection(75);
		else {
			try {
				int v = settings.getInt(QUALITY);
				numControl.setSelection(v <= 0 || v > 100 ? 75 : v);
			} catch (NumberFormatException e) {
				numControl.setSelection(75);
			}
			if (methodButton != null) {
				boolean v = settings.getBoolean(METHOD);
				methodButton.setSelection(v);
			}
		}
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(QUALITY, numControl.getSelection());
		if (methodButton != null)
			settings.put(METHOD, methodButton.getSelection());
	}

	public void fillValues(int quality, boolean useWebP) {
		numControl.setSelection(quality > 0 ? quality : 75);
		if (methodButton != null)
			methodButton.setSelection(useWebP);
	}

	public int getJpegQuality() {
		return numControl.getSelection();
	}

	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Control#isEnabled()
	 */
	public boolean isEnabled() {
		return numControl.isEnabled();
	}

	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Control#isVisible()
	 */
	public boolean isVisible() {
		return group.isVisible();
	}

	/**
	 * @param visible
	 * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		group.setVisible(visible);
	}

	public double getSizeFactor() {
		return sizeTab[getJpegQuality()];
	}

	public void addSelectionListener(SelectionListener listener) {
		numControl.addSelectionListener(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		numControl.addSelectionListener(listener);
	}

	public boolean getUseWebp() {
		return methodButton == null ? false : methodButton.getSelection();
	}

}
