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
 * (c) 2016-2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.ImportModeDialog;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

@SuppressWarnings("restriction")
public class ImportAnalogCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText(Messages.ImportAnalogAction_import_analog);
		UiActivator activator = UiActivator.getDefault();
		dialog.setFilterPath(activator.getFileLocation());
		dialog.setFilterIndex(activator.getFilterIndex());
		String[] supportedImageFileExtensions = ImageConstants.getSupportedImageFileExtensionsGroups(false);
		StringBuilder sb = new StringBuilder();
		int l = supportedImageFileExtensions.length;
		String[] allImageFileExtensions = new String[l + 1];
		for (int i = 1; i < l; i++) {
			allImageFileExtensions[i] = supportedImageFileExtensions[i - 1];
			allImageFileExtensions[i] += ";" //$NON-NLS-1$
					+ supportedImageFileExtensions[i - 1].toUpperCase();
			if (sb.length() > 0)
				sb.append(';');
			sb.append(allImageFileExtensions[i]);
		}
		allImageFileExtensions[0] = sb.toString();
		dialog.setFilterExtensions(allImageFileExtensions);
		String[] supportedImageFileNames = ImageConstants.getSupportedImageFileNames(false);
		String[] allImageFileNames = new String[l + 1];
		System.arraycopy(supportedImageFileNames, 0, allImageFileNames, 1, l);
		allImageFileNames[0] = Messages.ImportFileAction_all_supported_files;
		dialog.setFilterNames(allImageFileNames);
		String file = dialog.open();
		if (file != null) {
			String filterPath = dialog.getFilterPath();
			activator.setFileLocation(filterPath);
			activator.setFilterIndex(dialog.getFilterIndex());
			String[] fileNames = dialog.getFileNames();
			if (fileNames.length > 0) {
				try {
					List<File> digitalFiles = extractDigitalFiles(filterPath, fileNames);
					if (!digitalFiles.isEmpty()) {
						sb = new StringBuilder();
						sb.append(Messages.ImportAnalogCommand_digital_files);
						int i = 0;
						for (File f : digitalFiles) {
							if (++i > 3 && digitalFiles.size() > 4) {
								sb.append('\n')
										.append(NLS.bind(Messages.ImportAnalogCommand_n_more, digitalFiles.size() - 3));
								break;
							}
							sb.append('\n').append(f);
						}
						if (!AcousticMessageDialog.openQuestion(getShell(),
								Messages.ImportAnalogCommand_digital_detected, sb.toString()))
							return;
					}
					List<File> files = new ArrayList<File>(fileNames.length);
					for (String fileName : fileNames)
						files.add(new File(filterPath, fileName));
					File parent = files.get(0).getParentFile();
					ICore core = Core.getCore();
					File catRootFile = core.getVolumeManager().getRootFile(core.getDbManager().getFile());
					boolean foreignFolders = catRootFile == null
							|| !catRootFile.equals(core.getVolumeManager().getRootFile(parent));
					ImportModeDialog imDialog = new ImportModeDialog(getShell(), foreignFolders);
					if (imDialog.open() == ImportModeDialog.OK) {
						ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(files.toArray(new File[files.size()]),
								null, false, false, imDialog.isNewStructure(), null, true);
						WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
						wizard.init(null, null);
						wizardDialog.open();
					}
				} catch (InvocationTargetException | InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	private List<File> extractDigitalFiles(String filterPath, String[] fileNames)
			throws InvocationTargetException, InterruptedException {
		List<File> digitalFiles = new ArrayList<>(fileNames.length);
		final ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
		dialog.create();
		dialog.getShell().setText(Constants.APPLICATION_NAME + " - " + Messages.ImportAnalogCommand_analog_import); //$NON-NLS-1$
		dialog.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ExifTool exifTool = new ExifTool(null, false);
				exifTool.setFast(2);
				monitor.beginTask(Messages.ImportAnalogCommand_checking_files, fileNames.length);
				try {
					for (String fileName : fileNames) {
						File f = new File(filterPath, fileName);
						exifTool.reset(f);
						Map<String, String> metadata = exifTool.getMetadata();
						if (metadata.containsKey(QueryField.EXIF_MAKE.getExifToolKey())
								|| metadata.containsKey(QueryField.EXIF_FOCALLENGTH.getExifToolKey()))
							digitalFiles.add(f);
						monitor.worked(1);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
				} finally {
					exifTool.dispose();
				}
			}
		});
		return digitalFiles;
	}

}
