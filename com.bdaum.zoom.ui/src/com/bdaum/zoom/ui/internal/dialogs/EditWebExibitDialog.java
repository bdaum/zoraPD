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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.DescriptionGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class EditWebExibitDialog extends ZTitleAreaDialog {

	private WebExhibitImpl current;
	private final String title;
	private Text captionField;
	private Text altField;
	private CheckboxButton downloadableButton;
	private CheckboxButton includeMetadataButton;
	private final WebGallery gallery;
	private Text imageField;
	private DescriptionGroup descriptionGroup;

	public EditWebExibitDialog(Shell parentShell, WebExhibitImpl current,
			String title, WebGallery gallery) {
		super(parentShell, HelpContextIds.EDITWEBEXHIBIT_DIALOG);
		this.current = current;
		this.title = title;
		this.gallery = gallery;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.EditWebExibitDialog_edit_specific_image_properties);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		new Label(comp, SWT.NONE).setText(Messages.EditWebExibitDialog_caption);
		captionField = new Text(comp, SWT.BORDER);
		captionField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		new Label(comp, SWT.NONE).setText(Messages.EditWebExibitDialog_image);
		imageField = new Text(comp, SWT.BORDER | SWT.READ_ONLY);
		imageField
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		descriptionGroup = new DescriptionGroup(comp, SWT.NONE);
		new Label(comp, SWT.NONE).setText(Messages.EditWebExibitDialog_html_alt_text);
		altField = new Text(comp, SWT.BORDER);
		altField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		downloadableButton = WidgetFactory.createCheckButton(comp,
				Messages.EditWebExibitDialog_downloadable, new GridData(
						SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		downloadableButton.setEnabled(gallery.getDownloadText() != null
				&& !gallery.getDownloadText().isEmpty()
				&& !gallery.getHideDownload());
		includeMetadataButton = WidgetFactory.createCheckButton(comp,
				Messages.EditWebExibitDialog_include_metadata, new GridData(
						SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		fillValues();
		return area;
	}

	private void fillValues() {
		setText(captionField, current.getCaption());
		descriptionGroup.setText(current.getDescription(), current.getHtmlDescription());
		setText(altField, current.getAltText());
		downloadableButton.setSelection(current.getDownloadable());
		includeMetadataButton.setSelection(current.getIncludeMetadata());
		String imageName;
		String assetID = current.getAsset();
		if (assetID == null)
			imageName = " - "; //$NON-NLS-1$
		else {
			AssetImpl asset = Core.getCore().getDbManager()
					.obtainAsset(assetID);
			if (asset != null)
				imageName = asset.getName();
			else
				imageName = Messages.EditWebExibitDialog_deleted;
		}
		imageField.setText(imageName);
		captionField.selectAll();
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
		current.setCaption(captionField.getText().trim());
		current.setDescription(descriptionGroup.getText());
		current.setHtmlDescription(descriptionGroup.isHtml());
		current.setAltText(altField.getText().trim());
		current.setDownloadable(downloadableButton.getSelection());
		current.setIncludeMetadata(includeMetadataButton.getSelection());
		super.okPressed();
	}

}
