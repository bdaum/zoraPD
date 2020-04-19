package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class TransferPage extends ColoredWizardPage {

	int[] FSVALUES = { Constants.FILESOURCE_UNKNOWN, Constants.FILESOURCE_FILMSCANNER,
			Constants.FILESOURCE_REFLECTIVE_SCANNER, Constants.FILESOURCE_DIGITAL_CAMERA,
			Constants.FILESOURCE_SIGMA_DIGITAL_CAMERA };

	private static final String[] FSLABELS = new String[] { Messages.TransferPage_unknown,
			Messages.TransferPage_film_scanner, Messages.TransferPage_reflective_scanner,
			Messages.TransferPage_digital_camera, Messages.TransferPage_sigma };

	private static final int[] POLICYMAP = new int[] { 0, 1, 3 };
	private static final String[] POLICIES = new String[] { ImportFileSelectionPage.SKIPPOLICIES[0],
			ImportFileSelectionPage.SKIPPOLICIES[1], ImportFileSelectionPage.SKIPPOLICIES[3] };
	private static final String[] EMPTYSTRINGS = new String[0];
	private static final String ARTISTS = "artists"; //$NON-NLS-1$

	private Combo artistField;
	private final WatchedFolderImpl watchedFolder;
	private Combo skipCombo;
	private Combo fileSourceField;

	public TransferPage(String pageName, WatchedFolderImpl watchedFolder) {
		super(pageName);
		this.watchedFolder = watchedFolder;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = createComposite(parent, 1);
		CGroup metaComp = new CGroup(comp, SWT.NONE);
		metaComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		metaComp.setLayout(new GridLayout(4, false));
		metaComp.setText(Messages.ImportAddMetadataPage_add_metadata);

		WatchedFolderWizard wizard = (WatchedFolderWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();

		artistField = createHistoryCombo(metaComp, Messages.ImportFromDeviceWizard_Artist, dialogSettings, ARTISTS);
		if (watchedFolder.getArtist() != null)
			artistField.setText(watchedFolder.getArtist());
		new Label(metaComp, SWT.NONE).setText(Messages.TransferPage_file_source);
		fileSourceField = new Combo(metaComp, SWT.NONE);
		fileSourceField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fileSourceField.setItems(FSLABELS);

		Composite innerComp = new Composite(comp, SWT.NONE);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		innerComp.setLayout(new GridLayout());
		Composite skipComp = new Composite(comp, SWT.NONE);
		skipComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		skipComp.setLayout(new GridLayout(2, false));
		new Label(skipComp, SWT.NONE).setText(Messages.ImportFileSelectionPage_skipped_file_types);
		skipCombo = new Combo(skipComp, SWT.READ_ONLY);
		skipCombo.setItems(POLICIES);
		skipCombo.setVisibleItemCount(5);
		setControl(comp);
		setHelp(HelpContextIds.WATCHED_FOLDER_METADATA);
		setTitle(getName());
		setMessage(Messages.TransferPage_transfer_page_msg);
		fillValues();
		super.createControl(parent);
	}

	private static Combo createHistoryCombo(Composite parent, String lab, IDialogSettings settings, String key) {
		new Label(parent, SWT.NONE).setText(lab);
		Combo combo = new Combo(parent, SWT.NONE);
		String[] items = settings.getArray(key);
		if (items != null) {
			combo.setItems(items);
			combo.setVisibleItemCount(8);
		} else
			combo.setItems(EMPTYSTRINGS);
		combo.setData(UiConstants.KEY, key);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return combo;
	}

	private static void saveComboHistory(Combo combo, IDialogSettings settings) {
		settings.put((String) combo.getData(UiConstants.KEY), UiUtilities.updateComboHistory(combo)); 
	}

	private void fillValues() {
		updateAuthor();
		int skipPolicy = watchedFolder.getSkipPolicy();
		for (int i = 0; i < POLICYMAP.length; i++)
			if (POLICYMAP[i] == skipPolicy) {
				skipCombo.select(i);
				break;
			}
		int fileSource = watchedFolder.getFileSource();
		for (int i = 0; i < FSVALUES.length; i++)
			if (FSVALUES[i] == fileSource) {
				fileSourceField.select(i);
				break;
			}
	}

	public void updateAuthor() {
		// do nothing
	}

	@Override
	protected String validate() {
		return null;
	}

	public void performFinish() {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		saveComboHistory(artistField, dialogSettings);
		watchedFolder.setArtist(artistField.getText());
		watchedFolder.setSkipPolicy(POLICYMAP[Math.max(0, skipCombo.getSelectionIndex())]);
		watchedFolder.setSkipDuplicates(true);
		watchedFolder.setFileSource(FSVALUES[Math.max(0, fileSourceField.getSelectionIndex())]);
	}

}
