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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.gps.CoordinatesListener;
import com.bdaum.zoom.gps.MaptypeChangedListener;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.gps.internal.Icons;
import com.bdaum.zoom.gps.internal.Icons.Icon;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.gps.WaypointArea;

@SuppressWarnings("restriction")
public abstract class AbstractMapComponent implements IMapComponent {

	public class PositionAndZoom {
		public double lat = Double.NaN;
		public double lng = Double.NaN;
		public int zoom = 12;
		public int type = CoordinatesListener.ADDLOC;

		public PositionAndZoom(double lat, double lng, int zoom, int type) {
			this.lat = lat;
			this.lng = lng;
			this.zoom = zoom;
			this.type = type;
		}

		public PositionAndZoom(String s) {
			StringTokenizer st = new StringTokenizer(s, "(),&"); //$NON-NLS-1$
			if (st.hasMoreElements())
				lat = parseDouble(st.nextToken());
			if (st.hasMoreElements())
				lng = parseDouble(st.nextToken());
			if (st.hasMoreElements())
				zoom = parseInt(st.nextToken(), zoom);
			if (st.hasMoreElements())
				type = parseInt(st.nextToken(), type);
		}
	}

	/**
	 * Instances of this class remember a step in the map navigation history
	 */
	protected static class HistoryItem {
		private double latitude;

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public void setDetail(double detail) {
			this.detail = detail;
		}

		private double longitude;
		private double detail;

		/**
		 * @param latitude
		 *            - latitude of position
		 * @param longitude
		 *            - longitude of position
		 * @param detail
		 *            - detail of position (zoomlevel or view range)
		 */
		public HistoryItem(double latitude, double longitude, double detail) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.detail = detail;
		}

		/**
		 * @return the latitude
		 */
		public double getLatitude() {
			return latitude;
		}

		/**
		 * @return the longitude
		 */
		public double getLongitude() {
			return longitude;
		}

