package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.LabelConfigGroup;

public class ConfigureCaptionDialog extends ZTitleAreaDialog implements Listener {

	private String template;
	private int alignment;
	private Asset asset;
	private LabelConfigGroup labelConfigGroup;
	private int fontsize;
	private int show;
	private boolean overlay;

	public ConfigureCaptionDialog(Shell parentShell, int show, String template, int alignment, int fontsize,
			boolean overlay, Asset asset) {
		super(parentShell, HelpContextIds.CONFIG_CAPTIONS_DIALOG);
		this.show = show;
		this.alignment = alignment;
		this.fontsize = fontsize;
		this.overlay = overlay;
		this.asset = asset;
		this.template = template == null ? "" : template; //$NON-NLS-1$
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		setTitle(Messages.ConfigureCaptionDialog_configure_caption);
		setMessage(Messages.ConfigureCaptionDialog_configure_caption_msg);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(4, false));
		labelConfigGroup = new LabelConfigGroup(composite, true, true, true);
		labelConfigGroup.addListener(SWT.Modify, this);
		return area;
	}

	private void fillValues() {
		labelConfigGroup.setContext("", asset); //$NON-NLS-1$
		int align;
		switch (alignment) {
		case SWT.LEFT:
			align = 0;
			break;
		case SWT.RIGHT:
			align = 2;
			break;
		default:
			align = 1;
		}
		labelConfigGroup.setSelection(show, template, fontsize, align, overlay);
	}

	@Override
	protected void okPressed() {
		show = labelConfigGroup.getSelection();
		template = labelConfigGroup.getTemplate();
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
		overlay = labelConfigGroup.getOverlay();
		fontsize = labelConfigGroup.getFontSize();
		super.okPressed();
	}

	public String getTemplate() {
		return template;
	}

	public int getAlignment() {
		return alignment;
	}

	public int getShow() {
		return show;
	}

	public boolean getOverlay() {
		return overlay;
	}

	public int getFontsize() {
		return fontsize;
	}

	@Override
	public void handleEvent(Event event) {
		updateButtons();
	}

	public void updateButtons() {
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			boolean valid = validate();
			getShell().setModified(valid);
			button.setEnabled(valid);
		}
	}

	private boolean validate() {
		if (readonly)
			return false;
		String errorMessage = labelConfigGroup.validate();
		setErrorMessage(errorMessage);
		return errorMessage == null;
	}

}
