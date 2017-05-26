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

package com.bdaum.zoom.core.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageProvider;
import com.bdaum.zoom.image.ImageUtilities;

public class AssetImageProvider implements ImageProvider {

	private int cms = ImageConstants.NOCMS;


	public Image loadThumbnail(Device device, Object imageSource) {
		return ImageUtilities.loadThumbnail(device,
				(imageSource instanceof Asset) ? ((Asset) imageSource)
						.getJpegThumbnail() : null, cms, SWT.IMAGE_JPEG, true);
	}


	public Object obtainImageSource(String id) {
		return Core.getCore().getDbManager().obtainAsset(id);
	}


	public String obtainSourceId(Object imageSource) {
		return imageSource.toString();
	}


	public int getCMS() {
		return cms;
	}


	public void setCMS(int cms) {
		this.cms = cms;
	}


}
