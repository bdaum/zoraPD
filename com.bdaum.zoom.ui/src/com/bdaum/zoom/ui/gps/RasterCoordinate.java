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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.gps;

import java.util.Map;

import com.bdaum.zoom.core.Core;

/**
 * A raster coordinate identifies a geographical location. This implementation
 * features a matching algorithm that can match other locations close by.
 * 
 */
public class RasterCoordinate {

	private final static int[] xdiff = new int[] { 0, -1, 1, 0, -1, 1, 0, -1, 1 };
	private final static int[] ydiff = new int[] { 0, 0, 0, -1, -1, -1, 1, 1, 1 };

	private int x;
	private int y;
	public double lon;
	public double lat;

	/**
	 * Constructor
	 * 
	 * @param lat
	 *            - latitude
	 * @param lon
	 *            - longitude
	 * @param seconds
	 *            - matching resolution in geographical seconds
	 */
	public RasterCoordinate(double lat, double lon, int seconds) {
		this.lat = lat;
		this.lon = lon;
		double f = 3600d / seconds;
		this.x = (int) (lat * f + 0.5d); // arc seconds resolution
		this.y = (int) (lon * f + 0.5d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof RasterCoordinate) ? ((RasterCoordinate) obj).x == x && ((RasterCoordinate) obj).y == y
				: super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * x + y;
	}

	/**
	 * Finds a matching waypoint in a given place map with a give tolerance
	 * 
	 * @param placeMap
	 *            - mapping raster coordinates to waypoints
	 * @param tolerance
	 *            - tolerance in kilometers
	 * @param unit
	 *            -the unit for tolerance: 'M' is statute miles, 'K' is
	 *            kilometers, 'N' is nautical miles
	 * @return - closest waypoint within tolerance radius or null
	 */
	public Waypoint findClosestMatch(Map<RasterCoordinate, ? extends Waypoint> placeMap, double tolerance,
			char unit) {
		if (placeMap == null)
			return null;
		int oldX = x;
		int oldY = y;
		Waypoint place = null;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < xdiff.length; i++) {
			x = oldX + xdiff[i];
			y = oldY + ydiff[i];
			Waypoint cand = placeMap.get(this);
			if (cand != null && !Double.isNaN(cand.getLat()) && !Double.isNaN(cand.getLon())) {
				double distance = Core.distance(lat, lon, cand.getLat(), cand.getLon(), unit);
				if (distance < minDist) {
					minDist = distance;
					place = cand;
				}
			}
		}
		x = oldX;
		y = oldY;
		return minDist < tolerance ? place : null;
	}

	/**
	 * Finds an exactly matching waypoint in a given place map
	 * 
	 * @param placeMap
	 *            - mapping raster coordinates to waypoints
	 * @return matching waypoint or null
	 */
	public Waypoint findExactMatch(Map<RasterCoordinate, ? extends Waypoint> placeMap) {
		return placeMap.get(this);
	}

}
