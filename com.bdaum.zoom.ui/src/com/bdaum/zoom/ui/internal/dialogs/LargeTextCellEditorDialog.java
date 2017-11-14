package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;

public class LargeTextCellEditorDialog extends ZTitleAreaDialog {

	private Object value;
	private QueryField qfield;
	private CheckedText viewer;
	private boolean valid;
	private final Asset asset;

	public LargeTextCellEditorDialog(Shell parentShell, Object value,
			QueryField qfield, Asset asset) {
		super(parentShell);
		this.asset = asset;
		this.value = value;
		this.qfield = qfield;
	}

	@Override
	public void create() {
		super.create();
		setTitle(qfield.getLabelWithUnit());
		setMessage(Messages.LargeTextCellEditorDialog_enter_text);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 150;
		viewer = new CheckedText(comp, SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER);
		viewer.setSpellingOptions(10, qfield.getSpellingOptions());
		if (value != null)
			viewer.setText(value.toString());
		viewer.setLayoutData(layoutData);
		viewer.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String newText = viewer.getText().substring(0, e.start) + e.text
						+ viewer.getText().substring(e.end);
				String msg = qfield.isValid(newText, asset);
				setErrorMessage(msg);
				valid = msg == null;
				updateButtons();
			}
		});
		return comp;
	}

	protected void updateButtons() {
		getButton(IDialogConstants.OK_ID).setEnabled(valid);
	}

	@Override
	protected void okPressed() {
		value = viewer.getText();
		super.okPressed();
	}

	public Object getResult() {
		return value;
	}

}