/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.gps.CoordinatesListener;
import com.bdaum.zoom.gps.MaptypeChangedListener;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.GeoService;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsConfiguration;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.gps.internal.Icons;
import com.bdaum.zoom.gps.internal.operations.GeotagOperation;
import com.bdaum.zoom.gps.widgets.IMapComponent;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.gps.RasterCoordinate;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.internal.views.BasicView;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class MapView extends BasicView {

	private class ComboContributionItem extends ControlContribution implements ISelectionChangedListener {

		private ComboViewer viewer;

		protected ComboContributionItem(String id) {
			super(id);
		}

		@Override
		protected Control createControl(Composite parent) {
			viewer = new ComboViewer(parent);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof IConfigurationElement)
						return ((IConfigurationElement) element).getAttribute("name"); //$NON-NLS-1$
					return element.toString();
				}
			});
			viewer.setComparator(new ViewerComparator());
			viewer.setComparer(new IElementComparer() {

				public int hashCode(Object element) {
					if (element instanceof IConfigurationElement)
						return ((IConfigurationElement) element).getAttribute("id").hashCode(); //$NON-NLS-1$
					return 0;
				}

				public boolean equals(Object a, Object b) {
					if (a instanceof IConfigurationElement && b instanceof IConfigurationElement)
						return ((IConfigurationElement) a).getAttribute("id").equals(((IConfigurationElement) b) //$NON-NLS-1$
								.getAttribute("id")); //$NON-NLS-1$
					return false;
				}
			});
			viewer.setInput(GpsActivator.collectMappingSystems());
			IConfigurationElement current = GpsActivator.findCurrentMappingSystem();
			if (current != null)
				viewer.setSelection(new StructuredSelection(current));
			viewer.addSelectionChangedListener(this);
			return viewer.getControl();
		}

		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			if (!sel.isEmpty())
				switchTo((IConfigurationElement) sel.getFirstElement());
		}

		public IStructuredSelection getSelection() {
			return (IStructuredSelection) viewer.getSelection();
		}

		public void setSelection(IStructuredSelection selection) {
			viewer.removeSelectionChangedListener(this);
			viewer.setSelection(selection);
			viewer.addSelectionChangedListener(this);
		}

	}

	private static final String LAST_LONGITUDE = "lastLongitude"; //$NON-NLS-1$
	private static final String LAST_LATITUDE = "lastLatitude"; //$NON-NLS-1$
	public static final String ID = "com.bdaum.zoom.gps.MapView"; //$NON-NLS-1$
	private static final String LAST_ZOOM = "lastZoom"; //$NON-NLS-1$
	private Action lockAction;
	protected boolean locked = true;
	private double lastLongitude = Double.NaN;
	private double lastLatitude = Double.NaN;
	private AssetSelection lastSelection = AssetSelection.EMPTY;
	private Map<RasterCoordinate, Place> placeMap = new HashMap<RasterCoordinate, Place>();
	private Place mapPosition;
	private int initialZoomLevel;
	private IMapComponent mapComponent;
	private IEclipsePreferences.IPreferenceChangeListener preferenceListener;
	private ComboContributionItem comboContributionItem;
	private Composite viewParent;
	private boolean clientClustering = false;
	private Action webAction;
	protected int lastZoom;
	private IConfigurationElement currentConf;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Float lat = memento.getFloat(LAST_LATITUDE);
			Float lon = memento.getFloat(LAST_LONGITUDE);
			if (lat != null && lon != null) {
				lastLatitude = lat.floatValue();
				lastLongitude = lon.floatValue();
			}
			Integer zoom = memento.getInteger(LAST_ZOOM);
			if (zoom != null)
				lastZoom = zoom.intValue();
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null && !Double.isNaN(lastLatitude) && !Double.isNaN(lastLongitude)) {
			memento.putFloat(LAST_LATITUDE, (float) lastLatitude);
			memento.putFloat(LAST_LONGITUDE, (float) lastLongitude);
			memento.putInteger(LAST_ZOOM, lastZoom);
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.viewParent = parent;
		IConfigurationElement conf = GpsActivator.findCurrentMappingSystem();
		createMapComponent(parent, conf);
		makeActions();
		installListeners(parent);
		contributeToActionBars();
		showMap();
		if (mapComponent != null)
			comboContributionItem.setSelection(new StructuredSelection(conf));
		preferenceListener = new IEclipsePreferences.IPreferenceChangeListener() {

			public void preferenceChange(PreferenceChangeEvent event) {
				if (event.getKey().equals(PreferenceConstants.BACKGROUNDCOLOR)) {
					showMap();
				}
			}
		};
		Ui.getUi().addPreferenceChangeListener(preferenceListener);
		Core.getCore().addCatalogListener(new CatalogAdapter() {
			@Override
			public void assetsModified(BagChange<Asset> changes, QueryField node) {
				assetsChanged(getNavigationHistory().getSelectedAssets());
			}
		});
		updateActions();
	}

	private void createMapComponent(Composite parent, IConfigurationElement conf) {
		mapComponent = GpsActivator.getMapComponent(conf);
		if (mapComponent != null) {
			currentConf = conf;
			clientClustering = Boolean.parseBoolean(conf.getAttribute("clientClustering")); //$NON-NLS-1$
			mapComponent.createComponent(parent, true);
			mapComponent.addCoordinatesListener(new CoordinatesListener() {
				public void setCoordinates(double latitude, double longitude, int zoom) {
					updateGeoPosition(latitude, longitude);
					lastLatitude = latitude;
					lastLongitude = longitude;
					lastZoom = zoom;
				}
			});
			mapComponent.addMaptypeListener(new MaptypeChangedListener() {
				public void setMaptype(String maptype) {
					IStructuredSelection sel = comboContributionItem.getSelection();
					if (!sel.isEmpty())
						GpsActivator.setCurrentMapType((IConfigurationElement) sel.getFirstElement(), maptype);
				}
			});
			PlatformUI.getWorkbench().getHelpSystem().setHelp(mapComponent.getControl(), HelpContextIds.MAP_VIEW);
			mapComponent.addStatusTextListener(new StatusTextListener() {
				public void changed(StatusTextEvent event) {
					setStatusMessage(event.text, false);
				}
			});
		}
		addKeyListener();
	}

	protected void switchTo(IConfigurationElement conf) {
		GpsActivator.setCurrentMappingSystem(conf);
		disposeMapComponent();
		createMapComponent(viewParent, conf);
		updateActions();
		viewParent.layout();
		showMap();
	}

	@Override
	public void dispose() {
		disposeMapComponent();
		if (preferenceListener != null)
			Ui.getUi().removePreferenceChangeListener(preferenceListener);
		super.dispose();
	}

	private void disposeMapComponent() {
		if (mapComponent != null) {
			removeKeyListener();
			mapComponent.dispose();
		}
	}

	protected void updateGeoPosition(double latitude, double longitude) {
		if (!lastSelection.isEmpty()) {
			GpsConfiguration gpsConfig = GpsActivator.getDefault().createGpsConfiguration();
			gpsConfig.overwrite = true;
			String[] assetIds = new String[lastSelection.size()];
			int i = 0;
			for (Asset asset : lastSelection)
				assetIds[i++] = asset.getStringId();
			OperationJob.executeOperation(new GeotagOperation(
					new Trackpoint[] { new Trackpoint(latitude, longitude, false) }, assetIds, gpsConfig), this);
			isDirty = true;
		}
	}

	protected void contributeToActionBars() {
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		fillLocalToolBar(toolBarManager);
		IMenuManager menuManager = bars.getMenuManager();
		fillLocalPullDown(menuManager);
		toolBarManager.update(true);
		menuManager.update(true);
	}

	protected void fillLocalPullDown(IMenuManager menuManager) {
		menuManager.add(webAction);
		menuManager.add(lockAction);
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		if (comboContributionItem != null) {
			manager.add(comboContributionItem);
			manager.add(new Separator());
		}
		manager.add(webAction);
		manager.add(lockAction);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		lockAction = new Action(Messages.MapView_lock_locked, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				locked = !locked;
				lockAction.setChecked(locked);
				lockAction.setImageDescriptor((locked ? Icons.closedlock : Icons.openlock).getDescriptor());
				lockAction.setToolTipText(locked ? Messages.MapView_click_to_unlock : Messages.MapView_click_to_lock);
				lockAction.setText(locked ? Messages.MapView_lock_locked : Messages.MapView_lock_unlocked);
				AssetSelection selectedAssets = lastSelection;
				lastSelection = null;
				if (assetsChanged(selectedAssets))
					refresh();
			}
		};
		lockAction.setImageDescriptor(Icons.closedlock.getDescriptor());
		lockAction.setChecked(true);
		lockAction.setToolTipText(Messages.MapView_click_to_unlock);

		webAction = new Action(Messages.MapView_show_in_web, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				try {
					GeoService.showInWebbrowser(lastLatitude, lastLongitude, lastZoom);
				} catch (Exception e) {
					// do nothing
				}
			}
		};
		webAction.setImageDescriptor(Icons.external.getDescriptor());

		createComboContributionItem();
	}

	private void createComboContributionItem() {
		comboContributionItem = new ComboContributionItem("mappingSystem"); //$NON-NLS-1$
	}

	@Override
	public void setFocus() {
		mapComponent.setFocus();
	}

	public void showLocation(Location loc) {
		Place[] markerPositions = null;
		Double latitude = loc.getLatitude();
		Double longitude = loc.getLongitude();
		if (latitude == null)
			latitude = Double.NaN;
		if (longitude == null)
			longitude = Double.NaN;
		mapPosition = new Place(latitude, longitude);
		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			mapPosition.setCountryCode(loc.getCountryISOCode());
			mapPosition.setCountryName(loc.getCountryName());
			String city = loc.getCity();
			mapPosition.setName(city);
			String provinceOrState = loc.getProvinceOrState();
			mapPosition.setState(provinceOrState);
			String sublocation = loc.getSublocation();
			mapPosition.setStreet(sublocation);
			if (sublocation != null && sublocation.length() > 0)
				initialZoomLevel = 14;
			else if (city != null && city.length() > 0)
				initialZoomLevel = 12;
			else if (provinceOrState != null && provinceOrState.length() > 0)
				initialZoomLevel = 7;
			else
				initialZoomLevel = 5;
		} else {
			markerPositions = new Place[] { new Place(latitude, longitude) };
			initialZoomLevel = 12;
		}
		showMap(markerPositions, IMapComponent.LOCATION);
	}

	public void showLocations(AssetSelection selectedAssets) {
		assetsChanged(selectedAssets);
		refresh();
		updateActions();
	}

	public void showMap() {
		Place[] markerPositions = placeMap.values().toArray(new Place[placeMap.size()]);
		int mode;
		switch (placeMap.size()) {
		case 0:
			if (!Double.isNaN(lastLatitude) && !Double.isNaN(lastLongitude)) {
				mapPosition = new Place(lastLatitude, lastLongitude);
				initialZoomLevel = 12;
			} else {
				mapPosition = new Place();
				String country = Locale.getDefault().getCountry();
				if (country.length() == 0) {
					mapPosition.setCountryCode("DE"); //$NON-NLS-1$
					initialZoomLevel = 2;
				} else {
					mapPosition.setCountryCode(country);
					initialZoomLevel = 6;
				}
			}
			mode = (lastSelection.isEmpty()) ? IMapComponent.NONE : IMapComponent.ONE;
			break;
		case 1:
			Collection<Place> values = placeMap.values();
			mapPosition = values.toArray(new Place[values.size()])[0];
			initialZoomLevel = 12;
			mode = IMapComponent.ONE;
			break;
		default:
			mapPosition = new Place();
			initialZoomLevel = computeZoomLevel(markerPositions, mapPosition);
			mode = IMapComponent.MULTI;
			break;
		}
		showMap(markerPositions, mode);
	}

	private void showMap(Place[] markerPositions, int mode) {
		if (mapComponent != null) {
			mapComponent.setInput(mapPosition, initialZoomLevel, markerPositions, null,
					dbIsReadonly() ? IMapComponent.LOCATION : mode);
			if (!Double.isNaN(mapPosition.getLat()) && !Double.isNaN(mapPosition.getLon())) {
				lastLatitude = mapPosition.getLat();
				lastLongitude = mapPosition.getLon();
			}
		}
	}

	private static int computeZoomLevel(Place[] locCreated, Place median) {
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double maxLon = Double.MIN_VALUE;
		for (int i = 0; i < locCreated.length; i++) {
			double lat = locCreated[i].getLat();
			double lon = locCreated[i].getLon();
			if (lat > maxLat)
				maxLat = lat;
			if (lat < minLat)
				minLat = lat;
			if (lon > maxLon)
				maxLon = lon;
			if (lat < minLon)
				minLon = lon;
		}
		median.setLat((minLat + maxLat) / 2);
		median.setLon((minLon + maxLon) / 2);
		double km = Math.max(8d, Core.distance(minLat, minLon, maxLat, maxLon, 'K'));
		int i = 0;
		while (true) {
			if (km > 50000)
				break;
			km *= 2;
			++i;
		}
		return i;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != this)
			setSelection(selection);
	}

	@Override
	public boolean assetsChanged() {
		return assetsChanged(getNavigationHistory().getSelectedAssets());
	}

	protected boolean assetsChanged(AssetSelection selectedAssets) {
		if (locked) {
			lastSelection = selectedAssets;
			int oldMapSize = placeMap.size();
			extractLocations(selectedAssets);
			int mapSize = placeMap.size();
			return (oldMapSize > 0 || mapSize > 0 || mapSize != oldMapSize
					|| lastSelection.isEmpty() != selectedAssets.isEmpty());
		}
		lastSelection = selectedAssets;
		if (!placeMap.isEmpty()) {
			placeMap.clear();
			return true;
		}
		return false;
	}

	private void extractLocations(AssetSelection selectedAssets) {
		placeMap.clear();
		for (Asset asset : selectedAssets.getAssets()) {
			double lat = asset.getGPSLatitude();
			double lon = asset.getGPSLongitude();
			if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
				RasterCoordinate coord = new RasterCoordinate(lat, lon, 2);
				Place place = (Place) (clientClustering ? coord.findExactMatch(placeMap)
						: coord.findClosestMatch(placeMap, 0.06d, 'K'));
				if (place == null) {
					place = new Place(lat, lon);
					place.setImageName(asset.getName());
					placeMap.put(coord, place);
					place.setImageAssetId(asset.getStringId());
				} else {
					String description = place.getImageName();
					if (description.length() > 40) {
						if (!description.endsWith(",...")) //$NON-NLS-1$
							description += ",..."; //$NON-NLS-1$
					} else
						description += ", " + asset.getName(); //$NON-NLS-1$
					place.setImageName(description);
				}
			}
		}
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	public Control getControl() {
		return mapComponent.getControl();
	}

	@Override
	public void refresh() {
		showMap();
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public void updateActions() {
		String query = currentConf == null ? null : currentConf.getAttribute("query"); //$NON-NLS-1$
		webAction.setEnabled(query != null);
		updateActions(-1, -1);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// do nothing
	}

	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// do nothing
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

}
