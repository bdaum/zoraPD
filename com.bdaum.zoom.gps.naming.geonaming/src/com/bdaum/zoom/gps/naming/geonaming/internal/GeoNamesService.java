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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.gps.naming.geonaming.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.xml.sax.SAXException;

import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.AbstractGeocodingService;
import com.bdaum.zoom.gps.internal.preferences.GpsPreferencePage;
import com.bdaum.zoom.net.ui.internal.NetActivator;
import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class GeoNamesService extends AbstractGeocodingService implements Listener {

	private static final String USERNAME = "photozora"; //$NON-NLS-1$
	private static final String[] CONTINENTCODES = new String[] { "AF", "AS", "EU", "NA", "OC", "SA", "AN" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	private static final String CONTINENT_CODE = "&continentCode="; //$NON-NLS-1$
	private static final String FEATURE_CLASS_V = "&featureClass=V"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_U = "&featureClass=U"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_T = "&featureClass=T"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_S = "&featureClass=S"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_R = "&featureClass=R"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_P = "&featureClass=P"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_L = "&featureClass=L"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_H = "&featureClass=H"; //$NON-NLS-1$
	private static final String FEATURE_CLASS_A = "&featureClass=A"; //$NON-NLS-1$
	private static boolean asked;

	private CheckboxButton nameRequiredButton;
	private Combo modeCombo;
	private CheckboxButton aButton;
	private CheckboxButton hButton;
	private CheckboxButton lButton;
	private CheckboxButton pButton;
	private CheckboxButton rButton;
	private CheckboxButton sButton;
	private CheckboxButton tButton;
	private CheckboxButton uButton;
	private CheckboxButton vButton;
	private Combo contCombo;

	public Place fetchPlaceInfo(double lat, double lon) throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException, UnknownHostException, HttpException {
		return fetchPlaceInfoFromGeoNames(Locale.getDefault().getLanguage(),
				new String[] { usformat.format(lat), usformat.format(lon), Locale.getDefault().getLanguage() });
	}

	private static Place fetchPlaceInfoFromGeoNames(String lang, String[] parms) throws IOException, SAXException,
			ParserConfigurationException, WebServiceException, UnknownHostException, HttpException {
		Place place = new Place();
		try (InputStream in = openGeonamesService(
				NLS.bind("http://api.geonames.org/findNearbyPlaceName?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
						parms))) {
			new PlaceParser(in).parse(place);
		}
		if (place.getId() != null) {
			try (InputStream in = openGeonamesService(
					NLS.bind("http://api.geonames.org/hierarchy?geonameId={0}&lang={1}", //$NON-NLS-1$
							place.getId(), lang))) {
				new HierarchyParser(in).parse(place);
			}
		}
		if ("US".equals(place.getCountryCode())) //$NON-NLS-1$
			try (InputStream in = openGeonamesService(
					NLS.bind("http://api.geonames.org/findNearestAddress?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
							parms))) {
				new AddressParser(in).parse(place);
				return (place.getName() != null) ? place : null;
			}
		try (InputStream in = openGeonamesService(
				NLS.bind("http://api.geonames.org/findNearbyPostalCodes?lat={0}&lng={1}&lang={2}", //$NON-NLS-1$
						parms))) {
			new PostcodeParser(in).parse(place);
			return (place.getName() != null) ? place : null;
		}
	}

	private static InputStream openGeonamesService(String query)
			throws IOException, UnknownHostException, HttpException {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String parms = preferencesService.getString(GeonamingActivator.PLUGIN_ID,
				PreferenceConstants.GEONAMESPARMS, "", null); //$NON-NLS-1$
		if ((parms == null || parms.isEmpty()) && !asked) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				PreferencesUtil.createPreferenceDialogOn(activeWorkbenchWindow.getShell(), GpsPreferencePage.ID, null,
						GpsPreferencePage.ACCOUNTS).open();
				parms = preferencesService.getString(GeonamingActivator.PLUGIN_ID,
						PreferenceConstants.GEONAMESPARMS, "", null); //$NON-NLS-1$
				asked = true;
			}
		}
		if (parms != null && !parms.trim().isEmpty()) {
			parms = parms.trim();
			if (parms.startsWith("&")) //$NON-NLS-1$
				query += parms;
			else if (parms.indexOf('=') >= 0 || parms.indexOf('&') >= 0)
				query += '&' + parms;
			else
				query += "&username=" + parms; //$NON-NLS-1$
		} else
			query += "&username=" + USERNAME; //$NON-NLS-1$
		return NetActivator.getDefault().openStream(query);
	}

	public WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException, ParserConfigurationException, HttpException {
		List<WaypointArea> pnts = new ArrayList<WaypointArea>();
		Locale locale = Locale.getDefault();
		String template = "http://api.geonames.org/search?" + getSearchParms() + "&countryBias={1}&lang={2}&maxRows={3}"; //$NON-NLS-1$ //$NON-NLS-2$
		String query = NLS.bind(template, new Object[] { URLEncoder.encode(address, "UTF-8"), locale.getCountry(), //$NON-NLS-1$
				locale.getLanguage(), GeoCodeParser.MAXRESULTS });
		try (InputStream in = openGeonamesService(query)) {
			new GeoCodeParser(in).parse(pnts);
			return pnts.toArray(new WaypointArea[pnts.size()]);
		}
	}

	@Override
	public Control createParameterGroup(CGroup parmGroup) {
		Composite composite = new Composite(parmGroup, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.getString("GeoNamesService.search_mode")); //$NON-NLS-1$
		modeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		modeCombo.setItems(Messages.getString("GeoNamesService.whole_text"), //$NON-NLS-1$
				Messages.getString("GeoNamesService.contains"), Messages.getString("GeoNamesService.starts_with"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("GeoNamesService.equals")); //$NON-NLS-1$
		modeCombo.addListener(SWT.Selection, this);
		new Label(composite, SWT.NONE).setText(Messages.getString("GeoNamesService.continent")); //$NON-NLS-1$
		contCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		contCombo.setItems(Messages.getString("GeoNamesService.all"), Messages.getString("GeoNamesService.af"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("GeoNamesService.as"), Messages.getString("GeoNamesService.eu"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("GeoNamesService.na"), Messages.getString("GeoNamesService.oc"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("GeoNamesService.sa"), //$NON-NLS-1$
				Messages.getString("GeoNamesService.an")); //$NON-NLS-1$
		contCombo.addListener(SWT.Selection, this);
		nameRequiredButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("GeoNamesService.at_least_one"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite classGroup = new Composite(composite, SWT.NONE);
		classGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		classGroup.setLayout(new GridLayout(2, true));
		aButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.country"), //$NON-NLS-1$
				null);
		aButton.addListener(SWT.Selection, this);
		hButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.stream"), //$NON-NLS-1$
				null);
		hButton.addListener(SWT.Selection, this);
		lButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.parks"), //$NON-NLS-1$
				null);
		lButton.addListener(SWT.Selection, this);
		pButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.city"), //$NON-NLS-1$
				null);
		pButton.addListener(SWT.Selection, this);
		rButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.road"), //$NON-NLS-1$
				null);
		rButton.addListener(SWT.Selection, this);
		sButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.spot"), //$NON-NLS-1$
				null);
		sButton.addListener(SWT.Selection, this);
		tButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.mountain"), //$NON-NLS-1$
				null);
		tButton.addListener(SWT.Selection, this);
		uButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.undersea"), //$NON-NLS-1$
				null);
		uButton.addListener(SWT.Selection, this);
		vButton = WidgetFactory.createCheckButton(classGroup, Messages.getString("GeoNamesService.forest"), //$NON-NLS-1$
				null);
		vButton.addListener(SWT.Selection, this);
		nameRequiredButton.addListener(SWT.Selection, this);
		searchParms = getSearchParms();
		if (searchParms.startsWith("q")) //$NON-NLS-1$
			modeCombo.select(0);
		else if (searchParms.startsWith("name_startsWith")) //$NON-NLS-1$
			modeCombo.select(2);
		else if (searchParms.startsWith("name_equals")) { //$NON-NLS-1$
			modeCombo.select(3);
			nameRequiredButton.setEnabled(false);
		} else if (searchParms.startsWith("name")) //$NON-NLS-1$
			modeCombo.select(1);
		nameRequiredButton.setSelection(searchParms.indexOf("isNameRequired=true") >= 0); //$NON-NLS-1$
		aButton.setSelection(searchParms.indexOf(FEATURE_CLASS_A) >= 0);
		hButton.setSelection(searchParms.indexOf(FEATURE_CLASS_H) >= 0);
		lButton.setSelection(searchParms.indexOf(FEATURE_CLASS_L) >= 0);
		pButton.setSelection(searchParms.indexOf(FEATURE_CLASS_P) >= 0);
		rButton.setSelection(searchParms.indexOf(FEATURE_CLASS_R) >= 0);
		sButton.setSelection(searchParms.indexOf(FEATURE_CLASS_S) >= 0);
		tButton.setSelection(searchParms.indexOf(FEATURE_CLASS_T) >= 0);
		uButton.setSelection(searchParms.indexOf(FEATURE_CLASS_U) >= 0);
		vButton.setSelection(searchParms.indexOf(FEATURE_CLASS_V) >= 0);
		int p = searchParms.indexOf(CONTINENT_CODE);
		if (p >= 0) {
			p += CONTINENT_CODE.length();
			int q = searchParms.indexOf("&", p); //$NON-NLS-1$
			String code = q < 0 ? searchParms.substring(p) : searchParms.substring(p, q);
			for (int i = 0; i < CONTINENTCODES.length; i++)
				if (code.equals(CONTINENTCODES[i]))
					contCombo.select(i + 1);
		} else
			contCombo.select(0);
		return composite;
	}
	
	@Override
	public void handleEvent(Event e) {
		updateSettings();
	}


	protected void updateSettings() {
		StringBuilder sb = new StringBuilder(64);
		switch (modeCombo.getSelectionIndex()) {
		case 0:
			sb.append("q"); //$NON-NLS-1$
			nameRequiredButton.setEnabled(true);
			break;
		case 2:
			sb.append("name_startsWith"); //$NON-NLS-1$
			nameRequiredButton.setEnabled(true);
			break;
		case 3:
			sb.append("name_equals"); //$NON-NLS-1$
			nameRequiredButton.setEnabled(false);
			break;
		default:
			sb.append("name"); //$NON-NLS-1$
			nameRequiredButton.setEnabled(true);
			break;
		}
		sb.append("={0}"); //$NON-NLS-1$
		if (nameRequiredButton.getSelection() && nameRequiredButton.isEnabled())
			sb.append("&isNameRequired=true"); //$NON-NLS-1$
		if (aButton.getSelection())
			sb.append(FEATURE_CLASS_A);
		if (hButton.getSelection())
			sb.append(FEATURE_CLASS_H);
		if (lButton.getSelection())
			sb.append(FEATURE_CLASS_L);
		if (pButton.getSelection())
			sb.append(FEATURE_CLASS_P);
		if (rButton.getSelection())
			sb.append(FEATURE_CLASS_R);
		if (sButton.getSelection())
			sb.append(FEATURE_CLASS_S);
		if (tButton.getSelection())
			sb.append(FEATURE_CLASS_T);
		if (uButton.getSelection())
			sb.append(FEATURE_CLASS_U);
		if (vButton.getSelection())
			sb.append(FEATURE_CLASS_V);
		int si = contCombo.getSelectionIndex();
		if (si > 0)
			sb.append(CONTINENT_CODE).append(CONTINENTCODES[si - 1]);
		searchParms = sb.toString();
	}
	

	@Override
	public void saveSearchParameters() {
		if (searchParms != null)
			GeonamingActivator.getDefault().getPreferenceStore().setValue(
					PreferenceConstants.SEARCHPARAMETERS,
					searchParms);
	}

	protected String getSearchParms() {
		return GeonamingActivator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.SEARCHPARAMETERS);
	}


	@Override
	public double getElevation(double lat, double lon) throws UnknownHostException, HttpException, IOException {
		NumberFormat usformat = NumberFormat.getInstance(Locale.US);
		usformat.setMaximumFractionDigits(5);
		usformat.setGroupingUsed(false);
		InputStream in = openGeonamesService(NLS.bind("http://api.geonames.org/srtm3?lat={0}&lng={1}", //$NON-NLS-1$
				usformat.format(lat), usformat.format(lon)));
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String readLine = reader.readLine();
			if (readLine == null)
				return Double.NaN;
			try {
				double v = Double.parseDouble(readLine);
				return (v < -32000) ? Double.NaN : v;
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
	}

}
