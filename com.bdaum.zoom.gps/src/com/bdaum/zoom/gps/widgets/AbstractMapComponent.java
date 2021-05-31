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

package com.bdaum.zoom.gps.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.framework.Bundle;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.gps.MapListener;
import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.gps.internal.IMapComponent;
import com.bdaum.zoom.gps.internal.Icons;
import com.bdaum.zoom.gps.internal.Icons.Icon;
import com.bdaum.zoom.gps.internal.dialogs.DirPinDialog;
import com.bdaum.zoom.gps.internal.dialogs.SearchDetailDialog;
import com.bdaum.zoom.gps.internal.preferences.PreferenceConstants;
import com.bdaum.zoom.gps.internal.views.Mapdata;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.program.HtmlEncoderDecoder;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public abstract class AbstractMapComponent implements IMapComponent, IPreferenceChangeListener, Listener, LocationListener {

	private static final String GMAP_ICONS = "gmap/icons/"; //$NON-NLS-1$
	private static final String SHOWN = "shown="; //$NON-NLS-1$
	protected static final String PLUGINS = "/plugins/"; //$NON-NLS-1$
	protected static final String GMAP = "gmap/"; //$NON-NLS-1$

	public class PositionAndZoom {
		public double lat = Double.NaN;
		public double lng = Double.NaN;
		public int zoom = 12;
		public int type = MapListener.ADDLOC;
		public String uuid;

		public PositionAndZoom(double lat, double lng, int zoom, int type) {
			this.lat = lat;
			this.lng = lng;
			this.zoom = zoom;
			this.type = type;
		}

		public PositionAndZoom(String s) {
			StringTokenizer st = new StringTokenizer(s, ",&"); //$NON-NLS-1$
			if (st.hasMoreElements())
				lat = parseDouble(st.nextToken());
			if (st.hasMoreElements())
				lng = parseDouble(st.nextToken());
			if (st.hasMoreElements())
				zoom = parseInt(st.nextToken(), zoom);
			if (st.hasMoreElements())
				type = parseInt(st.nextToken(), type);
			if (st.hasMoreElements())
				uuid = st.nextToken();
		}
	}

	/**
	 * Format for formatting floating point number that are going to be used in HTML
	 * or JavaScript
	 */
	public static final NumberFormat usformat = NumberFormat.getInstance(Locale.US);
	/**
	 * Click location event. Parameters: latitude&longitude
	 */
	protected static final String CLICK = "click"; //$NON-NLS-1$
	/**
	 * Drag location event. Parameters: latitude&longitude
	 */
	protected static final String DRAG = "drag"; //$NON-NLS-1$
	/**
	 * Remove marker location event. No parameters
	 */
	protected static final String REMOVE = "remove"; //$NON-NLS-1$
	/**
	 * CamCount event. Parameter: Number of cameras placed
	 */
	protected static final String CAMCOUNT = "camCount"; //$NON-NLS-1$
	/**
	 * Moved location event. Parameters: moved
	 */
	protected static final Object MOVED = "moved"; //$NON-NLS-1$
	/**
	 * Existing marker modified
	 */
	private static final String MODIFY = "modify"; //$NON-NLS-1$
	/**
	 * Existing marker selected
	 */
	private static final String SELECT = "select"; //$NON-NLS-1$
	/**
	 * Maptype changed location event. Parameters: maptype
	 */
	protected static final String MAPTYPE = "maptype"; //$NON-NLS-1$
	/**
	 * Position notification to be added to history. Parameters:
	 * latitude&longitude&detail
	 */
	protected static final String POS = "pos"; //$NON-NLS-1$
	/**
	 * Show Javascript debug information
	 */
	protected static final String DEBUG = "debug"; //$NON-NLS-1$
	/**
	 * Show marker information
	 */
	protected static final String MARKERINFO = "info"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by scripts and script references
	 */
	protected static final String SCRIPTVAR = "${scripts}"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by style paragraphs
	 */
	protected static final String FORMATVAR = "${format}"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by initial values for script
	 * variables
	 */
	protected static final String CONFIG = "${config}"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by page title
	 */
	private static final String TITLE = "${title}"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by javascript warning
	 */
	private static final String NOSCRIPT = "${noscript}"; //$NON-NLS-1$
	/**
	 * Map file name
	 */
	private static final String MAP_HTML = "map.html"; //$NON-NLS-1$
	/**
	 * Icon names
	 */
	private static final String[] picNames = new String[] { "pinUrl", "camPinUrl", "dirPinUrl", "shownPinUrl", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"primaryIconUrl", "secondaryIconUrl", "primaryIconSelUrl", "secondaryIconSelUrl" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/**
	 * Icon files
	 */
	private static final String[] picFiles = new String[] { "pin.png", "campin.png", "dirpin.png", "shownpin.png", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"camicon.png", "shownicon.png", "camiconsel.png", "showniconsel.png" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/**
	 * Icon URLs
	 */
	private static final String[] picUrls = new String[picFiles.length];

	static {
		URL url = findUrl(GpsActivator.getDefault().getBundle(), GMAP_ICONS);
		String folderUrl = url == null ? GMAP_ICONS : url.toString();
		for (int i = 0; i < picFiles.length; i++)
			picUrls[i] = folderUrl + picFiles[i];
	}

	private ListenerList<MapListener> mapListeners = new ListenerList<>();
	private LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();
	private int historyPosition = 0;
	private String maptype;
	private Composite comp;
	private Browser browser;
	private ToolItem pin1Button;
	private Combo searchCombo;
	private Label explanationLabel;
	private Map<String, WaypointArea> areaMap;
	private ToolItem backButton;
	private ToolItem forwardButton;
	private Composite area;
	private int selectionMode;
	private Composite header;
	private ToolItem pin2Button;
	private int camCount = 0;
	private ToolBar pinbar2;
	private String selectedMarker = ""; //$NON-NLS-1$
	private ToolBar deleteBar;
	private ToolItem deleteButton;
	private Button searchButton;
	private String root = "http://www.photozora.org/zoom/gmap/"; //$NON-NLS-1$
	private Mapdata mapData;

	static {
		usformat.setMaximumFractionDigits(5);
		usformat.setGroupingUsed(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#createComponent(org.eclipse.
	 * swt.widgets.Composite)
	 */

	public void createComponent(Composite parent, boolean header) {
		area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		area.setLayout(layout);
		if (header)
			createHeader();
		comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new FillLayout());
		browser = new Browser(comp, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.addLocationListener(this);
		browser.addListener(SWT.MenuDetect, this);
	}
	
	public void changing(LocationEvent event) {
		String location = event.location;
		if (location.startsWith(root)) {
			String ev = location.substring(root.length());
			try {
				ev = URLDecoder.decode(ev, "utf-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				// should not happen
			}
			event.doit = false;
			int p = ev.indexOf('?');
			String data = ""; //$NON-NLS-1$
			if (p >= 0) {
				data = ev.substring(p + 1);
				ev = ev.substring(0, p);
			}
			if (CLICK.equals(ev) || DRAG.equals(ev)) {
				fireCoordinatesChanged(null, new PositionAndZoom(data));
				updateHistory(data, true);
			} else if (MOVED.equals(ev)) {
				updateHistory(data, false);
			} else if (SELECT.equals(ev)) {
				selectedMarker = data;
				updateControls();
			} else if (MODIFY.equals(ev)) {
				p = data.lastIndexOf('&');
				if (p >= 0) {
					String assetIds = data.substring(p + 1);
					data = data.substring(0, p);
					PositionAndZoom pz = new PositionAndZoom(data);
					if (assetIds.startsWith(SHOWN)) {
						pz.type = MapListener.SHOWNLOC;
						assetIds = assetIds.substring(SHOWN.length());
					}
					List<String> assetList = Core.fromStringList(assetIds, ", "); //$NON-NLS-1$
					fireCoordinatesChanged(assetList.toArray(new String[assetList.size()]), pz);
					updateHistory(data, true);
				}
			} else if (CAMCOUNT.equals(ev)) {
				updateCamCount(data);
			} else if (POS.equals(ev)) {
				updateHistory(data, true);
			} else if (MAPTYPE.equals(ev)) {
				maptype = data;
				fireMaptypeChanged(maptype);
			} else if (MARKERINFO.equals(ev)) {
				p = data.lastIndexOf('&');
				String format = "html"; //$NON-NLS-1$
				if (p >= 0) {
					format = data.substring(p + 1);
					data = data.substring(0, p);
				}
				showMarkerInfo(data, format);
			} else if (DEBUG.equals(ev)) {
				System.out.println(data);
			} else {
				System.err.println(NLS.bind("Unknown event {0}: {1}", ev, data)); //$NON-NLS-1$
			}
		}
	}

	public void changed(LocationEvent event) {
		// do nothing
	}

	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == pin1Button) {
				execute(selectionMode == ONE || selectionMode == CLUSTER || selectionMode == MULTI
						? NLS.bind("cameraPin({0}, {1});", mapData.computeAllTitels(), mapData.computesAllImages()) //$NON-NLS-1$
						: "locationPin();"); //$NON-NLS-1$
			} else if (e.widget == pin2Button) {
				if (camCount > 0) {
					DirPinDialog dialog = new DirPinDialog(header.getShell());
					if (dialog.open() == DirPinDialog.OK) {
						switch (dialog.getResult()) {
						case DirPinDialog.DIRECTION:
							execute("direction();"); //$NON-NLS-1$
							return;
						case DirPinDialog.LOCATIONSHOWN:
							execute(NLS.bind("locationShown({0}, {1});", mapData.computeAllTitels(), //$NON-NLS-1$
									mapData.computesAllImages()));
							break;
						}
					}
				} else
					execute(NLS.bind("locationShown({0}, {1});", mapData.computeAllTitels(), //$NON-NLS-1$
							mapData.computesAllImages()));
			} else if (e.widget == deleteButton) {
				deleteMarker();
			} else if (e.widget == deleteButton) {
				backwards();
			} else if (e.widget == deleteButton) {
				forwards();
			} else if (e.widget == searchCombo) {
				if ((e.stateMask & SWT.CONTROL) != 0) {
					IGeocodingService currentService = getSearchService();
					SearchDetailDialog dialog = new SearchDetailDialog(searchButton.getShell(),
							currentService == null ? null : currentService.getName());
					if (dialog.open() == SearchDetailDialog.OK) {
						String result = dialog.getResult();
						GpsActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.NAMINGSERVICE,
								result);
						updateSearchButton();
					}
				}
				startSearch(e.stateMask);
			}

			break;

		case SWT.KeyDown:
			if (e.character == 13) {
				startSearch(e.stateMask);
				e.doit = false;
			}
			break;
		default:
			e.doit = false;
		}

	}

	protected void updateCamCount(String data) {
		camCount = parseInt(data, camCount);
		updateControls();
	}

	public void createHeader() {
		header = new Composite(area, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		header.setLayout(new GridLayout(7, false));
		ToolBar pinbar = new ToolBar(header, SWT.HORIZONTAL);
		pin1Button = new ToolItem(pinbar, SWT.PUSH);
		pin1Button.setImage(Icons.pin.getImage());
		pin1Button.setToolTipText(Messages.AbstractMapComponent_add_marker);
		pin1Button.addListener(SWT.Selection, this);
		pinbar2 = new ToolBar(header, SWT.HORIZONTAL);
		pin2Button = new ToolItem(pinbar2, SWT.PUSH);
		pin2Button.setImage(Icons.dirPin.getImage());
		pin2Button.setToolTipText(Messages.AbstractMapComponent_add_dir_marker);
		pin2Button.addListener(SWT.Selection, this);
		explanationLabel = new Label(header, SWT.WRAP);
		explanationLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		explanationLabel.setText(Messages.AbstractMapComponent_explanation);
		deleteBar = new ToolBar(header, SWT.HORIZONTAL);
		deleteButton = new ToolItem(deleteBar, SWT.PUSH);
		deleteButton.setImage(Icons.delete.getImage());
		deleteButton.setToolTipText(Messages.AbstractMapComponent_delete_pin);
		deleteButton.addListener(SWT.Selection, this);
		ToolBar navbar = new ToolBar(header, SWT.HORIZONTAL);
		backButton = new ToolItem(navbar, SWT.PUSH);
		backButton.setImage(Icons.backward.getImage());
		backButton.setToolTipText(Messages.AbstractMapComponent_previous_page);
		backButton.addListener(SWT.Selection, this);
		forwardButton = new ToolItem(navbar, SWT.PUSH);
		forwardButton.setImage(Icons.forward.getImage());
		forwardButton.setToolTipText(Messages.AbstractMapComponent_next_page);
		forwardButton.addListener(SWT.Selection, this);
		searchCombo = new Combo(header, SWT.DROP_DOWN);
		searchCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		searchCombo.setVisibleItemCount(8);
		searchCombo.addListener(SWT.KeyDown, this);
		searchCombo.setItems(GpsActivator.getDefault().getSearchHistory());
		searchButton = new Button(header, SWT.PUSH);
		searchButton.setLayoutData(new GridData(120, SWT.DEFAULT));
		searchButton.setText(Messages.AbstractMapComponent_search);
		searchButton.setToolTipText(Messages.AbstractMapComponent_configure);
		searchButton.addListener(SWT.Selection, this);
		updateSearchButton();
		InstanceScope.INSTANCE.getNode(GpsActivator.PLUGIN_ID).addPreferenceChangeListener(this);
	}

	protected void execute(String script) {
		if (!browser.execute(script))
			GpsActivator.getDefault().logError(NLS.bind(Messages.AbstractMapComponent_script_failed, script), null);
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PreferenceConstants.NAMINGSERVICE))
			updateSearchButton();
	}

	private static IGeocodingService getSearchService() {
		return GpsActivator.getDefault().getNamingService();
	}

	private void updateSearchButton() {
		if (searchButton != null) {
			IGeocodingService service = getSearchService();
			if (service == null)
				searchButton.setEnabled(false);
			else {
				searchButton.setEnabled(true);
				searchButton.setText(NLS.bind(Messages.AbstractMapComponent_search_with, service.getName()));
			}
		}
	}

	protected void deleteMarker() {
		if (!selectedMarker.isEmpty()) {
			execute("deleteSelected();"); //$NON-NLS-1$
			PositionAndZoom pz = new PositionAndZoom(""); //$NON-NLS-1$
			if (selectedMarker.startsWith(SHOWN)) {
				selectedMarker = selectedMarker.substring(SHOWN.length());
				pz.type = MapListener.SHOWNLOC;
			}
			List<String> items = Core.fromStringList(selectedMarker, ", "); //$NON-NLS-1$
			fireCoordinatesChanged(items.toArray(new String[items.size()]), pz);
			selectedMarker = ""; //$NON-NLS-1$
			updateControls();
		}
	}

	public void startSearch(int stateMask) {
		String text = searchCombo.getText();
		if (!text.isEmpty())
			try {
				WaypointArea[] areas = GpsUtilities.findLocation(text);
				if (areas != null && areas.length > 0) {
					if (areas.length == 1)
						showLocationPoint(areas[0]);
					else {
						areaMap = new HashMap<String, WaypointArea>();
						for (WaypointArea waypointArea : areas)
							areaMap.put(waypointArea.getName(), waypointArea);
						Set<String> keySet = areaMap.keySet();
						Object[] items = keySet.toArray();
						Arrays.sort(items);
						LocationSelectionDialog dialog = new LocationSelectionDialog(searchCombo.getShell(), items);
						dialog.create();
						dialog.getShell().setLocation(searchCombo.toDisplay(0, 0));
						dialog.open();
						String item = dialog.getResult();
						if (item != null && areaMap != null) {
							WaypointArea waypointArea = areaMap.get(item);
							if (waypointArea != null)
								showLocationPoint(waypointArea);
						}
					}
				} else
					AcousticMessageDialog.openInformation(searchCombo.getShell(),
							Messages.AbstractMapComponent_location_search,
							Messages.AbstractMapComponent_location_not_found);
			} catch (WebServiceException e1) {
				AcousticMessageDialog.openError(searchCombo.getShell(),
						Messages.AbstractMapComponent_web_service_error_search, e1.getMessage());
			} catch (Exception e1) {
				AcousticMessageDialog.openError(searchCombo.getShell(), Messages.AbstractMapComponent_error_search,
						e1.toString());
			}
	}

	protected void showLocationPoint(WaypointArea waypointArea) {
		if (!(Double.isNaN(waypointArea.getNElat()) || Double.isNaN(waypointArea.getNElon())
				|| Double.isNaN(waypointArea.getSWlat()) || Double.isNaN(waypointArea.getSWlon()))) {
			StringBuilder sb = new StringBuilder("setViewPort("); //$NON-NLS-1$
			sb.append("{sw: ").append(Mapdata.latLon(waypointArea.getSWlat(), waypointArea.getSWlon())).append(", ne:") //$NON-NLS-1$//$NON-NLS-2$
					.append(Mapdata.latLon(waypointArea.getNElat(), waypointArea.getNElon())).append("});"); //$NON-NLS-1$
			execute(sb.toString());
			updateCombo(waypointArea.getName());
		} else if (!(Double.isNaN(waypointArea.getLat()) || Double.isNaN(waypointArea.getLon()))) {
			updateCombo(waypointArea.getName());
			if (historyPosition > 0 && historyPosition <= history.size()) {
				HistoryItem historyItem = history.get(historyPosition - 1);
				if (historyItem.getLatitude() == waypointArea.getLat()
						&& historyItem.getLongitude() == waypointArea.getLon() && historyItem.getDetail() >= 12)
					return;
			}
			execute(NLS.bind("setCenter({0},{1});", //$NON-NLS-1$
					Mapdata.latLon(waypointArea.getLat(), waypointArea.getLon()), 12));
		}
	}

	private void updateCombo(String name) {
		if (searchCombo != null) {
			String oldText = searchCombo.getText();
			if (!oldText.isEmpty()) {
				GpsActivator activator = GpsActivator.getDefault();
				String[] newHist = UiUtilities.addToHistoryList(activator.getSearchHistory(), oldText);
				searchCombo.setItems(newHist);
				activator.setSearchHistory(newHist);
			}
			searchCombo.setText(name);
		}
	}

	/**
	 * Notifies listener about coordinate changes
	 *
	 * @param assetIds
	 * @param pz
	 */
	protected void fireCoordinatesChanged(String[] assetIds, PositionAndZoom pz) {
		for (MapListener listener : mapListeners)
			listener.setCoordinates(assetIds, pz.lat, pz.lng, pz.zoom, pz.type, pz.uuid);
	}

	/**
	 * Notifies listener about searches
	 *
	 * @param latitude
	 * @param longitude
	 */
	protected void fireHistoryChanged(double lat, double lon, int zoom) {
		for (MapListener listener : mapListeners)
			listener.historyChanged(lat, lon, zoom);
	}

	/**
	 * Notifies listeners about map type changes
	 *
	 * @param newType
	 */
	protected void fireMaptypeChanged(String newType) {
		for (MapListener listener : mapListeners)
			listener.setMaptype(newType);
	}

	/**
	 * Adds an item to the navigation history
	 *
	 * @param data
	 *            - 'pos' parameters
	 * @param add
	 *            - true if item is added, false if top item is updated
	 */
	protected void updateHistory(String data, boolean add) {
		while (historyPosition < history.size() - 1)
			history.removeLast();
		StringTokenizer st = new StringTokenizer(data, ",&"); //$NON-NLS-1$
		double latitude = Double.NaN;
		double longitude = Double.NaN;
		int detail = 8;
		if (st.hasMoreElements())
			latitude = parseDouble(st.nextToken());
		if (st.hasMoreElements())
			longitude = parseDouble(st.nextToken());
		if (st.hasMoreElements())
			detail = parseInt(st.nextToken(), detail);
		fireHistoryChanged(latitude, longitude, detail);
		if (add) {
			if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
				while (historyPosition < history.size())
					history.removeLast();
				HistoryItem item = new HistoryItem(latitude, longitude, detail);
				if (!history.isEmpty() && history.getLast().equals(item))
					return;
				history.add(item);
				historyPosition++;
			}
			while (history.size() > 99)
				history.removeFirst();
		} else {
			if (history.isEmpty()) {
				HistoryItem item = new HistoryItem(latitude, longitude, detail);
				history.add(item);
				historyPosition++;
			} else {
				HistoryItem item = history.getLast();
				item.setLatitude(latitude);
				item.setLongitude(longitude);
				item.setDetail(detail);
			}
		}
		updateButtons();
	}

	private static double parseDouble(String s) {
		try {
			return Double.parseDouble(s.trim());
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}

	private static int parseInt(String s, int dflt) {
		try {
			return Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	private void updateButtons() {
		if (forwardButton != null)
			forwardButton.setEnabled(historyPosition < history.size());
		if (backButton != null)
			backButton.setEnabled(historyPosition > 1 && !history.isEmpty());
		updateControls();
	}

	/**
	 * Displays an info pop-up for a marker
	 *
	 * @param id
	 *            - image asset ID
	 * @param format
	 *            - html or svg
	 */
	protected void showMarkerInfo(String id, String format) {
		boolean shown = false;
		if (id.startsWith(SHOWN)) {
			shown = true;
			id = id.substring(SHOWN.length());
		}
		int comma = -1;
		if (id != null) {
			comma = id.indexOf(',');
			if (comma >= 0)
				id = id.substring(0, comma);
		}
		if (shown) {
			LocationShownImpl rel = Core.getCore().getDbManager().obtainById(LocationShownImpl.class, id);
			if (rel != null)
				id = rel.getAsset();
		}
		AssetImpl asset = Core.getCore().getDbManager().obtainAsset(id);
		if (asset != null) {
			try {
				File img = ImageActivator.getDefault().createTempFile("Gmap", ".jpg"); //$NON-NLS-1$//$NON-NLS-2$
				img.delete();
				try (FileOutputStream out = new FileOutputStream(img)) {
					out.write(ImageUtilities.asJpeg(asset.getJpegThumbnail()));
				}
				String title = (String) QueryField.TITLEORNAME.obtainFieldValue(asset);
				if (comma >= 0)
					title += Messages.AbstractMapComponent_et_al;
				double factor = 160d / Math.max(asset.getWidth(), asset.getHeight());
				int w = (int) (factor * asset.getWidth());
				int h = (int) (factor * asset.getHeight());
				Date date = UiUtilities.getCreationDate(asset);
				execute(createShowInfoCall(format, img.toURI(), title, w, h,
						date == null ? "" : Format.DFDT.get().format(date))); //$NON-NLS-1$
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Generates a call to Javascript function showInfo(html)
	 *
	 * @param imageUri
	 *            - uri of thumbnail file
	 * @param title
	 *            - image title
	 * @param w
	 *            - display width
	 * @param h
	 *            - display height
	 * @param imageDate
	 *            - image date string
	 * @return Javascript call to be executed
	 */
	protected String createShowInfoCall(String format, URI imageUri, String title, int w, int h, String imageDate) {
		if ("json".equals(format)) //$NON-NLS-1$
			return "showInfo('{\"imageURL\": \"" + imageUri + "\",\"title\": \"" + title + "\",\"subTitle\": \"" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ imageDate + "\"}');"; //$NON-NLS-1$
		return new StringBuilder().append("showInfo('<div><div style=\"padding-bottom:5px;\"><b>").append(title) //$NON-NLS-1$
				.append("</b></div><div><img src=\"").append(imageUri) //$NON-NLS-1$
				.append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" alt=\"") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(title).append("\"/></div><div style=\"padding-top:3px;\"><small>").append(imageDate) //$NON-NLS-1$
				.append("</small></div></div>');").toString(); //$NON-NLS-1$
	}

	protected static URL findUrl(Bundle bundle, String path) {
		try {
			return FileLocator.findFileURL(bundle, "/$nl$/" + path, true); //$NON-NLS-1$
		} catch (IOException e) {
			GpsActivator.getDefault().logError(NLS.bind(Messages.AbstractMapComponent_error_unpacking, path), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#addCoordinatesListener(com.bdaum
	 * .zoom.gps.CoordinatesListener)
	 */

	public void addMapListener(MapListener listener) {
		mapListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#removeCoordinatesListener(com
	 * .bdaum.zoom.gps.CoordinatesListener)
	 */

	public void removeMapListener(MapListener listener) {
		mapListeners.remove(listener);
	}

	/**
	 * Moves one step forward in the history
	 */
	protected void forwards() {
		if (historyPosition < history.size()) {
			execute(createSetPosDetailScript(history.get(historyPosition++)));
			updateButtons();
		}
	}

	/**
	 * Creates Javascript to position and zoom the map
	 *
	 * @param item
	 *            - history item containing location and detail
	 * @return - Javascript to be executed
	 */
	protected String createSetPosDetailScript(HistoryItem item) {
		return NLS.bind("setZoomDetails({0}, {1})", (int) item.getDetail(), //$NON-NLS-1$
				Mapdata.latLon(item.getLatitude(), item.getLongitude()));
	}

	/**
	 * Moves one step backward in the history
	 */
	protected void backwards() {
		if (historyPosition > 1 && !history.isEmpty()) {
			--historyPosition;
			execute(createSetPosDetailScript(history.get(historyPosition - 1)));
			updateButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#addProgressListener(org.eclipse
	 * .swt.browser.ProgressListener)
	 */

	public void addProgressListener(ProgressListener listener) {
		browser.addProgressListener(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#addStatusTextListener(org.
	 * eclipse .swt.browser.StatusTextListener)
	 */

	public void addStatusTextListener(StatusTextListener listener) {
		browser.addStatusTextListener(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#removeProgressListener(org.
	 * eclipse .swt.browser.ProgressListener)
	 */

	public void removeProgressListener(ProgressListener listener) {
		browser.removeProgressListener(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#removeStatusTextListener(org
	 * .eclipse.swt.browser.StatusTextListener)
	 */

	public void removeStatusTextListener(StatusTextListener listener) {
		browser.removeStatusTextListener(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#setFocus()
	 */

	public boolean setFocus() {
		return browser.setFocus();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#getControl()
	 */

	public Control getControl() {
		return area;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#setInitialMapType(java.lang.
	 * String)
	 */

	public void setInitialMapType(String maptype) {
		this.maptype = maptype;
	}

	public String[] getPicAssignments() {
		String folder = PLUGINS + GpsActivator.PLUGIN_ID + '/' + GMAP + "icons/"; //$NON-NLS-1$
		String[] result = new String[picNames.length];
		for (int i = 0; i < picNames.length; i++)
			result[i] = new StringBuilder().append("var ").append(picNames[i]).append(" = \"").append(folder) //$NON-NLS-1$ //$NON-NLS-2$
					.append(picFiles[i]).append("\";\n").toString(); //$NON-NLS-1$
		return result;
	}

	public void setInput(Mapdata mapData, int initialZoomLevel, int mode) {
		this.mapData = mapData;
		selectedMarker = ""; //$NON-NLS-1$
		selectionMode = mode;
		camCount = mapData != null ? mapData.getCamCount() : 0;
		updateControls();
		try {
			StringBuilder sb = new StringBuilder(3000);
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(findMapPage(MAP_HTML).openStream()))) {
				char[] cbuf = new char[2000];
				sb.append(cbuf, 0, reader.read(cbuf));
			}
			replaceText(sb, FORMATVAR, "body{font-family:Arial;font-size:small;background-color:" //$NON-NLS-1$
					+ HtmlEncoderDecoder.createHtmlColor(browser.getBackground()) + "color:" //$NON-NLS-1$
					+ HtmlEncoderDecoder.createHtmlColor(browser.getForeground()) + "height:95%;}"); //$NON-NLS-1$
			replaceText(sb, SCRIPTVAR, createScriptEntries());
			replaceText(sb, TITLE, NLS.bind(Messages.AbstractMapComponent_embedded_map, Constants.APPLICATION_NAME));
			replaceText(sb, NOSCRIPT, Messages.AbstractMapComponent_enable_JS);
			StringBuilder cb = new StringBuilder(2000).append("\nvar root='").append(root) //$NON-NLS-1$
					.append("';\nvar mapIsLoading='") //$NON-NLS-1$
					.append(Messages.AbstractMapComponent_map_is_loading).append("';\nvar initFailed='") //$NON-NLS-1$
					.append(NLS.bind(Messages.AbstractMapComponent_Init_failed, getMappingSystemName()))
					.append("';\nvar newLocationTitle='").append(Messages.AbstractMapComponent_drag_me) //$NON-NLS-1$
					.append("';\nvar notFound='").append(Messages.AbstractMapComponent_not_found) //$NON-NLS-1$
					.append("';\nvar initialDetail=").append(initialZoomLevel) //$NON-NLS-1$
					.append(";\nvar initialMapType, locCreated, locTitles, locImage, imgDirection, track, initialPosition, locShown, locShownTitles, locShownImage;\n"); //$NON-NLS-1$
			for (int i = 0; i < picNames.length; i++)
				cb.append("var ").append(picNames[i]).append(" = \"").append(picUrls[i]) //$NON-NLS-1$ //$NON-NLS-2$
						.append("\";\n"); //$NON-NLS-1$
			String addons = createAdditionalVariables();
			if (addons != null)
				cb.append(addons);
			if (maptype != null)
				cb.append("initialMapType='").append(maptype).append("';\n"); //$NON-NLS-1$ //$NON-NLS-2$
			cb.append("locCreated = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendCameraPositions(cb);
			cb.append("];\nlocTitles = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendCameraTitles(cb);
			cb.append("];\nlocImage = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendCameraImages(cb);
			cb.append("];\nimgDirection = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendCameraDirection(cb);
			cb.append("];\ntrack = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendTrackpoints(cb);
			cb.append("];\nlocShown= ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendSubjectLocation(cb);
			cb.append("];\nlocShownTitles = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendSubjectTitles(cb);
			cb.append("];\nlocShownImage = ["); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendSubjectImages(cb);
			cb.append("];\n"); //$NON-NLS-1$
			if (mapData != null)
				mapData.appendMapPosition(cb);
			replaceText(sb, CONFIG, cb.toString());

			final File copy = ImageActivator.getDefault().createTempFile("Gmap", ".htm"); //$NON-NLS-1$//$NON-NLS-2$
			copy.delete();
			try (FileOutputStream out = new FileOutputStream(copy)) {
				out.write(sb.toString().getBytes("UTF-8")); //$NON-NLS-1$
			}
			browser.getDisplay().asyncExec(() -> {
				if (!browser.isDisposed()) {
					try {
						browser.setUrl(copy.toURI().toURL().toString());
						browser.setFocus();
					} catch (MalformedURLException e) {
						// should not happen
					}
				}
			});
			clearHistory();
		} catch (FileNotFoundException e) {
			GpsActivator.getDefault().logError(NLS.bind(Messages.AbstractMapComponent_resource_missing, MAP_HTML), e);
		} catch (IOException e) {
			GpsActivator.getDefault().logError(NLS.bind(Messages.AbstractMapComponent_io_error_loading, MAP_HTML), e);
		}
		updateButtons();
	}

	public void updateControls() {

		String expl;
		boolean pin1Enabled = false;
		boolean pin2Visible = false;
		boolean deleteEnabled = false;
		boolean deleteVisible = true;
		boolean disposeInvisible = false;
		boolean blank = false;
		Icon icon = Icons.pin;
		if (pin1Button != null)
			pin1Button.setImage(icon.getImage());
		switch (selectionMode) {
		case ADDLOCATION:
			expl = Messages.AbstractMapComponent_create_new_location;
			pin1Enabled = true;
			deleteVisible = false;
			disposeInvisible = true;
			break;
		case LOCATION:
			expl = ""; //$NON-NLS-1$
			deleteVisible = false;
			break;
		case TRACK:
			expl = ""; //$NON-NLS-1$
			blank = false;
			deleteVisible = false;
			disposeInvisible = true;
			break;
		case BLANK:
			expl = ""; //$NON-NLS-1$
			blank = true;
			deleteVisible = false;
			break;
		case AREA:
			expl = Messages.AbstractMapComponent_define_center;
			pin1Enabled = true;
			deleteVisible = false;
			disposeInvisible = true;
			break;
		case NONE:
			expl = Messages.AbstractMapComponent_no_images;
			deleteVisible = false;
			break;
		case ONE:
			expl = Messages.AbstractMapComponent_single_image;
			pin1Enabled = true;
			pin2Visible = true;
			deleteEnabled = !selectedMarker.isEmpty();
			icon = Icons.camPin;
			break;
		case CLUSTER:
			expl = Messages.AbstractMapComponent_cluster;
			pin1Enabled = true;
			pin2Visible = true;
			deleteEnabled = !selectedMarker.isEmpty();
			icon = Icons.camPin;
			break;
		default:
			expl = Messages.AbstractMapComponent_multiple_images;
			pin1Enabled = true;
			deleteEnabled = !selectedMarker.isEmpty();
			icon = Icons.camPin;
			break;
		}
		if (explanationLabel != null)
			explanationLabel.setText(expl);
		if (pin1Button != null) {
			pin1Button.setEnabled(pin1Enabled);
			pin1Button.setToolTipText(pin1Enabled ? Messages.AbstractMapComponent_add_marker
					: Messages.AbstractMapComponent_geotagging_disabled);
			pin1Button.setImage(icon.getImage());
		}
		if (pin2Button != null) {
			pinbar2.setVisible(pin2Visible);
			if (pin2Visible) {
				if (camCount > 0) {
					pin2Button.setImage(Icons.dirPin.getImage());
					pin2Button.setToolTipText(Messages.AbstractMapComponent_add_dir_marker);
				} else {
					pin2Button.setImage(Icons.shownPin.getImage());
					pin2Button.setToolTipText(Messages.AbstractMapComponent_click_shown_loc);
				}
				pin2Button.setEnabled(true);
			} else if (disposeInvisible) {
				pinbar2.dispose();
				pin2Button = null;
			}
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(deleteEnabled);
			deleteBar.setVisible(deleteVisible);
			if (disposeInvisible && !deleteVisible) {
				deleteBar.dispose();
				deleteButton = null;
			}
		}
		area.setVisible(!blank);
		if (disposeInvisible)
			area.layout(true, true);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#setArea(double, double, double)
	 */
	@Override
	public void setArea(double latitude, double longitude, double km) {
		if (!(Double.isNaN(latitude) || Double.isNaN(longitude)))
			execute(NLS.bind("setAreaCircle({0},{1});", //$NON-NLS-1$
					Mapdata.latLon(latitude, longitude), (int) (km * 1000)));
	}

	protected abstract String getMappingSystemName();

	/**
	 * Return URL for requested map page
	 *
	 * @param mapPage
	 *            - map page name
	 * @return map page URL
	 */
	protected URL findMapPage(String mapPage) {
		return findUrl(GpsActivator.getDefault().getBundle(), "gmap/" + mapPage); //$NON-NLS-1$
	}

	/**
	 * Clears the navigation history
	 */
	protected void clearHistory() {
		history.clear();
		historyPosition = 0;
		updateButtons();
	}

	/**
	 * Generates the required script links to be attached to the generated HTML page
	 *
	 * @return - HTML script links
	 */
	protected abstract String createScriptEntries();

	/**
	 * Create Javascript for creating a new latitude-longitude instance
	 *
	 * @param lat
	 *            - latitude
	 * @param lon
	 *            - longitude
	 * @return - script
	 */
	// protected abstract String createLatLng(double lat, double lon);

	/**
	 * Create Javascript for creating a new latitude-longitude area instance
	 *
	 * @param swLat
	 * @param swLon
	 * @param neLat
	 * @param neLon
	 * @return
	 */
	// protected abstract String createLatLngBounds(double swLat, double swLon,
	// double neLat, double neLon);

	/**
	 * Creates a HTML script link to the specifed URL
	 *
	 * @param url
	 *            - script URL
	 * @return - HTML script link
	 */
	protected String createScriptEntry(String url) {
		return new StringBuilder().append("<script src=\"").append(url).append("\" type=\"text/javascript\"></script>") //$NON-NLS-1$ //$NON-NLS-2$
				.toString();
	}

	/**
	 * Creates a style link to the specifed URL
	 *
	 * @param url
	 *            - css URL
	 * @return - HTML style link
	 */
	protected String createStyleEntry(String url) {
		return new StringBuilder().append("<link rel=\"stylesheet\" href=\"").append(url).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.toString();
	}

	private static void replaceText(StringBuilder sb, String var, String text) {
		int p = sb.indexOf(var);
		if (p >= 0) {
			if (text == null || text.isEmpty())
				sb.delete(p, p + var.length());
			else
				sb.replace(p, p + var.length(), text);
		}
	}

	public HistoryItem getLastHistoryItem() {
		if (historyPosition > 0 && historyPosition <= history.size())
			return history.get(historyPosition - 1);
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.widgets.IMapComponent#refresh()
	 */
	public void refresh() {
		browser.refresh();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.gps.IMapComponent#dispose()
	 */

	public void dispose() {
		InstanceScope.INSTANCE.getNode(GpsActivator.PLUGIN_ID).removePreferenceChangeListener(this);
		getControl().dispose();
	}

	@Override
	public void setRoot(String root) {
		this.root = root;
	}

}