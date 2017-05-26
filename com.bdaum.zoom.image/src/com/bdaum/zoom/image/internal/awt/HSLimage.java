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
package com.bdaum.zoom.image.internal.awt;

import java.awt.image.BufferedImage;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.util.stream.IntStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.ColorShift;
import com.bdaum.zoom.image.recipe.ColorShift.Sector;
import com.bdaum.zoom.image.recipe.IMask;
import com.bdaum.zoom.image.recipe.SplitTone;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;

public class HSLimage {

	private static int nP = ImageConstants.NPROCESSORS;
	private static final int _1F = 255 << 4;
	private static final int _1F8 = 255 << 8;
	private static final int _05F = _1F >> 1;
	private static final int _2F = _1F * 2;
	private static final int _3F = _1F * 3;
	private static final int _4F = _1F * 4;
	private static final int _6F = _1F * 6;
	public int width;
	public int height;
	private int len;
	public byte[] h;
	public byte[] s;
	public byte[] l;
	private boolean useHSV;

	public HSLimage(int width, int height, boolean useHSV) {
		this.width = width;
		this.height = height;
		this.useHSV = useHSV;
		this.len = width * height;
		h = new byte[len];
		s = new byte[len];
		l = new byte[len];
	}

	/**
	 * @param image
	 *            - input image
	 * @param lt
	 *            - lookup table for gray and color translation (offset not
	 *            supported)
	 * @param useHSV
	 *            - true if HSV color space is used instead of HSL
	 */
	public HSLimage(BufferedImage image, ShortLookupTable lt, final boolean useHSV) {
		this(image.getWidth(), image.getHeight(), useHSV);
		bufferedToHSL(image, lt, useHSV);
	}

