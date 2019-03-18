package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.widgets.DateInput;

public class ComputeTimeshiftDialog extends ZTitleAreaDialog {

	private int diff;
	private DateInput trackerTime;
	private DateInput cameraTime;
	private String msg;
	private String refLabel;
	private String cameraLabel;

	public ComputeTimeshiftDialog(Shell shell, int diff, String msg, String refLabel, String cameraLabel) {
		super(shell);
		this.diff = diff;
		this.msg = msg;
		this.refLabel = refLabel;
		this.cameraLabel = cameraLabel;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ComputeTimeshiftDialog_compute_time_diff);
		setMessage(msg);
		getShell().pack();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(refLabel); 
		trackerTime = new DateInput(composite, SWT.DATE | SWT.TIME);
		trackerTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		new Label(composite, SWT.NONE).setText(cameraLabel);
		cameraTime = new DateInput(composite, SWT.DATE | SWT.TIME);
		cameraTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Date tt = new Date();
		trackerTime.setDate(tt);
		cameraTime.setDate(new Date(tt.getTime() - diff * 60000L));
		return area;
	}

	@Override
	protected void okPressed() {
		diff = (int) ((trackerTime.getDate().getTime() - cameraTime.getDate().getTime()) / 60000L);
		super.okPressed();
	}

	public int getResult() {
		return diff;
	}

}