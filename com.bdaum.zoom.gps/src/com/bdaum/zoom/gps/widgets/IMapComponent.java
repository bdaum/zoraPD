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

package com.bdaum.zoom.gps.widgets;

import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.gps.CoordinatesListener;
import com.bdaum.zoom.gps.MaptypeChangedListener;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.ui.gps.Trackpoint;

public interface IMapComponent {
	
	/**
	 * Instances of this class remember a step in the map navigation history
	 */
	static class HistoryItem {
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
	 * Creates the map component
	 *
	 * @param parent
	 *            - parent container
	 * @param header
	 *            - true if a header with pin, explanation and search field is required
	 */
	void createComponent(Composite parent, boolean header);

	/**
	 * Display a location
	 */
	int LOCATION = -1;
	/**
	 * Define a new location
	 */
	int ADDLOCATION = -2;
	/**
	 * Display a track
	 */
	int TRACK = -3;
	/**
	 * Display a track
	 */
	int BLANK = -4;
	/**
	 * No image with geo tags
	 */
	int AREA = -5;
	/**
	 * No image with geo tags
	 */
	int NONE = 0;
	/**
	 * One image with geo tags
	 */
	int ONE = 1;
	/**
	 * Multiple images at same position
	 */
	int CLUSTER = 2;
	/**
	 * Multiple images with geo tags
	 */
	int MULTI = 3;

	/**
	 * Sets input position and markers
	 *
	 * @param mapPosition
	 *            - map position
	 * @param initialZoomLevel
	 *            - zoom level 1-14
	 * @param markerPositions
	 *            - marker positions locations created
	 * @param shownPositions
	 *            - marker positions locations shown
	 * @param trackpoints
	 * 			  - trackpoints to display
	 * @param mode
	 *            - map mode
	 */
	void setInput(Place mapPosition, int initialZoomLevel,
			Place[] markerPositions, Place[] shownPositions, Trackpoint[] trackpoints, int mode);

	/**
	 * Add a progress listener
	 *
	 * @param listener
	 *            - progress listener
	 */
	void addProgressListener(ProgressListener listener);

	/**
	 * Add a status listener
	 *
	 * @param listener
	 *            - progress listener
	 */
	void addStatusTextListener(StatusTextListener listener);

	/**
	 * Remove a progress listener
	 *
	 * @param listener
	 *            - progress listener
	 */
	void removeProgressListener(ProgressListener listener);

	/**
	 * Remove a status listener
	 *
	 * @param listener
	 *            - progress listener
	 */
	void removeStatusTextListener(StatusTextListener listener);

	/**
	 * Set focus to composite control
	 *
	 * @return - true in case of success
	 */
	boolean setFocus();

	/**
	 * Return composite control
	 *
	 * @return composite control
	 */
	Control getControl();

	/**
	 * Dispose the this component with all its resources
	 */
	void dispose();

	/**
	 * @param listener
	 *            - listener for coordinate changes
	 */
	void addCoordinatesListener(CoordinatesListener listener);

	/**
	 * @param listener
	 *            - listener for coordinate changes
	 */
	void removeCoordinatesListener(CoordinatesListener listener);

	/**
	 * @param listener
	 *            - listener for maptype changes
	 */
	void addMaptypeListener(MaptypeChangedListener listener);

	/**
	 * @param listener
	 *            - listener for maptype changes
	 */
	void removeMaptypeListener(MaptypeChangedListener listener);

	/**
	 * Sets the initial map type
	 *
	 * @param maptype
	 *            - map type name
	 */
	void setInitialMapType(String maptype);

	/**
	 * Refreshes the map browser
	 */
	void refresh();

	/**
	 * @return the most recent history item
	 */
	HistoryItem getLastHistoryItem();

	/**
	 * Shows a highlighted area around the specified coordinates with the specified diameter
	 * @param latitude
	 * @param longitude
	 * @param km
	 */
	void setArea(double latitude, double longitude, double km);


}