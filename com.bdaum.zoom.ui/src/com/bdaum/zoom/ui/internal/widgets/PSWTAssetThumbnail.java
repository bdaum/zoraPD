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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.graphics.Image;
import org.piccolo2d.extras.swt.PSWTCanvas;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.image.ImageStore;

public class PSWTAssetThumbnail extends ZPSWTImage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5539327360971870974L;
	private final ImageStore imageSource;
	private final Asset asset;

	public PSWTAssetThumbnail(PSWTCanvas canvas, ImageStore imageSource,
			Asset asset) {
		super(canvas, imageSource.getImage(asset));
		this.imageSource = imageSource;
		this.asset = asset;
	}

	
	@Override
	public Image getImage() {
		Image image = super.getImage();
		if (image.isDisposed()) {
			image = imageSource.getImage(getAsset());
			setImage(image);
		}
		return image;
	}

	public Asset getAsset() {
		return asset;
	}
}
