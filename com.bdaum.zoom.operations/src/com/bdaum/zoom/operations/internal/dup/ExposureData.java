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
package com.bdaum.zoom.operations.internal.dup;

import java.util.Arrays;
import java.util.Date;

public class ExposureData {
	Date creationTime;

	double exposureTime;
	String model;
	String make;
	double fnumber;
	boolean flashFired;
	int[] isoSpeedRatings;

	private final double focalLength;

	public ExposureData(Date creationTime, double exposureTime,
			String model, String make, double fnumber, boolean flashFired,
			double focalLength, int[] isoSpeedRatings) {
		super();
		this.creationTime = creationTime;
		this.exposureTime = exposureTime;
		this.model = model;
		this.make = make;
		this.fnumber = fnumber;
		this.flashFired = flashFired;
		this.focalLength = focalLength;
		this.isoSpeedRatings = isoSpeedRatings;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExposureData) {
			ExposureData sibling = (ExposureData) obj;
			if (creationTime == null)
				return sibling.creationTime == null;
			if (!creationTime.equals(sibling.creationTime))
				return false;
			if (exposureTime != sibling.exposureTime)
				return false;
			if (model == null)
				return sibling.model == null;
			if (!model.equals(sibling.model))
				return false;
			if (make == null)
				return sibling.make == null;
			if (!make.equals(sibling.make))
				return false;
			if (fnumber != sibling.fnumber)
				return false;
			if (flashFired != sibling.flashFired)
				return false;
			if (focalLength != sibling.focalLength)
				return false;
			if (isoSpeedRatings == null)
				return sibling.isoSpeedRatings == null;
			if (!Arrays.equals(isoSpeedRatings, sibling.isoSpeedRatings))
				return false;
			return true;
		}
		return false;
	}

	
	@Override
	public int hashCode() {
		int code = 0;
		if (creationTime != null)
			code += creationTime.hashCode();
		code *= 37;
		if (!Double.isNaN(exposureTime))
			code += 1000 * exposureTime;
		code *= 37;
		if (model != null)
			code += model.hashCode();
		code *= 37;
		if (make != null)
			code += make.hashCode();
		code *= 37;
		if (!Double.isNaN(fnumber))
			code += 10 * fnumber;
		code *= 37;
		if (flashFired)
			code += 1;
		code *= 37;
		if (!Double.isNaN(focalLength))
			code += 10 * focalLength;
		if (isoSpeedRatings != null)
			code += Arrays.hashCode(isoSpeedRatings);
		return code;
	}
}