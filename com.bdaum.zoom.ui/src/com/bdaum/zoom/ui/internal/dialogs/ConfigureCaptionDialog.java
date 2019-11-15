package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.LabelConfigGroup;
import com.bdaum.zoom.ui.internal.widgets.TextWithVariableGroup;

public class ConfigureCaptionDialog extends ZTitleAreaDialog {

	private TextWithVariableGroup templateGroup;
	private String template;
	private int alignment;
	private Asset asset;
	private LabelConfigGroup labelConfigGroup;
	private int fontsize;
	private int show;

	public ConfigureCaptionDialog(Shell parentShell, int show, String template, int alignment, int fontsize,
			Asset asset) {
		super(parentShell, HelpContextIds.CONFIG_CAPTIONS_DIALOG);
		this.show = show;
		this.alignment = alignment;
		this.fontsize = fontsize;
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
		labelConfigGroup = new LabelConfigGroup(composite, true, true);
		labelConfigGroup.setContext("", asset); //$NON-NLS-1$
		int a;
		switch (alignment) {
		case SWT.LEFT:
			a = 0;
			break;
		case SWT.RIGHT:
			a = 2;
			break;
		default:
			a = 1;
		}
		labelConfigGroup.setSelection(show, template, fontsize, a);
		return area;
	}

	@Override
	protected void okPressed() {
		template = templateGroup.getText();
		switch (labelConfigGroup.getAlignment()) {
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
