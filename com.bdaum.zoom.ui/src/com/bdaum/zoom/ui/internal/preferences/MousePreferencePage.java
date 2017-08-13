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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class MousePreferencePage extends AbstractPreferencePage {

	private static String[] zoomLabels = new String[] { Messages.getString("MousePreferencePage.alt"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.shift"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.right_mouse_button"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.no_zoom") }; //$NON-NLS-1$
	private static String[] wheelLabels = new String[] { Messages.getString("MousePreferencePage.shiftscrolls"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.altscrolls"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.shiftzooms"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.altzooms"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.zoom_only"), //$NON-NLS-1$
			Messages.getString("MousePreferencePage.scroll_only")//$NON-NLS-1$
	};

	private RadioButtonGroup zoomGroup;
	private RadioButtonGroup wheelGroup;
	private Scale wheelScale;
	private Scale speedScale;

	@Override
	protected void contributeButtons(Composite parent) {
		super.contributeButtons(parent);
		if (getShell().getDisplay().getTouchEnabled()) {
			Button touchButton = new Button(parent, SWT.PUSH);
			((GridLayout) parent.getLayout()).numColumns++;
			touchButton.setText(Messages.getString("MousePreferencePage.touch_settings")); //$NON-NLS-1$
			touchButton.setToolTipText(Messages.getString("MousePreferencePage.touch_settings_tooltip")); //$NON-NLS-1$
			touchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IPreferenceStore store = getPreferenceStore();
					store.setValue(PreferenceConstants.WHEELKEY, PreferenceConstants.WHEELZOOMONLY);
					store.setValue(PreferenceConstants.ZOOMKEY, PreferenceConstants.NOZOOM);
					fillValues();
				}
			});
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void createPageContents(Composite comp) {
		setHelp(HelpContextIds.MOUSE_PREFERENCE_PAGE);
		new Label(comp, SWT.WRAP).setText(Messages.getString("MousePreferencePage.mouse_descr")); //$NON-NLS-1$
		new Label(comp, SWT.NONE);
		CGroup speedGroup = new CGroup(comp, SWT.NONE);
		speedGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		speedGroup.setLayout(new GridLayout(2, false));
		speedGroup.setText(Messages.getString("MousePreferencePage.speed")); //$NON-NLS-1$
		new Label(speedGroup, SWT.NONE).setText(Messages.getString("MousePreferencePage.mouse_speed")); //$NON-NLS-1$
		speedScale = new Scale(speedGroup, SWT.HORIZONTAL);
		speedScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		speedScale.setMaximum(20);
		speedScale.setIncrement(0);
		new Label(speedGroup, SWT.NONE).setText(Messages.getString("MousePreferencePage.soft_acceleration")); //$NON-NLS-1$
		wheelScale = new Scale(speedGroup, SWT.HORIZONTAL);
		wheelScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		wheelScale.setMaximum(100);
		wheelScale.setIncrement(0);
		CGroup keyGroup = new CGroup(comp, SWT.NONE);
		keyGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		keyGroup.setLayout(new GridLayout(2, true));
		keyGroup.setText(Messages.getString("MousePreferencePage.control_keys")); //$NON-NLS-1$
		zoomGroup = new RadioButtonGroup(keyGroup, Messages.getString("MousePreferencePage.zoom_key"), SWT.VERTICAL, zoomLabels); //$NON-NLS-1$
		zoomGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		wheelGroup = new RadioButtonGroup(keyGroup, Messages.getString("MousePreferencePage.mouse_wheel_behavior"), SWT.VERTICAL, wheelLabels); //$NON-NLS-1$
		wheelGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		fillValues();
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		speedScale.setSelection(preferenceStore.getInt(PreferenceConstants.MOUSE_SPEED));
		wheelScale.setSelection(preferenceStore.getInt(PreferenceConstants.WHEELSOFTNESS));
		zoomGroup.setSelection(preferenceStore.getInt(PreferenceConstants.ZOOMKEY));
		wheelGroup.setSelection(preferenceStore.getInt(PreferenceConstants.WHEELKEY));
	}
	
	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.MOUSE_SPEED,
				preferenceStore.getDefaultInt(PreferenceConstants.MOUSE_SPEED));
		preferenceStore.setValue(PreferenceConstants.WHEELSOFTNESS,
				preferenceStore.getDefaultInt(PreferenceConstants.WHEELSOFTNESS));
		preferenceStore.setValue(PreferenceConstants.ZOOMKEY,
				preferenceStore.getDefaultInt(PreferenceConstants.ZOOMKEY));
		preferenceStore.setValue(PreferenceConstants.WHEELKEY,
				preferenceStore.getDefaultInt(PreferenceConstants.WHEELKEY));
	}
	
	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.MOUSE_SPEED, speedScale.getSelection());
		preferenceStore.setValue(PreferenceConstants.WHEELSOFTNESS, wheelScale.getSelection());
		preferenceStore.setValue(PreferenceConstants.ZOOMKEY, zoomGroup.getSelection());
		preferenceStore.setValue(PreferenceConstants.WHEELKEY, wheelGroup.getSelection());
	}

}
