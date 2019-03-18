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

package com.bdaum.zoom.program;

import java.io.File;

import com.bdaum.zoom.batch.internal.ConversionException;
import com.bdaum.zoom.batch.internal.Options;

/**
 * Converter service description
 *
 */
public interface IConverter {

	static final String CONVERTER = "converter"; //$NON-NLS-1$´
	//Options
//	static final String ADOBE_RGB = "AdobeRGB"; //$NON-NLS-1$
	static final String EXPOSURE = "exposure"; //$NON-NLS-1$
	static final String GAMMA= "gamma"; //$NON-NLS-1$
	static final String DENOISE = "denoise"; //$NON-NLS-1$
	static final String ABBERATION = "abberation"; //$NON-NLS-1$

	/**
	 * Sets the location of the converter executable
	 * @param location - location of the converter executable
	 */
	void setConverterLocation(String location);

	/**
	 * Prepares the execution parameters from the provided options
	 * @param options - conversion options
	 * @return - array of command line parameters
	 */
	String[] getParms(Options options);

	/**
	 * Sets the input file for conversion
	 * @param file - input file
	 * @param options - conversion options
	 * @return output file
	 * @throws ConversionException 
	 */
	File setInput(File file, Options options) throws ConversionException;

	/**
	 * This method can be called after setInput to obtain the real input directory
	 * (which may be the directory of a temporary input file)
	 * @return - parent folder of actual input file
	 */
	File getInputDirectory();

}
