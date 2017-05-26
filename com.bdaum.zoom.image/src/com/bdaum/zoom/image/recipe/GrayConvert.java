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
 * This class describes a gray conversion operation
 */
public class GrayConvert {
	/**
	 * factor for red colors
	 */
	public float grayMixerRed;
	/**
	 * factor for orange colors
	 */
	public float grayMixerOrange;
	/**
	 * factor for yellow colors
	 */
	public float grayMixerYellow;
	/**
	 * factor for green colors
	 */
	public float grayMixerGreen;
	/**
	 * factor for aqua colors
	 */
	public float grayMixerAqua;
	/**
	 * factor for blue colors
	 */
	public float grayMixerBlue;
	/**
	 * factor for purple colors
	 */
	public float grayMixerPurple;
	/**
	 * factor for magenta colors
	 */
	public float grayMixerMagenta;

	/**
	 * Constructor
	 * 
	 * @param grayMixerRed
	 *            - factor for red colors
	 * @param grayMixerOrange
	 *            - factor for orange colors
	 * @param grayMixerYellow
	 *            - factor for yellow colors
	 * @param grayMixerGreen
	 *            - factor for green colors
	 * @param grayMixerAqua
	 *            - factor for aqua colors
	 * @param grayMixerBlue
	 *            - factor for blue colors
	 * @param grayMixerPurple
	 *            - factor for purple colors
	 * @param grayMixerMagenta
	 *            - factor for magenta colors
	 */
	public GrayConvert(float grayMixerRed, float grayMixerOrange,
			float grayMixerYellow, float grayMixerGreen, float grayMixerAqua,
			float grayMixerBlue, float grayMixerPurple, float grayMixerMagenta) {
		this.grayMixerRed = grayMixerRed;
		this.grayMixerOrange = grayMixerOrange;
		this.grayMixerYellow = grayMixerYellow;
		this.grayMixerGreen = grayMixerGreen;
		this.grayMixerAqua = grayMixerAqua;
		this.grayMixerBlue = grayMixerBlue;
		this.grayMixerPurple = grayMixerPurple;
		this.grayMixerMagenta = grayMixerMagenta;
	}

	public boolean isConverting() {
		return true;
	}
}