package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportTargetPage extends ColoredWizardPage {

	private static final String TARGETDIR = "targetDir"; //$NON-NLS-1$
	private static final String SUBFOLDERS = "subfolder"; //$NON-NLS-1$

	private Text targetDirField;
	private Combo subfolderCombo;
	private final boolean media;
	private Label copyLabel;

	public ImportTargetPage(String pageName, boolean media) {
		super(pageName);
		this.media = media;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(final Composite parent) {
		Composite targetComp = new Composite(parent, SWT.NONE);
		targetComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetComp.setLayout(new GridLayout(3, false));
		copyLabel = new Label(targetComp, SWT.NONE);
		copyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));
		Label transferToLabel = new Label(targetComp, SWT.NONE);
		transferToLabel.setText(Messages.ImportFromDeviceWizard_transfer_to);

		targetDirField = new Text(targetComp, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_text.widthHint = 200;
		targetDirField.setLayoutData(gd_text);

		final Button browseButton = WidgetFactory.createPushButton(targetComp,
				Messages.ImportFromDeviceWizard_browse, SWT.BEGINNING);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setText(Messages.ImportTargetPage_target_folder);
				dialog.setMessage(Messages.ImportTargetPage_select_folder);
				String lastTargetDir = targetDirField.getText();
				dialog.setFilterPath(lastTargetDir.length() > 0 ? lastTargetDir
						: null);
				String dir = dialog.open();
				if (dir != null) {
					if (!dir.endsWith(File.separator))
						dir += File.separator;
					targetDirField.setText(dir);
					updateSpaceLabel();
					validatePage();
				}
			}
		});

		new Label(targetComp, SWT.NONE)
				.setText(Messages.ImportFromDeviceWizard_create_subfolder);

		subfolderCombo = new Combo(targetComp, SWT.READ_ONLY);
		subfolderCombo.setItems(new String[] {
				Messages.ImportFromDeviceWizard_no,
				Messages.ImportFromDeviceWizard_by_year,
				Messages.ImportFromDeviceWizard_by_year_month,
				Messages.ImportFromDeviceWizard_by_year_month_day });
		final GridData gd_subfolderCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		subfolderCombo.setLayoutData(gd_subfolderCombo);
		new Label(targetComp, SWT.NONE);
		setControl(targetComp);
		setHelp(media ? HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_TARGET_SELECTION
				: HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_TARGET_SELECTION);
		setTitle(Messages.ImportTargetPage_target_selection);
		setMessage(Messages.ImportTargetPage_select_the_target_folder);
		super.createControl(parent);
		fillValues();
		validatePage();
	}

	private void fillValues() {
		IDialogSettings dialogSettings = ((ImportFromDeviceWizard) getWizard()).getDialogSettings();
		String targetDir = dialogSettings.get(TARGETDIR);
		targetDirField.setText(targetDir == null ? "" : targetDir); //$NON-NLS-1$
		try {
			subfolderCombo.select(dialogSettings.getInt(SUBFOLDERS));
		} catch (NumberFormatException e) {
			subfolderCombo.select(2);
		}
		updateSpaceLabel();
	}

	private void updateSpaceLabel() {
		List<File> selectedFiles = ((ImportFromDeviceWizard) getWizard())
				.getSelectedFiles();
		if (selectedFiles == null) {
			copyLabel.setText(Messages.ImportTargetPage_medium_offline);
			copyLabel.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			copyLabel.setData("id", null); //$NON-NLS-1$
			long requiredSpace = 0;
			int n = selectedFiles.size();
			long freespace = 0;
			String target = targetDirField.getText();
			if (target.length() > 0) {
				File targetFile = new File(target);
				if (targetFile.exists())
					freespace = CoreActivator.getDefault().getFreeSpace(targetFile);
			}
			if (freespace <= 0) {
				copyLabel.setText(""); //$NON-NLS-1$
				return;
			}
			for (File file : selectedFiles)
				requiredSpace += file.length();
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			switch (n) {
			case 0:
				copyLabel.setText(Messages.ImportTargetPage_no_files);
				break;
			case 1:
				copyLabel.setText(NLS.bind(Messages.ImportTargetPage_one_image,
						nf.format(requiredSpace / 1048576.0),
						nf.format(freespace / 1048576.0)));
				break;
			default:
				copyLabel.setText(NLS.bind(Messages.ImportTargetPage_n_images,
						new Object[] { n, nf.format(requiredSpace / 1048576.0),
								nf.format(freespace / 1048576.0) }));
				break;
			}
		}
	}

	@Override
	protected void validatePage() {
		String target = targetDirField.getText();
		if (target.length() == 0) {
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

	public void performFinish(ImportFromDeviceData importData) {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		String targetDir = targetDirField.getText();
		importData.setTargetDir(targetDir);
		dialogSettings.put(TARGETDIR, targetDir);
		int subfolderPolicy = subfolderCombo.getSelectionIndex();
		importData.setSubfolderPolicy(subfolderPolicy);
		dialogSettings.put(SUBFOLDERS, subfolderPolicy);
	}

}
