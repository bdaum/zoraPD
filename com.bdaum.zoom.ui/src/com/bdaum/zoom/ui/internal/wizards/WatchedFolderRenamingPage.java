package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.RenamingTemplate;
import com.bdaum.zoom.ui.internal.widgets.RenameGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class WatchedFolderRenamingPage extends ColoredWizardPage {

	private RenameGroup renameGroup;
	private final WatchedFolderImpl watchedFolder;

	public WatchedFolderRenamingPage(String pageName, WatchedFolderImpl watchedFolder) {
		super(pageName);
		this.watchedFolder = watchedFolder;
	}

	@Override
	public void createControl(final Composite parent) {
		renameGroup = new RenameGroup(parent, SWT.NONE, null, false,
				new RenamingTemplate[] {
						new RenamingTemplate(Messages.ImportRenamingPage_date_filename, "_" + Constants.TV_YYYY //$NON-NLS-1$
								+ Constants.TV_MM + Constants.TV_DD + "-" //$NON-NLS-1$
								+ Constants.TV_FILENAME, true),
						new RenamingTemplate(Messages.ImportRenamingPage_user_year_seq,
								Constants.TV_USER + Constants.TV_YYYY + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.ImportRenamingPage_cue_year_seq,
								Constants.TV_CUE + "_" + Constants.TV_YYYY + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$ //$NON-NLS-2$
						new RenamingTemplate(Messages.ImportRenamingPage_filename_seq,
								Constants.TV_FILENAME + "-" + Constants.TV_SEQUENCE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.ImportRenamingPage_orig_filename, Constants.TV_FILENAME, true) },
				true);
		renameGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}
		});
		renameGroup.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		renameGroup.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
		});
		setControl(renameGroup);
		setHelp(HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_RENAMING);
		setTitle(getName());
		setMessage(Messages.ImportRenamingPage_select_a_template);
		super.createControl(parent);
		fillValues();
	}

	private void fillValues() {
		renameGroup.fillValues(getWizard().getDialogSettings(), watchedFolder.getSelectedTemplate(),
				watchedFolder.getCue());
	}

	@Override
	protected void validatePage() {
		String errorMsg = renameGroup.validate();
		setPageComplete(errorMsg == null);
		setErrorMessage(errorMsg);
	}

	public void performFinish() {
		RenamingTemplate selectedTemplate = renameGroup.getSelectedTemplate();
		watchedFolder.setSelectedTemplate(selectedTemplate.getContent());
		watchedFolder.setCue(renameGroup.getCue());

	}

}
