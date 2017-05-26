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
package com.bdaum.zoom.operations.internal.job;

import java.io.File;

import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.asset.Asset;

public class SourceAndTarget {

	private final Asset asset;
	private final File outfile;
	private final Rectangle targetBounds;

	public SourceAndTarget(Asset asset, File outfile, Rectangle targetBounds) {
		this.asset = asset;
		this.outfile = outfile;
		this.targetBounds = targetBounds;
	}

	public Asset getAsset() {
		return asset;
	}

	public File getOutfile() {
		return outfile;
	}

	public Rectangle getTargetBounds() {
		return targetBounds;
	}

}