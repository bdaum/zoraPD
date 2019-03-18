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

package com.bdaum.zoom.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.IntStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.internal.awt.LabImage;
import com.bdaum.zoom.image.internal.swt.ImageLoader;
import com.bdaum.zoom.image.recipe.PerspectiveCorrection;
import com.bdaum.zoom.image.recipe.SplitTone;
import com.bdaum.zoom.image.recipe.Transformation;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;

import Jama.Matrix;
import mediautil.gen.Log;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

/**
 * Commonly used methods for image manipulation
 * 
 * Downscaling in parts inspired from the ImgScalr library - The Buzz Media, LLC
 *
 */
public class ImageUtilities {

	private static final Random randomNumberGenerator = new Random();
	private static Random randomNumbers;
	private static int nP = ImageConstants.NPROCESSORS;
	private static final int P16 = 1 << 16;
	private static final int P15 = 1 << 15;

	public static String detectImageFormat(String uri) {
		String fileExtension = ""; //$NON-NLS-1$
		try {
			File file = new File(new URI(uri));
			try (FileInputStream in = new FileInputStream(file)) {
				byte[] buffer = new byte[10];
				in.read(buffer);
				if (buffer[0] == 'M' && buffer[1] == 'M' && buffer[2] == '*'
						|| buffer[0] == 'I' && buffer[1] == 'I' && buffer[2] == '*')
					fileExtension = "tif"; //$NON-NLS-1$
				else if (buffer[0] == 0x89 && buffer[1] == 'P' && buffer[2] == 'N' && buffer[3] == 'G')
					fileExtension = "png"; //$NON-NLS-1$
				else if (buffer[0] == 'G' && buffer[1] == 'I' && buffer[2] == 'F')
					fileExtension = "gif"; //$NON-NLS-1$
				else if (buffer[0] == 0xFF && buffer[1] == 0xD8 && buffer[2] == 0xFF)
					fileExtension = "jpg"; //$NON-NLS-1$
				else if (buffer[0] == 0x42 && buffer[1] == 0x4d)
					fileExtension = "bmp"; //$NON-NLS-1$
			} catch (IOException e) {
				// do nothing
			}
		} catch (URISyntaxException | IllegalArgumentException e) {
			// Do nothing
		}
		return fileExtension;
	}

	/**
	 * Rotates an SWT image
	 *
	 * @param image
	 *            - input image
	 * @param rotation
	 *            - rotation angle (0,90,180,270)
	 * @param scaleX
	 *            - horizontal scaling value (negative for mirroring)
	 * @param scaleY
	 *            - vertical scaling value (negative for mirroring)
	 * @return output image
	 */
	public static Image rotateSwtImage(Image image, int rotation, float scaleX, float scaleY) {
		Transform t = null;
		GC gc = null;
		try {
			Device device = image.getDevice();
			Rectangle ibounds = image.getBounds();
			int w = ibounds.width;
			int h = ibounds.height;
			Image img2 = (rotation % 180 == 0) ? new Image(device, w, h) : new Image(device, h, w);
			gc = new GC(img2);
			t = new Transform(device);
			float offsetX = w / 2f;
			float offsetY = h / 2f;
			if (rotation % 180 == 0)
				t.translate(offsetX, offsetY);
			else
				t.translate(offsetY, offsetX);
			t.rotate(rotation);
			t.scale(scaleX, scaleY);
			t.translate(-offsetX, -offsetY);
			gc.setTransform(t);
			gc.drawImage(image, 0, 0);
			return img2;
		} catch (Exception e) {
			return null;
		} finally {
			if (t != null)
				t.dispose();
			if (gc != null)
				gc.dispose();
			image.dispose();
		}
	}

	/**
	 * Rotates SWT image data
	 *
	 * @param image
	 *            - source image data
	 * @param rotation
	 *            0, 90, 180, 270
	 * @param scaleX
	 *            - -1 or +1
	 * @param scaleY
	 *            - -1 or +1
	 * @return rotated image
	 */
	public static ImageData rotateSwtImage(final ImageData image, int rotation, float scaleX, float scaleY) {
		switch (rotation) {
		case 90:
			scaleX = -scaleX;
			break;
		case 180:
			scaleX = -scaleX;
			scaleY = -scaleY;
			rotation = 0;
			break;
		case 270:
			scaleY = -scaleY;
			rotation = 90;
			break;
		}
		final ImageData newData;
		if (rotation != 0) {
			int h = image.width;
			newData = new ImageData(image.height, h, image.depth, image.palette);
			if (nP == 1)
				rotateImage(0, h, image, scaleX, scaleY, newData, 0);
			else {
				final float scaleX1 = scaleX;
				final float scaleY1 = scaleY;
				IntStream.range(0, nP).parallel().forEach(p -> rotateImage(h * p / nP, h * (p + 1) / nP, image, scaleX1,
						scaleY1, newData, scaleY1 < 0 ? h * (nP - p - 1) / nP : h * p / nP));
			}
		} else {
			if (scaleX > 0 && scaleY > 0)
				return image;
			int h = image.height;
			newData = new ImageData(image.width, h, image.depth, image.palette);
			if (nP == 1)
				flipImage(0, h, image, scaleX, scaleY, newData);
			else {
				final float scaleX1 = scaleX;
				final float scaleY1 = scaleY;
				IntStream.range(0, nP).parallel()
						.forEach(p -> flipImage(h * p / nP, h * (p + 1) / nP, image, scaleX1, scaleY1, newData));
			}
		}
		return newData;
	}

	private static void flipImage(int from, int to, ImageData image, float scaleX, float scaleY, ImageData newData) {
		byte[] sd = image.data;
		byte[] td = newData.alphaData;
		int bytesPerLine = image.bytesPerLine;
		int tx, sx;
		int w = image.width;
		// int[] sourceline = new int[w];
		if (scaleY < 0) {
			int h1 = image.height - 1;
			if (scaleX < 0) {
				// int[] scanline = new int[w];
				for (int y = to - 1; y >= from; y--) {
					tx = (h1 - y) * bytesPerLine;
					sx = y * bytesPerLine + w * 3 - 3;
					for (int x = 0; x < w; x++) {
						td[tx++] = sd[sx++];
						td[tx++] = sd[sx++];
						td[tx++] = sd[sx++];
						sx -= 6;
					}
					// image.getPixels(0, y, w, sourceline, 0);
					// for (int x = w - 1, xt = 0; x >= xt; x--, xt++) {
					// scanline[xt] = sourceline[x];
					// scanline[x] = sourceline[xt];
					// }
					// newData.setPixels(0, h1 - y, w, scanline, 0);
				}
			} else
				for (int y = to - 1; y >= from; y--) {
					sx = y * bytesPerLine;
					tx = (h1 - y) * bytesPerLine;
					for (int x = 0; x < w; x++) {
						td[tx++] = sd[sx++];
						td[tx++] = sd[sx++];
						td[tx++] = sd[sx++];
					}
					// image.getPixels(0, y, w, sourceline, 0);
					// newData.setPixels(0, h1 - y, w, sourceline, 0);
				}
		} else {
			// int[] scanline = new int[w];
			for (int y = from; y < to; y++) {
				tx = y * bytesPerLine;
				sx = tx + w * 3 - 3;
				// image.getPixels(0, y, w, sourceline, 0);
				for (int x = 0; x < w; x++) {
					td[tx++] = sd[sx++];
					td[tx++] = sd[sx++];
					td[tx++] = sd[sx++];
					sx -= 6;
				}
				// for (int x = w - 1, xt = 0; x >= xt; x--, xt++) {
				// scanline[xt] = sourceline[x];
				// scanline[x] = sourceline[xt];
				// }
				// newData.setPixels(0, y, w, scanline, 0);
			}
		}
	}

	private static void rotateImage(int from, int to, ImageData image, float scaleX, float scaleY, ImageData newData,
			int offset) {
		int w = image.height;
		int[] scanline = new int[w];
		if (scaleY < 0) {
			int yt = offset;
			if (scaleX < 0)
				for (int y = to - 1; y >= from; y--) {
					int xt = 0;
					for (int x = w - 1; x >= 0; x--)
						scanline[xt++] = image.getPixel(y, x);
					newData.setPixels(0, yt++, w, scanline, 0);
				}
			else
				for (int y = to - 1; y >= from; y--) {
					for (int x = 0; x < w; x++)
						scanline[x] = image.getPixel(y, x);
					newData.setPixels(0, yt++, w, scanline, 0);
				}
		} else {
			if (scaleX < 0)
				for (int y = from; y < to; y++) {
					int xt = 0;
					for (int x = w - 1; x >= 0; x--)
						scanline[xt++] = image.getPixel(y, x);
					newData.setPixels(0, y, w, scanline, 0);
				}
			else
				for (int y = from; y < to; y++) {
					for (int x = 0; x < w; x++)
						scanline[x] = image.getPixel(y, x);
					newData.setPixels(0, y, w, scanline, 0);
				}
		}
	}

	/**
	 * Rotates BufferedImage Faster than drawImage with affine transform
	 *
	 * @param image
	 *            - source image data
	 * @param rotation
	 *            0, 90, 180, 270
	 * @param scaleX
	 *            - -1 or +1
	 * @param scaleY
	 *            - -1 or +1
	 * @return rotated image
	 */
	public static BufferedImage rotateImage(BufferedImage image, int rotation, float scaleX, float scaleY) {
		switch (rotation) {
		case 90:
			scaleX = -scaleX;
			break;
		case 180:
			scaleX = -scaleX;
			scaleY = -scaleY;
			rotation = 0;
			break;
		case 270:
			scaleY = -scaleY;
			rotation = 90;
			break;
		}
		if (scaleX > 0 && scaleY > 0 && rotation == 0)
			return image;
		ColorModel colorModel = image.getColorModel();
		final WritableRaster src = image.getRaster();
		final WritableRaster out;
		if (rotation != 0) {
			int h = image.getWidth();
			out = colorModel.createCompatibleWritableRaster(image.getHeight(), h);
			if (nP == 1)
				rotateImage(0, h, scaleX, scaleY, src, out);
			else {
				final float scaleX1 = scaleX;
				final float scaleY1 = scaleY;
				IntStream.range(0, nP).parallel()
						.forEach(p -> rotateImage(h * p / nP, h * (p + 1) / nP, scaleX1, scaleY1, src, out));
			}
		} else {
			int h = image.getHeight();
			out = colorModel.createCompatibleWritableRaster(image.getWidth(), h);
			if (nP == 1)
				flipImage(0, h, scaleX, scaleY, src, out);
			else {
				final float scaleX1 = scaleX;
				final float scaleY1 = scaleY;
				IntStream.range(0, nP).parallel()
						.forEach(p -> flipImage(h * p / nP, h * (p + 1) / nP, scaleX1, scaleY1, src, out));
			}
		}
		return new BufferedImage(colorModel, out, false, null);
	}

