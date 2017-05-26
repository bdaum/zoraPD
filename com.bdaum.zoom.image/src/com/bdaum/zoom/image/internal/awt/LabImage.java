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

import java.awt.image.BufferedImage;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.util.stream.IntStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.ColorBoost;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.IMask;
import com.bdaum.zoom.image.recipe.UnsharpMask;

public class LabImage {

	public static final int WP_D50 = 0;
	public static final int WP_D55 = 1;
	public static final int WP_D65 = 2;
	public static final int WP_D75 = 3;

	private static int nP = ImageConstants.NPROCESSORS;

	private static final double[] D50 = { 96.4212, 100.0, 82.5188 };
	private static final double[] D55 = { 95.6797, 100.0, 92.1481 };
	private static final double[] D65 = { 95.0429, 100.0, 108.8900 };
	private static final double[] D75 = { 94.9722, 100.0, 122.6394 };

	private static int[] exp24ft;
	private static int[] rt24ft;
	private static int[] xyz2labft;
	private static int[] lab2xyzft;

	private static final int P15 = 1 << 15;
	private static final int P14 = 1 << 14;
	private static final int P8 = 1 << 8;
	private static final int P7 = 1 << 7;
	private static final int P6 = 1 << 6;
	private static final int P5 = 1 << 5;
	private static final int F1 = 51; // (255 / 5)

	public static final int MAXL = F1 * 100;

	public static final int MAXAB = F1 * 200;
	private static final int F2 = 5 * 16;
	private static final int F2R = F2 >> 1;
	private static final int FTOT = F1 * F2;
	private static final int FTOT_16 = FTOT * 16;
	private static final int F2V = 16;
	private static final int FTOTV = F1 * F2V;
	private static final int FTOTV_16 = FTOTV * 16;
	private static final int F3 = F1 * P6;
	private static final int LAB2XOFF = 2560;
	private static final int F4 = F1 * P15;

	public int[][] M = { { Math.round(0.4124564f * P15), Math.round(0.35757614f * P15), Math.round(0.18043754f * P15) },
			{ Math.round(0.21267294f * P15), Math.round(0.71515224f * P15), Math.round(0.07217504f * P15) },
			{ Math.round(0.01933694f * P15), Math.round(0.11919204f * P15), Math.round(0.95030414f * P15) } };

	// Original Rowsums: 0.95047 , 1.0000001, 1.088833
	// Scaling factor 32768
	// Max Rowsum Int: 35679 = (16 Bit)

	/**
	 * XYZ to sRGB conversion matrix
	 */
	public int[][] Mi = {
			{ Math.round(3.2404542f * P15), Math.round(-1.5371385f * P15), Math.round(-0.4985314f * P15) },
			{ Math.round(-0.9692660f * P15), Math.round(1.8760108f * P15), Math.round(0.0415560f * P15) },
			{ Math.round(0.0556434f * P15), Math.round(-0.2040259f * P15), Math.round(1.0572252f * P15) } };

