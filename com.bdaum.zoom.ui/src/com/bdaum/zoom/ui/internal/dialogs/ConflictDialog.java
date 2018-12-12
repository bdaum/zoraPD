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

import java.text.SimpleDateFormat;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class ConflictDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.conflictDialog"; //$NON-NLS-1$
	private static final String STATUS = "status";//$NON-NLS-1$
	private static final String FACE = "face";//$NON-NLS-1$
	private static final String GPS = "gps";//$NON-NLS-1$
	private static final String IPTC = "iptc";//$NON-NLS-1$
	private static final String IMAGE = "image"; //$NON-NLS-1$
	private static final String EXIF = "exif"; //$NON-NLS-1$
	private CheckboxButton resetIptcButton;
	private CheckboxButton resetGpsButton;
	private CheckboxButton resetStatusButton;
	private IDialogSettings settings;
	private CheckboxButton resetImageButton;
	private CheckboxButton resetExifButton;
	private CheckboxButton resetFaceButton;
	private CheckboxButton allButton;
	private ImportConfiguration currentConfig;
	private final boolean multi;
	private final String title;
	private final String message;
	private CheckboxButton newerButton;
	private final Asset asset;
	private RadioButtonGroup conflictButtonGroup;

	public ConflictDialog(Shell parentShell, String title, String message, Asset asset,
			ImportConfiguration currentConfig, boolean multi) {
		super(parentShell, HelpContextIds.CONFLICT_DIALOG);
		this.title = title;
		this.message = message;
		this.asset = asset;
		this.currentConfig = currentConfig;
		this.multi = multi;
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		updateButtons();
		setTitle(title);
		setMessage(message);
	}

	@Override
	public int open() {
		Ui.getUi().playSound("question", PreferenceConstants.ALARMONPROMPT); //$NON-NLS-1$
		return super.open();
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		try {
			resetImageButton.setSelection(settings.getBoolean(IMAGE));
		} catch (Exception e) {
			// ignore
		}
		try {
			resetStatusButton.setSelection(settings.getBoolean(STATUS));
		} catch (Exception e) {
			// ignore
		}
		try {
			resetFaceButton.setSelection(settings.getBoolean(FACE));
		} catch (Exception e) {
			// ignore
		}
		try {
			resetExifButton.setSelection(settings.getBoolean(EXIF));
		} catch (Exception e) {
			// ignore
		}
		try {
			resetGpsButton.setSelection(settings.getBoolean(GPS));
		} catch (Exception e) {
			// ignore
		}
		try {
			resetIptcButton.setSelection(settings.getBoolean(IPTC));
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(area, SWT.NONE);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		main.setLayout(new GridLayout(asset != null ? 2 : 1, false));
		conflictButtonGroup = new RadioButtonGroup(main, null, SWT.NONE, Messages.ConflictDialog_skip,
				Messages.ConflictDialog_overwrite, Messages.ConflictDialog_synchronize);
		conflictButtonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite synchGroup = new Composite(conflictButtonGroup, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.horizontalIndent = 15;
		synchGroup.setLayoutData(data);
		synchGroup.setLayout(new GridLayout());
		final GridData gd_resetImageButton = new GridData(SWT.FILL, SWT.CENTER, true, false);
		resetImageButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_refresh_image,
				gd_resetImageButton);

		resetFaceButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_reset_face_data,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		resetStatusButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_refresh_image_status,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		resetExifButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_refresh_exif,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		resetGpsButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_refresh_gps,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		resetIptcButton = WidgetFactory.createCheckButton(synchGroup, Messages.RefreshDialog_refresh_iptc,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		Listener selectionListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				updateButtons();
			}
		};
		conflictButtonGroup.addListener(selectionListener);
		if (multi) {
			new Label(conflictButtonGroup, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
			allButton = WidgetFactory.createCheckButton(conflictButtonGroup, Messages.ConflictDialog_apply_to_all,
					new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			allButton.addListener(selectionListener);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gridData.horizontalIndent = 15;
			newerButton = WidgetFactory.createCheckButton(parent, Messages.ConflictDialog_only_newer_items, gridData);
		}
		if (asset != null) {
			Composite ccomp = new Composite(main, SWT.NONE);
			ccomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			ccomp.setLayout(new GridLayout());
			final Canvas canvas = new Canvas(ccomp, SWT.DOUBLE_BUFFERED);
			GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
			gridData.heightHint = 200;
			gridData.widthHint = 200;
			canvas.setLayoutData(gridData);
			Label canvasLabel = new Label(ccomp, SWT.NONE);
			canvasLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			canvasLabel.setText(asset.getName());
			Label importLabel = new Label(ccomp, SWT.NONE);
			importLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			SimpleDateFormat sdf = new SimpleDateFormat(Messages.ConflictDialog_importDateFormat);
			String by = asset.getImportedBy();
			importLabel.setText(NLS.bind(Messages.ConflictDialog_imported, sdf.format(asset.getImportDate()),
					by == null ? Messages.ConflictDialog_unknown : by));
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					Rectangle clientArea = canvas.getClientArea();
					Image image = Core.getCore().getImageCache().getImage(asset);
					Rectangle bounds = image.getBounds();
					double factor = (double) Math.min(clientArea.width, clientArea.height)
							/ Math.max(bounds.width, bounds.height);
					int w = (int) (bounds.width * factor);
					int h = (int) (bounds.height * factor);
					e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, (clientArea.width - w) / 2,
							(clientArea.height - h) / 2, w, h);
				}
			});
			canvas.redraw();
		}
		return area;
	}

	protected void updateButtons() {
		int selection = conflictButtonGroup.getSelection();
		boolean details = selection == 2;
		resetImageButton.setEnabled(details);
		resetExifButton.setEnabled(details);
		resetStatusButton.setEnabled(details);
		resetFaceButton.setEnabled(details);
		resetGpsButton.setEnabled(details);
		resetIptcButton.setEnabled(details);
		if (newerButton != null && allButton != null)
			newerButton.setEnabled(allButton.getSelection() && selection != 0);
		getButton(OK).setEnabled(selection >= 0);
	}

	@Override
	protected void cancelPressed() {
		currentConfig = null;
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		boolean all = allButton != null && allButton.getSelection();
		boolean newer = all && newerButton != null && newerButton.getSelection();
		int selection = conflictButtonGroup.getSelection();
		if (selection == 2) {
			currentConfig = new ImportConfiguration(this, currentConfig.timeline, currentConfig.locations, true,
					newer ? ImportState.SYNCNEWER : all ? ImportState.SYNCALL : ImportState.SYNC,
					resetImageButton.getSelection(), resetStatusButton.getSelection(), resetExifButton.getSelection(),
					resetIptcButton.getSelection(), resetGpsButton.getSelection(), resetFaceButton.getSelection(),
					currentConfig.processSidecars, currentConfig.rawOptions, currentConfig.dngLocator,
					currentConfig.dngUncompressed, currentConfig.dngLinear, currentConfig.deriveRelations,
					currentConfig.autoDerive, currentConfig.applyXmp, currentConfig.dngFolder, currentConfig.onPrompt,
					currentConfig.onFinish, currentConfig.inBackground, currentConfig.makerNotes,
					currentConfig.faceData, currentConfig.archiveRecipes, currentConfig.useWebP,
					currentConfig.jpegQuality, currentConfig.showImported, currentConfig.relationDetectors,
					currentConfig.rules);
			settings.put(IPTC, resetIptcButton.getSelection());
			settings.put(GPS, resetGpsButton.getSelection());
			settings.put(STATUS, resetStatusButton.getSelection());
			settings.put(FACE, resetFaceButton.getSelection());
			settings.put(EXIF, resetExifButton.getSelection());
			settings.put(IMAGE, resetImageButton.getSelection());
		} else if (selection == 0)
			currentConfig.conflictPolicy = all ? ImportState.IGNOREALL : ImportState.IGNORE;
		else if (selection == 1)
			currentConfig.conflictPolicy = newer ? ImportState.OVERWRITENEWER
					: all ? ImportState.OVERWRITEALL : ImportState.OVERWRITE;
		super.okPressed();
	}

	/**
	 * @return currentConfig
	 */
	public ImportConfiguration getCurrentConfig() {
		return currentConfig;
	}

}
