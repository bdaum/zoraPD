/*******************************************************************************
 * Copyright (c) 2009-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal;

public class GeoArea {
	private String name;
	private double latitude, longitude, km;

	/**
	 * @param name - name of area
	 * @param latitude - center latitude
	 * @param longitude - center longitude
	 * @param km - radius in km
	 */
	public GeoArea(String name, double latitude, double longitude, double km) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.km = km;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setKm(double km) {
		this.km = km;
	}

	public String getName() {
		return name;
	}

	public double getKm() {
		return km;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return name;
	}

}