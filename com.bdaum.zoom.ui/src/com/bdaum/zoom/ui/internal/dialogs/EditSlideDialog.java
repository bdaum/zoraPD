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
 * (c) 2009-2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class EditSlideDialog extends ZTitleAreaDialog {

	private static final int RESET = 9999;
	private final SlideImpl slide;
	private Text titleField;
	private Text durationField;
	private Text fadinField;
	private Combo effectField;
	private Text fadoutField;
	private Text delayField;
	private NumericControl zoomField;

	private static NumberFormat af = (NumberFormat.getNumberInstance());
	private SlideShow show;

	public EditSlideDialog(Shell parentShell, SlideImpl slide, SlideShow show) {
		super(parentShell, HelpContextIds.SLIDE_DIALOG);
		this.slide = slide;
		this.show = show;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.EditSlideDialog_slide_properties);
		setMessage(NLS.bind(Messages.EditSlideDialog_please_specify_inidividual, slide.getCaption()));
		updateButtons();
	}

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};
	private Text imageField;
	private CheckboxButton voiceButton;
	private NumericControl zoomXField;
	private NumericControl zoomYField;
	private CheckedText descriptionField;
	private Combo thumbnailField;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(5, false));
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_title);
		titleField = new Text(comp, SWT.BORDER);
		titleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		titleField.addModifyListener(modifyListener);
		if (slide.getAsset() != null) {
			new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_image);
			imageField = new Text(comp, SWT.BORDER | SWT.READ_ONLY);
			imageField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		} else {
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.SectionBreakDialog_description);
			descriptionField = new CheckedText(comp, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
			layoutData.widthHint = 400;
			layoutData.heightHint = 100;
			descriptionField.setLayoutData(layoutData);
			new Label(comp, SWT.NONE).setText(Messages.SectionBreakDialog_thumbnails);
			thumbnailField = new Combo(comp, SWT.DROP_DOWN);
			thumbnailField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
			thumbnailField.setItems(SectionBreakDialog.THUMBNAILS);
		}
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_delay);
		delayField = new Text(comp, SWT.BORDER);
		delayField.addModifyListener(modifyListener);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 40;
		delayField.setLayoutData(data);
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_fadein);
		fadinField = new Text(comp, SWT.BORDER);
		fadinField.addModifyListener(modifyListener);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 40;
		fadinField.setLayoutData(data);
		new Label(comp, SWT.NONE).setText(Messages.SlideshowEditDialog_transition_effect);
		effectField = new Combo(comp, SWT.DROP_DOWN);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 170;
		effectField.setLayoutData(data);
		effectField.setItems(SlideshowEditDialog.EFFECTS);
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_zoom_in);
		zoomField = new NumericControl(comp, SWT.NONE);
		zoomField.setMaximum(100);
		Label lab = new Label(comp, SWT.NONE);
		lab.setText(Messages.EditSlideDialog_zoom_dir);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.horizontalIndent = 20;
		lab.setLayoutData(data);
		zoomXField = new NumericControl(comp, SWT.NONE);
		zoomXField.setMaximum(100);
		zoomXField.setMinimum(-100);
		zoomYField = new NumericControl(comp, SWT.NONE);
		zoomYField.setMaximum(100);
		zoomYField.setMinimum(-100);
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_duration);
		durationField = new Text(comp, SWT.BORDER);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 40;
		durationField.setLayoutData(data);
		durationField.addModifyListener(modifyListener);
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_fadeout);
		fadoutField = new Text(comp, SWT.BORDER);
		fadoutField.addModifyListener(modifyListener);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 40;
		fadoutField.setLayoutData(data);
		if (slide.getAsset() != null)
			voiceButton = WidgetFactory.createCheckButton(comp, Messages.EditSlideDialog_suppress_voicenote, null);
		fillValues();
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (!readonly)
			createButton(parent, RESET, Messages.EditSlideDialog_reset, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == RESET) {
			if (slide.getAsset() == null)
				durationField.setText(af.format(Math.max(show.getDuration(),
						CommonUtilities.computeHoverTime(slide.getCaption().length() + slide.getDescription().length()))
						/ 1000d));
			else {
				durationField.setText(af.format(show.getDuration() / 1000d));
				voiceButton.setSelection(false);
			}
			effectField.select(show.getEffect());
			zoomField.setSelection(show.getZoom());
			zoomXField.setSelection(0);
			zoomYField.setSelection(0);
			delayField.setText(af.format(show.getFading() / 1000d));
			fadinField.setText(af.format(show.getFading() / 1000d));
			fadoutField.setText(af.format(show.getFading() / 1000d));
			return;
		}
		super.buttonPressed(buttonId);
	}

	private void fillValues() {
		titleField.setText(slide.getCaption() == null ? "" : slide.getCaption()); //$NON-NLS-1$
		durationField.setText(af.format(slide.getDuration() / 1000d));
		effectField.select(slide.getEffect());
		zoomField.setSelection(slide.getZoom());
		zoomXField.setSelection(slide.getZoomX());
		zoomYField.setSelection(slide.getZoomY());
		delayField.setText(af.format(slide.getDelay() / 1000d));
		fadinField.setText(af.format(slide.getFadeIn() / 1000d));
		fadoutField.setText(af.format(slide.getFadeOut() / 1000d));
		if (voiceButton != null)
			voiceButton.setSelection(slide.getNoVoice());
		String assetID = slide.getAsset();
		if (assetID != null) {
			AssetImpl asset = Core.getCore().getDbManager().obtainAsset(assetID);
			imageField.setText(asset != null ? asset.getName() : Messages.EditSlideDialog_deleted);
		} else {
			descriptionField.setText(slide.getDescription());
			thumbnailField.select(slide.getLayout());
		}
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled = validate() && !readonly;
			getShell().setModified(enabled);
			okButton.setEnabled(enabled);
		}
	}

	private boolean validate() {
		if (!validDouble(durationField, Messages.EditSlideDialog_duration_err, 0d, 3600d))
			return false;
		if (!validDouble(fadinField, Messages.EditSlideDialog_fadein_err, 0d, 30d))
			return false;
		if (!validDouble(fadoutField, Messages.EditSlideDialog_fadeout_err, 0d, 30d))
			return false;
		if (!validDouble(delayField, Messages.EditSlideDialog_delay_err, 0d, 30d))
			return false;
		setErrorMessage(null);
		return true;
	}

	private boolean validDouble(Text field, String label, double min, double max) {
		String s = field.getText();
		try {
			double v = af.parse(s).doubleValue();
			if (v > max)
				setErrorMessage(NLS.bind(Messages.EditSlideDialog_value_must_not_be_larger_than, label, min));
			else if (v >= min)
				return true;
			setErrorMessage(NLS.bind(Messages.EditSlideDialog_value_must_be_larger, label, min));
		} catch (ParseException e) {
			setErrorMessage(NLS.bind(Messages.EditSlideDialog_value_is_not_a_number, label));
		}
		return false;
	}

	@Override
	protected void okPressed() {
		slide.setCaption(titleField.getText());
		slide.setDuration(stringToMsec(durationField));
		slide.setDelay(stringToMsec(delayField));
		slide.setFadeIn(stringToMsec(fadinField));
		slide.setFadeOut(stringToMsec(fadoutField));
		slide.setEffect(Math.max(0, effectField.getSelectionIndex()));
		slide.setZoom(zoomField.getSelection());
		slide.setZoomX(zoomXField.getSelection());
		slide.setZoomY(zoomYField.getSelection());
		if (slide.getAsset() != null)
			slide.setNoVoice(voiceButton.getSelection());
		else {
			slide.setDescription(descriptionField.getText());
			slide.setLayout(Math.max(0, thumbnailField.getSelectionIndex()));
		}
		Core.getCore().getDbManager().safeTransaction(null, slide);
		super.okPressed();
	}

	private static int stringToMsec(Text field) {
		try {
			return (int) (1000 * af.parse(field.getText()).doubleValue());
		} catch (ParseException e) {
			// should not happen
			return 0;
		}
	}

}
