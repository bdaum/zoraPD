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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.internal.wizards.AbstractAssetSelectionWizard;

@SuppressWarnings("restriction")
public class EmailPDFWizard extends AbstractAssetSelectionWizard implements IExportWizard, IPdfWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.emailPdfProperties"; //$NON-NLS-1$
	private static int count = 0;
	private CreatePDFPage layoutPage;
	private SendEmailPage mainPage;
	private File pdfFile;

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (assets.isEmpty())
			return null;
		IWizardPage nextPage = super.getNextPage(page);
		return nextPage;
	}

	@Override
	public boolean canFinish() {
		if (assets.isEmpty())
			return false;
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		boolean finish = layoutPage.finish();
		saveDialogSettings();
		return finish;
	}

	public EmailPDFWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		CoreActivator coreActivator = CoreActivator.getDefault();
		setDialogSettings(Activator.getDefault(),SETTINGSID);
		setAssets(workbench, selection, false);
		Iterator<Asset> it = assets.iterator();
		while (it.hasNext()) {
			IMediaSupport mediaSupport = coreActivator.getMediaSupport(it.next().getFormat());
			if (mediaSupport != null && !mediaSupport.testProperty(IMediaSupport.PDF))
					it.remove();
		}
		int size = assets.size();
		setWindowTitle(size == 0 ? Messages.EmailPDFWizard_nothing_selected
				: size == 1 ? Messages.EmailPDFWizard_email_one_image_as_pdf
						: NLS.bind(
								Messages.EmailPDFWizard_email_n_images_as_pdf,
								size));
	}

	@Override
	public void addPages() {
		super.addPages();
		ImageDescriptor imageDescriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						"icons/banner/pdfemail64.png"); //$NON-NLS-1$
		layoutPage = new CreatePDFPage(assets, "PDF"); //$NON-NLS-1$
		layoutPage.setImageDescriptor(imageDescriptor);
		addPage(layoutPage);
		mainPage = new SendEmailPage(assets, true);
		mainPage.setImageDescriptor(imageDescriptor);
		addPage(mainPage);
	}

	public int getQuality() {
		return mainPage.getQuality();
	}

	public UnsharpMask getUnsharpMask() {
		return mainPage.getUnsharpMask();
	}

	public File getTargetFile() {
		if (pdfFile == null) {
			try {
				pdfFile = Activator
						.getDefault()
						.createTempFile(
								Constants.APPNAME + "_pdf" + (++count) + ".pdf", ".pdf"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			} catch (IOException e) {
				Activator.getDefault().logError(
						Messages.EmailPDFWizard_cannot_create_temp_PDF_file, e);
			}
		}
		return pdfFile;
	}

	public String getSubject() {
		return mainPage.getSubject();
	}

	public String getMessage() {
		return mainPage.getMailMessage();
	}

	public float getImageSize() {
		return layoutPage.getImageSize();
	}

	public String getTitle() {
		return layoutPage.getTitle();
	}

	public int getJpegQuality() {
		return mainPage.getJpegQuality();
	}

	public FtpAccount getFtpAccount() {
		return null;
	}

	public int getTarget() {
		return 0;
	}

	public String getWeblink() {
		return null;
	}

	public int getMode() {
		return Constants.FORMAT_JPEG;
	}

}
