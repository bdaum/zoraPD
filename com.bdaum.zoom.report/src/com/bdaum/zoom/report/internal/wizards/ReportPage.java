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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.report.internal.HelpContextIds;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class ReportPage extends ColoredWizardPage {

	private ReportComponent reportComponent;
	private Report report;

	public ReportPage(String id, String message, String msg, ImageDescriptor imageDescriptor) {
		super(id, message, imageDescriptor);
		setMessage(msg);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		setHelp(HelpContextIds.REPORT_WIZARD);
		reportComponent = new ReportComponent(composite, SWT.NONE, Integer.MAX_VALUE);
		reportComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createControl(parent);
	}

	@Override
	public void setVisible(boolean visible) {
		report = ((ReportWizard) getWizard()).getReport();
		reportComponent.setReport(visible ? report : null);
		super.setVisible(visible);
	}

	@Override
	protected void validatePage() {
		// nothing to do
	}

	public boolean finish() {
		reportComponent.saveChartProperties(report);
		Core.getCore().getDbManager().storeAndCommit(report);
		return true;
	}

}
