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

/**
 * This class describes an unsharp mask operation
 * 
 */
public class UnsharpMask implements IMask {
	/**
	 * bitshift value for mask values
	 */
	public static final int SHIFT = 12;
	/**
	 * maximum value and scaling factor for mask
	 */
	private static final int MAXVALUE = 1 << SHIFT;

	/**
	 * sharpening amount
	 */
	public float amount;
	/**
	 * unsharp mask radius in pixel
	 */
	public float radius;
	/**
	 * threshold value for noise control
	 */
	public float threshold;
	/**
	 * additional detail enhancement
	 */
	public float detail;
	/**
	 * tonemask for applying the sharpening operation only to specific gray
	 * values
	 */
	public Curve toneMask;
	/**
	 * Type of unsharp mask (SHARPEN, LOCAL_CONTRAST
	 */
	public final int type;
	/**
	 * used for sharpening
	 */
	public static final int SHARPEN = 0;
	/**
	 * used for local contrast
	 */
	public static final int LOCAL_CONTRAST = 1;

	/**
	 * @param amount
	 *            - sharpening amount 0..1
	 * @param radius
	 *            - unsharp mask radius in pixel 1..100
	 * @param threshold
	 *            - threshold value for noise control 0...1 - threshold value
	 *            for halo suppression 0...-1
	 * @param detail
	 *            - additional detail enhancement 0...1
	 * @param toneMask
	 *            - tonemask for applying the sharpening operation only to
	 *            specific gray values, or null
	 * @param type
	 *            - type of unsharp mask (SHARPEN, LOCAL_CONTRAST
	 */
	public UnsharpMask(float amount, float radius, float threshold,
			float detail, Curve toneMask, int type) {
		super();
		this.amount = amount;
		this.radius = radius;
		this.threshold = threshold;
		this.detail = detail;
		this.toneMask = toneMask;
		this.type = type;
	}

	/**
	 * Make a Gaussian blur kernel.
	 * 
	 * @return the kernel, elements in the range of 0...1<<SHIFT. Sums up to
	 *         1<<SHIFT. Returns null on a radius = 0.
	 */
	public int[] getKernel() {
		int r = (int) Math.ceil(radius);
		if (r == 0)
			return null;
		int rows = r * 2 + 1;
		float[] matrix = computeBell(r);
		if (detail != 0) {
			float[] dmatrix = computeBell(1);
			for (int i = 1; i <= dmatrix.length; i++)
				matrix[matrix.length - i] += detail
						* dmatrix[dmatrix.length - i];
		}
		float total = 0;
		for (int index = 0; index <= r; index++)
			total += (index == r ? matrix[index] : 2 * matrix[index]);
		int a = 0;
		while (a < r && matrix[a] / total <= 0.004)
			a++;
		int l = rows - 2 * a;
		if (l == 0)
			return null;
		total = 0;
		int[] kernel = new int[l];
		for (int index = a; index <= r; index++)
			total += (index == r ? matrix[index] : 2 * matrix[index]);
		for (int index = a, k = 0, m = l - 1; index <= r; index++, k++, m--)
			kernel[k] = kernel[m] = (int) (MAXVALUE * matrix[index] / total);
		return kernel;
	}

	private float[] computeBell(int r) {
		float[] matrix = new float[r + 1];
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float sigmaPi2 = (float) (2 * Math.PI * sigma);
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius * radius;
		for (int row = -r, index = 0; row <= 0; row++, index++) {
			float distance = row * row;
			if (distance > radius2)
				matrix[index] = 0f;
			else
				matrix[index] = (float) Math.exp(-(distance) / sigma22)
						/ sqrtSigmaPi2;
		}
		return matrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.zoom.image.recipe.IMask#createHslMaskArray(byte[], int,
	 * int)
	 */
	public byte[] createHslMaskArray(byte[] l, int width, int height) {
		if (amount <= 0.01f)
			return null;
		int[] kernel = getKernel();
		if (kernel == null || kernel.length <= 1)
			return null;
		int len = l.length;
		byte[] work1 = new byte[len];
		byte[] work2 = new byte[len];
		System.arraycopy(l, 0, work1, 0, len);
		ImageUtilities.convolveAndTranspose(kernel, work1, work2, width, height);
		ImageUtilities.convolveAndTranspose(kernel, work2, work1, height, width);
		return work1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.zoom.image.recipe.IMask#createLabMaskArray(short[], int,
	 * int)
	 */
	public short[] createLabMaskArray(short[] l, int width, int height) {
		if (amount <= 0.01f)
			return null;
		int[] kernel = getKernel();
		if (kernel == null || kernel.length <= 1 || amount <= 0.01f)
			return null;
		int len = l.length;
		short[] work1 = new short[len];
		short[] work2 = new short[len];
		System.arraycopy(l, 0, work1, 0, len);
		ImageUtilities.convolveAndTranspose(kernel, work1, work2, width, height);
		ImageUtilities.convolveAndTranspose(kernel, work2, work1, height, width);
		return work1;
	}

}
