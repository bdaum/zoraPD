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
