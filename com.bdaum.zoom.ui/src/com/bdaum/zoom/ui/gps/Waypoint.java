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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.gps;

/**
 * This class describes a single GPS waypoint
 *
 */

public class Waypoint {

	// <wpt lat="49.835830" lon="9.159422">
	// <ele>-0.11</ele>
	// <name>Autohaus Bieger Gmbh</name>
	// <desc>Am Stachus 1 63820 Elsenfeld, Milten</desc>
	// <sym>Waypoint</sym>
	// <extensions>
	// <gpxx:WaypointExtension>
	// <gpxx:Categories>
	// <gpxx:Category>Autoservices</gpxx:Category>
	// </gpxx:Categories>
	// <gpxx:Address>
	// <gpxx:StreetAddress>Am Stachus 1</gpxx:StreetAddress>
	// <gpxx:City>Elsenfeld</gpxx:City>
	// <gpxx:State>Miltenberg</gpxx:State>
	// <gpxx:PostalCode>63820</gpxx:PostalCode>
	// </gpxx:Address>
	// <gpxx:PhoneNumber>+49 6022 26360</gpxx:PhoneNumber>
	// </gpxx:WaypointExtension>
	// </extensions>
	// </wpt>

	protected double lat = Double.NaN;
	protected double lon = Double.NaN;
	protected String name;
	protected String description;
	protected double elevation = Double.NaN;
	private double direction = Double.NaN;

	/**
	 * Constructor
	 * 
	 * @param longitude
	 * @param latitude
	 */
	public Waypoint(double longitude, double latitude) {
		this.lon = longitude;
		this.lat = latitude;
	}
	
	/**
	 * Default contructor
	 */
	public Waypoint() {
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the elevation
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * @param elevation
	 *            the elevation to set
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * @param lat the latitude to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * @return the latitude
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lon the longitude to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	/**
	 * @return the longitude
	 */
	public double getLon() {
		return lon;
	}
	
	
	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}


}
