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
package com.bdaum.zoom.exr.internal;

import java.awt.color.ICC_Profile;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bdaum.zoom.image.IExifLoader;
import com.bdaum.zoom.image.IImageLoader;
import com.bdaum.zoom.image.ZImage;

public class ExrExifLoader implements IExifLoader {

	private IImageLoader imageLoader;

	public ExrExifLoader(File file, IImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}


	public Map<String, String> getMetadata() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("ImageSize", imageLoader.getImageWidth() + "x" //$NON-NLS-1$ //$NON-NLS-2$
				+ imageLoader.getImageHeight());
		String comments = imageLoader.getComments();
		if (comments != null && comments.length() > 0)
			settings.put("Software", comments); //$NON-NLS-1$
		settings.put("BitsPerSample", "-1 -1 -1"); //$NON-NLS-1$ //$NON-NLS-2$
		settings.put("SamplesPerPixel", "3"); //$NON-NLS-1$ //$NON-NLS-2$
		settings.put("PhotometricInterpretation", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		settings.put("FileType", "OpenEXR"); //$NON-NLS-1$ //$NON-NLS-2$
		return settings;
	}


	public ICC_Profile getICCProfile() {
		return null;
	}


	public ZImage getPreviewImage(boolean check) {
		return null;
	}


	public Set<String> getMakerNotes() {
		return null;
	}


	public double get35mm() {
		return Double.NaN;
	}


	public byte[] getBinaryData(String tag, boolean check) {
		return null;
	}

}
