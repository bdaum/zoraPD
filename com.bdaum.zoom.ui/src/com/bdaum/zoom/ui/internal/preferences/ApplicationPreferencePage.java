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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class ApplicationPreferencePage extends AbstractPreferencePage {

	private CheckboxButton menuButton;
	private CheckboxButton statusButton;
	private CheckboxButton trayButton;
	private CheckboxButton trashButton;


	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.GENERAL_PREFERENCE_PAGE);
		createScreenGroup(composite);
		createTaskGroup(composite);
		fillValues();
		updateButtons();
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		menuButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.HIDE_MENU_BAR));
		statusButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.HIDE_STATUS_BAR));
		trayButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.TRAY_MODE));
		trashButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.FORCEDELETETRASH));
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HIDE_MENU_BAR,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.HIDE_MENU_BAR));
		preferenceStore
				.setValue(PreferenceConstants.HIDE_STATUS_BAR, preferenceStore
						.getDefaultBoolean(PreferenceConstants.HIDE_STATUS_BAR));
		preferenceStore.setValue(PreferenceConstants.TRAY_MODE, preferenceStore
				.getDefaultBoolean(PreferenceConstants.TRAY_MODE));
		preferenceStore
				.setValue(
						PreferenceConstants.FORCEDELETETRASH,
						preferenceStore
								.getDefaultBoolean(PreferenceConstants.FORCEDELETETRASH));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HIDE_MENU_BAR,
				menuButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.HIDE_STATUS_BAR,
				statusButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TRAY_MODE,
				trayButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.FORCEDELETETRASH,
				trashButton.getSelection());
	}

	private void createScreenGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1,
				Messages.getString("ApplicationPreferencePage.fullscreen_mode")); //$NON-NLS-1$
		Label explLabel = new Label(group, SWT.WRAP);
		explLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		explLabel.setText(Messages
				.getString("ApplicationPreferencePage.full_screen_expl")); //$NON-NLS-1$
		menuButton = WidgetFactory
				.createCheckButton(group, Messages
						.getString("ApplicationPreferencePage.hide_menu"), null); //$NON-NLS-1$
		statusButton = WidgetFactory
				.createCheckButton(
						group,
						Messages.getString("ApplicationPreferencePage.hide_status"), null); //$NON-NLS-1$
	}

	private void createTaskGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1,
				Messages.getString("ApplicationPreferencePage.tray_mode")); //$NON-NLS-1$
		Label explLabel = new Label(group, SWT.WRAP);
		explLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		explLabel.setText(Messages
				.getString("ApplicationPreferencePage.tray_mode_expl")); //$NON-NLS-1$
		trayButton = WidgetFactory
				.createCheckButton(
						group,
						Messages.getString("ApplicationPreferencePage.run_in_tray"), null); //$NON-NLS-1$
		trashButton = WidgetFactory
				.createCheckButton(
						group,
						Messages.getString("ApplicationPreferencePage.delete_trashcan"), null); //$NON-NLS-1$
		trayButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
	}

	@Override
	protected void updateButtons() {
		trashButton.setEnabled(trayButton.getSelection());
	}

}
