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
 * This class describes a color boost and color shift operation in the Lab color model
 * 
 */
public class ColorBoost {

	/**
	 * Boost value for the a-channel of the Lab color model
	 */
	public final float aBoost;
	/**
	 * Shift value for the a-channel of the Lab color model
	 */
	public final float bShift;
	/**
	 * Boost value for the b-channel of the Lab color model
	 */
	public final float bBoost;
	/**
	 * Shift value for the b-channel of the Lab color model
	 */
	public final float aShift;

	/**
	 * Constructor.
	 * 
	 * @param aBoost
	 *            - Boost value for the a-channel of the Lab color model: 0 .. 1
	 *            .. 3
	 * @param bBoost
	 *            - Boost value for the b-channel of the Lab color model: 0 .. 1
	 *            .. 3
	 * @param aShift
	 *            - Shift value for the a-channel of the Lab color model: -1 ..
	 *            0 .. 1
	 * @param bShift
	 *            - Shift value for the b-channel of the Lab color model: -1 ..
	 *            0 .. 1
	 */
	public ColorBoost(float aBoost, float bBoost, float aShift, float bShift) {
		this.aBoost = aBoost;
		this.bBoost = bBoost;
		this.aShift = aShift;
		this.bShift = bShift;
	}
	
	/**
	 * Returns if colors are boosted
	 * @return true if colors are boosted
	 */
	public boolean isBoosting() {
		return aBoost != 1f || bBoost != 1f;
	}
	
	/**
	 * Returns if colors are shifted
	 * @return true if colors are shifted
	 */
	public boolean isShifting() {
		return aShift != 0f || bShift != 0f;
	}
}
