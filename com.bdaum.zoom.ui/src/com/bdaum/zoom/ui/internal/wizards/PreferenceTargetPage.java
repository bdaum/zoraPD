package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class PreferenceTargetPage extends ColoredWizardPage {

	private static final String PATH = "path"; //$NON-NLS-1$
	private FileEditor fileEditor;
	protected String path;
	private IDialogSettings dialogSettings;

	public PreferenceTargetPage() {
		super(
				"main", Messages.PreferenceTargetPage_export_preferences_to_file, null); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(PATH);
		Composite composite = createComposite(parent, 3);
		fileEditor = new FileEditor(
				composite,
				SWT.SAVE,
				Messages.PreferenceTargetPage_target_file,
				true,
				new String[] { "*.zpf" }, //$NON-NLS-1$
				new String[] { Messages.PreferenceTargetPage_user_preferences },
				path, path == null ? BatchConstants.APP_PREFERENCES : path,
				true);
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		fileEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				path = getTargetFile();
				validatePage();
			}
		});
		setControl(composite);
		validatePage();
		super.createControl(parent);
	}

	public String getTargetFile() {
		return fileEditor.getText().trim();
	}

	@Override
	protected void validatePage() {
		String errorMessage = null;
		if (path == null || path.isEmpty()
				|| (path.indexOf('/') < 0 && path.indexOf('\\') < 0))
			errorMessage = Messages.PreferenceTargetPage_specify_target_file;
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	public String getPath() {
		return path;
	}

	public void saveSettings() {
		dialogSettings.put(PATH, path);
	}

}
