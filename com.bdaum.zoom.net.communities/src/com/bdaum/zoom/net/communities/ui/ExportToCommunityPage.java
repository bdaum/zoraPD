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
package com.bdaum.zoom.net.communities.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.net.communities.HelpContextIds;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WatermarkGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ExportModeGroup;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class ExportToCommunityPage extends AbstractExportToCommunityPage implements IAdaptable {

	private CheckboxButton metaButton;
	private boolean hasRawImaages = false;

	private AlbumDescriptor[] associatedAlbums;
	private final String[] titles;
	private final String[] descriptions;
	private CheckboxButton descriptionButton;
	private WatermarkGroup watermarkGroup;
	private ExportModeGroup exportModeGroup;

	public ExportToCommunityPage(IConfigurationElement configElement, List<Asset> assets,
			AlbumDescriptor[] associatedAlbums, String[] titles, String[] descriptions, String id, String title,
			ImageDescriptor titleImage) {
		super(configElement, assets, id, title, titleImage);
		this.assets = assets;
		this.associatedAlbums = associatedAlbums;
		this.titles = titles;
		this.descriptions = descriptions;
		for (Asset asset : assets)
			if (ImageConstants.isRaw(asset.getUri(), true)) {
				hasRawImaages = true;
				break;
			}
		int size = assets.size();
		msg = (assets.isEmpty()) ? Messages.ExportToCommunityPage_nothing_to_export
				: (size == 1) ? Messages.ExportToCommunityPage_exporting_one_image
						: NLS.bind(Messages.ExportToCommunityPage_exporting_n_images, size);

	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE);
		createAccountGroup(composite);
		boolean raw = Core.getCore().containsRawImage(assets, true);
		exportModeGroup = new ExportModeGroup(composite,
				ExportModeGroup.ALLFORMATS | ExportModeGroup.SIZING | (raw ? ExportModeGroup.RAWCROP : 0));
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
		final CGroup metaGroup = new CGroup(composite, SWT.NONE);
		metaGroup.setText(Messages.ExportToCommunityPage_metadata);
		metaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		metaGroup.setLayout(new GridLayout());
		metaButton = WidgetFactory.createCheckButton(metaGroup, Messages.ExportToCommunityPage_include_metadata, null);
		metaButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getWizard().getContainer().updateButtons();
			}
		});
		descriptionButton = WidgetFactory.createCheckButton(metaGroup, Messages.ExportToCommunityPage_show_descriptions,
				null);
		watermarkGroup = new WatermarkGroup(metaGroup);
		setTitle(Messages.ExportToCommunityPage_title);
		setMessage(msg);
		fillValues();
		checkImages();
		setControl(composite);
		setHelp(HelpContextIds.EXPORTCOMMUNITY_WIZARD);
		super.createControl(parent);
	}

	@Override
	protected void updateFields() {
		super.updateFields();
		IStructuredSelection sel = (IStructuredSelection) accountViewer.getSelection();
		if (!sel.isEmpty()) {
			CommunityAccount account = (CommunityAccount) sel.getFirstElement();
			boolean noOrigs = !account.isSupportsRaw() && hasRawImaages;
			if (noOrigs) {
				exportModeGroup.setOriginalsEnabled(false);
				setMessage(msg + Messages.ExportToCommunityPage_cannot_send_orginals);
			} else
				setMessage(msg);
		}
		validatePage();
	}

	@Override
	protected void validatePage() {
		String message = assets.isEmpty() ? Messages.ExportToCommunityPage_no_images_selected : checkAccount();
		if (message == null)
			message = watermarkGroup.validate();
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private void checkImages() {
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		int allmx = 16;
		Ticketbox box = new Ticketbox();
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
						if (volumeManager.findExistingFile(asset, false) != null)
							allmx = Math.max(allmx, Math.max(asset.getWidth(), asset.getHeight()));
						else {
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
		exportModeGroup.setMaximumDim(allmx);
		setErrorMessage(Ticketbox.computeErrorMessage(errands, volumes));
		exportModeGroup.updateScale();
	}

	@Override
	protected void fillValues() {
		super.fillValues();
		IDialogSettings settings = getDialogSettings();
		exportModeGroup.fillValues(settings);
		if (settings != null) {
			watermarkGroup.fillValues(settings);
			descriptionButton.setSelection(settings.getBoolean(CommunityExportWizard.SHOWDESCRIPTIONS));
			metaButton.setSelection(settings.getBoolean(CommunityExportWizard.INCLUDEMETA));
		}
		updateControls();
	}

	@Override
	protected boolean doFinish(CommunityAccount acc) throws CommunicationException, AuthException {
		CommunityApi api = ((CommunityExportWizard) getWizard()).getApi();
		Session session = new Session(api, acc);
		session.init();
		if (acc.isAuthenticated()) {
			new ExportToCommunityJob(configElement, assets, associatedAlbums, titles, descriptions, getMode(), getSizing(),
					exportModeGroup.getScalingFactor(), exportModeGroup.getDimension(), exportModeGroup.getCropMode(),
					exportModeGroup.getUnsharpMask(), exportModeGroup.getJpegQuality(),
					session, getIncludeMeta() ? ((CommunityExportWizard) getWizard()).getFilter() : null,
					descriptionButton.getSelection(),
					watermarkGroup.getCreateWatermark(), watermarkGroup.getCopyright(),
					this).schedule();
			return true;
		}
		return false;
	}

	private int getSizing() {
		return exportModeGroup.getSizing();
	}

	@Override
	protected void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			exportModeGroup.saveSettings(settings);
			watermarkGroup.saveSettings(settings);
			settings.put(CommunityExportWizard.SHOWDESCRIPTIONS, getShowDescriptions());
			settings.put(CommunityExportWizard.INCLUDEMETA, getIncludeMeta());
			super.saveSettings();
		}
	}

	private boolean getShowDescriptions() {
		return descriptionButton.getSelection();
	}

	protected boolean getIncludeMeta() {
		return metaButton.getSelection();
	}

	protected int getMode() {
		return exportModeGroup.getMode();
	}

	private void updateControls() {
		boolean enabled = getMode() != Constants.FORMAT_ORIGINAL;
		metaButton.setEnabled(enabled);
		watermarkGroup.setEnabled(getMode() != Constants.FORMAT_ORIGINAL);
	}

}
