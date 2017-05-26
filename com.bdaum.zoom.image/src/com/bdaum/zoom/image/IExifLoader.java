/*******************************************************************************
 * Copyright (c) 2009-2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image;

import java.awt.color.ICC_Profile;
import java.util.Map;
import java.util.Set;

/**
 * This interface describes loaders that are able to load EXIF data from
 * specific image formats
 *
 */
public interface IExifLoader extends IFocalLengthProvider {

	/**
	 * @return map of metadata contained in image file
	 */
	Map<String, String> getMetadata();

	/**
	 * @return ICC profile contained in the image file
	 */
	ICC_Profile getICCProfile();

	/**
	 * Retrieves preview image from Exif data
	 * @param check
	 *            - true if loader should first check if there is a preview
	 *            entry in the metadata (used by ExifTool for performance
	 *            optimization, other implementors may ignore)
	 * @return SWT image data or null
	 */
	ZImage getPreviewImage(boolean check);

	/**
	 * Retrieves binary data from the EXIF data
	 * @param tag - tag name of binary data
	 * @param check  - true if loader should first check if there is a preview
	 *            entry in the metadata (used by ExifTool for performance
	 *            optimization, other implementors may ignore)
	 * @return binary data
	 */
	byte[] getBinaryData(String tag, boolean check);

	/**
	 * @return set with all makenote tags
	 */
	Set<String> getMakerNotes();

}
