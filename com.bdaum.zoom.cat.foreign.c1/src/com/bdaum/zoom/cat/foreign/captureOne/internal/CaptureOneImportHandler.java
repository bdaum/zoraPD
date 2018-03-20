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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.cat.foreign.captureOne.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bdaum.zoom.cat.foreign.internal.ImportForeignCatOperation;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class CaptureOneImportHandler extends AbstractHandler implements
		IAdaptable {

	private Shell shell;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null)
			shell = activeWorkbenchWindow.getShell();
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText(Messages.CaptureOneImportHandler_import);
		dialog.setFilterExtensions(new String[] { "*.col;*.col45;*.col50" }); //$NON-NLS-1$
		dialog.setFilterNames(new String[] { Messages.CaptureOneImportHandler_cat_extensions });
		String filename = dialog.open();
		if (filename != null)
			OperationJob.executeOperation(new ImportForeignCatOperation(filename, new C1CatHandler(),
					UiActivator.getDefault().createImportConfiguration(this)), this);
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return shell;
		return null;
	}
}
