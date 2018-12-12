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

package com.bdaum.zoom.csv.internal.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.csv.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class CsvTargetFilePage extends ColoredWizardPage {

	private static final String CSVPATH = "csvPath"; //$NON-NLS-1$
	private static final String NOFIRSTLINE = "firstLine"; //$NON-NLS-1$
	private String path;
	private FileEditor fileEditor;
	private IDialogSettings dialogSettings;
	private final List<Asset> assets;
	private CheckboxButton firstLineButton;

	protected CsvTargetFilePage(List<Asset> assets) {
		super("targetFile"); //$NON-NLS-1$
		this.assets = assets;
	}

	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(CSVPATH);
		Composite composite = createComposite(parent, 3);
		String[] filterExtensions = new String[] { "*.csv;*.CSV", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
		String[] filterNames = new String[] {
				Messages.CsvTargetFilePage_comma_separated_values,
				Messages.CsvTargetFilePage_all_files };
		String fileName = "*.csv"; //$NON-NLS-1$

		fileEditor = new FileEditor(composite, SWT.SAVE | SWT.READ_ONLY,
				Messages.CsvTargetFilePage_target_file, true, filterExtensions,
				filterNames, path, fileName, true, dialogSettings);
		fileEditor.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				path = fileEditor.getFilterPath();
				validatePage();
			}
		});
		firstLineButton = WidgetFactory.createCheckButton(composite,
				Messages.CsvTargetFilePage_first_line, new GridData(SWT.BEGINNING,
						SWT.CENTER, false, false, 3, 1));
		firstLineButton.setSelection(!dialogSettings.getBoolean(NOFIRSTLINE));
		new Label(composite, SWT.NONE).setLayoutData(new GridData(
				SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		setControl(composite);
		setHelp(HelpContextIds.CSV_WIZARD);
		setTitle(Messages.CsvTargetFilePage_output_file);
		setMessage(Messages.CsvTargetFilePage_specify_location);
		fillValues();
		super.createControl(parent);
	}

	private void fillValues() {
		// do nothing
	}

	@Override
	protected void validatePage() {
		boolean valid = true;
		String targetFile = getTargetFile();
		if (assets.isEmpty()) {
			setErrorMessage(Messages.CsvTargetFilePage_no_image_selected);
			valid = false;
		} else if (targetFile.isEmpty()) {
			setErrorMessage(Messages.CsvTargetFilePage_file_name_empty);
			valid = false;
		} else if (targetFile.indexOf('*') >= 0 || targetFile.indexOf('?') >= 0) {
			setErrorMessage(Messages.CsvTargetFilePage_specify_target_file);
			valid = false;
		} else
			setErrorMessage(null);
		setPageComplete(valid);
	}

	public String getTargetFile() {
		return fileEditor.getText().trim();
	}

	public boolean getFirstLine() {
		return firstLineButton.getSelection();
	}

	public void finish() {
		dialogSettings.put(CSVPATH, path);
		dialogSettings.put(NOFIRSTLINE, !firstLineButton.getSelection());
		fileEditor.saveValues();
	}

}
