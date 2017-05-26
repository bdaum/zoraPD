package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public abstract class AbstractListCellEditorDialog extends
		ZTitleAreaDialog {

	protected Object value;
	protected QueryField qfield;

	public AbstractListCellEditorDialog(Shell parentShell, Object value,
			QueryField qfield) {
		super(parentShell);
		this.value = value;
		this.qfield = qfield;
	}

	@Override
	public void create() {
		super.create();
		setTitle(qfield.getLabelWithUnit());
	}

	public Object getResult() {
		return value;
	}
}