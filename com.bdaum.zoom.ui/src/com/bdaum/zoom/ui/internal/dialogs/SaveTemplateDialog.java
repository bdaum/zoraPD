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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.aoModeling.runtime.AomMap;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Font_typeImpl;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameterImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.internal.swt.ImageLoader;

public class SaveTemplateDialog extends ZTrayDialog {

	private static final String MESSAGE = Messages.SaveTemplateDialog_you_can_ssve_the_design;
	private final File start;
	private final WebGalleryImpl show;
	private Browser browser;
	private Text nameField;
	private WebGalleryImpl template;
	private Label msgLabel;
	private Composite browserComp;

	public SaveTemplateDialog(Shell parentShell, File start, WebGalleryImpl show) {
		super(parentShell);
		this.start = start;
		this.show = show;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setAlpha(244);
	}

	@Override
	public void create() {
		super.create();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(3, false));
		browserComp = new Composite(comp, SWT.NONE);
		browserComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		browserComp.setLayout(new FillLayout());
		browser = new Browser(browserComp, SWT.NONE);
		browser.setJavascriptEnabled(true);
		Label sep = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		msgLabel = new Label(comp, SWT.WRAP);
		msgLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		msgLabel.setText(MESSAGE);
		Label nameLabel = new Label(comp, SWT.NONE);
		GridData data = new GridData(SWT.END, SWT.CENTER, false, false);
		data.horizontalIndent = 30;
		nameLabel.setLayoutData(data);
		nameLabel.setText(Messages.SaveTemplateDialog_design_name);
		nameField = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		data = new GridData(SWT.END, SWT.CENTER, false, false);
		data.widthHint = 250;
		nameField.setLayoutData(data);
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});

		try {
			browser.setUrl(start.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			// should not happen
		}

		return area;
	}

	protected void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		String name = nameField.getText().trim();
		if (name.isEmpty()) {
			msgLabel.setText(Messages.SaveTemplateDialog_please_specify_a_design_name);
			msgLabel.setForeground(msgLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			getShell().setModified(false);
			okButton.setEnabled(false);
			return;
		}
		getShell().setModified(true);
		okButton.setEnabled(true);
		List<IdentifiableObject> set = Core.getCore().getDbManager().obtainObjects(WebGalleryImpl.class, false, "name", //$NON-NLS-1$
				name, QueryField.EQUALS, "template", Boolean.TRUE, QueryField.EQUALS); //$NON-NLS-1$
		if (!set.isEmpty()) {
			msgLabel.setText(Messages.SaveTemplateDialog_a_design_with_that_name_already_exists);
			msgLabel.setForeground(msgLabel.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
			okButton.setText(Messages.SaveTemplateDialog_overwrite);
		} else {
			msgLabel.setText(MESSAGE);
			msgLabel.setForeground(msgLabel.getParent().getForeground());
			okButton.setText(IDialogConstants.OK_LABEL);
		}
	}

	@Override
	protected void okPressed() {
		template = new WebGalleryImpl();
		template.setTemplate(true);
		template.setName(nameField.getText().trim());
		// Copy values
		template.setAddWatermark(show.getAddWatermark());
		template.setBgColor(cloneColor(show.getBgColor()));
		template.setBgImage(show.getBgImage());
		template.setBgRepeat(show.getBgRepeat());
		template.setBorderColor(cloneColor(show.getBorderColor()));
		template.setCaptionFont(cloneFont(show.getCaptionFont()));
		template.setContactName(show.getContactName());
		template.setCopyright(show.getCopyright());
		template.setDescriptionFont(cloneFont(show.getDescriptionFont()));
		template.setDownloadText(show.getDownloadText());
		template.setHideDownload(show.getHideDownload());
		template.setEmail(show.getEmail());
		template.setFooterFont(cloneFont(show.getFooterFont()));
		template.setLinkColor(cloneColor(show.getLinkColor()));
		template.setLogo(show.getLogo());
		template.setOpacity(show.getOpacity());
		template.setPadding(show.getPadding());
		AomMap<String, WebParameter> parameters = show.getParameter();
		for (WebParameter parm : parameters.values())
			template.putParameter(
					new WebParameterImpl(parm.getId(), parm.getValue(), parm.getEncodeHtml(), parm.getLinkTo()));
		template.setSectionFont(cloneFont(show.getSectionFont()));
		template.setSelectedEngine(show.getSelectedEngine());
		template.setShadeColor(cloneColor(show.getShadeColor()));
		template.setShowMeta(show.getShowMeta());
		template.setThumbSize(show.getThumbSize());
		template.setTitleFont(cloneFont(show.getTitleFont()));
		template.setWebUrl(show.getWebUrl());
		template.setPoweredByText(show.getPoweredByText());
		Image preview = screenshot(browserComp, 300);
		try {
			ImageLoader swtLoader = new ImageLoader();
			swtLoader.data = new ImageData[] { preview.getImageData() };
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			swtLoader.save(bout, SWT.IMAGE_JPEG);
			byte[] bytes = bout.toByteArray();
			template.setPreview(bytes);
		} finally {
			preview.dispose();
		}
		super.okPressed();
	}

	private static Font_type cloneFont(Font_type f) {
		return new Font_typeImpl(f.getSize(), f.getStyle(), f.getWeight(), f.getVariant(), cloneColor(f.getColor()));
	}

	private static Rgb_type cloneColor(Rgb_type c) {
		return new Rgb_typeImpl(c.getR(), c.getG(), c.getB());
	}

	public WebGalleryImpl getResult() {
		return template;
	}

	private static Image screenshot(Control control, int maxSize) {
		GC gc = new GC(control);
		Rectangle bounds = control.getBounds();
		Display display = control.getDisplay();
		Image image = new Image(display, bounds.width, bounds.height);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		Rectangle iBounds = image.getBounds();
		int owidth = iBounds.width - 16;
		int oheight = iBounds.height;
		double scale = ImageUtilities.computeScale(owidth, iBounds.height, maxSize, maxSize);
		int newWidth = (int) (owidth * scale + 0.5d);
		int newHeight = (int) (iBounds.height * scale + 0.5d);
		Image thumbnail = new Image(image.getDevice(), newWidth, newHeight);
		gc = new GC(thumbnail);
		try {
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
//			gc.setAdvanced(true);
			gc.drawImage(image, 0, 0, owidth, oheight, 0, 0, newWidth, newHeight);
			return thumbnail;
		} finally {
			image.dispose();
			gc.dispose();
		}
	}
}
