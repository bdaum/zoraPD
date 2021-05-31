/*******************************************************************************
 * Copyright (c) 2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.gps.internal.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ExportPage extends ColoredWizardPage implements Listener {

	private static final String EXTENSIONS = "*.{0};*.{1}"; //$NON-NLS-1$

	private static final String PATH = "{0}Path"; //$NON-NLS-1$

	private List<Asset> assets;
	private String type;
	private IDialogSettings dialogSettings;

	private String path;

	private FileEditor fileEditor;

	public ExportPage(List<Asset> assets, String type) {
		super("export"); //$NON-NLS-1$
		this.assets = assets;
		this.type = type;
	}

	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(NLS.bind(PATH, type));
		Composite composite = createComposite(parent, 3);
		String[] filterExtensions = new String[] { NLS.bind(EXTENSIONS, type, type.toUpperCase()), "*.*" }; //$NON-NLS-1$
		String[] filterNames = new String[] { NLS.bind(Messages.ExportPage_x_files, type.toUpperCase(), type),
				Messages.ExportPage_all_files };
		String fileName = "*." + type; //$NON-NLS-1$
		fileEditor = new FileEditor(composite, SWT.SAVE | SWT.READ_ONLY, Messages.ExportPage_target_file, true,
				filterExtensions, filterNames, path, fileName, false, dialogSettings);
		fileEditor.addListener(SWT.Modify, this);
		new Label(composite, SWT.NONE).setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		setControl(composite);
		setHelp(type == AbstractGeoExportWizard.KML ? HelpContextIds.KML_WIZARD : HelpContextIds.GPX_WIZARD);
		setTitle(Messages.ExportPage_output_file);
		setMessage(NLS.bind(Messages.ExportPage_specify_location, type.toUpperCase()));
		fillValues();
		super.createControl(parent);
	}

	@Override
	public void handleEvent(Event event) {
		path = fileEditor.getFilterPath();
		validatePage();
	}

	private void fillValues() {
		// do nothing
	}

	@Override
	protected String validate() {
		String targetFile = getTargetFile();
		if (assets.isEmpty())
			return Messages.ExportPage_no_geocoded_image;
		if (targetFile.isEmpty())
			return Messages.ExportPage_file_name_empty;
		if (targetFile.indexOf('*') >= 0 || targetFile.indexOf('?') >= 0)
			return Messages.ExportPage_specify_target_file;
		return null;
	}

	public String getTargetFile() {
		return fileEditor.getText().trim();
	}

	public boolean finish() {
		if (fileEditor.testSave()) {
			dialogSettings.put(NLS.bind(PATH, type), path);
			fileEditor.saveValues();
			return true;
		}
		return false;
	}

}
