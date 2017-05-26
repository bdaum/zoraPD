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
package com.bdaum.zoom.image.recipe;

/**
 * This class describes a user defined rotation
 */
public class Rotation {

	/**
	 * transformation angle (0, 90, 180, 270)
	 */
	public int angle;
	/**
	 * true for horizontal mirroring
	 */
	public boolean flipH;
	/**
	 * true for vertical mirroring
	 */
	public boolean flipV;

	/**
	 * Constructor
	 * 
	 * @param angle
	 *            - transformation angle (0, 90, 180, 270)
	 * @param flipH
	 *            - true for horizontal mirroring
	 * @param flipV
	 *            - true for vertical mirroring
	 */
	public Rotation(int angle, boolean flipH, boolean flipV) {
		this.angle = angle;
		while (this.angle < 0)
			this.angle += 360;
		while (this.angle >= 360)
			this.angle -= 360;
		this.flipH = flipH;
		this.flipV = flipV;
	}

}
