/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image;

/**
 * This interface describes instances that can deliver the 35 mm equivalent
 * focal length
 * 
 */

public interface IFocalLengthProvider {
	/**
	 * Return the 35mm equivalent focal length
	 * 
	 * @returns 35mm equivalent focal length
	 */
	double get35mm();

}