	private static void flipImage(int from, int to, float scaleX, float scaleY, WritableRaster src,
			WritableRaster out) {
		int bands = src.getNumBands();
		int w = out.getWidth();
		int len = w * bands;
		int[] inLine = new int[len];
		int[] outLine = new int[len];
		if (scaleY < 0) {
			int h1 = out.getHeight() - 1;
			if (scaleX < 0)
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(0, y, w, 1, inLine);
					for (int x = len - bands, xt = 0; x >= xt; x -= bands, xt += bands) {
						for (int k = 0; k < bands; k++) {
							outLine[xt + k] = inLine[x + k];
							outLine[x + k] = inLine[xt + k];
						}
					}
					out.setPixels(0, h1 - y, w, 1, outLine);
				}
			else
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(0, y, w, 1, inLine);
					out.setPixels(0, h1 - y, w, 1, inLine);
				}
		} else
			for (int y = to - 1; y >= from; y--) {
				src.getPixels(0, y, w, 1, inLine);
				for (int x = len - bands, xt = 0; x >= xt; x -= bands, xt += bands) {
					for (int k = 0; k < bands; k++) {
						outLine[xt + k] = inLine[x + k];
						outLine[x + k] = inLine[xt + k];
					}
				}
				out.setPixels(0, y, w, 1, outLine);
			}
	}

	private static void rotateImage(int from, int to, float scaleX, float scaleY, WritableRaster src,
			WritableRaster out) {
		int w = out.getWidth();
		int h = out.getHeight();
		int h1 = h - 1;
		int m = Math.max(w, h);
		int bands = src.getNumBands();
		int[] inLine = new int[m * bands];
		int[] outLine = new int[m * bands];
		int len = w * bands;
		if (scaleY < 0) {
			if (scaleX < 0)
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(y, 0, 1, w, inLine);
					for (int x = len - bands, xt = 0; x >= xt; x -= bands, xt += bands) {
						for (int k = 0; k < bands; k++) {
							outLine[xt + k] = inLine[x + k];
							outLine[x + k] = inLine[xt + k];
						}
					}
					out.setPixels(0, h1 - y, w, 1, outLine);
				}
			else
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(y, 0, 1, w, inLine);
					out.setPixels(0, h1 - y, w, 1, inLine);
				}
		} else {
			if (scaleX < 0)
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(y, 0, 1, w, inLine);
					for (int x = len - bands, xt = 0; x >= xt; x -= bands, xt += bands) {
						for (int k = 0; k < bands; k++) {
							outLine[xt + k] = inLine[x + k];
							outLine[x + k] = inLine[xt + k];
						}
					}
					out.setPixels(0, y, w, 1, outLine);
				}
			else
				for (int y = to - 1; y >= from; y--) {
					src.getPixels(y, 0, 1, w, inLine);
					out.setPixels(0, y, w, 1, inLine);
				}
		}
	}

	public static boolean testOnJpeg(byte[] data) {
		return (data.length > 2 && data[0] == 0xFF && data[1] == 0xD8);
	}

	public static byte[] asJpeg(byte[] bytes) {
		if (testOnJpeg(bytes))
			return bytes;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(is);
		} catch (IOException e) {
			// should never happen
		}
		if (bi == null)
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "jpeg", out); //$NON-NLS-1$
			return out.toByteArray();
		} catch (IOException e) {
			return null;
		}

	}

	private static final int LLJTRAN_ROT_90 = 5;
	private static final int LLJTRAN_ROT_180 = 6;
	private static final int LLJTRAN_ROT_270 = 7;
	private static final int FRAMEWIDTH = 3;
	private static final int MISSINGIMAGESIZE = 128;
	private static final int THRESHOLD_QUALITY_BALANCED = 800;
	private static final int THRESHOLD_BALANCED_SPEED = 1600;
	private static final int SPEED = 0;
	private static final int BALANCED = 1;
	private static final int QUALITY = 2;
	private static final int ULTRA_QUALITY = 3;

	/**
	 * Rotates a JPEG image losslessly. The image dimensions must be multiples of
	 * the MCU size
	 *
	 * @param angle
	 *            - rotation angle in degrees (0, 90, 180, 270)
	 * @param data
	 *            - JPEG image as byte array
	 * @return
	 */
	public static byte[] lljTran(int angle, byte[] data) {
		int op;
		switch (angle) {
		case 90:
			op = LLJTRAN_ROT_90;
			break;
		case 180:
			op = LLJTRAN_ROT_180;
			break;
		case 270:
			op = LLJTRAN_ROT_270;
			break;
		default:
			return data;
		}
		try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			// Initialize LLJTran and Read the entire Image including Appx
			// markers
			Log.debugLevel = Log.LEVEL_NONE;
			LLJTran llj = new LLJTran(in);
			// Read without Exif
			llj.read(LLJTran.READ_ALL, false);
			// Transform the image using default options along with
			// transformation of the Orientation tags. Try other
			// combinations of
			// LLJTran_XFORM.. flags. Use a jpeg with partial MCU
			// (partialMCU.jpg)
			// for testing LLJTran.XFORM_TRIM and LLJTran.XFORM_ADJUST_EDGES
			int options = LLJTran.OPT_XFORM_ADJUST_EDGES;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				// Save with vertical transformation without changing the
				// llj
				// image.
				llj.transform(out, op, options);
				return out.toByteArray();
			}
		} catch (LLJTranException e) {
			ImageActivator.getDefault().logError(Messages.Utilities_LLJTran_Error_rotating, e);
		} catch (IOException e) {
			ImageActivator.getDefault().logError(Messages.Utilities_IO_Error_rotating, e);
		}
		return null;
	}

	public static boolean saveBufferedImageToStream(BufferedImage bufferedImage, OutputStream out, int format,
			int quality) throws IOException {
		try (ImageOutputStream stream = ImageIO.createImageOutputStream(out)) {
			Iterator<ImageWriter> it = ImageIO.getImageWriters(
					ImageTypeSpecifier.createFromRenderedImage(bufferedImage),
					format == SWT.IMAGE_PNG ? "png" : format == ZImage.IMAGE_WEBP ? "webp" : "jpg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (it.hasNext()) {
				ImageWriter writer = it.next();
				writer.setOutput(stream);
				try {
					ImageWriteParam param = writer.getDefaultWriteParam();
					if (format == SWT.IMAGE_JPEG) {
						if (quality > 0) {
							param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
							param.setCompressionQuality(quality / 100f);
						}
					} else if (format == ZImage.IMAGE_WEBP) {
						if (quality > 0) {
							param.setCompressionType("Lossy"); //$NON-NLS-1$
							param.setCompressionQuality(quality / 100f);
						} else
							param.setCompressionType("Lossless"); //$NON-NLS-1$
					}
					writer.write(null, new IIOImage(bufferedImage, null, null), param);
					return true;
				} finally {
					writer.dispose();
					stream.flush();
				}
			}
			return false;
		}
	}

	/**
	 * Scales an SWT image
	 *
	 * @param image
	 *            - image to scale
	 * @param width
	 *            - max width - negative for min width
	 * @param height
	 *            - max height - negative for min height
	 * @param advanced
	 *            - use quality graphics
	 * @param raster
	 *            - enforce output width a multiple of raster
	 * @param dispose
	 *            - set to true to dispose source image
	 * @param frameColor
	 *            - Color for frame or null for no color
	 * @return scaled image
	 *
	 */
	public static Image scaleSWT(Image image, int width, int height, boolean advanced, int raster, boolean dispose,
			Color frameColor) {
		Rectangle bounds = image.getBounds();
		double scale = computeScale(bounds.width, bounds.height, width, height);
		int newWidth = (int) (bounds.width * scale + 0.5d);
		int newHeight = (int) (bounds.height * scale + 0.5d);
		if (raster > 0) {
			newWidth = (newWidth + raster / 2) / raster * raster;
			newHeight = (newHeight + raster / 2) / raster * raster;
		}
		if (bounds.width == newWidth && bounds.height == newHeight)
			return image;

		int interWidth = 4 * newWidth;
		int interHeight = 4 * newHeight;
		if (bounds.width > interWidth && bounds.height > interHeight) {
			Image intermediate = new Image(image.getDevice(), interWidth, interHeight);
			GC gc = null;
			try {
				gc = new GC(intermediate);
				gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, interWidth, interHeight);
			} finally {
				if (dispose)
					image.dispose();
				dispose = true;
				if (gc != null)
					gc.dispose();
			}
			image = intermediate;
			bounds = image.getBounds();
		}
		int owidth = bounds.width;
		int oheight = bounds.height;
		int x = 0;
		int y = 0;
		if (width < 0 || height < 0) {
			int w = Math.abs(width);
			int h = Math.abs(height);
			double f = computeScale(owidth, oheight, width, height);
			owidth = (int) (w / f + 0.5);
			oheight = (int) (h / f + 0.5);
			x = (bounds.width - owidth) / 2;
			y = (bounds.height - oheight) / 2;
			newWidth = w;
			newHeight = h;
		}
		int nw = newWidth;
		int nh = newHeight;
		int nx = 0;
		int ny = 0;
		if (frameColor != null) {
			nw = nh = Math.max(nw, nh) + 2 * FRAMEWIDTH;
			nx = (nw - newWidth) / 2;
			ny = (nh - newHeight) / 2;
		}
		Image thumbnail = new Image(image.getDevice(), nw, nh);
		GC gc = null;
		try {
			gc = new GC(thumbnail);
			if (advanced) {
				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);
			}
			if (frameColor != null) {
				gc.setBackground(frameColor);
				gc.fillRectangle(0, 0, nw, nh);
			}
			gc.drawImage(image, x, y, owidth, oheight, nx, ny, newWidth, newHeight);
			return thumbnail;
		} finally {
			if (dispose)
				image.dispose();
			if (gc != null)
				gc.dispose();
		}
	}

	/**
	 * Downsamples SWT image data
	 *
	 * @param original
	 *            - the original image data
	 * @param width
	 *            - max width - negative for min width
	 * @param height
	 *            - max height - negative for min height
	 * @param raster
	 *            - enforce output width a multiple of raster, 0 = no raster
	 * @return scaled image
	 */
	public static ImageData downSample(final ImageData original, int width, int height, int raster) {
		if (original == null)
			return null;
		int origWidth = original.width;
		int origHeight = original.height;
		double scale = computeScale(origWidth, origHeight, width, height);
		int newWidth = (int) (origWidth * scale + 0.5d);
		int newHeight = (int) (origHeight * scale + 0.5d);
		if (raster > 0) {
			int ra2 = raster / 2;
			newWidth = (newWidth + ra2) / raster * raster;
			newHeight = (newHeight + raster / 2) / raster * raster;
		}
		if (origWidth <= newWidth && origHeight <= newHeight)
			return original;

		final PaletteData palette = original.palette;
		PaletteData newPalette = palette;
		boolean isDirect = palette.isDirect;
		int depth = original.depth;
		if (!isDirect) {
			newPalette = new PaletteData(0xff, 0xff00, 0xff0000);
			depth = 24;
		}
		final ImageData newData = new ImageData(newWidth, newHeight, depth, newPalette);
		final int wx = origWidth / newWidth;
		final int wy = origHeight / newHeight;
		int incx = 1;
		int incy = 1;
		if (wx >= 4) {
			if (wx >= 9) {
				incx = wx / 3;
				if (incx > 4)
					incx = 4;
			} else
				incx = wx / 2;
		}
		if (wy >= 4) {
			if (wy >= 9) {
				incy = wy / 3;
				if (incy > 4)
					incy = 4;
			} else
				incy = wy / 2;
		}

		if (nP == 1)
			downSample(0, newHeight, original, newWidth, newHeight, palette, newData, wx, wy, incx, incy);
		else {
			final int incx1 = incx;
			final int incy1 = incy;
			final int newWidth1 = newWidth;
			final int newHeight1 = newHeight;
			IntStream.range(0, nP).parallel().forEach(p -> downSample(newHeight1 * p / nP, newHeight1 * (p + 1) / nP,
					original, newWidth1, newHeight1, palette, newData, wx, wy, incx1, incy1));
		}
		return newData;
	}

	private static void downSample(int from, int to, ImageData original, int newWidth, int newHeight,
			PaletteData palette, ImageData newData, int wx, int wy, int incx, int incy) {
		int r;
		int g;
		int b;
		int ly;
		int uy;
		int n;
		int ny;
		int ux1;
		int uy1;
		boolean isDirect = palette.isDirect;
		int origWidth = original.width;
		int origWidth_1 = origWidth - 1;
		int origHeight = original.height;
		int origHeight_1 = origHeight - 1;
		int wx2 = wx / 2;
		int wy2 = wy / 2;
		int sampleSize = incx * incy;
		PaletteData newPalette = newData.palette;
		int redMask = newPalette.redMask;
		int greenMask = newPalette.greenMask;
		int blueMask = newPalette.blueMask;
		int redShift = newPalette.redShift;
		int greenShift = newPalette.greenShift;
		int blueShift = newPalette.blueShift;
		int tx = from * newWidth * 3;
		int[] lxi = new int[newWidth];
		int[] uxi = new int[newWidth];
		int[] nxi = new int[newWidth];
		for (int x = 0; x < newWidth; x++) {
			lxi[x] = (x * origWidth / newWidth) - wx2;
			uxi[x] = lxi[x] + wx;
			if (lxi[x] < 0) {
				lxi[x] = 0;
				if (uxi[x] < 1)
					uxi[x] = 1;
			} else if (uxi[x] > origWidth) {
				uxi[x] = origWidth;
				if (lxi[x] >= origWidth)
					lxi[x] = origWidth_1;
			}
			nxi[x] = ((uxi[x] - lxi[x]) / incx);
		}
		int bytesPerLine = newData.bytesPerLine;
		for (int y = from; y < to; y++) {
			ly = (y * origHeight / newHeight) - wy2;
			uy = ly + wy;
			if (ly < 0) {
				ly = 0;
				if (uy < 1)
					uy = 1;
			} else if (uy > origHeight) {
				uy = origHeight;
				if (ly >= origHeight)
					ly = origHeight_1;
			}
			ny = ((uy - ly) / incy);
			uy1 = uy - incy;
			tx = bytesPerLine * y;
			for (int x = 0; x < newWidth; x++) {
				n = nxi[x] * ny;
				r = g = b = n / 2;
				if (sampleSize == 1)
					for (int oy = ly; oy < uy; oy++)
						for (int ox = lxi[x]; ox < uxi[x]; ox++) {
							int pixel = original.getPixel(ox, oy);
							if (!isDirect) {
								RGB rgb = palette.getRGB(pixel);
								r += rgb.red;
								g += rgb.green;
								b += rgb.blue;
							} else if (pixel != 0) {
								r += ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
								g += ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
										: (pixel & greenMask) << greenShift);
								b += ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
										: (pixel & blueMask) << blueShift);
							}
						}
				else {
					ux1 = uxi[x] - incx;
					for (int oy = ly; oy <= uy1; oy += incy)
						for (int ox = lxi[x]; ox <= ux1; ox += incx) {
							int rr = randomNumberGenerator.nextInt(sampleSize);
							int pixel = original.getPixel(ox + rr % incx, oy + rr / incx);
							if (!isDirect) {
								RGB rgb = palette.getRGB(pixel);
								r += rgb.red;
								g += rgb.green;
								b += rgb.blue;
							} else if (pixel != 0) {
								r += ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
								g += ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
										: (pixel & greenMask) << greenShift);
								b += ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
										: (pixel & blueMask) << blueShift);
							}
						}
				}
				newData.data[tx++] = (byte) (b /= n);
				newData.data[tx++] = (byte) (g /= n);
				newData.data[tx++] = (byte) (r /= n);
			}
		}
	}

	private static int determineScalingMethod(int size, boolean advanced) {
		if (size <= THRESHOLD_QUALITY_BALANCED)
			return advanced ? ULTRA_QUALITY : QUALITY;
		if (size <= THRESHOLD_BALANCED_SPEED)
			return advanced ? QUALITY : BALANCED;
		return advanced ? QUALITY : SPEED;
	}

	/**
	 * Downsamples SWT image data
	 *
	 * @param original
	 *            - the original image data
	 * @param width
	 *            - max width - negative for min width
	 * @param height
	 *            - max height - negative for min height
	 * @param raster
	 *            - enforce output width a multiple of raster
	 * @param advanced
	 *            - true for better interpolation
	 * @return scaled image
	 */
	public static BufferedImage downSample(BufferedImage original, int width, int height, int raster,
			boolean advanced) {
		if (original == null)
			return null;

		// long time = System.currentTimeMillis();
		int origWidth = original.getWidth();
		int origHeight = original.getHeight();
		double scale = computeScale(origWidth, origHeight, width, height);
		int newWidth = (int) (origWidth * scale + 0.5d);
		int newHeight = (int) (origHeight * scale + 0.5d);
		if (raster > 0) {
			newWidth = (newWidth + (raster / 2)) / raster * raster;
			newHeight = (newHeight + raster / 2) / raster * raster;
		}
		if (origWidth <= newWidth && origHeight <= newHeight)
			return original;
		int scalingMethod = determineScalingMethod(Math.max(newWidth, newHeight), advanced);
		BufferedImage scaledImage = null;
		if (scalingMethod == SPEED)
			scaledImage = scaleImage(original, newWidth, newHeight,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		else if (scalingMethod == BALANCED)
			scaledImage = scaleImage(original, newWidth, newHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		else
			scaledImage = scaleImageIncrementally(original, newWidth, newHeight, scalingMethod,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		original.flush();
		return scaledImage;
	}

	private static BufferedImage scaleImage(BufferedImage src, int targetWidth, int targetHeight,
			Object interpolationHintValue) {
		ColorModel colorModel = src.getColorModel();
		WritableRaster raster = colorModel.createCompatibleWritableRaster(targetWidth, targetHeight);
		BufferedImage result = new BufferedImage(colorModel, raster, false, null);
		Graphics2D resultGraphics = result.createGraphics();
		resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHintValue);
		resultGraphics.drawImage(src, 0, 0, targetWidth, targetHeight, null);
		resultGraphics.dispose();
		return result;
	}

	protected static BufferedImage scaleImageIncrementally(BufferedImage src, int targetWidth, int targetHeight,
			int scalingMethod, Object interpolationHintValue) {
		boolean hasReassignedSrc = false;
		int currentWidth = src.getWidth();
		int currentHeight = src.getHeight();
		int fraction = (scalingMethod == ULTRA_QUALITY ? 7 : 2);
		do {
			int prevCurrentWidth = currentWidth;
			int prevCurrentHeight = currentHeight;
			if (currentWidth > targetWidth) {
				currentWidth -= (currentWidth / fraction);
				if (currentWidth < targetWidth)
					currentWidth = targetWidth;
			}
			if (currentHeight > targetHeight) {
				currentHeight -= (currentHeight / fraction);
				if (currentHeight < targetHeight)
					currentHeight = targetHeight;
			}
			if (prevCurrentWidth == currentWidth && prevCurrentHeight == currentHeight)
				break;
			BufferedImage incrementalImage = scaleImage(src, currentWidth, currentHeight, interpolationHintValue);
			if (hasReassignedSrc)
				src.flush();
			src = incrementalImage;
			hasReassignedSrc = true;
		} while (currentWidth != targetWidth || currentHeight != targetHeight);
		return src;
	}

	public static BufferedImage convertToDirectColor(final BufferedImage bufferedImage, boolean forceSRGB) {
		ColorModel colorModel = bufferedImage.getColorModel();
		if (colorModel instanceof DirectColorModel && colorModel.getNumComponents() == 3)
			return bufferedImage;
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		BufferedImage result = forceSRGB || colorModel.getColorSpace().getType() == ColorSpace.TYPE_CMYK
				? new BufferedImage(w, h,
						(bufferedImage.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
								: BufferedImage.TYPE_INT_ARGB))
				: new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(w, h), false, null);
		// Render the src image into our new optimal source.
		Graphics g = result.getGraphics();
		g.drawImage(bufferedImage, 0, 0, null); //TODO dead slow
		g.dispose();
		bufferedImage.flush();
		return result;

	}

	public static ImageData convertToDirectColor(final ImageData data) {
		PaletteData palette = data.palette;
		if (palette.isDirect)
			return data;
		final int w = data.width;
		int h = data.height;
		int tPixel = data.transparentPixel;
		final RGB[] rgBs = palette.getRGBs();
		int colors[] = new int[rgBs.length];
		for (int i = 0; i < rgBs.length; i++) {
			RGB rgb = rgBs[i];
			colors[i] = rgb.red | rgb.green << 8 | rgb.blue << 16;
		}
		final ImageData copy = new ImageData(w, h, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
		if (nP == 1)
			convertToDirectColor(0, h, data, w, colors, copy, tPixel);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> convertToDirectColor(h * p / nP, h * (p + 1) / nP, data, w, colors, copy, tPixel));
		return copy;
	}

	private static void convertToDirectColor(int from, int to, ImageData data, int w, int[] colors, ImageData copy,
			int tPixel) {
		int rgblen = colors.length;
		int[] scanLine = new int[w];
		if (tPixel >= 0) {
			byte[] alphas = new byte[w];
			for (int y = from; y < to; y++) {
				data.getPixels(0, y, w, scanLine, 0);
				for (int x = 0; x < scanLine.length; x++) {
					int index = scanLine[x];
					if (index >= 0 && index < rgblen)
						scanLine[x] = colors[index];
					alphas[x] = tPixel == index ? 0 : (byte) 255;
				}
				copy.setPixels(0, y, w, scanLine, 0);
				copy.setAlphas(0, y, w, alphas, 0);
			}
		} else
			for (int y = from; y < to; y++) {
				data.getPixels(0, y, w, scanLine, 0);
				for (int x = 0; x < scanLine.length; x++) {
					int index = scanLine[x];
					if (index >= 0 && index < rgblen)
						scanLine[x] = colors[index];
				}
				copy.setPixels(0, y, w, scanLine, 0);
			}
	}

	/**
	 * Computes the scaling factor for scaling an image
	 *
	 * @param iWidth
	 *            - original image width
	 * @param iHeight
	 *            - original image height
	 * @param width
	 *            - desired image width - negative for min width.
	 * @param height
	 *            - desired image height - negative for min height.
	 * @return computed scaling factor
	 */
	public static double computeScale(int iWidth, int iHeight, int width, int height) {
		int w = Math.abs(width);
		int h = Math.abs(height);
		boolean oriImage = iWidth > iHeight;
		boolean oriThumb = w > h;
		if (oriImage != oriThumb) {
			double f = (double) Math.min(w, h) / Math.max(w, h);
			int ww = w;
			w = (int) (h * f + 0.5d);
			h = (int) (ww * f + 0.5d);
		}
		double xscale = (double) w / iWidth;
		double yscale = (double) h / iHeight;
		return (width < 0 || height < 0) ? Math.max(xscale, yscale) : Math.min(xscale, yscale);
	}

	/**
	 * Converts a buffered image to an SWT image
	 *
	 * @param bufferedImage
	 *            - input buffered image
	 * @return - SWT image data
	 */
	public static ImageData bufferedImage2swt(BufferedImage bufferedImage) {
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		ColorModel model = bufferedImage.getColorModel();
		if (model instanceof DirectColorModel) {
			final PaletteData palette = new PaletteData(0xff, 0xff00, 0xff0000);
			final ImageData data = new ImageData(w, h, 24, palette);
			final WritableRaster raster = bufferedImage.getRaster();
			if (nP == 1)
				fromDirectOrComponentModel(0, h, data, raster, model);
			else
				IntStream.range(0, nP).parallel()
						.forEach(p -> fromDirectOrComponentModel(h * p / nP, h * (p + 1) / nP, data, raster, model));
			return data;
		}
		if (model instanceof IndexColorModel) {
			/*
			 * We convert to direct color model because of a bug in the SWT ImageData
			 * constructor for indexed colors with a depth of 16
			 */
			IndexColorModel colorModel = (IndexColorModel) model;
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			final PaletteData palette = new PaletteData(0xff, 0xff00, 0xff0000);
			final ImageData data = new ImageData(w, h, 24, palette);
			final WritableRaster raster = bufferedImage.getRaster();
			int transparentPixel = colorModel.getTransparentPixel();
			if (nP == 1)
				fromIndexedModel(0, h, data, raster, transparentPixel, reds, greens, blues);
			else
				IntStream.range(0, nP).parallel().forEach(p -> fromIndexedModel(h * p / nP, h * (p + 1) / nP, data,
						raster, transparentPixel, reds, greens, blues));
			return data;
		}
		if (model instanceof ComponentColorModel) {
			PaletteData palette = new PaletteData(0xff, 0xff00, 0xff0000);
			final ImageData data = new ImageData(w, h, 24, palette);
			if (model.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
				if (nP == 1)
					fromCMYKComponentModel(0, h, data, bufferedImage);
				else
					IntStream.range(0, nP).parallel()
							.forEach(p -> fromCMYKComponentModel(h * p / nP, h * (p + 1) / nP, data, bufferedImage));
			} else {
				final WritableRaster raster = bufferedImage.getRaster();
				if (nP == 1)
					fromDirectOrComponentModel(0, h, data, raster, model);
				else
					IntStream.range(0, nP).parallel().forEach(
							p -> fromDirectOrComponentModel(h * p / nP, h * (p + 1) / nP, data, raster, model));
			}
			return data;
		}
		return null;
	}

	private static void fromCMYKComponentModel(int from, int to, ImageData data, BufferedImage bufferedImage) {
		int w = data.width;
		int tx;
		int[] scanLine = new int[w];
		byte[] datadata = data.data;
		int bytesPerLine = data.bytesPerLine;
		for (int y = from; y < to; y++) {
			tx = bytesPerLine * y;
			bufferedImage.getRGB(0, y, w, 1, scanLine, 0, w);
			for (int i : scanLine) {
				int pix = scanLine[i];
				datadata[tx++] = (byte) pix;
				datadata[tx++] = (byte) (pix >> 8);
				datadata[tx++] = (byte) (pix >> 16);
			}
		}
	}

	private static void fromDirectOrComponentModel(int from, int to, ImageData data, WritableRaster raster,
			ColorModel model) {
		boolean hasAlpha = model.hasAlpha();
		int bands = raster.getNumBands();
		int nColors = bands - (hasAlpha ? 1 : 0);
		int w = data.width;
		int bytesPerLine = data.bytesPerLine;
		int tx;
		int[] nbits = model.getComponentSize();
		byte[] datadata = data.data;
		switch (nColors) {
		case 1: {
			DataBuffer dataBuffer = raster.getDataBuffer();
			if (dataBuffer instanceof DataBufferUShort) {
				int shift = nbits[0] - 8;
				if (hasAlpha) {
					int alphashift = nbits[1] - 8;
					byte[] alphas = new byte[w];
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++) {
							datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) ((raster.getSample(x, y,
									0) >> shift));
							alphas[x] = (byte) (raster.getSample(x, y, 1) >> alphashift);
						}
						data.setAlphas(0, y, w, alphas, 0);
					}
				} else
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++)
							datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) ((raster.getSample(x, y,
									0) >> shift));
					}
			} else {
				int shift = 8 - nbits[0];
				int[] pixelArray = new int[w * bands];
				if (hasAlpha) {
					byte[] alphas = new byte[w];
					int alphashift = 8 - nbits[1];
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						raster.getPixels(0, y, w, 1, pixelArray);
						for (int x = 0, xi = 0; x < w; x++) {
							datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) (shift < 0
									? (pixelArray[xi++] >> (-shift))
									: (pixelArray[xi++] << shift));
							alphas[x] = (byte) (alphashift < 0 ? pixelArray[xi++] >> -alphashift
									: pixelArray[xi++] << alphashift);
						}
						data.setAlphas(0, y, w, alphas, 0);
					}
				} else
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						raster.getPixels(0, y, w, 1, pixelArray);
						for (int x = 0, xi = 0; x < w; x++)
							datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) (shift < 0
									? (pixelArray[xi++] >> (-shift))
									: (pixelArray[xi++] << shift));
					}
			}
			break;
		}
		case 2: {
			int shift = 8 - nbits[0] - 1;
			int[] pixelArray = new int[w * bands];
			if (hasAlpha) {
				int alphashift = 8 - nbits[2];
				byte[] alphas = new byte[w];
				for (int y = from; y < to; y++) {
					tx = bytesPerLine * y;
					raster.getPixels(0, y, w, 1, pixelArray);
					for (int x = 0, xi = 0; x < w; x++) {
						int v = pixelArray[xi++] + pixelArray[xi++];
						datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) (shift < 0 ? (v >> (-shift))
								: (v << shift));
						alphas[x] = (byte) (alphashift < 0 ? pixelArray[xi++] >> -alphashift
								: pixelArray[xi++] << alphashift);
					}
					data.setAlphas(0, y, w, alphas, 0);
				}
			} else
				for (int y = from; y < to; y++) {
					tx = bytesPerLine * y;
					raster.getPixels(0, y, w, 1, pixelArray);
					for (int x = 0, xi = 0; x < w; x++) {
						int v = pixelArray[xi++] + pixelArray[xi++];
						datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) (shift < 0 ? (v >> (-shift))
								: (v << shift));
					}
				}
			break;
		}
		case 3: {
			DataBuffer dataBuffer = raster.getDataBuffer();
			if (dataBuffer instanceof DataBufferFloat) {
				if (hasAlpha) {
					byte[] alphas = new byte[w];
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++) {
							datadata[tx++] = (byte) (255f * raster.getSampleFloat(x, y, 2) + 0.5f);
							datadata[tx++] = (byte) (255f * raster.getSampleFloat(x, y, 1) + 0.5f);
							datadata[tx++] = (byte) (255f * raster.getSampleFloat(x, y, 0) + 0.5f);
							alphas[x] = (byte) (255f * raster.getSampleFloat(x, y, 3) + 0.5f);
						}
						data.setAlphas(0, y, w, alphas, 0);
					}
				} else
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++) {
							datadata[tx++] = (byte) (int) (255f * raster.getSampleFloat(x, y, 2) + 0.5f);
							datadata[tx++] = (byte) (int) (255f * raster.getSampleFloat(x, y, 1) + 0.5f);
							datadata[tx++] = (byte) (int) (255f * raster.getSampleFloat(x, y, 0) + 0.5f);
						}
					}
			} else if (dataBuffer instanceof DataBufferDouble) {
				if (hasAlpha) {
					byte[] alphas = new byte[w];
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++) {
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 2) + 0.5f);
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 1) + 0.5f);
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 0) + 0.5f);
							alphas[x] = (byte) (255d * raster.getSampleDouble(x, y, 3) + 0.5f);
						}
						data.setAlphas(0, y, w, alphas, 0);
					}
				} else
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						for (int x = 0; x < w; x++) {
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 0) + 0.5f);
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 1) + 0.5f);
							datadata[tx++] = (byte) (255d * raster.getSampleDouble(x, y, 2) + 0.5f);
						}
					}
			} else {
				int redshift = nbits[0] - 8;
				int greenshift = nbits[1] - 8;
				int blueshift = nbits[2] - 8;
				int[] pixelArray = new int[bands * w];
				if (redshift == 0 && greenshift == 0 && blueshift == 0 && !hasAlpha) {
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						raster.getPixels(0, y, w, 1, pixelArray);
						for (int x = 0, xi = 0; x < w; x++) {
							byte red = (byte) pixelArray[xi++];
							byte green = (byte) pixelArray[xi++];
							datadata[tx++] = (byte) pixelArray[xi++];
							datadata[tx++] = green;
							datadata[tx++] = red;
						}
					}
				} else if (hasAlpha) {
					byte[] alphas = new byte[w];
					int alphashift = nbits[3] - 8;
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						raster.getPixels(0, y, w, 1, pixelArray);
						for (int x = 0, xi = 0; x < w; x++) {
							byte red = (byte) (redshift > 0 ? pixelArray[xi++] >> redshift
									: pixelArray[xi++] << -redshift);
							byte green = (byte) (greenshift > 0 ? pixelArray[xi++] >> greenshift
									: pixelArray[xi++] << -greenshift);
							datadata[tx++] = (byte) (blueshift > 0 ? pixelArray[xi++] >> blueshift
									: pixelArray[xi++] << -blueshift);
							datadata[tx++] = green;
							datadata[tx++] = red;
							alphas[x] = (byte) (alphashift > 0 ? pixelArray[xi++] >> alphashift
									: pixelArray[xi++] << -alphashift);
						}
						data.setAlphas(0, y, w, alphas, 0);
					}
				} else
					for (int y = from; y < to; y++) {
						tx = bytesPerLine * y;
						raster.getPixels(0, y, w, 1, pixelArray);
						for (int x = 0, xi = 0; x < w; x++) {
							byte red = (byte) (redshift > 0 ? pixelArray[xi++] >> redshift
									: pixelArray[xi++] << -redshift);
							byte green = (byte) (greenshift > 0 ? pixelArray[xi++] >> greenshift
									: pixelArray[xi++] << -greenshift);
							datadata[tx++] = (byte) (blueshift > 0 ? pixelArray[xi++] >> blueshift
									: pixelArray[xi++] << -blueshift);
							datadata[tx++] = green;
							datadata[tx++] = red;
						}
					}
			}
			break;
		}
		}
	}

	private static void fromIndexedModel(int from, int to, ImageData data, WritableRaster raster, int transparentPixel,
			byte[] reds, byte[] greens, byte[] blues) {
		int w = data.width;
		int bytesPerLine = data.bytesPerLine;
		int tx;
		int[] pixel = new int[1];
		if (transparentPixel >= 0) {
			byte[] alphas = new byte[w];
			for (int y = from; y < to; y++) {
				tx = bytesPerLine * y;
				for (int x = 0; x < w; x++) {
					raster.getPixel(x, y, pixel);
					int index = pixel[0];
					alphas[x] = (byte) (index == transparentPixel ? 0 : 255);
					data.data[tx++] = blues[index];
					data.data[tx++] = greens[index];
					data.data[tx++] = reds[index];
				}
				data.setAlphas(0, y, w, alphas, 0);
			}
		} else
			for (int y = from; y < to; y++) {
				tx = bytesPerLine * y;
				for (int x = 0; x < w; x++) {
					raster.getPixel(x, y, pixel);
					int index = pixel[0];
					data.data[tx++] = blues[index];
					data.data[tx++] = greens[index];
					data.data[tx++] = reds[index];
				}
			}
	}

	/**
	 * Converts an SWT image to a buffered image
	 *
	 * @param data
	 *            - SWT image data
	 * @return - output buffered image
	 */
	public static BufferedImage swtImage2buffered(final ImageData data, ColorSpace colorSpace) {
		int w = data.width;
		int h = data.height;
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(colorSpace, data.depth, palette.redMask, palette.greenMask,
					palette.blueMask, 0, false, DataBuffer.TYPE_INT);
			BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(w, h),
					false, null);
			final WritableRaster raster = bufferedImage.getRaster();
			if (nP == 1)
				directToBuffered(0, h, data, raster);
			else
				IntStream.range(0, nP).parallel()
						.forEach(p -> directToBuffered(h * p / nP, h * (p + 1) / nP, data, raster));
			return bufferedImage;
		}
		RGB[] rgbs = palette.getRGBs();
		int tPixel = data.transparentPixel;
		int[] colors = new int[rgbs.length];
		for (int i = 0; i < rgbs.length; i++) {
			RGB rgb = rgbs[i];
			colors[i] = (rgb.red << 16) | (rgb.green << 8) | rgb.blue | (tPixel < 0 || tPixel == i ? 0 : 0xff000000);
		}
		colorModel = new DirectColorModel(colorSpace, data.depth, 0xff0000, 0xff00, 0xff, tPixel >= 0 ? 0xff000000 : 0,
				false, DataBuffer.TYPE_INT);
		BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(w, h),
				false, null);
		final WritableRaster raster = bufferedImage.getRaster();
		if (nP == 1)
			indexedToBuffered(0, h, data, raster, colors);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> indexedToBuffered(h * p / nP, h * (p + 1) / nP, data, raster, colors));
		return bufferedImage;
	}

	private static void indexedToBuffered(int from, int to, final ImageData data, WritableRaster raster, int[] colors) {
		int rgblen = colors.length;
		int w = data.width;
		int[] scanLine = new int[w];
		for (int y = from; y < to; y++) {
			data.getPixels(0, y, w, scanLine, 0);
			for (int x = 0; x < scanLine.length; x++) {
				int index = scanLine[x];
				if (index >= 0 && index < rgblen)
					scanLine[x] = colors[index];
			}
			raster.setPixels(0, y, w, 1, scanLine);
		}
	}

	private static void directToBuffered(int from, int to, ImageData data, WritableRaster raster) {
		byte[] datadata = data.data;
		int bytesPerLine = data.bytesPerLine;
		int sx;
		// PaletteData palette = data.palette;
		// int redMask = palette.redMask;
		// int greenMask = palette.greenMask;
		// int blueMask = palette.blueMask;
		// int redShift = palette.redShift;
		// int greenShift = palette.greenShift;
		// int blueShift = palette.blueShift;
		int bands = raster.getNumBands();
		int w = data.width;
		int[] pixelArray = new int[w * bands];
		// int[] scanLine = new int[w];
		// int pixel;
		for (int y = from; y < to; y++) {
			sx = bytesPerLine * y;
			// data.getPixels(0, y, w, scanLine, 0);
			for (int x = 0, k = 0; x < w; x++, k += bands) {
				pixelArray[k + 2] = datadata[sx++];
				pixelArray[k + 1] = datadata[sx++];
				pixelArray[k] = datadata[sx++];
				// pixel = scanLine[x];
				// pixelArray[k] = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel &
				// redMask) << redShift);
				// pixelArray[k + 1] = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
				// : (pixel & greenMask) << greenShift);
				// pixelArray[k + 2] = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift
				// : (pixel & blueMask) << blueShift);
			}
			raster.setPixels(0, y, w, 1, pixelArray);
		}
	}

	/**
	 * Loads a thumbnail image from the supplied image data byte array
	 *
	 * @param device
	 *            - device for which the image is created
	 * @param thumbnailData
	 *            - image data as byte array (e.g. JPEG image data)
	 * @param cms
	 *            - the output color space
	 * @param formatHint
	 *            - SWT image format constant or SWT.DEFAULT
	 * @param dflt
	 *            - true if a default image is to be generated when image loading
	 *            fails
	 * @return - output image
	 */
	public static Image loadThumbnail(Device device, byte[] thumbnailData, int cms, int formatHint, boolean dflt) {
		try {
			ImageData data = loadThumbnailData(thumbnailData, cms, formatHint);
			if (data != null)
				return new Image(device, data);
		} catch (Exception e) {
			// ignore
		}
		return dflt ? createDefaultImage(device) : null;
	}

	/**
	 * Loads thumbnail image data from the supplied image data byte array
	 *
	 * @param thumbnailData
	 *            - image data as byte array (e.g. JPEG image data)
	 * @param cms
	 *            - the output color space
	 * @param formatHint
	 *            - SWT image format constant or SWT.DEFAULT
	 * @return -image data
	 */
	public static ImageData loadThumbnailData(byte[] thumbnailData, int cms, int formatHint) {
		if (thumbnailData != null) {
			ByteArrayInputStream is = new ByteArrayInputStream(thumbnailData);
			try {
				if (cms != ImageConstants.NOCMS && cms != ImageConstants.SRGB) {
					BufferedImage bi = ImageIO.read(is);
					if (bi == null)
						return null;
					ImageActivator.getDefault().getCOLORCONVERTOP_SRGB2ICC(cms).filter(bi, bi);
					return bufferedImage2swt(bi);
				}
				ImageData[] iData = new ImageLoader().load(is, formatHint);
				if (iData != null && iData.length > 0)
					return iData[0];
			} catch (Exception e) {
				try {
					is.reset();
					return bufferedImage2swt(ImageIO.read(is));
				} catch (Exception e1) {
					// ignore
				}
			}
		}
		return null;
	}

	private static Image createDefaultImage(Device device) {
		Image image = new Image(device, MISSINGIMAGESIZE, MISSINGIMAGESIZE);
		GC gc = new GC(image);
		gc.setBackground(device.getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.fillRectangle(0, 0, MISSINGIMAGESIZE, MISSINGIMAGESIZE);
		gc.setForeground(device.getSystemColor(SWT.COLOR_RED));
		String text = Messages.Utilities_No_thumbnail_image;
		Point p = gc.textExtent(text);
		gc.drawText(text, (MISSINGIMAGESIZE - p.x) / 2, (MISSINGIMAGESIZE - p.y) / 2, true);
		gc.dispose();
		return image;
	}

	/**
	 * Saves an image as JPEG file
	 *
	 * @param image
	 *            - input image
	 * @param filename
	 *            - path of output file
	 */
	public static void saveSwtImageAsJpg(ImageData image, String filename) {
		ImageLoader swtLoader = new ImageLoader();
		swtLoader.data = new ImageData[] { image };
		swtLoader.save(filename, SWT.IMAGE_JPEG);
	}

	/**
	 * Adds a watermark to an SWT image
	 *
	 * @param image
	 *            - input image
	 * @param copyright
	 *            - copyright remark without the copyright character
	 * @return - output image
	 */
	public static Image addWatermark(Image image, String copyright) {
		if (copyright != null) {
			File watermarkFile = toFile(copyright);
			if (watermarkFile != null) {
				final Rectangle bounds = image.getBounds();
				final Device device = image.getDevice();
				Image outputImage = new Image(device, bounds);
				final GC gc = new GC(outputImage);
				// gc.setAdvanced(true);
				gc.drawImage(image, 0, 0);
				ImageLoader loader = new ImageLoader();
				ImageData[] data = loader.load(watermarkFile.getAbsolutePath());
				Image watermark = new Image(device, data[0]);
				Rectangle wbounds = watermark.getBounds();
				double f = (double) bounds.width / (3 * wbounds.width);
				gc.setAlpha(144);
				int w = (int) (f * wbounds.width);
				int x = bounds.x + bounds.width - w;
				int h = (int) (f * wbounds.height);
				int y = bounds.y + bounds.height - h;
				gc.drawImage(watermark, 0, 0, wbounds.width, wbounds.height, x, y, w, h);
				watermark.dispose();
				return outputImage;
			}
			if (copyright.startsWith("©")) //$NON-NLS-1$
				copyright = copyright.substring(1).trim();
			if (!copyright.isEmpty()) {
				final String text = "© " + copyright; //$NON-NLS-1$
				final Rectangle bounds = image.getBounds();
				final Device device = image.getDevice();
				Image outputImage = new Image(device, bounds);
				final GC gc = new GC(outputImage);
				// gc.setAdvanced(true);
				gc.drawImage(image, 0, 0);
				Display display = (device instanceof Display) ? (Display) device : Display.getDefault();
				display.syncExec(() -> {
					Font systemFont = device.getSystemFont();
					gc.setFont(systemFont);
					Point tx = gc.textExtent(text);
					double f = (double) bounds.width / (3 * tx.x);
					FontData fontData = systemFont.getFontData()[0];
					int h = (int) (fontData.getHeight() * f + 0.5d);
					fontData.setHeight(h);
					fontData.setStyle(SWT.BOLD);
					Font font = new Font(device, fontData);
					gc.setFont(font);
					tx = gc.textExtent(text);
					gc.setAlpha(144);
					gc.setForeground(device.getSystemColor(SWT.COLOR_WHITE));
					int x = bounds.x + (bounds.width - tx.x) / 2;
					int y = bounds.y + bounds.height - 2 * tx.y;
					gc.drawText(text, x - 1, y - 1, true);
					gc.setForeground(device.getSystemColor(SWT.COLOR_DARK_GRAY));
					gc.drawText(text, x + 1, y + 1, true);
					font.dispose();
				});
				gc.dispose();
				return outputImage;
			}
		}
		return image;
	}

	private static File toFile(String copyright) {
		if (copyright.endsWith(".png") || copyright.endsWith(".bmp")) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				File file = new File(copyright);
				if (file.exists())
					return file;
			} catch (Exception e) {
				// no file
			}
		}
		return null;
	}

	/**
	 * Converts SWT image data to black&white
	 *
	 * @param data
	 *            - image to be converted
	 * @param bwmode
	 */
	public static void convert2Bw(ImageData data, RGB bwmode) {
		int redFilter = bwmode.red;
		int greenFilter = bwmode.green;
		int blueFilter = bwmode.blue;
		int fsum = redFilter + greenFilter + blueFilter;
		int fsum2 = fsum / 2;
		PaletteData palette = data.palette;
		int w = data.width;
		// int[] scanLine = new int[w];
		if (palette.isDirect) {
			byte[] datadata = data.data;
			int bytesPerLine = data.bytesPerLine;
			int tx;
			// int redMask = palette.redMask;
			// int greenMask = palette.greenMask;
			// int blueMask = palette.blueMask;
			// int redShift = palette.redShift;
			// int greenShift = palette.greenShift;
			// int blueShift = palette.blueShift;
			// int pixel;
			// int grey;
			for (int y = 0; y < data.height; y++) {
				tx = bytesPerLine * y;
				// data.getPixels(0, y, w, scanLine, 0);
				for (int x = 0; x < w; x++) {
					datadata[tx++] = datadata[tx++] = datadata[tx++] = (byte) ((fsum2
							+ (datadata[tx + 2] & 0xff) * redFilter + (datadata[tx + 1] & 0xff) * greenFilter
							+ (datadata[tx] & 0xff) * blueFilter) / fsum);
					// pixel = scanLine[x];
					// grey = ((redShift < 0 ? (pixel & redMask) >>> -redShift : (pixel & redMask)
					// << redShift) * redFilter
					// + (greenShift < 0 ? (pixel & greenMask) >>> -greenShift : (pixel & greenMask)
					// << greenShift)
					// * greenFilter
					// + (blueShift < 0 ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) <<
					// blueShift)
					// * blueFilter)
					// / fsum;
					// scanLine[x] = (redShift < 0 ? grey << -redShift : grey >>> redShift) &
					// redMask
					// | ((greenShift < 0 ? grey << -greenShift : grey >>> greenShift) & greenMask)
					// | ((blueShift < 0 ? grey << -blueShift : grey >>> blueShift) & blueMask);
				}
				// data.setPixels(0, y, w, scanLine, 0);
			}
		} else
			for (RGB rgb : palette.getRGBs())
				rgb.red = rgb.green = rgb.blue = (fsum2 + rgb.red * redFilter + rgb.green * greenFilter + rgb.blue * blueFilter)
						/ fsum;
	}

	private static final int IFD_ENTRY_SIZE = 12;
	private static final int TYPE_SHORT = 3;
	private static final int TYPE_LONG = 4;

	public static InputStream extractXMP(String uri) throws IOException, URISyntaxException {
		InputStream xmpIn = null;
		File file = new File(new URI(uri));
		try (RandomAccessFile raFile = new RandomAccessFile(file.getAbsolutePath(), "r")) { //$NON-NLS-1$
			byte[] header = new byte[8];
			raFile.read(header);
			if (header[0] != header[1])
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			if (!(header[0] == 0x49 && header[2] == 42 && header[3] == 0)
					&& !(header[0] == 0x4d && header[2] == 0 && header[3] == 42)) {
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			}
			boolean isLittleEndian = header[0] == 0x49;
			int offset = toInt(header, 4, TYPE_LONG, isLittleEndian);
			raFile.seek(offset);
			byte[] buffer = new byte[2];
			raFile.read(buffer);
			int numberEntries = toInt(buffer, 0, TYPE_SHORT, isLittleEndian);
			buffer = new byte[IFD_ENTRY_SIZE * numberEntries];
			raFile.read(buffer);
			int count, eoff, len;
			for (int off = 0; off < buffer.length; off += IFD_ENTRY_SIZE) {
				int tag = toInt(buffer, off, TYPE_SHORT, isLittleEndian);
				if (tag == 700) {
					count = toInt(buffer, off + 4, TYPE_LONG, isLittleEndian);
					eoff = toInt(buffer, off + 8, TYPE_LONG, isLittleEndian);
					byte[] ebuffer = new byte[count];
					raFile.seek(eoff);
					len = raFile.read(ebuffer);
					if (len < count)
						throw new IOException("end of file reached"); //$NON-NLS-1$
					xmpIn = new ByteArrayInputStream(ebuffer);
					break;
				}
			}
		}
		return xmpIn;
	}

	private static int toInt(byte[] buffer, int i, int type, boolean isLittleEndian) {
		if (type == TYPE_LONG) {
			return isLittleEndian
					? (buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8) | ((buffer[i + 2] & 0xFF) << 16)
							| ((buffer[i + 3] & 0xFF) << 24)
					: (buffer[i + 3] & 0xFF) | ((buffer[i + 2] & 0xFF) << 8) | ((buffer[i + 1] & 0xFF) << 16)
							| ((buffer[i] & 0xFF) << 24);
		}
		if (type == TYPE_SHORT) {
			return isLittleEndian ? (buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8)
					: (buffer[i + 1] & 0xFF) | ((buffer[i] & 0xFF) << 8);
		}
		return -1;
	}

	public static BufferedImage convert2Bw(BufferedImage image, final RGB filter) {
		ColorModel model = image.getColorModel();
		if (!(model instanceof DirectColorModel))
			image = ImageUtilities.convertToDirectColor(image, true);
		final WritableRaster raster = image.getRaster();
		final int w = raster.getWidth();
		int h = raster.getHeight();
		if (raster.getNumBands() < 3)
			return image;
		if (nP == 1)
			convert2Bw(0, h, raster, w, filter.red, filter.green, filter.blue);
		else
			IntStream.range(0, nP).parallel().forEach(
					p -> convert2Bw(h * p / nP, h * (p + 1) / nP, raster, w, filter.red, filter.green, filter.blue));
		return image;
	}

	private static void convert2Bw(int from, int to, WritableRaster raster, int w, int redFilter, int greenFilter,
			int blueFilter) {
		int fsum = redFilter + greenFilter + blueFilter;
		int numBands = raster.getNumBands();
		int len = w * numBands;
		int[] pixels = new int[len];
		for (int y = from; y < to; y++) {
			raster.getPixels(0, y, w, 1, pixels);
			for (int x = 0; x < len; x += numBands)
				pixels[x] = pixels[x + 1] = pixels[x
						+ 2] = (pixels[x] * redFilter + pixels[x + 1] * greenFilter + pixels[x + 2] * blueFilter)
								/ fsum;
			raster.setPixels(0, y, w, 1, pixels);
		}
	}

	public static BufferedImage convertToColor(BufferedImage original) {
		ColorModel model = original.getColorModel();
		if (!(model instanceof DirectColorModel))
			return ImageUtilities.convertToDirectColor(original, true);
		final WritableRaster in = original.getRaster();
		final int w = in.getWidth();
		int h = in.getHeight();
		if (in.getNumBands() >= 3)
			return original;
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final WritableRaster out = result.getRaster();
		if (nP == 1)
			convertToColor(0, h, in, w, out);
		else
			IntStream.range(0, nP).parallel().forEach(p -> convertToColor(h * p / nP, h * (p + 1) / nP, in, w, out));
		return result;
	}

	private static void convertToColor(int from, int to, WritableRaster in, int w, WritableRaster out) {
		int bands = in.getNumBands();
		int len = w * bands;
		int[] inpixels = new int[len];
		int[] outpixels = new int[w * 3];
		int gray;
		for (int y = from; y < to; y++) {
			in.getPixels(0, y, w, 1, inpixels);
			for (int x = 0, xs = 0; x < len;) {
				gray = 0;
				for (int k = 0; k < bands; k++)
					gray += inpixels[x++];
				outpixels[xs++] = outpixels[xs++] = outpixels[xs++] = gray / bands;
			}
			out.setPixels(0, y, w, 1, outpixels);
		}
	}

	public static Image applyNoise(Image im) {
		if (randomNumbers == null)
			randomNumbers = new Random();
		ImageData data = im.getImageData();
		Device device = im.getDevice();
		im.dispose();
		int pixel, red, green, blue;
		final PaletteData palette = data.palette;
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int width = data.width;
		int[] scanLine = new int[width];
		for (int i = 0; i < data.height; i++) {
			data.getPixels(0, i, width, scanLine, 0);
			for (int j = i % 2; j < width; j += 2) {
				pixel = scanLine[j];
				red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
				green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift : (pixel & greenMask) << greenShift);
				blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
				int n = (int) (randomNumbers.nextGaussian() * 8);
				red += n;
				green += n;
				blue += n;
				if (red < 0)
					red = 0;
				else if (red > 255)
					red = 255;
				if (green < 0)
					green = 0;
				else if (green > 255)
					green = 255;
				if (blue < 0)
					blue = 0;
				else if (blue > 255)
					blue = 255;
				scanLine[j] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
			data.setPixels(0, i, width, scanLine, 0);
		}
		return new Image(device, data);
	}

	public static ImageData applyCurves(final ImageData data, ShortLookupTable lookupTable) {
		final short[] ltred;
		final short[] ltgreen;
		final short[] ltblue;
		if (lookupTable.getNumComponents() < 3)
			ltred = ltgreen = ltblue = lookupTable.getTable()[0];
		else {
			ltred = lookupTable.getTable()[0];
			ltgreen = lookupTable.getTable()[1];
			ltblue = lookupTable.getTable()[2];
		}
		final PaletteData palette = data.palette;
		if (palette.isDirect) {
			int h = data.height;
			if (nP == 1)
				applyCurve(0, h, data, ltred, ltgreen, ltblue);
			else
				IntStream.range(0, nP).parallel()
						.forEach(p -> applyCurve(h * p / nP, h * (p + 1) / nP, data, ltred, ltgreen, ltblue));
		} else
			for (RGB rgb : palette.getRGBs()) {
				int red = ltred[rgb.red];
				int green = ltgreen[rgb.green];
				int blue = ltblue[rgb.blue];
				rgb.red = red < 0 ? 0 : red > 255 ? 255 : red;
				rgb.green = green < 0 ? 0 : green > 255 ? 255 : green;
				rgb.blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
			}
		return data;
	}

	private static void applyCurve(int from, int to, ImageData data, short[] ltred, short[] ltgreen, short[] ltblue) {
		
//	},			PaletteData palette) {
		byte[] datadata = data.alphaData;
		int bytesPerLine = data.bytesPerLine;
		int tx;
//		int pixel, red, green, blue;
//		int redMask = palette.redMask;
//		int greenMask = palette.greenMask;
//		int blueMask = palette.blueMask;
//		int redShift = palette.redShift;
//		int greenShift = palette.greenShift;
//		int blueShift = palette.blueShift;
		int w = data.width;
//		int[] scanLine = new int[w];
		for (int y = from; y < to; y++) {
			tx = bytesPerLine*y;
//			data.getPixels(0, y, w, scanLine, 0);
			for (int x = 0; x < w; x++) {
				datadata[tx++] = (byte) ltblue[datadata[tx] & 0xff];
				datadata[tx++] = (byte) ltgreen[datadata[tx] & 0xff];
				datadata[tx++] = (byte) ltred[datadata[tx] & 0xff];
//				pixel = scanLine[x];
//				red = ltred[((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift)];
//				green = ltgreen[((greenShift < 0) ? (pixel & greenMask) >>> -greenShift
//						: (pixel & greenMask) << greenShift)];
//				blue = ltblue[((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift)];
//				scanLine[x] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
//						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
//						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
//			data.setPixels(0, y, w, scanLine, 0);
		}
	}

	public static BufferedImage applyCurves(BufferedImage image, ShortLookupTable lookupTable) {
		final short[] ltred;
		final short[] ltgreen;
		final short[] ltblue;
		if (lookupTable.getNumComponents() < 3)
			ltred = ltgreen = ltblue = lookupTable.getTable()[0];
		else {
			ltred = lookupTable.getTable()[0];
			ltgreen = lookupTable.getTable()[1];
			ltblue = lookupTable.getTable()[2];
		}
		ColorModel model = image.getColorModel();
		if (!(model instanceof DirectColorModel))
			image = ImageUtilities.convertToDirectColor(image, false);
		else if (image.getRaster().getNumBands() < 3)
			image = ImageUtilities.convertToColor(image);
		final WritableRaster r = image.getRaster();
		int h = r.getHeight();
		if (nP == 1)
			applyCurve(0, h, ltred, ltgreen, ltblue, r);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> applyCurve(h * p / nP, h * (p + 1) / nP, ltred, ltgreen, ltblue, r));
		return image;
	}

	private static void applyCurve(int from, int to, short[] ltred, short[] ltgreen, short[] ltblue, WritableRaster r) {
		int red, green, blue;
		int bands = r.getNumBands();
		int w = r.getWidth();
		int len = w * bands;
		int inc = bands - 2;
		int[] pixels = new int[len];
		for (int y = from; y < to; y++) {
			r.getPixels(0, y, w, 1, pixels);
			for (int x = 0; x < len; x += inc) {
				red = ltred[pixels[x]];
				green = ltgreen[pixels[x + 1]];
				blue = ltblue[pixels[x + 2]];
				pixels[x] = red < 0 ? 0 : red > 255 ? 255 : red;
				pixels[++x] = green < 0 ? 0 : green > 255 ? 255 : green;
				pixels[++x] = blue < 0 ? 0 : blue > 255 ? 255 : blue;
			}
			r.setPixels(0, y, w, 1, pixels);
		}
	}

	public static BufferedImage applySplitTone(BufferedImage image, SplitTone split) {
		ColorModel model = image.getColorModel();
		if (!(model instanceof DirectColorModel))
			image = ImageUtilities.convertToDirectColor(image, false);
		else if (image.getRaster().getNumBands() < 3)
			image = ImageUtilities.convertToColor(image);
		float redH, greenH, blueH, r, q, d, t, t1, t3, redS, greenS, blueS;
		q = 0.5f * (1f - split.highlightSaturation) + split.highlightSaturation;
		redH = greenH = blueH = r = 1f - q;
		d = q - r;
		t = (split.highlightHue < 0.5f ? split.highlightHue + 0.5f : split.highlightHue - 0 - 5f) * 6;
		t1 = (t >= 4f) ? t - 4f : t + 2f;
		if (t1 < 3f)
			redH = (t1 < 1f) ? r + d * t1 : q;
		else if (t1 < 4f)
			redH += d * (4f - t1);
		if (t < 3f)
			greenH = (t < 1f) ? r + d * t : q;
		else if (t < 4f)
			greenH += d * (4f - t);
		t3 = (t < 2f) ? t + 4f : t - 2f;
		if (t3 < 3f)
			blueH = (t3 < 1f) ? r + d * t3 : q;
		else if (t3 < 4f)
			blueH += d * (4f - t3);
		int rh = (int) (redH * 255f);
		int gh = (int) (greenH * 255f);
		int bh = (int) (blueH * 255f);
		int mn = Math.min(rh, Math.min(gh, bh));
		rh -= mn;
		gh -= mn;
		bh -= mn;
		q = 0.5f * (1f - split.shadowSaturation) + split.shadowSaturation;
		redS = greenS = blueS = r = 1f - q;
		d = q - r;
		t = split.shadowHue * 6;
		t1 = (t >= 4f) ? t - 4f : t + 2f;
		if (t1 < 3f)
			redS = (t1 < 1f) ? r + d * t1 : q;
		else if (t1 < 4f)
			redS += d * (4f - t1);
		if (t < 3f)
			greenS = (t < 1f) ? r + d * t : q;
		else if (t < 4f)
			greenS += d * (4f - t);
		t3 = (t < 2f) ? t + 4f : t - 2f;
		if (t3 < 3f)
			blueS = (t3 < 1f) ? r + d * t3 : q;
		else if (t3 < 4f)
			blueS += d * (4f - t3);
		int rs = (int) (redS * 255f);
		int gs = (int) (greenS * 255f);
		int bs = (int) (blueS * 255f);
		mn = Math.min(rs, Math.min(gs, bs));
		rs -= mn;
		gs -= mn;
		bs -= mn;
		final short[] balance = split.distribution.makeTable(255);
		final WritableRaster wr = image.getRaster();
		int h = wr.getHeight();
		int n = ImageConstants.NPROCESSORS;
		if (n == 1)
			applySplitTone(0, h, rh, gh, bh, rs, gs, bs, balance, wr);
		else {
			final int rh1 = rh;
			final int gh1 = gh;
			final int bh1 = bh;
			final int rs1 = rs;
			final int gs1 = gs;
			final int bs1 = bs;
			IntStream.range(0, nP).parallel().forEach(
					p -> applySplitTone(h * p / nP, h * (p + 1) / nP, rh1, gh1, bh1, rs1, gs1, bs1, balance, wr));
		}
		return image;
	}

	private static void applySplitTone(int from, int to, int rh, int gh, int bh, int rs, int gs, int bs,
			short[] balance, WritableRaster r) {
		int w = r.getWidth();
		int bands = r.getNumBands();
		int len = w * bands;
		int[] pixels = new int[len];
		int inc = bands - 2;
		int red, green, blue, bal, ibal;
		for (int y = from; y < to; y++) {
			r.getPixels(0, y, w, 1, pixels);
			for (int x = 0; x < len; x += inc) {
				red = pixels[x];
				green = pixels[x + 1];
				blue = pixels[x + 2];
				bal = balance == null ? (red + green + blue) / 3 : balance[(red + green + blue) / 3];
				ibal = 255 - bal;
				red += (ibal * rs - bal * rh) / 255;
				green += (ibal * gs - bal * gh) / 255;
				blue += (ibal * bs - bal * bh) / 255;
				pixels[x] = red < 0 ? 0 : red > 255 ? 255 : red;
				pixels[++x] = green < 0 ? 0 : green > 255 ? 255 : green;
				pixels[++x] = blue < 0 ? 0 : blue > 255 ? 255 : blue;
			}
			r.setPixels(0, y, w, 1, pixels);
		}
	}

	public static ImageData applySplitTone(final ImageData data, SplitTone split) {
		float redH, greenH, blueH, r, q, d, t, t1, t3, redS, greenS, blueS;
		q = 0.5f * (1 - split.highlightSaturation) + split.highlightSaturation;
		redH = greenH = blueH = r = 1f - q;
		d = q - r;
		t = (split.highlightHue < 0.5f ? split.highlightHue + 0.5f : split.highlightHue - 0 - 5f) * 6;
		t1 = (t >= 4f) ? t - 4f : t + 2f;
		if (t1 < 3f)
			redH = (t1 < 1f) ? r + d * t1 : q;
		else if (t1 < 4f)
			redH += d * (4f - t1);
		if (t < 3f)
			greenH = (t < 1f) ? r + d * t : q;
		else if (t < 4f)
			greenH += d * (4f - t);
		t3 = (t < 2f) ? t + 4f : t - 2f;
		if (t3 < 3f)
			blueH = (t3 < 1f) ? r + d * t3 : q;
		else if (t3 < 4f)
			blueH += d * (4f - t3);
		int rh = (int) (redH * 255f);
		int gh = (int) (greenH * 255f);
		int bh = (int) (blueH * 255f);
		int mn = Math.min(rh, Math.min(gh, bh));
		rh -= mn;
		gh -= mn;
		bh -= mn;

		q = 0.5f * (1 - split.shadowSaturation) + split.shadowSaturation;
		redS = greenS = blueS = r = 1f - q;
		d = q - r;
		t = split.shadowHue * 6;
		t1 = (t >= 4f) ? t - 4f : t + 2f;
		if (t1 < 3f)
			redS = (t1 < 1f) ? r + d * t1 : q;
		else if (t1 < 4f)
			redS += d * (4f - t1);
		if (t < 3f)
			greenS = (t < 1f) ? r + d * t : q;
		else if (t < 4f)
			greenS += d * (4f - t);
		t3 = (t < 2f) ? t + 4f : t - 2f;
		if (t3 < 3f)
			blueS = (t3 < 1f) ? r + d * t3 : q;
		else if (t3 < 4f)
			blueS += d * (4f - t3);
		int rs = (int) (redS * 255f);
		int gs = (int) (greenS * 255f);
		int bs = (int) (blueS * 255f);
		mn = Math.min(rs, Math.min(gs, bs));
		rs -= mn;
		gs -= mn;
		bs -= mn;
		final short[] balance = split.distribution.makeTable(255);
		final PaletteData palette = data.palette;
		if (palette.isDirect) {
			int h = data.height;
			int n = ImageConstants.NPROCESSORS;
			if (n == 1)
				applySplitTone(0, h, data, rh, gh, bh, rs, gs, bs, balance);
			else {
				final int rh1 = rh;
				final int gh1 = gh;
				final int bh1 = bh;
				final int rs1 = rs;
				final int gs1 = gs;
				final int bs1 = bs;
				IntStream.range(0, nP).parallel().forEach(p -> applySplitTone(h * p / nP, h * (p + 1) / nP, data, rh1,
						gh1, bh1, rs1, gs1, bs1, balance));
			}
		} else {
			int red, green, blue, bal, ibal;
			for (RGB rgb : palette.getRGBs()) {
				red = rgb.red;
				green = rgb.green;
				blue = rgb.blue;
				bal = balance[(red + green + blue) / 3] & 0xFF;
				ibal = 255 - bal;
				red += (ibal * rs - bal * rh) / 255;
				green += (ibal * gs - bal * gh) / 255;
				blue += (ibal * bs - bal * bh) / 255;
				rgb.red = red < 0 ? 0 : red > 255 ? 255 : red;
				rgb.green = green < 0 ? 0 : green > 255 ? 255 : green;
				rgb.blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
			}
		}
		return data;
	}

	private static void applySplitTone(int from, int to, ImageData data, int rh, int gh, int bh, int rs, int gs, int bs,
			short[] balance) {
//		, PaletteData palette) {
		byte[] datadata = data.alphaData;
		int bytesPerLine = data.bytesPerLine;
		int tx;
//		int pixel;
		int red;
		int green;
		int blue;
		int bal;
		int ibal;
		int w = data.width;
//		int[] scanLine = new int[w];
//		int redMask = palette.redMask;
//		int greenMask = palette.greenMask;
//		int blueMask = palette.blueMask;
//		int redShift = palette.redShift;
//		int greenShift = palette.greenShift;
//		int blueShift = palette.blueShift;
		for (int y = from; y < to; y++) {
			tx = bytesPerLine*y;
//			data.getPixels(0, y, w, scanLine, 0);
			for (int x = 0; x < w; x++) {
				blue = datadata[tx] & 0xff;
				green = datadata[tx+1] & 0xff;
				red = datadata[tx+2] & 0xff;
//				pixel = scanLine[x];
//				red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
//				green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift : (pixel & greenMask) << greenShift);
//				blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
				bal = balance[(red + green + blue) / 3];
				ibal = 255 - bal;
				red += (ibal * rs - bal * rh) / 255;
				green += (ibal * gs - bal * gh) / 255;
				blue += (ibal * bs - bal * bh) / 255;
				red = red < 0 ? 0 : red > 255 ? 255 : red;
				green = green < 0 ? 0 : green > 255 ? 255 : green;
				blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
				datadata[tx++] = (byte) blue;
				datadata[tx++] = (byte) green;
				datadata[tx++] = (byte) red;
//				scanLine[x] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
//						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
//						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
//			data.setPixels(0, y, w, scanLine, 0);
		}
	}

	public static BufferedImage applyVignette(BufferedImage image, Vignette vignette) {
		ColorModel model = image.getColorModel();
		if (!(model instanceof DirectColorModel))
			image = ImageUtilities.convertToDirectColor(image, false);
		final WritableRaster r = image.getRaster();
		final int w = r.getWidth();
		final int h = r.getHeight();
		final int hlow = -h / 2;
		int hhigh = h + hlow;
		final int wlow = -w / 2;
		final int whigh = w + wlow;
		final double offX = vignette.centerX * w;
		final double offY = vignette.centerY * h;
		int maxRadius = (int) Math.sqrt((whigh * whigh + hhigh * hhigh));
		double vx = whigh + Math.abs(offX);
		double vy = hhigh + Math.abs(offY);
		int vRadius = (int) Math.sqrt((vx * vx + vy * vy));
		double v = 1 - vignette.strength * vignette.vignetteAmount * 0.75;
		double b = 1 + vignette.vignetteMidpoint * 7;
		double mul = (1 - v) / Math.tanh(b);
		final int[] lt = new int[vRadius + 1];
		for (int i = 0; i <= vRadius; i++) {
			float vign = i >= maxRadius ? 0f : (float) (v + mul * Math.tanh(b * (maxRadius - i) / maxRadius));
			lt[i] = Math.round(vign * P16);
		}
		int n = ImageConstants.NPROCESSORS;
		if (n == 1)
			applyVignette(0, h, r, w, hlow, wlow, whigh, lt, offX, offY);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> applyVignette(h * p / nP, h * (p + 1) / nP, r, w, hlow, wlow, whigh, lt, offX, offY));
		return image;
	}

	private static void applyVignette(int from, int to, WritableRaster r, int w, int hlow, int wlow, int whigh,
			int[] lt, double offX, double offY) {
		int bands = r.getNumBands();
		int len = w * bands;
		int[] pixels = new int[len];
		int inc = bands > 2 ? bands - 2 : bands;
		int red, green, blue;
		for (int j = from; j < to; j++) {
			int y = j + hlow;
			double vy2 = y - offY;
			vy2 *= vy2;
			r.getPixels(0, j, w, 1, pixels);
			for (int x = wlow, i = 0; x < whigh; x++, i += inc) {
				double vx = x - offX;
				int f = lt[(int) Math.sqrt(vx * vx + vy2)];
				red = pixels[i] * f + P15 >>> 16;
				pixels[i] = red > 255 ? 255 : red;
				if (bands > 2) {
					green = pixels[++i] * f + P15 >>> 16;
					pixels[i] = green > 255 ? 255 : green;
					blue = pixels[++i] * f + P15 >>> 16;
					pixels[i] = blue > 255 ? 255 : blue;
				}
			}
			r.setPixels(0, j, w, 1, pixels);
		}
	}

	public static ImageData applyVignette(ImageData data, Vignette vignette) {
		final ImageData result = ImageUtilities.convertToDirectColor(data);
		final int w = result.width;
		final int h = result.height;
		final int hhigh = h + (-h / 2);
		final int wlow = -w / 2;
		final int whigh = w + wlow;
		final double offX = vignette.centerX * w;
		final double offY = vignette.centerY * h;
		int maxRadius = (int) Math.sqrt((whigh * whigh + hhigh * hhigh));
		double vx = whigh + Math.abs(offX);
		double vy = hhigh + Math.abs(offY);
		int vRadius = (int) Math.sqrt((vx * vx + vy * vy));
		double v = 1 - vignette.strength * vignette.vignetteAmount * 0.75;
		double b = 1 + vignette.vignetteMidpoint * 7;
		double mul = (1 - v) / Math.tanh(b);
		final int[] lt = new int[vRadius + 1];
		for (int i = 0; i <= vRadius; i++) {
			float vign = i >= maxRadius ? 0f : (float) (v + mul * Math.tanh(b * (maxRadius - i) / maxRadius));
			lt[i] = Math.round(vign * P16);
		}
		int n = ImageConstants.NPROCESSORS;
		if (n == 1)
			applyVignette(0, h, result, result.width, -h / 2, hhigh, wlow, whigh, lt, offX, offY);
		else
			IntStream.range(0, nP).parallel().forEach(p -> applyVignette(h * p / nP, h * (p + 1) / nP, result,
					result.width, -h / 2, hhigh, wlow, whigh, lt, offX, offY));
		return result;
	}

	private static void applyVignette(int from, int to, ImageData data, int w, int hlow, int hhigh, int wlow, int whigh,
			int[] lt, double offX, double offY) {
		int[] scanLine = new int[w];
		int pixel, red, green, blue;
		PaletteData palette = data.palette;
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		for (int j = from, y = hlow; j < to && y < hhigh; y++, j++) {
			double vy2 = y - offY;
			vy2 *= vy2;
			data.getPixels(0, j, w, scanLine, 0);
			for (int i = 0, x = wlow; x < whigh; x++, i++) {
				double vx = x - offX;
				pixel = scanLine[i];
				red = ((redShift < 0) ? (pixel & redMask) >>> -redShift : (pixel & redMask) << redShift);
				green = ((greenShift < 0) ? (pixel & greenMask) >>> -greenShift : (pixel & greenMask) << greenShift);
				blue = ((blueShift < 0) ? (pixel & blueMask) >>> -blueShift : (pixel & blueMask) << blueShift);
				int f = lt[(int) Math.sqrt(vx * vx + vy2)];
				red = red * f + P15 >>> 16;
				green = green * f + P15 >>> 16;
				blue = blue * f + P15 >>> 16;
				red = red > 255 ? 255 : red;
				green = green > 255 ? 255 : green;
				blue = blue > 255 ? 255 : blue;
				scanLine[i] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
			data.setPixels(0, j, w, scanLine, 0);
		}
	}

	public static ImageData applyPerspectiveCorrection(ImageData data,
			final PerspectiveCorrection perspectiveCorrection) {
		final ImageData in = ImageUtilities.convertToDirectColor(data);
		final int w = in.width;
		final int h = in.height;
		Matrix tmatrix = perspectiveCorrection.transformation;
		Matrix normi = new Matrix(
				new double[][] { { w, 0d, 0d, 0d }, { 0d, h, 0d, 0d }, { 0d, 0d, 1d, 0d }, { 0d, 0d, 0d, 1d } });
		Matrix norm = normi.inverse();
		tmatrix = tmatrix.times(norm);
		tmatrix = normi.times(tmatrix);
		final double[][] m = tmatrix.getArray();
		int n = ImageConstants.NPROCESSORS;
		final ImageData out = new ImageData(w, h, data.depth, data.palette);
		if (n == 1)
			applyPerspectiveCorrection(0, h, in, perspectiveCorrection, w, h, m, out);
		else
			IntStream.range(0, nP).parallel().forEach(p -> applyPerspectiveCorrection(h * p / nP, h * (p + 1) / nP, in,
					perspectiveCorrection, w, h, m, out));
		return out;
	}

	private static void applyPerspectiveCorrection(int from, int to, ImageData data,
			PerspectiveCorrection perspectiveCorrection, int w, int h, double[][] m, ImageData out) {
		double m00 = m[0][0];
		double m01 = m[0][1];
		double m02 = m[0][2];
		double m03 = m[0][3];
		double m10 = m[1][0];
		double m11 = m[1][1];
		double m12 = m[1][2];
		double m13 = m[1][3];
		double m30 = m[3][0];
		double m31 = m[3][1];
		double m32 = m[3][2];
		double m33 = m[3][3];
		double f = perspectiveCorrection.flen;
		RGB fillColor = perspectiveCorrection.fillColor;
		PaletteData palette = data.palette;
		int bgpixel = palette.getPixel(fillColor);
		int bgred = fillColor.red;
		int bggreen = fillColor.green;
		int bgblue = fillColor.blue;

		int hlow = -h / 2;
		int wlow = -w / 2;
		int whigh = w + wlow;
		int h1 = h - 1;
		int w1 = w - 1;
		int[] scanLine = new int[w];
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int red, green, blue;
		double m32_f_m33 = m32 * f + m33;
		double m02_f_m03 = m02 * f + m03;
		double m12_f_m13 = m12 * f + m13;
		Point2D.Double translation = perspectiveCorrection.finalTranslation;
		double tx = (translation == null ? 0d : translation.x * w) + wlow;
		double ty = (translation == null ? 0d : translation.y * h) + hlow;
		for (int j = from; j < to; j++) {
			int y = j + hlow;
			double ym3 = m31 * y + m32_f_m33;
			double ym0 = m01 * y + m02_f_m03;
			double ym1 = m11 * y + m12_f_m13;
			for (int i = 0, x = wlow; x < whigh; x++, i++) {
				double s = m30 * x + ym3;
				double px = (m00 * x + ym0) / s - tx;
				double py = (m10 * x + ym1) / s - ty;
				int x1 = (int) Math.floor(px);
				int y1 = (int) Math.floor(py);
				int dx = (int) ((px - x1) * 256d);
				int dy = (int) ((py - y1) * 256d);
				int dx1 = 256 - dx;
				int dy1 = 256 - dy;
				if (x1 < 0 || x1 >= w1 || y1 < 0 || y1 >= h1) {
					if (x1 == -1 && y1 >= 0 && y1 < h) {
						int p00 = data.getPixel(x1 + 1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dx1 * bgred + dx * red00 + 128 >> 8;
						green = dx1 * bggreen + dx * green00 + 128 >> 8;
						blue = dx1 * bgblue + dx * blue00 + 128 >> 8;
					} else if (x1 == w1 && y1 >= 0 && y1 < h) {
						int p00 = data.getPixel(x1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dx1 * red00 + dx * bgred + 128 >> 8;
						green = dx1 * green00 + dx * bggreen + 128 >> 8;
						blue = dx1 * blue00 + dx * bgblue + 128 >> 8;
					} else if (y1 == -1 && x1 >= 0 && x1 < w) {
						int p00 = data.getPixel(x1, y1 + 1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dy1 * bgred + dy * red00 + 128 >> 8;
						green = dy1 * bggreen + dy * green00 + 128 >> 8;
						blue = dy1 * bgblue + dy * blue00 + 128 >> 8;
					} else if (y1 == h1 && x1 >= 0 && x1 < w) {
						int p00 = data.getPixel(x1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dy1 * red00 + dy * bgred + 128 >> 8;
						green = dy1 * green00 + dy * bggreen + 128 >> 8;
						blue = dy1 * blue00 + dy * bgblue + 128 >> 8;
					} else {
						scanLine[i] = bgpixel;
						continue;
					}
				} else {
					int p00 = data.getPixel(x1, y1);
					int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
					int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
							: (p00 & greenMask) << greenShift);
					int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift : (p00 & blueMask) << blueShift);
					int p01 = data.getPixel(x1 + 1, y1);
					int red01 = ((redShift < 0) ? (p01 & redMask) >>> -redShift : (p01 & redMask) << redShift);
					int green01 = ((greenShift < 0) ? (p01 & greenMask) >>> -greenShift
							: (p01 & greenMask) << greenShift);
					int blue01 = ((blueShift < 0) ? (p01 & blueMask) >>> -blueShift : (p01 & blueMask) << blueShift);
					int p10 = data.getPixel(x1, y1 + 1);
					int red10 = ((redShift < 0) ? (p10 & redMask) >>> -redShift : (p10 & redMask) << redShift);
					int green10 = ((greenShift < 0) ? (p10 & greenMask) >>> -greenShift
							: (p10 & greenMask) << greenShift);
					int blue10 = ((blueShift < 0) ? (p10 & blueMask) >>> -blueShift : (p10 & blueMask) << blueShift);
					int p11 = data.getPixel(x1 + 1, y1 + 1);
					int red11 = ((redShift < 0) ? (p11 & redMask) >>> -redShift : (p11 & redMask) << redShift);
					int green11 = ((greenShift < 0) ? (p11 & greenMask) >>> -greenShift
							: (p11 & greenMask) << greenShift);
					int blue11 = ((blueShift < 0) ? (p11 & blueMask) >>> -blueShift : (p11 & blueMask) << blueShift);
					red = dy1 * (dx1 * red00 + dx * red01) + dy * (dx1 * red10 + dx * red11) + 128 >> 16;
					green = dy1 * (dx1 * green00 + dx * green01) + dy * (dx1 * green10 + dx * green11) + 128 >> 16;
					blue = dy1 * (dx1 * blue00 + dx * blue01) + dy * (dx1 * blue10 + dx * blue11) + 128 >> 16;
				}
				scanLine[i] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
			out.setPixels(0, j, w, scanLine, 0);
		}
	}

	public static BufferedImage applyPerspectiveCorrection(BufferedImage image,
			final PerspectiveCorrection perspectiveCorrection) {
		ColorModel colorModel = image.getColorModel();
		if (!(colorModel instanceof DirectColorModel)) {
			image = ImageUtilities.convertToDirectColor(image, false);
			colorModel = image.getColorModel();
		}
		final WritableRaster r = image.getRaster();
		final int w = r.getWidth();
		final int h = r.getHeight();
		final WritableRaster out = colorModel.createCompatibleWritableRaster(w, h);
		Matrix tmatrix = perspectiveCorrection.transformation;
		Matrix normi = new Matrix(
				new double[][] { { w, 0d, 0d, 0d }, { 0d, h, 0d, 0d }, { 0d, 0d, 1d, 0d }, { 0d, 0d, 0d, 1d } });
		Matrix norm = normi.inverse();
		tmatrix = tmatrix.times(norm);
		tmatrix = normi.times(tmatrix);
		final double[][] m = tmatrix.getArray();
		int n = ImageConstants.NPROCESSORS;
		if (n == 1)
			applyPerspectiveCorrection(0, h, perspectiveCorrection, r, w, h, out, m);
		else
			IntStream.range(0, nP).parallel().forEach(p -> applyPerspectiveCorrection(h * p / nP, h * (p + 1) / nP,
					perspectiveCorrection, r, w, h, out, m));
		return new BufferedImage(colorModel, out, false, null);
	}

	private static void applyPerspectiveCorrection(int from, int to, PerspectiveCorrection perspectiveCorrection,
			WritableRaster r, int w, int h, WritableRaster out, double[][] m) {
		double m00 = m[0][0];
		double m01 = m[0][1];
		double m02 = m[0][2];
		double m03 = m[0][3];
		double m10 = m[1][0];
		double m11 = m[1][1];
		double m12 = m[1][2];
		double m13 = m[1][3];
		double m30 = m[3][0];
		double m31 = m[3][1];
		double m32 = m[3][2];
		double m33 = m[3][3];
		double f = perspectiveCorrection.flen;
		RGB fillColor = perspectiveCorrection.fillColor;
		int red = fillColor.red;
		int green = fillColor.green;
		int blue = fillColor.blue;
		int bands = r.getNumBands();
		int[] bgpixel = new int[bands];
		if (bands <= 2)
			bgpixel[0] = bgpixel[1] = (red + green + blue) / 3;
		else {
			bgpixel[0] = red;
			bgpixel[1] = green;
			bgpixel[2] = blue;
		}
		int hlow = -h / 2;
		int wlow = -w / 2;
		int whigh = w + wlow;
		int h1 = h - 1;
		int w1 = w - 1;
		int bands2 = bands + bands;
		int bands3 = bands2 + bands;
		int len = w * bands;
		int[] pixel = new int[bands * 4];
		int[] pixels = new int[len];
		double m32_f_m33 = m32 * f + m33;
		double m02_f_m03 = m02 * f + m03;
		double m12_f_m13 = m12 * f + m13;

		Point2D.Double translation = perspectiveCorrection.finalTranslation;
		double tx = (translation == null ? 0d : translation.x * w) + wlow;
		double ty = (translation == null ? 0d : translation.y * h) + hlow;

		for (int j = from; j < to; j++) {
			int y = j + hlow;
			double ym3 = m31 * y + m32_f_m33;
			double ym0 = m01 * y + m02_f_m03;
			double ym1 = m11 * y + m12_f_m13;
			for (int x = wlow, i = 0; x < whigh; x++, i += bands) {
				double s = m30 * x + ym3;
				double px = (m00 * x + ym0) / s - tx;
				double py = (m10 * x + ym1) / s - ty;
				int x1 = (int) Math.floor(px);
				int y1 = (int) Math.floor(py);
				int dx = (int) ((px - x1) * 256d);
				int dy = (int) ((py - y1) * 256d);
				int dx1 = 256 - dx;
				int dy1 = 256 - dy;
				if (x1 < 0 || x1 >= w1 || y1 < 0 || y1 >= h1) {
					if (x1 == -1 && y1 >= 0 && y1 < h) {
						r.getPixel(x1 + 1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dx1 * bgpixel[b] + dx * pixel[b] + 128 >> 8;
					} else if (x1 == w1 && y1 >= 0 && y1 < h) {
						r.getPixel(x1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dx1 * pixel[b] + dx * bgpixel[b] + 128 >> 8;
					} else if (y1 == -1 && x1 >= 0 && x1 < w) {
						r.getPixel(x1, y1 + 1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dy1 * bgpixel[b] + dy * pixel[b] + 128 >> 8;
					} else if (y1 == h1 && x1 >= 0 && x1 < w) {
						r.getPixel(x1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dy1 * pixel[b] + dy * bgpixel[b] + 128 >> 8;
					} else
						System.arraycopy(bgpixel, 0, pixels, i, bands);
				} else {
					r.getPixels(x1, y1, 2, 2, pixel);
					for (int b = 0; b < bands; b++)
						pixels[i + b] = dy1 * (dx1 * pixel[b] + dx * pixel[b + bands])
								+ dy * (dx1 * pixel[b + bands2] + dx * pixel[b + bands3]) + 128 >> 16;
				}
			}
			out.setPixels(0, j, w, 1, pixels);
		}
	}

	public static ImageData applyTransformation(ImageData data, final Transformation transformation) {
		final ImageData in = ImageUtilities.convertToDirectColor(data);
		final int w = in.width;
		final int h = in.height;
		transformation.init(w, h);
		int n = ImageConstants.NPROCESSORS;
		final ImageData out = new ImageData(w, h, data.depth, data.palette);
		if (n == 1)
			applyTransformation(0, h, in, transformation, w, h, out);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> applyTransformation(h * p / nP, h * (p + 1) / nP, in, transformation, w, h, out));
		return out;

	}

	/**
	 * Algorithms are based on the algorithm found in Raw Therapee 3
	 * (iptransform.cc)
	 */
	private static void applyTransformation(int from, int to, ImageData data, Transformation transformation, int w,
			int h, ImageData out) {
		RGB fillColor = transformation.fillColor;
		PaletteData palette = data.palette;
		int bgpixel = 0, bgred = 0, bggreen = 0, bgblue = 0;
		if (fillColor != null) {
			bgpixel = palette.getPixel(fillColor);
			bgred = fillColor.red;
			bggreen = fillColor.green;
			bgblue = fillColor.blue;
		}
		int hlow = -h / 2;
		int wlow = -w / 2;
		int whigh = w + wlow;
		int h1 = h - 1;
		int w1 = w - 1;
		int[] scanLine = new int[w];
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int red, green, blue;

		double maxRadius = transformation.maxRadius;
		// auxiliary variables for distortion correction
		double distortion = transformation.distortion;
		// auxiliary variables for rotation
		double cost = transformation.rotCos;
		double sint = transformation.rotSin;
		// auxiliary variables for perspective correction
		double vpcospt = transformation.verPerspCos;
		double vptanpt = transformation.verPerspTan;

		// auxiliary variables for horizontal perspective correction
		double hpcospt = transformation.horPerspCos;
		double hptanpt = transformation.horPerspTan;
		double ascale = transformation.fillScale;
		boolean perspectiveChange = transformation.perspectiveChange;
		boolean isRotating = transformation.isRotating;
		for (int j = from; j < to; j++) {
			int y = j + hlow;
			for (int i = 0, x = wlow; x < whigh; x++, i++) {
				double x_d = ascale * x; // scale
				double y_d = ascale * y; // scale
				if (perspectiveChange) {
					// horizontal perspective transformation
					y_d = y_d * maxRadius / (maxRadius + x_d * hptanpt);
					x_d = x_d * maxRadius * hpcospt / (maxRadius + x_d * hptanpt);

					// vertical perspective transformation
					x_d = x_d * maxRadius / (maxRadius - y_d * vptanpt);
					y_d = y_d * maxRadius * vpcospt / (maxRadius - y_d * vptanpt);
				}
				double px, py;
				if (isRotating) {
					// rotate
					px = x_d * cost - y_d * sint;
					py = x_d * sint + y_d * cost;
				} else {
					px = x_d;
					py = y_d;
				}
				if (distortion != 0d) {
					// distortion correction
					double r = Math.sqrt(px * px + py * py) / maxRadius;
					double s = 1.0 - distortion + distortion * r;
					px *= s;
					py *= s;
				}
				// de-center
				px -= wlow;
				py -= hlow;
				int x1 = (int) Math.floor(px);
				int y1 = (int) Math.floor(py);
				int dx = (int) ((px - x1) * 256d);
				int dy = (int) ((py - y1) * 256d);
				int dx1 = 256 - dx;
				int dy1 = 256 - dy;
				if (x1 < 0 || x1 >= w1 || y1 < 0 || y1 >= h1) {
					if (x1 == -1 && y1 >= 0 && y1 < h) {
						int p00 = data.getPixel(x1 + 1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dx1 * bgred + dx * red00 + 128 >> 8;
						green = dx1 * bggreen + dx * green00 + 128 >> 8;
						blue = dx1 * bgblue + dx * blue00 + 128 >> 8;
					} else if (x1 == w1 && y1 >= 0 && y1 < h) {
						int p00 = data.getPixel(x1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dx1 * red00 + dx * bgred + 128 >> 8;
						green = dx1 * green00 + dx * bggreen + 128 >> 8;
						blue = dx1 * blue00 + dx * bgblue + 128 >> 8;
					} else if (y1 == -1 && x1 >= 0 && x1 < w) {
						int p00 = data.getPixel(x1, y1 + 1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dy1 * bgred + dy * red00 + 128 >> 8;
						green = dy1 * bggreen + dy * green00 + 128 >> 8;
						blue = dy1 * bgblue + dy * blue00 + 128 >> 8;
					} else if (y1 == h1 && x1 >= 0 && x1 < w) {
						int p00 = data.getPixel(x1, y1);
						int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
						int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
								: (p00 & greenMask) << greenShift);
						int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift
								: (p00 & blueMask) << blueShift);
						red = dy1 * red00 + dy * bgred + 128 >> 8;
						green = dy1 * green00 + dy * bggreen + 128 >> 8;
						blue = dy1 * blue00 + dy * bgblue + 128 >> 8;
					} else {
						scanLine[i] = bgpixel;
						continue;
					}
				} else {
					int p00 = data.getPixel(x1, y1);
					int red00 = ((redShift < 0) ? (p00 & redMask) >>> -redShift : (p00 & redMask) << redShift);
					int green00 = ((greenShift < 0) ? (p00 & greenMask) >>> -greenShift
							: (p00 & greenMask) << greenShift);
					int blue00 = ((blueShift < 0) ? (p00 & blueMask) >>> -blueShift : (p00 & blueMask) << blueShift);
					int p01 = data.getPixel(x1 + 1, y1);
					int red01 = ((redShift < 0) ? (p01 & redMask) >>> -redShift : (p01 & redMask) << redShift);
					int green01 = ((greenShift < 0) ? (p01 & greenMask) >>> -greenShift
							: (p01 & greenMask) << greenShift);
					int blue01 = ((blueShift < 0) ? (p01 & blueMask) >>> -blueShift : (p01 & blueMask) << blueShift);
					int p10 = data.getPixel(x1, y1 + 1);
					int red10 = ((redShift < 0) ? (p10 & redMask) >>> -redShift : (p10 & redMask) << redShift);
					int green10 = ((greenShift < 0) ? (p10 & greenMask) >>> -greenShift
							: (p10 & greenMask) << greenShift);
					int blue10 = ((blueShift < 0) ? (p10 & blueMask) >>> -blueShift : (p10 & blueMask) << blueShift);
					int p11 = data.getPixel(x1 + 1, y1 + 1);
					int red11 = ((redShift < 0) ? (p11 & redMask) >>> -redShift : (p11 & redMask) << redShift);
					int green11 = ((greenShift < 0) ? (p11 & greenMask) >>> -greenShift
							: (p11 & greenMask) << greenShift);
					int blue11 = ((blueShift < 0) ? (p11 & blueMask) >>> -blueShift : (p11 & blueMask) << blueShift);
					red = dy1 * (dx1 * red00 + dx * red01) + dy * (dx1 * red10 + dx * red11) + 128 >> 16;
					green = dy1 * (dx1 * green00 + dx * green01) + dy * (dx1 * green10 + dx * green11) + 128 >> 16;
					blue = dy1 * (dx1 * blue00 + dx * blue01) + dy * (dx1 * blue10 + dx * blue11) + 128 >> 16;
				}
				scanLine[i] = ((redShift < 0 ? red << -redShift : red >>> redShift) & redMask)
						| ((greenShift < 0 ? green << -greenShift : green >>> greenShift) & greenMask)
						| ((blueShift < 0 ? blue << -blueShift : blue >>> blueShift) & blueMask);
			}
			out.setPixels(0, j, w, scanLine, 0);
		}

	}

	public static BufferedImage applyTransformation(BufferedImage image, final Transformation transformation) {
		ColorModel colorModel = image.getColorModel();
		if (!(colorModel instanceof DirectColorModel)) {
			image = ImageUtilities.convertToDirectColor(image, false);
			colorModel = image.getColorModel();
		}
		final WritableRaster r = image.getRaster();
		final int w = r.getWidth();
		final int h = r.getHeight();
		transformation.init(w, h);
		final WritableRaster raster = colorModel.createCompatibleWritableRaster(w, h);
		int n = ImageConstants.NPROCESSORS;
		if (n == 1)
			applyTransformation(0, h, transformation, r, w, h, raster);
		else
			IntStream.range(0, nP).parallel()
					.forEach(p -> applyTransformation(h * p / nP, h * (p + 1) / nP, transformation, r, w, h, raster));
		return new BufferedImage(colorModel, raster, false, null);
	}

	/**
	 * Algorithms are based on the algorithm found in Raw Therapee 3
	 * (iptransform.cc)
	 */
	private static void applyTransformation(int from, int to, Transformation transformation, WritableRaster wr, int w,
			int h, WritableRaster out) {
		RGB fillColor = transformation.fillColor;
		int bands = wr.getNumBands();
		int[] bgpixel = new int[bands];
		if (fillColor != null) {
			if (bands <= 2)
				bgpixel[0] = bgpixel[1] = (fillColor.red + fillColor.green + fillColor.blue) / 3;
			else {
				bgpixel[0] = fillColor.red;
				bgpixel[1] = fillColor.green;
				bgpixel[2] = fillColor.blue;
			}
		}
		int hlow = -h / 2;
		int wlow = -w / 2;
		int whigh = w + wlow;
		int h1 = h - 1;
		int w1 = w - 1;
		int bands2 = bands + bands;
		int bands3 = bands2 + bands;
		int len = w * bands;
		int[] pixel = new int[bands * 4];
		int[] pixels = new int[len];

		double maxRadius = transformation.maxRadius;
		// auxiliary variables for distortion correction
		double distortion = transformation.distortion;
		// auxiliary variables for rotation
		double cost = transformation.rotCos;
		double sint = transformation.rotSin;
		// auxiliary variables for perspective correction
		double vpcospt = transformation.verPerspCos;
		double vptanpt = transformation.verPerspTan;

		// auxiliary variables for horizontal perspective correction
		double hpcospt = transformation.horPerspCos;
		double hptanpt = transformation.horPerspTan;
		double ascale = transformation.fillScale;
		boolean perspectiveChange = transformation.perspectiveChange;
		boolean isRotating = transformation.isRotating;
		for (int j = from; j < to; j++) {
			int y = j + hlow;
			for (int x = wlow, i = 0; x < whigh; x++, i += bands) {
				double x_d = ascale * x; // scale
				double y_d = ascale * y; // scale
				if (perspectiveChange) {
					// horizontal perspective transformation
					y_d = y_d * maxRadius / (maxRadius + x_d * hptanpt);
					x_d = x_d * maxRadius * hpcospt / (maxRadius + x_d * hptanpt);

					// vertical perspective transformation
					x_d = x_d * maxRadius / (maxRadius - y_d * vptanpt);
					y_d = y_d * maxRadius * vpcospt / (maxRadius - y_d * vptanpt);
				}
				double px, py;
				if (isRotating) {
					// rotate
					px = x_d * cost - y_d * sint;
					py = x_d * sint + y_d * cost;
				} else {
					px = x_d;
					py = y_d;
				}
				if (distortion != 0d) {
					// distortion correction
					double r = Math.sqrt(px * px + py * py) / maxRadius;
					double s = 1.0 - distortion + distortion * r;
					px *= s;
					py *= s;
				}
				// de-center
				px -= wlow;
				py -= hlow;

				int x1 = (int) Math.floor(px);
				int y1 = (int) Math.floor(py);
				int dx = (int) ((px - x1) * 256d);
				int dy = (int) ((py - y1) * 256d);
				int dx1 = 256 - dx;
				int dy1 = 256 - dy;
				if (x1 < 0 || x1 >= w1 || y1 < 0 || y1 >= h1) {
					if (x1 == -1 && y1 >= 0 && y1 < h) {
						wr.getPixel(x1 + 1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dx1 * bgpixel[b] + dx * pixel[b] + 128 >> 8;
					} else if (x1 == w1 && y1 >= 0 && y1 < h) {
						wr.getPixel(x1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dx1 * pixel[b] + dx * bgpixel[b] + 128 >> 8;
					} else if (y1 == -1 && x1 >= 0 && x1 < w) {
						wr.getPixel(x1, y1 + 1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dy1 * bgpixel[b] + dy * pixel[b] + 128 >> 8;
					} else if (y1 == h1 && x1 >= 0 && x1 < w) {
						wr.getPixel(x1, y1, pixel);
						for (int b = 0; b < bands; b++)
							pixels[i + b] = dy1 * pixel[b] + dy * bgpixel[b] + 128 >> 8;
					} else
						System.arraycopy(bgpixel, 0, pixels, i, bands);
				} else {
					wr.getPixels(x1, y1, 2, 2, pixel);
					for (int b = 0; b < bands; b++)
						pixels[i + b] = dy1 * (dx1 * pixel[b] + dx * pixel[b + bands])
								+ dy * (dx1 * pixel[b + bands2] + dx * pixel[b + bands3]) + 128 >> 16;
				}
			}
			out.setPixels(0, j, w, 1, pixels);
		}
	}

	public static void convolveAndTranspose(final int[] kernel, final byte[] in, final byte[] out, final int width,
			final int height) {
		if (nP == 1)
			convolveAndTranspose(0, height, kernel, in, out, width, height);
		else
			IntStream.range(0, nP).parallel().forEach(
					p -> convolveAndTranspose(height * p / nP, height * (p + 1) / nP, kernel, in, out, width, height));
	}

	private static void convolveAndTranspose(int from, int to, int[] kernel, byte[] in, byte[] out, int width,
			int height) {
		int cols = kernel.length;
		int shift = UnsharpMask.SHIFT;
		int round = 1 << shift - 1;
		int maxvalue = (1 << shift) * 255;
		int cols2 = cols / 2;
		int w1 = width - 1;
		int index, yoff, lum, ix;
		for (int y = from; y < to; y++) {
			index = y;
			yoff = y * width;
			for (int x = 0; x < width; x++) {
				lum = round;
				for (int col = -cols2; col <= cols2; col++) {
					ix = x + col;
					lum += kernel[cols2 + col] * (in[yoff + (ix < 0 ? 0 : ix > w1 ? w1 : ix)] & 0xff);
				}
				out[index] = lum < 0 ? 0 : lum > maxvalue ? -1 : (byte) (lum >>> shift);
				index += height;
			}
		}
	}

	public static void convolveAndTranspose(final int[] kernel, final short[] in, final short[] out, final int width,
			final int height) {
		if (nP == 1)
			convolveAndTranspose(0, height, kernel, in, out, width, height);
		else
			IntStream.range(0, nP).parallel().forEach(
					p -> convolveAndTranspose(height * p / nP, height * (p + 1) / nP, kernel, in, out, width, height));
	}

	private static void convolveAndTranspose(int from, int to, int[] kernel, short[] in, short[] out, int width,
			int height) {
		short max = LabImage.MAXL;
		int cols = kernel.length;
		int shift = UnsharpMask.SHIFT;
		int fac = 1 << shift;
		int facr = fac >>> 1;
		int cols2 = cols >>> 1;
		int w1 = width - 1;
		int index, yoff, lum, ix;
		for (int y = from; y < to; y++) {
			index = y;
			yoff = y * width;
			for (int x = 0; x < width; x++) {
				lum = facr;
				for (int col = -cols2; col <= cols2; col++) {
					ix = x + col;
					lum += kernel[cols2 + col] * (in[yoff + (ix < 0 ? 0 : ix > w1 ? w1 : ix)]);
				}
				lum /= fac;
				out[index] = lum < 0 ? 0 : lum > max ? max : (short) lum;
				index += height;
			}
		}
	}

	public static void convolveAndTranspose(final int radius, final byte[] in, final byte[] out, final int width,
			final int height, final boolean min) {
		if (nP == 1)
			convolveAndTranspose(0, height, radius, in, out, width, height, min);
		else
			IntStream.range(0, nP).parallel().forEach(p -> convolveAndTranspose(height * p / nP, height * (p + 1) / nP,
					radius, in, out, width, height, min));
	}

	public static void convolveAndTranspose(final int radius, final short[] in, final short[] out, final int width,
			final int height, final boolean min) {
		if (nP == 1)
			convolveAndTranspose(0, height, radius, in, out, width, height, min);
		else
			IntStream.range(0, nP).parallel().forEach(p -> convolveAndTranspose(height * p / nP, height * (p + 1) / nP,
					radius, in, out, width, height, min));
	}

	private static void convolveAndTranspose(int from, int to, int radius, byte[] in, byte[] out, int width, int height,
			boolean min) {
		int w1 = width - 1;
		int index, yoff, lum, ix;
		for (int y = from; y < to; y++) {
			index = y;
			yoff = y * width;
			for (int x = 0; x < width; x++) {
				lum = (min) ? 255 : 0;
				for (int col = -radius; col <= radius; col++) {
					ix = x + col;
					if (ix >= 0 && ix <= w1) {
						int lum1 = in[yoff + ix] & 0xff;
						if (min) {
							if (lum1 < lum)
								lum = lum1;
						} else if (lum1 > lum)
							lum = lum1;
					}
				}
				out[index] = (byte) lum;
				index += height;
			}
		}
	}

	private static void convolveAndTranspose(int from, int to, int radius, short[] in, short[] out, int width,
			int height, boolean min) {
		int w1 = width - 1;
		int index, yoff, lum, ix;
		for (int y = from; y < to; y++) {
			index = y;
			yoff = y * width;
			for (int x = 0; x < width; x++) {
				lum = (min) ? LabImage.MAXL : 0;
				for (int col = -radius; col <= radius; col++) {
					ix = x + col;
					if (ix >= 0 && ix <= w1) {
						int lum1 = in[yoff + ix];
						if (min) {
							if (lum1 < lum)
								lum = lum1;
						} else if (lum1 > lum)
							lum = lum1;
					}
				}
				out[index] = (short) lum;
				index += height;
			}
		}
	}

	public static void waitUntilFileIsReady(File file) throws AccessDeniedException {
		int i = 20;
		while (--i > 0) {
			if (file.exists() && file.canRead())
				return;
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				break;
			}
		}
		throw new AccessDeniedException(file.getAbsolutePath());
	}

}
