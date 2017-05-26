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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class RefreshDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.refreshDialog"; //$NON-NLS-1$
	private static final String OUTDATED = "outdated";//$NON-NLS-1$
	private static final String STATUS = "status";//$NON-NLS-1$
	private static final String FACE = "face";//$NON-NLS-1$
	private static final String GPS = "gps";//$NON-NLS-1$
	private static final String IPTC = "iptc";//$NON-NLS-1$
	private static final String MISSING = "missing";//$NON-NLS-1$
	private static final String UPTODATE = "uptodate";//$NON-NLS-1$
	private static final String IMAGE = "image"; //$NON-NLS-1$
	private static final String EXIF = "exif"; //$NON-NLS-1$
	private static final String REMOTE = "remote"; //$NON-NLS-1$
	private int outdated;
	private int missing;
	private CheckboxButton existingButton;
	private CheckboxButton missingButton;
	private CheckboxButton resetIptcButton;
	private boolean reImport;
	private boolean delete;
	private boolean resetIptc;
	private CheckboxButton resetGpsButton;
	private boolean resetGps;
	private boolean resetStatus;
	private CheckboxButton resetStatusButton;
	private IDialogSettings settings;
	private final int uptodate;
	private CheckboxButton uptoDateButton;
	private boolean refresh;
	private CheckboxButton resetImageButton;
	private boolean resetImage;
	private CheckboxButton resetExifButton;
	private boolean resetExif;
	private final int remote;
	private CheckboxButton remoteButton;
	private boolean includeRemote;
	private boolean resetFace;
	private CheckboxButton resetFaceButton;

	public RefreshDialog(Shell parentShell, int uptodate, int outdated,
			int missing, int remote) {
		super(parentShell, HelpContextIds.REFRESH_DIALOG);
		this.uptodate = uptodate;
		this.outdated = outdated;
		this.missing = missing;
		this.remote = remote;
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		updateButtons();
		setTitle(Messages.RefreshDialog_synchronizing_cat_entries);
		setMessage(Messages.RefreshDialog_synchronizing_updates
				+ Messages.RefreshDialog_select_to_sync);
	}

	private void fillValues() {
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);

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
		try {
			existingButton.setSelection(settings.getBoolean(OUTDATED));
		} catch (Exception e) {
			// ignore
		}
		try {
			uptoDateButton.setSelection(settings.getBoolean(UPTODATE));
		} catch (Exception e) {
			// ignore
		}
		try {
			missingButton.setSelection(settings.getBoolean(MISSING));
		} catch (Exception e) {
			// ignore
		}
		try {
			remoteButton.setSelection(settings.getBoolean(REMOTE));
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout());
		existingButton = WidgetFactory.createCheckButton(
				comp,
				outdated + uptodate == 1 ? Messages.RefreshDialog_reimport_one : NLS
						.bind(Messages.RefreshDialog_reimport, outdated
								+ uptodate), new GridData(SWT.FILL, SWT.CENTER,
						true, false));
		final GridData gd_uptoDateButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_uptoDateButton.horizontalIndent = 15;
		uptoDateButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_include_up_to_date, gd_uptoDateButton);
		uptoDateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		final GridData gd_resetImageButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_resetImageButton.horizontalIndent = 15;
		resetImageButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_refresh_image, gd_resetImageButton);

		final GridData gd_resetFaceButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_resetFaceButton.horizontalIndent = 15;
		resetFaceButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_reset_face_data, gd_resetFaceButton);
		final GridData gd_resetStatusButton = new GridData(SWT.FILL,
				SWT.CENTER, true, false);
		gd_resetStatusButton.horizontalIndent = 15;
		resetStatusButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_refresh_image_status,
				gd_resetStatusButton);
		final GridData gd_resetExifButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_resetExifButton.horizontalIndent = 15;
		resetExifButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_refresh_exif, gd_resetExifButton);

		final GridData gd_resetGpsButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_resetGpsButton.horizontalIndent = 15;
		resetGpsButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_refresh_gps, gd_resetGpsButton);
		final GridData gd_resetIptcButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_resetIptcButton.horizontalIndent = 15;
		resetIptcButton = WidgetFactory.createCheckButton(comp,
				Messages.RefreshDialog_refresh_iptc, gd_resetIptcButton);
		existingButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		remoteButton = WidgetFactory.createCheckButton(comp,
				NLS.bind(Messages.RefreshDialog_include_remote, remote),
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		missingButton = WidgetFactory.createCheckButton(comp,
				NLS.bind(Messages.RefreshDialog_remove_missing_files, missing),
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		return area;
	}

	protected void updateButtons() {
		missingButton.setEnabled(missing > 0);
		int existing = outdated + uptodate;
		boolean details = existing > 0;
		existingButton.setEnabled(existing > 0);
		remoteButton.setEnabled(remote > 0);
		if (details)
			details &= existingButton.getSelection();
		uptoDateButton.setEnabled(details && uptodate > 0);
		if (details && !uptoDateButton.getSelection())
			details = outdated > 0;
		resetImageButton.setEnabled(details);
		resetExifButton.setEnabled(details);
		resetStatusButton.setEnabled(details);
		resetFaceButton.setEnabled(details);
		resetGpsButton.setEnabled(details);
		resetIptcButton.setEnabled(details);
	}

	@Override
	protected void okPressed() {
		reImport = existingButton.getEnabled() && existingButton.getSelection();
		refresh = uptoDateButton.getEnabled() && uptoDateButton.getSelection();
		resetIptc = resetIptcButton.getSelection();
		delete = missingButton.getEnabled() && missingButton.getSelection();
		resetGps = resetGpsButton.getSelection();
		resetStatus = resetStatusButton.getSelection();
		resetFace = resetFaceButton.getSelection();
		resetExif = resetExifButton.getSelection();
		resetImage = resetImageButton.getSelection();
		includeRemote = remoteButton.getSelection();
		settings.put(IPTC, resetIptc);
		settings.put(MISSING, delete);
		settings.put(OUTDATED, reImport);
		settings.put(UPTODATE, refresh);
		settings.put(GPS, resetGps);
		settings.put(STATUS, resetStatus);
		settings.put(FACE, resetFace);
		settings.put(EXIF, resetExif);
		settings.put(IMAGE, resetImage);
		settings.put(REMOTE, includeRemote);
		super.okPressed();
	}

	public boolean isReImport() {
		return reImport;
	}

	public boolean isDelete() {
		return delete;
	}

	public boolean isResetIptc() {
		return resetIptc;
	}

	public boolean isResetGps() {
		return resetGps;
	}

	public boolean isResetStatus() {
		return resetStatus;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public boolean isIncludeRemote() {
		return includeRemote;
	}

	public boolean isResetImage() {
		return resetImage;
	}

	public boolean isResetExif() {
		return resetExif;
	}

	public boolean isResetFaceData() {
		return resetFace;
	}

}
