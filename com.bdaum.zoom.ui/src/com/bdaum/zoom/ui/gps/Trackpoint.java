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

package com.bdaum.zoom.ui.gps;

import java.util.Date;

/**
 * This class describes a single GPS trackpoint
 *
 */
public class Trackpoint implements Comparable<Object> {

	private long time = -1;
	private double longitude = Double.NaN;
	private double latitude = Double.NaN;
	private double altitude = Double.NaN;
	private double speed = Double.NaN;
	private long minTime = -1;
	private long maxTime = -1;
	private boolean segmentEnd;

	/**
	 * Constructor
	 * @param latitude - Latitude
	 * @param longitude - Longitude
	 */
	public Trackpoint(double latitude, double longitude, boolean segmentEnd) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.segmentEnd = segmentEnd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		long t = -1;
		if (obj instanceof Date)
			t = ((Date) obj).getTime();
		if (obj instanceof Trackpoint)
			t = ((Trackpoint) obj).getTime();
		return (time > t) ? 1 : (time < t) ? -1 : 0;
	}

	/**
	 * @return Longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return Latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return Altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @return Speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Set latitude
	 * @param latitude
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Set longitude
	 * @param longitude
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Set altitude
	 * @param altitude
	 */
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	/**
	 * Set speed
	 * @param speed
	 */
	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	/**
	 * Set time
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return minTime
	 */
	public long getMinTime() {
		return minTime;
	}

	/**
	 * @param minTime das zu setzende Objekt minTime
	 */
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

	/**
	 * @return maxTime
	 */
	public long getMaxTime() {
		return maxTime;
	}

	/**
	 * @param maxTime das zu setzende Objekt maxTime
	 */
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	/**
	 * @return segmentEnd
	 */
	public boolean isSegmentEnd() {
		return segmentEnd;
	}

	/**
	 * @param segmentEnd das zu setzende Objekt segmentEnd
	 */
	public void setSegmentEnd(boolean segmentEnd) {
		this.segmentEnd = segmentEnd;
	}


}
