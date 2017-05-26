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

import java.io.File;

/**
 * This interface describes the support for specific file formats
 *
 */
public interface IImportFilterFactory {
	/**
	 * @param file - the image file
	 * @return a file specific image loader
	 */
	IImageLoader getImageLoader(File file);
	
	/**
	 * This method requires that getImageLoader() was called before.
	 * @param file - the image file
	 * @return a file specific exif loader
	 */
	IExifLoader getExifLoader(File file);
	
	/**
	 * @param extension
	 * @return - the label for the file format
	 */
	String getLabel(String extension);
	
}
