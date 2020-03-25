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
package com.bdaum.zoom.gps.internal.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.gps.internal.GpsActivator;

public class GeotagAction extends GeonameAction {

	public GeotagAction() {
		setImageDescriptor(
				GpsActivator.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID, "/icons/gps.gif")); //$NON-NLS-1$
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(true);
	}

	@Override
	public void run() {
		final Shell shell = adaptable.getAdapter(Shell.class);
		FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		dialog.setText(Messages.getString("GeotagAction.Select")); //$NON-NLS-1$
		GpsActivator activator = GpsActivator.getDefault();
		dialog.setFilterPath(activator.getFileLocation());
		dialog.setFilterIndex(activator.getFilterIndex());
		String[] supportedGpsFileExtensions = activator.getSupportedGpsFileExtensions();
		for (int i = 0; i < supportedGpsFileExtensions.length - 1; i++)
			supportedGpsFileExtensions[i] += ";" //$NON-NLS-1$
					+ supportedGpsFileExtensions[i].toUpperCase();
		dialog.setFilterExtensions(supportedGpsFileExtensions);
		dialog.setFilterNames(activator.getSupportedGpsFileNames());
		String file = dialog.open();
		if (file != null) {
			String filterPath = dialog.getFilterPath();
			activator.setFileLocation(filterPath);
			activator.setFilterIndex(dialog.getFilterIndex());
			String[] fileNames = dialog.getFileNames();
			List<File> files = new ArrayList<File>(fileNames.length);
			for (String fileName : fileNames)
				files.add(new File(filterPath, fileName));
			run(files.toArray(new File[files.size()]), adaptable);
		}

	}

}
