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
package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class WatchedFolderSelectionPage extends ColoredWizardPage {

	private Text folderField;
	private final WatchedFolderImpl watchedFolder;
	private CheckboxButton subfolderButton;
	private RadioButtonGroup typeButtonGroup;
	private boolean typeChoice;
	private boolean subfolderChoice;

	public WatchedFolderSelectionPage(String pageName, WatchedFolderImpl watchedFolder, boolean typeChoice,
			boolean subfolderChoice) {
		super(pageName);
		this.watchedFolder = watchedFolder;
		this.typeChoice = typeChoice;
		this.subfolderChoice = subfolderChoice;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite comp = createComposite(parent, 3);
		new Label(comp, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		new Label(comp, SWT.NONE).setText(Messages.WatchedFolderSelectionPage_folder_path);
		folderField = new Text(comp, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_text.widthHint = 200;
		folderField.setLayoutData(gd_text);
		final Button browseButton = WidgetFactory.createPushButton(comp, Messages.ImportFromDeviceWizard_browse,
				SWT.BEGINNING);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setText(Messages.WatchedFolderSelectionPage_watched_folder);
				dialog.setMessage(Messages.WatchedFolderSelectionPage_Select_a_folder);
				String lastTargetDir = folderField.getText();
				dialog.setFilterPath(lastTargetDir.isEmpty() ? null : lastTargetDir);
				String dir = dialog.open();
				if (dir != null) {
					if (!dir.endsWith(File.separator))
						dir += File.separator;
					folderField.setText(dir);
					validatePage();
				}
			}
		});
		subfolderButton = WidgetFactory.createCheckButton(comp, Messages.WatchedFolderSelectionPage_include_subfolders,
				new GridData(SWT.BEGINNING, SWT.FILL, true, false, 3, 1));
		typeButtonGroup = new RadioButtonGroup(comp, Messages.WatchedFolderSelectionPage_folder_type, SWT.HORIZONTAL,
				Messages.WatchedFolderSelectionPage_storage, Messages.WatchedFolderSelectionPage_transfer);
		typeButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		setControl(comp);
		setHelp(HelpContextIds.WATCHED_FOLDER_FILE_SELECTION);
		setTitle(getName());
		String msg = Messages.WatchedFolderSelectionPage_msg;
		if (watchedFolder != null && watchedFolder.getTethered())
			msg += '\n' + Messages.WatchedFolderSelectionPage_dedicated_to_tethered;
		setMessage(msg);
		fillValues();
		super.createControl(parent);
	}

	private void fillValues() {
		try {
			String uri = watchedFolder.getUri();
			if (uri != null)
				folderField.setText(new File(new URI(uri)).getAbsolutePath());
		} catch (URISyntaxException e1) {
			// do nothing
		}
		if (watchedFolder.getTransfer())
			typeButtonGroup.setSelection(1);
		else
			typeButtonGroup.setSelection(0);
		typeButtonGroup.setVisible(typeChoice && !watchedFolder.getTethered());
		subfolderButton.setSelection(watchedFolder.getRecursive());
		subfolderButton.setVisible(subfolderChoice);
	}

	@Override
	protected void validatePage() {
		if (folderField.getText().isEmpty()) {
			setErrorMessage(Messages.WatchedFolderSelectionPage_specify_path);
			setPageComplete(false);
		} else {
			File file = new File(folderField.getText());
			if (!file.exists()) {
				setErrorMessage(Messages.WatchedFolderSelectionPage_folder_does_not_exist);
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
	}

	public void performFinish() {
		File file = new File(folderField.getText());
		String volume = CoreActivator.getDefault().getVolumeManager().getVolumeForFile(file);
		watchedFolder.setUri(file.toURI().toString());
		watchedFolder.setVolume(volume);
		watchedFolder.setTransfer(isTransfer());
		watchedFolder.setRecursive(subfolderButton.getSelection());
	}

	public boolean isTransfer() {
		return typeButtonGroup.getSelection() == 1;
	}

}
