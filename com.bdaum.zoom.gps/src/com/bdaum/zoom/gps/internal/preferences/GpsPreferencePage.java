/*******************************************************************************
 * Copyright (c) 2009-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.geonames.IGeocodingService.Parameter;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;

@SuppressWarnings("restriction")
public class GpsPreferencePage extends AbstractPreferencePage {

	public static final String ID = "com.bdaum.zoom.gps.preferences.GpsPreferencePage"; //$NON-NLS-1$
	public static final Object ACCOUNTS = "accounts"; //$NON-NLS-1$
	private CheckboxButton coordinateButton;
	private CheckboxButton placenameButton;
	private CheckboxButton tagButton;
	private CheckboxButton waypointButton;
	private CheckboxButton altitudeButton;
	private Spinner timeShiftField;
	private Spinner toleranceHourField;
	private Spinner toleranceMinuteField;
	private Combo combo;
	private Map<String, CheckedText> keyMap = new HashMap<>(5);
	private Map<String, Label> linkMap = new HashMap<>(5);

	private String[] serviceNames;

	private int dflt = -1;
	private CheckboxButton editButton;
	private Spinner reqhField;

	public GpsPreferencePage() {
		setDescription(Messages.getString("GpsPreferencePage.how_gps_tracker_data_is_applied")); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(GpsActivator.getDefault().getPreferenceStore());
	}

	@Override
	public void applyData(Object data) {
		if (ACCOUNTS.equals(data))
			tabFolder.setSelection(1);
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.PREFERENCE_PAGE);
		createTabFolder(composite, "GPS"); //$NON-NLS-1$
		UiUtilities.createTabItem(tabFolder, Messages.getString("GpsPreferencePage.general"), null) //$NON-NLS-1$
				.setControl(createGeneralGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.getString("GpsPreferencePage.webservices"), //$NON-NLS-1$
				Messages.getString("GpsPreferencePage.webservice_tooltip")) //$NON-NLS-1$
				.setControl(createWebServiceGroup(tabFolder));
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
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("AdvancedPreferencePage.webservice")); //$NON-NLS-1$
		combo = new Combo(group, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		IGeocodingService[] namingServices = GpsActivator.getDefault().getNamingServices();
		serviceNames = new String[namingServices.length];
		for (int i = 0; i < namingServices.length; i++) {
			serviceNames[i] = namingServices[i].getName();
			if (namingServices[i].isDefault())
				dflt = i;
		}
		combo.setItems(serviceNames);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index >= 0) {
					String description = GpsActivator.getDefault().getNamingServiceByName(serviceNames[index])
							.getDescription();
					if (description != null && !description.isEmpty())
						combo.setToolTipText(description);
				} else
					combo.setToolTipText(""); //$NON-NLS-1$
			}
		});
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.maxRequests")); //$NON-NLS-1$
		reqhField = new Spinner(group, SWT.BORDER);
		reqhField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		reqhField.setMaximum(100000);
		reqhField.setMinimum(40);
		reqhField.setIncrement(10);
		reqhField.setPageIncrement(100);
	}

	private void createPremiumGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("AdvancedPreferencePage.premium")); //$NON-NLS-1$
		Label label = new Label(group, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		label.setText(Messages.getString("AdvancedPreferencePage.premium_descr")); //$NON-NLS-1$
		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateAccounts();
			}
		};
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices()) {
			for (Parameter parameter : service.getParameters()) {
				new Label(group, SWT.NONE).setText(parameter.getLabel());
				CheckedText field = new CheckedText(group, SWT.SINGLE | SWT.LEAD | SWT.BORDER,
						ISpellCheckingService.NOSPELLING);
				field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				field.setToolTipText(parameter.getTooltip());
				field.addModifyListener(modifyListener);
				keyMap.put(parameter.getId(), field);
			}
			String link = service.getLink();
			if (link != null && !link.isEmpty())
				createLink(group, link, service.getName());
		}
		validateAccounts();
	}

	private void createLink(CGroup group, String link, String premium) {
		Label label = new Label(group, SWT.NONE);
		linkMap.put(premium, label);
		label.setText(NLS.bind(Messages.getString("GpsPreferencePage.website"), premium)); //$NON-NLS-1$
		CLink clink = new CLink(group, SWT.NONE);
		clink.setText(link);
		clink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(link));
				} catch (PartInitException | MalformedURLException e1) {
					// do nothing
				}
			}
		});
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

	private String validateAccounts() {
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices()) {
			String name = service.getName();
			Label label = linkMap.get(name);
			String msg = null;
			for (Parameter parameter : service.getParameters()) {
				if (keyMap.get(parameter.getId()).getText().isEmpty())
					msg = parameter.getReqMsg();
				if (msg != null && !msg.isEmpty())
					break;
			}
			if (msg != null && !msg.isEmpty()) {
				label.setText(msg);
				label.setData("id", "errors"); //$NON-NLS-1$//$NON-NLS-2$
			} else {
				label.setText(NLS.bind(Messages.getString("GpsPreferencePage.website"), name)); //$NON-NLS-1$
				label.setData("id", null); //$NON-NLS-1$
			}
			CssActivator.getDefault().setColors(label);
		}
		return null;
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		altitudeButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.UPDATEALTITUDE));
		coordinateButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.INCLUDECOORDINATES));
		placenameButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.INCLUDENAMES));
		editButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.EDIT));
		tagButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.OVERWRITE));
		waypointButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.USEWAYPOINTS));
		timeShiftField.setSelection(preferenceStore.getInt(PreferenceConstants.TIMESHIFT));
		toleranceHourField.setSelection(preferenceStore.getInt(PreferenceConstants.TOLERANCE) / 60);
		toleranceMinuteField.setSelection(preferenceStore.getInt(PreferenceConstants.TOLERANCE) % 60);
		reqhField.setSelection(preferenceStore.getInt(PreferenceConstants.HOURLYLIMIT));
		String nservice = preferenceStore.getString(PreferenceConstants.NAMINGSERVICE);
		int select = dflt;
		for (int i = 0; i < serviceNames.length; i++)
			if (serviceNames[i].equals(nservice))
				select = i;
		if (select >= 0) {
			combo.select(select);
			String description = GpsActivator.getDefault().getNamingServiceByName(serviceNames[select])
					.getDescription();
			if (description != null && !description.isEmpty())
				combo.setToolTipText(description);
		}
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices())
			for (Parameter parameter : service.getParameters()) {
				CheckedText field = keyMap.get(parameter.getId());
				field.setText(preferenceStore.getString(parameter.getId()));
				field.setHint(parameter.getHint());
			}
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UPDATEALTITUDE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.UPDATEALTITUDE));
		preferenceStore.setValue(PreferenceConstants.INCLUDECOORDINATES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.INCLUDECOORDINATES));
		preferenceStore.setValue(PreferenceConstants.INCLUDENAMES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.INCLUDENAMES));
		preferenceStore.setValue(PreferenceConstants.EDIT, preferenceStore.getDefaultBoolean(PreferenceConstants.EDIT));
		preferenceStore.setValue(PreferenceConstants.OVERWRITE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.OVERWRITE));
		preferenceStore.setValue(PreferenceConstants.USEWAYPOINTS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.USEWAYPOINTS));
		preferenceStore.setValue(PreferenceConstants.TIMESHIFT,
				preferenceStore.getDefaultInt(PreferenceConstants.TIMESHIFT));
		preferenceStore.setValue(PreferenceConstants.TOLERANCE,
				preferenceStore.getDefaultInt(PreferenceConstants.TOLERANCE));
		preferenceStore.setValue(PreferenceConstants.NAMINGSERVICE,
				preferenceStore.getDefaultString(PreferenceConstants.NAMINGSERVICE));
		preferenceStore.setValue(PreferenceConstants.HOURLYLIMIT,
				preferenceStore.getDefaultInt(PreferenceConstants.HOURLYLIMIT));
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices())
			for (Parameter parameter : service.getParameters())
				preferenceStore.setValue(parameter.getId(), preferenceStore.getDefaultString(parameter.getId()));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.UPDATEALTITUDE, altitudeButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.INCLUDECOORDINATES, coordinateButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.INCLUDENAMES, placenameButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.EDIT, editButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.OVERWRITE, tagButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.USEWAYPOINTS, waypointButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TIMESHIFT, timeShiftField.getSelection());
		preferenceStore.setValue(PreferenceConstants.TOLERANCE,
				toleranceHourField.getSelection() * 60 + toleranceMinuteField.getSelection());
		preferenceStore.setValue(PreferenceConstants.NAMINGSERVICE, combo.getText());
		preferenceStore.setValue(PreferenceConstants.HOURLYLIMIT, reqhField.getSelection());
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices())
			for (Parameter parameter : service.getParameters())
				preferenceStore.setValue(parameter.getId(), keyMap.get(parameter.getId()).getText());
	}

	private void createTimingGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("GpsPreferencePage.timing")); //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.Timeshift")); //$NON-NLS-1$
		timeShiftField = new Spinner(group, SWT.BORDER);
		timeShiftField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		timeShiftField.setMaximum(12 * 60);
		timeShiftField.setMinimum(-12 * 60);
		timeShiftField.setIncrement(1);
		timeShiftField.setPageIncrement(60);
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.Tolerance")); //$NON-NLS-1$
		Composite sub = new Composite(group, SWT.NONE);
		sub.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		sub.setLayout(layout);
		toleranceHourField = new Spinner(sub, SWT.BORDER);
		toleranceHourField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		toleranceHourField.setMaximum(168);
		toleranceHourField.setIncrement(1);
		toleranceHourField.setPageIncrement(3);
		toleranceMinuteField = new Spinner(sub, SWT.BORDER);
		toleranceMinuteField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		toleranceMinuteField.setMaximum(59);
		toleranceMinuteField.setIncrement(1);
		toleranceMinuteField.setPageIncrement(15);
	}

	private void createModeGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1, Messages.getString("GpsPreferencePage.mode")); //$NON-NLS-1$
		editButton = WidgetFactory.createCheckButton(group, Messages.getString("GpsPreferencePage.edit_trackpoints"), //$NON-NLS-1$
				null);
		tagButton = WidgetFactory.createCheckButton(group, Messages.getString("GpsPreferencePage.Overwrite_tags"), //$NON-NLS-1$
				null);
		waypointButton = WidgetFactory.createCheckButton(group, Messages.getString("GpsPreferencePage.use_waypoints"), //$NON-NLS-1$
				null);
		altitudeButton = WidgetFactory.createCheckButton(group, Messages.getString("GpsPreferencePage.Update_altitude"), //$NON-NLS-1$
				null);
	}

	private void createKeywordGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 1, Messages.getString("GpsPreferencePage.keywords")); //$NON-NLS-1$
		coordinateButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Include_coordinates"), //$NON-NLS-1$
				null);
		placenameButton = WidgetFactory.createCheckButton(group,
				Messages.getString("GpsPreferencePage.Include_place_names"), //$NON-NLS-1$
				null);
	}

}
