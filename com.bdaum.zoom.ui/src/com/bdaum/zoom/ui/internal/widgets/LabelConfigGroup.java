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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class LabelConfigGroup implements Listener {

	private static final String[] CAPTIONTEMPLATES = new String[] { "{title} - {creationDate}", //$NON-NLS-1$
			"{title} - {creationYear}", //$NON-NLS-1$
			"ISO{meta=scalarSpeedRatings}    {meta=focalLengthIn35MmFilm}($     f{meta=fNumber}$)    {meta=exposureTime}    {format}", //$NON-NLS-1$
			"ISO{meta=scalarSpeedRatings}     {meta=focalLength}($     f{meta=fNumber}$)    {meta=exposureTime}    {format}", //$NON-NLS-1$
			"{meta=iptc_location_created&city}|({title})" //$NON-NLS-1$
	};

	private RadioButtonGroup showGroup;
	private TextWithVariableGroup templateGroup;
	private NumericControl fontField;
	private int offset;
	private RadioButtonGroup alignmentGroup;
	private CheckboxButton overlayButton;
	private ListenerList<Listener> listeners = new ListenerList<Listener>();

	public LabelConfigGroup(Composite parent, boolean inherit, boolean alignment, boolean overlay) {
		offset = inherit ? 0 : 1;
		CGroup labelgroup = UiUtilities.createGroup(parent, 1, Messages.LabelConfigGroup_tumbnail_labels);
		showGroup = new RadioButtonGroup(labelgroup, Messages.LabelConfigGroup_show, SWT.VERTICAL,
				inherit ? new String[] { Messages.LabelConfigGroup_inherited, Messages.LabelConfigGroup_title,
						Messages.LabelConfigGroup_nothing, Messages.LabelConfigGroup_custom }
						: new String[] { Messages.LabelConfigGroup_title, Messages.LabelConfigGroup_nothing,
								Messages.LabelConfigGroup_custom });
		showGroup.addListener(SWT.Selection, this);
		Composite tempComp = new Composite(labelgroup, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalIndent = 15;
		tempComp.setLayoutData(layoutData);
		tempComp.setLayout(new GridLayout(5, false));
		templateGroup = new TextWithVariableGroup(tempComp, Messages.LabelConfigGroup_template, 400, Constants.TH_ALL,
				true, CAPTIONTEMPLATES, null, null);
		templateGroup.addListener(SWT.Modify, this);
		new Label(tempComp, SWT.NONE).setText(Messages.LabelConfigGroup_fontsize);
		Composite layoutComp = new Composite(tempComp, SWT.NONE);
		layoutComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		layoutComp.setLayout(layout);
		fontField = new NumericControl(layoutComp, SWT.NONE);
		fontField.setMinimum(5);
		fontField.setMaximum(14);
		fontField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		if (alignment)
			alignmentGroup = new RadioButtonGroup(layoutComp, Messages.LabelConfigGroup_alignment, SWT.HORIZONTAL,
					Messages.LabelConfigGroup_left, Messages.LabelConfigGroup_center, Messages.LabelConfigGroup_right);
		if (overlay)
			overlayButton = WidgetFactory.createCheckButton(layoutComp, Messages.LabelConfigGroup_overlay,
					new GridData(SWT.END, SWT.CENTER, true, false));
	}

	@Override
	public void handleEvent(Event e) {
		updateControls();
		fireEvent(e);
	}

	private void fireEvent(Event e) {
		e.type = SWT.Modify;
		e.data = this;
		for (Listener listener : listeners)
			listener.handleEvent(e);
	}

	private void updateControls() {
		boolean enabled = showGroup.getSelection() + offset == Constants.CUSTOM_LABEL;
		templateGroup.setEnabled(enabled);
		fontField.setEnabled(enabled);
	}

	public void setSelection(int index, String template, int fontSize, int align, boolean overlay) {
		showGroup.setSelection(index >= offset ? index - offset : Constants.TITLE_LABEL);
		if (template != null)
			templateGroup.setText(template);
		if (alignmentGroup != null)
			alignmentGroup.setSelection(align);
		if (overlayButton != null)
			overlayButton.setSelection(overlay);
		if (fontSize == 0)
			fontSize = JFaceResources.getDefaultFont().getFontData()[0].getHeight();
		fontField.setSelection(fontSize);
		updateControls();
	}

	public void setContext(String collection, Asset asset) {
		templateGroup.setContext(collection, asset);
	}

	public int getSelection() {
		int sel = showGroup.getSelection();
		if (sel >= 0)
			return sel + offset;
		return -1;
	}

	public String getTemplate() {
		return templateGroup.getText();
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify)
			listeners.add(listener);
	}

	public void removeSelectionListener(int type, Listener listener) {
		listeners.remove(listener);
	}

	public String validate() {
		if (showGroup.getSelection() + offset == Constants.CUSTOM_LABEL && templateGroup.getText().isEmpty())
			return Messages.LabelConfigGroup_provide_template;
		return null;
	}

	public int getFontSize() {
		return fontField.getSelection();
	}

	public void setAlignment(int align) {
		if (alignmentGroup != null)
			alignmentGroup.setSelection(align);
	}

	public int getAlignment() {
		return alignmentGroup == null ? 1 : alignmentGroup.getSelection();
	}

	public boolean getOverlay() {
		return overlayButton == null ? false : overlayButton.getSelection();
	}

}
