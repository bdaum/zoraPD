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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.RetargetAction;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.IPreferenceUpdater;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.PreferencesUpdater;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class PasteAction extends RetargetAction implements IAdaptable {
	private IWorkbenchWindow window;
	private Clipboard clipboard;
	private Listener focusListener;

	public PasteAction(IWorkbenchWindow window, Clipboard clipboard, String id,
			String text) {
		super(id, text);
		setActionHandler(this);
		this.window = window;
		this.clipboard = clipboard;
		Display display = window.getShell().getDisplay();
		focusListener = new Listener() {
			public void handleEvent(Event event) {
				updateEnablement();
			}
		};
		display.addFilter(SWT.FocusIn, focusListener);
	}

	@Override
	public void dispose() {
		Shell shell = window.getShell();
		if (!shell.isDisposed())
			shell.getDisplay().removeFilter(SWT.FocusIn, focusListener);
		super.dispose();
	}

	protected void updateEnablement() {
		boolean enabled = false;
		if (!Core.getCore().getDbManager().isReadOnly()) {
			Object contents = clipboard.getContents(FileTransfer.getInstance());
			enabled = contents instanceof String[] && ((String[]) contents).length > 0;
		}
		setEnabled(enabled);
	}

	@Override
	public void runWithEvent(Event event) {
		run();
	}

	@Override
	public void run() {
		UiActivator activator = UiActivator.getDefault();
		try {
			Object contents = clipboard.getContents(FileTransfer.getInstance());
			if (contents instanceof String[]) {
				String[] fileNames = (String[]) contents;
				if (fileNames.length > 0) {
					List<File> images = new ArrayList<File>(fileNames.length);
					List<File> folders = new ArrayList<File>(fileNames.length);
					Utilities.collectImages(fileNames, images);
					Utilities.collectFolders(fileNames, folders);
					if (!images.isEmpty())
						OperationJob.executeOperation(
								new ImportOperation(
										new FileInput(images, false), activator
												.createImportConfiguration(this),
										null, folders.toArray(new File[folders
												.size()])), this);
				}
			}
		} catch (Exception e) {
			activator.logError(Messages.PasteAction_Error_pasting, e);
		}
		super.run();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Shell.class))
			return window.getShell();
		if (adapter.equals(IPreferenceUpdater.class))
			return new PreferencesUpdater();
		return null;
	}

}
