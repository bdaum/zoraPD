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
 * (c) 2021 Berthold Daum  
 */

package com.bdaum.zoom.email.internal;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.PrivacyGroup;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.widgets.WatermarkGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ExportModeGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ProcessingPage extends ColoredWizardPage implements Listener {

	private List<Asset> assets;
	private Label mailSizeLabel, imageSizeLabel;
	private long totalSize;
	private int commonWidth = -1, commonHeight = -1;
	private final boolean pdf, multiMedia;
	private CheckboxButton metaButton, trackExportButton;
	private WatermarkGroup watermarkGroup;
	private PrivacyGroup privacyGroup;
	private ExportModeGroup exportModeGroup;
	private QualityGroup qualityGroup;

	public ProcessingPage(List<Asset> assets, boolean pdf) {
		super("main", pdf ? Messages.SendEmailPage_send_pdf_per_email //$NON-NLS-1$
				: Messages.SendEmailPage_Send_per_email, null);
		multiMedia = Core.getCore().isMultiMedia(this.assets = assets);
		this.pdf = pdf;
	}

	@Override
	public void createControl(Composite parent) {
		int size = assets.size();
		Composite composite = createComposite(parent, 1);
		Label imagesLabel = new Label(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalIndent = 5;
		imagesLabel.setLayoutData(data);
		if (size > 0) {
			if (pdf) {
				qualityGroup = new QualityGroup(composite, true, false);
				qualityGroup.addListener(SWT.Selection, this);
			} else {
				boolean raw = Core.getCore().containsRawImage(assets, true);
				exportModeGroup = new ExportModeGroup(composite,
						multiMedia ? ExportModeGroup.ORIGINALS
								: ExportModeGroup.ALLFORMATS | ExportModeGroup.SIZING
										| (raw ? ExportModeGroup.RAWCROP : 0),
						multiMedia ? Messages.SendEmailPage_media : Messages.SendEmailPage_image) {
					@Override
					public void updateScale() {
						super.updateScale();
						updateScaling();
					}
				};
				exportModeGroup.addListener(SWT.Modify, this);
			}
			mailSizeLabel = new Label(composite, SWT.NONE);
			data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.horizontalIndent = 15;
			data.verticalIndent = 10;
			mailSizeLabel.setLayoutData(data);
			mailSizeLabel.setText(Messages.SendEmailPage_Mail_size);

			imageSizeLabel = new Label(composite, SWT.NONE);
			data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.horizontalIndent = 15;
			imageSizeLabel.setLayoutData(data);
			if (!pdf)
				imageSizeLabel.setText(Messages.SendEmailPage_Image_size);
		}
		new Label(composite, SWT.NONE);
		final CGroup metaGroup = UiUtilities.createGroup(composite, 2, Messages.SendEmailPage_Matadata);
		if (!multiMedia) {
			if (!pdf && size > 0) {
				metaButton = WidgetFactory.createCheckButton(metaGroup, Messages.SendEmailPage_include_metadata, null);
				metaButton.addListener(SWT.Selection, this);
			}
			watermarkGroup = new WatermarkGroup(metaGroup);
			trackExportButton = WidgetFactory.createCheckButton(metaGroup, Messages.SendEmailPage_Track_exports,
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		}
		privacyGroup = new PrivacyGroup(metaGroup, Messages.SendEmailPage_Export_only, assets);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		fillValues();
		updateControls();
		setControl(composite);
		setHelp(HelpContextIds.EMAIL_WIZARD);
		setTitle(Messages.SendEmailPage_title);
		String msg;
		if (size == 0)
			msg = Messages.SendEmailPage_No_image_selected;
		else if (multiMedia)
			msg = Messages.SendEmailPage_exporting_multimedia;
		else {
			msg = (size == 1) ? Messages.SendEmailPage_Send_one : NLS.bind(Messages.SendEmailPage_Send_n, size);
			msg += pdf ? Messages.SendEmailPage_select_quality : Messages.SendEmailPage_Adjust_size;
		}
		setMessage(msg);
		super.createControl(parent);
	}

	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == metaButton)
				getWizard().getContainer().updateButtons();
			else
				computePdfSize();
			return;
		case SWT.Modify:
			updateControls();
			checkImages();
		}

	}

	private String checkImages() {
		commonHeight = -1;
		commonWidth = -1;
		totalSize = 0;
		int imageSize = 0;
		StringBuffer message = new StringBuffer(256);
		if (pdf) {
			IPdfWizard w = (IPdfWizard) getWizard();
			File targetFile = w.getTargetFile();
			message.append('\n').append(NLS.bind(Messages.SendEmailPage_pdf_file_n_attached, targetFile.getName()));
			imageSize = (int) (w.getImageSize() * 300 / 72);
		}
		int allmx = 16;
		try (Assetbox box = new Assetbox(assets, null, false)) {
			for (File file : box) {
				Asset asset = box.getAsset();
				if (file != null) {
					if (!pdf) {
						if (message.length() == 0)
							message.append('\n').append(Messages.SendEmailPage_attachments);
						String name = file.getName();
						message.append("\n\t").append(name); //$NON-NLS-1$
						String title = UiUtilities.createSlideTitle(asset);
						if (!name.equals(title))
							message.append(" (").append(title).append(')'); //$NON-NLS-1$
					}
					int width = asset.getWidth();
					int height = asset.getHeight();
					int longEdge = Math.max(width, height);
					allmx = Math.max(allmx, longEdge);
					if (exportModeGroup != null) {
						int mode = exportModeGroup.getMode();
						if (mode == Constants.FORMAT_ORIGINAL)
							totalSize += file.length();
						else if (mode == Constants.SCALE_FIXED) {
							double scale = Math.min(1, (double) exportModeGroup.getDimension() / longEdge);
							width = (int) (scale * width);
							height = (int) (scale * height);
						}
						totalSize += 3L * width * height / EmailWizard.COMPRESSION;
					} else {
						int mx = Math.max(width, height);
						double factor = (double) imageSize / mx;
						totalSize += 3L * (int) (factor * width) * (int) (factor * height) / IPdfWizard.COMPRESSION;
					}
					if (commonWidth >= 0) {
						if (commonWidth != width)
							commonWidth = Integer.MAX_VALUE;
					} else
						commonWidth = width;
					if (commonHeight >= 0) {
						if (commonHeight != height)
							commonHeight = Integer.MAX_VALUE;
					} else
						commonHeight = height;
				}
			}
			setErrorMessage(box.getErrorMessage());
		}
		if (exportModeGroup != null)
			exportModeGroup.setMaximumDim(allmx);
		if (!pdf) {
			computeScaledSize();
			if (exportModeGroup != null)
				exportModeGroup.updateScale();
		} else
			computePdfSize();
		return message.toString();
	}

	private void computeScaledSize() {
		long scaledBytes = exportModeGroup.computeScaledSize(totalSize);
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		if (mailSizeLabel != null)
			mailSizeLabel.setText(NLS.bind(Messages.SendEmailPage_Estimated_size, nf.format(scaledBytes / 1000000d)));
	}

	private void computePdfSize() {
		if (qualityGroup != null) {
			long scaledBytes = getQuality() == Constants.SCREEN_QUALITY ? (long) (totalSize * 0.02f)
					: (long) (totalSize * 0.13f);
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(2);
			if (mailSizeLabel != null)
				mailSizeLabel
						.setText(NLS.bind(Messages.SendEmailPage_Estimated_size, nf.format(scaledBytes / 1000000d)));
		}
	}

	private void updateScaling() {
		if (exportModeGroup != null) {
			double f = exportModeGroup.getScalingFactor();
			if (getMode() == Constants.SCALE_FIXED)
				f = 1d;
			String t = commonWidth < 0 || commonHeight < 0 ? Messages.SendEmailPage_undefined
					: commonWidth == Integer.MAX_VALUE || commonHeight == Integer.MAX_VALUE
							? Messages.SendEmailPage_mixed
							: (int) (commonWidth * f + 0.5d) + "x" + (int) (commonHeight * f + 0.5d); //$NON-NLS-1$
			if (imageSizeLabel != null)
				imageSizeLabel.setText(NLS.bind(Messages.SendEmailPage_Image_size_n, t));
		}
	}

	private void fillValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (multiMedia) {
				if (privacyGroup != null)
					privacyGroup.fillValues(settings);
				return;
			}
			if (exportModeGroup != null)
				exportModeGroup.fillValues(settings);
			if (qualityGroup != null)
				qualityGroup.fillValues(settings);
			if (privacyGroup != null)
				privacyGroup.fillValues(settings);
			if (metaButton != null)
				metaButton.setSelection(settings.getBoolean(EmailWizard.INCLUDEMETA));
			if (watermarkGroup != null)
				watermarkGroup.fillValues(settings);
			if (trackExportButton != null)
				trackExportButton.setSelection(settings.getBoolean(EmailWizard.TRACKEXPORTS));
		}
	}

	// public boolean finish() {
	// saveSettings();
	// Set<QueryField> filter = null;
	// IWizard w = getWizard();
	// if (w instanceof EmailWizard)
	// filter = ((EmailWizard) w).getFilter();
	// List<String> to = null;
	// IWorkbenchWindow activeWorkbenchWindow =
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	// if (activeWorkbenchWindow != null) {
	// SmartCollectionImpl coll =
	// Ui.getUi().getNavigationHistory(activeWorkbenchWindow).getSelectedCollection();
	// if (coll.getAlbum() && coll.getSystem()) {
	// String description = coll.getDescription();
	// int p = description.indexOf('\n');
	// if (p > 0) {
	// int q = description.indexOf(": ", p); //$NON-NLS-1$
	// if (q >= 0) {
	// int r = description.indexOf(';', q + 2);
	// String email = r > 0 ? description.substring(q + 2, r) :
	// description.substring(q + 2);
	// to = Collections.singletonList(email.trim());
	// }
	// }
	// }
	// }
	// new EmailJob(assets, to, getMode(), getSizing(),
	// exportModeGroup.getScalingFactor(),
	// exportModeGroup.getDimension(), exportModeGroup.getCropMode(),
	// exportModeGroup.getUnsharpMask(),
	// exportModeGroup.getJpegQuality(), subjectField.getText(),
	// messageField.getText(), filter,
	// watermarkGroup == null ? false : watermarkGroup.getCreateWatermark(),
	// watermarkGroup == null ? "" : watermarkGroup.getCopyright(),
	// privacyGroup.getSelection(), //$NON-NLS-1$
	// trackExportButton == null ? false : trackExportButton.getSelection(),
	// this).schedule();
	// return true;
	// }

	private int getSizing() {
		return exportModeGroup != null ? exportModeGroup.getSizing() : Constants.SCALE_ORIGINAL;
	}

	public void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		if (exportModeGroup != null)
			exportModeGroup.saveSettings(settings);
		if (qualityGroup != null)
			qualityGroup.saveSettings(settings);
		if (privacyGroup != null)
			privacyGroup.saveSettings(settings);
		if (metaButton != null)
			settings.put(EmailWizard.INCLUDEMETA, getIncludeMeta());
		if (watermarkGroup != null)
			watermarkGroup.saveSettings(settings);
		if (trackExportButton != null)
			settings.put(EmailWizard.TRACKEXPORTS, trackExportButton.getSelection());
	}

	private int getMode() {
		return exportModeGroup != null ? exportModeGroup.getMode() : Constants.FORMAT_ORIGINAL;
	}

	public int getQuality() {
		return qualityGroup == null ? Constants.SCREEN_QUALITY : qualityGroup.getQuality();
	}

	public boolean getIncludeMeta() {
		return metaButton != null ? metaButton.getSelection() : false;
	}

	private void updateControls() {
		if (exportModeGroup != null) {
			int mode = getMode();
			if (metaButton != null)
				metaButton.setEnabled(mode != Constants.FORMAT_ORIGINAL && mode != Constants.FORMAT_WEBP);
			if (watermarkGroup != null)
				watermarkGroup.setEnabled(mode != Constants.FORMAT_ORIGINAL);
		}
	}

	public UnsharpMask getUnsharpMask() {
		if (exportModeGroup != null)
			return exportModeGroup.getUnsharpMask();
		if (qualityGroup != null)
			return qualityGroup.getUnsharpMask();
		return null;
	}

	@Override
	protected String validate() {
		int n = 0;
		int privacy = privacyGroup.getSelection();
		for (Asset a : assets)
			if (a.getSafety() <= privacy)
				++n;
		if (n == 0)
			return Messages.no_images_pass_privacy0;
		if (watermarkGroup != null)
			return watermarkGroup.validate();
		return null;
	}

	public int getJpegQuality() {
		return (exportModeGroup != null) ? exportModeGroup.getJpegQuality()
				: (qualityGroup != null) ? qualityGroup.getJpegQuality() : -1;
	}

	public double getScalingFactor() {
		return (exportModeGroup != null) ? exportModeGroup.getScalingFactor() : 1d;
	}

	public int getCropMode() {
		return (exportModeGroup != null) ? exportModeGroup.getCropMode() : ZImage.UNCROPPED;
	}

	public int getDimension() {
		return (exportModeGroup != null) ? exportModeGroup.getDimension() : 1;
	}

	public int getPrivacy() {
		return privacyGroup != null ? privacyGroup.getSelection() : QueryField.SAFETY_SAFE;
	}

	public boolean getCreateWatermark() {
		return watermarkGroup == null ? false : watermarkGroup.getCreateWatermark();
	}

	public boolean getTrackExport() {
		return trackExportButton == null ? false : trackExportButton.getSelection();
	}

	public String getCopyright() {
		return watermarkGroup == null ? "" : watermarkGroup.getCopyright(); //$NON-NLS-1$
	}

	public String getImageList() {
		return checkImages();
	}

	public EmailData getEmailData() {
		IWizard w = getWizard();
		return new EmailData(assets, getMode(), getSizing(), getScalingFactor(), getDimension(), getCropMode(),
				getUnsharpMask(), getJpegQuality(), (w instanceof EmailWizard) ? ((EmailWizard) w).getFilter() : null,
				getCreateWatermark(), getCopyright(), getPrivacy(), getTrackExport());
	}

}
