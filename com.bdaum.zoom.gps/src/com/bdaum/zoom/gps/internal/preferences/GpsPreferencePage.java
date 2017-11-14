/*******************************************************************************
 * Copyright (c) 2009-2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class GpsPreferencePage extends AbstractPreferencePage {

	public static final String ID = "com.bdaum.zoom.gps.preferences.GpsPreferencePage"; //$NON-NLS-1$
	private CheckboxButton coordinateButton;
	private CheckboxButton placenameButton;
	private CheckboxButton tagButton;
	private CheckboxButton waypointButton;
	private CheckboxButton altitudeButton;
	private Spinner timeShiftField;
	private Spinner toleranceHourField;
	private Spinner toleranceMinuteField;
	private CTabItem tabItem0;
	private CTabItem tabItem1;
	private Combo combo;
	private Map<IConfigurationElement, Text> keyMap = new HashMap<IConfigurationElement, Text>();

	private String[] serviceNames;

	private int dflt = -1;
	private CheckboxButton editButton;
	private Spinner reqhField;

	public GpsPreferencePage() {
		setDescription(Messages
				.getString("GpsPreferencePage.how_gps_tracker_data_is_applied")); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(GpsActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.PREFERENCE_PAGE);
		createTabFolder(composite, "GPS"); //$NON-NLS-1$
		tabItem0 = UiUtilities.createTabItem(tabFolder,
				Messages.getString("GpsPreferencePage.general")); //$NON-NLS-1$
		tabItem0.setControl(createGeneralGroup(tabFolder));
		tabItem1 = UiUtilities.createTabItem(tabFolder,
				Messages.getString("GpsPreferencePage.webservices")); //$NON-NLS-1$
		tabItem1.setControl(createWebServiceGroup(tabFolder));
		initTabFolder(0);
		fillValues();
	}

	private Composite createWebServiceGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		createSelectionGroup(composite);
		createPremiumGroup(composite);
		return composite;
	}

	private void createSelectionGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2,
				Messages.getString("AdvancedPreferencePage.webservice")); //$NON-NLS-1$
		combo = new Combo(group, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));
		IConfigurationElement[] namingServices = GpsActivator
				.getNamingServices();
		serviceNames = new String[namingServices.length];
		for (int i = 0; i < namingServices.length; i++) {
			serviceNames[i] = namingServices[i].getAttribute("name"); //$NON-NLS-1$
			if (new Boolean(namingServices[i].getAttribute("default"))) //$NON-NLS-1$
				dflt = i;
		}
		combo.setItems(serviceNames);
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.maxRequests")); //$NON-NLS-1$
		reqhField = new Spinner(group, SWT.BORDER);
		reqhField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		reqhField.setMaximum(100000);
		reqhField.setMinimum(40);
		reqhField.setIncrement(10);
		reqhField.setPageIncrement(100);
	}

	private void createPremiumGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2,
				Messages.getString("AdvancedPreferencePage.premium")); //$NON-NLS-1$
		Label label = new Label(group, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false,
				2, 1));
		label.setText(Messages
				.getString("AdvancedPreferencePage.premium_descr")); //$NON-NLS-1$
		IConfigurationElement[] premiums = GpsActivator.getPremiumServices();
		for (IConfigurationElement premium : premiums) {
			String description = premium.getAttribute("description"); //$NON-NLS-1$
			for (IConfigurationElement key : premium.getChildren("key")) { //$NON-NLS-1$
				new Label(group, SWT.NONE).setText(key.getAttribute("name")); //$NON-NLS-1$
				Text field = new Text(group, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
				field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false));
				field.setToolTipText(description);
				keyMap.put(key, field);
			}
		}
	}

	private Composite createGeneralGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		createTimingGroup(composite);
		createModeGroup(composite);
		createKeywordGroup(composite);
		return composite;
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		altitudeButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.UPDATEALTITUDE));
		coordinateButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.INCLUDECOORDINATES));
		placenameButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.INCLUDENAMES));
		editButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.EDIT));
		tagButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.OVERWRITE));
		waypointButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.USEWAYPOINTS));
		timeShiftField.setSelection(preferenceStore
				.getInt(PreferenceConstants.TIMESHIFT));
		toleranceHourField.setSelection(preferenceStore
				.getInt(PreferenceConstants.TOLERANCE) / 60);
		toleranceMinuteField.setSelection(preferenceStore
				.getInt(PreferenceConstants.TOLERANCE) % 60);
		reqhField.setSelection(preferenceStore
				.getInt(PreferenceConstants.HOURLYLIMIT));
		String service = preferenceStore
				.getString(PreferenceConstants.NAMINGSERVICE);
		int select = dflt;
		for (int i = 0; i < serviceNames.length; i++)
			if (serviceNames[i].equals(service))
				select = i;
		combo.select(select);
		for (Map.Entry<IConfigurationElement, Text> entry : keyMap.entrySet())
			entry.getValue().setText(
					preferenceStore
							.getString(entry.getKey().getAttribute("id"))); //$NON-NLS-1$
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UPDATEALTITUDE,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.UPDATEALTITUDE));
		preferenceStore
				.setValue(
						PreferenceConstants.INCLUDECOORDINATES,
						preferenceStore
								.getDefaultBoolean(PreferenceConstants.INCLUDECOORDINATES));
		preferenceStore.setValue(PreferenceConstants.INCLUDENAMES,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.INCLUDENAMES));
		preferenceStore.setValue(PreferenceConstants.EDIT,
				preferenceStore.getDefaultBoolean(PreferenceConstants.EDIT));
		preferenceStore.setValue(PreferenceConstants.OVERWRITE, preferenceStore
				.getDefaultBoolean(PreferenceConstants.OVERWRITE));
		preferenceStore.setValue(PreferenceConstants.USEWAYPOINTS,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.USEWAYPOINTS));
		preferenceStore.setValue(PreferenceConstants.TIMESHIFT,
				preferenceStore.getDefaultInt(PreferenceConstants.TIMESHIFT));
		preferenceStore.setValue(PreferenceConstants.TOLERANCE,
				preferenceStore.getDefaultInt(PreferenceConstants.TOLERANCE));
		preferenceStore.setValue(PreferenceConstants.NAMINGSERVICE,
				preferenceStore
						.getDefaultString(PreferenceConstants.NAMINGSERVICE));
		preferenceStore.setValue(PreferenceConstants.HOURLYLIMIT,
				preferenceStore.getDefaultInt(PreferenceConstants.HOURLYLIMIT));
		for (IConfigurationElement key : keyMap.keySet()) {
			String id = key.getAttribute("id"); //$NON-NLS-1$
			preferenceStore.setValue(id, preferenceStore.getDefaultString(id));
		}
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UPDATEALTITUDE,
				altitudeButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.INCLUDECOORDINATES,
				coordinateButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.INCLUDENAMES,
				placenameButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.EDIT,
				editButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.OVERWRITE,
				tagButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.USEWAYPOINTS,
				waypointButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TIMESHIFT,
				timeShiftField.getSelection());
		preferenceStore.setValue(
				PreferenceConstants.TOLERANCE,
				toleranceHourField.getSelection() * 60
						+ toleranceMinuteField.getSelection());
		preferenceStore.setValue(PreferenceConstants.NAMINGSERVICE,
				combo.getText());
		preferenceStore.setValue(PreferenceConstants.HOURLYLIMIT,
				reqhField.getSelection());
		for (Map.Entry<IConfigurationElement, Text> entry : keyMap.entrySet())
			preferenceStore.setValue(entry.getKey().getAttribute("id"), //$NON-NLS-1$
					entry.getValue().getText());
	}

	private void createTimingGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2,
				Messages.getString("GpsPreferencePage.timing")); //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages
				.getString("GpsPreferencePage.Timeshift")); //$NON-NLS-1$
		timeShiftField = new Spinner(group, SWT.BORDER);
		timeShiftField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
				false));
		timeShiftField.setMaximum(12 * 60);
		timeShiftField.setMinimum(-12 * 60);
		timeShiftField.setIncrement(1);
		timeShiftField.setPageIncrement(60);
		new Label(group, SWT.NONE).setText(Messages
				.getString("GpsPreferencePage.Tolerance")); //$NON-NLS-1$
		Composite sub = new Composite(group, SWT.NONE);
		sub.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		sub.setLayout(layout);
		toleranceHourField = new Spinner(sub, SWT.BORDER);
		toleranceHourField.setLayoutData(new GridData(SWT.END, SWT.CENTER,
				false, false));
		toleranceHourField.setMaximum(168);
		toleranceHourField.setIncrement(1);
		toleranceHourField.setPageIncrement(3);
		toleranceMinuteField = new Spinner(sub, SWT.BORDER);
		toleranceMinuteField.setLayoutData(new GridData(SWT.END, SWT.CENTER,
				false, false));
		toleranceMinuteField.setMaximum(59);
		toleranceMinuteField.setIncrement(1);
		toleranceMinuteField.setPageIncrement(15);
	}

	private void createModeGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1,
				Messages.getString("GpsPreferencePage.mode")); //$NON-NLS-1$
		editButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.edit_trackpoints"), null); //$NON-NLS-1$
		tagButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Overwrite_tags"), null); //$NON-NLS-1$
		waypointButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.use_waypoints"), null); //$NON-NLS-1$
		altitudeButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Update_altitude"), null); //$NON-NLS-1$
	}

	private void createKeywordGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1,
				Messages.getString("GpsPreferencePage.keywords")); //$NON-NLS-1$
		coordinateButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Include_coordinates"), //$NON-NLS-1$
				null);
		placenameButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Include_place_names"), //$NON-NLS-1$
				null);
	}

}
