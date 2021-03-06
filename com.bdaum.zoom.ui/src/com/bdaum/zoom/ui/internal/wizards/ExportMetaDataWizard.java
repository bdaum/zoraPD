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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ExportMetadataOperation;
import com.bdaum.zoom.ui.internal.ExportXmpViewerFilter;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class ExportMetaDataWizard extends AbstractAssetSelectionWizard implements IExportWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.exportMetaProperties"; //$NON-NLS-1$

	private MetaSelectionPage metaPage;

	public ExportMetaDataWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), SETTINGSID);
		int size = assets.size();
		setWindowTitle(size == 0 ? Messages.ExportMetaDataWizard_noting_selected
				: size == 1 ? Messages.ExportMetaDataWizard_export_one_image
						: NLS.bind(Messages.ExportMetaDataWizard_export_n_images, size));
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		super.addPages();
		metaPage = new MetaSelectionPage(
				new QueryField[] { QueryField.IMAGE_ALL, QueryField.EXIF_ALL, QueryField.IPTC_ALL }, false,
				ExportXmpViewerFilter.INSTANCE, true);
		metaPage.setImageDescriptor(Icons.meta64.getDescriptor());
		addPage(metaPage);
	}

	@Override
	public boolean canFinish() {
		if (assets.isEmpty())
			return false;
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		saveDialogSettings();
		OperationJob.executeOperation(
				new ExportMetadataOperation(assets, getFilter(), metaPage.isJpegSet(), false, true), this);
		return true;
	}

	public Set<QueryField> getFilter() {
		return metaPage.getFilter();
	}

}
