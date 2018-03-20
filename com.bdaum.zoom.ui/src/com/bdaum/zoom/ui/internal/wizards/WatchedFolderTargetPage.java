package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class WatchedFolderTargetPage extends ColoredWizardPage {

	private Text targetDirField;
	private Combo subfolderCombo;
	private final WatchedFolderImpl watchedFolder;

	public WatchedFolderTargetPage(String pageName, WatchedFolderImpl watchedFolder) {
		super(pageName);
		this.watchedFolder = watchedFolder;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(final Composite parent) {
		Composite targetComp = createComposite(parent, 3);
		new Label(targetComp, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		new Label(targetComp, SWT.NONE).setText(Messages.ImportFromDeviceWizard_transfer_to);
		targetDirField = new Text(targetComp, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_text.widthHint = 200;
		targetDirField.setLayoutData(gd_text);
		final Button browseButton = WidgetFactory.createPushButton(targetComp, Messages.ImportFromDeviceWizard_browse,
				SWT.BEGINNING);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setText(Messages.ImportTargetPage_target_folder);
				dialog.setMessage(Messages.ImportTargetPage_select_folder);
				String lastTargetDir = targetDirField.getText();
				dialog.setFilterPath(lastTargetDir.isEmpty() ? null : lastTargetDir);
				String dir = dialog.open();
				if (dir != null) {
					if (!dir.endsWith(File.separator))
						dir += File.separator;
					targetDirField.setText(dir);
					validatePage();
				}
			}
		});

		new Label(targetComp, SWT.NONE).setText(Messages.ImportFromDeviceWizard_create_subfolder);
		subfolderCombo = new Combo(targetComp, SWT.READ_ONLY);
		subfolderCombo.setItems(new String[] { Messages.ImportFromDeviceWizard_no,
				Messages.ImportFromDeviceWizard_by_year, Messages.ImportFromDeviceWizard_by_year_month,
				Messages.ImportFromDeviceWizard_by_year_month_day });
		final GridData gd_subfolderCombo = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		subfolderCombo.setLayoutData(gd_subfolderCombo);
		new Label(targetComp, SWT.NONE);
		setControl(targetComp);
		setHelp(HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_TARGET_SELECTION);
		setTitle(getName());
		setMessage(Messages.ImportTargetPage_select_the_target_folder);
		super.createControl(parent);
		fillValues();
		validatePage();
	}

	private void fillValues() {
		String targetDir = watchedFolder.getTargetDir();
		if (targetDir != null)
			targetDirField.setText(targetDir);
		subfolderCombo.select(watchedFolder.getSubfolderPolicy());
	}

	@Override
	protected void validatePage() {
		String target = targetDirField.getText();
		if (target.isEmpty()) {
			setErrorMessage(Messages.ImportFromDeviceWizard_specify_output_dir);
			setPageComplete(false);
		} else {
			if (!new File(target).exists()) {
				setErrorMessage(Messages.ImportFromDeviceWizard_target_dir_does_not_exist);
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
	}

	public void performFinish() {
		watchedFolder.setTargetDir(targetDirField.getText());
		watchedFolder.setSubfolderPolicy(subfolderCombo.getSelectionIndex());
	}

}
