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
package com.bdaum.zoom.image;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * This interface describes entities that are able to deliver images such as thumbnails
 *
 */
public interface ImageProvider {

	/**
	 * Obtain a unique ID for a given image source
	 * @param imageSource - an image source
	 * @return ID of image source
	 */
	String obtainSourceId(Object imageSource);

	/**
	 * Obtain an image source object for a given ID
	 * @param id - ID of image source
	 * @return image source identified by the given ID
	 */
	Object obtainImageSource(String id);

	/**
	 * Loads a thumbnail image from the given image source
	 * @param device - the device for which the image is created
	 * @param imageSource - an image source
	 * @return - thumbnail image
	 */
	Image loadThumbnail(Device device, Object imageSource);

	/**
	 * Sets the desired output color space
	 * @param cms - output color space
	 */
	public void setCMS(int cms);

	/**
	 * Returns the current output color space
	 * @return color space
	 */
	public int getCMS();

}
