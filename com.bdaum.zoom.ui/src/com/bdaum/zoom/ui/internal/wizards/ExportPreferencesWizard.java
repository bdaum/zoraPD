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
 * (c) 2009-2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

public class ExportPreferencesWizard extends ZWizard implements IExportWizard {

	private static final String SETTINGSID = "com.bdaum.zoom.exportPreferencesProperties"; //$NON-NLS-1$
	private PreferenceTargetPage prefPage;

	public ExportPreferencesWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), SETTINGSID);
		setWindowTitle(Messages.ExportPreferencesWizard_export_preferences);
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.pref64.getDescriptor();
		addPage(prefPage = new PreferenceTargetPage());
		prefPage.setImageDescriptor(imageDescriptor);
	}

	@Override
	public boolean performFinish() {
		if (prefPage.finish()) {
			saveDialogSettings();
			BatchUtilities.exportPreferences(new File(prefPage.getPath()), prefPage.getFilter());
			return true;
		}
		return false;
	}

}
