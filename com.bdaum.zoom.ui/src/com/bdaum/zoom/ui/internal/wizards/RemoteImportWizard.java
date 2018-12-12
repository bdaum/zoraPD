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

import java.net.URI;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IFTPService;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class RemoteImportWizard extends ZWizard implements IWorkbenchWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.remoteImportProperties"; //$NON-NLS-1$

	private RemoteImportPage mainPage;
	private FtpDirPage ftpPage;

	private UrlPreviewPage urlPage;

	public RemoteImportWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), SETTINGSID);
		setWindowTitle(Constants.APPLICATION_NAME);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.merge64.getDescriptor();
		super.addPages();
		mainPage = new RemoteImportPage();
		mainPage.setImageDescriptor(imageDescriptor);
		addPage(mainPage);
		ftpPage = new FtpDirPage();
		ftpPage.setImageDescriptor(imageDescriptor);
		addPage(ftpPage);
		urlPage = new UrlPreviewPage();
		urlPage.setImageDescriptor(imageDescriptor);
		addPage(urlPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == mainPage) {
			URL url = mainPage.getUrl();
			if (url == null)
				return null;
			if (IFTPService.FTPSCHEME.equals(url.getProtocol()))
				return ftpPage;
			return urlPage;
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == mainPage) {
			URL url = mainPage.getUrl();
			if (url == null)
				return false;
			if (IFTPService.FTPSCHEME.equals(url.getProtocol()))
				return false;
		}
		return true;
	}

	@Override
	public boolean performFinish() {
		URL url = mainPage.getUrl();
		URI[] uris;
		if (IFTPService.FTPSCHEME.equals(url.getProtocol()))
			uris = ftpPage.getURIs();
		else
			uris = urlPage.getURIs();
		if (uris != null) {
			mainPage.saveSettings();
			saveDialogSettings();
			ImportConfiguration config = UiActivator.getDefault()
					.createImportConfiguration(this);
			config.rawOptions = Constants.RAWIMPORT_ONLYRAW;
			OperationJob.executeOperation(new ImportOperation(
					Messages.RemoteImportWizard_import_remote_images, uris,
					config), this, true);
		}
		return true;
	}

	public URL getUrl() {
		return mainPage.getUrl();
	}

}
