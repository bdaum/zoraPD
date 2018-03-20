package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.TextWithVariableGroup;

public class ConfigureCaptionDialog extends ZTitleAreaDialog {

	private TextWithVariableGroup templateGroup;
	private String template;
	private RadioButtonGroup alignmentGroup;
	private int alignment;
	private Asset asset;

	public ConfigureCaptionDialog(Shell parentShell, String template, int alignment, Asset asset) {
		super(parentShell);
		this.alignment = alignment;
		this.asset = asset;
		this.template = template == null ? "" : template; //$NON-NLS-1$
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ConfigureCaptionDialog_configure_caption);
		setMessage(Messages.ConfigureCaptionDialog_configure_caption_msg);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(4, false));
		templateGroup = new TextWithVariableGroup(composite, Messages.ConfigureCaptionDialog_template, 500,
				Constants.TH_ALL, true, template == null ? null : new String[] { template }, asset, ""); //$NON-NLS-1$
		alignmentGroup = new RadioButtonGroup(composite, Messages.ConfigureCaptionDialog_alignment, SWT.HORIZONTAL,
				Messages.ConfigureCaptionDialog_left, Messages.ConfigureCaptionDialog_center,
				Messages.ConfigureCaptionDialog_right);
		alignmentGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 4, 1));
		switch (alignment) {
		case SWT.LEFT:
			alignmentGroup.setSelection(0);
			break;
		case SWT.CENTER:
			alignmentGroup.setSelection(1);
			break;
		case SWT.RIGHT:
			alignmentGroup.setSelection(2);
			break;
		}
		return area;
	}

	@Override
	protected void okPressed() {
		template = templateGroup.getText();
		switch (alignmentGroup.getSelection()) {
		case 0:
			alignment = SWT.LEFT;
			break;
		case 1:
			alignment = SWT.CENTER;
			break;
		case 2:
			alignment = SWT.RIGHT;
			break;
		}
		super.okPressed();
	}

	public String getTemplate() {
		return template;
	}

	public int getAlignment() {
		return alignment;
	}

}
