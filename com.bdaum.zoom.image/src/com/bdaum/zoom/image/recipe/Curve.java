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

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements curves of various types
 */
public class Curve {
	/**
	 * Linear interpolation between knots
	 */
	public static final int TYPE_LINEAR = 0;
	/**
	 * CatMull interpolation between knots. The curve will touch all knots
	 */
	public static final int TYPE_CATMULL_ROM = 1;
	/**
	 * B-Spline interpolation between knots. The curve will not necessarily
	 * touch all knots
	 */
	public static final int TYPE_B_SPLINE = 2;

	/**
	 * FCT-MinMax type
	 */
	public static final int TYPE_MINMAX = 3;

	private static final int TYPE_GAMMA = 4;
	private static final int TYPE_S = 5;

	public static final int CHANNEL_ALL = 1;
	public static final int CHANNEL_RED = 2;
	public static final int CHANNEL_GREEN = 4;
	public static final int CHANNEL_BLUE = 8;
	public static final int CHANNEL_LUMINANCE = 16;
	public static final int CHANNEL_L = 32;
	public static final int CHANNEL_A = 64;
	public static final int CHANNEL_B = 128;
	public static final int CHANNEL_HUE = 256;
	public static final int CHANNEL_SATURATION = 512;
	public static final int CHANNEL_VALUE = 1024;
	public static final int CHANNEL_CHROMA = 2048;

	private float[] x;
	private float[] y;
	private float[] leftTangent;
	private float[] rightTangent;
	private final int type;
	private final String name;
	private final int channel;
	private float preserveShadow;
	private IMask mask;
	private float gamma = 1f;
	private float median = 0f;
	private boolean periodic = false;
	private boolean init = false;
	private int numKnots = 0;

	/**
	 * Constructor for LINEAR, CATMULL, B-SPLINE
	 *
	 * @param type
	 *            - curve type
	 * @param name
	 *            - a curve name (for debug purposes only)
	 * @param channel
	 *            - channel (all, red, green, blue)
	 * @param preserveShadow
	 *            - amount of shadow preservation (0..1)
	 */
	public Curve(int type, String name, int channel, float preserveShadow) {
		this(type, name, channel, preserveShadow, false);
	}

	/**
	 * Constructor for LINEAR, CATMULL, B-SPLINE
	 *
	 * @param type
	 *            - curve type
	 * @param name
	 *            - a curve name (for debug purposes only)
	 * @param channel
	 *            - channel (all, red, green, blue)
	 * @param preserveShadow
	 *            - amount of shadow preservation (0..1)
	 * @param periodic
	 *            - true if curve is periodic (e.g. for hue)
	 */
	public Curve(int type, String name, int channel, float preserveShadow,
			boolean periodic) {
		this.type = type;
		this.name = name;
		this.channel = channel;
		this.preserveShadow = preserveShadow;
		this.periodic = periodic;
		x = new float[0];
		y = new float[0];
	}

	/**
	 * Constructor for GAMMA
	 *
	 * @param gamma
	 *            - gamma coefficient (default 1)
	 * @param name
	 *            - a curve name (for debug purposes only)
	 * @param channel
	 *            - channel (all, red, green, blue)
	 * @param preserveShadow
	 *            - amount of shadow preservation (0..1)
	 */
	public Curve(float gamma, String name, int channel, float preserveShadow) {
		this.gamma = gamma;
		this.type = TYPE_GAMMA;
		this.name = name;
		this.channel = channel;
		this.preserveShadow = preserveShadow;
	}

