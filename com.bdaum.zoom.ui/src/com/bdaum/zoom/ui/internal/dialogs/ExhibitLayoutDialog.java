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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.WebColorGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class ExhibitLayoutDialog extends ZTitleAreaDialog {

	private static final int DEFAULTBUTTON = 9999;
	private final Exhibit exhibit;
	private NumericControl matWidthField;
	private WebColorGroup matColorGroup;
	private NumericControl frameWidthField;
	private WebColorGroup frameColorGroup;
	private final ExhibitionImpl exhibition;
	private NumericControl imageWidthField;
	private NumericControl imageHeightField;
	private NumericControl totalWidthField;
	private NumericControl totalHeightField;
	private LabelLayoutGroup labelLayoutGroup;

	public ExhibitLayoutDialog(Shell parentShell, ExhibitionImpl exhibition,
			Exhibit exhibit) {
		super(parentShell, HelpContextIds.EXHIBITLAYOUT_DIALOG);
		this.exhibition = exhibition;
		this.exhibit = exhibit;
	}

	@Override
	public void create() {
		super.create();
		setTitle(NLS.bind(Messages.ExhibitLayoutDialog_single_exhibit_layout, exhibit.getTitle()));
		setMessage(Messages.ExhibitLayoutDialog_exhibit_layout_msg);
		fillValues();
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(3, false));
		CGroup imageGroup = new CGroup(comp, SWT.NONE);
		imageGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
				true, false));
		imageGroup.setLayout(new GridLayout(4, false));
		imageGroup.setText(Messages.ExhibitLayoutDialog_image);
		new Label(imageGroup, SWT.NONE)
				.setText(Messages.ExhibitLayoutDialog_size);
		imageWidthField = new NumericControl(imageGroup, SWT.NONE);
		imageWidthField.setMaximum(10000);
		imageWidthField.setDigits(1);
		imageWidthField.setLogrithmic(true);
		imageWidthField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSizeFields(imageWidthField);
			}
		});
		new Label(imageGroup, SWT.NONE).setText(Messages.ExhibitLayoutDialog_x);
		imageHeightField = new NumericControl(imageGroup, SWT.NONE);
		imageHeightField.setMaximum(10000);
		imageHeightField.setDigits(1);
		imageHeightField.setLogrithmic(true);
		imageHeightField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSizeFields(imageHeightField);
			}
		});
		CGroup matGroup = new CGroup(comp, SWT.NONE);
		matGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true,
				false));
		matGroup.setLayout(new GridLayout(4, false));
		matGroup.setText(Messages.ExhibitLayoutDialog_mat);
		new Label(matGroup, SWT.NONE)
				.setText(Messages.ExhibitLayoutDialog_width);
		matWidthField = new NumericControl(matGroup, SWT.NONE);
		matWidthField.setMaximum(1000);
		matWidthField.setDigits(1);
		matWidthField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateColorGroups();
				updateTotal();
			}
		});
		matColorGroup = new WebColorGroup(matGroup,
				Messages.ExhibitLayoutDialog_color);
		CGroup frameGroup = new CGroup(comp, SWT.NONE);
		frameGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
				true, false));
		frameGroup.setLayout(new GridLayout(4, false));
		frameGroup.setText(Messages.ExhibitLayoutDialog_frame);
		new Label(frameGroup, SWT.NONE)
				.setText(Messages.ExhibitLayoutDialog_width);
		frameWidthField = new NumericControl(frameGroup, SWT.NONE);
		frameWidthField.setMaximum(100);
		frameWidthField.setDigits(1);
		frameWidthField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateColorGroups();
				updateTotal();
			}
		});
		frameColorGroup = new WebColorGroup(frameGroup,
				Messages.ExhibitLayoutDialog_color);
		CGroup totalGroup = new CGroup(comp, SWT.NONE);
		totalGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
				true, false));
		totalGroup.setLayout(new GridLayout(4, false));
		totalGroup.setText(Messages.ExhibitLayoutDialog_total);
		new Label(totalGroup, SWT.NONE)
				.setText(Messages.ExhibitLayoutDialog_size);
		totalWidthField = new NumericControl(totalGroup, SWT.NONE);
		totalWidthField.setMaximum(10000);
		totalWidthField.setDigits(1);
		totalWidthField.setLogrithmic(true);
		totalWidthField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSizeFields(totalWidthField);
			}
		});
		new Label(totalGroup, SWT.NONE).setText(Messages.ExhibitLayoutDialog_x);
		totalHeightField = new NumericControl(totalGroup, SWT.NONE);
		totalHeightField.setMaximum(10000);
		totalHeightField.setDigits(1);
		totalHeightField.setLogrithmic(true);
		totalHeightField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSizeFields(totalHeightField);
			}
		});
		CGroup labelGroup = new CGroup(comp, SWT.NONE);
		labelGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false, 2, 1));
		labelGroup.setLayout(new GridLayout());
		labelGroup.setText(Messages.ExhibitLayoutDialog_label);
		labelLayoutGroup = new LabelLayoutGroup(labelGroup, SWT.NONE, false);
		labelLayoutGroup.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		new Label(comp, SWT.NONE);
		return area;
	}

	protected void validate() {
		String errorMessage = labelLayoutGroup.validate();
		getButton(OK).setEnabled(errorMessage == null);
		setErrorMessage(errorMessage);
	}

	protected void updateSizeFields(Widget widget) {
		if (widget == imageWidthField) {
			imageHeightField.setSelection(exhibit.getHeight()
					* imageWidthField.getSelection()
					/ Math.max(1, exhibit.getWidth()));
			updateTotal();
		} else if (widget == imageHeightField) {
			imageWidthField.setSelection(exhibit.getWidth()
					* imageHeightField.getSelection()
					/ Math.max(1, exhibit.getHeight()));
			updateTotal();
		} else if (widget == totalWidthField) {
			int tara = 2 * matWidthField.getSelection() + 2
					* frameWidthField.getSelection();
			totalHeightField.setSelection(exhibit.getHeight()
					* (totalWidthField.getSelection() - tara)
					/ Math.max(1, exhibit.getWidth()) + tara);
			updateImage(tara);
		} else if (widget == totalHeightField) {
			int tara = 2 * matWidthField.getSelection() + 2
					* frameWidthField.getSelection();
			totalWidthField.setSelection(exhibit.getWidth()
					* (totalHeightField.getSelection() - tara)
					/ Math.max(1, exhibit.getHeight()) + tara);
			updateImage(tara);
		}

	}

	private void updateImage(int tara) {
		imageWidthField.setSelection(totalWidthField.getSelection() - tara);
		imageHeightField.setSelection(totalHeightField.getSelection() - tara);
	}

	private void updateTotal() {
		int tara = 2 * matWidthField.getSelection() + 2
				* frameWidthField.getSelection();
		totalWidthField.setSelection(imageWidthField.getSelection() + tara);
		totalHeightField.setSelection(imageHeightField.getSelection() + tara);
		totalWidthField.setMinimum(tara);
		totalHeightField.setMinimum(tara);
	}

	private void updateColorGroups() {
		matColorGroup.setEnabled(matWidthField.getSelection() != 0);
		frameColorGroup.setEnabled(frameWidthField.getSelection() != 0);
	}

	private void fillValues() {
		int width = exhibit.getWidth();
		imageWidthField.setSelection(width);
		int height = exhibit.getHeight();
		imageHeightField.setSelection(height);
		Integer o = exhibit.getMatWidth();
		int matWidth = (o == null) ? exhibition.getMatWidth() : o;
		Rgb_type matColor = exhibit.getMatColor();
		if (matColor == null)
			matColor = exhibition.getMatColor();
		if (matColor == null)
			matColor = new Rgb_typeImpl(255, 255, 252);

		o = exhibit.getFrameWidth();
		int frameWidth = (o == null) ? exhibition.getFrameWidth() : o;
		Rgb_type frameColor = exhibit.getFrameColor();
		if (frameColor == null)
			frameColor = exhibition.getFrameColor();
		if (frameColor == null)
			frameColor = new Rgb_typeImpl(8, 8, 8);
		matWidthField.setSelection(matWidth);
		matColorGroup.setRGB(matColor);
		frameWidthField.setSelection(frameWidth);
		frameColorGroup.setRGB(frameColor);
		int tara = 2 * matWidth + 2 * frameWidth;
		totalWidthField.setSelection(width + tara);
		totalHeightField.setSelection(height + tara);
		totalWidthField.setMinimum(tara);
		totalHeightField.setMinimum(tara);
		Boolean h = exhibit.getHideLabel();
		Integer a = exhibit.getLabelAlignment();
		if (a == null)
			a = exhibition.getLabelAlignment();
		Integer d = exhibit.getLabelDistance();
		if (d == null)
			d = exhibition.getLabelDistance();
		Integer i = exhibit.getLabelIndent();
		if (i == null)
			i = exhibition.getLabelIndent();
		labelLayoutGroup.fillValues(
				h == null ? exhibition.getHideLabel() : h.booleanValue(),
				a == null ? Constants.DEFAULTLABELALIGNMENT : a.intValue(),
				d == null ? Constants.DEFAULTLABELDISTANCE : d.intValue(),
				i == null ? Constants.DEFAULTLABELINDENT : i.intValue());
		updateColorGroups();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DEFAULTBUTTON,
				Messages.ExhibitLayoutDialog_set_defaults, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == DEFAULTBUTTON) {
			exhibit.setMatWidth(null);
			exhibit.setMatColor(null);
			exhibit.setFrameWidth(null);
			exhibit.setFrameColor(null);
			exhibit.setHideLabel(null);
			exhibit.setLabelAlignment(null);
			exhibit.setLabelDistance(null);
			exhibit.setLabelIndent(null);
			okPressed();
			return;
		}
		if (buttonId == OK) {
			exhibit.setMatWidth(matWidthField.getSelection());
			exhibit.setMatColor(matColorGroup.getRGB());
			exhibit.setFrameWidth(frameWidthField.getSelection());
			exhibit.setFrameColor(frameColorGroup.getRGB());
			exhibit.setWidth(imageWidthField.getSelection());
			exhibit.setHeight(imageHeightField.getSelection());
			exhibit.setHideLabel(labelLayoutGroup.isHide());
			exhibit.setLabelAlignment(labelLayoutGroup.getAlign());
			exhibit.setLabelDistance(labelLayoutGroup.getDist());
			exhibit.setLabelIndent(labelLayoutGroup.getIndent());
		}
		super.buttonPressed(buttonId);
	}

}
