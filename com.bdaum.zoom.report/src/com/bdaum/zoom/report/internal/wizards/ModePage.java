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
package com.bdaum.zoom.report.internal.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.report.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ModePage extends ColoredWizardPage implements Listener {

	private CheckboxButton countButton;
	private CheckboxButton salesButton;
	private CheckboxButton earningsButton;
	private Report report;
	private int mode;
	private RadioButtonGroup domainGroup;
	private RadioButtonGroup sortButtonGroup;
	private RadioButtonGroup sortDirectionGroup;

	public ModePage(String id, String title, String msg, ImageDescriptor imageDescriptor) {
		super(id, title, imageDescriptor);
		setMessage(msg);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		setHelp(HelpContextIds.REPORT_WIZARD);
		CGroup valueGroup = new CGroup(composite, SWT.NONE);
		valueGroup.setLayout(new GridLayout());
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		layoutData.verticalIndent = 20;
		valueGroup.setLayoutData(layoutData);
		valueGroup.setText(Messages.ModePage_values_y);
		countButton = WidgetFactory.createCheckButton(valueGroup, Messages.ModePage_imageCount,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		countButton.addListener(this);
		salesButton = WidgetFactory.createCheckButton(valueGroup, Messages.ModePage_dales,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		salesButton.addListener(this);
		earningsButton = WidgetFactory.createCheckButton(valueGroup, Messages.ModePage_earnings,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		earningsButton.addListener(this);
		CGroup xGroup = new CGroup(composite, SWT.NONE);
		xGroup.setLayout(new FillLayout());
		xGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		xGroup.setText(Messages.ModePage_xAxis);
		domainGroup = new RadioButtonGroup(xGroup, null, SWT.NONE, Messages.ModePage_daytime, Messages.ModePage_caltime, Messages.ModePage_numeric,
				Messages.ModePage_discrete);
		domainGroup.addListener(this);
		domainGroup.setSelection(1);
		CGroup optionsGroup = new CGroup(composite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		optionsGroup.setLayout(new GridLayout());
		optionsGroup.setText(Messages.ModePage_sorting);
		sortButtonGroup = new RadioButtonGroup(optionsGroup, Messages.ModePage_sortBy, SWT.NONE, Messages.ModePage_noSort, Messages.ModePage_name, Messages.ModePage_count, Messages.ModePage_sales,
				Messages.ModePage_earnings);
		sortButtonGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true));
		sortButtonGroup.addListener(this);
		sortButtonGroup.setSelection(0);
		sortDirectionGroup = new RadioButtonGroup(optionsGroup, Messages.ModePage_sortDirection, SWT.NONE, Messages.ModePage_ascending, Messages.ModePage_descending);
		layoutData = new GridData(SWT.BEGINNING, SWT.FILL, true, true);
		layoutData.verticalIndent = 5;
		sortDirectionGroup.setLayoutData(layoutData);
		sortDirectionGroup.addListener(this);
		sortDirectionGroup.setSelection(0);
		countButton.setSelection(true);
		super.createControl(parent);
	}

	private void updateFields() {
		if (report != null) {
			mode &= ~ReportWizard.ALLMODES;
			mode &= ~ReportWizard.ALLVALUES;
			if (countButton.getSelection())
				mode |= ReportWizard.IMAGECOUNT;
			if (salesButton.getSelection())
				mode |= ReportWizard.SALES;
			if (earningsButton.getSelection())
				mode |= ReportWizard.EARNINGS;
			switch (domainGroup.getSelection()) {
			case 0:
				mode |= ReportWizard.DAYTIME;
				break;
			case 1:
				mode |= ReportWizard.TIME;
				break;
			case 2:
				mode |= ReportWizard.NUMERIC;
				break;
			case 3:
				mode |= ReportWizard.DISCRETE;
				break;
			}
			updateSortOptions();
			report.setMode(mode);
		}
	}

	@Override
	protected void validatePage() {
		String errorMessage = null;
		if ((mode & ReportWizard.ALLVALUES) == 0)
			errorMessage = Messages.ModePage_at_east_one;
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			report = ((ReportWizard) getWizard()).getReport();
			mode = report.getMode();
			countButton.setSelection((mode & ReportWizard.IMAGECOUNT) != 0);
			salesButton.setSelection((mode & ReportWizard.SALES) != 0);
			earningsButton.setSelection((mode & ReportWizard.EARNINGS) != 0);
			domainGroup.setSelection((mode & ReportWizard.DAYTIME) != 0 ? 0
					: (mode & ReportWizard.TIME) != 0 ? 1 : (mode & ReportWizard.NUMERIC) != 0 ? 2 : 3);
			int sortMode = report.getSortField();
			sortButtonGroup.setSelection((sortMode & ReportWizard.NAME) != 0 ? 1
					: (sortMode & ReportWizard.IMAGECOUNT) != 0 ? 2
							: (sortMode & ReportWizard.SALES) != 0 ? 3
									: (sortMode & ReportWizard.EARNINGS) != 0 ? 4 : 0);
			sortDirectionGroup.setSelection(report.getDescending() ? 1 : 0);
			updateSortOptions();
			validatePage();
		}
		super.setVisible(visible);
	}

	private void updateSortOptions() {
		if ((mode & ReportWizard.DISCRETE) != 0) {
			sortButtonGroup.setEnabled(true);
			sortDirectionGroup.setEnabled(true);
			if ((mode & ReportWizard.IMAGECOUNT) == 0) {
				if (sortButtonGroup.getSelection() == 2)
					sortButtonGroup.setSelection(0);
				sortButtonGroup.setEnabled(2, false);
			}
			if ((mode & ReportWizard.SALES) == 0) {
				if (sortButtonGroup.getSelection() == 3)
					sortButtonGroup.setSelection(0);
				sortButtonGroup.setEnabled(3, false);
			}
			if ((mode & ReportWizard.EARNINGS) == 0) {
				if (sortButtonGroup.getSelection() == 4)
					sortButtonGroup.setSelection(0);
				sortButtonGroup.setEnabled(4, false);
			}
			int sortMode = 0;
			switch (sortButtonGroup.getSelection()) {
			case 1:
				sortMode = ReportWizard.NAME;
				break;
			case 2:
				sortMode = ReportWizard.IMAGECOUNT;
				break;
			case 3:
				sortMode = ReportWizard.SALES;
				break;
			case 4:
				sortMode = ReportWizard.EARNINGS;
				break;
			}
			report.setSortField(sortMode);
			report.setDescending(sortDirectionGroup.getSelection() == 1);
		} else {
			sortButtonGroup.setEnabled(false);
			sortDirectionGroup.setEnabled(false);
		}
	}

	public void handleEvent(Event e) {
		updateFields();
		validatePage();
	}

}
