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
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Date;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class FindSeriesDialog extends ZTitleAreaDialog implements Listener {

	private static final String SEPARATE_FORMATS = "separateFormats"; //$NON-NLS-1$

	private static final String SETTINGSID = "com.bdaum.zoom.findSeriesDialog"; //$NON-NLS-1$

	private static final String INTERVAL = "interval"; //$NON-NLS-1$

	private static final String IGNOREDERIVED = "ignoreDerived"; //$NON-NLS-1$

	private static final String SIZE = "size"; //$NON-NLS-1$

	private static final String[] TYPES = new String[] {Messages.FindSeriesDialog_all, Messages.FindSeriesDialog_exposure_bracket, Messages.FindSeriesDialog_focus_bracket, Messages.FindSeriesDialog_zoom_bracket, Messages.FindSeriesDialog_rapid_fire, Messages.FindSeriesDialog_other };

	private static final String TYPE = "type"; //$NON-NLS-1$

	private boolean ignoreDerivates;

	private NumericControl intervalField;

	private FindWithinGroup findWithinGroup;

	private IDialogSettings settings;

	private CheckboxButton ignoreDerivedButton;

	private int interval;

	private SmartCollectionImpl result;

	private NumericControl sizeField;

	private int size;

	private CheckboxButton formatField;

	private boolean separateFormats;

	private int type;

	private Combo typeField;

	public FindSeriesDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.SERIES_DIALOG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FindSeriesDialog_find_series);
		setMessage(Messages.FindSeriesDialog_please_specify_maximum_interval);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		new Label(comp, SWT.NONE).setText(Messages.FindSeriesDialog_series_type);
		typeField = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		typeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		typeField.setItems(TYPES);
		typeField.setVisibleItemCount(6);
		typeField.addListener(SWT.Selection, this);
		new Label(comp, SWT.NONE).setText(Messages.FindSeriesDialog_maximum_interval);
		intervalField = new NumericControl(comp, NumericControl.LOGARITHMIC);
		intervalField.setMinimum(0);
		intervalField.setMaximum(1000);
		new Label(comp, SWT.NONE).setText(Messages.FindSeriesDialog_minimum_length);
		sizeField = new NumericControl(comp, NumericControl.LOGARITHMIC);
		sizeField.setMinimum(2);
		sizeField.setMaximum(1000);
		formatField = WidgetFactory.createCheckButton(comp,
				Messages.FindSeriesDialog_separate_by_format, new GridData(
						SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		ignoreDerivedButton = WidgetFactory.createCheckButton(comp,
				Messages.FindDuplicatesDialog_ignore_derived, new GridData(SWT.BEGINNING, SWT.CENTER, false,
						false, 2, 1));
		findWithinGroup = new FindWithinGroup(area);
		fillValues();
		return area;
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		try {
			intervalField.setSelection(settings.getInt(INTERVAL));
		} catch (NumberFormatException e) {
			intervalField.setSelection(1);
		}
		try {
			sizeField.setSelection(settings.getInt(SIZE));
		} catch (NumberFormatException e) {
			sizeField.setSelection(5);
		}
		try {
			typeField.select(settings.getInt(TYPE));
		} catch (NumberFormatException e) {
			typeField.select(0);
		}
		formatField.setSelection(settings.getBoolean(SEPARATE_FORMATS));
		ignoreDerivedButton.setSelection(settings.getBoolean(IGNOREDERIVED));
		findWithinGroup.fillValues(settings);
		updateButtons();
	}

	private void updateButtons() {
		int type = typeField.getSelectionIndex();
		boolean enabled = type == Constants.SERIES_ALL;
		formatField.setEnabled(enabled);
		ignoreDerivedButton.setEnabled(enabled);
		intervalField.setEnabled(type != Constants.SERIES_RAPID);
	}

	@Override
	protected void okPressed() {
		interval = intervalField.getSelection();
		settings.put(INTERVAL, interval);
		size = sizeField.getSelection();
		settings.put(SIZE, size);
		type = typeField.getSelectionIndex();
		settings.put(TYPE, type);
		separateFormats = formatField.getSelection();
		settings.put(SEPARATE_FORMATS, separateFormats);
		ignoreDerivates = ignoreDerivedButton.getSelection();
		settings.put(IGNOREDERIVED, ignoreDerivates);
		findWithinGroup.saveValues(settings);
		result = new SmartCollectionImpl(Messages.FindSeriesDialog_find_series,
				false, false, true, false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
		result.addCriterion(new CriterionImpl(
				QueryField.EXIF_DATETIMEORIGINAL.getKey(),null, new Date(86400000L),null,
				QueryField.GREATER, false));
		if (separateFormats)
			result.addSortCriterion(new SortCriterionImpl(
					QueryField.FORMAT.getKey(),  null, false));
		result.addSortCriterion(new SortCriterionImpl(
				QueryField.IPTC_DATECREATED.getKey(), null,  false));
		result.setSmartCollection_subSelection_parent(findWithinGroup
				.getParentCollection());
		super.okPressed();
	}

	public int getInterval() {
		return interval;
	}

	public boolean getIgnoreDerivates() {
		return ignoreDerivates;
	}

	public SmartCollectionImpl getResult() {
		return result;
	}

	public int getSize() {
		return size;
	}

	public boolean isSeparateFormats() {
		return separateFormats;
	}

	public int getType() {
		return type;
	}

	@Override
	public void handleEvent(Event event) {
		updateButtons();
	}

}
