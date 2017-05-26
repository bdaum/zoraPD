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

import java.io.IOException;

/**
 * This interface describes image loaders for specific file formats
 *
 */
public interface IImageLoader {

	/**
	 * @param width - width restriction or -1
	 * @param height - height restriction or -1
	 * @param raster - pixel raster size (for lossles JPEG rotation) or 0
	 * @param exposure - exposure factor for high dynamic range
	 * @param maxFactor -  scaling limit
	 * @return ZImage instance
	 * @throws IOException
	 */
	ZImage loadImage(int width, int height, int raster,
			float exposure, double maxFactor) throws IOException;

	/**
	 * Must be called after loadImage
	 * @return comments
	 */
	String getComments();
	
	/**
	 * Must be called after loadImage
	 * @return width
	 */
	int getImageWidth();
	
	/**
	 * Must be called after loadImage
	 * @return height
	 */
	int getImageHeight();


}
