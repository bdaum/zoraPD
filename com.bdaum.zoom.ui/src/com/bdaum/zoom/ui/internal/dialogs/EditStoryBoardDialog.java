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
import org.eclipse.jface.dialogs.IDialogSettings;
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

import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.DescriptionGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class EditStoryBoardDialog extends ZTitleAreaDialog {

	private static final String IMAGE_SIZE = "imageSize"; //$NON-NLS-1$
	private static final String HIDE_DESCRIPTION = "hideDescription"; //$NON-NLS-1$
	private static final String HIDE_CAPTION = "hideCaption"; //$NON-NLS-1$
	private static final String SETTINGSID = "com.bdaum.zoom.storyboardProperties"; //$NON-NLS-1$
	private final String title;
	private StoryboardImpl current;
	private Text titleField;

	private final ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};
	private CheckboxButton showCaptionButton;
	private CheckboxButton showDescriptionButton;
	private Combo imagesizeField;
	private WebGallery gallery;
	private CheckboxButton enlargeButton;
	private CheckboxButton showExifButton;
	private IDialogSettings settings;
	private DescriptionGroup descriptionGroup;

	public EditStoryBoardDialog(Shell parentShell, WebGallery webGallery, StoryboardImpl current, String title) {
		super(parentShell, HelpContextIds.EDITSTORYBOARD_DIALOG);
		this.gallery = webGallery;
		this.current = current;
		this.title = title;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.EditStoryBoardDialog_edit_storyboard_properties);
		updateButtons();
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		new Label(comp, SWT.NONE).setText(Messages.EditStoryBoardDialog_section_title);
		titleField = new Text(comp, SWT.BORDER);
		titleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleField.addModifyListener(modifyListener);
		new Label(comp, SWT.NONE);
		showCaptionButton = WidgetFactory.createCheckButton(comp, Messages.EditStoryBoardDialog_show_captions, null);
		descriptionGroup = new DescriptionGroup(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		showDescriptionButton = WidgetFactory.createCheckButton(comp, Messages.EditStoryBoardDialog_show_descriptions,
				null);
		new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite sizeGroup = new Composite(comp, SWT.NONE);
		sizeGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		GridLayout layout = new GridLayout(4, false);
		layout.marginWidth = layout.marginHeight = 0;
		sizeGroup.setLayout(layout);
		new Label(sizeGroup, SWT.NONE).setText(Messages.EditStoryBoardDialog_image_size);
		imagesizeField = new Combo(sizeGroup, SWT.READ_ONLY);
		imagesizeField.setItems(new String[] { Messages.EditStoryBoardDialog_medium,
				Messages.EditStoryBoardDialog_large, Messages.EditStoryBoardDialog_vary_large,
				Messages.EditStoryBoardDialog_small, Messages.EditStoryBoardDialog_very_small });
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalIndent = 15;
		enlargeButton = WidgetFactory.createCheckButton(sizeGroup, Messages.EditStoryBoardDialog_enlarge_small_images,
				data);
		showExifButton = WidgetFactory.createCheckButton(sizeGroup, Messages.EditStoryBoardDialog_show_exif_data,
				new GridData(SWT.END, SWT.CENTER, false, false));
		showExifButton.setEnabled(gallery.getShowMeta());
		fillValues();
		return area;
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		if (current != null) {
			setText(titleField, current.getTitle());
			descriptionGroup.setText(current.getDescription(), current.getHtmlDescription());
			showCaptionButton.setSelection(current.getShowCaptions());
			showDescriptionButton.setSelection(current.getShowDescriptions());
			showExifButton.setSelection(current.getShowExif());
			int imageSize = Math.max(0, Math.min(imagesizeField.getItemCount() - 1, current.getImageSize()));
			imagesizeField.select(imageSize);
			imagesizeField.setText(imagesizeField.getItem(imageSize));
			enlargeButton.setSelection(current.getEnlargeSmall());
		} else {
			titleField.setText(title);
			showCaptionButton.setSelection(!settings.getBoolean(HIDE_CAPTION));
			showDescriptionButton.setSelection(!settings.getBoolean(HIDE_DESCRIPTION));
			showExifButton.setSelection(gallery.getShowMeta());
			try {
				int imageSize = settings.getInt(IMAGE_SIZE);
				imageSize = Math.min(imagesizeField.getItemCount() - 1, Math.max(0, imageSize));
				imagesizeField.select(imageSize);
				imagesizeField.setText(imagesizeField.getItem(imageSize));
			} catch (NumberFormatException e) {
				imagesizeField.select(0);
				imagesizeField.setText(imagesizeField.getItem(0));
			}
		}
	}

	public Storyboard getResult() {
		return current;
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
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void okPressed() {
		if (current == null)
			current = new StoryboardImpl();
		current.setTitle(titleField.getText().trim());
		current.setHtmlDescription(descriptionGroup.isHtml());
		current.setDescription(descriptionGroup.getText());
		current.setShowCaptions(showCaptionButton.getSelection());
		current.setShowDescriptions(showDescriptionButton.getSelection());
		current.setShowExif(showExifButton.getSelection());
		current.setImageSize(imagesizeField.getSelectionIndex());
		current.setEnlargeSmall(enlargeButton.getSelection());
		saveValue(current);
		super.okPressed();
	}

	private void saveValue(StoryboardImpl sb) {
		settings.put(HIDE_CAPTION, !sb.getShowCaptions());
		settings.put(HIDE_DESCRIPTION, !sb.getShowDescriptions());
		settings.put(IMAGE_SIZE, sb.getImageSize());
	}

}
