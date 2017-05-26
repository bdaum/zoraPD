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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
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
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class EditSlideDialog extends ZTitleAreaDialog {

	private final SlideImpl slide;
	private Text titleField;
	private Text durationField;
	private Text fadinField;
	private Combo effectField;
	private Text fadoutField;
	private Text delayField;

	public static final String[] EFFECTS = new String[] {
			Messages.SlideshowEditDialog_Fade,
			Messages.SlideshowEditDialog_move_left,
			Messages.SlideshowEditDialog_move_right,
			Messages.SlideshowEditDialog_move_up,
			Messages.SlideshowEditDialog_move_down };

	private static NumberFormat af = (NumberFormat.getNumberInstance());

	public EditSlideDialog(Shell parentShell, SlideImpl slide) {
		super(parentShell, HelpContextIds.SLIDE_DIALOG);
		this.slide = slide;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.EditSlideDialog_slide_properties);
		setMessage(NLS.bind(
				Messages.EditSlideDialog_please_specify_inidividual,
				slide.getCaption()));
		updateButtons();
	}

	private final ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};
	private Text imageField;
	private CheckboxButton voiceButton;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		final Label titleLabel = new Label(comp, SWT.NONE);
		titleLabel.setText(Messages.EditSlideDialog_title);

		titleField = new Text(comp, SWT.BORDER);
		titleField
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleField.addModifyListener(modifyListener);

		final Label imageLabel = new Label(comp, SWT.NONE);
		imageLabel.setText(Messages.EditSlideDialog_image);

		imageField = new Text(comp, SWT.BORDER | SWT.READ_ONLY);
		imageField
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label delayLabel = new Label(comp, SWT.NONE);
		delayLabel.setText(Messages.EditSlideDialog_delay);

		delayField = new Text(comp, SWT.BORDER);
		delayField.addModifyListener(modifyListener);
		GridData data = new GridData(40, SWT.DEFAULT);
		delayField.setLayoutData(data);

		final Label fadinsecLabel = new Label(comp, SWT.NONE);
		fadinsecLabel.setText(Messages.EditSlideDialog_fadein);

		fadinField = new Text(comp, SWT.BORDER);
		fadinField.addModifyListener(modifyListener);
		data = new GridData(40, SWT.DEFAULT);
		fadinField.setLayoutData(data);

		final Label effectLabel = new Label(comp, SWT.NONE);
		effectLabel.setText(Messages.SlideshowEditDialog_transition_effect);
		effectField = new Combo(comp, SWT.DROP_DOWN);
		data = new GridData(70, SWT.DEFAULT);
		effectField.setLayoutData(data);
		effectField.setItems(EFFECTS);

		final Label durationLabel = new Label(comp, SWT.NONE);
		durationLabel.setText(Messages.EditSlideDialog_duration);

		durationField = new Text(comp, SWT.BORDER);
		durationField.setLayoutData(new GridData(40, SWT.DEFAULT));
		durationField.addModifyListener(modifyListener);

		final Label fadeoutLabel = new Label(comp, SWT.NONE);
		fadeoutLabel.setText(Messages.EditSlideDialog_fadeout);

		fadoutField = new Text(comp, SWT.BORDER);
		fadoutField.addModifyListener(modifyListener);
		data = new GridData(40, SWT.DEFAULT);
		fadoutField.setLayoutData(data);
		voiceButton = WidgetFactory.createCheckButton(comp, Messages.EditSlideDialog_suppress_voicenote, null);
		fillValues();

		return area;
	}

	private void fillValues() {
		titleField
				.setText(slide.getCaption() == null ? "" : slide.getCaption()); //$NON-NLS-1$
		durationField.setText(af.format(slide.getDuration() / 1000d));
		effectField.select(slide.getEffect());
		delayField.setText(af.format(slide.getDelay() / 1000d));
		fadinField.setText(af.format(slide.getFadeIn() / 1000d));
		fadoutField.setText(af.format(slide.getFadeOut() / 1000d));
		voiceButton.setSelection(slide.getNoVoice());
		String imageName;
		String assetID = slide.getAsset();
		if (assetID == null)
			imageName = " - "; //$NON-NLS-1$
		else {
			AssetImpl asset = Core.getCore().getDbManager()
					.obtainAsset(assetID);
			if (asset != null)
				imageName = asset.getName();
			else
				imageName = Messages.EditSlideDialog_deleted;
		}
		imageField.setText(imageName);
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
		if (!validDouble(durationField, Messages.EditSlideDialog_duration_err,
				0d, 3600d))
			return false;
		if (!validDouble(fadinField, Messages.EditSlideDialog_fadein_err, 0d,
				30d))
			return false;
		if (!validDouble(fadoutField, Messages.EditSlideDialog_fadeout_err, 0d,
				30d))
			return false;
		if (!validDouble(delayField, Messages.EditSlideDialog_delay_err, 0d,
				30d))
			return false;
		setErrorMessage(null);
		return true;
	}

	private boolean validDouble(Text field, String label, double min, double max) {
		String s = field.getText();
		try {
			double v = af.parse(s).doubleValue();
			if (v > max) {
				setErrorMessage(NLS.bind(
						Messages.EditSlideDialog_value_must_not_be_larger_than,
						label, min));
			} else if (v >= min)
				return true;
			setErrorMessage(NLS.bind(
					Messages.EditSlideDialog_value_must_be_larger, label, min));
		} catch (ParseException e) {
			setErrorMessage(NLS.bind(
					Messages.EditSlideDialog_value_is_not_a_number, label));
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
		slide.setNoVoice(voiceButton.getSelection());
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
