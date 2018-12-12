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


package com.bdaum.zoom.gps.internal;

import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;

public class GpsConfiguration {

	public int timeshift;
	public int tolerance;
	public boolean overwrite;
	public boolean includeCoordinates;
	public boolean includeNames;
	public boolean updateAltitude;
	public FilterChain keywordFilter;
	public final boolean useWaypoints;
	public final boolean edit;
	public long interval;
	public boolean excludeNoGoAreas;

	public GpsConfiguration(int timeshift, int tolerance,boolean edit, boolean overwrite, boolean useWaypoints,
			boolean includeCoordinates, boolean includeNames, FilterChain keywordFilter, boolean updateAltitude, long interval) {
		this.timeshift = timeshift;
		this.tolerance = tolerance;
		this.edit = edit;
		this.overwrite = overwrite;
		this.useWaypoints = useWaypoints;
		this.includeCoordinates = includeCoordinates;
		this.includeNames = includeNames;
		this.updateAltitude = updateAltitude;
		this.keywordFilter = keywordFilter;
		this.interval = interval;
	}

}