	static {
		exp24ft = new int[256];
		int mx = exp24ft.length - 1;
		double pscale = 100 * Math.pow(1.0 / mx, 1.4);
		double poff = mx * 0.055;
		int threshold = (int) (0.04045 * mx);
		for (int x = 0; x <= threshold; x++)
			exp24ft[x] = x * 10000 / 1292;
		for (int x = threshold + 1; x <= mx; x++)
			exp24ft[x] = (int) Math.round(pscale * Math.pow(((x + poff) / 1.055), 2.4));

		int epsilon = Math.round(FTOT * 216.0f / 24389.0f);
		double kappa = 24389.0 / 27.0;
		double xfac = FTOT * FTOT;
		// Maxvalue: 1.32 * 51 * 5 * 16 = 5385.6
		// Scaling factor: 51 * 5 * 16
		xyz2labft = new int[5386];
		for (int x = 0; x <= epsilon; x++)
			xyz2labft[x] = (int) Math.round((kappa * x + FTOT_16) / 116.0);
		for (int x = epsilon + 1; x < xyz2labft.length; x++)
			xyz2labft[x] = (int) Math.round(Math.pow(x * xfac, 1.0 / 3.0));
		// Reverse
		lab2xyzft = new int[LAB2XOFF + 5552];
		epsilon = Math.round(FTOTV * 216.0f / 24389.0f);
		xfac = FTOTV * FTOTV;
		for (int x = -LAB2XOFF; x <= epsilon; x++)
			lab2xyzft[x + LAB2XOFF] = (int) Math.round((116.0 * x - FTOTV_16) / kappa);
		int u = lab2xyzft.length - LAB2XOFF;
		for (int x = epsilon + 1; x < u; x++) {
			int v = (int) Math.round((double) (x * x) * x / xfac);
			lab2xyzft[x + LAB2XOFF] = v > 88850 ? 88850 : v;
		}
		xfac = Math.pow(F3, 1.4);
		threshold = Math.round(0.0031308f * F3);
		rt24ft = new int[F3 + 1];
		double yoff = 0.055 * F3;
		// assume sRGB
		for (int x = 0; x <= threshold; x++)
			rt24ft[x] = Math.round(12.92f * x);
		for (int x = threshold + 1; x <= F3; x++)
			rt24ft[x] = (int) Math.round((1.055 * Math.pow(x * xfac, 1.0 / 2.4)) - yoff);
	}

	private short[] ltred, ltgreen, ltblue;
	public int width;
	public int height;
	private int len;
	/* L*a*b values are stored as CIE values multiplied by 51 */
	public short[] LChannel;
	public short[] aChannel;
	public short[] bChannel;
	private int[] wpi = new int[3];
	private int[] wpir = new int[3];

	public LabImage(int width, int height, int whitepoint) {
		this.width = width;
		this.height = height;
		this.len = width * height;
		LChannel = new short[len];
		aChannel = new short[len];
		bChannel = new short[len];
		int fw = 1 << 11;
		switch (whitepoint) {
		case WP_D50:
			wpi[0] = (int) Math.round(fw * D50[0]); // max 250000
			wpi[1] = (int) Math.round(fw * D50[1]);
			wpi[2] = (int) Math.round(fw * D50[2]);
			break;
		case WP_D55:
			wpi[0] = (int) Math.round(fw * D55[0]); // max 250000
			wpi[1] = (int) Math.round(fw * D55[1]);
			wpi[2] = (int) Math.round(fw * D55[2]);
			break;
		case WP_D75:
			wpi[0] = (int) Math.round(fw * D75[0]); // max 250000
			wpi[1] = (int) Math.round(fw * D75[1]);
			wpi[2] = (int) Math.round(fw * D75[2]);
			break;
		default:
			wpi[0] = (int) Math.round(fw * D65[0]); // max 250000
			wpi[1] = (int) Math.round(fw * D65[1]);
			wpi[2] = (int) Math.round(fw * D65[2]);
			break;
		}
		wpir[0] = wpi[0] >> 1;
		wpir[1] = wpi[1] >> 1;
		wpir[2] = wpi[2] >> 1;
	}

