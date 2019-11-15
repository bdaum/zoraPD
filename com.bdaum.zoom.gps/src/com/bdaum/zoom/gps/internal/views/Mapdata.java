/*******************************************************************************
 * Copyright (c) 2019 Berthold Daum.
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.gps.RasterCoordinate;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.gps.WaypointArea;

public class Mapdata {

	private static final String[] EMPTY = new String[0];
	private AssetSelection assetSelection;
	private Map<RasterCoordinate, Place> placeMap = new HashMap<RasterCoordinate, Place>();
	private Map<RasterCoordinate, Place> shownMap = new HashMap<RasterCoordinate, Place>();
	private boolean clientClustering;
	private Trackpoint[] trackpoints;
	private Place mapPosition;

	public Mapdata(AssetSelection assetSelection, Trackpoint[] trackpoints, boolean clientClustering) {
		this.assetSelection = assetSelection;
		this.trackpoints = trackpoints;
		this.clientClustering = clientClustering;
		if (assetSelection != null) {
			extractLocations();
			extractLocationsShown();
		}
	}

	public Mapdata(Place mapPosition, Place[] markers, Trackpoint[] trackpoints) {
		this.mapPosition = mapPosition;
		this.trackpoints = trackpoints;
		if (markers != null)
			for (Place place : markers)
				if (!Double.isNaN(place.getLat()) && !Double.isNaN(place.getLon()))
					placeMap.put(new RasterCoordinate(place.getLat(), place.getLon(), 2), place);
	}

	public Place[] getCameraPositions() {
		Collection<Place> values = placeMap.values();
		return values.toArray(new Place[values.size()]);
	}

	public Place getInitialPosition() {
		if (mapPosition != null)
			return mapPosition;
		if (placeMap.isEmpty())
			return null;
		Collection<Place> values = placeMap.values();
		return values.toArray(new Place[values.size()])[0];
	}

	public Place[] getSubjectPositions() {
		Collection<Place> values = shownMap.values();
		return values.toArray(new Place[values.size()]);
	}

	private void extractLocations() {
		boolean mixedDir = false;
		int n = 0;
		for (Asset asset : assetSelection.getAssets()) {
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

	private void extractLocationsShown() {
		IDbManager dbManager = Core.getCore().getDbManager();
		for (Asset asset : assetSelection.getAssets())
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

	public boolean isEmpty() {
		return placeMap.isEmpty();
	}

	public String[] getAssetIds() {
		if (assetSelection == null)
			return EMPTY;
		String[] ids = new String[assetSelection.size()];
		int i = 0;
		for (Asset asset : assetSelection)
			ids[i++] = asset.getStringId();
		return ids;
	}

	public void appendCameraPositions(StringBuilder cb) {
		int j = 0;
		for (Place place : getCameraPositions())
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				cb.append(latLon(place.getLat(), place.getLon()));
			}

	}

	public void appendCameraTitles(StringBuilder cb) {
		appendTitles(cb, getCameraPositions());
	}

	private void appendTitles(StringBuilder cb, Place[] places) {
		int j = 0;
		for (Place place : places)
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				cb.append('"').append(place.getImageName()).append('"');
			}
	}

	public void appendCameraImages(StringBuilder cb) {
		int j = 0;
		for (Place place : getCameraPositions()) {
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				List<String> imageAssetIds = place.getImageAssetIds();
				cb.append('[');
				Iterator<String> iterator = imageAssetIds.iterator();
				while (iterator.hasNext()) {
					cb.append('"').append(iterator.next()).append('"');
					if (iterator.hasNext())
						cb.append(", "); //$NON-NLS-1$
				}
				cb.append(']');
			}
		}
	}

	public void appendCameraDirection(StringBuilder cb) {
		int j = 0;
		for (Place place : getCameraPositions())
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				if (!Double.isNaN(place.getDirection()))
					cb.append(place.getDirection());
				else
					cb.append("NaN"); //$NON-NLS-1$
			}
	}

	public void appendTrackpoints(StringBuilder cb) {
		int j = 0;
		if (trackpoints != null)
			for (Trackpoint pnt : trackpoints)
				if (pnt != null) {
					if (j++ > 0)
						cb.append(",\n"); //$NON-NLS-1$
					cb.append(latLon(pnt.getLatitude(), pnt.getLongitude()));
				}
	}

	public void appendSubjectLocation(StringBuilder cb) {
		int j = 0;
		for (Place place : getSubjectPositions())
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				cb.append(latLon(place.getLat(), place.getLon()));
			}
	}

	public void appendSubjectTitles(StringBuilder cb) {
		appendTitles(cb, getSubjectPositions());
	}

	public void appendSubjectImages(StringBuilder cb) {
		int j = 0;
		for (Place place : getSubjectPositions())
			if (place != null) {
				if (j++ > 0)
					cb.append(",\n"); //$NON-NLS-1$
				List<String> imageAssetIds = place.getImageAssetIds();
				cb.append("\"shown="); //$NON-NLS-1$
				Iterator<String> iterator = imageAssetIds.iterator();
				while (iterator.hasNext()) {
					cb.append(iterator.next());
					if (iterator.hasNext())
						cb.append(',');
				}
				cb.append('"');
			}
	}

	public void appendMapPosition(StringBuilder cb) {
		Place mapPosition = getInitialPosition();
		if (mapPosition != null) {
			if (Double.isNaN(mapPosition.getLat()) || Double.isNaN(mapPosition.getLon())) {
				StringBuilder buf = new StringBuilder();
				Core.condAppend(mapPosition.getStreet(), buf, ", "); //$NON-NLS-1$
				Core.condAppend(mapPosition.getName(), buf, ", "); //$NON-NLS-1$
				Core.condAppend(mapPosition.getState(), buf, ", "); //$NON-NLS-1$
				Core.condAppend(mapPosition.getCountryName(), buf, ", "); //$NON-NLS-1$
				Core.condAppend(getCountryCode(mapPosition.getCountryCode()), buf, ", "); //$NON-NLS-1$
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
						.append(latLon(mapPosition.getLat(), mapPosition.getLon())); // $NON-NLS-1$
				cb.append(";\n"); //$NON-NLS-1$
			}
		}
	}

	public int getCamCount() {
		return placeMap.size();
	}

	public static String latLon(double lat, double lon) {
		return new StringBuilder().append("{lat: ").append(lat).append(", lon:").append(lon).append('}').toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns a country code understood by the mapping system Default
	 * implementation: 2-byte ISO country code
	 *
	 * @param input
	 *            - 2- or 3-byte country code or null
	 * @return - Country code understood by the mapping system
	 */
	private static String getCountryCode(String input) {
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

	public void setMapPosition(Place mapPosition) {
		this.mapPosition = mapPosition;
	}

	public Object computesAllImages() {
		StringBuilder sb = new StringBuilder();
		sb.append("[["); //$NON-NLS-1$
		if (assetSelection != null)
			for (Asset asset : assetSelection) {
				if (sb.length() > 2)
					sb.append(", "); //$NON-NLS-1$
				sb.append('"').append(asset.getStringId()).append('"');
			}
		return sb.append("]]").toString(); //$NON-NLS-1$
	}

	public Object computeAllTitels() {
		StringBuilder sb = new StringBuilder();
		sb.append("["); //$NON-NLS-1$
		if (assetSelection != null)
			for (Asset asset : assetSelection) {
				if (sb.length() > 2)
					sb.append(", "); //$NON-NLS-1$
				sb.append('"').append(asset.getName()).append('"');
			}
		return sb.append("]").toString(); //$NON-NLS-1$
	}

	public int getAssetCount() {
		return assetSelection == null ? 0 : assetSelection.size();
	}

}
