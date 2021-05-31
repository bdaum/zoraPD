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
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.csv.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.csv.internal.CsvActivator;
import com.bdaum.zoom.csv.internal.Icons;
import com.bdaum.zoom.csv.internal.operations.ExportCsvOperation;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.ExportFieldViewerFilter;
import com.bdaum.zoom.ui.internal.wizards.AbstractAssetSelectionWizard;
import com.bdaum.zoom.ui.internal.wizards.MetaSelectionPage;

@SuppressWarnings("restriction")
public class CsvExportWizard extends AbstractAssetSelectionWizard implements IExportWizard, IPageChangedListener {

	private static final String CSVSETTINGSID = "com.bdaum.zoom.csvProperties"; //$NON-NLS-1$
	private CsvTargetFilePage filePage;
	private MetaSelectionPage metaPage;
	private RelabelPage relabelPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(CsvActivator.getDefault(), CSVSETTINGSID);
		int size = assets.size();
		String msg = (size == 0) ? Messages.CsvExportWizard_nothing_selected
				: (size == 1) ? Messages.CsvExportWizard_one_image : NLS.bind(Messages.CsvExportWizard_n_images, size);
		setWindowTitle(msg);
		IWizardContainer container = getContainer();
		if (container instanceof IPageChangeProvider)
			((IPageChangeProvider) container).addPageChangedListener(this);
	}

	@Override
	public void addPages() {
		super.addPages();
		ImageDescriptor imageDescriptor = Icons.csv64.getDescriptor();
		filePage = new CsvTargetFilePage(assets);
		filePage.setImageDescriptor(imageDescriptor);
		addPage(filePage);
		metaPage = new MetaSelectionPage(
				new QueryField[] { QueryField.IMAGE_ALL, QueryField.EXIF_ALL, QueryField.IPTC_ALL }, false,
				ExportFieldViewerFilter.INSTANCE, false);
		metaPage.setImageDescriptor(imageDescriptor);
		addPage(metaPage);
		relabelPage = new RelabelPage();
		relabelPage.setImageDescriptor(imageDescriptor);
		addPage(relabelPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (assets.isEmpty())
			return null;
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		if (assets.isEmpty())
			return false;
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		if (filePage.finish()) {
			saveDialogSettings();
			OperationJob.executeOperation(
					new ExportCsvOperation(assets, getFilter(), getRelabelMap(), getTargetFile(), getFirstLine()),
					this);
			return true;
		}
		return false;
	}

	public Map<String, String> getRelabelMap() {
		return relabelPage.getRelabelMap();
	}

	public boolean getFirstLine() {
		return filePage.getFirstLine();
	}

	public Set<QueryField> getFilter() {
		return metaPage.getFilter();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.email.IPdfWizard#getTargetFile()
	 */
	public File getTargetFile() {
		String targetFile = filePage.getTargetFile();
		File file = new File(targetFile);
		file.delete();
		try {
			file.createNewFile();
			return file;
		} catch (IOException e) {
			CsvActivator.getDefault().logError(NLS.bind(Messages.CsvExportWizard_cannot_create_file, targetFile), e);
			return null;
		}
	}

	@Override
	public void pageChanged(PageChangedEvent event) {
		Object selectedPage = event.getSelectedPage();
		if (selectedPage instanceof RelabelPage)
			((RelabelPage) selectedPage).fillViewer();
	}

}
