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
 * (c) 2009-2021 Berthold Daum  
 */

package com.bdaum.zoom.email.internal;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.internal.dialogs.OutputTargetGroup;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.wizards.ExportModeGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class PDFTargetFilePage extends ColoredWizardPage implements Listener {

	private static final String PDFPATH = "pdfPath"; //$NON-NLS-1$
	private static final String HTMLPATH = "htmlPath"; //$NON-NLS-1$
	private static final String WEBLINK = "weblink"; //$NON-NLS-1$
	private String path;
	private final String type;
	private FileEditor fileEditor;
	private QualityGroup qualityGroup;
	private IDialogSettings dialogSettings;
	private final List<Asset> assets;
	private OutputTargetGroup outputTargetGroup;
	private Text linkField;
	private boolean pdf;
	private ExportModeGroup exportModeGroup;

	protected PDFTargetFilePage(String type, List<Asset> assets) {
		super("targetFile"); //$NON-NLS-1$
		this.type = type;
		pdf = !"HTML".equals(type); //$NON-NLS-1$
		this.assets = assets;
	}

	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(pdf ? PDFPATH : HTMLPATH);
		Composite composite = createComposite(parent, 3);
		String[] filterExtensions;
		String[] filterNames;
		if (pdf) {
			filterExtensions = new String[] { "*.pdf;*.PDF", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
			filterNames = new String[] { Messages.PDFTargetFilePage_portable_document_format,
					Messages.PDFTargetFilePage_all_files };
			fileEditor = new FileEditor(composite, SWT.SAVE | SWT.READ_ONLY, Messages.PDFTargetFilePage_target_file,
					true, filterExtensions, filterNames, path, "*.pdf", false, dialogSettings); //$NON-NLS-1$
			fileEditor.addListener(SWT.Modify, this);
		} else {
			outputTargetGroup = new OutputTargetGroup(composite,
					new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1), this, false, true, false);
			new Label(composite, SWT.NONE).setText(Messages.PDFTargetFilePage_weblink);
			linkField = new Text(composite, SWT.BORDER);
			linkField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}

		new Label(composite, SWT.NONE).setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		setControl(composite);
		if (pdf) {
			setHelp(HelpContextIds.PDF_WIZARD);
			qualityGroup = new QualityGroup(composite, pdf, false);
		} else {
			setHelp(HelpContextIds.HTML_WIZARD);
			exportModeGroup = new ExportModeGroup(composite, ExportModeGroup.JPEG | ExportModeGroup.WEBP);
		}
		setTitle(Messages.PDFTargetFilePage_target_file);
		setMessage(NLS.bind(Messages.PDFTargetFilePage_set_target_file, type));
		fillValues();
		super.createControl(parent);
	}

	public void handleEvent(Event event) {
		if (pdf)
			path = fileEditor.getFilterPath();
		validatePage();
	}

	private void fillValues() {
		if (qualityGroup != null)
			qualityGroup.fillValues(dialogSettings);
		if (exportModeGroup != null)
			exportModeGroup.fillValues(dialogSettings);
		if (outputTargetGroup != null)
			outputTargetGroup.initValues(dialogSettings);
		if (linkField != null) {
			String s = dialogSettings.get(WEBLINK);
			linkField.setText(s == null ? "index.html" : s); //$NON-NLS-1$
		}

	}

	@Override
	protected String validate() {
		String targetFile = getTargetFile();
		if (assets.isEmpty())
			return Messages.PDFTargetFilePage_no_image_selected;
		if (fileEditor != null) {
			if (targetFile.isEmpty())
				return Messages.PDFTargetFilePage_file_name_empty;
			if (targetFile.indexOf('*') >= 0 || targetFile.indexOf('?') >= 0)
				return Messages.PDFTargetFilePage_please_specify_target_file;
		} else
			return outputTargetGroup.validate();
		return null;
	}

	public String getTargetFile() {
		return fileEditor != null ? fileEditor.getText().trim()
				: outputTargetGroup.getTarget() == Constants.FILE ? outputTargetGroup.getLocalFolder() : null;
	}

	public FtpAccount getFtpAccount() {
		return outputTargetGroup == null ? null : outputTargetGroup.getFtpDir();
	}

	public String getWeblink() {
		if (linkField == null)
			return null;
		String weblink = linkField.getText().trim();
		return weblink.isEmpty() ? "index.html" : weblink; //$NON-NLS-1$
	}

	public int getQuality() {
		return qualityGroup != null ? qualityGroup.getQuality() : exportModeGroup.getQuality();
	}

	public UnsharpMask getUnsharpMask() {
		return qualityGroup != null ? qualityGroup.getUnsharpMask() : exportModeGroup.getUnsharpMask();
	}

	public boolean finish() {
		if (fileEditor == null || fileEditor.testSave()) {
			dialogSettings.put(pdf ? PDFPATH : HTMLPATH, path);
			if (qualityGroup != null)
				qualityGroup.saveSettings(dialogSettings);
			if (exportModeGroup != null)
				exportModeGroup.saveSettings(dialogSettings);
			if (outputTargetGroup != null)
				outputTargetGroup.saveValues(dialogSettings);
			if (linkField != null)
				dialogSettings.put(WEBLINK, linkField.getText());
			if (fileEditor != null)
				fileEditor.saveValues();
			return true;
		}
		return false;
	}

	public int getJpegQuality() {
		return qualityGroup != null ? qualityGroup.getJpegQuality() : exportModeGroup.getJpegQuality();
	}

	public int getMode() {
		return exportModeGroup != null ? exportModeGroup.getMode() : Constants.FORMAT_JPEG;
	}

}
