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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.cat.model.report.ReportImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.report.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.wizards.ZWizard;

@SuppressWarnings("restriction")
public class ReportWizard extends ZWizard implements IWorkbenchWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.reportProperties"; //$NON-NLS-1$
	public static final int NAME = 1;
	public static final int IMAGECOUNT = 1 << 1;
	public static final int SALES = 1 << 2;
	public static final int EARNINGS = 1 << 3;
	public static final int ALLVALUES = 255;
	public static final int DAYTIME = 1 << 8;
	public static final int TIME = 1 << 9;
	public static final int NUMERIC = 1 << 10;
	public static final int DISCRETE = 1 << 11;
	public static final int ALLMODES = 255 << 8;
	public static final int BAR = 1 << 16;
	public static final int LINE = 1 << 17;
	public static final int PIE = 1 << 18;
	public static final int AREA = 1 << 19;
	public static final int STEP = 1 << 20;
	public static final int ALLTYPES = 255 << 16;
	public static final int THREEDIM = 1 << 24;
	public static final int CYLINDER = 1 << 25;
	public static final int CUMULATE = 1 << 26;
	public static final int ALLOPTIONS = 255 << 24;
	public static final int T_DAY = 1;
	public static final int T_WEEK = 2;
	public static final int T_MONTH = 3;
	public static final int T_QUARTER = 4;
	public static final int T_YEAR = 5;

	private SourcePage sourcePage;
	private ModePage modePage;
	private ValuePage valuePage;
	private PresentationPage presentationPage;
	private ReportPage reportPage;
	private Report report;

	public ReportWizard() {
		setHelpAvailable(true);
	}

	public static boolean isMultiple(int mode) {
		int i = 0;
		if ((mode & ReportWizard.IMAGECOUNT) != 0)
			++i;
		if ((mode & ReportWizard.SALES) != 0)
			++i;
		if ((mode & ReportWizard.EARNINGS) != 0)
			++i;
		return i > 1;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), SETTINGSID);
		setWindowTitle(Constants.APPLICATION_NAME);
		SimpleDateFormat sdf = new SimpleDateFormat(Messages.ReportWizard_yyyyMMddhhmmaa);
		String title = NLS.bind(Messages.ReportWizard_report_from_x, sdf.format(new Date()));
		IDbManager dbManager = Core.getCore().getDbManager();
		String description = dbManager.getMeta(true).getDescription();
		if (description == null || description.isEmpty())
			description = dbManager.getFile().getName();
		String descr = NLS.bind(Messages.ReportWizard_based_on, description);
		report = new ReportImpl(title, descr, null, TIME | BAR | IMAGECOUNT, -1, false, null, 0L, 0L, 0L, 0L, 24, 0, 50,
				1f, null, false);
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.report64.getDescriptor();
		sourcePage = new SourcePage("source", Messages.ReportWizard_source_sel, //$NON-NLS-1$
				Messages.ReportWizard_source_sel_msg,
				imageDescriptor);
		sourcePage.setImageDescriptor(imageDescriptor);
		addPage(sourcePage);
		modePage = new ModePage("mode", Messages.ReportWizard_mode_sel, Messages.ReportWizard_mode_sel_msg, imageDescriptor); //$NON-NLS-1$
		modePage.setImageDescriptor(imageDescriptor);
		addPage(modePage);
		valuePage = new ValuePage("value",  Messages.ReportWizard_value_sel, Messages.ReportWizard_value_sel_msg,imageDescriptor); //$NON-NLS-1$
		valuePage.setImageDescriptor(imageDescriptor);
		addPage(valuePage);
		presentationPage = new PresentationPage("presentation", Messages.ReportWizard_presentation_sel, Messages.ReportWizard_presentation_sel_msg, imageDescriptor); //$NON-NLS-1$
		presentationPage.setImageDescriptor(imageDescriptor);
		addPage(presentationPage);
		reportPage = new ReportPage("report",Messages.ReportWizard_view_report, //$NON-NLS-1$
				Messages.ReportWizard_view_report_msg,
				imageDescriptor);
		addPage(reportPage);
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == reportPage;
	}

	@Override
	public boolean performFinish() {
		return reportPage.finish();
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public Report getReport() {
		return report;
	}

}
