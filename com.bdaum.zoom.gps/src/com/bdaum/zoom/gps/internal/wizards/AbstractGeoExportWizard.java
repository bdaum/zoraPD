/*******************************************************************************
 * Copyright (c) 2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.gps.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.Icons;
import com.bdaum.zoom.gps.internal.operations.ExportGeoOperation;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.wizards.AbstractAssetSelectionWizard;

@SuppressWarnings("restriction")
public abstract class AbstractGeoExportWizard extends AbstractAssetSelectionWizard
		implements IExportWizard, IAdaptable {

	private static final String SETTINGSID = "com.bdaum.zoom.[0}export"; //$NON-NLS-1$

	public static final String KML = "kml"; //$NON-NLS-1$
	public static final String GPX = "gpx"; //$NON-NLS-1$
	private ExportPage exportPage;
	private String type;

	public AbstractGeoExportWizard(String type) {
		this.type = type;
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(GpsActivator.getDefault(), NLS.bind(SETTINGSID, type));
		for (Iterator<Asset> it = assets.iterator(); it.hasNext();) {
			Asset asset = it.next();
			if (Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLongitude()))
				it.remove();
		}
		int size = assets.size();
		setWindowTitle(size == 0 ? Messages.AbstractGeoExportWizard_no_media
				: size == 1 ? NLS.bind(Messages.AbstractGeoExportWizard_one_image, type.toUpperCase())
						: NLS.bind(Messages.AbstractGeoExportWizard_multiple_images, type.toUpperCase(), size));
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		return assets.isEmpty() ? null : super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		return !assets.isEmpty() && super.canFinish();
	}

	@Override
	public boolean performFinish() {
		if (exportPage.finish()) {
			saveDialogSettings();
			OperationJob.executeOperation(new ExportGeoOperation(assets, getTargetFile(), type), this);
			return true;
		}
		return false;
	}

	public File getTargetFile() {
		String targetFile = exportPage.getTargetFile();
		File file = new File(targetFile);
		file.delete();
		try {
			file.createNewFile();
			return file;
		} catch (IOException e) {
			GpsActivator.getDefault().logError(
					NLS.bind(Messages.AbstractGeoExportWizard_cannot_create, type.toUpperCase(), targetFile), e);
			return null;
		}
	}

	@Override
	public void addPages() {
		exportPage = new ExportPage(assets, type);
		addPage(exportPage);
		exportPage.setImageDescriptor((type == KML ? Icons.kml64 : Icons.gpx64).getDescriptor());
	}

}
