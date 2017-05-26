package com.bdaum.zoom.ui.dialogs;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.css.internal.CssActivator;

public class ZProgressMonitorDialog extends ProgressMonitorDialog {

	public ZProgressMonitorDialog(Shell parent) {
		super(parent);
	}
	
	@Override
	public void create() {
		super.create();
		CssActivator.getDefault().setColors(getShell());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Constants.APPLICATION_NAME);
	}

}
