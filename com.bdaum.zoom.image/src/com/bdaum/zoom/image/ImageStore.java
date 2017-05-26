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

import org.eclipse.swt.graphics.Image;

/**
 * An image store returns an image for a given image source
 * Typically it will cache images for better performance
 *
 */
public interface ImageStore {

	/**
	 * Fetch the image from a given image source
	 * @param imageSource - image source
	 * @return - image 
	 */
	Image getImage(Object imageSource);

}