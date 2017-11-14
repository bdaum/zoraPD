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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.gps.CoordinatesListener;
import com.bdaum.zoom.gps.MaptypeChangedListener;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.GeoService;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsConfiguration;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.gps.internal.Icons;
import com.bdaum.zoom.gps.internal.operations.GeodirOperation;
import com.bdaum.zoom.gps.internal.operations.GeoshownOperation;
import com.bdaum.zoom.gps.internal.operations.GeotagOperation;
import com.bdaum.zoom.gps.widgets.IMapComponent;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.IProfiledOperation;
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
	private double lastLongitude = Double.NaN;
	private double lastLatitude = Double.NaN;
	private AssetSelection lastSelection = AssetSelection.EMPTY;
	private Map<RasterCoordinate, Place> placeMap = new HashMap<RasterCoordinate, Place>();
	private Map<RasterCoordinate, Place> shownMap = new HashMap<RasterCoordinate, Place>();
	private Place mapPosition;
	private int initialZoomLevel;
	private IMapComponent mapComponent;
	private IEclipsePreferences.IPreferenceChangeListener preferenceListener;
	private ComboContributionItem comboContributionItem;
	private Composite viewParent;
	private boolean clientClustering = false;
	private Action webAction;
	protected int lastZoom = 12;
	private IConfigurationElement currentConf;
	private Action refreshAction;

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
		if (memento != null) {
			IMapComponent.HistoryItem item = mapComponent.getLastHistoryItem();
			if (item != null) {
				memento.putFloat(LAST_LATITUDE, (float) item.getLatitude());
				memento.putFloat(LAST_LONGITUDE, (float) item.getLongitude());
				memento.putInteger(LAST_ZOOM, (int) item.getDetail());
			} else if (!Double.isNaN(lastLatitude) && !Double.isNaN(lastLongitude)) {
				memento.putFloat(LAST_LATITUDE, (float) lastLatitude);
				memento.putFloat(LAST_LONGITUDE, (float) lastLongitude);
				memento.putInteger(LAST_ZOOM, lastZoom);
			}
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
				// Refresh for Undo only
				assetsChanged(getNavigationHistory().getSelectedAssets(), changes == null);
			}

			@Override
			public void catalogClosed(int mode) {
				if (mode != CatalogListener.SHUTDOWN && mode != CatalogListener.EMERGENCY)
					refresh();
			}

			@Override
			public void catalogOpened(boolean newDb) {
				if (!parent.isDisposed())
					parent.getDisplay().asyncExec(() -> {
						if (!parent.isDisposed())
							refresh();
					});
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
				public void setCoordinates(String[] assetIds, double latitude, double longitude, int zoom, int type,
						String uuid) {
					updateGeoPosition(assetIds, latitude, longitude, type, uuid);
					if (Double.isNaN(latitude) && Double.isNaN(longitude)) {
						lastLatitude = latitude;
						lastLongitude = longitude;
						lastZoom = zoom;
					}
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

	protected void updateGeoPosition(String[] ids, double latitude, double longitude, int type, String uuid) {
		if (!lastSelection.isEmpty()) {
			GpsConfiguration gpsConfig = GpsActivator.getDefault().createGpsConfiguration();
			gpsConfig.overwrite = true;
			boolean modify = ids != null;
			if (ids == null || ids.length == 0) {
				ids = new String[lastSelection.size()];
				int i = 0;
				for (Asset asset : lastSelection)
					ids[i++] = asset.getStringId();
			}
			if (ids.length > 0) {
				IProfiledOperation op;
				switch (type) {
				case CoordinatesListener.IMGDIR:
					op = new GeodirOperation(new Trackpoint(latitude, longitude, false), ids[0]);
					break;
				case CoordinatesListener.SHOWNLOC:
					op = new GeoshownOperation(new Trackpoint(latitude, longitude, false), modify, ids, uuid,
							gpsConfig);
					break;
				default:
					op = new GeotagOperation(new Trackpoint[] { new Trackpoint(latitude, longitude, false) }, ids,
							gpsConfig);
				}
				OperationJob.executeOperation(op, this);
				isDirty = true;
			}
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
		IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
		undoContext = operationSupport.getUndoContext();
		UndoRedoActionGroup undoRedoGroup = new UndoRedoActionGroup(viewSite, undoContext, true);
		undoRedoGroup.fillActionBars(bars);
	}

	protected void fillLocalPullDown(IMenuManager menuManager) {
		menuManager.add(webAction);
		menuManager.add(refreshAction);
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		if (comboContributionItem != null) {
			manager.add(comboContributionItem);
			manager.add(new Separator());
		}
		manager.add(webAction);
		manager.add(refreshAction);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		webAction = new Action(Messages.MapView_show_in_web, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				try {
					IMapComponent.HistoryItem item = mapComponent.getLastHistoryItem();
					if (item == null)
						GeoService.showInWebbrowser(lastLatitude, lastLongitude, lastZoom);
					else
						GeoService.showInWebbrowser(item.getLatitude(), item.getLongitude(), (int) item.getDetail());
				} catch (Exception e) {
					// do nothing
				}
			}
		};
		webAction.setImageDescriptor(Icons.external.getDescriptor());
		webAction.setToolTipText(Messages.MapView_external_tooltip);
		refreshAction = new Action(Messages.MapView_refresh, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				refreshBusy();
			}
		};
		refreshAction.setImageDescriptor(Icons.refresh.getDescriptor());
		refreshAction.setToolTipText(Messages.MapView_refresh_tooltip);
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
			if (sublocation != null && !sublocation.isEmpty())
				initialZoomLevel = 14;
			else if (city != null && !city.isEmpty())
				initialZoomLevel = 12;
			else if (provinceOrState != null && !provinceOrState.isEmpty())
				initialZoomLevel = 7;
			else
				initialZoomLevel = 5;
		} else {
			markerPositions = new Place[] { new Place(latitude, longitude) };
			initialZoomLevel = 12;
		}
		showMap(markerPositions, null, IMapComponent.LOCATION);
	}

	public void showLocations(AssetSelection selectedAssets) {
		assetsChanged(selectedAssets, true);
		updateActions();
	}

	public void showMap() {
		int size = placeMap.size();
		Place[] markerPositions = placeMap.values().toArray(new Place[size]);
		int mode;
		switch (size) {
		case 0:
			if (!Double.isNaN(lastLatitude) && !Double.isNaN(lastLongitude)) {
				mapPosition = new Place(lastLatitude, lastLongitude);
				initialZoomLevel = 12;
			} else {
				mapPosition = new Place();
				String country = Locale.getDefault().getCountry();
				if (country.isEmpty()) {
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
			mode = lastSelection.size() > 1 ? IMapComponent.CLUSTER : IMapComponent.ONE;
			break;
		default:
			mapPosition = new Place();
			initialZoomLevel = computeZoomLevel(markerPositions, mapPosition);
			mode = IMapComponent.MULTI;
			break;
		}
		showMap(markerPositions, shownMap.values().toArray(new Place[size]), mode);
	}

	private void showMap(Place[] markerPositions, Place[] shownPositions, int mode) {
		if (mapComponent != null) {
			mapComponent.setInput(mapPosition, initialZoomLevel, markerPositions, shownPositions, null,
					dbIsReadonly() || isPeerOnly() ? IMapComponent.LOCATION : mode);
			if (!Double.isNaN(mapPosition.getLat()) && !Double.isNaN(mapPosition.getLon())) {
				lastLatitude = mapPosition.getLat();
				lastLongitude = mapPosition.getLon();
			}
		}
	}

	private boolean isPeerOnly() {
		IPeerService peerService = Core.getCore().getPeerService();
		AssetSelection selectedAssets = getNavigationHistory().getSelectedAssets();
		if (selectedAssets.isEmpty())
			return false;
		for (Asset asset : selectedAssets)
			if (peerService == null || !peerService.isOwnedByPeer(asset.getStringId()))
				return false;
		return true;
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
		assetsChanged(getNavigationHistory().getSelectedAssets(), true);
		return false;
	}

	protected void assetsChanged(AssetSelection selectedAssets, boolean refresh) {
		lastSelection = selectedAssets;
		extractLocations(selectedAssets);
		extractLocationsShown(selectedAssets);
		if (refresh) {
			Shell shell = getSite().getShell();
			if (!shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed())
						refreshBusy();
				});
		}
	}

	private void extractLocations(AssetSelection selectedAssets) {
		placeMap.clear();
		boolean mixedDir = false;
		int n = 0;
		for (Asset asset : selectedAssets.getAssets()) {
			double lat = asset.getGPSLatitude();
			double lon = asset.getGPSLongitude();
			if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
				RasterCoordinate coord = new RasterCoordinate(lat, lon, 2);
				Place place = (Place) (clientClustering ? coord.findExactMatch(placeMap)
						: coord.findClosestMatch(placeMap, 0.06d, 'K'));
				double direction = asset.getGPSImgDirection();
				if (place == null) {
					place = new Place(lat, lon);
					place.setImageName(asset.getName());
					place.setDirection(direction);
					placeMap.put(coord, place);
				} else {
					String description = place.getImageName();
					if (description.length() > 40) {
						if (!description.endsWith(",...")) //$NON-NLS-1$
							description += ",..."; //$NON-NLS-1$
					} else
						description += ", " + asset.getName(); //$NON-NLS-1$
					place.setImageName(description);
					if (!mixedDir) {
						if (Double.isNaN(place.getDirection()))
							place.setDirection(direction);
						else if (!Double.isNaN(direction)) {
							if (Math.abs(place.getDirection() - direction) < 1d)
								place.setDirection((place.getDirection() * n + direction) / ++n);
							else {
								place.setDirection(Double.NaN);
								mixedDir = true;
							}
						}
					}
				}
				place.addImageAssetId(asset.getStringId());
			}
		}
	}

	private void extractLocationsShown(AssetSelection selectedAssets) {
		IDbManager dbManager = Core.getCore().getDbManager();
		shownMap.clear();
		for (Asset asset : selectedAssets.getAssets()) {
			for (LocationShownImpl locationShown : dbManager.obtainStructForAsset(LocationShownImpl.class,
					asset.getStringId(), false)) {
				LocationImpl loc = dbManager.obtainById(LocationImpl.class, locationShown.getLocation());
				if (loc != null) {
					double lon = loc.getLongitude();
					double lat = loc.getLatitude();
					if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
						RasterCoordinate coord = new RasterCoordinate(lat, lon, 2);
						Place place = (Place) (clientClustering ? coord.findExactMatch(shownMap)
								: coord.findClosestMatch(shownMap, 0.06d, 'K'));
						if (place == null) {
							place = new Place(lat, lon);
							place.setImageName(asset.getName());
							shownMap.put(coord, place);
						} else {
							String description = place.getImageName();
							if (description.length() > 40) {
								if (!description.endsWith(",...")) //$NON-NLS-1$
									description += ",..."; //$NON-NLS-1$
							} else
								description += ", " + asset.getName(); //$NON-NLS-1$
							place.setImageName(description);
						}
						place.addImageAssetId(locationShown.getStringId());
					}
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
