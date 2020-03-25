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

package com.bdaum.zoom.ui.internal.wizards;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.OutputTargetGroup;
import com.bdaum.zoom.ui.internal.job.ExportfolderJob;
import com.bdaum.zoom.ui.internal.widgets.AddToCatGroup;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.PrivacyGroup;
import com.bdaum.zoom.ui.internal.widgets.WatermarkGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class ExportFolderPage extends ColoredWizardPage implements Listener {

	private List<Asset> assets;
	private OutputTargetGroup outputTargetGroup;
	private CheckboxButton metaButton;
	private WatermarkGroup watermarkGroup;
	private PrivacyGroup privacyGroup;
	private AddToCatGroup addToCatGroup;
	private ExportModeGroup exportModeGroup;
	private boolean updating;
	private boolean multiMedia;

	public ExportFolderPage(List<Asset> assets) {
		super("main", Messages.ExportFolderPage_export_into_folder, null); //$NON-NLS-1$
		multiMedia = Core.getCore().isMultiMedia(this.assets = assets);
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE);
		outputTargetGroup = new OutputTargetGroup(composite,
				new GridData(GridData.FILL, GridData.BEGINNING, true, false), new Listener() {
					public void handleEvent(Event e) {
						if (e.data == OutputTargetGroup.SUBFOLDER) {
							if (!updating)
								saveOptions();
						} else {
							if (!updating)
								updateOptions();
							updateCatButtons();
							validatePage();
						}
					}
				}, true, true, true);
		exportModeGroup = new ExportModeGroup(composite,
				multiMedia ? ExportModeGroup.ORIGINALS
						: ExportModeGroup.ALLFORMATS | ExportModeGroup.SIZING
								| (Core.getCore().containsRawImage(assets, true) ? ExportModeGroup.RAWCROP : 0),
				multiMedia ? Messages.ExportFolderPage_media : Messages.ExportFolderPage_image);
		exportModeGroup.addListener(SWT.Modify, this);
		final CGroup metaGroup = CGroup.create(composite, 1, Messages.ExportFolderPage_metadata);
		if (!multiMedia) {
			metaButton = WidgetFactory.createCheckButton(metaGroup, Messages.ExportFolderPage_include_metadata, null);
			metaButton.addListener(SWT.Selection, this);
			watermarkGroup = new WatermarkGroup(metaGroup);
			watermarkGroup.addListener(SWT.Modify, this);
		}
		privacyGroup = new PrivacyGroup(metaGroup, Messages.ExportFolderPage_export_only, assets);
		privacyGroup.addListener(SWT.Selection, this);
		if (!multiMedia) {
			addToCatGroup = new AddToCatGroup(composite);
			addToCatGroup.addListener(SWT.Selection, this);
		}
		fillValues(getDialogSettings());
		updateOptions();
		checkImages();
		setControl(composite);
		setHelp(HelpContextIds.EXPORTFOLDER_WIZARD);
		setTitle(Messages.ExportFolderPage_title);
		int size = assets.size();
		String msg;
		if (assets.isEmpty())
			msg = Messages.ExportFolderPage_nothing_to_export;
		else if (multiMedia)
			msg = Messages.ExportFolderPage_exporting_media;
		else {
			msg = (size == 1) ? Messages.SendEmailPage_Send_one : NLS.bind(Messages.SendEmailPage_Send_n, size);
			msg += Messages.ExportFolderPage_adjust_size;
		}
		setMessage(msg);
		super.createControl(parent);
		validatePage();
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == metaButton) {
				saveOptions();
				getWizard().getContainer().updateButtons();
			} else
				saveOptions();
			break;

		case SWT.Modify:
			saveOptions();
			if (e.data == exportModeGroup) {
				updateCatButtons();
				updateControls();
				checkImages();
			}
			break;
		}
		
	}

	protected void saveOptions() {
		IDialogSettings section = outputTargetGroup.getTargetSection(getDialogSettings(), true);
		if (section != null)
			saveSettings(section);
	}

	protected void updateOptions() {
		try {
			updating = true;
			IDialogSettings section = outputTargetGroup.getTargetSection(getDialogSettings(), false);
			if (section != null) {
				outputTargetGroup.updateSubfolderOption(section);
				fillValues(section);
			} else
				saveOptions();
		} finally {
			updating = false;
		}
	}

	protected void updateCatButtons() {
		if (addToCatGroup != null) {
			boolean reformat = getMode() != ExportModeGroup.ORIGINALS;
			addToCatGroup.setEnabled(reformat, reformat && outputTargetGroup.getTarget() == Constants.FILE);
		}
	}

	@Override
	protected void validatePage() {
		String msg = null;
		if (assets.isEmpty())
			msg = Messages.ExportFolderPage_nothing_to_export;
		else {
			int privacy = privacyGroup.getSelection();
			int n = 0;
			for (Asset a : assets)
				if (a.getSafety() <= privacy)
					++n;
			msg = n == 0 ? Messages.ExportFolderPage_no_images_pass_privacy : outputTargetGroup.validate();
		}
		if (msg == null && watermarkGroup != null)
			msg = watermarkGroup.validate();
		setErrorMessage(msg);
		setPageComplete(msg == null);
	}

	private void checkImages() {
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		int allmx = 16;
		for (Asset asset : assets) {
			URI uri = volumeManager.findFile(asset);
			if (uri != null) {
				if (volumeManager.findExistingFile(asset, false) != null)
					allmx = Math.max(allmx, Math.max(asset.getWidth(), asset.getHeight()));
				else {
					String volume = asset.getVolume();
					if (volume != null && !volume.isEmpty())
						volumes.add(volume);
					errands.add(uri.toString());
				}
			}
		}
		exportModeGroup.setMaximumDim(allmx);
		if (!errands.isEmpty()) {
			String msg;
			if (errands.size() == 1)
				msg = NLS.bind(Messages.SendEmailPage_File_offline, errands.get(0), volumes.toArray()[0]);
			else {
				StringBuffer sb = new StringBuffer();
				for (String volume : volumes) {
					if (sb.length() > 0)
						sb.append(", "); //$NON-NLS-1$
					sb.append(volume);
				}
				msg = NLS.bind(Messages.SendEmailPage_Files_offline, errands.size(), sb.toString());

			}
			setErrorMessage(msg);
		} else
			setErrorMessage(null);
		exportModeGroup.updateScale();
	}

	private void fillValues(IDialogSettings settings) {
		if (settings != null) {
			if (multiMedia) {
				privacyGroup.fillValues(settings);
				outputTargetGroup.initValues(settings);
			} else {
				exportModeGroup.fillValues(settings);
				privacyGroup.fillValues(settings);
				boolean includeMeta = settings.getBoolean(ExportFolderWizard.INCLUDEMETA);
				metaButton.setSelection(includeMeta);
				watermarkGroup.fillValues(settings);
				addToCatGroup.fillValues(settings);
				outputTargetGroup.initValues(settings);
			}
		}
		updateCatButtons();
		updateControls();
	}

	public boolean finish() {
		saveSettings(getDialogSettings());
		new ExportfolderJob(assets, getMode(), getSizing(), exportModeGroup.getScalingFactor(),
				exportModeGroup.getDimension(), exportModeGroup.getUnsharpMask(), exportModeGroup.getJpegQuality(),
				exportModeGroup.getCropMode(), outputTargetGroup.getTarget(), outputTargetGroup.getFtpDir(),
				outputTargetGroup.getLocalFolder(), outputTargetGroup.getSubfolderoption(),
				getIncludeMeta() ? ((ExportFolderWizard) getWizard()).getFilter() : null,
				watermarkGroup == null ? false : watermarkGroup.getCreateWatermark(),
				watermarkGroup == null ? "" : watermarkGroup.getCopyright(), privacyGroup.getSelection(), //$NON-NLS-1$
				addToCatGroup == null ? false : addToCatGroup.getAddSelection(),
				addToCatGroup == null ? false : addToCatGroup.getWatchSelection(), this).schedule();
		return true;
	}

	private int getSizing() {
		return exportModeGroup.getSizing();
	}

	private void saveSettings(IDialogSettings settings) {
		exportModeGroup.saveSettings(settings);
		privacyGroup.saveSettings(settings);
		if (watermarkGroup != null)
			watermarkGroup.saveSettings(settings);
		if (addToCatGroup != null)
			addToCatGroup.saveValues(settings);
		settings.put(ExportFolderWizard.INCLUDEMETA, getIncludeMeta());
		outputTargetGroup.saveValues(settings);
	}

	protected boolean getIncludeMeta() {
		return metaButton == null ? false : metaButton.getSelection();
	}

	protected int getMode() {
		return exportModeGroup.getMode();
	}

	private void updateControls() {
		int mode = getMode();
		if (metaButton != null)
			metaButton.setEnabled(mode != Constants.FORMAT_ORIGINAL && mode != Constants.FORMAT_WEBP);
		if (watermarkGroup != null)
			watermarkGroup.setEnabled(mode != Constants.FORMAT_ORIGINAL);
	}

}
