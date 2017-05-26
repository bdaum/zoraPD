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
package com.bdaum.zoom.image.internal.awt;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.stream.IntStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.UnsharpMask;

public class UnsharpFilter implements BufferedImageOp {
	private static final int SHIFT = UnsharpMask.SHIFT;
	private static final int SHIFTFAC = 1 << UnsharpMask.SHIFT;
	private static final int _1F = SHIFTFAC * 255;
	private static final int ROUND = 1 << SHIFT - 1;
	private static final int MAXV = (1 << SHIFT) * 255;
	private static int nP = ImageConstants.NPROCESSORS;

	private final UnsharpMask mask;

	public UnsharpFilter(UnsharpMask mask) {
		this.mask = mask;
	}

	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int[] kernel = mask.getKernel();
		float amount = mask.amount;
		if (kernel == null || kernel.length <= 1 || amount <= 0.01f) {
			if (dest == null)
				return src;
			WritableRaster raster = dest.getRaster();
			src.copyData(raster);
			return dest;
		}
		final int width = src.getWidth();
		final int height = src.getHeight();
		int len = width * height;
		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		final int[] work1 = src.getRGB(0, 0, width, height, null, 0, width);
		final int[] work2 = new int[len];
		convolveAndTranspose(kernel, work1, work2, width, height);
		convolveAndTranspose(kernel, work2, work1, height, width);
		src.getRGB(0, 0, width, height, work2, 0, width);

