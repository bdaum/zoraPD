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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.SectionLayoutGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class EditSlideDialog extends ZTitleAreaDialog implements Listener {

	private static final int RESET = 9999;
	private final SlideImpl slide;
	private Text titleField;
	private NumericControl durationField;
	private NumericControl fadeinField;
	private Combo effectField;
	private NumericControl fadeoutField;
	private NumericControl delayField;
	private NumericControl zoomField;
	private Text imageField;
	private CheckboxButton voiceButton;
	private NumericControl zoomXField;
	private NumericControl zoomYField;
	private CheckedText descriptionField;
	private SectionLayoutGroup layoutGroup;
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
		fillValues();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(5, false));
		new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_title);
		titleField = new Text(comp, SWT.BORDER);
		titleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		titleField.addListener(SWT.Modify, this);
		if (slide.getAsset() != null) {
			new Label(comp, SWT.NONE).setText(Messages.EditSlideDialog_image);
			imageField = new Text(comp, SWT.BORDER | SWT.READ_ONLY);
			imageField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		} else {
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.EditSlideDialog_description);
			descriptionField = new CheckedText(comp, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
			layoutData.widthHint = 400;
			layoutData.heightHint = 100;
			descriptionField.setLayoutData(layoutData);
			layoutGroup = new SectionLayoutGroup(comp, 4);
			String slideId = slide.getStringId();
			boolean start = false;
			List<String> assetIds = new ArrayList<>();
			for (String id : show.getEntry()) {
				if (start) {
					SlideImpl slide = dbManager.obtainById(SlideImpl.class, id);
					if (slide != null) {
						String assetId = slide.getAsset();
						if (assetId == null)
							break;
						assetIds.add(assetId);
					}
				} else if (id.equals(slideId))
					start = true;
			}
			layoutGroup.setAssets(assetIds);
		}
		delayField = createNumericField(comp, 300, Messages.EditSlideDialog_delay);
		fadeinField = createNumericField(comp, 300, Messages.EditSlideDialog_fadein);
		new Label(comp, SWT.NONE).setText(Messages.SlideshowEditDialog_transition_effect);
		effectField = new Combo(comp, SWT.DROP_DOWN);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1);
		data.widthHint = 170;
		effectField.setLayoutData(data);
		effectField.setItems(SlideshowEditDialog.EFFECTS);
		if (slide.getAsset() != null) {
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
		}
		durationField = createNumericField(comp, 36000, Messages.EditSlideDialog_duration);
		fadeoutField = createNumericField(comp, 300, Messages.EditSlideDialog_fadeout);
		if (slide.getAsset() != null)
			voiceButton = WidgetFactory.createCheckButton(comp, Messages.EditSlideDialog_suppress_voicenote, null);
		return area;
	}

	private NumericControl createNumericField(final Composite comp, int max, String text) {
		new Label(comp, SWT.NONE).setText(text);
		NumericControl field = new NumericControl(comp, SWT.BORDER);
		field.setMinimum(0);
		field.setDigits(1);
		field.setMaximum(max);
		field.setIncrement(5);
		field.setPageIncrement(50);
		field.addListener(SWT.Selection, this);
		field.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
		return field;
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
				durationField.setSelection((int) (Math.max(show.getDuration(),
						CommonUtilities.computeHoverTime(slide.getCaption().length() + slide.getDescription().length()))
						/ 100d));
			else {
				durationField.setSelection((int) (show.getDuration() / 100d));
				voiceButton.setSelection(false);
			}
			effectField.select(show.getEffect());
			if (zoomField != null) {
				zoomField.setSelection(show.getZoom());
				zoomXField.setSelection(0);
				zoomYField.setSelection(0);
			}
			int fadin = (int) (show.getFading() / 100d);
			delayField.setSelection(fadin);
			fadeinField.setSelection(fadin);
			fadeoutField.setSelection(fadin);
			return;
		}
		super.buttonPressed(buttonId);
	}

	private void fillValues() {
		titleField.setText(slide.getCaption() == null ? "" : slide.getCaption()); //$NON-NLS-1$
		durationField.setSelection((int) (show.getDuration() / 100d));
		effectField.select(slide.getEffect() - Constants.SLIDE_TRANSITION_START);
		if (zoomField != null) {
			zoomField.setSelection(slide.getZoom());
			zoomXField.setSelection(slide.getZoomX());
			zoomYField.setSelection(slide.getZoomY());
		}
		delayField.setSelection((int) (slide.getDelay() / 100d));
		fadeinField.setSelection((int) (slide.getFadeIn() / 100d));
		fadeoutField.setSelection((int) (slide.getFadeOut() / 100d));
		if (voiceButton != null)
			voiceButton.setSelection(slide.getNoVoice());
		String assetID = slide.getAsset();
		if (assetID != null) {
			AssetImpl asset = Core.getCore().getDbManager().obtainAsset(assetID);
			imageField.setText(asset != null ? asset.getName() : Messages.EditSlideDialog_deleted);
		} else {
			descriptionField.setText(slide.getDescription());
			layoutGroup.setSelection(slide.getLayout());
		}
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			getShell().setModified(!readonly);
			okButton.setEnabled(!readonly);
		}
	}

	@Override
	protected void okPressed() {
		slide.setCaption(titleField.getText());
		slide.setDuration(durationField.getSelection() * 100);
		slide.setDelay(delayField.getSelection() * 100);
		slide.setFadeIn(fadeinField.getSelection() * 100);
		slide.setFadeOut(fadeoutField.getSelection() * 100);
		slide.setEffect(effectField.getSelectionIndex() < 0 ? Constants.SLIDE_TRANSITION_FADE
				: effectField.getSelectionIndex() + Constants.SLIDE_TRANSITION_START);
		if (zoomField != null) {
			slide.setZoom(zoomField.getSelection());
			slide.setZoomX(zoomXField.getSelection());
			slide.setZoomY(zoomYField.getSelection());
		} else
			slide.setZoom(0);
		if (slide.getAsset() != null)
			slide.setNoVoice(voiceButton.getSelection());
		else {
			slide.setDescription(descriptionField.getText());
			slide.setLayout(Math.max(0, layoutGroup.getSelection()));
		}
		Core.getCore().getDbManager().safeTransaction(null, slide);
		super.okPressed();
	}
	
	@Override
	public void handleEvent(Event event) {
		updateButtons();
	}


}
