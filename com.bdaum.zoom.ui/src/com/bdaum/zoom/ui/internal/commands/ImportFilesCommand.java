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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.ImportModeDialog;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

@SuppressWarnings("restriction")
public class ImportFilesCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText(Messages.ImportFileAction_Import_file);
		UiActivator activator = UiActivator.getDefault();
		dialog.setFilterPath(activator.getFileLocation());
		dialog.setFilterIndex(activator.getFilterIndex());
		String[] supportedImageFileExtensions = ImageConstants.getSupportedImageFileExtensionsGroups(true);
		StringBuilder sb = new StringBuilder();
		IMediaSupport[] mediaSupport = CoreActivator.getDefault().getMediaSupport();
		int l = supportedImageFileExtensions.length;
		String[] allImageFileExtensions = new String[l + 2 + mediaSupport.length];
		for (int i = 0; i < l; i++) {
			allImageFileExtensions[i + 2] = supportedImageFileExtensions[i];
			allImageFileExtensions[i + 2] += ";" //$NON-NLS-1$
					+ supportedImageFileExtensions[i].toUpperCase();
			if (sb.length() > 0)
				sb.append(';');
			sb.append(allImageFileExtensions[i + 2]);
		}
		allImageFileExtensions[1] = sb.toString();
		for (int i = 0; i < mediaSupport.length; i++) {
			StringBuilder msb = new StringBuilder();
			for (String ext : mediaSupport[i].getFileExtensions()) {
				if (msb.length() > 0)
					msb.append(';');
				String uext = ext.toUpperCase();
				msb.append("*.").append(ext).append(';').append("*.").append(uext); //$NON-NLS-1$ //$NON-NLS-2$
				if (sb.length() > 0)
					sb.append(';');
				sb.append("*.").append(ext).append(';').append("*.").append(uext); //$NON-NLS-1$ //$NON-NLS-2$
			}
			allImageFileExtensions[l + 2 + i] = msb.toString();
		}
		allImageFileExtensions[0] = sb.toString();
		dialog.setFilterExtensions(allImageFileExtensions);

		String[] supportedImageFileNames = ImageConstants.getSupportedImageFileNames(true);
		String[] allImageFileNames = new String[l + 2 + mediaSupport.length];
		System.arraycopy(supportedImageFileNames, 0, allImageFileNames, 2, l);
		allImageFileNames[0] = Messages.ImportFileAction_supported_file_types;
		allImageFileNames[1] = Messages.ImportFileAction_all_supported_files;
		for (int i = 0; i < mediaSupport.length; i++) {
			IMediaSupport ms = mediaSupport[i];
			StringBuilder msb = new StringBuilder();
			msb.append(ms.getName()).append(" ("); //$NON-NLS-1$
			for (String ext : ms.getFileExtensions()) {
				if (msb.length() > 2)
					msb.append(", "); //$NON-NLS-1$
				msb.append("*.").append(ext); //$NON-NLS-1$
			}
			msb.append(')');
			allImageFileNames[l + 2 + i] = msb.toString();
		}
		dialog.setFilterNames(allImageFileNames);
		String file = dialog.open();
		if (file != null) {
			String filterPath = dialog.getFilterPath();
			activator.setFileLocation(filterPath);
			int filterIndex = dialog.getFilterIndex();
			activator.setFilterIndex(filterIndex);
			String[] fileNames = dialog.getFileNames();
			if (fileNames.length > 0) {
				List<File> files = new ArrayList<File>(fileNames.length);
				for (String fileName : fileNames)
					files.add(new File(filterPath, fileName));
				File parent = files.get(0).getParentFile();
				ICore core = Core.getCore();
				File catRootFile = core.getVolumeManager().getRootFile(core.getDbManager().getFile());
				boolean foreignFolders = catRootFile == null
						|| !catRootFile.equals(core.getVolumeManager().getRootFile(parent));
				boolean device = false;
				while (parent != null) {
					String name = parent.getName();
					if ("DCIM".equals(name)) { //$NON-NLS-1$
						device = true;
						break;
					}
					parent = parent.getParentFile();
				}
				ImportModeDialog imDialog = new ImportModeDialog(getShell(), device || foreignFolders);
				if (imDialog.open() == ImportModeDialog.OK) {
					ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(StorageObject.fromFile(files),
							null, device && foreignFolders, false, imDialog.isNewStructure(), null, false);
					WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
					wizard.init(null, null);
					wizardDialog.open();
				}
			}
		}
	}

}
