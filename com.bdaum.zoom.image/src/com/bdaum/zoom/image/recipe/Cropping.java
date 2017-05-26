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

import java.awt.Dimension;
import java.awt.Rectangle;

import org.eclipse.swt.graphics.RGB;

/**
 * This class describes the cropping of an image.
 * First, the image is rotated around its center width the given crop angle.
 * If the fill mode FILL was specified, the image is enlarged 
 * so that no empty areas appear in the original image area. 
 * If the crop-angle is not a multiple of 180 degrees, some image areas will fall outside
 * the original image area.
 * Then the cropping is performed in relation to the original image area.
 *
 */
public class Cropping {
	/**
	 * Do enlarge image to fill empty areas caused by rotation
	 */
	public static final int NOFILL = 0;
	/**
	 * Enlarge image to fill empty areas caused by rotation
	 */
	public static final int FILL = 1;
	/**
	 * Y-position of top crop border (0..1)
	 */
	public float cropTop;
	/**
	 * Y-position of top bottom border (0..1)
	 */
	public float cropBottom;
	/**
	 * X-position of left crop border (0..1)
	 */
	public float cropLeft;
	/**
	 * X-position of right crop border (0..1)
	 */
	public float cropRight;
	/**
	 * Crop angle in degrees
	 */
	public float cropAngle;
	/**
	 * Contains fill mode (FILL, NOFILL)
	 */
	public int fillMode = NOFILL;
	/**
	 * Fill color for empty areas
	 */
	public final RGB fillColor;

	private Rectangle rect;
	private Dimension cropDim;

	/**
	 * Constructor
	 * 
	 * @param cropTop
	 *            - Y-position of top crop border (0..1)
	 * @param cropBottom
	 *            - Y-position of bottom crop border (0..1)
	 * @param cropLeft
	 *            - X-position of left crop border (0..1)
	 * @param cropRight
	 *            - X-position of right crop border (0..1)
	 * @param cropAngle
	 *            - Crop angle in degrees
	 * @param fillMode
	 *            - fill mode (FILL, NOFILL)
	 * @param fillColor
	 *            - fill color for empty areas or null
	 */
	public Cropping(float cropTop, float cropBottom, float cropLeft,
			float cropRight, float cropAngle, int fillMode, RGB fillColor) {
		this.cropTop = cropTop;
		this.cropBottom = cropBottom;
		this.cropLeft = cropLeft;
		this.cropRight = cropRight;
		this.cropAngle = cropAngle;
		this.fillMode = fillMode;
		this.fillColor = fillColor;
	}

	/**
	 * Constructor
	 * 
	 * @param cropTop
	 *            - Y-position of top crop border (0..1)
	 * @param cropLeft
	 *            - X-position of left crop border (0..1)
	 * @param cropDim
	 *            - Width and height of crop area in pixels
	 * @param cropAngle
	 *            - Crop angle in degrees
	 * @param fillMode
	 *            - fill mode (FILL, NOFILL)
	 * @param fillColor
	 *            - fill color for empty areas or null
	 */
	public Cropping(float cropTop, float cropLeft, Dimension cropDim,
			float cropAngle, int fillMode, RGB fillColor) {
		this.cropTop = cropTop;
		this.cropLeft = cropLeft;
		this.cropDim = cropDim;
		this.cropAngle = cropAngle;
		this.fillMode = fillMode;
		this.fillColor = fillColor;
	}

	/**
	 * Constructor
	 * 
	 * @param rect
	 *            - rectangle describing size and position of crop area in pixels
	 * @param cropAngle
	 *            - Crop angle in degrees
	 * @param fill
	 *            - fill mode (FILL, NOFILL)
	 * @param fillColor
	 *            - fill color for empty areas or null
	 */
	public Cropping(Rectangle rect, float cropAngle, int fill, RGB fillColor) {
		this.rect = rect;
		this.cropAngle = cropAngle;
		this.fillMode = fill;
		this.fillColor = fillColor;
	}

	/**
	 * Initializes the cropping operator. MUST be called before cropping is
	 * executed.
	 * 
	 * @param width
	 *            - image width in pixel
	 * @param height
	 *            - image height in pixel
	 * @param scalingFactor
	 *            - image scaling factor
	 * @return true if cropping occurs, false if nothing is cropped
	 */
	public boolean init(int width, int height, double scalingFactor) {
		if (rect != null) {
			cropLeft = (float) (rect.x * scalingFactor / width);
			cropTop = (float) (rect.y * scalingFactor / height);
			cropRight = (float) ((rect.x  + rect.width) * scalingFactor / width);
			cropBottom = (float) ((rect.y  + rect.height) * scalingFactor / height);
			rect = null;
		} else if (cropDim != null) {
			cropRight = (float) (cropLeft + cropDim.getWidth() * scalingFactor
					/ width);
			cropBottom = (float) (cropTop + cropDim.getHeight() * scalingFactor
					/ height);
		}
		return cropLeft != 0f || cropTop != 0f || cropRight != 0f
				|| cropBottom != 0f || cropAngle != 0f;
	}

}