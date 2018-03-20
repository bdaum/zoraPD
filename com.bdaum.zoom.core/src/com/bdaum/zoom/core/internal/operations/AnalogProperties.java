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
package com.bdaum.zoom.core.internal.operations;

import java.util.Date;
import java.util.List;

public class AnalogProperties {
	public String emulsion;
	public String processingNotes;
	public int format;
	public int type;
	public int scalarSpeedRatings;
	public String make;
	public String model;
	public String serial;
	public String lens;
	public String lensSerial;
	public double focalLength;
	public int focalLengthIn35MmFilm;
	public double focalLengthFactor;
	public Date creationDate;
	public int lightSource;
	public double lv;
	public double exposureTime;
	public double fNumber;
	public String artist;
	public String copyright;
	public String event;
	public List<String> keywords;
	public String providerId;
	public String modelId;
	public int maxRating;
	public boolean overwriteRating;
	public int safety;
}