		final short[] toneTable = mask.toneMask == null ? null : mask.toneMask.makeTable(255);
		final int a = (int) (SHIFTFAC * 4 * amount);
		final int threshold = (int) (mask.threshold * 255);
		if (nP == 1)
			filter(0, height, width, work1, work2, toneTable, a, threshold);
		else
			IntStream.range(0, nP).parallel().forEach(
					p -> filter(height * p / nP, height * (p + 1) / nP, width, work1, work2, toneTable, a, threshold));
		dest.setRGB(0, 0, width, height, work1, 0, width);
		return dest;
	}

	private static void filter(int from, int to, int width, int[] work1, int[] work2, short[] toneTable, int a,
			int threshold) {
		int index = from * width;
		if (toneTable != null) {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int rgb = work2[index];
						int r1 = (rgb >> 16) & 0xff;
						int g1 = (rgb >> 8) & 0xff;
						int b1 = rgb & 0xff;
						int fac = toneTable[(r1 + g1 + b1) / 3];
						rgb = work1[index];
						int r2 = (rgb >> 16) & 0xff;
						int g2 = (rgb >> 8) & 0xff;
						int b2 = rgb & 0xff;
						int rdiff = r1 - r2;
						int gdiff = g1 - g2;
						int bdiff = b1 - b2;
						if (threshold > 0) {
							if (rdiff > 0 && rdiff >= threshold || rdiff < 0 && -rdiff >= threshold) {
								r1 += a * fac / 255 * rdiff / _1F;
								r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							}
							if (gdiff > 0 && gdiff >= threshold || gdiff < 0 && -gdiff >= threshold) {
								g1 += a * fac / 255 * gdiff / _1F;
								g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							}
							if (bdiff > 0 && bdiff >= threshold || bdiff < 0 && -bdiff >= threshold) {
								b1 += a * fac / 255 * bdiff / _1F;
								b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
							}
							work1[index] = r1 << 16 | g1 << 8 | b1;
						} else {
							if (rdiff + gdiff + bdiff < 0) {
								r1 += b * fac / 255 * rdiff / _1F;
								g1 += b * fac / 255 * gdiff / _1F;
								b1 += b * fac / 255 * bdiff / _1F;
							} else {
								r1 += a * fac / 255 * rdiff / _1F;
								g1 += a * fac / 255 * gdiff / _1F;
								b1 += a * fac / 255 * bdiff / _1F;
							}
							r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						}
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int rgb = work2[index];
						int r1 = (rgb >> 16) & 0xff;
						int g1 = (rgb >> 8) & 0xff;
						int b1 = rgb & 0xff;
						int fac = toneTable[(r1 + g1 + b1) / 3];
						rgb = work1[index];
						r1 += a * fac / 255 * (r1 - ((rgb >> 16) & 0xff)) / _1F;
						g1 += a * fac / 255 * (g1 - ((rgb >> 8) & 0xff)) / _1F;
						b1 += a * fac / 255 * (b1 - (rgb & 0xff)) / _1F;
						work1[index] = (r1 < 0 ? 0 : r1 > 255 ? 255 : r1) << 16
								| (g1 < 0 ? 0 : g1 > 255 ? 255 : g1) << 8 | (b1 < 0 ? 0 : b1 > 255 ? 255 : b1);
					}
				}
		} else {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int rgb = work2[index];
						int r1 = (rgb >> 16) & 0xff;
						int g1 = (rgb >> 8) & 0xff;
						int b1 = rgb & 0xff;
						rgb = work1[index];
						int r2 = (rgb >> 16) & 0xff;
						int g2 = (rgb >> 8) & 0xff;
						int b2 = rgb & 0xff;
						int rdiff = r1 - r2;
						int gdiff = g1 - g2;
						int bdiff = b1 - b2;
						if (threshold > 0) {
							if (rdiff > 0 && rdiff >= threshold || rdiff < 0 && -rdiff >= threshold) {
								r1 += a * rdiff / _1F;
								r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							}
							if (gdiff > 0 && gdiff >= threshold || gdiff < 0 && -gdiff >= threshold) {
								g1 += a * gdiff / _1F;
								g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							}
							if (bdiff > 0 && bdiff >= threshold || bdiff < 0 && -bdiff >= threshold) {
								b1 += a * bdiff / _1F;
								b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
							}
						} else {
							if (rdiff + gdiff + bdiff < 0) {
								r1 += b * rdiff / _1F;
								g1 += b * gdiff / _1F;
								b1 += b * bdiff / _1F;
							} else {
								r1 += a * rdiff / _1F;
								g1 += a * gdiff / _1F;
								b1 += a * bdiff / _1F;
							}
							r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						}
						work1[index] = r1 << 16 | g1 << 8 | b1;
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int rgb = work2[index];
						int r1 = (rgb >> 16) & 0xff;
						int g1 = (rgb >> 8) & 0xff;
						int b1 = rgb & 0xff;
						rgb = work1[index];
						r1 += a * (r1 - ((rgb >> 16) & 0xff)) / _1F;
						g1 += a * (g1 - ((rgb >> 8) & 0xff)) / _1F;
						b1 += a * (b1 - (rgb & 0xff)) / _1F;
						work1[index] = (r1 < 0 ? 0 : r1 > 255 ? 255 : r1) << 16
								| (g1 < 0 ? 0 : g1 > 255 ? 255 : g1) << 8 | (b1 < 0 ? 0 : b1 > 255 ? 255 : b1);
					}
				}
		}
	}

	public void filter(final ImageData src) {
		int[] kernel = mask.getKernel();
		float amount = mask.amount;
		if (kernel == null || kernel.length == 0 || amount <= 0.01f || mask.radius < 0.1f)
			return;
		int width = src.width;
		int height = src.height;
		int len = width * height;
		final PaletteData palette = src.palette;
		if (palette.isDirect) {
			final int[] work1 = new int[len];
			src.getPixels(0, 0, len, work1, 0);
			final int[] work2 = new int[len];
			convolveAndTranspose(kernel, work1, work2, width, height, palette);
			convolveAndTranspose(kernel, work2, work1, height, width, palette);
			src.getPixels(0, 0, len, work2, 0);
			final short[] toneTable = mask.toneMask == null ? null : mask.toneMask.makeTable(255);
			final int a = (int) (SHIFTFAC * 4 * amount);
			final int threshold = (int) (mask.threshold * 255);
			if (nP == 1)
				filter(0, height, src, palette, work1, work2, toneTable, a, threshold);
			else
				IntStream.range(0, nP).parallel().forEach(p -> filter(height * p / nP, height * (p + 1) / nP, src,
						palette, work1, work2, toneTable, a, threshold));
		}
	}

	private static void filter(int from, int to, ImageData src, PaletteData palette, int[] work1, int[] work2,
			short[] toneTable, int a, int threshold) {
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int width = src.width;
		int height = src.height;
		int len = width * height;
		int index = from * width;
		if (toneTable != null) {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int pixel = work2[index];
						int r1 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g1 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b1 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						int fac = toneTable[(r1 + g1 + b1) / 3];
						pixel = work1[index];
						int r2 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g2 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b2 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						int rdiff = r1 - r2;
						int gdiff = g1 - g2;
						int bdiff = b1 - b2;
						if (threshold > 0) {
							if (rdiff > 0 && rdiff >= threshold || rdiff < 0 && -rdiff >= threshold) {
								r1 += a * fac / 255 * rdiff / _1F;
								r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							}
							if (gdiff > 0 && gdiff >= threshold || gdiff < 0 && -gdiff >= threshold) {
								g1 += a * fac / 255 * gdiff / _1F;
								g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							}
							if (bdiff > 0 && bdiff >= threshold || bdiff < 0 && -bdiff >= threshold) {
								b1 += a * fac / 255 * bdiff / _1F;
								b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
							}
						} else {
							if (rdiff + gdiff + bdiff < 0) {
								r1 += b * fac / 255 * rdiff / _1F;
								g1 += b * fac / 255 * gdiff / _1F;
								b1 += b * fac / 255 * bdiff / _1F;
							} else {
								r1 += a * fac / 255 * rdiff / _1F;
								g1 += a * fac / 255 * gdiff / _1F;
								b1 += a * fac / 255 * bdiff / _1F;
							}
							r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						}
						pixel = (redShift < 0 ? r1 << -redShift : r1 >>> redShift) & redMask;
						pixel |= (greenShift < 0 ? g1 << -greenShift : g1 >>> greenShift) & greenMask;
						pixel |= (blueShift < 0 ? b1 << -blueShift : b1 >>> blueShift) & blueMask;
						work1[index] = pixel;
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int pixel = work2[index];
						int r1 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g1 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b1 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						int fac = toneTable[(r1 + g1 + b1) / 3];
						pixel = work1[index];
						r1 += a * fac / 255 * (r1
								- (redShift < 0 ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift))
								/ _1F;
						g1 += a * fac / 255 * (g1 - (greenShift < 0 ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift)) / _1F;
						b1 += a * fac / 255 * (b1
								- (blueShift < 0 ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift))
								/ _1F;
						r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
						g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
						b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						pixel = (redShift < 0 ? r1 << -redShift : r1 >>> redShift) & redMask;
						pixel |= (greenShift < 0 ? g1 << -greenShift : g1 >>> greenShift) & greenMask;
						pixel |= (blueShift < 0 ? b1 << -blueShift : b1 >>> blueShift) & blueMask;
						work1[index] = pixel;
					}
				}
		} else {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int pixel = work2[index];
						int r1 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g1 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b1 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						pixel = work1[index];
						int r2 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g2 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b2 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						int rdiff = r1 - r2;
						int gdiff = g1 - g2;
						int bdiff = b1 - b2;
						if (threshold > 0) {
							if (rdiff > 0 && rdiff >= threshold || rdiff < 0 && -rdiff >= threshold) {
								r1 += a * rdiff / _1F;
								r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							}
							if (gdiff > 0 && gdiff >= threshold || gdiff < 0 && -gdiff >= threshold) {
								g1 += a * gdiff / _1F;
								g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							}
							if (bdiff > 0 && bdiff >= threshold || bdiff < 0 && -bdiff >= threshold) {
								b1 += a * bdiff / _1F;
								b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
							}
						} else {
							if (rdiff + gdiff + bdiff < 0) {
								r1 += b * rdiff / _1F;
								g1 += b * gdiff / _1F;
								b1 += b * bdiff / _1F;
							} else {
								r1 += a * rdiff / _1F;
								g1 += a * gdiff / _1F;
								b1 += a * bdiff / _1F;
							}
							r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
							g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
							b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						}
						pixel = (redShift < 0 ? r1 << -redShift : r1 >>> redShift) & redMask;
						pixel |= (greenShift < 0 ? g1 << -greenShift : g1 >>> greenShift) & greenMask;
						pixel |= (blueShift < 0 ? b1 << -blueShift : b1 >>> blueShift) & blueMask;
						work1[index] = pixel;
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						int pixel = work2[index];
						int r1 = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
						int g1 = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift);
						int b1 = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
								: (pixel & blueMask) << blueShift);
						pixel = work1[index];
						r1 += a * (r1
								- (redShift < 0 ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift))
								/ _1F;
						g1 += a * (g1 - (greenShift < 0 ? (pixel & greenMask) >>> -greenShift
								: (pixel & greenMask) << greenShift)) / _1F;
						b1 += a * (b1
								- (blueShift < 0 ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift))
								/ _1F;
						r1 = r1 < 0 ? 0 : r1 > 255 ? 255 : r1;
						g1 = g1 < 0 ? 0 : g1 > 255 ? 255 : g1;
						b1 = b1 < 0 ? 0 : b1 > 255 ? 255 : b1;
						pixel = (redShift < 0 ? r1 << -redShift : r1 >>> redShift) & redMask;
						pixel |= (greenShift < 0 ? g1 << -greenShift : g1 >>> greenShift) & greenMask;
						pixel |= (blueShift < 0 ? b1 << -blueShift : b1 >>> blueShift) & blueMask;
						work1[index] = pixel;
					}
				}
		}
		src.setPixels(0, 0, len, work1, 0);
	}

	public static void convolveAndTranspose(final int[] kernel, final int[] in, final int[] out, final int width,
			final int height) {
		if (nP == 1)
			convolveAndTranspose(0, height, kernel, in, out, width, height);
		else
			IntStream.range(0, nP).parallel().forEach(
					p -> convolveAndTranspose(height * p / nP, height * (p + 1) / nP, kernel, in, out, width, height));
	}

	public static void convolveAndTranspose(int from, int to, int[] kernel, int[] in, int[] out, int width,
			int height) {
		int cols = kernel.length;
		int cols2 = cols / 2;
		int w1 = width - 1;
		int index, yoff, r, g, b, ix;
		for (int y = from; y < to; y++) {
			index = y;
			yoff = y * width;
			for (int x = 0; x < width; x++) {
				r = ROUND;
				g = ROUND;
				b = ROUND;
				for (int col = -cols2; col <= cols2; col++) {
					int f = kernel[cols2 + col];
					ix = x + col;
					int rgb = in[yoff + (ix < 0 ? 0 : ix > w1 ? w1 : ix)];
					r += f * ((rgb >> 16) & 0xff);
					g += f * ((rgb >> 8) & 0xff);
					b += f * (rgb & 0xff);
				}
				out[index] = (r < 0 ? 0 : r > MAXV ? 255 : r >>> SHIFT) << 16
						| (g < 0 ? 0 : g > MAXV ? 255 : g >>> SHIFT) << 8 | (b < 0 ? 0 : b > MAXV ? 255 : b >>> SHIFT);
				index += height;
			}
		}
	}

	public static void convolveAndTranspose(int[] kernel, int[] in, int[] out, int width, int height,
			PaletteData palette) {
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int cols = kernel.length;
		int cols2 = cols / 2;
		int w1 = width - 1;
		for (int y = 0; y < height; y++) {
			int index = y;
			int yoff = y * width;
			for (int x = 0; x < width; x++) {
				int r = 0;
				int g = 0;
				int b = 0;
				for (int col = -cols2; col <= cols2; col++) {
					int f = kernel[cols2 + col];
					int ix = x + col;
					int pixel = in[yoff + (ix < 0 ? 0 : ix > w1 ? w1 : ix)];
					r += f * ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
					g += f * ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
							: (pixel & greenMask) << greenShift);
					b += f * ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
				}
				r = r < 0 ? 0 : r > 255 ? 255 : r;
				g = g < 0 ? 0 : g > 255 ? 255 : g;
				b = b < 0 ? 0 : b > 255 ? 255 : b;
				int pixel = (redShift < 0 ? r << -redShift : r >>> redShift) & redMask;
				pixel |= (greenShift < 0 ? g << -greenShift : g >>> greenShift) & greenMask;
				pixel |= (blueShift < 0 ? b << -blueShift : b >>> blueShift) & blueMask;
				out[index] = pixel;
				index += height;
			}
		}
	}

	public Rectangle2D getBounds2D(BufferedImage img) {
		return new Rectangle(0, 0, img.getWidth(), img.getHeight());
	}

	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
		if (dstPt == null)
			dstPt = new Point2D.Double();
		dstPt.setLocation(srcPt.getX(), srcPt.getY());
		return dstPt;
	}

	public RenderingHints getRenderingHints() {
		return null;
	}

	public BufferedImage createCompatibleDestImage(BufferedImage img, ColorModel cm) {
		if (cm == null)
			cm = img.getColorModel();
		return new BufferedImage(cm, cm.createCompatibleWritableRaster(img.getWidth(), img.getHeight()),
				cm.isAlphaPremultiplied(), null);
	}

}
