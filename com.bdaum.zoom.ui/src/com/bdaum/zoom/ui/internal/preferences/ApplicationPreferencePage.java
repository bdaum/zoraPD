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
 * (c) 2009-2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class ApplicationPreferencePage extends AbstractPreferencePage {

	private CheckboxButton menuButton;
	private CheckboxButton statusButton;
	private CheckboxButton trashButton;
	private RadioButtonGroup trayButtonGroup;

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
		menuButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.HIDE_MENU_BAR));
		statusButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.HIDE_STATUS_BAR));
		String mode = preferenceStore.getString(PreferenceConstants.TRAY_MODE);
		trayButtonGroup.setSelection(PreferenceConstants.TRAY_PROMPT.equals(mode) ? 2
				: PreferenceConstants.TRAY_TRAY.equalsIgnoreCase(mode) ? 0 : 1); 
		trashButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.FORCEDELETETRASH));
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HIDE_MENU_BAR,
				preferenceStore.getDefaultBoolean(PreferenceConstants.HIDE_MENU_BAR));
		preferenceStore.setValue(PreferenceConstants.HIDE_STATUS_BAR,
				preferenceStore.getDefaultBoolean(PreferenceConstants.HIDE_STATUS_BAR));
		preferenceStore.setValue(PreferenceConstants.TRAY_MODE,
				preferenceStore.getDefaultString(PreferenceConstants.TRAY_MODE));
		preferenceStore.setValue(PreferenceConstants.FORCEDELETETRASH,
				preferenceStore.getDefaultBoolean(PreferenceConstants.FORCEDELETETRASH));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HIDE_MENU_BAR, menuButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.HIDE_STATUS_BAR, statusButton.getSelection());
		String mode;
		switch (trayButtonGroup.getSelection()) {
		case 0:
			mode = PreferenceConstants.TRAY_TRAY; 
			break;
		case 1:
			mode = PreferenceConstants.TRAY_DESK;
			break;
		default:
			mode = PreferenceConstants.TRAY_PROMPT;
		}
		preferenceStore.setValue(PreferenceConstants.TRAY_MODE, mode);
		preferenceStore.setValue(PreferenceConstants.FORCEDELETETRASH, trashButton.getSelection());
	}

	private void createScreenGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1,
				Messages.getString("ApplicationPreferencePage.fullscreen_mode")); //$NON-NLS-1$
		new Label(group, SWT.WRAP).setText(Messages.getString("ApplicationPreferencePage.full_screen_expl")); //$NON-NLS-1$
		menuButton = WidgetFactory.createCheckButton(group, Messages.getString("ApplicationPreferencePage.hide_menu"), //$NON-NLS-1$
				null);
		statusButton = WidgetFactory.createCheckButton(group,
				Messages.getString("ApplicationPreferencePage.hide_status"), null); //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
	private void createTaskGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1, Messages.getString("ApplicationPreferencePage.tray_mode")); //$NON-NLS-1$
		new Label(group, SWT.WRAP).setText(Messages.getString("ApplicationPreferencePage.tray_mode_expl")); //$NON-NLS-1$
		new Label(group, SWT.NONE);
		trayButtonGroup = new RadioButtonGroup(group, Messages.getString("ApplicationPreferencePage.start_app"), //$NON-NLS-1$
				SWT.HORIZONTAL, Messages.getString("ApplicationPreferencePage.in_tray"), //$NON-NLS-1$
				Messages.getString("ApplicationPreferencePage.on_desktop"), //$NON-NLS-1$
				Messages.getString("ApplicationPreferencePage.with_prompt")); //$NON-NLS-1$
		trashButton = WidgetFactory.createCheckButton(group,
				Messages.getString("ApplicationPreferencePage.delete_trashcan"), null); //$NON-NLS-1$
		trayButtonGroup.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateButtons();
			}
		});
	}

	@Override
	protected void updateButtons() {
		trashButton.setEnabled(trayButtonGroup.getSelection() == 0);
	}

}
