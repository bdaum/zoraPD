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
 * (c) 2020 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;

public class HistogramManager {

	private static final String LIGHT_BLUE = "lightBlue"; //$NON-NLS-1$

	public HistogramManager() {
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		if (!colorRegistry.hasValueFor(LIGHT_BLUE))
			colorRegistry.put(LIGHT_BLUE, new RGB(96, 144, 255));
	}

	public final static int LIGHT = 0;
	public final static int DARK = 1;
	public final static int CONTRAST = 2;

	private static final int MINTHUMBNAILSIZE = 160 * 120;
	private int[] reds = new int[256], greens = new int[256], blues = new int[256], greys = new int[256];
	private int[] polyline = new int[516];
	private int mxvalue;
	private boolean weighted;

	public void recalculate(Image image, boolean weighted) {
		this.weighted = weighted;
		mxvalue = 0;
		if (image != null) {
			for (int i = 0; i < 256; i++)
				reds[i] = greens[i] = blues[i] = 0;
			ImageData data = image.getImageData();
			PaletteData palette = data.palette;
			int yinc = 1;
			int xinc = 1;
			long size = (long) data.height * data.width;
			if (size > MINTHUMBNAILSIZE) {
				yinc = (int) (Math.sqrt(size / (double) MINTHUMBNAILSIZE) + 0.5d);
				xinc = (int) (size / ((double) MINTHUMBNAILSIZE * yinc));
			}
			if (weighted) {
				int h1 = data.height * 12 / 100;
				int h2 = data.height * 30 / 100;
				int h3 = data.height * 70 / 100;
				int h4 = data.height * 88 / 100;
				int w1 = data.width * 12 / 100;
				int w2 = data.width * 30 / 100;
				int w3 = data.width * 70 / 100;
				int w4 = data.width * 88 / 100;
				for (int y = h1; y < h2; y += yinc)
					for (int x = w1; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
				for (int y = h2; y < h3; y += yinc) {
					for (int x = w1; x < w2; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
					for (int x = w2; x < w3; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						reds[rgb.red] += 2;
						greens[rgb.green] += 2;
						blues[rgb.blue] += 2;
					}
					for (int x = w3; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
				}
				for (int y = h3; y < h4; y += yinc)
					for (int x = w1; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
			} else
				for (int y = 0; y < data.height; y += yinc)
					for (int x = 0; x < data.width; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
			for (int i = 0; i < 255; i++) {
				int i1 = i + 1;
				int r = reds[i] + reds[i1];
				int g = greens[i] + greens[i1];
				int b = blues[i] + blues[i1];
				if (r > mxvalue)
					mxvalue = r;
				if (g > mxvalue)
					mxvalue = g;
				if (b > mxvalue)
					mxvalue = b;
			}
			mxvalue >>= 1;
		}
	}

	public void paint(GC gc, int x, int y, int width, int height, int colorMode, boolean inline, boolean showGrey,
			boolean showRed, boolean showGreen, boolean showBlue, boolean proposals) {
		Device display = gc.getDevice();
		String message = null;
		int gmax = 0;
		int gavg = 0;
		int shadows = 0;
		int highlights = 0;
		for (int i = 0; i < 256; i++) {
			int g = greys[i] = (reds[i] * 306 + greens[i] * 601 + blues[i] * 117) >> 10; // 0.299R+0.587G+0.114B
			gavg += g;
			if (i > 0) {
				g += greys[i - 1];
				if (g > gmax)
					gmax = g;
				if (i <= 25) {
					if (shadows < g)
						shadows = g;
				} else if (i > 230 && highlights < g)
					highlights = g;
			}
		}
		if (proposals) {
			gmax >>= 1;
			gavg >>= 8;
			shadows >>= 1;
			highlights >>= 1;
			double threshold = gavg * 0.05;
			if (shadows <= threshold)
				message = highlights > gavg ? Messages.getString("HistogramView.overexpose") //$NON-NLS-1$
						: highlights < threshold ? Messages.getString("HistogramView.enhance_contrast") //$NON-NLS-1$
								: Messages.getString("HistogramView.darken_shadows"); //$NON-NLS-1$
			else if (highlights <= threshold)
				message = shadows > gavg ? Messages.getString("HistogramView.underexposed") //$NON-NLS-1$
						: Messages.getString("HistogramView.lighten_highlights"); //$NON-NLS-1$
			else if (highlights > gavg && shadows > gavg)
				message = Messages.getString("HistogramView.compress_contrast"); //$NON-NLS-1$
		}
		Transform transform = new Transform(display);
		transform.translate(x, y + height);
		transform.scale(width / 256f, -(float) height / mxvalue);
		gc.setTransform(transform);
		if (showGrey) {
			boolean hasColorCurves = showRed || showGreen || showBlue;
			int color;
			switch (colorMode) {
			case DARK:
				color = hasColorCurves ? SWT.COLOR_GRAY : SWT.COLOR_WHITE;
				break;
			case CONTRAST:
				color = SWT.COLOR_CYAN;
				break;
			default:
				color = hasColorCurves ? SWT.COLOR_DARK_GRAY : SWT.COLOR_BLACK;
			}
			drawCurve(gc, greys, display.getSystemColor(color), true, false);
		}
		if (showRed)
			drawCurve(gc, reds, display.getSystemColor(SWT.COLOR_RED), !showGrey, true);
		if (showGreen)
			drawCurve(gc, greens, display.getSystemColor(colorMode == DARK ? SWT.COLOR_GREEN : SWT.COLOR_DARK_GREEN),
					!showGrey, true);
		if (showBlue)
			drawCurve(gc, blues, colorMode == DARK ? JFaceResources.getColorRegistry().get(LIGHT_BLUE)
					: display.getSystemColor(SWT.COLOR_BLUE), !showGrey, true);
		gc.setTransform(null);
		transform.dispose();
		if (message != null) {
			gc.setForeground(display.getSystemColor(colorMode == DARK ? SWT.COLOR_CYAN : SWT.COLOR_DARK_CYAN));
			TextLayout textLayout = new TextLayout(gc.getDevice());
			textLayout.setText(message);
			textLayout.draw(gc, width - textLayout.getBounds().width - 10, 15);
			textLayout.dispose();
		}
		if (weighted) {
			gc.setForeground(display.getSystemColor(colorMode == DARK ? SWT.COLOR_GRAY : SWT.COLOR_DARK_GRAY));
			TextLayout textLayout = new TextLayout(gc.getDevice());
			textLayout.setText(Messages.getString("HistogramManager.center_weighted")); //$NON-NLS-1$
			textLayout.draw(gc, 10, 5);
			textLayout.dispose();
		}
		if (!inline) {
			gc.setAlpha(255);
			gc.setForeground(display.getSystemColor(colorMode == DARK ? SWT.COLOR_GRAY : SWT.COLOR_DARK_GRAY));
			gc.drawLine(width / 3, 0, width / 3, height);
			gc.drawLine(2 * width / 3, 0, 2 * width / 3, height);
		}
	}

	private void drawCurve(GC gc, int[] values, Color color, boolean fill, boolean line) {
		int[] points = polyline;
		int p = 2;
		points[++p] = values[0] + values[1] >> 1;
		for (int i = 1; i < 255; i++) {
			points[++p] = i;
			points[++p] = values[i - 1] + (values[i] << 1) + values[i + 1] >> 2;
		}
		points[512] = 255;
		points[513] = values[254] + values[255] >> 1;
		if (fill) {
			points[514] = 255;
			gc.setAlpha(64);
			gc.setBackground(color);
			gc.fillPolygon(points);
			gc.setAlpha(255);
			points[514] = 0;
		}
		if (line) {
			points[1] = points[3];
			points[514] = points[512];
			points[515] = points[513];
			gc.setForeground(color);
			gc.drawPolyline(points);
			points[1] = points[514] = points[515] = 0;
		}
	}

}
