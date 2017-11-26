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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class LabelConfigGroup implements SelectionListener {

	private RadioButtonGroup showGroup;
	private TextWithVariableGroup templateGroup;
	private NumericControl fontField;

	private int offset;

	public LabelConfigGroup(Composite parent, boolean inherit) {
		offset = inherit ? 0 : 1;
		CGroup labelgroup = UiUtilities.createGroup(parent, 1,
				com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_tumbnail_labels);
		showGroup = new RadioButtonGroup(labelgroup, com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_show,
				SWT.VERTICAL,
				inherit ? new String[] { com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_inherited,
						com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_title,
						com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_nothing,
						com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_custom }
						: new String[] { com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_title,
								com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_nothing,
								com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_custom });
		showGroup.addSelectionListener(this);
		Composite tempComp = new Composite(labelgroup, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalIndent = 15;
		tempComp.setLayoutData(layoutData);
		tempComp.setLayout(new GridLayout(4, false));
		templateGroup = new TextWithVariableGroup(tempComp,
				com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_template,
				com.bdaum.zoom.ui.internal.widgets.Messages.LabelConfigGroup_variables, Constants.TH_ALL, true);
		new Label(tempComp, SWT.NONE).setText(Messages.LabelConfigGroup_fontsize);
		fontField = new NumericControl(tempComp, SWT.NONE);
		fontField.setMinimum(5);
		fontField.setMaximum(14);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		updateControls();
	}

	private void updateControls() {
		boolean enabled = showGroup.getSelection() + offset == Constants.CUSTOM_LABEL;
		templateGroup.setEnabled(enabled);
		fontField.setEnabled(enabled);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	public void setSelection(int index, String template, int fontSize) {
		showGroup.setSelection(index - offset);
		if (index == Constants.CUSTOM_LABEL && template != null)
			templateGroup.setText(template);
		if (fontSize == 0)
			fontSize = JFaceResources.getDefaultFont().getFontData()[0].getHeight();
		fontField.setSelection(fontSize);
		updateControls();
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

	public void addSelectionListener(SelectionListener selectionListener) {
		showGroup.addSelectionListener(selectionListener);
	}

	public void removeSelectionListener(SelectionListener selectionListener) {
		showGroup.removeSelectionListener(selectionListener);
	}

	public String validate() {
		if (showGroup.getSelection() + offset == Constants.CUSTOM_LABEL && templateGroup.getText().isEmpty())
			return Messages.LabelConfigGroup_provide_template;
		return null;
	}

	public int getFontSize() {
		return fontField.getSelection();
	}

}
