/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.batch.internal;

import java.awt.color.ICC_Profile;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.bdaum.zoom.image.IExifLoader;
import com.bdaum.zoom.image.IImageLoader;
import com.bdaum.zoom.image.IImportFilterFactory;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;

@SuppressWarnings("restriction")
public class ExifToolSubstitute implements IExifLoader {

	private String extension;
	private IImportFilterFactory importFilter;
	private IImageLoader imageLoader;
	private IExifLoader exifLoader;

	public ExifToolSubstitute(File file) {
		String name = file.getName();
		int p = name.lastIndexOf('.');
		extension = (p >= 0) ? name.substring(p + 1).toLowerCase() : ""; //$NON-NLS-1$
		importFilter = ImageActivator.getDefault().getImportFilters()
				.get(extension);
		if (importFilter != null) {
			imageLoader = importFilter.getImageLoader(file);
			exifLoader = importFilter.getExifLoader(file);
		}
	}

	public ICC_Profile getICCProfile() {
		return exifLoader == null ? null : exifLoader.getICCProfile();
	}

	/**
	 * Must be executed after an image loading method
	 */

	public Map<String, String> getMetadata() {
		return exifLoader == null ? null : exifLoader.getMetadata();
	}

	public Set<String> getMakerNotes() {
		return exifLoader == null ? null : exifLoader.getMakerNotes();
	}

	public ZImage getPreviewImage(boolean check) {
		return exifLoader == null ? null : exifLoader.getPreviewImage(check);
	}

	/**
	 *
	 * @param width
	 *            - width of bounding box
	 * @param height
	 *            - height of bounding box
	 * @param raster
	 *            - raster size (because of JPEG rotation
	 * @param exposure
	 *            - exposure value
	 * @return image
	 * @throws IOException
	 */
	public ZImage loadThumbnail(int width, int height, int raster,
			float exposure) throws IOException {
		return imageLoader == null ? null : imageLoader.loadImage(width,
				height, raster, exposure, Double.MAX_VALUE);
	}

	public double get35mm() {
		return exifLoader == null ? Double.NaN : exifLoader.get35mm();
	}

	public byte[] getBinaryData(String tag, boolean check) {
		return null;
	}

}
