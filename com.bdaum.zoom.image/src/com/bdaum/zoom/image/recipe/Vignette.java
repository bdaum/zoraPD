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
 * This class describes a vignette operation
 *
 */
public class Vignette {
	/**
	 * Vignette is applied to the L channel of the HSL color model
	 */
	public static final int HSL = 0;
	/**
	 * Vignette is applied to all channels of the RGB color model
	 */
	public static final int RGB = 1;
	/**
	 * vignette amount
	 */
	public float vignetteAmount;
	/**
	 * vignette amount
	 */
	public int strength = 1;
	/**
	 * vignette midpoint / radius
	 */
	public float vignetteMidpoint;
	/**
	 * vignette type
	 */
	public float centerX = 0f;
	/**
	 * vignette type
	 */
	public float centerY = 0f;
	/**
	 * vignette type
	 */
	public final int type;

	/**
	 * @param vignetteAmount
	 *            - vignette amount ... -1 ... +1
	 * @param vignetteMidpoint
	 *            - vignette midpoint 0...1
	 * @param type
	 *            - vignette type (HSL, RGB)
	 */
	public Vignette(float vignetteAmount, float vignetteMidpoint, int type) {
		this.vignetteAmount = vignetteAmount;
		this.vignetteMidpoint = vignetteMidpoint;
		this.type = type;
	}

	/**
	 * @param vignetteAmount
	 *            - vignette amount ... -1 ... +1
	 * @param vignetteMidpoint
	 *            - vignette midpoint 0...1
	 * @param strength - vignette strength 1...100
	 * @param centerX - vignette center X coordinate ... -1 ... +1
	 * @param centerY - vignette center Y coordinate ... -1 ... +1
	 * @param background - background color
	 * @param type - vignette type (HSL, RGB)
	 */
	public Vignette(float vignetteAmount, float vignetteMidpoint, int strength, float centerX, float centerY, int type) {
		this.vignetteAmount = vignetteAmount;
		this.vignetteMidpoint = vignetteMidpoint;
		this.strength = strength;
		this.centerX = centerX;
		this.centerY = centerY;
		this.type = type;
	}


	/**
	 * Tests if there is any vignetting
	 *
	 * @return - true if there is any vignetting
	 */
	public boolean isVignetting() {
		return vignetteAmount != 0f;
	}
}