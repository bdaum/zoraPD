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

package com.bdaum.zoom.gps.geonames;

import java.util.ArrayList;
import java.util.List;

import com.bdaum.zoom.ui.gps.WaypointExtension;

/**
 * Descriptor for a geographical place
 */
public class Place extends WaypointExtension {

	private String id;
	private double distance = Double.NaN;
	private String countryName;
	private String countryCode;
	private String continent;
	private String imageName;
	private List<String> imageAssetIds = new ArrayList<>(3);

	/**
	 * Constructor
	 * 
	 * @param lat
	 *            - Latitude
	 * @param lon
	 *            - Longitude
	 */
	public Place(double lat, double lon) {
		super(lat, lon);
	}

	/**
	 * Default constructor
	 */
	public Place() {
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param distance
	 *            - the distance from the named place
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @return the distance from the named place
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param countryName
	 *            the countryName to set
	 */
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	/**
	 * @return the countryName
	 */
	public String getCountryName() {
		return countryName;
	}

	/**
	 * @param countryCode
	 *            the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @param continent
	 *            the continent to set
	 */
	public void setContinent(String continent) {
		this.continent = continent;
	}

	/**
	 * @return the continent
	 */
	public String getContinent() {
		return continent;
	}

	/**
	 * Returns the name of the associated image
	 * 
	 * @return title
	 */
	public String getImageName() {
		return imageName == null ? "" : imageName; //$NON-NLS-1$
	}

	/**
	 * Sets the name of the associated image
	 * 
	 * @param name
	 */
	public void setImageName(String name) {
		this.imageName = name;
	}

	/**
	 * Returns the asset ID of the associated image
	 * 
	 * @return asset ID of the associated image
	 */
	public List<String> getImageAssetIds() {
		return imageAssetIds;
	}

	/**
	 * Sets the asset ID of the associated image
	 * 
	 * @param id
	 *            - asset ID of the associated image
	 */
	public void addImageAssetId(String id) {
		imageAssetIds.add(id);
	}

}
