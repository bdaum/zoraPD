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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.peer;

import java.io.File;
import java.io.Serializable;

import com.bdaum.zoom.cat.model.asset.Asset;

public class AssetSearchResult implements Serializable {

	private static final long serialVersionUID = -3224996660213395529L;
	private Asset asset;
	private File catalog;
	private String location;

	public AssetSearchResult(Asset asset, File catalog, String location) {
		this.asset = asset;
		this.catalog = catalog;
		this.location = location;
	}

	/**
	 * @return asset
	 */
	public Asset getAsset() {
		return asset;
	}

	/**
	 * @param asset das zu setzende Objekt asset
	 */
	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	/**
	 * @return catalog
	 */
	public File getCatalog() {
		return catalog;
	}

	/**
	 * @param catalog das zu setzende Objekt catalog
	 */
	public void setCatalog(File catalog) {
		this.catalog = catalog;
	}

	/**
	 * @return location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location das zu setzende Objekt location
	 */
	public void setLocation(String location) {
		this.location = location;
	}
}
