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
 * (c) 2009-2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.RenamingTemplate;
import com.bdaum.zoom.ui.internal.widgets.IFileProvider;
import com.bdaum.zoom.ui.internal.widgets.RenameGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportRenamingPage extends ColoredWizardPage implements IFileProvider, Listener {

	private final boolean media;
	private RenameGroup renameGroup;

	public ImportRenamingPage(String pageName, boolean media) {
		super(pageName);
		this.media = media;
	}

	@Override
	public void createControl(final Composite parent) {
		renameGroup = new RenameGroup(parent, SWT.NONE, this, false,
				new RenamingTemplate[] {
						new RenamingTemplate(Messages.ImportRenamingPage_date_filename, "_" + Constants.TV_YYYY //$NON-NLS-1$
								+ Constants.TV_MM + Constants.TV_DD + "-" //$NON-NLS-1$
								+ Constants.TV_FILENAME, true),
						new RenamingTemplate(Messages.ImportRenamingPage_user_year_seq,
								Constants.TV_USER + Constants.TV_YYYY + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.ImportRenamingPage_cue_year_seq,
								Constants.TV_CUE + "_" + Constants.TV_YYYY + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$ //$NON-NLS-2$
						new RenamingTemplate(Messages.ImportRenamingPage_filename_seq,
								Constants.TV_FILENAME + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.ImportRenamingPage_orig_filename, Constants.TV_FILENAME, true) },
				Constants.TV_ALL);
		renameGroup.addListener(SWT.Modify, this);
		setControl(renameGroup);
		setHelp(media ? HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_RENAMING
				: HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_RENAMING);
		setTitle(Messages.ImportRenamingPage_image_file_renaming);
		setMessage(Messages.ImportRenamingPage_select_a_template);
		super.createControl(parent);
		fillValues();
	}
	
	@Override
	public void handleEvent(Event event) {
		validatePage();
	}

	private void fillValues() {
		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();
		renameGroup.fillValues(dialogSettings, null, null);
		updateValues(wizard.getCurrentDevice());
	}
	
	public void updateValues(LastDeviceImport current) {
		if (current != null && renameGroup != null)
			renameGroup.updateValues(current.getSelectedTemplate(),  current.getCue());
	}

	@Override
	protected void validatePage() {
		String errorMsg = renameGroup.validate();
		setPageComplete(errorMsg == null);
		setErrorMessage(errorMsg);
	}

	public void performFinish(ImportFromDeviceData importData) {
		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();
		LastDeviceImport newDevice = wizard.getNewDevice();
		renameGroup.saveSettings(dialogSettings);
		RenamingTemplate selectedTemplate = renameGroup.getSelectedTemplate();
		if (selectedTemplate != null) {
			importData.setRenamingTemplate(selectedTemplate.getContent());
			newDevice.setSelectedTemplate(selectedTemplate.getLabel());
			String cue = renameGroup.getCue();
			importData.setCue(cue);
			newDevice.setCue(cue);
		}
	}

	@Override
	public StorageObject getFile() {
		return ((ImportFromDeviceWizard) getWizard()).getFirstSelectedFile();
	}

	public void update() {
		renameGroup.update();
	}


}
