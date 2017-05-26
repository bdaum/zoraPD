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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.widgets.CGroup;

public class QualityGroup {

	protected static final String PDFQUALITY = "pdfQuality"; //$NON-NLS-1$

	private Combo qualityField;
	private SharpeningGroup sharpenGroup;
	private ListenerList<SelectionListener> selectionListeners = new ListenerList<SelectionListener>();

	private CompressionGroup compressionGroup;

	private CGroup group;

	public QualityGroup(Composite parent, boolean resolution) {
		group = new CGroup(parent, SWT.NONE);
		GridLayout layout = (GridLayout) parent.getLayout();
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true,
				false, layout.numColumns, 1);
		group.setLayoutData(layoutData);
		group.setText(Messages.QualityGroup_output_quality);
		group.setLayout(new GridLayout(2, false));
		if (resolution) {
			final Label qualityLabel = new Label(group, SWT.NONE);
			qualityLabel.setText(Messages.QualityGroup_resolution);
			qualityField = new Combo(group, SWT.READ_ONLY);
			qualityField.setItems(new String[] { Messages.QualityGroup_screen,
					Messages.QualityGroup_printer });
			qualityField.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fireSelectionEvent(e);
				}
			});
		}
		sharpenGroup = new SharpeningGroup(group);
		Composite jpegGroup = new Composite(group, SWT.NONE);
		jpegGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		jpegGroup.setLayout(layout);
		compressionGroup = new CompressionGroup(jpegGroup, false);
	}

	protected void fireSelectionEvent(SelectionEvent e) {
		for (Object listener : selectionListeners.getListeners())
			((SelectionListener) listener).widgetSelected(e);
	}

	public void saveSettings(IDialogSettings settings) {
		compressionGroup.saveSettings(settings);
		sharpenGroup.saveSettings(settings);
		if (qualityField != null)
			settings.put(PDFQUALITY, qualityField.getSelectionIndex());
	}

	public int getQuality() {
		return qualityField == null ? Constants.SCREEN_QUALITY : qualityField
				.getSelectionIndex();
	}

	public int getJpegQuality() {
		return compressionGroup == null ? -1 : compressionGroup
				.getJpegQuality();
	}

	public UnsharpMask getUnsharpMask() {
		return sharpenGroup != null ? sharpenGroup.getUnsharpMask() : null;
	}

	public void fillValues(IDialogSettings settings) {
		sharpenGroup.fillValues(settings);
		compressionGroup.fillValues(settings);
		if (qualityField != null) {
			int quality;
			try {
				quality = settings.getInt(PDFQUALITY);
			} catch (NumberFormatException e1) {
				quality = Constants.SCREEN_QUALITY;
			}
			qualityField.select(quality);
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * @return
	 * @see com.bdaum.zoom.ui.internal.widgets.SharpeningGroup#getApplySharpening()
	 */
	public boolean getApplySharpening() {
		return sharpenGroup.getApplySharpening();
	}

	/**
	 * @return
	 * @see com.bdaum.zoom.ui.internal.widgets.SharpeningGroup#getRadius()
	 */
	public float getRadius() {
		return sharpenGroup.getRadius();
	}

	/**
	 * @return
	 * @see com.bdaum.zoom.ui.internal.widgets.SharpeningGroup#getAmount()
	 */
	public float getAmount() {
		return sharpenGroup.getAmount();
	}

	/**
	 * @return
	 * @see com.bdaum.zoom.ui.internal.widgets.SharpeningGroup#getThreshold()
	 */
	public int getThreshold() {
		return sharpenGroup.getThreshold();
	}

	public void fillValues(Boolean applySharpening, float radius, float amount,
			int threshold, int jpegQuality, int scalingMethod) {
			sharpenGroup.fillValues(applySharpening, radius, amount, threshold);
			compressionGroup.fillValues(jpegQuality, false);
	}

	public void setEnabled(boolean enabled) {
			sharpenGroup.setEnabled(enabled);
			compressionGroup.setEnabled(enabled);
	}

	public double getSizeFactor() {
		return compressionGroup.isEnabled() ? compressionGroup
				.getSizeFactor() : 1d;
	}

	public void setVisible(boolean visible) {
		group.setVisible(visible);
	}

}
