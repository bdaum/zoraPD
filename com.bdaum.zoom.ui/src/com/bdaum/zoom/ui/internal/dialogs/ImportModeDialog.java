package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class ImportModeDialog extends ZDialog {

	private boolean newStructure;
	private RadioButtonGroup buttonGroup;

	public ImportModeDialog(Shell parent) {
		this(parent, false);
	}

	public ImportModeDialog(Shell parent, boolean newStructure) {
		super(parent);
		this.newStructure = newStructure;
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE)
				.setText(Messages.ImportModeDialog_select_mode + "     "); //$NON-NLS-1$
		buttonGroup = new RadioButtonGroup(composite, null, SWT.NONE, Messages.ImportModeDialog_direct_import, Messages.ImportModeDialog_new_folder_structure);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalIndent = 20;
		buttonGroup.setLayoutData(layoutData);
		if (newStructure)
			buttonGroup.setSelection(1);
		else
			buttonGroup.setSelection(0);
		return super.createDialogArea(parent);
	}

	@Override
	protected void okPressed() {
		newStructure = buttonGroup.getSelection() == 1;
		super.okPressed();
	}

	public boolean isNewStructure() {
		return newStructure;
	}

}
