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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.AnalogPropertiesDialog;

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
			int filterIndex = dialog.getFilterIndex();
			activator.setFilterIndex(filterIndex);
			String[] fileNames = dialog.getFileNames();
			if (fileNames.length > 0) {
				AnalogPropertiesDialog aDialog = new AnalogPropertiesDialog(getShell());
				if (aDialog.open() == Window.OK) {
					AnalogProperties properties = aDialog.getResult();
					List<File> files = new ArrayList<File>(fileNames.length);
					for (String fileName : fileNames)
						files.add(new File(filterPath, fileName));
					OperationJob.executeOperation(new ImportOperation(new FileInput(files, false),
							activator.createImportConfiguration(this), properties, null), this);
				}
			}
		}
	}

}
