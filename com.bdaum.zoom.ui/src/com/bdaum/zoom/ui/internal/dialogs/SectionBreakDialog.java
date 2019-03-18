/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;

public class SectionBreakDialog extends ZTitleAreaDialog {

	public static final String[] THUMBNAILS = new String[] {
			Messages.SectionBreakDialog_no_thumbnails,
			Messages.SectionBreakDialog_left,
			Messages.SectionBreakDialog_right, Messages.SectionBreakDialog_top,
			Messages.SectionBreakDialog_bottom };
	private SlideImpl slide;
	private CheckedText captionField;
	private CheckedText descriptionField;
	private Combo thumbnailField;

	public SectionBreakDialog(Shell parentShell, SlideImpl slide) {
		super(parentShell, HelpContextIds.SECTIONBREAK_DIALOG);
		this.slide = slide;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SectionBreakDialog_slide_props);
		setMessage(Messages.SectionBreakDialog_please_specify);
		updateButtons();
		getShell().layout();
		getShell().pack();
	}

	private void updateButtons() {
		getShell().setModified(!readonly);
		getButton(IDialogConstants.OK_ID).setEnabled(!readonly);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.SectionBreakDialog_caption);
		captionField = new CheckedText(comp,
				SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		captionField.setSpellingOptions(8, ISpellCheckingService.TITLEOPTIONS);
		captionField.setLayoutData( new GridData(SWT.FILL,
						SWT.CENTER, true, false));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.SectionBreakDialog_description);
		descriptionField = new CheckedText(comp, SWT.MULTI | SWT.LEAD
				| SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL,
				SWT.FILL, true, true);
		layoutData.widthHint = 400;
		layoutData.heightHint = 100;
		descriptionField.setLayoutData(layoutData);
		new Label(comp, SWT.NONE).setText(Messages.SectionBreakDialog_thumbnails);
		thumbnailField = new Combo(comp, SWT.DROP_DOWN);
		thumbnailField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		thumbnailField.setItems(THUMBNAILS);
		fillValues();
		return area;
	}

	private void fillValues() {
		if (slide != null) {
			captionField.setText(slide.getCaption());
			descriptionField.setText(slide.getDescription());
			thumbnailField.select(slide.getLayout());
		} else
			thumbnailField.select(0);
	}

	@Override
	protected void okPressed() {
		if (slide == null)
			slide = new SlideImpl();
		slide.setCaption(captionField.getText());
		slide.setDescription(descriptionField.getText());
		slide.setLayout(Math.max(0, thumbnailField.getSelectionIndex()));
		super.okPressed();
	}

	public SlideImpl getResult() {
		return slide;
	}

}
