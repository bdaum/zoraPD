/*******************************************************************************
 * Copyright (c) 2009-2017 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.recipe;

import com.bdaum.zoom.image.ImageUtilities;

public class MinMaxMask implements IMask {

	private float amount;

	public MinMaxMask(float amount, int radius, boolean min) {
		super();
		this.amount = amount;
		this.radius = radius;
		this.min = min;
	}

	private int radius;
	private boolean min;

	public byte[] createHslMaskArray(byte[] l, int width, int height) {
		if (amount <= 0.01f || radius <= 1)
			return null;
		int len = l.length;
		byte[] work1 = new byte[len];
		byte[] work2 = new byte[len];
		System.arraycopy(l, 0, work1, 0, len);
		ImageUtilities.convolveAndTranspose(radius, work1, work2, width, height, min);
		ImageUtilities.convolveAndTranspose(radius, work2, work1, height, width, min);
		return work1;
	}

	public short[] createLabMaskArray(short[] l, int width, int height) {
		if (amount <= 0.01f || radius <= 1)
			return null;
		int len = l.length;
		short[] work1 = new short[len];
		short[] work2 = new short[len];
		System.arraycopy(l, 0, work1, 0, len);
		ImageUtilities.convolveAndTranspose(radius, work1, work2, width, height, min);
		ImageUtilities.convolveAndTranspose(radius, work2, work1, height, width, min);
		return work1;
	}

}