	protected void bufferedToHSL(BufferedImage image, ShortLookupTable lt, final boolean useHSV) {
		int nc = 0;
		if (lt != null) {
			if (lt.getOffset() != 0)
				throw new UnsupportedOperationException();
			nc = lt.getNumComponents();
		}
		final short[] ltred = (lt == null) ? null : lt.getTable()[0];
		final short[] ltgreen = (lt == null) ? null : (nc == 1) ? ltred : lt.getTable()[1];
		final short[] ltblue = (lt == null) ? null : (nc == 1) ? ltred : lt.getTable()[2];
		final WritableRaster src = image.getRaster();
		final short[] ltgray = (src.getNumBands() < 3 && ltred != null && ltgreen != null && ltblue != null)
				? new short[ltred.length] : null;
		if (ltgray != null)
			for (int i = 0; i < ltgray.length; i++)
				ltgray[i] = (short) (((ltred[i] & 0xffff) + (ltgreen[i] & 0xffff) + (ltblue[i] & 0xffff)) / 3);
		if (nP == 1)
			fromBufferedImage(0, height, useHSV, ltred, ltgreen, ltblue, ltgray, src);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> fromBufferedImage(height * p / nP, height * (p + 1) / nP, useHSV, ltred, ltgreen, ltblue, ltgray, src));
	}

	private void fromBufferedImage(int from, int to, boolean hsv, short[] ltred, short[] ltgreen, short[] ltblue,
			short[] ltgray, WritableRaster src) {
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		int bands = src.getNumBands();
		int[] pixel = new int[bands * width];
		int k = from * width;
		switch (bands) {
		case 1:
			for (int y = from; y < to; y++) {
				src.getPixels(0, y, width, 1, pixel);
				for (int x = 0, xs = 0; x < width; x++, k++)
					_l[k] = (byte) ((ltgray == null ? pixel[xs++] : ltgray[pixel[xs++]] & 0xff) << 4);
			}
			return;
		case 2:
			for (int y = from; y < to; y++) {
				src.getPixels(0, y, width, 1, pixel);
				for (int x = 0, xs = 0; x < width; x++, k++)
					_l[k] = (byte) (ltgray == null ? (pixel[xs++] + pixel[xs++] << 5)
							: (ltgray[pixel[xs++] + pixel[xs++] >> 2] & 0xff) << 4);
			}
			return;
		default:
			int inc = bands - 2;
			int red, green, blue, max, min, hue, sat, lum2, diff;
			if (hsv)
				for (int y = from; y < to; y++) {
					src.getPixels(0, y, width, 1, pixel);
					for (int x = 0, xs = 0; x < width; x++, xs += inc, k++) {
						red = (ltred == null ? pixel[xs] : ltred[pixel[xs]]) << 4;
						green = (ltgreen == null ? pixel[++xs] : ltgreen[pixel[++xs]]) << 4;
						blue = (ltblue == null ? pixel[++xs] : ltblue[pixel[++xs]]) << 4;
						max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
						min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
						if (max != min) {
							diff = max - min;
							sat = diff * _1F / max;
							if (max == red) {
								hue = (green - blue) * _1F / diff;
								if (hue < 0)
									hue += _6F;
							} else if (max == green)
								hue = _2F + (blue - red) * _1F / diff;
							else
								hue = _4F + (red - green) * _1F / diff;
							hue /= 6;
						} else
							hue = sat = 0;
						_h[k] = (byte) (hue + 8 >>> 4);
						_s[k] = (byte) (sat + 8 >>> 4);
						_l[k] = (byte) (max + 8 >>> 4);
					}
				}
			else
				for (int y = from; y < to; y++) {
					src.getPixels(0, y, width, 1, pixel);
					for (int x = 0, xs = 0; x < width; x++, xs += inc, k++) {
						red = (ltred == null ? pixel[xs] : ltred[pixel[xs]]) << 4;
						green = (ltgreen == null ? pixel[++xs] : ltgreen[pixel[++xs]]) << 4;
						blue = (ltblue == null ? pixel[++xs] : ltblue[pixel[++xs]]) << 4;

						max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
						min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
						lum2 = max + min;
						if (max != min && lum2 != 0) {
							diff = max - min;
							sat = lum2 > _1F ? diff * _1F / (_2F - lum2) : diff * _1F / lum2;
							if (max == red) {
								hue = (green - blue) * _1F / diff;
								if (hue < 0)
									hue += _6F;
							} else if (max == green)
								hue = (blue - red) * _1F / diff + _2F;
							else
								hue = (red - green) * _1F / diff + _4F;
							hue /= 6;
						} else
							hue = sat = 0;
						_h[k] = (byte) (hue + 8 >>> 4);
						_s[k] = (byte) (sat + 8 >>> 4);
						_l[k] = (byte) (lum2 + 16 >>> 5);
					}
				}
		}
	}

	public HSLimage(final ImageData swtImageData, ShortLookupTable lt, final boolean useHSV) {
		this(swtImageData.width, swtImageData.height, useHSV);
		int nc = 0;
		if (lt != null) {
			if (lt.getOffset() != 0)
				throw new UnsupportedOperationException();
			nc = lt.getNumComponents();
		}
		final short[] ltred = (lt == null) ? null : lt.getTable()[0];
		final short[] ltgreen = (lt == null) ? null : (nc == 1) ? ltred : lt.getTable()[1];
		final short[] ltblue = (lt == null) ? null : (nc == 1) ? ltred : lt.getTable()[2];
		final PaletteData palette = swtImageData.palette;
		if (palette.isDirect) {
			if (nP == 1)
				fromDirectImageData(0, height, swtImageData, useHSV, ltred, ltgreen, ltblue, palette);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> fromDirectImageData(height * p / nP, height * (p + 1) / nP, swtImageData, useHSV, ltred, ltgreen, ltblue, palette));
		} else {
			int pixel, red, green, blue, max, min, hue, sat, lum2, diff;
			RGB[] rgBs = palette.getRGBs();
			int[] scanLine = new int[width];
			byte[] hues = new byte[rgBs.length];
			byte[] sats = new byte[rgBs.length];
			byte[] lums = new byte[rgBs.length];
			if (useHSV)
				for (int i = 0; i < rgBs.length; i++) {
					RGB rgb = rgBs[i];
					red = (ltred != null ? ltred[rgb.red] : rgb.red) << 4;
					green = (ltgreen != null ? ltgreen[rgb.green] : rgb.green) << 4;
					blue = (ltblue != null ? ltblue[rgb.blue] : rgb.blue) << 4;
					max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
					min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
					if (max != min) {
						diff = max - min;
						sat = diff << 4 / max;
						int d2 = diff * 48; // (16 * 6 / 2)
						int d_R = ((max - red << 4) + d2) / diff;
						int d_G = ((max - green << 4) + d2) / diff;
						int d_B = ((max - blue << 4) + d2) / diff;
						if (max == red)
							hue = d_B - d_G;
						else if (max == green)
							hue = _2F + d_R - d_B;
						else
							hue = _4F + d_G - d_R;
						hue /= 6;
						if (hue < 0)
							hue += 1F;
						if (hue > 1)
							hue -= 1F;
					} else
						hue = sat = 0;
					hues[i] = (byte) (hue + 8 >>> 4);
					sats[i] = (byte) (sat + 8 >>> 4);
					lums[i] = (byte) (max + 8 >>> 4);
				}
			else
				for (int i = 0; i < rgBs.length; i++) {
					RGB rgb = rgBs[i];
					red = (ltred != null ? ltred[rgb.red] : rgb.red) << 4;
					green = (ltgreen != null ? ltgreen[rgb.green] : rgb.green) << 4;
					blue = (ltblue != null ? ltblue[rgb.blue] : rgb.blue) << 4;
					max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
					min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
					lum2 = max + min;
					if (max != min) {
						diff = max - min;
						sat = lum2 > _1F ? diff * _1F / (_2F - lum2) : diff * _1F / lum2;
						if (max == red)
							hue = (green - blue) * _1F / diff + (green < blue ? _6F : 0);
						else if (max == green)
							hue = (blue - red) * _1F / diff + _2F;
						else
							hue = (red - green) * _1F / diff + _4F;
						hue /= 6;
					} else
						hue = sat = 0;
					hues[i] = (byte) (hue + 8 >>> 4);
					sats[i] = (byte) (sat + 8 >>> 4);
					lums[i] = (byte) (lum2 + 16 >>> 5);
				}
			byte[] _h = h;
			byte[] _s = s;
			byte[] _l = l;
			for (int y = 0, k = 0; y < height; y++) {
				swtImageData.getPixels(0, y, width, scanLine, 0);
				for (int x = 0; x < width; x++, k++) {
					pixel = scanLine[x];
					_h[k] = hues[pixel];
					_s[k] = sats[pixel];
					_l[k] = lums[pixel];
				}
			}
		}
	}

	private void fromDirectImageData(int from, int to, ImageData swtImageData, boolean hsv, short[] ltred,
			short[] ltgreen, short[] ltblue, PaletteData palette) {
		int pixel;
		int red;
		int green;
		int blue;
		int max;
		int min;
		int hue;
		int sat;
		int lum2;
		int diff;

		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int[] scanLine = new int[width];
		int k = from * width;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		if (hsv)
			for (int y = from; y < to; y++) {
				swtImageData.getPixels(0, y, width, scanLine, 0);
				for (int x = 0; x < width; x++, k++) {
					pixel = scanLine[x];
					red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
					green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
							: (pixel & greenMask) << greenShift);
					blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
					red = (ltred != null ? ltred[red] : red) << 4;
					green = (ltgreen != null ? ltgreen[green] : green) << 4;
					blue = (ltblue != null ? ltblue[blue] : blue) << 4;
					max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
					min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
					if (max != min) {
						diff = max - min;
						sat = diff * _1F / max;
						if (max == red) {
							hue = (green - blue) * _1F / diff;
							if (hue < 0)
								hue += _6F;
						} else if (max == green)
							hue = _2F + (blue - red) * _1F / diff;
						else
							hue = _4F + (red - green) * _1F / diff;
						hue /= 6;
					} else
						hue = sat = 0;
					_h[k] = (byte) (hue + 8 >>> 4);
					_s[k] = (byte) (sat + 8 >>> 4);
					_l[k] = (byte) (max + 8 >>> 4);
				}
			}
		else
			for (int y = from; y < to; y++) {
				swtImageData.getPixels(0, y, width, scanLine, 0);
				for (int x = 0; x < width; x++, k++) {
					pixel = scanLine[x];
					red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
					green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
							: (pixel & greenMask) << greenShift);
					blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
					red = (ltred != null ? ltred[red] : red) << 4;
					green = (ltgreen != null ? ltgreen[green] : green) << 4;
					blue = (ltblue != null ? ltblue[blue] : blue) << 4;
					max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
					min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
					lum2 = max + min;
					if (max != min && lum2 != 0) {
						diff = max - min;
						sat = lum2 > _1F ? diff * _1F / (_2F - lum2) : diff * _1F / lum2;
						if (max == red) {
							hue = (green - blue) * _1F / diff;
							if (hue < 0)
								hue += _6F;
						} else if (max == green)
							hue = (blue - red) * _1F / diff + _2F;
						else
							hue = (red - green) * _1F / diff + _4F;
						hue /= 6;
					} else
						hue = sat = 0;
					_h[k] = (byte) ((hue + 8) >>> 4);
					_s[k] = (byte) ((sat + 8) >>> 4);
					_l[k] = (byte) ((lum2 + 16) >>> 5);
				}
			}
	}

	public BufferedImage toBufferedImage() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final WritableRaster raster = image.getRaster();
		int rh = raster.getHeight();
		int mh = rh < height ? rh : height;
		if (nP == 1)
			toBufferedImage(0, mh, raster);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> toBufferedImage(height * p / nP, height * (p + 1) / nP, raster));
		return image;
	}

	private void toBufferedImage(int from, int to, WritableRaster raster) {
		int[] pixel = new int[3 * width];
		int hue, sat, lum, red, green, blue, p, q, d, t, t1, t3;
		int k = from * width;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		if (useHSV)
			for (int y = from; y < to; y++) {
				for (int x = 0, xs = 0; x < width; x++, k++) {
					sat = (_s[k] & 0xff) << 4;
					// To RGB
					if (sat == 0)
						pixel[xs++] = pixel[xs++] = pixel[xs++] = _l[k] & 0xff;
					else {
						hue = (_h[k] & 0xff) * 96; // 16 * 6;
						int v = _l[k] & 0xff;
						lum = v << 4;
						int sector = hue / _1F;
						int res = hue - sector * _1F;
						int v_1 = lum * (_1F - sat) / _1F;
						int v_2 = lum * (_1F - sat * res / _1F) / _1F;
						int v_3 = lum * (_1F - sat * (_1F - res) / _1F) / _1F;
						switch (sector) {
						case 0:
							pixel[xs++] = v;
							pixel[xs++] = (v_3 + 8) >> 4;
							pixel[xs++] = (v_1 + 8) >> 4;
							break;
						case 1:
							pixel[xs++] = (v_2 + 8) >> 4;
							pixel[xs++] = v;
							pixel[xs++] = (v_1 + 8) >> 4;
							break;
						case 2:
							pixel[xs++] = (v_1 + 8) >> 4;
							pixel[xs++] = v;
							pixel[xs++] = (v_3 + 8) >> 4;
							break;
						case 3:
							pixel[xs++] = (v_1 + 8) >> 4;
							pixel[xs++] = (v_2 + 8) >> 4;
							pixel[xs++] = v;
							break;
						case 4:
							pixel[xs++] = v;
							pixel[xs++] = (v_1 + 8) >> 4;
							pixel[xs++] = (v_2 + 8) >> 4;
							break;
						default:
							pixel[xs++] = (v_3 + 8) >> 4;
							pixel[xs++] = (v_1 + 8) >> 4;
							pixel[xs++] = v;
							break;
						}
					}
				}
				raster.setPixels(0, y, width, 1, pixel);
			}
		else
			for (int y = from; y < to; y++) {
				for (int x = 0, xs = 0; x < width; x++, k++) {
					// To RGB
					sat = (_s[k] & 0xff) << 4;
					if (sat == 0)
						pixel[xs++] = pixel[xs++] = pixel[xs++] = _l[k] & 0xff;
					else {
						hue = (_h[k] & 0xff) << 4;
						lum = (_l[k] & 0xff) << 4;
						q = lum < _05F ? (lum * (_1F + sat)) / _1F : lum * (_1F - sat) / _1F + sat;
						red = green = blue = p = lum + lum - q;
						d = q - p;
						t = hue * 6;
						t1 = (t >= _4F) ? t - _4F : t + _2F;
						if (t1 < _3F)
							red = (t1 < _1F) ? p + d * t1 / _1F : q;
						else if (t1 < _4F)
							red += d * (_4F - t1) / _1F;
						if (t < _3F)
							green = (t < _1F) ? p + d * t / _1F : q;
						else if (t < _4F)
							green += d * (_4F - t) / _1F;
						t3 = (t < _2F) ? t + _4F : t - _2F;
						if (t3 < _3F)
							blue = (t3 < _1F) ? p + d * t3 / _1F : q;
						else if (t3 < _4F)
							blue += d * (_4F - t3) / _1F;
						pixel[xs++] = (red + 8) >> 4;
						pixel[xs++] = (green + 8) >> 4;
						pixel[xs++] = (blue + 8) >> 4;
					}
				}
				raster.setPixels(0, y, width, 1, pixel);
			}
	}

	public ImageData toSwtData() {
		PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
		final ImageData data = new ImageData(width, height, 24, palette);
		if (nP == 1)
			toSwtData(0, height, data);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> toSwtData(height * p / nP, height * (p + 1) / nP, data));
		return data;
	}

	private void toSwtData(int from, int to, ImageData data) {
		int hue, sat, lum, red, green, blue, p, q, d, t, t1, t3;
		int[] scanLine = new int[width];
		int k = from * width;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;

		if (useHSV)
			for (int y = from; y < to; y++) {
				for (int x = 0; x < width; x++, k++) {
					// To RGB
					sat = (_s[k] & 0xff) << 4;
					if (sat == 0) {
						int lu = _l[k] & 0xff;
						scanLine[x] = lu << 16 | lu << 8 | lu;
					} else {
						hue = (_h[k] & 0xff) * 96; // 16 * 6;
						int v = _l[k] & 0xff;
						lum = v << 4;
						int sector = hue / _1F;
						int res = hue - sector * _1F;
						int v_1 = lum * (_1F - sat) / _1F;
						int v_2 = lum * (_1F - sat * res / _1F) / _1F;
						int v_3 = lum * (_1F - sat * (_1F - res) / _1F) / _1F;
						switch (sector) {
						case 0:
							scanLine[x] = v | v_3 + 8 >> 4 << 8 | v_1 + 8 >> 4 << 16;
							break;
						case 1:
							scanLine[x] = v_2 + 8 >> 4 | v << 8 | v_1 + 8 >> 4 << 16;
							break;
						case 2:
							scanLine[x] = v_1 + 8 >> 4 | v << 8 | v_3 + 8 >> 4 << 16;
							break;
						case 3:
							scanLine[x] = v_1 + 8 >> 4 | v_2 + 8 >> 4 << 8 | v << 16;
							break;
						case 4:
							scanLine[x] = v | v_1 + 8 >> 4 << 8 | v_2 + 8 >> 4 << 16;
							break;
						default:
							scanLine[x] = v_3 + 8 >> 4 | v_1 + 8 >> 4 << 8 | v << 16;
							break;
						}
					}
				}
				data.setPixels(0, y, width, scanLine, 0);
			}
		else
			for (int y = from; y < to; y++) {
				for (int x = 0; x < width; x++, k++) {
					sat = (_s[k] & 0xff) << 4;
					// To RGB
					if (sat == 0) {
						int lu = _l[k] & 0xff;
						scanLine[x] = lu << 16 | lu << 8 | lu;
					} else {
						hue = (_h[k] & 0xff) << 4;
						lum = (_l[k] & 0xff) << 4;
						q = lum < _05F ? (lum * (_1F + sat)) / _1F : lum * (_1F - sat) / _1F + sat;
						red = green = blue = p = lum + lum - q;
						d = q - p;
						t = hue * 6;
						t1 = (t >= _4F) ? t - _4F : t + _2F;
						if (t1 < _3F)
							red = (t1 < _1F) ? p + d * t1 / _1F : q;
						else if (t1 < _4F)
							red += d * (_4F - t1) / _1F;
						if (t < _3F)
							green = (t < _1F) ? p + d * t / _1F : q;
						else if (t < _4F)
							green += d * (_4F - t) / _1F;
						t3 = (t < _2F) ? t + _4F : t - _2F;
						if (t3 < _3F)
							blue = (t3 < _1F) ? p + d * t3 / _1F : q;
						else if (t3 < _4F)
							blue += d * (_4F - t3) / _1F;
						scanLine[x] = red + 8 >> 4 | green + 8 >> 4 << 8 | blue + 8 >> 4 << 16;
					}
				}
				data.setPixels(0, y, width, scanLine, 0);
			}
	}

	public void applyLookup(final ShortLookupTable lt) {
		if (lt.getOffset() != 0)
			throw new UnsupportedOperationException();
		if (nP == 1)
			applyLookup(0, len, lt);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyLookup(height * p / nP, height * (p + 1) / nP, lt));
	}

	private void applyLookup(int from, int to, ShortLookupTable lt) {
		byte[] _l = l;
		if (lt.getNumComponents() == 1) {
			short[] lumlt = lt.getTable()[0];
			for (int i = from; i < to; i++)
				_l[i] = (byte) lumlt[_l[i] & 0xff];
		} else {
			byte[] _h = h;
			byte[] _s = s;
			short[] huelt = lt.getTable()[0];
			short[] satlt = lt.getTable()[1];
			short[] lumlt = lt.getTable()[2];
			int range = satlt.length;
			int median = range / 2;
			for (int i = from; i < to; i++) {
				int hue = _h[i] & 0xff;
				hue += huelt[hue] * 2 - huelt.length;
				hue = (hue > 255 ? hue - 256 : hue < 0 ? hue + 256 : hue);
				_h[i] = (byte) hue;
				int satShift = satlt[hue] * 2 - range;
				int sat = _s[i] & 0xff;
				int satinv2 = ((255 - sat) * (255 - sat) + 127) / 255;
				if (satShift > 0) {
					sat = (((255 - satShift) * sat + satShift * (255 - satinv2)) + 127) / 255;
					if (sat < 0)
						sat = 0;
				} else if (satShift < 0)
					sat = ((255 + satShift) * sat + 127) / 255;
				_s[i] = (byte) sat;
				int valShift = lumlt[hue] - median;
				int val = _l[i] & 0xff;
				valShift = (valShift * (255 - (satinv2 * satinv2 + 127) / 255) + 127) / 255;
				if (valShift > 0) {
					val = (((255 - valShift) * val + valShift * (255 - ((255 - val) * (255 - val) + 127) / 255)) + 127)
							/ 255;
					if (val < 0)
						val = 0;
				} else if (valShift < 0)
					val = (val * (255 + valShift) + 127) / 255;
				_l[i] = (byte) val;
			}
		}
	}

	public void applyGrayConvert(ShortLookupTable lt) {
		if (lt.getOffset() != 0)
			throw new UnsupportedOperationException();
		final short[] lumlt = lt.getTable()[0];
		if (nP == 1)
			applyGrayConvert(0, len, lumlt);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyGrayConvert(height * p / nP, height * (p + 1) / nP, lumlt));
	}

	private void applyGrayConvert(int from, int to, short[] lumlt) {
		int lum;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		for (int i = from; i < to; i++) {
			lum = (_l[i] & 0xff) * lumlt[(_h[i] & 0xff)];
			_l[i] = lum >= _1F8 ? -1 : (byte) (lum >>> 8);
			_s[i] = _h[i] = 0;
		}
	}

	public void applyColorShift(final ColorShift cs) {
		final boolean glob = cs.glob;
		final Sector[] sectors = cs.getSectors();
		final boolean huemod[] = new boolean[ColorShift.Sector.BELLSIZE];
		if (sectors != null) {
			for (Sector sector : sectors) {
				int[] bell = sector.bell;
				for (int i = 0; i < bell.length; i++)
					huemod[i] |= bell[i] != 0;
			}
		}
		final int vibrance = (int) (cs.vibrance * _1F);
		final int globH = (int) (cs.globHShift * _1F);
		final int globH2 = Float.isNaN(cs.globHOverlay) ? Integer.MIN_VALUE : (int) (cs.globHOverlay * _1F);
		final int globS = (int) (cs.globS * _1F);
		final int globL = (int) (cs.globL * _1F);
		if (nP == 1)
			applyColorShift(0, len, glob, sectors, vibrance, globH, globH2, globS, globL, cs.satMode, cs.isPreserving,
					huemod);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyColorShift(height * p / nP, height * (p + 1) / nP, glob, sectors, vibrance, globH, globH2, globS, globL, cs.satMode,
					cs.isPreserving, huemod));
	}

	private void applyColorShift(int from, int to, boolean glob, Sector[] sectors, int vibrance, int globH, int globH2,
			int globS, int globL, int satMode, boolean isPreserving, boolean[] huemod) {
		int hind, hue, sat, lum, f;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		int _2fmglobS = _2F - globS;
		int globS1m = globS - _1F;
		for (int i = from; i < to; i++) {
			hind = _h[i] & 0xFF;
			hue = hind << 4;
			sat = (_s[i] & 0xFF) << 4;
			lum = (_l[i] & 0xFF) << 4;
			if (sat == 0 && isPreserving)
				continue;
			if (glob) {
				hue += globH;
				if (globH2 != Integer.MIN_VALUE)
					hue = (hue + globH2) / 2;
				switch (satMode) {
				case ColorShift.SAT_ADD:
					sat += globS;
					break;
				case ColorShift.SAT_CROSS:
					sat = (globS < _1F ? (sat * globS) / _1F : _1F - ((_1F - sat) * _2fmglobS) / _1F);
					break;
				case ColorShift.SAT_MULT_DEGRESSIVE:
					if (globS > _1F) {
						int satinv2 = (_1F - sat) * (_1F - sat) / _1F;
						sat = (_2fmglobS * sat + globS1m * (_1F - (satinv2 * satinv2 / _1F))) / _1F;
						if (sat < 0)
							sat = 0;
					} else if (globS < _1F)
						sat = sat * globS / _1F;
					break;
				default:
					sat = (sat * globS) / _1F;
					break;
				}
				lum = (lum * globL) / _1F;
			}
			if (vibrance != _1F) {
				// TODO skin tones
				if (vibrance > _1F)
					sat = (sat * (_1F + ((vibrance - _1F) * (_1F - sat) / _1F))) / _1F;
				else
					sat = (sat * (_1F + (((vibrance - _1F) * sat) / _1F))) / _1F;
			}
			if (huemod[hind])
				for (Sector sector : sectors) {
					f = sector.bell[hind];
					if (f != 0) {
						hue += sector.hueInt * f >> 8;
						switch (sector.satMode) {
						case ColorShift.SAT_ADD:
							sat = sat * (256 - f) + sat + sector.satInt * f >> 8;
							break;
						case ColorShift.SAT_CROSS:
							sat = sat * ((256 - f) + (globS < _1F ? sector.satInt / _1F
									: (_1F - (_1F - sat) * (_2F - sector.satInt) / _1F)) * f) >> 8;
							break;
						default:
							sat = sat * ((256 - f) + sector.satInt / _1F * f) >> 8;

							sat = sat * sector.satInt / _1F;
							break;
						}
						lum = lum * (256 - f) + lum * sector.lumInt / _1F * f >> 8;
					}
				}
			while (hue < 0)
				hue += 4096;
			while (hue >= 4096)
				hue -= 4096;
			_h[i] = (byte) (hue >>> 4);
			_s[i] = sat <= 0 ? 0 : sat >= _1F ? -1 : (byte) (sat >>> 4);
			_l[i] = lum <= 0 ? 0 : lum >= _1F ? -1 : (byte) (lum >>> 4);
		}
	}

	public void applySplitTone(SplitTone splitTone) {
		final int shadowHue = (int) (splitTone.shadowHue * _1F);
		final int shadowSaturation = (int) (splitTone.shadowSaturation * _1F);
		final int highlightHue = (int) (splitTone.highlightHue * _1F);
		final int highlightSaturation = (int) (splitTone.highlightSaturation * _1F);
		final short[] balance = splitTone.distribution.makeTable(255);
		if (nP == 1)
			applySplitTone(0, len, shadowHue, shadowSaturation, highlightHue, highlightSaturation, balance);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applySplitTone(height * p / nP, height * (p + 1) / nP, shadowHue, shadowSaturation, highlightHue, highlightSaturation, balance));
	}

	private void applySplitTone(int from, int to, int shadowHue, int shadowSaturation, int highlightHue,
			int highlightSaturation, short[] balance) {
		int hue, sat, satH, dhueH, satS, dhueS, b, dhue;
		byte[] _h = h;
		byte[] _s = s;
		byte[] _l = l;
		for (int i = from; i < to; i++) {
			hue = (_h[i] & 0xFF) << 4;
			sat = (_s[i] & 0xFF) << 4;
			satH = sat * (_1F + highlightSaturation) / _1F;
			dhueH = hue - highlightHue;
			satS = sat * (_1F + shadowSaturation) / _2F + shadowSaturation >> 1;
			dhueS = hue - shadowHue;
			b = balance == null ? _l[i] & 0xFF : balance[_l[i] & 0xFF] & 0xFFFF;
			sat = (b * satH + (255 - b) * satS) / 255;
			dhue = (b * dhueH + (255 - b) * dhueS) / 255;
			hue += dhue;
			while (hue < 0)
				hue += 4096;
			while (hue >= 4096)
				hue -= 4096;
			_h[i] = (byte) (hue >>> 4);
			_s[i] = sat < 0 ? 0 : sat > _1F ? -1 : (byte) (sat >>> 4);
		}
	}

	public void applyUnsharpMask(UnsharpMask mask) {
		int shiftfac = 1 << UnsharpMask.SHIFT;
		final int c1 = shiftfac * 255;
		float amount = mask.amount;
		final byte[] work1 = mask.createHslMaskArray(l, width, height);
		if (work1 == null)
			return;
		final short[] toneTable = mask.toneMask == null ? null : mask.toneMask.makeTable(255);
		final int a = (int) (shiftfac * 4 * amount);
		final int threshold = (int) (mask.threshold * 255);
		if (nP == 1)
			applyUnsharpMask(0, height, c1, work1, toneTable, a, threshold);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyUnsharpMask(height * p / nP, height * (p + 1) / nP, c1, work1, toneTable, a, threshold));
	}

	private void applyUnsharpMask(int from, int to, int c1, byte[] work, short[] toneTable, int a, int threshold) {
		int lum1, lum2, diff;
		byte[] _l = l;
		int index = from * width;
		if (toneTable != null) {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = _l[index] & 0xff;
						lum2 = work[index] & 0xff;
						diff = lum1 - lum2;
						if (threshold > 0) {
							if (diff > 0 && diff >= threshold || diff < 0 && -diff >= threshold) {
								lum1 += a * toneTable[lum1] / 255 * diff / c1;
								_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
							}
						} else {
							lum1 += (diff < 0 ? b : a) * toneTable[lum1] / 255 * diff / c1;
							_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
						}
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = _l[index] & 0xff;
						lum1 += a * toneTable[lum1] / 255 * (lum1 - (work[index] & 0xff)) / c1;
						_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
					}
				}
		} else {
			if (threshold > 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = _l[index] & 0xff;
						lum2 = work[index] & 0xff;
						diff = lum1 - lum2;
						if (threshold > 0) {
							if (diff > 0 && diff >= threshold || diff < 0 && -diff >= threshold) {
								lum1 += a * diff / c1;
								_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
							}
						} else {
							lum1 += (diff < 0 ? b : a) * diff / c1;
							_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
						}
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = _l[index] & 0xff;
						lum1 += a * (lum1 - (work[index] & 0xff)) / c1;
						_l[index] = lum1 < 0 ? 0 : lum1 > 255 ? -1 : (byte) lum1;
					}
				}
		}
	}

	public void applyVignette(Vignette vignette) {
		final float amount = vignette.vignetteAmount;
		final float aa = Math.abs(amount);
		float midpoint = vignette.vignetteMidpoint;
		float m = Math.min(40f, 2f / (1f - midpoint) + 0.2f);
		final int hlow = -height / 2;
		int hhigh = height + hlow;
		final int wlow = -width / 2;
		final int whigh = width + wlow;
		int dmaxSquare = hhigh * hhigh + whigh * whigh;
		final float fSquare = dmaxSquare * m * m;
		if (nP == 1)
			applyVignette(0, height, amount, aa, hlow, wlow, whigh, fSquare);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyVignette(height * p / nP, height * (p + 1) / nP,  amount, aa, hlow, wlow, whigh, fSquare));
	}

	private void applyVignette(int from, int to, float amount, float aa, int hlow, int wlow, int whigh, float fSquare) {
		int lum;
		byte[] _l = l;
		float sq, prod;
		int i = from * (whigh - wlow);
		for (int y1 = from; y1 < to; y1++) {
			int y = y1 + hlow;
			for (int x = wlow; x < whigh; x++, i++) {
				sq = 1 + aa * (x * x + y * y) / fSquare;
				prod = amount < 0 ? 1 / (sq * sq) : sq * sq;
				lum = (int) ((_l[i] & 0xff) * prod + 0.5f);
				_l[i] = lum < 0 ? 0 : lum > 255 ? -1 : (byte) lum;
			}
		}
	}

	public void toBw() {
		h = new byte[len];
		s = new byte[len];
	}

	public void applyMaskedLookup(final ShortLookupTable lt, IMask mask) {
		if (lt.getOffset() != 0)
			throw new UnsupportedOperationException();
		final byte[] array = mask.createHslMaskArray(l, width, height);
		if (array != null) {
			if (nP == 1)
				applyMaskedLookup(0, len, lt, array);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> applyMaskedLookup(height * p / nP, height * (p + 1) / nP, lt, array));
		}
	}

	private void applyMaskedLookup(int from, int to, ShortLookupTable lt, byte[] mask) {
		if (lt.getNumComponents() == 1) {
			byte[] _l = l;
			short[] lumlt = lt.getTable()[0];
			for (int i = from; i < to; i++) {
				int lum = _l[i] & 0xff;
				int lum2 = lumlt[lum];
				int m = mask[i] & 0xff;
				_l[i] = (byte) ((m * lum2 + (255 - m) * lum) / 255);
			}
		}
	}

	public void toHSL() {
		if (useHSV) {
			if (nP == 1)
				toHSL(0, len);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> toHSL(height * p / nP, height * (p + 1) / nP));
			useHSV = false;
		}
	}

	private void toHSL(int from, int to) {
		int sat, val, ll, ss;
		for (int i = from; i < to; i++) {
			sat = (s[i] & 0xFF) << 4;
			val = (l[i] & 0xFF) << 4;
			ll = ((_2F - sat) * val + _05F) / _1F;
			ss = (sat * val + _05F) / _1F;
			if (ll <= _1F) {
				if (ll > 0)
					ss = (ss * _1F / ll);
			} else if (ll < _2F)
				ss = (ss * _1F / (_2F - ll));
			s[i] = (byte) (ss >>> 4);
			l[i] = (byte) (ll >>> 5);
		}
	}

	public void toHSV() {
		if (!useHSV) {
			if (nP == 1)
				toHSL(0, len);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> toHSV(height * p / nP, height * (p + 1) / nP));
			useHSV = true;
		}
	}

	private void toHSV(int from, int to) {
		int sat, ll, ss, v;
		for (int i = from; i < to; i++) {
			sat = (s[i] & 0xFF) << 4;
			ll = (l[i] & 0xFF) << 5;
			if (ll <= _1F)
				ss = (sat * ll + _05F) / _1F;
			else
				ss = (sat * (_2F - ll) + _05F) / _1F;
			v = ll + ss;
			if (v != 0)
				ss = (ss * _2F) / v;
			s[i] = (byte) (ss >>> 4);
			l[i] = (byte) (v >>> 5);
		}
	}

}
