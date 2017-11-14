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

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
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
	private CheckboxButton displayButton;

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
				String.valueOf(ImageConstants.SRGB), String.valueOf(ImageConstants.ARGB) };

		final String[] labels = new String[] { Messages.getString("GeneralPreferencePage.disableCMS"), //$NON-NLS-1$
				"sRGB", //$NON-NLS-1$
				"Adobe RGB" }; //$NON-NLS-1$

		iccViewer = createComboViewer(group, Messages.getString("GeneralPreferencePage.icc_profile"), options, //$NON-NLS-1$
				labels, false);
		advancedButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GeneralPreferencePage.use_quality_interpolation"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		new Label(group, SWT.NONE).setText(Messages.getString("GeneralPreferencePage.inactivity_timeout")); //$NON-NLS-1$
		inactivityField = new NumericControl(group, SWT.NONE);
		inactivityField.setMinimum(1);
		inactivityField.setMaximum(180);
	}

	private void createViewerGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("GeneralPreferencePage.internal_viewers")); //$NON-NLS-1$
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
			if (composite.getDisplay().getPrimaryMonitor().getBounds().contains(r.x + r.width/2,  r.y + r.height/2))
				displayButton = WidgetFactory.createCheckButton(group,
						Messages.getString("GeneralPreferencePage.use_secondary_monitor"), //$NON-NLS-1$
						new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		}
	}

	private void createBackupGroup(Composite composite) {
		CGroup group = new CGroup(composite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		group.setLayout(new GridLayout(2, false));
		group.setText(Messages.getString("GeneralPreferencePage.backup_update")); //$NON-NLS-1$
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
		advancedButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADVANCEDGRAPHICS));
		previewButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.PREVIEW));
		noiseButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADDNOISE));
		enlargeButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ENLARGESMALL));
		if (displayButton != null)
			displayButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SECONDARYMONITOR));
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
				preferenceStore.getDefaultBoolean(PreferenceConstants.SECONDARYMONITOR));
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
		IStructuredSelection selection = (IStructuredSelection) backupGenerationsField.getSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.BACKUPGENERATIONS,
					Integer.parseInt(selection.getFirstElement().toString()));
		selection = (IStructuredSelection) iccViewer.getSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.COLORPROFILE, (String) selection.getFirstElement());
		preferenceStore.setValue(PreferenceConstants.ADVANCEDGRAPHICS, advancedButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.PREVIEW, previewButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ADDNOISE, noiseButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ENLARGESMALL, enlargeButton.getSelection());
		if (displayButton != null)
			preferenceStore.setValue(PreferenceConstants.SECONDARYMONITOR, displayButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.INACTIVITYINTERVAL, inactivityField.getSelection());
		IStructuredSelection sel = (IStructuredSelection) updateViewer.getSelection();
		if (!sel.isEmpty())
			preferenceStore.setValue(PreferenceConstants.UPDATEPOLICY, sel.getFirstElement().toString());
		preferenceStore.setValue(PreferenceConstants.NOPROGRESS, noProgressButton.getSelection());
	}

}
