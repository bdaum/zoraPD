/*******************************************************************************
 * Copyright (c) 2009-2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.recipe;

import org.eclipse.swt.graphics.RGB;

/**
 * Generic geometric transformation
 * perspective, rotation, lens correction
 *
 * Algorithms are based on the algorithm found in Raw Therapee 3 (iptransform.cc)
 */
public class Transformation {

	/**
	 * Rotation degrees
	 */
	public double rotation;
	/**
	 * Horizontal perspective degrees
	 */
	public double horizontal;
	/**
	 * Vertical perspective degrees
	 */
	public double vertical;
	/**
	 * Lens distortion amount
	 */
	public double distortion;
	/**
	 * fill color for empty areas Null if autofill option is set
	 */
	public final RGB fillColor;

	public double fillScale;
	public double horPerspTan;
	public double horPerspCos;
	public double verPerspTan;
	public double verPerspCos;
	public double rotCos;
	public double rotSin;
	public boolean perspectiveChange;
	public boolean isRotating;
	public double maxRadius;

	/**
	 * Geometric transformation including rotation, perspective and lens
	 * distortion
	 *
	 * @param rotation
	 *            - Rotation degrees
	 * @param horizontal
	 *            - Horizontal perspective degrees
	 * @param vertical
	 *            - Vertical perspective degrees
	 * @param distortion
	 *            - Lens distortion amount
	 * @param fillColor
	 *            - fill color for empty areas, Null if autofill option is set
	 */
	public Transformation(double rotation, double horizontal, double vertical,
			double distortion, RGB fillColor) {
		super();
		this.rotation = rotation;
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.distortion = distortion;
		this.fillColor = fillColor;
	}

	/**
	 * Initialize this transform with width and height
	 * @param w - image width in pixels
	 * @param h - image height in pixels
	 */
	public void init(int w, int h) {
		perspectiveChange = horizontal != 0d || vertical != 0d;
		isRotating = rotation != 0d;
		maxRadius = Math.sqrt((w * w + h * h)) / 2d;
		rotCos = Math.cos(Math.toRadians(rotation));
		rotSin = Math.sin(Math.toRadians(rotation));

		double vpalpha = Math.toRadians(90d - vertical);
		double vtan = Math.tan(vpalpha);
		double vpteta = Math.abs(vpalpha - Math.PI / 2d) < 1e-3 ? 0.0 : Math
				.acos((vertical > 0 ? 1d : -1d)
						* Math.sqrt((-w * w * vtan * vtan + (vertical > 0 ? 1d
								: -1d)
								* w
								* vtan
								* Math.sqrt(16 * maxRadius * maxRadius + w * w
										* vtan * vtan))
								/ (maxRadius * maxRadius * 8)));
		verPerspCos = (vertical >= 0 ? 1d : -1d) * Math.cos(vpteta);
		verPerspTan = Math.tan(vpteta);
		double hpalpha = Math.toRadians(90d - horizontal);
		double htan = Math.tan(hpalpha);
		double hpteta = Math.abs(hpalpha - Math.PI / 2d) < 1e-3 ? 0.0
				: Math.acos((horizontal > 0 ? 1d : -1d)
						* Math.sqrt((-h * h * htan * htan + (horizontal > 0 ? 1d
								: -1d)
								* h
								* htan
								* Math.sqrt(16 * maxRadius * maxRadius + h * h
										* htan * htan))
								/ (maxRadius * maxRadius * 8)));
		horPerspCos = (horizontal >= 0 ? 1d : -1d) * Math.cos(hpteta);
		horPerspTan = Math.tan(hpteta);

		fillScale = fillColor == null ? getTransformAutoFill(w, h) : 1d;
	}

	private double getTransformAutoFill(int w, int h) {
		int x2 = w - 1;
		int y2 = h - 1;
		double[] xcoord = new double[] { 0, x2 / 2d, x2, x2, 0, x2 / 2d, x2, 0 };
		double[] ycoord = new double[] { 0, 0, 0, y2 / 2d, y2, y2, y2, y2 / 2d };
		double scaleU = 1d;
		double scaleL = 0.001d;
		while (scaleU - scaleL > 0.001d) {
			double scale = (scaleU + scaleL) / 2d;
			if (testClipping(w, h, scale, xcoord, ycoord))
				scaleU = scale;
			else
				scaleL = scale;
		}
		return scaleL;
	}

	private boolean testClipping(double w, double h, double ascaleDef,
			double[] xcoord, double[] ycoord) {
		double w2 = (w - 1) / 2d;
		double h2 = (h - 1) / 2d;
		for (int i = 0; i < ycoord.length; i++) {
			double x_d = ascaleDef * (xcoord[i] - w2); // centering x coord &
			// scale
			double y_d = ascaleDef * (ycoord[i] - h2); // centering y coord &
			// scale
			if (perspectiveChange) {
				// horizontal perspective transformation
				y_d = y_d * maxRadius / (maxRadius + x_d * horPerspTan);
				x_d = x_d * maxRadius * horPerspCos / (maxRadius + x_d * horPerspTan);

				// vertical perspective transformation
				x_d = x_d * maxRadius / (maxRadius - y_d * verPerspTan);
				y_d = y_d * maxRadius * verPerspCos / (maxRadius - y_d * verPerspTan);
			}
			double px, py;
			if (isRotating) {
				// rotate
				px = x_d * rotCos - y_d * rotSin;
				py = x_d * rotSin + y_d * rotCos;
			} else {
				px = x_d;
				py = y_d;
			}
			if (distortion != 0d) {
				// distortion correction
				double s = 1d - distortion + distortion
						* (Math.sqrt(px * px + py * py) / maxRadius);
				px *= s;
				py *= s;
			}
			// de-center
			px += w2;
			py += h2;
			if (px < 0d || px >= w || py < 0d || py >= h)
				return true;
		}
		return false;
	}

	/**
	 * Tests if this transformation has any effect
	 *
	 * @return - true if the perspective transformation has any effect
	 */
	public boolean isCorrecting() {
		return rotation != 0d || horizontal != 0d || vertical != 0d
				|| distortion != 0d;
	}

}
