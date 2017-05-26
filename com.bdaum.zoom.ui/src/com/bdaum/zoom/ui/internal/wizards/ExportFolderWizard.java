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

package com.bdaum.zoom.ui.internal.wizards;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.ExportXmpViewerFilter;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

public class ExportFolderWizard extends AbstractAssetSelectionWizard implements
		IExportWizard {

	protected static final double COMPRESSION = 25d;
	private static final String SETTINGSID = "com.bdaum.zoom.exportFolderProperties"; //$NON-NLS-1$
	public static final String INCLUDEMETA = "includeMeta"; //$NON-NLS-1$
	public static final String SELECTEDFIELDS = "selectedFields"; //$NON-NLS-1$
	public static final String FOLDER = "folder"; //$NON-NLS-1$

	private ExportFolderPage mainPage;
	private MetaSelectionPage metaPage;

	public ExportFolderWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setDialogSettings(UiActivator.getDefault(),SETTINGSID);
		setAssets(workbench, selection, false);
		int size = assets.size();
		setWindowTitle(assets.isEmpty() ? Messages.ExportFolderWizard_nothing_selected
				: size == 1 ? Messages.ExportFolderWizard_export_one_image
						: NLS.bind(Messages.ExportFolderWizard_export_n_images,
								size));
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.folder64.getDescriptor();
		super.addPages();
		mainPage = new ExportFolderPage(assets);
		mainPage.setImageDescriptor(imageDescriptor);
		addPage(mainPage);
		metaPage = new MetaSelectionPage(new QueryField[] {
				QueryField.EXIF_ALL, QueryField.IPTC_ALL }, false, ExportXmpViewerFilter.INSTANCE, false);
		metaPage.setImageDescriptor(imageDescriptor);
		addPage(metaPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == mainPage && !mainPage.getIncludeMeta())
			return null;
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		if (assets.isEmpty())
			return false;
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == mainPage && mainPage.getIncludeMeta())
			return false;
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		boolean finish = mainPage.finish();
		saveDialogSettings();
		return finish;
	}

	public Set<QueryField> getFilter() {
		return metaPage.getFilter();
	}

}
