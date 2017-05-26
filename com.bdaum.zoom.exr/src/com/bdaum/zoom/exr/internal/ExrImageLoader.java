/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.exr.internal;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import net.exrforjava.image.Header;
import net.exrforjava.io.FrameBuffer;
import net.exrforjava.io.RgbaInputFile;
import net.exrforjava.math.Box2i;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import com.bdaum.zoom.image.IImageLoader;
import com.bdaum.zoom.image.ZImage;

public class ExrImageLoader implements IImageLoader {

	private final File file;
	private int w;
	private int h;
	private Header header;

	public ExrImageLoader(File file) {
		this.file = file;
	}

	
	public String getComments() {
		StringTokenizer st = new StringTokenizer(header.toString(), "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (token.startsWith("comments")) { //$NON-NLS-1$
				int p = token.indexOf(':');
				if (p >= 0)
					token = token.substring(p + 1).trim();
				if ((token.startsWith("\"") && token.endsWith("\"")) //$NON-NLS-1$//$NON-NLS-2$
						|| (token.startsWith("'") && token.endsWith("'"))) //$NON-NLS-1$ //$NON-NLS-2$
					token = token.substring(1, token.length() - 2);
				return token;
			}
		}
		return null;
	}

	
	public int getImageHeight() {
		return h;
	}

	
	public int getImageWidth() {
		return w;
	}

	@SuppressWarnings("deprecation")
	
	public ZImage loadImage(int width, int height, int raster, float exposure,
			double maxFactor) throws IOException {
		float m = (float) Math.pow(2f, Math.min(Math.max(exposure + 2.47393f,
				-20f), 20f));
		RgbaInputFile rgbafile = new RgbaInputFile(file.getAbsolutePath());
		header = rgbafile.getHeader();
		float pixelAspectRatio = header.getPixelAspectRatio();
		Box2i dw = header.getDataWindow();
		FrameBuffer buffer = new FrameBuffer(dw);
		rgbafile.setFrameBuffer(buffer);
		rgbafile.readPixels();
		float[][] r = buffer.getFloatData("R"); //$NON-NLS-1$
		float[][] g = buffer.getFloatData("G"); //$NON-NLS-1$
		float[][] b = buffer.getFloatData("B"); //$NON-NLS-1$
		rgbafile = null;
		buffer = null;
		h = Math.min(r.length, Math.min(g.length, b.length));
		if (h > 0) {
			w = Math.min(r[0].length, Math.min(g[0].length, b[0].length));
			if (w > 0) {
				double f = 1d;
				if (width > 0 && height > 0)
					f = Math.min(maxFactor, computeScale(w, h, width, height));
				int previewWidth = (int) (w * f + 0.5d);
				int previewHeight = (int) (h * f + 0.5d);
				previewHeight = (int) (previewHeight
						/ (previewWidth * pixelAspectRatio) * previewWidth + .5f);
				previewWidth = Math.max((previewWidth + raster / 2) / raster
						* raster, 1);
				previewHeight = Math.max((previewHeight + raster / 2) / raster
						* raster, 1);
				double fy = (double) h / previewHeight;
				double fx = (double) w / previewWidth;
				PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
				ImageData data = new ImageData(previewWidth, previewHeight, 24,
						palette);
				int[] scanLine = new int[previewWidth];
				int red, green, blue;
				for (int y = 0; y < previewHeight; ++y) {
					int sy = (int) (y * fy);
					float[] rR = r[sy];
					float[] rG = g[sy];
					float[] rB = b[sy];
					for (int x = 0; x < previewWidth; ++x) {
						int sx = (int) (x * fx);
						red = gamma(rR[sx], m);
						green = gamma(rG[sx], m);
						blue = gamma(rB[sx], m);
						scanLine[x] = red | green << 8 | blue << 16;
					}
					data.setPixels(0, y, previewWidth, scanLine, 0);
				}
				return new ZImage(data, file.getAbsolutePath());
			}
		}
		return null;
	}

	private static int gamma(float h, float m) {
		//
		// Conversion from float to byte pixel data,
		// with gamma correction. The conversion is the same
		// as in the exrdisplay program's ImageView class,
		// except with defog, kneeLow, and kneeHigh fixed
		// at 0.0, 0.0, and 5.0 respectively.
		//

		float x = Math.max(0f, h * m);

		if (x > 1)
			x = 1 + knee(x - 1, 0.184874f);
		return Math.min(Math.max(0,
				(int) (Math.pow(x, 0.4545f) * 84.66f + 0.5f)), 255);
	}

	private static float knee(float x, float f) {
		return (float) (Math.log(x * f + 1) / f);
	}

	private static double computeScale(int iWidth, int iHeight, int width,
			int height) {
		boolean oriImage = iWidth > iHeight;
		boolean oriThumb = width > height;
		if (oriImage != oriThumb) {
			int w = width;
			width = height;
			height = w;
		}
		double xscale = (double) width / iWidth;
		double yscale = (double) height / iHeight;
		return Math.min(xscale, yscale);
	}
}
