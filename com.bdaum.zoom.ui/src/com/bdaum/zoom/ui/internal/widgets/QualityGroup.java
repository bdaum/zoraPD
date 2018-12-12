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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.widgets.CGroup;

public class QualityGroup {

	protected static final String PDFQUALITY = "pdfQuality"; //$NON-NLS-1$

	private Combo qualityField;
	private SharpeningGroup sharpenGroup;
	private ListenerList<Listener> selectionListeners = new ListenerList<>();

	private CompressionGroup compressionGroup;

	private CGroup group;

	public QualityGroup(Composite parent, boolean resolution) {
		group = new CGroup(parent, SWT.NONE);
		GridLayout layout = (GridLayout) parent.getLayout();
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, layout.numColumns, 1));
		group.setText(Messages.QualityGroup_output_quality);
		group.setLayout(new GridLayout(4, false));
		if (resolution) {
			new Label(group, SWT.NONE).setText(Messages.QualityGroup_resolution);
			qualityField = new Combo(group, SWT.READ_ONLY);
			qualityField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			qualityField.setItems(new String[] { Messages.QualityGroup_screen, Messages.QualityGroup_printer });
			qualityField.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					fireSelectionEvent(e);
				}
			});
		}
		Composite jpegGroup = new Composite(group, SWT.NONE);
		jpegGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, resolution ? 2 : 4, 1));
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		jpegGroup.setLayout(layout);
		compressionGroup = new CompressionGroup(jpegGroup, false);
		sharpenGroup = new SharpeningGroup(group);
	}

	protected void fireSelectionEvent(Event e) {
		for (Listener listener : selectionListeners)
			listener.handleEvent(e);
	}

	public void saveSettings(IDialogSettings settings) {
		compressionGroup.saveSettings(settings);
		sharpenGroup.saveSettings(settings);
		if (qualityField != null)
			settings.put(PDFQUALITY, qualityField.getSelectionIndex());
	}

	public int getQuality() {
		return qualityField == null ? Constants.SCREEN_QUALITY : qualityField.getSelectionIndex();
	}

	public int getJpegQuality() {
		return compressionGroup == null ? -1 : compressionGroup.getJpegQuality();
	}

	public UnsharpMask getUnsharpMask() {
		return sharpenGroup != null ? sharpenGroup.getUnsharpMask() : null;
	}

	public void fillValues(IDialogSettings settings) {
		sharpenGroup.fillValues(settings);
		compressionGroup.fillValues(settings);
		if (qualityField != null) {
			try {
				qualityField.select(settings.getInt(PDFQUALITY));
			} catch (NumberFormatException e1) {
				qualityField.select(Constants.SCREEN_QUALITY);
			}
		}
	}

	public void addListener(Listener listener) {
		selectionListeners.add(listener);
		compressionGroup.addListener(listener);
		sharpenGroup.addListener(listener);
	}

	public void removeListener(Listener listener) {
		selectionListeners.remove(listener);
		compressionGroup.removeListener(listener);
		sharpenGroup.removeListener(listener);
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

	public void fillValues(Boolean applySharpening, float radius, float amount, int threshold, int jpegQuality,
			int scalingMethod) {
		sharpenGroup.fillValues(applySharpening, radius, amount, threshold);
		compressionGroup.fillValues(jpegQuality, false);
	}

	public void setEnabled(boolean enabled) {
		sharpenGroup.setEnabled(enabled);
		compressionGroup.setEnabled(enabled);
	}

	public double getSizeFactor() {
		return compressionGroup.isEnabled() ? compressionGroup.getSizeFactor() : 1d;
	}

	public void setVisible(boolean visible) {
		group.setVisible(visible);
	}

}