	/**
	 * Constructor for S-shape
	 *
	 *
	 * @param gamma
	 *            - gamma coefficient (default 1)
	 * @param median
	 *            - median coefficient (default 0)
	 * @param name
	 *            - a curve name (for debug purposes only)
	 * @param channel
	 *            - channel (all, red, green, blue)
	 * @param preserveShadow
	 *            - amount of shadow preservation (0..1)
	 */
	public Curve(float gamma, float median, String name, int channel,
			float preserveShadow) {
		this.gamma = gamma;
		this.median = median;
		this.type = TYPE_S;
		this.name = name;
		this.channel = channel;
		this.preserveShadow = preserveShadow;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Adds a knot to the curve
	 *
	 * @param kx
	 *            - x position of knot (0..1)
	 * @param ky
	 *            - y position of knot (0..1)
	 * @param leftTan
	 *            - leftTangent of knot (0..1)
	 * @param rightTan
	 *            - rightTangent of knot (0..1)
	 */
	public void addKnot(float kx, float ky, float leftTan, float rightTan) {
		int pos = -1;
		float[] nx = new float[numKnots + 1];
		float[] ny = new float[numKnots + 1];
		float[] nLeftTan = new float[numKnots + 1];
		float[] nRightTan = new float[numKnots + 1];
		int j = 0;
		for (int i = 0; i < numKnots; i++) {
			if (pos == -1 && x[i] > kx) {
				pos = j;
				nx[j] = kx;
				ny[j] = ky;
				nLeftTan[j] = leftTan;
				nRightTan[j] = rightTan;
				j++;
			}
			nx[j] = x[i];
			ny[j] = y[i];
			nLeftTan[j] = leftTangent == null ? Float.NaN : leftTangent[i];
			nRightTan[j] = rightTangent == null ? Float.NaN : rightTangent[i];
			j++;
		}
		if (pos == -1) {
			nx[j] = kx;
			ny[j] = ky;
			nLeftTan[j] = leftTan;
			nRightTan[j] = rightTan;
		}
		x = nx;
		y = ny;
		leftTangent = nLeftTan;
		rightTangent = nRightTan;
		++numKnots;
	}

	/**
	 * Adds a knot to the curve
	 *
	 * @param kx
	 *            - x position of knot (0..1)
	 * @param ky
	 *            - y position of knot (0..1)
	 */
	public void addKnot(float kx, float ky) {
		int pos = -1;
		float[] nx = new float[numKnots + 1];
		float[] ny = new float[numKnots + 1];
		int j = 0;
		for (int i = 0; i < numKnots; i++) {
			if (pos == -1 && x[i] > kx) {
				pos = j;
				nx[j] = kx;
				ny[j] = ky;
				j++;
			}
			nx[j] = x[i];
			ny[j] = y[i];
			j++;
		}
		if (pos == -1) {
			nx[j] = kx;
			ny[j] = ky;
		}
		x = nx;
		y = ny;
		++numKnots;
	}

	/**
	 * Perform interpolation between knots using the curves interpolation type
	 *
	 * @param range
	 *            - value range for X and Y parameters
	 * @return - the interpolated and scaled curve
	 */
	public short[] makeTable(int range) {
		short[] table = new short[range + 1];
		switch (type) {
		case TYPE_CATMULL_ROM: {
			int l_4 = 4 * table.length;
			float[] nx = new float[numKnots + 2];
			float[] ny = new float[numKnots + 2];
			System.arraycopy(x, 0, nx, 1, numKnots);
			System.arraycopy(y, 0, ny, 1, numKnots);
			nx[0] = nx[1];
			ny[0] = ny[1];
			nx[numKnots + 1] = nx[numKnots];
			ny[numKnots + 1] = ny[numKnots];
			for (int i = 0; i < l_4; i++) {
				float f = (float) i / l_4;
				int xx = (int) (range * catmull(f, nx) + 0.5f);
				int yy = (int) (range * catmull(f, ny) + 0.5f);
				xx = (xx < 0) ? 0 : (xx > range) ? range : xx;
				table[xx] = yy <= 0 ? 0 : yy >= range ? (short) range : (short) yy;
			}
			break;
		}
		case TYPE_B_SPLINE: {
			float[] nx = new float[numKnots + 2];
			float[] ny = new float[numKnots + 2];
			System.arraycopy(x, 0, nx, 1, numKnots);
			System.arraycopy(y, 0, ny, 1, numKnots);
			nx[0] = nx[1];
			ny[0] = ny[1];
			nx[numKnots + 1] = nx[numKnots];
			ny[numKnots + 1] = ny[numKnots];
			// Bspline interpolation
			// after from Section 4.2 of
			// Ammeraal, L. (1998) Computer Graphics for Java Programmers,
			// Chichester: John Wiley.
			int n = nx.length;
			int px = 0;
			int py = (int) (ny[0] * range);
			table[0] = (short) py;
			float xA, yA, xB, yB, xC, yC, xD, yD, a0, a1, a2, a3, b0, b1, b2, b3, xx = 0, yy = 0, m;
			for (int i = 1; i < n - 2; i++) {
				xA = nx[i - 1];
				xB = nx[i];
				xC = nx[i + 1];
				xD = nx[i + 2];
				yA = ny[i - 1];
				yB = ny[i];
				yC = ny[i + 1];
				yD = ny[i + 2];
				a3 = (-xA + 3 * (xB - xC) + xD) / 6;
				b3 = (-yA + 3 * (yB - yC) + yD) / 6;
				a2 = (xA - 2 * xB + xC) / 2;
				b2 = (yA - 2 * yB + yC) / 2;
				a1 = (xC - xA) / 2;
				b1 = (yC - yA) / 2;
				a0 = (xA + 4 * xB + xC) / 6;
				b0 = (yA + 4 * yB + yC) / 6;
				// Mapping to table
				int txb = (int) (xB * range);
				int txc = (int) (xC * range);
				m = txc - txb;
				for (int j = 0; j <= m; j++) {
					float t = j / m;
					xx = ((a3 * t + a2) * t + a1) * t + a0;
					yy = ((b3 * t + b2) * t + b1) * t + b0;
					int tx = (int) (xx * range);
					if (tx < 0)
						tx = 0;
					else if (tx > range)
						tx = range;
					int ty = (int) (yy * range);
					if (ty < 0)
						ty = 0;
					else if (ty > range)
						ty = range;
					setTableValues(table, px, py, tx, ty, range);
					px = tx;
					py = ty;
				}
				setTableValues(table, px, py, range, (int) (ny[n - 1] * range),
						range);
			}
			break;
		}
		case TYPE_MINMAX: {
			// algorithm based on algorithm found in RawTherapee 3
			// (FlatCurves.cc)
			if (!init && numKnots > 0) {
				if (periodic) {
					init = true;
					float[] nX = new float[numKnots + 1];
					float[] nY = new float[numKnots + 1];
					System.arraycopy(x, 0, nX, 0, numKnots);
					System.arraycopy(y, 0, nY, 0, numKnots);
					nX[numKnots] = x[0] + 1f;
					nY[numKnots] = y[0];
					x = nX;
					y = nY;
					if (leftTangent != null) {
						float[] nLeft = new float[numKnots + 1];
						System.arraycopy(leftTangent, 0, nLeft, 0, numKnots);
						nLeft[numKnots] = leftTangent[0];
						leftTangent = nLeft;
					}
					if (rightTangent != null) {
						float[] nRight = new float[numKnots + 1];
						System.arraycopy(rightTangent, 0, nRight, 0, numKnots);
						nRight[numKnots] = rightTangent[0];
						rightTangent = nRight;
					}
				} else
					--numKnots;
			}
			int ppn = 3; // TODO find out correct value
			int nbSubCurvesPoints = numKnots * 6;
			List<Double> poly_x = new ArrayList<Double>();
			List<Double> poly_y = new ArrayList<Double>();
			double[] sc_x = new double[nbSubCurvesPoints];
			double[] sc_y = new double[nbSubCurvesPoints];
			double[] sc_length = new double[numKnots * 2];
			boolean[] sc_isLinear = new boolean[numKnots * 2];
			double total_length = 0.;

			int j = 0, k = 0;
			for (int i = 0; i < numKnots;) {
				double length;
				double dx;
				double dy;
				double xp1, xp2, yp2, xp3;

				boolean startLinear = (rightTangent[i] == 0.)
						|| (y[i] == y[i + 1]);
				boolean endLinear = (leftTangent[i + 1] == 0.)
						|| (y[i] == y[i + 1]);

				if (startLinear && endLinear) {
					// line shape
					sc_x[j] = x[i];
					sc_y[j++] = y[i];
					sc_x[j] = x[i + 1];
					sc_y[j] = y[i + 1];
					sc_isLinear[k] = true;
					i++;
					dx = sc_x[j] - sc_x[j - 1];
					dy = sc_y[j] - sc_y[j - 1];
					length = Math.sqrt(dx * dx + dy * dy);
					j++;
					sc_length[k++] = length;
					total_length += length;
				} else {
					if (startLinear)
						xp1 = x[i];
					else
						xp1 = (x[i + 1] - x[i]) * rightTangent[i] + x[i];
					if (endLinear)
						xp3 = x[i + 1];
					else
						xp3 = (x[i] - x[i + 1]) * leftTangent[i + 1] + x[i + 1];

					xp2 = (xp1 + xp3) / 2.0;
					yp2 = (y[i] + y[i + 1]) / 2.0;

					if (rightTangent[i] + leftTangent[i + 1] > 1.0)
						xp1 = xp3 = xp2;

					if (startLinear) {
						sc_x[j] = x[i];
						sc_y[j++] = y[i];
						sc_x[j] = xp2;
						sc_y[j] = yp2;
						sc_isLinear[k] = true;

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length = Math.sqrt(dx * dx + dy * dy);
						j++;
						sc_length[k++] = length;
						total_length += length;
					} else {
						sc_x[j] = x[i];
						sc_y[j++] = y[i];
						sc_x[j] = xp1;
						sc_y[j] = y[i];

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length = Math.sqrt(dx * dx + dy * dy);
						j++;

						sc_x[j] = xp2;
						sc_y[j] = yp2;
						sc_isLinear[k] = false;

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length += Math.sqrt(dx * dx + dy * dy);
						j++;
						sc_length[k++] = length;
						total_length += length;
					}
					if (endLinear) {
						sc_x[j] = xp2;
						sc_y[j++] = yp2;
						sc_x[j] = x[i + 1];
						sc_y[j] = y[i + 1];
						sc_isLinear[k] = true;

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length = Math.sqrt(dx * dx + dy * dy);
						j++;
						sc_length[k++] = length;
						total_length += length;
					} else {
						sc_x[j] = xp2;
						sc_y[j++] = yp2;
						sc_x[j] = xp3;
						sc_y[j] = y[i + 1];

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length = Math.sqrt(dx * dx + dy * dy);
						j++;

						sc_x[j] = x[i + 1];
						sc_y[j] = y[i + 1];
						sc_isLinear[k] = false;

						dx = sc_x[j] - sc_x[j - 1];
						dy = sc_y[j] - sc_y[j - 1];
						length += Math.sqrt(dx * dx + dy * dy);
						j++;
						sc_length[k++] = length;
						total_length += length;
					}
					i++;
				}
			}

			poly_x.clear();
			poly_y.clear();
			j = 0;

			if (!periodic && sc_x[j] != 0.) {
				poly_x.add(0d);
				poly_y.add(sc_y[j]);
			}

			poly_x.add(sc_x[j]);
			poly_y.add(sc_y[j]);

			boolean firstPointIncluded = false;

			for (int i = 0; i < k; i++) {
				if (sc_isLinear[i]) {
					j++;
					poly_x.add(sc_x[j]);
					poly_y.add(sc_y[j++]);
				} else {
					int nbr_points = (int) (((ppn) * sc_length[i]) / total_length);
					if (nbr_points < 0)
						return null;
					double increment = 1.0 / (nbr_points - 1);
					double x1 = sc_x[j];
					double y1 = sc_y[j++];
					double x2 = sc_x[j];
					double y2 = sc_y[j++];
					double x3 = sc_x[j];
					double y3 = sc_y[j++];
					addPolygons(poly_x, poly_y, firstPointIncluded, nbr_points,
							increment, x1, y1, x2, y2, x3, y3);
				}
			}

			poly_x.add(3.0);
			poly_y.add(sc_y[j - 1]);
			for (int i = 0; i < table.length; i++)
				table[i] = (short) (range * getVal((double) i / range, poly_x,
						poly_y));
			break;
		}
		case TYPE_GAMMA: {
			for (int i = 0; i < table.length; i++)
				table[i] = (short) (Math.pow((double) i / range, gamma) * range + 0.5d);
			break;
		}
		case TYPE_S: {
			double l2 = table.length / 2d;
			double ylow = Math.tanh(-gamma + median);
			double yhigh = Math.tanh(gamma + median);
			double fac = range / (yhigh - ylow);
			for (int i = 0; i < table.length; i++)
				table[i] = (short) ((Math
						.tanh(gamma * ((i - l2) / l2) + median) - ylow) * fac + 0.5d);
			break;
		}
		default: {
			float[] nx = new float[numKnots + 2];
			float[] ny = new float[numKnots + 2];
			System.arraycopy(x, 0, nx, 1, numKnots);
			System.arraycopy(y, 0, ny, 1, numKnots);
			nx[0] = 0f;
			ny[0] = ny[1];
			nx[numKnots + 1] = 1f;
			ny[numKnots + 1] = ny[numKnots];
			int k = 0;
			float ux = nx[0];
			float ox = nx[1];
			float uy = ny[0];
			float oy = ny[1];
			float dx = ox - ux;
			float dy = oy - uy;
			for (int i = 1; i <= range; i++) {
				float fx = (float) i / range;
				while (fx > ox || dx == 0f) {
					++k;
					ux = nx[k];
					ox = nx[k + 1];
					uy = ny[k];
					oy = ny[k + 1];
					dx = ox - ux;
					dy = oy - uy;
				}
				float fy = (fx - ux) / dx * dy + uy;
				int v = (int) (range * fy + 0.5f);
				table[i] = v <= 0 ? 0 : v >= range ? (short) range : (short) v;
			}
			break;
		}
		}
		return performPreserveShadows(table);
	}

	private static void addPolygons(List<Double> poly_x, List<Double> poly_y,
			boolean firstPointIncluded, int nbr_points, double increment,
			double x1, double y1, double x2, double y2, double x3, double y3) {
		if (firstPointIncluded) {
			poly_x.add(x1);
			poly_y.add(y1);
		}
		for (int k = 1; k < (nbr_points - 1); k++) {
			double t = k * increment;
			double t2 = t * t;
			double tr = 1. - t;
			double tr2 = tr * tr;
			double tr2t = tr * 2 * t;

			// adding a point to the polyline
			poly_x.add(tr2 * x1 + tr2t * x2 + t2 * x3);
			poly_y.add(tr2 * y1 + tr2t * y2 + t2 * y3);
		}
		// adding the last point of the sub-curve
		poly_x.add(x3);
		poly_y.add(y3);
	}

	private static double getVal(double t, List<Double> poly_x, List<Double> poly_y) {

		// magic to handle curve periodicity : we look above the 1.0 bound for
		// the value
		if (t < poly_x.get(0))
			t += 1.0;

		// do a binary search for the right interval:
		int k_lo = 0, k_hi = poly_x.size() - 1;
		while (k_hi - k_lo > 1) {
			int k = (k_hi + k_lo) / 2;
			if (poly_x.get(k) > t)
				k_hi = k;
			else
				k_lo = k;
		}
		double dx = poly_x.get(k_hi) - poly_x.get(k_lo);
		double dy = poly_y.get(k_hi) - poly_y.get(k_lo);
		return poly_y.get(k_lo) + (t - poly_x.get(k_lo)) * dy / dx;
	}

	private short[] performPreserveShadows(short[] table) {
		if (preserveShadow > 0f)
			for (int i = 0; i < table.length; i++)
				if (table[i] < i)
					table[i] += (short) ((i - table[i]) * preserveShadow + 0.5f);
		return table;
	}

	private static void setTableValues(short[] table, int px, int py, int tx, int ty,
			int max) {
		if (px < tx - 1) {
			int dx = tx - px;
			int dy = ty - py;
			for (int xx = px + 1; xx <= tx; xx++) {
				int v = py + ((xx - px) * dy + dx / 2) / dx;
				table[xx] = v <= 0 ? 0 : v > max ? (short) max : (short) v;
			}
		} else
			table[tx] = (short) ty;
	}

	// Catmull-Rom splines
	private final static float m00 = -0.5f;
	private final static float m01 = 1.5f;
	private final static float m02 = -1.5f;
	private final static float m03 = 0.5f;
	private final static float m10 = 1.0f;
	private final static float m11 = -2.5f;
	private final static float m12 = 2.0f;
	private final static float m13 = -0.5f;
	private final static float m20 = -0.5f;
	private final static float m21 = 0.0f;
	private final static float m22 = 0.5f;
	private final static float m23 = 0.0f;
	private final static float m30 = 0.0f;
	private final static float m31 = 1.0f;
	private final static float m32 = 0.0f;
	private final static float m33 = 0.0f;
	private static final float EPSILON = 0.00001f;

	public static float catmull(float x, float[] knots) {
		int n = knots.length;
		int span;
		int nSpans = n - 3;
		float k0, k1, k2, k3;
		float c0, c1, c2, c3;

		x = x < 0f ? 0f : x >= 1f ? nSpans : x * nSpans;
		span = (int) x;
		if (span > n - 4)
			span = n - 4;
		x -= span;

		k0 = knots[span];
		k1 = knots[span + 1];
		k2 = knots[span + 2];
		k3 = knots[span + 3];

		c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
		c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
		c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
		c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;

		return ((c3 * x + c2) * x + c1) * x + c0;
	}

	/**
	 * Tests if the curve has the minimum of required knots
	 *
	 * @return - true if the curve has the minimum of required knots
	 */
	public boolean isEmpty() {
		return type != TYPE_GAMMA && type != TYPE_S
				&& (numKnots == 0 || numKnots < 2 && type == TYPE_CATMULL_ROM);
	}

	/**
	 * Tests if the curve satisfies x == y for all (x,y)
	 *
	 * @return true if the curve satisfies x == y for all (x,y)
	 */
	public boolean isIdentity() {
		switch (type) {
		case TYPE_GAMMA:
			return gamma == 1f;
		case TYPE_S:
			return false;
		default:
			for (int i = 0; i < numKnots; i++)
				if (Math.abs(x[i] - y[i]) > EPSILON)
					return false;
			return true;
		}
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	public IMask getMask() {
		return mask;
	}

	/**
	 * Sets a mask object. Currently only supported for the Luminance channel of
	 * HSL images
	 *
	 * @param mask
	 *            - mask object
	 */
	public void setMask(IMask mask) {
		this.mask = mask;
	}

	/**
	 * @param gamma
	 *            the gamma to set
	 */
	public void setGamma(float gamma) {
		this.gamma = gamma;
	}

	/**
	 * Creates an identity table
	 *
	 * @param range
	 *            - value range for X and Y parameters
	 * @return identity table
	 */
	public short[] makeIdCurve(int range) {
		short[] result = new short[range + 1];
		if (type == TYPE_MINMAX) {
			short neut = (short) ((range + 1) / 2);
			for (int i = 0; i <= range; i++)
				result[i] = neut;
		} else
			for (short i = 0; i <= range; i++)
				result[i] = i;
		return result;
	}

	/**
	 * Folds two tables
	 *
	 * @param table1
	 *            - table 1
	 * @param table2
	 *            - table 2
	 * @return resulting table
	 */
	public short[] fold(short[] table1, short[] table2) {
		int l = table1.length;
		short[] nc = new short[l];
		if (type == TYPE_MINMAX) {
			int neut = l / 2;
			for (int j = 0; j < l; j++)
				nc[j] = (short) (table1[j] + table2[j] - neut);
		} else
			for (int j = 0; j < l; j++)
				nc[j] = table1[table2[j] & 0xffff];
		return nc;
	}

	/**
	 * @return the preserveShadow
	 */
	public float getPreserveShadow() {
		return preserveShadow;
	}

	/**
	 * @param preserveShadow the preserveShadow to set
	 */
	public void setPreserveShadow(float preserveShadow) {
		this.preserveShadow = preserveShadow;
	}
}
