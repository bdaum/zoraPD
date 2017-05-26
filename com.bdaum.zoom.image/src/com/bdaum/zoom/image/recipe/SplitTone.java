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
 * This class describes selective color toning for highlights and shadows 
 */
public class SplitTone {
	/**
	 * hue shift for shadows
	 */
	public float shadowHue;
	/**
	 * saturation shift for shadows
	 */
	public float shadowSaturation;
	/**
	 * hue shift for highlights
	 */
	public float highlightHue;
	/**
	 * saturation shift for highlights
	 */
	public float highlightSaturation;
	/**
	 * true if there will be any toning
	 */
	public boolean isToning;
	/**
	 * distribution of highlights and shadows
	 */
	public final Curve distribution;

	/**
	 * Constructor
	 * @param shadowHue - hue shift for shadows
	 * @param shadowSaturation - saturation shift for shadows
	 * @param highlightHue - hue shift for highlights
	 * @param highlightSaturation - saturation shift for highlights
	 * @param distribution - a curve defining the distribution of highlights and shadows
	 */
	public SplitTone(float shadowHue, float shadowSaturation,
			float highlightHue, float highlightSaturation, Curve distribution) {
		this.shadowHue = shadowHue;
		this.distribution = distribution;
		this.shadowSaturation = Math.max(0f, Math.min(1f, shadowSaturation));
		this.highlightHue = highlightHue;
		this.highlightSaturation =  Math.max(0f, Math.min(1f, highlightSaturation));
		isToning = shadowSaturation != 0f || highlightSaturation != 0f;
	}
}