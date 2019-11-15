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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.gps.MapAdapter;
import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.geonames.IGeocodingService.Parameter;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.GeoArea;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.gps.internal.IMapComponent;
import com.bdaum.zoom.gps.internal.dialogs.SearchDetailDialog;
import com.bdaum.zoom.gps.internal.views.Mapdata;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.ComputeTimeshiftDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class GpsPreferencePage extends AbstractPreferencePage {

	public class EditNogoDialog extends ZTitleAreaDialog {

		private static final String SETTINGSID = "com.bdaum.zoom.gps.nogoDialog"; //$NON-NLS-1$
		private static final String LATITUDE = "latitude"; //$NON-NLS-1$
		private static final String LONGITUDE = "longitude"; //$NON-NLS-1$
		private static final String KM = "km"; //$NON-NLS-1$
		private IMapComponent mapComponent;
		private IDialogSettings dialogSettings;
		private GeoArea area;
		private double latitude = Double.NaN, longitude = Double.NaN, diameter = 0.1d;
		private String areaName;
		private Text nameField;
		private NumericControl diameterScale;
		private char unit;

		public EditNogoDialog(Shell parentShell, GeoArea area) {
			super(parentShell);
			this.area = area;
		}

		@Override
		public void create() {
			super.create();
			setTitle(Messages.getString("GpsPreferencePage.edit_nogo")); //$NON-NLS-1$
			setMessage(Messages.getString("GpsPreferencePage.exempt_geotagging")); //$NON-NLS-1$
			validateGeo();
			getShell().pack();
		}

		private void validateGeo() {
			String errorMessage = null;
			if (nameField.getText().trim().isEmpty())
				errorMessage = Messages.getString("GpsPreferencePage.specify_area_name"); //$NON-NLS-1$
			else if (Double.isNaN(latitude) || Double.isNaN(longitude))
				errorMessage = Messages.getString("GpsPreferencePage.define_center"); //$NON-NLS-1$
			setErrorMessage(errorMessage);
			getButton(OK).setEnabled(errorMessage == null);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite dialogArea = (Composite) super.createDialogArea(parent);
			dialogSettings = getDialogSettings(GpsActivator.getDefault(), SETTINGSID);
			Composite composite = new Composite(dialogArea, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout());
			mapComponent = GpsActivator.getMapComponent(GpsActivator.findCurrentMappingSystem());
			Place mapPosition = null;
			try {
				mapPosition = area != null ? new Place(latitude = area.getLatitude(), longitude = area.getLongitude())
						: new Place(latitude = dialogSettings.getDouble(LATITUDE),
								longitude = dialogSettings.getDouble(LONGITUDE));
			} catch (NumberFormatException e) {
				// do nothing
			}
			if (mapComponent != null) {
				mapComponent.createComponent(composite, true);
				mapComponent.addMapListener(new MapAdapter() {
					public void setCoordinates(String[] assetId, double latitude, double longitude, int zoom, int type,
							String uuid) {
						area.setZoom(zoom);
						mapComponent.setArea(EditNogoDialog.this.latitude = latitude,
								EditNogoDialog.this.longitude = longitude, Core.toKm(diameter, unit));
						validateGeo();
					}
				});
				mapComponent.getControl().setLayoutData(new GridData(800, 600));
				mapComponent.setInput(new Mapdata(mapPosition, null, null), area.getZoom(), IMapComponent.AREA);
				if (area != null)
					dialogArea.getDisplay().timerExec(500, // give map time to load
							() -> mapComponent.setArea(latitude, longitude, Core.toKm(diameter, unit)));
			}
			Composite controlGroup = new Composite(dialogArea, SWT.NONE);
			controlGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			controlGroup.setLayout(new GridLayout(4, false));
			new Label(controlGroup, SWT.NONE).setText(Messages.getString("GpsPreferencePage.area_name")); //$NON-NLS-1$
			nameField = new Text(controlGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.widthHint = 250;
			nameField.setLayoutData(layoutData);
			nameField.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					if (e.keyCode == ',')
						e.doit = false;
				}
			});
			nameField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validateGeo();
				}
			});
			Label label = new Label(controlGroup, SWT.NONE);
			layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.horizontalIndent = 20;
			label.setLayoutData(layoutData);
			label.setText(Messages.getString("GpsPreferencePage.diameter")); //$NON-NLS-1$
			unit = Core.getCore().getDbFactory().getDistanceUnit();
			label.setText(NLS.bind(Messages.getString("GpsPreferencePage.diameter"), //$NON-NLS-1$
					unit == 'm' ? "mi" : unit == 'n' ? "NM" : "km")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			diameterScale = new NumericControl(controlGroup, SWT.NONE);
			diameterScale.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			diameterScale.setDigits(2);
			diameterScale.setMinimum(2);
			diameterScale.setMaximum(500);
			diameterScale.setIncrement(2);
			diameterScale.setPageIncrement(10);
			diameterScale.setLogrithmic(true);
			diameterScale.addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					diameter = diameterScale.getSelection() / 100d;
					if (mapComponent != null)
						mapComponent.setArea(latitude, longitude, Core.toKm(diameter, unit));
				}
			});
			try {
				if (area != null) {
					nameField.setText(area.getName());
					diameter = 2 * Core.fromKm(area.getKm(), unit);
				} else
					diameter = 2 * Core.fromKm(dialogSettings.getDouble(KM), unit);
				diameterScale.setSelection((int) (diameter * 100));
			} catch (NumberFormatException e1) {
				diameterScale.setSelection(10);
			}
			return dialogArea;
		}

		@Override
		protected void okPressed() {
			areaName = nameField.getText();
			diameter = diameterScale.getSelection() / 100d;
			dialogSettings.put(KM, Core.toKm(diameter / 2, unit));
			dialogSettings.put(LATITUDE, latitude);
			dialogSettings.put(LONGITUDE, longitude);
			super.okPressed();
		}

		public GeoArea getArea() {
			double radius = Core.toKm(diameter / 2, unit);
			if (area == null)
				return new GeoArea(areaName, latitude, longitude, radius, 8);
			area.setKm(radius);
			area.setLatitude(latitude);
			area.setLongitude(longitude);
			area.setName(areaName);
			return area;
		}

	}

	public static final String ID = "com.bdaum.zoom.gps.preferences.GpsPreferencePage"; //$NON-NLS-1$
	public static final String ACCOUNTS = "accounts"; //$NON-NLS-1$
	private CheckboxButton coordinateButton;
	private CheckboxButton placenameButton;
	private CheckboxButton tagButton;
	private CheckboxButton waypointButton;
	private CheckboxButton altitudeButton;
	private Spinner timeShiftMinuteField;
	private Spinner toleranceHourField;
	private Spinner toleranceMinuteField;
	private Combo combo;
	private Map<String, CheckedText> keyMap = new HashMap<>(5);
	private Map<String, Label> linkMap = new HashMap<>(5);

	private String[] serviceNames;

	private int dflt = -1;
	private CheckboxButton editButton;
	private Spinner reqhField;
	private Button addButton;
	private Button editNogoButton;
	private Button deleteButton;
	private TableViewer nogoViewer;
	private List<GeoArea> nogoAreas = new ArrayList<>(5);

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
		updateTableButtons();
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
		Composite comboGroup = new Composite(group, SWT.NONE);
		comboGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		comboGroup.setLayout(new GridLayout(2, false));
		combo = new Combo(comboGroup, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
		Button button = new Button(comboGroup, SWT.PUSH);
		button.setText(Messages.getString("GpsPreferencePage.details")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchDetailDialog dialog = new SearchDetailDialog(getShell(), combo.getText());
				if (dialog.open() == SearchDetailDialog.OK)
					setNamingServiceCombo(dialog.getResult());
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

	@SuppressWarnings("unused")
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
				String explanation = parameter.getExplanation();
				if (explanation != null && !explanation.isEmpty()) {
					new Label(group, SWT.NONE);
					new Label(group, SWT.WRAP).setText(explanation);
				}
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
		clink.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
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
				label.setData(CSSProperties.ID, CSSProperties.ERRORS);
			} else {
				label.setText(NLS.bind(Messages.getString("GpsPreferencePage.website"), name)); //$NON-NLS-1$
				label.setData(CSSProperties.ID, null);
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
		timeShiftMinuteField.setSelection(preferenceStore.getInt(PreferenceConstants.TIMESHIFT));
		toleranceHourField.setSelection(preferenceStore.getInt(PreferenceConstants.TOLERANCE) / 60);
		toleranceMinuteField.setSelection(preferenceStore.getInt(PreferenceConstants.TOLERANCE) % 60);
		GpsUtilities.getGeoAreas(preferenceStore, nogoAreas);
		nogoViewer.setInput(nogoAreas);
		reqhField.setSelection(preferenceStore.getInt(PreferenceConstants.HOURLYLIMIT));
		String nservice = preferenceStore.getString(PreferenceConstants.NAMINGSERVICE);
		setNamingServiceCombo(nservice);
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices())
			for (Parameter parameter : service.getParameters()) {
				CheckedText field = keyMap.get(parameter.getId());
				field.setText(preferenceStore.getString(parameter.getId()));
				field.setHint(parameter.getHint());
			}
	}

	private void setNamingServiceCombo(String nservice) {
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
		preferenceStore.setValue(PreferenceConstants.NOGO, preferenceStore.getDefaultString(PreferenceConstants.NOGO));
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
		preferenceStore.setValue(PreferenceConstants.TIMESHIFT, timeShiftMinuteField.getSelection());
		preferenceStore.setValue(PreferenceConstants.TOLERANCE,
				toleranceHourField.getSelection() * 60 + toleranceMinuteField.getSelection());
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(8);
		nf.setGroupingUsed(false);
		StringBuilder sb = new StringBuilder();
		for (GeoArea geoArea : nogoAreas)
			sb.append(geoArea.getName()).append(',').append(nf.format(geoArea.getLatitude())).append(',')
					.append(nf.format(geoArea.getLongitude())).append(',').append(nf.format(geoArea.getKm()))
					.append(',').append(nf.format(geoArea.getZoom())).append(';');
		preferenceStore.setValue(PreferenceConstants.NOGO, sb.toString());
		preferenceStore.setValue(PreferenceConstants.NAMINGSERVICE, combo.getText());
		preferenceStore.setValue(PreferenceConstants.HOURLYLIMIT, reqhField.getSelection());
		for (IGeocodingService service : GpsActivator.getDefault().getNamingServices())
			for (Parameter parameter : service.getParameters())
				preferenceStore.setValue(parameter.getId(), keyMap.get(parameter.getId()).getText());
	}

	private void createTimingGroup(Composite composite) {
		CGroup group = UiUtilities.createGroup(composite, 4, Messages.getString("GpsPreferencePage.timing")); //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.Timeshift")); //$NON-NLS-1$
		timeShiftMinuteField = new Spinner(group, SWT.BORDER);
		timeShiftMinuteField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		timeShiftMinuteField.setMaximum(3660 * 24 * 60);
		timeShiftMinuteField.setMinimum(-3660 * 24 * 60);
		timeShiftMinuteField.setIncrement(1);
		timeShiftMinuteField.setPageIncrement(60);
		Button computeButton = new Button(group, SWT.PUSH);
		computeButton.setText(Messages.getString("GpsPreferencePage.compute")); //$NON-NLS-1$
		computeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComputeTimeshiftDialog dialog = new ComputeTimeshiftDialog(getShell(),
						timeShiftMinuteField.getSelection(), Messages.getString("GpsPreferencePage.enter_current_time"), //$NON-NLS-1$
						Messages.getString("GpsPreferencePage.tracker_time"), //$NON-NLS-1$
						Messages.getString("GpsPreferencePage.camera_time")); //$NON-NLS-1$
				dialog.create();
				dialog.getShell().setLocation(timeShiftMinuteField.toDisplay(40, 20));
				if (dialog.open() == ComputeTimeshiftDialog.OK)
					timeShiftMinuteField.setSelection(dialog.getResult());
			}
		});
		Label label = new Label(group, SWT.CENTER);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		label.setText(Messages.getString("GpsPreferencePage.not_needed")); //$NON-NLS-1$

		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.Tolerance")); //$NON-NLS-1$
		Composite sub = new Composite(group, SWT.NONE);
		sub.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 2, 1));
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
		new Label(group, SWT.NONE).setText(Messages.getString("GpsPreferencePage.nogo_areas")); //$NON-NLS-1$
		Composite viewerGroup = new Composite(group, SWT.NONE);
		viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		viewerGroup.setLayout(layout);
		nogoViewer = new TableViewer(viewerGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(200, 100);
		layoutData.horizontalIndent = 16;
		nogoViewer.getControl().setLayoutData(layoutData);
		TableViewerColumn col1 = new TableViewerColumn(nogoViewer, SWT.NONE);
		col1.getColumn().setWidth(200);
		col1.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		nogoViewer.setContentProvider(ArrayContentProvider.getInstance());
		nogoViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateTableButtons();
			}
		});
		Composite buttonGroup = new Composite(viewerGroup, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonGroup.setLayout(new GridLayout(1, false));
		addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addButton.setText(Messages.getString("GpsPreferencePage.add")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditNogoDialog dialog = new EditNogoDialog(getShell(), null);
				if (dialog.open() == EditNogoDialog.OK) {
					GeoArea area = dialog.getArea();
					nogoAreas.add(area);
					nogoViewer.add(area);
					nogoViewer.setSelection(new StructuredSelection(area), true);
					updateTableButtons();
				}
			}
		});
		editNogoButton = new Button(buttonGroup, SWT.PUSH);
		editNogoButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		editNogoButton.setText(Messages.getString("GpsPreferencePage.edit")); //$NON-NLS-1$
		editNogoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GeoArea area = (GeoArea) nogoViewer.getStructuredSelection().getFirstElement();
				EditNogoDialog dialog = new EditNogoDialog(getShell(), area);
				if (dialog.open() == EditNogoDialog.OK)
					nogoViewer.update(area, null);
			}
		});
		deleteButton = new Button(buttonGroup, SWT.PUSH);
		deleteButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		deleteButton.setText(Messages.getString("GpsPreferencePage.remove")); //$NON-NLS-1$
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GeoArea area = (GeoArea) nogoViewer.getStructuredSelection().getFirstElement();
				nogoAreas.remove(area);
				nogoViewer.remove(area);
				updateButtons();
			}
		});
	}

	protected void updateTableButtons() {
		boolean enabled = !nogoViewer.getSelection().isEmpty();
		editNogoButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
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
