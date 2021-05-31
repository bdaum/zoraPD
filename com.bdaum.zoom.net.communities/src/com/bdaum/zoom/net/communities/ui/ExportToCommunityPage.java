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
package com.bdaum.zoom.net.communities.ui;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.net.communities.HelpContextIds;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WatermarkGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ExportModeGroup;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class ExportToCommunityPage extends AbstractExportToCommunityPage implements IAdaptable, Listener {

	private CheckboxButton metaButton;
	private boolean hasRawImaages = false;

	private AlbumDescriptor[] associatedAlbums;
	private final String[] titles;
	private final String[] descriptions;
	private CheckboxButton descriptionButton;
	private WatermarkGroup watermarkGroup;
	private ExportModeGroup exportModeGroup;
	private int media = IMediaSupport.PHOTO;
	private boolean multimedia;

	public ExportToCommunityPage(IConfigurationElement configElement, List<Asset> assets,
			AlbumDescriptor[] associatedAlbums, String[] titles, String[] descriptions, String id, String title,
			ImageDescriptor titleImage) {
		super(configElement, assets, id, title, titleImage);
		this.assets = assets;
		this.associatedAlbums = associatedAlbums;
		this.titles = titles;
		this.descriptions = descriptions;
		String attribute = configElement.getAttribute("media"); //$NON-NLS-1$
		if (attribute != null && attribute.length() > 0) {
			try {
				media = Integer.parseInt(attribute);
			} catch (NumberFormatException e) {
				// use default
			}
		}
		for (Iterator<Asset> it = assets.iterator(); it.hasNext();) {
			Asset asset = it.next();
			IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
			if (mediaSupport != null) {
				if (!mediaSupport.testProperty(media)) {
					it.remove();
					continue;
				}
			} else if ((media & QueryField.PHOTO) == 0) {
				it.remove();
				continue;
			}
			if (ImageConstants.isRaw(asset.getUri(), true)) {
				hasRawImaages = true;
				break;
			}
		}
		multimedia = Core.getCore().isMultiMedia(assets);
		int size = assets.size();
		msg = (assets.isEmpty()) ? Messages.ExportToCommunityPage_nothing_to_export
				: multimedia ? Messages.ExportToCommunityPage_exporting_multimedia
						: size == 1 ? Messages.ExportToCommunityPage_exporting_one_image
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
				multimedia ? ExportModeGroup.ORIGINALS
						: ExportModeGroup.ALLFORMATS | ExportModeGroup.SIZING | (raw ? ExportModeGroup.RAWCROP : 0),
				multimedia ? Messages.ExportToCommunityPage_media : Messages.ExportToCommunityPage_image);
		exportModeGroup.addListener(SWT.Modify, this);
		final CGroup metaGroup = UiUtilities.createGroup(parent, 1, Messages.ExportToCommunityPage_metadata);
		if (!multimedia) {
			metaButton = WidgetFactory.createCheckButton(metaGroup, Messages.ExportToCommunityPage_include_metadata,
					null);
			metaButton.addListener(SWT.Selection, this);
		}
		descriptionButton = WidgetFactory.createCheckButton(metaGroup, Messages.ExportToCommunityPage_show_descriptions,
				null);
		if (!multimedia)
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
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == metaButton)
				getWizard().getContainer().updateButtons();
			else
				super.handleEvent(e);
			return;
		case SWT.Modify:
			updateControls();
			checkImages();
		}
	}

	@Override
	protected void updateFields() {
		super.updateFields();
		IStructuredSelection sel = accountViewer.getStructuredSelection();
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
	protected String validate() {
		String message = assets.isEmpty() ? Messages.ExportToCommunityPage_no_images_selected : checkAccount();
		if (message == null && watermarkGroup != null)
			message = watermarkGroup.validate();
		return message;
	}

	private void checkImages() {
		int allmx = 16;
		try (Assetbox box = new Assetbox(assets, null, false)) {
			for (File file : box)
				if (file != null) {
					Asset asset = box.getAsset();
					allmx = Math.max(allmx, Math.max(asset.getWidth(), asset.getHeight()));
				}
			setErrorMessage(box.getErrorMessage());
		}
		exportModeGroup.setMaximumDim(allmx);
		exportModeGroup.updateScale();
	}

	@Override
	protected void fillValues() {
		super.fillValues();
		IDialogSettings settings = getDialogSettings();
		exportModeGroup.fillValues(settings);
		if (settings != null) {
			if (watermarkGroup != null)
				watermarkGroup.fillValues(settings);
			descriptionButton.setSelection(settings.getBoolean(CommunityExportWizard.SHOWDESCRIPTIONS));
			if (metaButton != null)
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
			new ExportToCommunityJob(configElement, assets, associatedAlbums, titles, descriptions, getMode(),
					getSizing(), exportModeGroup.getScalingFactor(), exportModeGroup.getDimension(),
					exportModeGroup.getCropMode(), exportModeGroup.getUnsharpMask(), exportModeGroup.getJpegQuality(),
					session, getIncludeMeta() ? ((CommunityExportWizard) getWizard()).getFilter() : null,
					descriptionButton.getSelection(),
					watermarkGroup == null ? false : watermarkGroup.getCreateWatermark(),
					watermarkGroup == null ? "" : watermarkGroup.getCopyright(), this).schedule(); //$NON-NLS-1$
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
			if (watermarkGroup != null)
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
		return metaButton == null ? false : metaButton.getSelection();
	}

	protected int getMode() {
		return exportModeGroup.getMode();
	}

	private void updateControls() {
		boolean enabled = getMode() != Constants.FORMAT_ORIGINAL;
		if (metaButton != null)
			metaButton.setEnabled(enabled);
		if (watermarkGroup != null)
			watermarkGroup.setEnabled(getMode() != Constants.FORMAT_ORIGINAL);
	}

}
