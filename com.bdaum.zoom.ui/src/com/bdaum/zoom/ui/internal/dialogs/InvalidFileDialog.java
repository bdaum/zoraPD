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
package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public class InvalidFileDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.invalidFileDialog"; //$NON-NLS-1$
	private static final String PATH = "path"; //$NON-NLS-1$
	private String title;
	private String message;
	private URI uri;
	private boolean isFile;
	private SimpleDateFormat df = new SimpleDateFormat(
			Messages.InvalidFileDialog_dateformat);
	private IProgressMonitor monitor;

	public InvalidFileDialog(Shell parentShell, String title, String message,
			URI uri, IProgressMonitor monitor) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.uri = uri;
		this.monitor = monitor;
		isFile = Constants.FILESCHEME.equals(uri.getScheme());
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(title);
		setErrorMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		if (isFile) {
			File file = new File(uri);
			new Label(composite, SWT.NONE)
					.setText(Messages.InvalidFileDialog_file_name);
			new Label(composite, SWT.NONE).setText(file.getName());
			new Label(composite, SWT.NONE)
					.setText(Messages.InvalidFileDialog_file_path);
			new Label(composite, SWT.NONE).setText(file.getParent());
			new Label(composite, SWT.NONE)
					.setText(Messages.InvalidFileDialog_file_size);
			new Label(composite, SWT.NONE).setText(Format.sizeFormatter
					.toString(file.length()));
			new Label(composite, SWT.NONE)
					.setText(Messages.InvalidFileDialog_last_mod);
			new Label(composite, SWT.NONE).setText(df.format(new Date(file
					.lastModified())));
		} else {
			new Label(composite, SWT.NONE)
					.setText(Messages.InvalidFileDialog_uri);
			new Label(composite, SWT.NONE).setText(uri.toString());
		}
		return dialogArea;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 2, Messages.InvalidFileDialog_skip, true);
		if (isFile) {
			createButton(parent, 1, Messages.InvalidFileDialog_transfer, false);
			createButton(parent, 0, Messages.InvalidFileDialog_delete, false);
		}
		createButton(parent, 3, Messages.InvalidFileDialog_abort, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case 0:
			File file = new File(uri);
			if (AcousticMessageDialog.openQuestion(
					getShell(),
					Messages.InvalidFileDialog_delete_invalid,
					NLS.bind(Messages.InvalidFileDialog_really_delete,
							file.getName()))) {
				file.delete();
				break;
			}
			return;
		case 1:
			IDialogSettings settings = getDialogSettings(
					UiActivator.getDefault(), SETTINGSID);
			String path = settings.get(PATH);
			file = new File(uri);
			FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
			dialog.setFilterPath(path != null ? path : file.getParent());
			dialog.setFileName(file.getName());
			dialog.setText(Messages.InvalidFileDialog_transfer_target);
			String targetFile = dialog.open();
			if (targetFile != null) {
				File target = new File(targetFile);
				target.delete();
				file.renameTo(target);
				settings.put(PATH, target.getParent());
			}
			break;
		case 3:
			if (AcousticMessageDialog.openQuestion(getShell(), Messages.InvalidFileDialog_abort_import,
					Messages.InvalidFileDialog_really_abort)) {
				monitor.setCanceled(true);
				break;
			}
			return;
		default:
			break;
		}
		okPressed();
	}

}
