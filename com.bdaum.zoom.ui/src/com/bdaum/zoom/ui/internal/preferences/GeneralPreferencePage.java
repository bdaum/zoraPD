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
 * (c) 2009-2019 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class GeneralPreferencePage extends AbstractPreferencePage {

	public static final String ID = "com.bdaum.zoom.ui.preferences.GeneralPreferencePage"; //$NON-NLS-1$
	private NumericControl undoField;
	private NumericControl backupField;
	private ComboViewer iccViewer;
	private CheckboxButton advancedButton;
	private CheckboxButton previewButton;
	private NumericControl inactivityField;
	private ComboViewer updateViewer;
	private CheckboxButton noiseButton;
	private CheckboxButton noProgressButton;
	private ComboViewer backupGenerationsField;
	private CheckboxButton enlargeButton;
	private RadioButtonGroup displayGroup;
	private String customFile;

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.GENERAL_PREFERENCE_PAGE);
		createDisplayGroup(composite);
		createViewerGroup(composite);
		createBackupGroup(composite);
		createMiscGroup(composite);
		fillValues();
	}

	@Override
	public void applyData(Object data) {
		if ("backup".equals(data)) //$NON-NLS-1$
			backupField.setFocus();
	}

	private void createDisplayGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("GeneralPreferencePage.display")); //$NON-NLS-1$
		final String[] options = new String[] { String.valueOf(ImageConstants.NOCMS),
				String.valueOf(ImageConstants.SRGB), String.valueOf(ImageConstants.ARGB),
				String.valueOf(ImageConstants.REC709), String.valueOf(ImageConstants.REC2002),
				String.valueOf(ImageConstants.DCIP3), String.valueOf(ImageConstants.DCIP60),
				String.valueOf(ImageConstants.DCIP65), String.valueOf(ImageConstants.CUSTOM) };

		final String[] labels = new String[] { Messages.getString("GeneralPreferencePage.disableCMS"), //$NON-NLS-1$
				"sRGB", //$NON-NLS-1$
				"Adobe RGB", "Rec. 709", "Rec. 2020", "DCI-P3", "P3-60", "P3-D65", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				Messages.getString("GeneralPreferencePage.custom") }; //$NON-NLS-1$

		iccViewer = createComboViewer(group, Messages.getString("GeneralPreferencePage.icc_profile"), options, //$NON-NLS-1$
				labels, false);
		iccViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				String selection = (String) iccViewer.getStructuredSelection().getFirstElement();
				if (Integer.parseInt(selection) == ImageConstants.CUSTOM) {
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
					if (customFile != null) {
						File file = new File(customFile);
						dialog.setFilterPath(file.getParent());
						dialog.setFileName(file.getName());
					}
					dialog.setFilterExtensions(new String[] { "*.icc;*.icm" }); //$NON-NLS-1$
					dialog.setFilterNames(new String[] { Messages.getString("GeneralPreferencePage.icc_profiles") }); //$NON-NLS-1$
					customFile = dialog.open();
				}
			}
		});
		advancedButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.use_quality_interpolation"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		new Label(group, SWT.NONE).setText(Messages.getString("GeneralPreferencePage.inactivity_timeout")); //$NON-NLS-1$
		inactivityField = new NumericControl(group, SWT.NONE);
		inactivityField.setMinimum(1);
		inactivityField.setMaximum(180);
	}

	private void createViewerGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2,
				Messages.getString("GeneralPreferencePage.internal_viewers")); //$NON-NLS-1$
		previewButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.precede_with_preview"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		noiseButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.hide_jpeg_artifacts"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		enlargeButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.enlarge_small_images"), new GridData( //$NON-NLS-1$
						SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		Monitor[] monitors = composite.getDisplay().getMonitors();
		if (monitors.length > 1) {
			Rectangle r = composite.getShell().getBounds();
			if (composite.getDisplay().getPrimaryMonitor().getBounds().contains(r.x + r.width / 2,
					r.y + r.height / 2)) {
				displayGroup = new RadioButtonGroup(group,
						Messages.getString("GeneralPreferencePage.use_secondary_monitor"), SWT.HORIZONTAL, //$NON-NLS-1$
						Messages.getString("GeneralPreferencePage.no"), Messages.getString("GeneralPreferencePage.yes"), //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("GeneralPreferencePage.alternating")); //$NON-NLS-1$
				displayGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			}
		}
	}

	private void createBackupGroup(Composite composite) {
		CGroup group = CGroup.create(composite, 1, Messages.getString("GeneralPreferencePage.backup_update")); //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages.getString("GeneralPreferencePage.backup_interval")); //$NON-NLS-1$
		backupField = new NumericControl(group, SWT.NONE);
		backupField.setMinimum(1);
		backupField.setMaximum(365);

		final String[] options = new String[] { "1", "2", "3", "4", "5", "6", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"7", "8", "9", String.valueOf(Integer.MAX_VALUE) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final String[] labels = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				Messages.getString("GeneralPreferencePage.unlimited") }; //$NON-NLS-1$
		backupGenerationsField = createComboViewer(group,
				Messages.getString("GeneralPreferencePage.max_backup_generations"), options, labels, false); //$NON-NLS-1$

		new Label(group, SWT.NONE).setText(Messages.getString("GeneralPreferencePage.check_for_updates")); //$NON-NLS-1$
		updateViewer = new ComboViewer(group);
		updateViewer.setContentProvider(ArrayContentProvider.getInstance());
		updateViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == PreferenceConstants.UPDATEPOLICY_MANUAL)
					return Messages.getString("GeneralPreferencePage.manually"); //$NON-NLS-1$
				if (element == PreferenceConstants.UPDATEPOLICY_WITHBACKUP)
					return Messages.getString("GeneralPreferencePage.with_backup"); //$NON-NLS-1$
				if (element == PreferenceConstants.UPDATEPOLICY_ONSTART)
					return Messages.getString("GeneralPreferencePage.on_start"); //$NON-NLS-1$
				return element.toString();
			}
		});
		updateViewer.setInput(new String[] { PreferenceConstants.UPDATEPOLICY_MANUAL,
				PreferenceConstants.UPDATEPOLICY_WITHBACKUP, PreferenceConstants.UPDATEPOLICY_ONSTART });
	}

	private void createMiscGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("GeneralPreferencePage.general")); //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages.getString("GeneralPreferencePage.Undo_levels")); //$NON-NLS-1$
		undoField = new NumericControl(group, SWT.NONE);
		undoField.setMinimum(1);
		undoField.setMaximum(99);
		noProgressButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.supress_progress"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		undoField.setSelection(preferenceStore.getInt(PreferenceConstants.UNDOLEVELS));
		backupField.setSelection(preferenceStore.getInt(PreferenceConstants.BACKUPINTERVAL));
		backupGenerationsField.setSelection(
				new StructuredSelection(preferenceStore.getString(PreferenceConstants.BACKUPGENERATIONS)));
		iccViewer.setSelection(new StructuredSelection(preferenceStore.getString(PreferenceConstants.COLORPROFILE)));
		customFile = preferenceStore.getString(PreferenceConstants.CUSTOMPROFILE);
		advancedButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADVANCEDGRAPHICS));
		previewButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.PREVIEW));
		noiseButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADDNOISE));
		enlargeButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ENLARGESMALL));
		if (displayGroup != null) {
			String s = preferenceStore.getString(PreferenceConstants.SECONDARYMONITOR);
			displayGroup
					.setSelection(PreferenceConstants.MON_ALTERNATE.equals(s) ? 2 : Boolean.parseBoolean(s) ? 1 : 0);
		}
		inactivityField.setSelection(preferenceStore.getInt(PreferenceConstants.INACTIVITYINTERVAL));
		updateViewer.setSelection(new StructuredSelection(preferenceStore.getString(PreferenceConstants.UPDATEPOLICY)));
		noProgressButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.NOPROGRESS));
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UNDOLEVELS,
				preferenceStore.getDefaultInt(PreferenceConstants.UNDOLEVELS));
		preferenceStore.setValue(PreferenceConstants.BACKUPINTERVAL,
				preferenceStore.getDefaultInt(PreferenceConstants.BACKUPINTERVAL));
		preferenceStore.setValue(PreferenceConstants.BACKUPGENERATIONS,
				preferenceStore.getDefaultInt(PreferenceConstants.BACKUPGENERATIONS));
		preferenceStore.setValue(PreferenceConstants.CUSTOMPROFILE,
				preferenceStore.getDefaultInt(PreferenceConstants.CUSTOMPROFILE));
		preferenceStore.setValue(PreferenceConstants.COLORPROFILE,
				preferenceStore.getDefaultInt(PreferenceConstants.COLORPROFILE));
		preferenceStore.setValue(PreferenceConstants.ADVANCEDGRAPHICS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ADVANCEDGRAPHICS));
		preferenceStore.setValue(PreferenceConstants.PREVIEW,
				preferenceStore.getDefaultBoolean(PreferenceConstants.PREVIEW));
		preferenceStore.setValue(PreferenceConstants.ADDNOISE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ADDNOISE));
		preferenceStore.setValue(PreferenceConstants.ENLARGESMALL,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ENLARGESMALL));
		preferenceStore.setValue(PreferenceConstants.SECONDARYMONITOR,
				preferenceStore.getDefaultString(PreferenceConstants.SECONDARYMONITOR));
		preferenceStore.setValue(PreferenceConstants.INACTIVITYINTERVAL,
				preferenceStore.getDefaultInt(PreferenceConstants.INACTIVITYINTERVAL));
		preferenceStore.setValue(PreferenceConstants.AUTOEXPORT,
				preferenceStore.getDefaultBoolean(PreferenceConstants.AUTOEXPORT));
		preferenceStore.setValue(PreferenceConstants.EXTERNALVIEWER,
				preferenceStore.getDefaultString(PreferenceConstants.EXTERNALVIEWER));
		preferenceStore.setValue(PreferenceConstants.UPDATEPOLICY,
				preferenceStore.getDefaultString(PreferenceConstants.UPDATEPOLICY));
		preferenceStore.setValue(PreferenceConstants.NOPROGRESS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.NOPROGRESS));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UNDOLEVELS, undoField.getSelection());
		preferenceStore.setValue(PreferenceConstants.BACKUPINTERVAL, backupField.getSelection());
		IStructuredSelection selection = backupGenerationsField.getStructuredSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.BACKUPGENERATIONS,
					Integer.parseInt(selection.getFirstElement().toString()));
		selection = iccViewer.getStructuredSelection();
		if (!selection.isEmpty()) {
			String iccno = (String) selection.getFirstElement();
			preferenceStore.setValue(PreferenceConstants.COLORPROFILE, iccno);
			if (Integer.parseInt(iccno) == ImageConstants.CUSTOM && customFile != null)
				preferenceStore.setValue(PreferenceConstants.CUSTOMPROFILE, customFile);
		}
		preferenceStore.setValue(PreferenceConstants.ADVANCEDGRAPHICS, advancedButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.PREVIEW, previewButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ADDNOISE, noiseButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ENLARGESMALL, enlargeButton.getSelection());
		if (displayGroup != null) {
			int i = displayGroup.getSelection();
			preferenceStore.setValue(PreferenceConstants.SECONDARYMONITOR,
					i == 2 ? PreferenceConstants.MON_ALTERNATE : i == 1 ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		preferenceStore.setValue(PreferenceConstants.INACTIVITYINTERVAL, inactivityField.getSelection());
		IStructuredSelection sel = updateViewer.getStructuredSelection();
		if (!sel.isEmpty())
			preferenceStore.setValue(PreferenceConstants.UPDATEPOLICY, sel.getFirstElement().toString());
		preferenceStore.setValue(PreferenceConstants.NOPROGRESS, noProgressButton.getSelection());
	}
	
	@Override
	protected String doValidate() {
		String selection = (String) iccViewer.getStructuredSelection().getFirstElement();
		if (selection != null && Integer.parseInt(selection) == ImageConstants.CUSTOM) {
			if (customFile == null)
				return Messages.getString("GeneralPreferencePage.no_icc_file"); //$NON-NLS-1$
			if (!new File(customFile).exists())
				return NLS.bind(Messages.getString("GeneralPreferencePage.icc_file_does_not_exist"), customFile); //$NON-NLS-1$
		}
		return null;
	}

}