		/**
		 * @return the detail
		 */
		public double getDetail() {
			return detail;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof HistoryItem))
				return false;
			HistoryItem other = (HistoryItem) obj;
			return this.latitude == other.latitude && this.longitude == other.longitude && this.detail == other.detail;
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Format for formatting floating point number that are going to be used in
	 * HTML or JavaScript
	 */
	public static final NumberFormat usformat = NumberFormat.getInstance(Locale.US);
	/**
	 * Location change events with this URL are vetoed Their only purpose is to
	 * transport data to the Java classes and/or trigger actions there
	 */
	protected static final String GMAPEVENTURL = "http://www.bdaum.de/zoom/gmap/"; //$NON-NLS-1$
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
	 * Mapmode changed location event. Parameters: empty string or 3D
	 */

	protected static final Object MAPMODE = "mapmode"; //$NON-NLS-1$
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
	 * Show markerinfo location event. Parameters: image asset ID
	 */
	protected static final String DEBUG = "debug"; //$NON-NLS-1$
	/**
	 * Show Javascript debug information
	 */
	protected static final String MARKERINFO = "info"; //$NON-NLS-1$
	/**
	 * Variable in HTML templates to be replaced by scripts and script
	 * references
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

	private static final String MAP_HTML = "map.html"; //$NON-NLS-1$

	// private
	private ListenerList<CoordinatesListener> coordinatesListeners = new ListenerList<CoordinatesListener>();
	private ListenerList<MaptypeChangedListener> maptypeListeners = new ListenerList<MaptypeChangedListener>();
	private LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();
	private int historyPosition = 0;
	private String maptype;
	private Composite comp;
	private Browser browser;
	private ToolItem pin1Button;
	private Combo combo;
	private Label explanationLabel;
	private Map<String, WaypointArea> areaMap;
	private ToolItem backButton;
	private ToolItem forwardButton;
	private Composite area;
	protected String mapmode;
	private int selectionMode;
	private Composite header;
	private ToolItem pin2Button;
	private int camCount = 0;
	private ToolBar pinbar2;

	static {
		usformat.setMaximumFractionDigits(5);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#createComponent(org.eclipse.
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
		findResources();
		comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new FillLayout());
		browser = new Browser(comp, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.addLocationListener(new LocationListener() {

			public void changing(LocationEvent event) {
				String location = event.location;
				if (location.startsWith(GMAPEVENTURL)) {
					String ev = location.substring(GMAPEVENTURL.length());
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
					} else if (MODIFY.equals(ev)) {
						p = data.lastIndexOf('&');
						if (p >= 0) {
							String assetIds = data.substring(p + 1);
							data = data.substring(0, p);
							List<String> assetList = assetIds == null ? null : Core.fromStringList(assetIds, ", "); //$NON-NLS-1$
							fireCoordinatesChanged(assetList.toArray(new String[assetList.size()]),
									new PositionAndZoom(data));
							updateHistory(data, true);
						}
					} else if (CAMCOUNT.equals(ev)) {
						updateCamCount(data);
					} else if (POS.equals(ev)) {
						updateHistory(data, true);
					} else if (MAPTYPE.equals(ev)) {
						maptype = data;
						fireMaptypeChanged(maptype);
					} else if (MAPMODE.equals(ev)) {
						mapmode = data;
						updateButtons();
					} else if (MARKERINFO.equals(ev)) {
						showMarkerInfo(data);
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
		});
		browser.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	protected void updateCamCount(String data) {
		camCount = parseInt(data, camCount);
		updateControls();
	}

	public void createHeader() {
		header = new Composite(area, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		header.setLayout(new GridLayout(6, false));
		ToolBar pinbar = new ToolBar(header, SWT.HORIZONTAL);
		pin1Button = new ToolItem(pinbar, SWT.PUSH);
		pin1Button.setImage(Icons.pin.getImage());
		pin1Button.setToolTipText(Messages.AbstractMapComponent_add_marker);
		pin1Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.execute(selectionMode == ONE || selectionMode == CLUSTER || selectionMode == MULTI ? "camera();" //$NON-NLS-1$
						: "location();"); //$NON-NLS-1$
			}
		});
		pinbar2 = new ToolBar(header, SWT.HORIZONTAL);
		pin2Button = new ToolItem(pinbar2, SWT.PUSH);
		pin2Button.setImage(Icons.dirPin.getImage());
		pin2Button.setToolTipText(Messages.AbstractMapComponent_add_dir_marker);
		pin2Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.execute("direction();"); //$NON-NLS-1$
			}
		});
		explanationLabel = new Label(header, SWT.WRAP);
		explanationLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		explanationLabel.setText(Messages.AbstractMapComponent_explanation);
		ToolBar navbar = new ToolBar(header, SWT.HORIZONTAL);
		backButton = new ToolItem(navbar, SWT.PUSH);
		backButton.setImage(Icons.backward.getImage());
		backButton.setToolTipText(Messages.AbstractMapComponent_previous_page);
		backButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				backwards();
			}
		});
		forwardButton = new ToolItem(navbar, SWT.PUSH);
		forwardButton.setImage(Icons.forward.getImage());
		forwardButton.setToolTipText(Messages.AbstractMapComponent_next_page);
		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				forwards();
			}
		});

		combo = new Combo(header, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(200, SWT.DEFAULT));
		combo.setVisibleItemCount(8);
		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == 13) {
					startSearch();
					e.doit = false;
				}
			}
		});
		combo.setItems(GpsActivator.getDefault().getSearchHistory());
		final Button searchButton = new Button(header, SWT.PUSH);
		searchButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		searchButton.setText(Messages.AbstractMapComponent_search);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startSearch();
			}
		});
	}

	public void startSearch() {
		String text = combo.getText();
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
					String[] items = keySet.toArray(new String[keySet.size()]);
					Arrays.sort(items);
					LocationSelectionDialog dialog = new LocationSelectionDialog(combo.getShell(), items);
					dialog.create();
					dialog.getShell().setLocation(combo.toDisplay(0, 0));
					dialog.open();
					String item = dialog.getResult();
					if (item != null && areaMap != null) {
						WaypointArea waypointArea = areaMap.get(item);
						if (waypointArea != null)
							showLocationPoint(waypointArea);
					}
				}
			} else
				AcousticMessageDialog.openInformation(combo.getShell(), Messages.AbstractMapComponent_location_search,
						Messages.AbstractMapComponent_location_not_found);
		} catch (WebServiceException e1) {
			AcousticMessageDialog.openError(combo.getShell(), Messages.AbstractMapComponent_web_service_error_search,
					e1.getMessage());
		} catch (Exception e1) {
			AcousticMessageDialog.openError(combo.getShell(), Messages.AbstractMapComponent_error_search,
					e1.toString());
		}
	}

	protected void showLocationPoint(WaypointArea waypointArea) {
		if (!(Double.isNaN(waypointArea.getNElat()) || Double.isNaN(waypointArea.getNElon())
				|| Double.isNaN(waypointArea.getSWlat()) || Double.isNaN(waypointArea.getSWlon()))) {
			browser.execute(NLS.bind("setViewPort({0});", //$NON-NLS-1$
					createLatLngBounds(waypointArea.getSWlat(), waypointArea.getSWlon(), waypointArea.getNElat(),
							waypointArea.getNElon())));
			updateCombo(waypointArea.getName());
		} else if (!(Double.isNaN(waypointArea.getLat()) || Double.isNaN(waypointArea.getLon()))) {
			browser.execute(NLS.bind("setCenter({0},{1});", //$NON-NLS-1$
					createLatLng(waypointArea.getLat(), waypointArea.getLon()), 12));
			updateCombo(waypointArea.getName());
		}
	}

	private void updateCombo(String name) {
		if (combo != null) {
			String oldText = combo.getText();
			if (!oldText.isEmpty()) {
				GpsActivator activator = GpsActivator.getDefault();
				String[] history = activator.getSearchHistory();
				if (history.length >= GpsActivator.MAXHISTORYLENGTH) {
					System.arraycopy(history, 0, history, 1, history.length - 1);
					history[0] = oldText;
					activator.setSearchHistory(history);
					combo.setItems(history);
				} else {
					String[] newHistory = new String[history.length + 1];
					System.arraycopy(history, 0, newHistory, 1, history.length);
					newHistory[0] = oldText;
					activator.setSearchHistory(newHistory);
					combo.setItems(newHistory);
				}
			}
			combo.setText(name);
		}
	}

	/**
	 * Notifies listener about coordinate changes
	 *
	 * @param latitude
	 * @param longitude
	 */
	protected void fireCoordinatesChanged(String[] assetIds, PositionAndZoom pz) {
		for (Object listener : coordinatesListeners.getListeners())
			((CoordinatesListener) listener).setCoordinates(assetIds, pz.lat, pz.lng, pz.zoom, pz.type);
	}

	/**
	 * Notifies listeners about map type changes
	 *
	 * @param newType
	 */
	protected void fireMaptypeChanged(String newType) {
		for (Object listener : maptypeListeners.getListeners())
			((MaptypeChangedListener) listener).setMaptype(newType);
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
		StringTokenizer st = new StringTokenizer(data, "(),&"); //$NON-NLS-1$
		double latitude = Double.NaN;
		double longitude = Double.NaN;
		int detail = 8;
		if (st.hasMoreElements())
			latitude = parseDouble(st.nextToken());
		if (st.hasMoreElements())
			longitude = parseDouble(st.nextToken());
		if (st.hasMoreElements())
			detail = parseInt(st.nextToken(), detail);
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
	 */
	protected void showMarkerInfo(String id) {
		int comma = -1;
		if (id != null) {
			comma = id.indexOf(',');
			if (comma >= 0)
				id = id.substring(0, comma);
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
				Date date = asset.getDateCreated();
				if (date == null)
					date = asset.getDateTimeOriginal();
				if (date == null)
					date = asset.getDateTime();
				browser.execute(
						createShowInfoCall(img.toURI(), title, w, h, date == null ? "" : Constants.DFDT.format(date))); //$NON-NLS-1$
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
	protected String createShowInfoCall(URI imageUri, String title, int w, int h, String imageDate) {
		return new StringBuilder().append("showInfo('<div><div style=\"padding-bottom:5px;\"><b>").append(title) //$NON-NLS-1$
				.append("</b></div><div><img src=\"").append(imageUri) //$NON-NLS-1$
				.append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" alt=\"") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(title).append("\"/></div><div style=\"padding-top:3px;\"><small>").append(imageDate) //$NON-NLS-1$
				.append("</small></div></div>');").toString(); //$NON-NLS-1$
	}

	protected abstract void findResources();

	private URL findUrl(String path) {
		return findUrl(GpsActivator.getDefault().getBundle(), path);
	}

	protected URL findUrl(Bundle bundle, String path) {
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

	public void addCoordinatesListener(CoordinatesListener listener) {
		coordinatesListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#addMaptypeListener(com.bdaum
	 * .zoom.gps.MaptypeChangedListener)
	 */

	public void addMaptypeListener(MaptypeChangedListener listener) {
		maptypeListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#removeCoordinatesListener(com
	 * .bdaum.zoom.gps.CoordinatesListener)
	 */

	public void removeCoordinatesListener(CoordinatesListener listener) {
		coordinatesListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#removeMaptypeListener(com.bdaum
	 * .zoom.gps.MaptypeChangedListener)
	 */

	public void removeMaptypeListener(MaptypeChangedListener listener) {
		maptypeListeners.remove(listener);
	}

	/**
	 * Moves one step forward in the history
	 */
	protected void forwards() {
		if (historyPosition < history.size()) {
			browser.execute(createSetPosDetailScript(history.get(historyPosition++)));
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
	protected abstract String createSetPosDetailScript(HistoryItem item);

	/**
	 * Moves one step backward in the history
	 */
	protected void backwards() {
		if (historyPosition > 1 && !history.isEmpty()) {
			--historyPosition;
			browser.execute(createSetPosDetailScript(history.get(historyPosition - 1)));
			updateButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#addProgressListener(org.eclipse
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
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#removeStatusTextListener(org
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
	 * @see
	 * com.bdaum.zoom.gps.widgets.IMapComponent#setInitialMapType(java.lang.
	 * String)
	 */

	public void setInitialMapType(String maptype) {
		this.maptype = maptype;
	}

	public void setInput(Place mapPosition, int initialZoomLevel, Place[] markerPositions, Trackpoint[] trackpoints,
			int mode) {
		selectionMode = mode;
		updateControls();
		try {
			StringBuilder sb = new StringBuilder();
			URL url = findMapPage(MAP_HTML);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(line);
				}
			}
			replaceText(sb, FORMATVAR,
					"body{font-family:Arial;font-size:small;background-color:" //$NON-NLS-1$
							+ createHtmlColor(browser.getBackground()) + "color:" //$NON-NLS-1$
							+ createHtmlColor(browser.getForeground()) + "height:95%;}"); //$NON-NLS-1$
			replaceText(sb, SCRIPTVAR, createScriptEntries());
			replaceText(sb, TITLE, NLS.bind(Messages.AbstractMapComponent_embedded_map, Constants.APPLICATION_NAME));
			replaceText(sb, NOSCRIPT, Messages.AbstractMapComponent_enable_JS);
			StringBuilder cb = new StringBuilder().append("\nvar mapIsLoading='") //$NON-NLS-1$
					.append(Messages.AbstractMapComponent_map_is_loading).append("';\nvar initFailed='") //$NON-NLS-1$
					.append(NLS.bind(Messages.AbstractMapComponent_Init_failed, getMappingSystemName()))
					.append("';\nvar newLocationTitle='").append(Messages.AbstractMapComponent_drag_me) //$NON-NLS-1$
					.append("';\nvar notFound='").append(Messages.AbstractMapComponent_not_found) //$NON-NLS-1$
					.append("';\nvar applicationKey"); //$NON-NLS-1$
			String appkey = getAppKey();
			if (appkey != null)
				cb.append("=").append(appkey).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
			cb.append(";\nvar initialDetail=").append(computeInitialDetail(initialZoomLevel, markerPositions))//$NON-NLS-1$
					.append(";\nvar initialMapType, locCreated, locTitles, locImage, imgDirection, track, initialPosition;\n"); //$NON-NLS-1$
			cb.append("var pinUrl = \"").append(findUrl("/gmap/icons/pin.png")) //$NON-NLS-1$ //$NON-NLS-2$
					.append("\";\n"); //$NON-NLS-1$
			cb.append("var camPinUrl = \"").append(findUrl("/gmap/icons/campin.png")) //$NON-NLS-1$ //$NON-NLS-2$
					.append("\";\n"); //$NON-NLS-1$
			cb.append("var dirPinUrl = \"").append(findUrl("/gmap/icons/dirpin.png")) //$NON-NLS-1$ //$NON-NLS-2$
					.append("\";\n"); //$NON-NLS-1$

			String addons = createAdditionalVariables();
			if (addons != null)
				cb.append(addons);
			if (maptype != null)
				cb.append("initialMapType='").append(maptype).append("';\n"); //$NON-NLS-1$ //$NON-NLS-2$
			cb.append("locCreated = ["); //$NON-NLS-1$
			if (markerPositions != null && markerPositions.length > 0) {
				Place last = markerPositions[markerPositions.length - 1];
				for (Place place : markerPositions) {
					cb.append(createLatLng(place.getLat(), place.getLon()));
					if (place != last)
						cb.append(",\n"); //$NON-NLS-1$
				}
			}
			cb.append("];\nlocTitles = ["); //$NON-NLS-1$
			if (markerPositions != null && markerPositions.length > 0) {
				Place last = markerPositions[markerPositions.length - 1];
				for (Place place : markerPositions) {
					cb.append('"').append(place.getImageName()).append('"');
					if (place != last)
						cb.append(",\n"); //$NON-NLS-1$
				}
			}
			cb.append("];\nlocImage = ["); //$NON-NLS-1$
			if (markerPositions != null && markerPositions.length > 0) {
				Place last = markerPositions[markerPositions.length - 1];
				for (Place place : markerPositions) {
					List<String> imageAssetIds = place.getImageAssetIds();
					cb.append('[');
					Iterator<String> iterator = imageAssetIds.iterator();
					while (iterator.hasNext()) {
						cb.append('"').append(iterator.next()).append('"');
						if (iterator.hasNext())
							cb.append(", "); //$NON-NLS-1$
					}
					cb.append(']');
					if (place != last)
						cb.append(",\n"); //$NON-NLS-1$
				}
			}
			cb.append("];\n"); //$NON-NLS-1$
			cb.append("imgDirection = ["); //$NON-NLS-1$
			if (markerPositions != null && markerPositions.length > 0) {
				Place last = markerPositions[markerPositions.length - 1];
				for (Place place : markerPositions) {
					if (!Double.isNaN(place.getDirection()))
						cb.append(place.getDirection());
					else
						cb.append("NaN"); //$NON-NLS-1$
					if (place != last)
						cb.append(",\n"); //$NON-NLS-1$
				}
			}
			cb.append("];\n"); //$NON-NLS-1$
			cb.append("track = ["); //$NON-NLS-1$
			if (trackpoints != null && trackpoints.length > 0) {
				Trackpoint last = trackpoints[trackpoints.length - 1];
				for (Trackpoint pnt : trackpoints) {
					cb.append(createLatLng(pnt.getLatitude(), pnt.getLongitude()));
					if (pnt != last)
						cb.append(",\n"); //$NON-NLS-1$
				}
			}
			cb.append("];\n"); //$NON-NLS-1$
			if (mapPosition != null) {
				if (Double.isNaN(mapPosition.getLat()) || Double.isNaN(mapPosition.getLon())) {
					StringBuilder buf = new StringBuilder();
					condAppend(mapPosition.getStreet(), buf);
					condAppend(mapPosition.getName(), buf);
					condAppend(mapPosition.getState(), buf);
					condAppend(mapPosition.getCountryName(), buf);
					condAppend(getCountryCode(mapPosition.getCountryCode()), buf);
					try {
						WaypointArea[] pnts = GpsUtilities.findLocation(buf.toString());
						if (pnts != null && pnts.length > 0) {
							mapPosition.setLat(pnts[0].getLat());
							mapPosition.setLon(pnts[0].getLon());
						}
					} catch (Exception e) {
						// no result
					}
				}
				if (!(Double.isNaN(mapPosition.getLat()) || Double.isNaN(mapPosition.getLon()))) {
					cb.append("initialPosition=") //$NON-NLS-1$
							.append(createLatLng(mapPosition.getLat(), mapPosition.getLon()));
					cb.append(";\n"); //$NON-NLS-1$
				}
			}
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
		boolean blank = false;
		Icon icon = Icons.pin;
		pin1Button.setImage(Icons.pin.getImage());
		if ("3D".equals(mapmode)) //$NON-NLS-1$
			expl = Messages.AbstractMapComponent_no_tagging_possible;
		else
			switch (selectionMode) {
			case ADDLOCATION:
				expl = Messages.AbstractMapComponent_create_new_location;
				pin1Enabled = true;
				break;
			case LOCATION:
				expl = ""; //$NON-NLS-1$
				break;
			case TRACK:
				expl = ""; //$NON-NLS-1$
				blank = true;
				break;
			case BLANK:
				expl = ""; //$NON-NLS-1$
				blank = true;
				break;
			case NONE:
				expl = Messages.AbstractMapComponent_no_images;
				break;
			case ONE:
				expl = Messages.AbstractMapComponent_single_image;
				pin1Enabled = true;
				pin2Visible = true;
				icon = Icons.camPin;
				break;
			case CLUSTER:
				expl = Messages.AbstractMapComponent_cluster;
				pin1Enabled = true;
				pin2Visible = true;
				icon = Icons.camPin;
				break;
			default:
				expl = Messages.AbstractMapComponent_multiple_images;
				pin1Enabled = true;
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
			if (pin2Visible)
				pin2Button.setEnabled(camCount > 0);
		}
		area.setVisible(!blank);
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
		return findUrl("gmap/" + mapPage); //$NON-NLS-1$
	}

	/**
	 * Computes initial detail setting from initial zoom level
	 *
	 * @param initialZoomLevel
	 *            (0-20)
	 * @return initial detail depending on mapping system
	 */
	protected int computeInitialDetail(int initialZoomLevel, Place[] markerPositions) {
		return initialZoomLevel;
	}

	/**
	 * Delivers the application key
	 *
	 * @return application key
	 */
	protected abstract String getAppKey();

	/**
	 * Returns a country code understood by the mapping system Default
	 * implementation: 2-byte ISO country code
	 *
	 * @param input
	 *            - 2- or 3-byte country code or null
	 * @return - Country code understood by the mapping system
	 */
	protected String getCountryCode(String input) {
		if (input != null) {
			if (input.length() == 3) {
				Locale[] availableLocales = Locale.getAvailableLocales();
				for (Locale locale : availableLocales)
					try {
						if (input.equals(locale.getISO3Country()))
							return locale.getCountry();
					} catch (MissingResourceException e) {
						// ignore this Locale
					}
			}
		}
		return input;
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
	 * Creates Javascript to declare and initialize additional script variables
	 *
	 * @return Javascript to be added to the configuration section of the
	 *         generated HTML page
	 */
	protected abstract String createAdditionalVariables();

	/**
	 * Generates the required script links to be attached to the generated HTML
	 * page
	 *
	 * @return - HTML script links
	 */
	protected abstract String createScriptEntries();

	private static void condAppend(String token, StringBuilder buf) {
		if (token != null) {
			if (buf.length() > 0)
				buf.append(", "); //$NON-NLS-1$
			buf.append(token);
		}
	}

	/**
	 * Create Javascript for creating a new latitude-longitude instance
	 *
	 * @param lat
	 *            - latitude
	 * @param lon
	 *            - longitude
	 * @return - script
	 */
	protected abstract String createLatLng(double lat, double lon);

	/**
	 * Create Javascript for creating a new latitude-longitude area instance
	 *
	 * @param swLat
	 * @param swLon
	 * @param neLat
	 * @param neLon
	 * @return
	 */
	protected abstract String createLatLngBounds(double swLat, double swLon, double neLat, double neLon);

	/**
	 * Creates an HTML color specification
	 *
	 * @param color
	 *            - SWT color
	 * @return - HTML color specification
	 */
	protected String createHtmlColor(Color color) {
		RGB rgb = color.getRGB();
		return new StringBuilder().append("rgb(").append(rgb.red).append(",").append(rgb.green) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append(rgb.blue).append(");").toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a HTML script link to the specifed URL
	 *
	 * @param url
	 *            - script URL
	 * @return - HTML script link
	 */
	protected String createScriptEntry(URL url) {
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
	protected String createStyleEntry(URL url) {
		return new StringBuilder().append("<link rel=\"stylesheet\" href=\"").append(url).append("\"></link>") //$NON-NLS-1$ //$NON-NLS-2$
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
		getControl().dispose();
	}

}