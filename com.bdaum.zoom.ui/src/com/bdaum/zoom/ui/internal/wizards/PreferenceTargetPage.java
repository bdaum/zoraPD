package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class PreferenceTargetPage extends ColoredWizardPage implements Listener {

	private static final String PATH = "path"; //$NON-NLS-1$
	private FileEditor fileEditor;
	protected String path;
	private IDialogSettings dialogSettings;

	public PreferenceTargetPage() {
		super("main", Messages.PreferenceTargetPage_export_preferences_to_file, null); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(PATH);
		Composite composite = createComposite(parent, 3);
		fileEditor = new FileEditor(composite, SWT.SAVE, Messages.PreferenceTargetPage_target_file, true,
				new String[] { "*.zpf" }, //$NON-NLS-1$
				new String[] { Messages.PreferenceTargetPage_user_preferences }, path,
				path == null ? BatchConstants.APP_PREFERENCES : path, true, dialogSettings);
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		fileEditor.addListener(SWT.Modify, this);
		setControl(composite);
		validatePage();
		super.createControl(parent);
	}
	
	@Override
	public void handleEvent(Event event) {
		path = getTargetFile();
		validatePage();
	}

	public String getTargetFile() {
		return fileEditor.getText().trim();
	}

	@Override
	protected void validatePage() {
		String errorMessage = null;
		if (path == null || path.isEmpty() || (path.indexOf('/') < 0 && path.indexOf('\\') < 0))
			errorMessage = Messages.PreferenceTargetPage_specify_target_file;
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	public String getPath() {
		return path;
	}

	public void saveSettings() {
		dialogSettings.put(PATH, path);
		fileEditor.saveValues();
	}

}
