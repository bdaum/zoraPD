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

package com.bdaum.zoom.ui.internal.wizards;

import java.util.Set;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.MergeCatOperation;
import com.bdaum.zoom.ui.internal.ExportFieldViewerFilter;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class MergeCatWizard extends ZWizard implements IWorkbenchWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.exportFolderProperties"; //$NON-NLS-1$
	public static final String INCLUDEMETA = "includeMeta"; //$NON-NLS-1$
	public static final String SELECTEDFIELDS = "selectedFields"; //$NON-NLS-1$

	private MergeCatPage mainPage;
	private MetaSelectionPage metaPage;
	private String filename;

	public MergeCatWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), SETTINGSID);
		setWindowTitle(Constants.APPLICATION_NAME);
		if (selection != null) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof String)
				filename = (String) firstElement;
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.merge64.getDescriptor();
		super.addPages();
		mainPage = new MergeCatPage(filename);
		mainPage.setImageDescriptor(imageDescriptor);
		addPage(mainPage);
		metaPage = new MetaSelectionPage(QueryField.ALL, true, ExportFieldViewerFilter.INSTANCE, false);
		metaPage.setImageDescriptor(imageDescriptor);
		addPage(metaPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == mainPage && mainPage.getDuplicatePolicy() != Constants.MERGE)
			return null;
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == mainPage && mainPage.getDuplicatePolicy() == Constants.MERGE)
			return false;
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		IDbManager externalDb = mainPage.getExternalDb();
		if (externalDb.getFile() == null)
			return false;
		int duplicatePolicy = mainPage.getDuplicatePolicy();
		Set<QueryField> filter = (mainPage.getDuplicatePolicy() == Constants.MERGE) ? metaPage.getFilter() : null;
		int options = (mainPage.getDuplicatePolicy() == Constants.MERGE) ? metaPage.getOptions() : Constants.MERGE;
		saveDialogSettings();
		CoreActivator.getDefault().getFileWatchManager().setPaused(true, this.getClass().toString());
		Job.getJobManager().cancel(Constants.FOLDERWATCH);
		Job.getJobManager().cancel(Constants.SYNCPICASA);
		Core.waitOnJobCanceled(Constants.FOLDERWATCH, Constants.SYNCPICASA);
		try {
			OperationJob.executeOperation(new MergeCatOperation(externalDb, duplicatePolicy, filter, options), this,
					true);
		} finally {
			UiActivator.getDefault().postCatInit(false);
			CoreActivator.getDefault().getFileWatchManager().setPaused(false, this.getClass().toString());
		}
		return true;
	}

	@Override
	public boolean performCancel() {
		IDbManager externalDb = mainPage.getExternalDb();
		if (externalDb.getFile() == null)
			return true;
		externalDb.close(CatalogListener.NORMAL);
		return super.performCancel();
	}

	public Set<QueryField> getFilter() {
		return metaPage.getFilter();
	}

}
