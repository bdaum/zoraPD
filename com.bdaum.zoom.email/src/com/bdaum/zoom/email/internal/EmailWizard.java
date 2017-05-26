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

package com.bdaum.zoom.email.internal;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.ExportFieldViewerFilter;
import com.bdaum.zoom.ui.internal.wizards.AbstractAssetSelectionWizard;
import com.bdaum.zoom.ui.internal.wizards.MetaSelectionPage;

@SuppressWarnings("restriction")
public class EmailWizard extends AbstractAssetSelectionWizard implements IExportWizard {

	public static final String MODE = "mode"; //$NON-NLS-1$
//	public static final int ORIGINALS = 0;
//	public static final int PREVIEW = 1;
//	public static final int FIXED = 2;
	public static final String SCALING = "scaling"; //$NON-NLS-1$
	protected static final double COMPRESSION = 25d;
	private static final String SETTINGSID = "com.bdaum.zoom.emailProperties"; //$NON-NLS-1$
	public static final String FIXEDSIZE = "fixedSize"; //$NON-NLS-1$
	public static final String INCLUDEMETA = "includeMeta"; //$NON-NLS-1$
	public static final String TRACKEXPORTS = "trackExports"; //$NON-NLS-1$

	private SendEmailPage mainPage;
	private MetaSelectionPage metaPage;


	public EmailWizard() {
		super();
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setDialogSettings(Activator.getDefault(),SETTINGSID);
		setAssets(workbench, selection, false);
		int size = assets.size();
		setWindowTitle(assets.isEmpty() ? Messages.EmailWizard_No_image_selected
				: size == 1 ? Messages.EmailWizard_email_one_image : NLS.bind(
						Messages.EmailWizard_Email_n_images, size));
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		super.addPages();
		ImageDescriptor imageDescriptor = Icons.email64.getDescriptor();
		mainPage = new SendEmailPage(assets, false);
		mainPage.setImageDescriptor(imageDescriptor);
		addPage(mainPage);
		if (!assets.isEmpty()) {
			metaPage = new MetaSelectionPage(new QueryField[] {
					QueryField.EXIF_ALL, QueryField.IPTC_ALL }, false, ExportFieldViewerFilter.INSTANCE, false);
			metaPage.setImageDescriptor(imageDescriptor);
			addPage(metaPage);
		}
	}


	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (assets.isEmpty())
			return null;
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
		return metaPage == null ? null : metaPage.getFilter();
	}

}