	/**
	 * @param image
	 *            - input image
	 * @param lt
	 *            - lookup table for gray and color translation (offset not
	 *            supported)
	 * @param mixer
	 *            - RGB channel mixer or null
	 * @param whitepoint
	 *            - one of WP_D50, WP_D55, WP_D65, WP_D75
	 */
	public LabImage(BufferedImage image, final ShortLookupTable lt, float[][] mixer, int whitepoint) {
		this(image.getWidth(), image.getHeight(), whitepoint);
		setupToneTables(lt);
		final WritableRaster src = image.getRaster();
		if (nP == 1)
			fromBufferedImage(0, height, lt, src);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> fromBufferedImage(height * p / nP, height * (p + 1) / nP, lt, src));
	}

	private void fromBufferedImage(int from, int to, ShortLookupTable lt, WritableRaster src) {
		int bands = src.getNumBands();
		int inc = bands > 2 ? bands - 2 : bands;
		int[] pixel = new int[bands * width];
		int red, green, blue;
		int k = from * width;
		short[] ltl = LChannel;
		short[] lta = aChannel;
		short[] ltb = bChannel;
		short[] lr = ltred;
		short[] lg = ltgreen;
		short[] lb = ltblue;
		int _wpi0 = wpi[0];
		int _wpi1 = wpi[1];
		int _wpi2 = wpi[2];
		int _wpir0 = wpir[0];
		int _wpir1 = wpir[1];
		int _wpir2 = wpir[2];
		for (int j = from; j < to; j++) {
			src.getPixels(0, j, width, 1, pixel);
			for (int i = 0, is = 0; i < width; i++, is += inc, k++) {
				red = pixel[is];
				if (bands > 2) {
					green = pixel[++is];
					blue = pixel[++is];
				} else
					blue = green = red;
				if (lt != null) {
					red = lr[red];
					green = lg[green];
					blue = lb[blue];
				}
				red = exp24ft[red];
				green = exp24ft[green];
				blue = exp24ft[blue];
				// Maxvalue : 25500 (15 bit)
				// Scalingfactor : 25500
				int x = (red * M[0][0]) + (green * M[0][1]) + (blue * M[0][2]);
				int y = (red * M[1][0]) + (green * M[1][1]) + (blue * M[1][2]);
				int z = (red * M[2][0]) + (green * M[2][1]) + (blue * M[2][2]);
				// Maxvalue: 25500 * 35679 = 909814500 (30 Bit)
				// Scaling factor: 100 * 255 * P15
				// Whitepoint
				x = (x + _wpir0) / _wpi0;
				y = (y + _wpir1) / _wpi1;
				z = (z + _wpir2) / _wpi2;

				// Maxvalue: 1.32 * 51 * 5 * 16 = 5385.6
				// Scaling factor: 51 * 5 * 16
				x = xyz2labft[x];
				y = xyz2labft[y];
				z = xyz2labft[z];

				// Maxvalue: 1.096961310486 * 51 * 5 * 16 = 4475.6
				// Scaling factor: 51 * 5 * 16

				int L = (116 * y) - FTOT_16;
				int a = 500 * (x - y);
				int b = 200 * (y - z);

				ltl[k] = (short) ((L + F2R) / F2);
				lta[k] = (short) ((a + F2R) / F2);
				ltb[k] = (short) ((b + F2R) / F2);
			}
		}
	}

	public LabImage(final ImageData swtImageData, final ShortLookupTable lt, float[][] mixer, int whitePoint) {
		this(swtImageData.width, swtImageData.height, whitePoint);
		setupToneTables(lt);
		final PaletteData palette = swtImageData.palette;
		int pixel, red, green, blue;
		if (palette.isDirect) {
			if (nP == 1)
				fromDirectImage(0, height, swtImageData, lt, palette);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> fromDirectImage(height * p / nP, height * (p + 1) / nP, swtImageData, lt, palette));
		} else {
			RGB[] rgBs = palette.getRGBs();
			int l = rgBs.length;
			short[] Ls = new short[l];
			short[] as = new short[l];
			short[] bs = new short[l];
			short[] lr = ltred;
			short[] lg = ltgreen;
			short[] lb = ltblue;
			int _wpi0 = wpi[0];
			int _wpi1 = wpi[1];
			int _wpi2 = wpi[2];
			int _wpir0 = wpir[0];
			int _wpir1 = wpir[1];
			int _wpir2 = wpir[2];

			for (int i = 0; i < l; i++) {
				RGB rgb = rgBs[i];
				red = rgb.red;
				green = rgb.green;
				blue = rgb.blue;
				if (lt != null) {
					red = lr[red];
					green = lg[green];
					blue = lb[blue];
				}
				red = exp24ft[red];
				green = exp24ft[green];
				blue = exp24ft[blue];
				// Maxvalue : 25500 (15 bit)
				// Scalingfactor : 25500
				int x = (red * M[0][0]) + (green * M[0][1]) + (blue * M[0][2]);
				int y = (red * M[1][0]) + (green * M[1][1]) + (blue * M[1][2]);
				int z = (red * M[2][0]) + (green * M[2][1]) + (blue * M[2][2]);
				// Maxvalue: 25500 * 35679 = 909814500 (30 Bit)
				// Scaling factor: 100 * 255 * P15
				// Whitepoint
				x = (x + _wpir0) / _wpi0;
				y = (y + _wpir1) / _wpi1;
				z = (z + _wpir2) / _wpi2;

				// Maxvalue: 1.32 * 51 * 5 * 16 = 5385.6
				// Scaling factor: 51 * 5 * 16
				x = xyz2labft[x];
				y = xyz2labft[y];
				z = xyz2labft[z];

				// Maxvalue: 1.096961310486 * 51 * 5 * 16 = 4475.6
				// Scaling factor: 51 * 5 * 16

				int L = (116 * y) - FTOT_16;
				int a = 500 * (x - y);
				int b = 200 * (y - z);

				Ls[i] = (short) ((L + F2R) / F2);
				as[i] = (short) ((a + F2R) / F2);
				bs[i] = (short) ((b + F2R) / F2);
			}
			int[] scanLine = new int[width];
			short[] ltl = LChannel;
			short[] lta = aChannel;
			short[] ltb = bChannel;
			for (int y = 0, k = 0; y < height; y++) {
				swtImageData.getPixels(0, y, width, scanLine, 0);
				for (int x = 0; x < width; x++, k++) {
					pixel = scanLine[x];
					ltl[k] = Ls[pixel];
					lta[k] = as[pixel];
					ltb[k] = bs[pixel];
				}
			}
		}
	}

	private void fromDirectImage(int from, int to, ImageData swtImageData, ShortLookupTable lt, PaletteData palette) {
		int pixel;
		int red;
		int green;
		int blue;
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int[] scanLine = new int[width];
		int k = from * width;
		short[] ltl = LChannel;
		short[] lta = aChannel;
		short[] ltb = bChannel;
		short[] lr = ltred;
		short[] lg = ltgreen;
		short[] lb = ltblue;
		int _wpi0 = wpi[0];
		int _wpi1 = wpi[1];
		int _wpi2 = wpi[2];
		int _wpir0 = wpir[0];
		int _wpir1 = wpir[1];
		int _wpir2 = wpir[2];

		for (int j = from; j < to; j++) {
			swtImageData.getPixels(0, j, width, scanLine, 0);
			for (int i = 0; i < width; i++, k++) {
				pixel = scanLine[i];
				red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
				green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift : (pixel & greenMask) << greenShift);
				blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
				if (lt != null) {
					red = lr[red];
					green = lg[green];
					blue = lb[blue];
				}
				red = exp24ft[red];
				green = exp24ft[green];
				blue = exp24ft[blue];
				// Maxvalue : 25500 (15 bit)
				// Scalingfactor : 25500
				int x = (red * M[0][0]) + (green * M[0][1]) + (blue * M[0][2]);
				int y = (red * M[1][0]) + (green * M[1][1]) + (blue * M[1][2]);
				int z = (red * M[2][0]) + (green * M[2][1]) + (blue * M[2][2]);
				// Maxvalue: 25500 * 35679 = 909814500 (30 Bit)
				// Scaling factor: 100 * 255 * P15
				// Whitepoint
				x = (x + _wpir0) / _wpi0;
				y = (y + _wpir1) / _wpi1;
				z = (z + _wpir2) / _wpi2;

				// Maxvalue: 1.32 * 51 * 5 * 16 = 5385.6
				// Scaling factor: 51 * 5 * 16
				x = xyz2labft[x];
				y = xyz2labft[y];
				z = xyz2labft[z];

				// Maxvalue: 1.096961310486 * 51 * 5 * 16 = 4475.6
				// Scaling factor: 51 * 5 * 16

				int L = (116 * y) - FTOT_16;
				int a = 500 * (x - y);
				int b = 200 * (y - z);

				ltl[k] = (short) ((L + F2R) / F2);
				lta[k] = (short) ((a + F2R) / F2);
				ltb[k] = (short) ((b + F2R) / F2);
			}
		}
	}

	private void setupToneTables(ShortLookupTable lt) {
		if (lt != null) {
			if (lt.getOffset() != 0)
				throw new UnsupportedOperationException();
			if (lt.getNumComponents() == 1)
				ltred = ltgreen = ltblue = lt.getTable()[0];
			else {
				ltred = lt.getTable()[0];
				ltgreen = lt.getTable()[1];
				ltblue = lt.getTable()[2];
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
		int k = from * width;
		int[] pixel = new int[3 * width];
		short[] ltl = LChannel;
		short[] lta = aChannel;
		short[] ltb = bChannel;
		for (int j = from; j < to; j++) {
			for (int i = 0, is = 0; i < width; i++, k++) {
				// Value range -32000 : 32000
				// Scaling factor: 51
				int L = (ltl[k] & 0xffff) * F2V;
				int a = lta[k] * F2V;
				int b = ltb[k] * F2V;
				// Value range -512000 : 512000
				// Scaling factor: 51 * 16
				int y = (L + FTOTV_16 + 58) / 116;
				// 0 .. 4527
				int x = ((a + 250) / 500) + y;
				// -1024 .. 5551
				int z = y - ((b + 100) / 200);
				// -2560 .. 1967
				// Scaling factor: 51 * 16
				x = lab2xyzft[x + LAB2XOFF];
				y = lab2xyzft[y + LAB2XOFF];
				z = lab2xyzft[z + LAB2XOFF];
				// Value range - 25197 : 88850
				// Scaling factor: 100 * 51 * P15
				x = x * wpi[0] + P14 >> 15;
				y = y * wpi[1] + P14 >> 15;
				z = z * wpi[2] + P14 >> 15;

				// Scaling factor = 51 * 100;
				int red = ((x * Mi[0][0]) + (y * Mi[0][1]) + (z * Mi[0][2]) + 50) / 100;
				int green = ((x * Mi[1][0]) + (y * Mi[1][1]) + (z * Mi[1][2]) + 50) / 100;
				int blue = ((x * Mi[2][0]) + (y * Mi[2][1]) + (z * Mi[2][2]) + 50) / 100;

				// Scaling factor = 51 * P15;
				red = (red < 0) ? 0 : (red > F4) ? F3 : red + P8 >> 9;
				green = (green < 0) ? 0 : (green > F4) ? F3 : green + P8 >> 9;
				blue = (blue < 0) ? 0 : (blue > F4) ? F3 : blue + P8 >> 9;

				// Scaling factor = 51 * P6;
				red = rt24ft[red];
				green = rt24ft[green];
				blue = rt24ft[blue];
				// Scaling factor = 51 * P6;

				// convert to 0..255
				pixel[is++] = red * 5 + P5 >> 6;
				pixel[is++] = green * 5 + P5 >> 6;
				pixel[is++] = blue * 5 + P5 >> 6;
			}
			raster.setPixels(0, j, width, 1, pixel);
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
		int[] scanLine = new int[width];
		int k = from * width;
		short[] ltl = LChannel;
		short[] lta = aChannel;
		short[] ltb = bChannel;
		for (int j = from; j < to; j++) {
			for (int i = 0; i < width; i++, k++) {
				// Value range -32000 : 32000
				// Scaling factor: 51
				int L = (ltl[k] & 0xffff) * F2V;
				int a = lta[k] * F2V;
				int b = ltb[k] * F2V;
				// Value range -512000 : 512000
				// Scaling factor: 51 * 16
				int y = (L + FTOTV_16 + 58) / 116;
				// 0 .. 4527
				int x = ((a + 250) / 500) + y;
				// -1024 .. 5551
				int z = y - ((b + 100) / 200);
				// -2560 .. 1967
				// Scaling factor: 51 * 16
				x = lab2xyzft[x + LAB2XOFF];
				y = lab2xyzft[y + LAB2XOFF];
				z = lab2xyzft[z + LAB2XOFF];
				// Value range - 25197 : 88850
				// Scaling factor: 100 * 51 * P15
				x = x * wpi[0] + P14 >> 15;
				y = y * wpi[1] + P14 >> 15;
				z = z * wpi[2] + P14 >> 15;

				// Scaling factor = 51 * 100;
				int red = ((x * Mi[0][0]) + (y * Mi[0][1]) + (z * Mi[0][2]) + 50) / 100;
				int green = ((x * Mi[1][0]) + (y * Mi[1][1]) + (z * Mi[1][2]) + 50) / 100;
				int blue = ((x * Mi[2][0]) + (y * Mi[2][1]) + (z * Mi[2][2]) + 50) / 100;

				// Scaling factor = 51 * P15;
				red = (red < 0) ? 0 : (red > F4) ? F3 : red + P8 >> 9;
				green = (green < 0) ? 0 : (green > F4) ? F3 : green + P8 >> 9;
				blue = (blue < 0) ? 0 : (blue > F4) ? F3 : blue + P8 >> 9;

				// Scaling factor = 51 * P6;
				red = rt24ft[red];
				green = rt24ft[green];
				blue = rt24ft[blue];
				// Scaling factor = 51 * P6;

				// convert to 0..255
				scanLine[i] = red * 5 + P5 >> 6 | green * 5 + P5 >> 6 << 8 | blue * 5 + P5 >> 6 << 16;
			}
			data.setPixels(0, j, width, scanLine, 0);
		}
	}

	public void applyLookup(final ShortLookupTable lt, final int curveType) {
		if (lt.getOffset() != 0)
			throw new UnsupportedOperationException();
		if (nP == 1)
			applyLookup(0, len, lt, curveType);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyLookup(height * p / nP, height * (p + 1) / nP, lt, curveType));
	}

	private void applyLookup(int from, int to, ShortLookupTable lt, int curveType) {
		short[][] table = lt.getTable();
		short[] ylt = table[0];
		if (lt.getNumComponents() < 3) {
			switch (curveType) {
			case Curve.CHANNEL_CHROMA:
				short[] achnl = aChannel;
				short[] bchnl = bChannel;
				for (int i = from; i < to; i++) {
					float a = (float) achnl[i] / MAXL;
					float b = (float) bchnl[i] / MAXL;
					double chroma = (Math.sqrt(a * a + b * b) * MAXL);
					double f = (chroma >= MAXL ? ylt[MAXL - 1] : ylt[(int) chroma]) / chroma * MAXL;
					a *= f;
					b *= f;
					achnl[i] = a <= -MAXL ? -MAXL : a >= MAXL ? MAXL : (short) a;
					bchnl[i] = b <= -MAXL ? -MAXL : b >= MAXL ? MAXL : (short) b;
				}
				break;
			default:
				short[] lchnl = LChannel;
				for (int i = from; i < to; i++) {
					int L = lchnl[i];
					lchnl[i] = (L >= MAXL) ? ylt[MAXL - 1] : ylt[L];
				}
				break;
			}
		} else {
			short[] lchnl = LChannel;
			short[] alt = table[1];
			short[] blt = table[2];
			short[] achnl = aChannel;
			short[] bchnl = bChannel;
			for (int i = from; i < to; i++) {
				int L = lchnl[i];
				lchnl[i] = (L >= MAXL) ? ylt[MAXL - 1] : ylt[L];
				int a = achnl[i] + MAXL;
				achnl[i] = (short) (((a <= 0) ? alt[0] : (a >= MAXAB) ? alt[MAXAB - 1] : alt[a]) - MAXL);
				int b = bchnl[i] + MAXL;
				bchnl[i] = (short) ((b <= 0) ? blt[0] : (b >= MAXAB) ? blt[MAXAB - 1] : blt[b] - MAXL);
			}
		}
	}

	public void applyUnsharpMask(UnsharpMask mask) {
		int shiftfac = 1 << UnsharpMask.SHIFT;
		final int c1 = shiftfac * 255;
		float amount = mask.amount;
		final short[] work1 = mask.createLabMaskArray(LChannel, width, height);
		if (work1 == null)
			return;
		final short[] toneTable = mask.toneMask == null ? null : mask.toneMask.makeTable(MAXL);
		final int a = (int) (shiftfac * 4 * amount);
		final int threshold = (int) (mask.threshold * 255);
		if (nP == 1)
			applyUnsharpMask(0, height, c1, work1, toneTable, a, threshold);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyUnsharpMask(height * p / nP, height * (p + 1) / nP, c1, work1, toneTable, a, threshold));
	}

	private void applyUnsharpMask(int from, int to, int c1, short[] work1, short[] toneTable, int a, int threshold) {
		int lum1, lum2, diff;
		int index = from * width;
		short[] ltl = LChannel;
		if (toneTable != null) {
			if (threshold != 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = ltl[index];
						lum2 = work1[index];
						diff = lum1 - lum2;
						if (threshold > 0) {
							if (diff > 0 && diff >= threshold || diff < 0 && -diff >= threshold) {
								lum1 += a * toneTable[lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : lum1] / MAXL * diff / c1;
								ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
							}
						} else {
							lum1 += (diff < 0 ? b : a) * diff * toneTable[lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : lum1]
									/ MAXL / c1;
							ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
						}
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = ltl[index];
						lum1 += a * toneTable[lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : lum1] / MAXL * (lum1 - work1[index])
								/ c1;
						ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
					}
				}
		} else {
			if (threshold > 0) {
				int b = a * (255 + threshold) / 255;
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = ltl[index];
						lum2 = work1[index];
						diff = lum1 - lum2;
						if (threshold > 0) {
							if (diff > 0 && diff >= threshold || diff < 0 && -diff >= threshold) {
								lum1 += a * diff / c1;
								ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
							}
						} else {
							lum1 += (diff < 0 ? b : a) * diff / c1;
							ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
						}
					}
				}
			} else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < width; x++, index++) {
						lum1 = ltl[index];
						lum1 += a * (lum1 - work1[index]) / c1;
						ltl[index] = lum1 < 0 ? 0 : lum1 > MAXL ? MAXL : (short) lum1;
					}
				}
		}
	}

	public void toBw() {
		aChannel = new short[len];
		aChannel = new short[len];
	}

	public void applyColorBoost(ColorBoost colorBoost) {
		float c;
		float a = colorBoost.aBoost;
		float b = colorBoost.bBoost;
		float amul = 1.0f, bmul = 1.0f;
		if (a > b) {
			c = a;
			if (a > 0f)
				bmul = b / a;
		} else {
			c = b;
			if (b > 0f)
				amul = a / b;
		}
		final int shift_a = Math.round(colorBoost.aShift * MAXAB);
		final int shift_b = Math.round(colorBoost.bShift * MAXAB);
		final int afac = (int) (c * amul * P8);
		final int bfac = (int) (c * bmul * P8);
		if (nP == 1)
			applyColorBoost(0, len, shift_a, shift_b, afac, bfac);
		else
			IntStream.range(0, nP).parallel()
			.forEach(p -> applyColorBoost(height * p / nP, height * (p + 1) / nP, shift_a, shift_b, afac, bfac));
	}

	private void applyColorBoost(int from, int to, int shift_a, int shift_b, int afac, int bfac) {
		for (int i = from; i < to; i++) {
			int na = (aChannel[i] + shift_a) * afac;
			int nb = (bChannel[i] + shift_b) * bfac;
			na = na + P7 >> 8;
			nb = nb + P7 >> 8;
			aChannel[i] = na < -32000 ? -32000 : na >= 32000 ? 32000 : (short) na;
			bChannel[i] = nb < -32000 ? -32000 : nb >= 32000 ? 32000 : (short) nb;
		}
	}

	public void applyMaskedLookup(ShortLookupTable lt, IMask mask) {
		if (lt.getOffset() != 0)
			throw new UnsupportedOperationException();
		final short[] array = mask.createLabMaskArray(LChannel, width, height);
		if (array != null) {
			final short[] ylt = lt.getTable()[0];
			if (nP == 1)
				applyMaskedLookup(0, len, ylt, array);
			else
				IntStream.range(0, nP).parallel()
				.forEach(p -> applyMaskedLookup(height * p / nP, height * (p + 1) / nP, ylt, array));
		}
	}

	protected void applyMaskedLookup(int from, int to, short[] ylt, short[] mask) {
		short[] ltl = LChannel;
		for (int i = from; i < to; i++) {
			int L = ltl[i];
			int L2 = (L > MAXL) ? ylt[MAXL] : ylt[L];
			ltl[i] = (short) ((mask[i] * L2 + (MAXL - mask[i]) * L) / MAXL);
		}
	}
}
