/*******************************************************************************
 * Copyright (c) 2010 Berthold Daum.
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
 * This interface describes filehandlers for specific file formats, for example
 * ZIP-based packages such as EIP files
 * 
 */
public interface IFileHandler {

	/**
	 * Returns the image file
	 * 
	 * @param in
	 *            input file
	 * @return - image file
	 */
	File getImageFile(File in);

	/**
	 * Returns the sidecar file or null if no sidecar exists
	 * 
	 * @param in
	 *            input file
	 * @return - sidecar file or null
	 */
	File getSidecar(File in);

}
