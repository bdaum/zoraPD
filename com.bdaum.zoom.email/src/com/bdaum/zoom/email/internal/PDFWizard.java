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

package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.internal.wizards.AbstractAssetSelectionWizard;

@SuppressWarnings("restriction")
public class PDFWizard extends AbstractAssetSelectionWizard implements IExportWizard,
		IExecutableExtension, IPdfWizard {

	private static final String PDFSETTINGSID = "com.bdaum.zoom.pdfProperties"; //$NON-NLS-1$
	private static final String HTMLSETTINGSID = "com.bdaum.zoom.htmlProperties"; //$NON-NLS-1$

	private CreatePDFPage layoutPage;
	private PDFTargetFilePage filePage;
	private String settingsId;
	private String type;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if ("html".equals(data)) { //$NON-NLS-1$
			settingsId = HTMLSETTINGSID;
			type = "HTML"; //$NON-NLS-1$
		} else {
			settingsId = PDFSETTINGSID;
			type = "PDF"; //$NON-NLS-1$
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		CoreActivator coreActivator = CoreActivator.getDefault();
		setDialogSettings(Activator.getDefault(),settingsId);
		setAssets(workbench, selection, false);
		Iterator<Asset> it = assets.iterator();
		while (it.hasNext()) {
			IMediaSupport mediaSupport = coreActivator.getMediaSupport(it.next()
					.getFormat());
			if (mediaSupport != null
					&& !mediaSupport.testProperty(IMediaSupport.PDF))
				it.remove();
		}
		int size = assets.size();
		String msg = (assets.isEmpty()) ? Messages.PDFWizard_nothing_selected
				: (size == 1) ? NLS.bind(Messages.PDFWizard_create_from_one,
						type) : NLS.bind(Messages.PDFWizard_create_from_n,
						type, size);
		setWindowTitle(msg);
	}

	@Override
	public void addPages() {
		super.addPages();
		ImageDescriptor imageDescriptor = Icons.pdf64.getDescriptor();
		filePage = new PDFTargetFilePage(type, assets);
		filePage.setImageDescriptor(imageDescriptor);
		addPage(filePage);
		layoutPage = new CreatePDFPage(assets, type);
		layoutPage.setImageDescriptor(imageDescriptor);
		addPage(layoutPage);
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
		filePage.finish();
		boolean finish = layoutPage.finish();
		saveDialogSettings();
		return finish;
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
			Activator.getDefault().logError(
					NLS.bind(Messages.PDFWizard_cannot_create_pdf_file,
							targetFile), e);
			return null;
		}
	}

	public FtpAccount getFtpAccount() {
		return filePage.getFtpAccount();
	}

	public String getWeblink() {
		return filePage.getWeblink();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.email.IPdfWizard#getQuality()
	 */
	public int getQuality() {
		return filePage.getQuality();
	}

	public float getImageSize() {
		return layoutPage.getImageSize();
	}

	public UnsharpMask getUnsharpMask() {
		return filePage.getUnsharpMask();
	}

	public int getJpegQuality() {
		return filePage.getJpegQuality();
	}

	public int getMode() {
		return filePage.getMode();
	}

}
