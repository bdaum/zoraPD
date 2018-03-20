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
 * (c) 2011 Berthold Daum  
 */
package com.bdaum.zoom.ui.gps;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

public interface IWaypointCollector {
	/**
	 * @param in - collects waypoints from the input source
	 * @param waypoints - map containing waypoints for given coordinates
	 * @throws IOException
	 * @throws ParseException
	 */
	void collect(InputStream in, Map<RasterCoordinate, Waypoint> waypoints) throws IOException, ParseException;

}
