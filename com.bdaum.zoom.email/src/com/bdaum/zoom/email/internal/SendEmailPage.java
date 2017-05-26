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

package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.email.internal.job.EmailJob;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.PrivacyGroup;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.widgets.WatermarkGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ExportModeGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class SendEmailPage extends ColoredWizardPage {

	private static final NumberFormat nf = NumberFormat.getNumberInstance();

	private List<Asset> assets;
	private Label mailSizeLabel;
	private long totalSize;
	private CheckedText subjectField;
	private CheckedText messageField;
	private Label imageSizeLabel;
	private int commonWidth = -1;

	private int commonHeight = -1;

	private final boolean pdf;

	private CheckboxButton metaButton;

	private WatermarkGroup watermarkGroup;

	private PrivacyGroup privacyGroup;

	private CheckboxButton trackExportButton;

	private ExportModeGroup exportModeGroup;

	private QualityGroup qualityGroup;

	public SendEmailPage(List<Asset> assets, boolean pdf) {
		super("main", pdf ? Messages.SendEmailPage_send_pdf_per_email //$NON-NLS-1$
				: Messages.SendEmailPage_Send_per_email, null);
		this.assets = assets;
		this.pdf = pdf;
	}

	@Override
	public void createControl(Composite parent) {
		int size = assets.size();
		Composite composite = createComposite(parent, 1);
		final Label email0ImagesLabel = new Label(composite, SWT.NONE);
		final GridData gd_email0ImagesLabel = new GridData();
		gd_email0ImagesLabel.horizontalIndent = 5;
		email0ImagesLabel.setLayoutData(gd_email0ImagesLabel);
		if (size > 0) {
			if (pdf) {
				qualityGroup = new QualityGroup(composite, true);
				qualityGroup.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						computePdfSize();
					}
				});
			} else {
				boolean raw = Core.getCore().containsRawImage(assets, true);
				exportModeGroup = new ExportModeGroup(composite, ExportModeGroup.ORIGINALS | ExportModeGroup.JPEG
						| ExportModeGroup.SIZING | (raw ? ExportModeGroup.RAWCROP : 0)) {
					@Override
					public void updateScale() {
						super.updateScale();
						updateScaling();
					}
				};
				exportModeGroup.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						updateControls();
						checkImages();
					}
				});
				exportModeGroup.addModifyListener(new ModifyListener() {

					public void modifyText(ModifyEvent e) {
						checkImages();
					}
				});
			}
			mailSizeLabel = new Label(composite, SWT.NONE);
			final GridData gd_mailSizeLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd_mailSizeLabel.horizontalIndent = 15;
			gd_mailSizeLabel.verticalIndent = 10;
			mailSizeLabel.setLayoutData(gd_mailSizeLabel);
			mailSizeLabel.setText(Messages.SendEmailPage_Mail_size);

			imageSizeLabel = new Label(composite, SWT.NONE);
			final GridData gd_imageSizeLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd_imageSizeLabel.horizontalIndent = 15;
			imageSizeLabel.setLayoutData(gd_imageSizeLabel);
			if (!pdf)
				imageSizeLabel.setText(Messages.SendEmailPage_Image_size);
		}
		CGroup textGroup = new CGroup(composite, SWT.NONE);
		textGroup.setText(Messages.SendEmailPage_email);
		textGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		textGroup.setLayout(new GridLayout(2, false));

		final Label subjectLabel = new Label(textGroup, SWT.NONE);
		subjectLabel.setText(Messages.SendEmailPage_Subject);

		subjectField = new CheckedText(textGroup, SWT.BORDER);
		subjectField.setSpellingOptions(8, ISpellCheckingService.TITLEOPTIONS);
		subjectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final Label messageLabel = new Label(textGroup, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		messageLabel.setText(Messages.SendEmailPage_Message);

		messageField = new CheckedText(textGroup, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.heightHint = 70;
		messageField.setLayoutData(data);
		final CGroup metaGroup = new CGroup(composite, SWT.NONE);
		metaGroup.setText(Messages.SendEmailPage_Matadata);
		metaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		metaGroup.setLayout(new GridLayout(2, false));
		if (!pdf && size > 0) {
			metaButton = WidgetFactory.createCheckButton(metaGroup, Messages.SendEmailPage_include_metadata, null);
			metaButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					getWizard().getContainer().updateButtons();
				}
			});
		}
		watermarkGroup = new WatermarkGroup(metaGroup);
		trackExportButton = WidgetFactory.createCheckButton(metaGroup, Messages.SendEmailPage_Track_exports,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		privacyGroup = new PrivacyGroup(metaGroup, Messages.SendEmailPage_Export_only, assets);
		fillValues();
		updateControls();
		String message = checkImages();
		messageField.setText(message);
		setControl(composite);
		setHelp(HelpContextIds.EMAIL_WIZARD);
		setTitle(Messages.SendEmailPage_title);
		String msg = size == 0 ? Messages.SendEmailPage_No_image_selected
				: (size == 1) ? Messages.SendEmailPage_Send_one : NLS.bind(Messages.SendEmailPage_Send_n, size);
		if (size > 0)
			msg += pdf ? Messages.SendEmailPage_select_quality : Messages.SendEmailPage_Adjust_size;
		setMessage(msg);
		super.createControl(parent);
	}

	private String checkImages() {
		commonHeight = -1;
		commonWidth = -1;
		totalSize = 0;
		int imageSize = 0;
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		StringBuffer message = new StringBuffer(256);
		if (pdf) {
			IPdfWizard w = (IPdfWizard) getWizard();
			File targetFile = w.getTargetFile();
			message.append('\n').append(NLS.bind(Messages.SendEmailPage_pdf_file_n_attached, targetFile.getName()));
			imageSize = (int) (w.getImageSize() * 300 / 72);
		}
		Ticketbox box = new Ticketbox();
		int allmx = 16;
		try {
			for (Asset asset : assets) {
				URI uri = volumeManager.findFile(asset);
				File file = null;
				if (uri != null) {
					try {
						file = box.obtainFile(uri);
					} catch (IOException e) {
						errands.add(uri.toString());
					}
				}
				if (file != null)
					try {
						if (volumeManager.findExistingFile(asset, false) != null) {
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
									totalSize += 3L * width * height / EmailWizard.COMPRESSION;
								} else
									totalSize += 3L * width * height / EmailWizard.COMPRESSION;
							} else {
								int mx = Math.max(width, height);
								double factor = (double) imageSize / mx;
								int w = (int) (factor * width);
								int h = (int) (factor * height);
								totalSize += 3L * w * h / IPdfWizard.COMPRESSION;
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
						} else {
							String volume = asset.getVolume();
							if (volume != null && volume.length() > 0)
								volumes.add(volume);
							errands.add(file.getAbsolutePath());
						}
					} finally {
						box.cleanup();
					}
			}
		} finally {
			box.endSession();
		}
		if (exportModeGroup != null)
			exportModeGroup.setMaximumDim(allmx);
		setErrorMessage(Ticketbox.computeErrorMessage(errands, volumes));
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
		nf.setMaximumFractionDigits(2);
		if (mailSizeLabel != null) {
			double mbytes = scaledBytes / 1000000d;
			mailSizeLabel.setText(NLS.bind(Messages.SendEmailPage_Estimated_size, nf.format(mbytes)));
		}
	}

	private void computePdfSize() {
		if (qualityGroup != null) {
			long scaledBytes = totalSize;
			if (getQuality() == Constants.SCREEN_QUALITY)
				scaledBytes = (long) (scaledBytes * 0.02f);
			else
				scaledBytes = (long) (scaledBytes * 0.13f);
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
			String t;
			if (commonWidth < 0 || commonHeight < 0)
				t = Messages.SendEmailPage_undefined;
			else if (commonWidth == Integer.MAX_VALUE || commonHeight == Integer.MAX_VALUE)
				t = Messages.SendEmailPage_mixed;
			else {
				int w = (int) (commonWidth * f + 0.5d);
				int h = (int) (commonHeight * f + 0.5d);
				t = w + "x" + h; //$NON-NLS-1$
			}
			if (imageSizeLabel != null)
				imageSizeLabel.setText(NLS.bind(Messages.SendEmailPage_Image_size_n, t));
		}
	}

	private void fillValues() {
		IDialogSettings settings = getDialogSettings();
		if (exportModeGroup != null)
			exportModeGroup.fillValues(settings);
		if (qualityGroup != null)
			qualityGroup.fillValues(settings);
		if (privacyGroup != null)
			privacyGroup.fillValues(settings);
		if (settings != null) {
			if (metaButton != null) {
				boolean includeMeta = settings.getBoolean(EmailWizard.INCLUDEMETA);
				metaButton.setSelection(includeMeta);
			}
			watermarkGroup.fillValues(settings);
			boolean trackExports = settings.getBoolean(EmailWizard.TRACKEXPORTS);
			trackExportButton.setSelection(trackExports);
		}
	}

	public boolean finish() {
		saveSettings();
		Set<QueryField> filter = null;
		IWizard w = getWizard();
		if (w instanceof EmailWizard)
			filter = ((EmailWizard) w).getFilter();
		List<String> to = null;
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			SmartCollectionImpl coll = UiActivator.getDefault().getNavigationHistory(activeWorkbenchWindow)
					.getSelectedCollection();
			if (coll.getAlbum() && coll.getSystem()) {
				String description = coll.getDescription();
				int p = description.indexOf('\n');
				if (p > 0) {
					int q = description.indexOf(": ", p); //$NON-NLS-1$
					if (q >= 0) {
						int r = description.indexOf(';', q + 2);
						String email = r > 0 ? description.substring(q + 2, r) : description.substring(q + 2);
						to = Collections.singletonList(email.trim());
					}
				}
			}
		}

		new EmailJob(assets, to, getMode(), getSizing(), exportModeGroup.getScalingFactor(), exportModeGroup.getDimension(),
				exportModeGroup.getCropMode(), exportModeGroup.getUnsharpMask(), exportModeGroup.getJpegQuality(),
				subjectField.getText(), messageField.getText(), filter, watermarkGroup.getCreateWatermark(),
				watermarkGroup.getCopyright(), privacyGroup.getSelection(), trackExportButton.getSelection(),
				this).schedule();
		return true;
	}

	private int getSizing() {
		return exportModeGroup != null ? exportModeGroup.getSizing() : Constants.SCALE_ORIGINAL;
	}

	private void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		if (exportModeGroup != null)
			exportModeGroup.saveSettings(settings);
		if (qualityGroup != null)
			qualityGroup.saveSettings(settings);
		if (privacyGroup != null)
			privacyGroup.saveSettings(settings);
		if (metaButton != null)
			settings.put(EmailWizard.INCLUDEMETA, getIncludeMeta());
		watermarkGroup.saveSettings(settings);
		settings.put(EmailWizard.TRACKEXPORTS, trackExportButton.getSelection());
	}

	private int getMode() {
		return exportModeGroup != null ? exportModeGroup.getMode() : Constants.FORMAT_ORIGINAL;
	}

	public int getQuality() {
		return qualityGroup == null ? Constants.SCREEN_QUALITY : qualityGroup.getQuality();
	}

	public String getSubject() {
		return subjectField.getText();
	}

	public String getMailMessage() {
		return messageField.getText();
	}

	public boolean getIncludeMeta() {
		return metaButton != null ? metaButton.getSelection() : false;
	}

	private void updateControls() {
		if (exportModeGroup != null) {
			int mode = getMode();
			if (metaButton != null)
				metaButton.setEnabled(mode != Constants.FORMAT_ORIGINAL);
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
	protected void validatePage() {
		String errorMessage = null;
		int n = 0;
		int privacy = privacyGroup.getSelection();
		for (Asset a : assets)
			if (a.getSafety() <= privacy)
				++n;
		if (n == 0)
			errorMessage = Messages.no_images_pass_privacy0;
		else
			errorMessage = watermarkGroup.validate();
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && subjectField != null && subjectField.getText().length() == 0
				&& getWizard() instanceof EmailPDFWizard) {
			subjectField.setText(((EmailPDFWizard) getWizard()).getTitle());
		}
		super.setVisible(visible);
	}

	public int getJpegQuality() {
		return (exportModeGroup != null) ? exportModeGroup.getJpegQuality()
				: (qualityGroup != null) ? qualityGroup.getJpegQuality() : -1;
	}

//	public int getScalingMethod() {
//		return (qualityGroup != null) ? qualityGroup.getScalingMethod() : ZImage.SCALE_DEFAULT;
//	}

}
