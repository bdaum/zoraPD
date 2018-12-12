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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.report.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class PresentationPage extends ColoredWizardPage implements Listener {

	private static final int PREVIEWLIMIT = 50;
	private CheckboxButton threeButton;
	private CheckboxButton cumulateButton;
	private Report report;
	private int mode;
	private ReportComponent reportComponent;
	private CheckboxButton cylinderButton;
	private RadioButtonGroup chartButtonGroup;

	public PresentationPage(String id, String title, String msg, ImageDescriptor imageDescriptor) {
		super(id, title, imageDescriptor);
		setMessage(msg);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		setHelp(HelpContextIds.REPORT_WIZARD);
		CGroup chartTypeGroup = UiUtilities.createGroup(composite, 2, Messages.PresentationPage_chartType);
		chartButtonGroup = new RadioButtonGroup(chartTypeGroup, null, 2, Messages.PresentationPage_pie,
				Messages.PresentationPage_bar, Messages.PresentationPage_line, Messages.PresentationPage_step,
				Messages.PresentationPage_area);
		chartButtonGroup.addListener(this);
		CGroup optionsGroup = UiUtilities.createGroup(composite, 1, Messages.PresentationPage_options);
		threeButton = WidgetFactory.createCheckButton(optionsGroup, Messages.PresentationPage_threeD, null);
		threeButton.addListener(this);
		cylinderButton = WidgetFactory.createCheckButton(optionsGroup, Messages.PresentationPage_cylindric, null);
		cylinderButton.addListener(this);
		cumulateButton = WidgetFactory.createCheckButton(optionsGroup, Messages.PresentationPage_cumulate, null);
		cumulateButton.addListener(this);
		CGroup previewGroup = UiUtilities.createGroup(composite, 1,
				NLS.bind(Messages.PresentationPage_preview, PREVIEWLIMIT));
		reportComponent = new ReportComponent(previewGroup, SWT.NONE, PREVIEWLIMIT);
		reportComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createControl(parent);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		reportComponent.setReport(null);
		if (visible) {
			report = ((ReportWizard) getWizard()).getReport();
			mode = report.getMode();
			if ((mode & ReportWizard.PIE) != 0)
				chartButtonGroup.setSelection(0);
			else if ((mode & ReportWizard.BAR) != 0)
				chartButtonGroup.setSelection(1);
			else if ((mode & ReportWizard.LINE) != 0)
				chartButtonGroup.setSelection(2);
			else if ((mode & ReportWizard.STEP) != 0)
				chartButtonGroup.setSelection(3);
			else if ((mode & ReportWizard.AREA) != 0)
				chartButtonGroup.setSelection(4);
			if ((mode & ReportWizard.DISCRETE) != 0) {
				chartButtonGroup.setEnabled(false);
				chartButtonGroup.setEnabled(0, true);
				chartButtonGroup.setEnabled(1, true);
				if (chartButtonGroup.getSelection() != 0)
					chartButtonGroup.setSelection(1);
			} else {
				chartButtonGroup.setEnabled(true);
				chartButtonGroup.setEnabled(0, false);
				if (chartButtonGroup.getSelection() == 0)
					chartButtonGroup.setSelection(1);
			}
			updateFields();
			validatePage();
			if (isPageComplete())
				reportComponent.setReport(report);
		}
	}

	protected void updateFields() {

		mode &= ~ReportWizard.ALLTYPES;
		int selection = chartButtonGroup.getSelection();
		if (selection == 1)
			mode |= ReportWizard.BAR;
		else if (selection == 2)
			mode |= ReportWizard.LINE;
		else if (selection == 3)
			mode |= ReportWizard.STEP;
		else if (selection == 0)
			mode |= ReportWizard.PIE;
		else if (selection == 4)
			mode |= ReportWizard.AREA;
		if ((mode & ReportWizard.BAR) != 0) {
			threeButton.setEnabled(true);
			cylinderButton.setEnabled(true);
			cumulateButton.setEnabled((mode & (ReportWizard.DAYTIME | ReportWizard.TIME)) != 0);
		} else if ((mode & ReportWizard.LINE) != 0) {
			threeButton.setEnabled(true);
			cylinderButton.setEnabled(false);
			cumulateButton.setEnabled((mode & (ReportWizard.DAYTIME | ReportWizard.TIME)) != 0);
		} else if ((mode & ReportWizard.STEP) != 0) {
			threeButton.setEnabled(false);
			cylinderButton.setEnabled(false);
			cumulateButton.setEnabled((mode & (ReportWizard.DAYTIME | ReportWizard.TIME)) != 0);
		} else if ((mode & ReportWizard.PIE) != 0) {
			threeButton.setEnabled(!ReportWizard.isMultiple(mode));
			cylinderButton.setEnabled(false);
			cumulateButton.setEnabled(false);
		} else if ((mode & ReportWizard.AREA) != 0) {
			threeButton.setEnabled(false);
			cylinderButton.setEnabled(false);
			cumulateButton.setEnabled((mode & (ReportWizard.DAYTIME | ReportWizard.TIME)) != 0);
		}

		mode &= ~ReportWizard.ALLOPTIONS;
		if (threeButton.isEnabled() && threeButton.getSelection())
			mode |= ReportWizard.THREEDIM;
		if (cylinderButton.isEnabled() && cylinderButton.getSelection())
			mode |= ReportWizard.CYLINDER;
		if (cumulateButton.isEnabled() && cumulateButton.getSelection())
			mode |= ReportWizard.CUMULATE;
		report.setMode(mode);
	}

	@Override
	protected void validatePage() {
		boolean valid = chartButtonGroup.getSelection() >= 0;
		setErrorMessage(valid ? null : Messages.PresentationPage_select_type);
		setPageComplete(valid);
	}

	@Override
	public void handleEvent(Event e) {
		reportComponent.setReport(null);
		if (e.widget == cylinderButton && cylinderButton.getSelection())
			threeButton.setSelection(true);
		else if (e.widget == threeButton && !threeButton.getSelection())
			cylinderButton.setSelection(false);
		updateFields();
		validatePage();
		if (isPageComplete())
			reportComponent.setReport(report);
	}

}
