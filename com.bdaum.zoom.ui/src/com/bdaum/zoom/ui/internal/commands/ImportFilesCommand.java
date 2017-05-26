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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;

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
				OperationJob.executeOperation(new ImportOperation(new FileInput(files, false),
						activator.createImportConfiguration(this), null, null), this);
			}
		}
	}

}
