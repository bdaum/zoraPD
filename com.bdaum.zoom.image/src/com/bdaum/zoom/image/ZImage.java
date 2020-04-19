/*******************************************************************************
 * Copyright (c) 2009-2019 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ShortLookupTable;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.internal.awt.HSLimage;
import com.bdaum.zoom.image.internal.awt.LabImage;
import com.bdaum.zoom.image.internal.awt.UnsharpFilter;
import com.bdaum.zoom.image.internal.swt.ImageLoader;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.PerspectiveCorrection;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.Rotation;
import com.bdaum.zoom.image.recipe.SplitTone;
import com.bdaum.zoom.image.recipe.Transformation;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.jai.JAIReader;
import com.sun.jimi.core.Jimi;

import ij.ImagePlus;
import ij.io.Opener;
import sun.awt.image.ToolkitImage;

/**
 * This class combines several image representations into one entity. It also
 * controls the recipe based development of RAW images.
 *
 */
public class ZImage {

	public static final int IMAGE_WEBP = 99;
	/**
	 * Ignore recipe
	 */
	public static final int ORIGINAL = -1;
	/**
	 * No cropping
	 */
	public static final int UNCROPPED = 0;
	/**
	 * Show crop borders with an overlay mask
	 */
	public static final int CROPMASK = 1;
	/**
	 * Crop image
	 */
	public static final int CROPPED = 2;

	private ImageData swtImageData;
	private BufferedImage bufferedImage;
	private HSLimage hslImage;
	private LabImage labImage;
	private Recipe recipe = null;
	private RGB bw = null;
	private UnsharpMask umask = null;
	private ColorConvertOp colorConvertOp = null;
	private boolean advanced = false;
	private int raster = 0;
	private Color frameColor = null;
	private int rotation = 0;
	private float scaleX = 1f;
	private float scaleY = 1f;
	public int width;
	public int height;
	public int sourceWidth;
	public int sourceHeight;
	private Device currentDevice;
	private Image swtImage;
	private boolean developed;
	private boolean isDisposed;
	private double scale = 1d;
	private boolean advancedRecipe;
	private final String fileName;
	private float userScaleX = 1f;
	private float userScaleY = 1f;
	private int userRotation = 0;
	private boolean thumbnail;
	private ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	private int superSamplingFactor = 1;

	/**
	 * Loads an image using a variety of loaders
	 *
	 * @param file
	 *            - the image file
	 * @param imageFormat
	 *            - image format (file extension), null for converted raw files
	 * @param maxDim
	 *            - maximum of height and width
	 * @return the loaded image
	 * @throws Exception
	 */
	public static ZImage loadThumbnail(File file, String imageFormat, int maxDim) throws Exception {
		ImageUtilities.waitUntilFileIsReady(file);
		ZImage image = null;
		try {
			image = loadViaImageIO(file, 3 * maxDim * maxDim);
			if (image == null)
				image = loadViaImageJ(file);
			if (image == null)
				image = loadExtraFileTypes(file);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Exception e) {
			image = loadExtraFileTypes(file);
		}
		if (image != null) {
			image.thumbnail = true;
			image.convertToRGB(AWTIMAGE);
		}
		return image;
	}

	/**
	 * Loads an image using a variety of loaders
	 *
	 * @param file
	 *            - the image file
	 * @param imageFormat
	 *            - image format (file extension)
	 * @param rotation
	 *            - user defined rotation to be applied during loading
	 * @param bounds
	 *            - preferred image bounds or null. To scale only for height set
	 *            bounds.width negative; to scale only for width, set bounds.height
	 *            negative. Note that the absolute values of width and height are
	 *            also used to determine the orientation-
	 * @param scalingFactor
	 *            - scaling factor to be used when bounds are specified as null Set
	 *            to 0 for full resolution without subsampling
	 * @param maxFactor
	 *            - maximum scaling factor (limits the effects of the bounds
	 *            parameter)
	 * @param advanced
	 *            - true if antialiasing and quality interpolation shall be used
	 * @param bw
	 *            - RGB value if image is to be converted into black and white, null
	 *            otherwise. The value will be used as filter
	 * @param umask
	 *            - unsharp mask for output sharpening or null
	 * @param cop
	 *            - color conversion operator or null
	 * @param rawRecipe
	 *            - recipe for raw development or null
	 * @return the loaded image
	 * @throws Exception
	 */
	public static ZImage loadImage(File file, String imageFormat, int rotation, Rectangle bounds, double scalingFactor,
			double maxFactor, boolean advanced, RGB bw, UnsharpMask umask, ColorConvertOp cop, Recipe rawRecipe)
			throws Exception {
		if (!file.canRead())
			throw new AccessDeniedException(file.getAbsolutePath());
		ZImage image = null;
		IImportFilterFactory importFilter = ImageActivator.getDefault().getImportFilters().get(imageFormat);
		if (importFilter != null) {
			IImageLoader imageLoader = importFilter.getImageLoader(file);
			image = (bounds != null) ? imageLoader.loadImage(bounds.width, bounds.height, 1, 0f, maxFactor)
					: imageLoader.loadImage(0, 0, 1, 0f, maxFactor);
		}
		if (image == null) {
			try {
				Runtime runtime = Runtime.getRuntime();
				runtime.gc();
				long allocatedMemory = (runtime.totalMemory() - runtime.freeMemory());
				long presumableFreeMemory = runtime.maxMemory() - allocatedMemory;
				image = loadViaImageIO(file, scalingFactor > 0 ? presumableFreeMemory >> 8 : Integer.MAX_VALUE);
				if (image == null)
					image = loadViaImageJ(file);
				if (image == null)
					image = loadExtraFileTypes(file);
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (Exception e) {
				image = loadExtraFileTypes(file);
			}
		}
		if (image != null) {
			if (bounds != null)
				scalingFactor = Math.min(maxFactor,
						rotation % 180 != 0 ? computeScale(image.height, image.width, bounds.width, bounds.height)
								: computeScale(image.width, image.height, bounds.width, bounds.height));
			if (scalingFactor != 1d && scalingFactor > 0)
				image.setScaling((int) (image.width * scalingFactor + 0.5d),
						(int) (image.height * scalingFactor + 0.5d), advanced, 0, null);
			image.setRotation(rotation, 1f, 1f);
			image.setOutputColorConvert(cop);
			image.setOutputSharpening(umask);
			image.setRecipe(rawRecipe, advanced);
			image.setBw(bw);
		}
		return image;
	}

	private static double computeScale(int iWidth, int iHeight, int width, int height) {
		if (width < 0)
			return (double) height / iHeight;
		if (height < 0)
			return (double) width / iWidth;
		return Math.min((double) width / iWidth, (double) height / iHeight);
	}

	// private boolean preferSwt(int format) {
	// if (format == AWTIMAGE)
	// return false;
	// if (format == SWTIMAGE)
	// return true;
	// return !thumbnail && (colorConvertOp == null
	// || bw != null && (recipe == null || recipe.getSplitTone() == null) && (scale
	// == 1d)); // ||
	// // scalingMethod
	// // ==
	// // SCALE_DEFAULT));
	// }

	private static ZImage loadViaImageIO(File file, long maxPix) throws Exception {
		try {
			try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
				Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
				if (readers.hasNext()) {
					ImageReader reader = readers.next();
					try {
						reader.setInput(input);
						int w = reader.getWidth(0);
						int h = reader.getHeight(0);
						double pix = (double) w * h;
						ImageReadParam param = reader.getDefaultReadParam();
						int fac = 1;
						if (pix > maxPix) {
							fac = (int) Math.sqrt(pix / maxPix);
							param.setSourceSubsampling(fac, fac, 0, 0);
						}
						BufferedImage bImage = reader.read(0, param);
						if (bImage != null) {
							ZImage zImage = new ZImage(bImage, file.getAbsolutePath());
							zImage.sourceWidth = w;
							zImage.sourceHeight = h;
							zImage.superSamplingFactor = fac;
							return zImage;
						}
					} finally {
						reader.dispose();
					}
				}
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Loads an image using the ImageJ loader
	 *
	 * @param file
	 *            - image file
	 * @return - the loaded image
	 * @throws Exception
	 */
	private static ZImage loadViaImageJ(File file) throws Exception {
		ImagePlus ijImage = new Opener().openImage(file.getPath());
		return ijImage != null ? new ZImage(ijImage.getBufferedImage(), file.getAbsolutePath()) : null;
	}

	private static boolean reportProgress(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.worked(1);
			return monitor.isCanceled();
		}
		return false;
	}

	/**
	 * Sets the image to black&white
	 *
	 * @param bw
	 *            - true if image is to be delivered as a B&W image
	 */
	public void setBw(RGB bw) {
		this.bw = bw;
	}

	/**
	 * Sets a recipe for RAW development
	 *
	 * @param rawRecipe
	 *            - the recipe
	 * @param advanced
	 *            - true if quality interpolation and antialiasing is to be used
	 */
	public void setRecipe(Recipe rawRecipe, boolean advanced) {
		recipe = rawRecipe;
		advancedRecipe = advanced;
	}

	/**
	 * Sets the unsharp mask for output sharpening
	 *
	 * @param umask
	 *            - unsharp mask
	 */
	public void setOutputSharpening(UnsharpMask umask) {
		this.umask = umask;
	}

	/**
	 * Sets the color conversion operator for output color conversion Typically used
	 * to adapt images to Wide Gamut screens
	 *
	 * @param colorConvertOp
	 *            - the color conversion operator
	 */
	public void setOutputColorConvert(ColorConvertOp colorConvertOp) {
		this.colorConvertOp = colorConvertOp;
	}

	public void setOrientation(int rotation, float scaleX, float scaleY) {
		this.rotation = rotation %= 360;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * Sets the user defined rotation and flip actions
	 *
	 * @param rotation
	 *            - rotation angle in degrees
	 * @param scaleX
	 *            - horizontal scaling factor: -1 or 1
	 * @param scaleY
	 *            - vertical scaling factor: -1 or 1
	 */
	public void setRotation(int rotation, float scaleX, float scaleY) {
		this.userRotation = rotation %= 360;
		this.userScaleX = scaleX;
		this.userScaleY = scaleY;
	}

	/**
	 * Returns the user defined rotation in degrees
	 *
	 * @return user defined rotation in degrees
	 */
	public int getRotation() {
		return userRotation;
	}

	/**
	 * Computes and sets the scaling factor
	 *
	 * @param outputWidth
	 *            - desired output width
	 * @param outputHeight
	 *            - desired output height
	 * @param advanced
	 *            - true if quality interpolation is to be used
	 * @param raster
	 *            - if > 0, image width and height will be adjusted to multiples of
	 *            the raster value. The purpose of this parameter is to facility
	 *            lossless JPEG rotation
	 * @param frameColor
	 *            - color of area surrounding the image or null
	 * @return - the scaling factor
	 */
	public double setScaling(int outputWidth, int outputHeight, boolean advanced, int raster, Color frameColor) {
		scale = ImageUtilities.computeScale(width, height, outputWidth, outputHeight);
		this.advanced = advanced;
		this.raster = raster;
		this.frameColor = frameColor;
		return scale;
	}

	/**
	 * Constructor. Creates an image instance from SWT image data
	 *
	 * @param image
	 *            - the input image data
	 * @param fileName
	 *            - the filename of the image
	 */
	public ZImage(ImageData swtImage, String fileName) {
		this.swtImageData = swtImage;
		this.fileName = fileName;
		this.sourceWidth = this.width = swtImage.width;
		this.sourceHeight = this.height = swtImage.height;
	}

	/**
	 * Constructor. Creates an image instance from a buffered image
	 *
	 * @param bufferedImage
	 *            - the input image
	 * @param fileName
	 *            - the filename of the image
	 */
	public ZImage(BufferedImage bufferedImage, String fileName) {
		this.bufferedImage = bufferedImage;
		this.fileName = fileName;
		this.sourceWidth = this.width = bufferedImage.getWidth();
		this.sourceHeight = this.height = bufferedImage.getHeight();
	}

	/**
	 * Constructor. Creates an image instance from an SWT image
	 *
	 * @param image
	 *            - the input image
	 * @param fileName
	 *            - the filename of the image
	 */
	public ZImage(Image image, String fileName) {
		this.swtImage = image;
		this.fileName = fileName;
		Rectangle bounds = image.getBounds();
		this.sourceWidth = this.width = bounds.width;
		this.sourceHeight = this.height = bounds.height;
		this.currentDevice = image.getDevice();
	}

	public String getFileName() {
		return fileName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fileName == null ? width + "x" + height : fileName + ' ' + width + 'x' + height; //$NON-NLS-1$
	}

	/**
	 * Loads images using Jimi or JAI.
	 *
	 * @param file
	 *            - the image file
	 * @return - the image loaded or null
	 * @throws Exception
	 */
	private static ZImage loadExtraFileTypes(File file) throws Exception {
		String filePath = file.getAbsolutePath();
		try {
			java.awt.Image img = Jimi.getImage(filePath, Jimi.SYNCHRONOUS | Jimi.ONE_SHOT);
			if (img instanceof BufferedImage)
				return new ZImage((BufferedImage) img, filePath);
			if (img instanceof VolatileImage)
				return new ZImage(((VolatileImage) img).getSnapshot(), filePath);
			if (img instanceof ToolkitImage) {
				BufferedImage bi = ((ToolkitImage) img).getBufferedImage();
				if (bi != null)
					return new ZImage(bi, filePath);
			}
		} catch (Exception e) {
			// failed. Try another reader
		}
		BufferedImage bi = JAIReader.read(file);
		return bi == null ? null : new ZImage(bi, filePath);
	}

	/**
	 * Return the dimensions of the image
	 *
	 * @return - image dimensions
	 */
	public synchronized Rectangle getBounds() {
		if (!developed)
			return ((rotation + userRotation) % 180 != 0) ? new Rectangle(0, 0, height, width)
					: new Rectangle(0, 0, width, height);
		return new Rectangle(0, 0, width, height);
	}

	/**
	 * Draws the image to a graphical context
	 *
	 * @param gc
	 *            - the graphical context
	 * @param cropXoffset
	 *            - offset of left crop border
	 * @param cropYoffset
	 *            - offset of top crop border
	 * @param cropWidth
	 *            - width of crop area
	 * @param cropHeight
	 *            - height of crop area
	 * @param canvasXoffset
	 *            - target X position
	 * @param canvasYoffset
	 *            - target Y position
	 * @param canvasWidth
	 *            - target width
	 * @param canvasHeight
	 *            - target height
	 * @param cropMode
	 *            - crop mode
	 * @param preferredWidth
	 *            - preferred image width
	 * @param preferredHeight
	 *            - preferred image height
	 */
	public void draw(GC gc, int cropXoffset, int cropYoffset, int cropWidth, int cropHeight, int canvasXoffset,
			int canvasYoffset, int canvasWidth, int canvasHeight, int cropMode, int preferredWidth, int preferredHeight,
			boolean ignoreRotation) {
		int oldWidth = ignoreRotation || (rotation + userRotation) % 180 == 0 ? width : height;
		Image img = getSwtImage(gc.getDevice(), true, cropMode, preferredWidth, preferredHeight);
		double f = (double) img.getBounds().width / oldWidth;
		gc.drawImage(img, (int) (cropXoffset * f), (int) (cropYoffset * f), (int) (cropWidth * f),
				(int) (cropHeight * f), canvasXoffset, canvasYoffset, canvasWidth, canvasHeight);
	}

	/**
	 * Draws the image to a graphical context
	 *
	 * @param gc
	 *            - the graphical context
	 * @param x
	 *            - X position
	 * @param y
	 *            - Y position
	 * @param cropMode
	 *            - crop mode
	 * @param preferredWidth
	 *            - preferred image width
	 * @param preferredHeight
	 *            - preferred image height
	 */
	public void draw(GC gc, int x, int y, int cropMode, int preferredWidth, int preferredHeight) {
		gc.drawImage(getSwtImage(gc.getDevice(), true, cropMode, preferredWidth, preferredHeight), x, y);
	}

	/**
	 * Return image as a developed image in SWT format
	 *
	 * @param device
	 *            - image device
	 * @param develop
	 *            - true if image is to be developed
	 * @param cropMode
	 *            - crop mode during development
	 * @param preferredWidth
	 *            - preferred image width
	 * @param preferredHeight
	 *            - preferred image height
	 * @return - resulting SWT image
	 */
	public Image getSwtImage(Device device, boolean develop, int cropMode, int preferredWidth, int preferredHeight) {
		if (device != currentDevice || develop && needsDevelopment()) {
			currentDevice = device;
			makeDeviceIndependent();
		}
		if (swtImage == null) {
			if (develop)
				develop(null, device, cropMode, preferredWidth, preferredHeight, SWTIMAGE);
			if (swtImage != null)
				return swtImage;
			if (swtImageData == null) {
				if (labImage != null) {
					swtImageData = labImage.toSwtData();
					labImage = null;
				} else if (hslImage != null) {
					swtImageData = hslImage.toSwtData();
					hslImage = null;
				} else {
					bufferedImage2Swt();
					bufferedImage = null;
				}
			}
			swtImage = new Image(device, swtImageData);
			swtImageData = null;
		}
		return swtImage;
	}

	private boolean needsDevelopment() {
		return scale != 1d || recipe != null || (rotation + userRotation) % 360 != 0 || scaleX * userScaleX != 1f
				|| scaleY * userScaleY != 1f || umask != null || bw != null || colorConvertOp != null;
	}

	/**
	 * Triggers the development process for RAW images
	 *
	 * @param monitor
	 *            - progress monitor
	 * @param device
	 *            - image device
	 * @param cropMode
	 *            - crop mode to be used during development
	 * @param preferredWidth
	 *            - preferred image width
	 * @param preferredHeight
	 *            - preferred image height
	 * @param targetSystem
	 *            - ANY, SWTIMAGE or AWTIMAGE
	 */
	public void develop(IProgressMonitor monitor, Device device, int cropMode, int preferredWidth,
			int preferredHeight, int targetSystem) {
		if (!developed) {
			boolean cropping = cropMode == CROPPED && recipe != null && recipe.getCropping() != null;
			if (scale < 1d && !cropping) {
				downscale(targetSystem);
				if (reportProgress(monitor))
					return;
			}
			if (recipe != null) {
				Vignette vignette = recipe.getVignette();
				if (vignette != null && vignette.type == Vignette.RGB) {
					convertToRGBData(targetSystem);
					if (bufferedImage != null)
						bufferedImage = ImageUtilities.applyVignette(bufferedImage, vignette);
					else if (swtImageData != null)
						swtImageData = ImageUtilities.applyVignette(swtImageData, vignette);
				}
				PerspectiveCorrection perspectiveCorrection = recipe.getPerspectiveCorrection();
				if (perspectiveCorrection != null && perspectiveCorrection.isCorrecting()) {
					convertToRGB(ANY);
					if (bufferedImage != null)
						bufferedImage = ImageUtilities.applyPerspectiveCorrection(bufferedImage, perspectiveCorrection);
					else {
						makeDeviceIndependent();
						swtImageData = ImageUtilities.applyPerspectiveCorrection(swtImageData, perspectiveCorrection);
					}
				}
				Transformation transformation = recipe.getTransform();
				if (transformation != null && transformation.isCorrecting()) {
					convertToRGB(ANY);
					if (bufferedImage != null)
						bufferedImage = ImageUtilities.applyTransformation(bufferedImage, transformation);
					else {
						makeDeviceIndependent();
						swtImageData = ImageUtilities.applyTransformation(swtImageData, transformation);
					}
				}
				Rotation rot = recipe.getRotation();
				if (rot != null)
					setRotation(rot.angle, (rot.flipH) ? -1 : 1, (rot.flipV) ? -1 : 1);
			}
			int angle = (rotation + userRotation) % 360;
			float sx = scaleX * userScaleX;
			float sy = scaleY * userScaleY;
			if (angle != 0 || sx != 1f || sy != 1f) {
				convertToRGB(SWTIMAGE);
				if (bufferedImage != null)
					bufferedImage = ImageUtilities.rotateImage(bufferedImage, angle, sx, sy);
				else if (swtImageData != null)
					swtImageData = ImageUtilities.rotateSwtImage(swtImageData, angle, sx, sy);
				else if (swtImage != null)
					swtImage = ImageUtilities.rotateSwtImage(swtImage, angle, sx, sy);
				if (angle % 180 != 0) {
					int h = width;
					width = height;
					height = h;
				}
				if (reportProgress(monitor))
					return;
			}
			if (recipe != null) {
				if (recipe.needsHslOrLab()) {
					if (recipe.needsHsl()) {
						if (bufferedImage != null) {
							hslImage = recipe.createHslImage(bufferedImage, recipe.hasHsvChannelCurves());
							bufferedImage.flush();
							bufferedImage = null;
						} else if (hslImage == null) {
							makeDeviceIndependent();
							if (swtImageData != null) {
								hslImage = recipe.createHslImage(swtImageData, recipe.hasHsvChannelCurves());
								swtImageData = null;
							}
						}
						recipe.develop(hslImage);
					}
					if (recipe.needsLab()) {
						if (hslImage != null) {
							bufferedImage = hslImage.toBufferedImage();
							hslImage = null;
						}
						if (bufferedImage != null) {
							labImage = recipe.createLabImage(bufferedImage);
							bufferedImage.flush();
							bufferedImage = null;
						} else {
							makeDeviceIndependent();
							if (swtImageData != null) {
								labImage = recipe.createLabImage(swtImageData);
								swtImageData = null;
							}
						}
						recipe.develop(labImage);
					}
					if (reportProgress(monitor))
						return;
				} else {
					ShortLookupTable lookupTable = recipe.constructLookupTable(recipe.getCurves(), 255,
							Curve.CHANNEL_ALL | Curve.CHANNEL_RED | Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE);
					if (lookupTable != null) {
						if (bufferedImage != null)
							bufferedImage = ImageUtilities.applyCurves(bufferedImage, lookupTable);
						else
							makeDeviceIndependent();
						if (swtImageData != null)
							swtImageData = ImageUtilities.applyCurves(swtImageData, lookupTable);
					}
				}
			}
			if (scale > 1d && !cropping) {
				upscale();
				if (reportProgress(monitor))
					return;
			}
			if (umask != null) {
				if (hslImage != null)
					hslImage.applyUnsharpMask(umask);
				else if (labImage != null)
					labImage.applyUnsharpMask(umask);
				else if (bufferedImage != null)
					new UnsharpFilter(umask).filter(bufferedImage, bufferedImage);
				else {
					makeDeviceIndependent();
					if (swtImageData != null)
						new UnsharpFilter(umask).filter(swtImageData);
				}
				if (reportProgress(monitor))
					return;
			}
			if (bw != null) {
				if (hslImage != null)
					hslImage.toBw();
				else if (labImage != null)
					labImage.toBw();
				else if (bufferedImage != null)
					bufferedImage = ImageUtilities.convert2Bw(bufferedImage, bw);
				else {
					makeDeviceIndependent();
					if (swtImageData != null)
						ImageUtilities.convert2Bw(swtImageData, bw);
				}
				if (reportProgress(monitor))
					return;
			}
			SplitTone splitTone = recipe == null ? null : recipe.getSplitTone();
			if (splitTone != null) {
				convertToRGB(ANY);
				if (bufferedImage != null)
					bufferedImage = ImageUtilities.applySplitTone(bufferedImage, splitTone);
				else {
					makeDeviceIndependent();
					if (swtImageData != null)
						swtImageData = ImageUtilities.applySplitTone(swtImageData, splitTone);
				}
			}
			if (colorConvertOp != null && (bw == null || splitTone != null)) {
				if (hslImage != null) {
					bufferedImage = hslImage.toBufferedImage();
					hslImage = null;
				} else if (labImage != null) {
					bufferedImage = labImage.toBufferedImage();
					hslImage = null;
				} else
					convertToBuffered();
				try {
					WritableRaster r = bufferedImage.getRaster();
					colorConvertOp.filter(r, r); //TODO still to slow
				} catch (Exception e) {
					// can't convert color profile
				}
				if (reportProgress(monitor))
					return;
			}
			if (cropMode != UNCROPPED && recipe != null && recipe.getCropping() != null) {
				if (scale != 1d && cropping) {
					if (preferredWidth < 0)
						preferredWidth = (int) (scale * width + 0.5d);
					if (preferredHeight < 0)
						preferredHeight = (int) (scale * height + 0.5d);
				}
				crop(device, cropMode, preferredWidth, preferredHeight);
			}
			developed = true;
		}
	}

	protected void convertToBuffered() {
		makeDeviceIndependent();
		if (swtImageData != null) {
			bufferedImage = ImageUtilities.swtImage2buffered(swtImageData, colorSpace);
			swtImageData = null;
		}
	}

	public final static int ANY = 0;
	public final static int SWTIMAGE = 1;
	public final static int AWTIMAGE = 2;

	private void convertToRGB(int format) { // format ignored at the moment

		if (hslImage != null) {
			// if (preferSwt(format)) {
			swtImageData = hslImage.toSwtData();
			hslImage = null;
			// } else {
			// bufferedImage = hslImage.toBufferedImage();
			// hslImage = null;
			// }
		} else if (labImage != null) {
			// if (preferSwt(format)) {
			swtImageData = labImage.toSwtData();
			labImage = null;
			// } else {
			// bufferedImage = labImage.toBufferedImage();
			// labImage = null;
			// }
		} else if (bufferedImage != null) {
			if (bufferedImage.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_CMYK)
				convertBufferedImageToRGB(format);
			else
				bufferedImage2Swt(); // our own SWT routines are faster
			// if (bufferedImage.getColorModel().getColorSpace().getType() ==
			// ColorSpace.TYPE_CMYK)
			// convertBufferedImageToRGB(format);
			// else if (!(bufferedImage.getRaster().getDataBuffer() instanceof
			// DataBufferInt))
			// convertBufferedImageToRGB(format);
			// else
			// for (int size : bufferedImage.getColorModel().getComponentSize())
			// if (size != 8) {
			// convertBufferedImageToRGB(format); // terribly slow for AWTIMAGE and 16 bit
			// TIFF
			// break;
			// }
		}
	}

	private void convertBufferedImageToRGB(int format) {
		// if (preferSwt(format))
		// bufferedImage2Swt();
		// else
		bufferedImage = ImageUtilities.convertToDirectColor(bufferedImage, thumbnail);
	}

	protected void bufferedImage2Swt() {
		swtImageData = ImageUtilities.bufferedImage2swt(bufferedImage);
		colorSpace = bufferedImage.getColorModel().getColorSpace();
		bufferedImage.flush();
		bufferedImage = null;
	}

	private void convertToRGBData(int format) {
		convertToRGB(format);
		makeDeviceIndependent();
	}

	private void makeDeviceIndependent() {
		if (swtImage != null) {
			if (!swtImage.isDisposed()) {
				swtImageData = swtImage.getImageData();
				swtImage.dispose();
			}
			swtImage = null;
		}
	}

	private void crop(Device device, int cropMode, int preferredWidth, int preferredHeight) {
		Cropping cropping = recipe.getCropping();
		if (cropping.init(width, height, recipe.getScaling() / recipe.getSampleFactor()))
			crop(device, cropMode, preferredWidth, preferredHeight, cropping);
	}

	/**
	 * Crops and scales the image
	 *
	 * @param device
	 *            - Display or Printer instance or null for default display
	 * @param cropMode
	 *            - UNCROPPED CROPPED CROPMASK
	 * @param preferredWidth
	 *            - targetWidth
	 * @param preferredHeight
	 *            - targetHeight
	 * @param cropping
	 *            - crop specification
	 */
	public void crop(Device device, int cropMode, int preferredWidth, int preferredHeight, Cropping cropping) {
		if (device == null)
			device = Display.getDefault();
		getSwtImage(device, false, UNCROPPED, SWT.DEFAULT, SWT.DEFAULT);
		switch (cropMode) {
		case CROPPED: {
			float x1 = cropping.cropLeft * width;
			float y1 = cropping.cropTop * height;
			float x2 = cropping.cropRight * width;
			float y2 = cropping.cropBottom * height;
			int cwidth = (int) (x2 - x1 + 0.5f);
			int cheight = (int) (y2 - y1 + 0.5f);
			float scaleFactor = 1f;
			float cscale = 1f;
			if (preferredHeight >= 0 || preferredWidth >= 0) {
				int h = preferredHeight >= 0 ? preferredHeight : height;
				int w = preferredWidth >= 0 ? preferredWidth : width;
				scaleFactor = Math.min((float) w / width, (float) h / height);
				cscale = Math.min((float) w / cwidth, (float) h / cheight);
			}
			if (cropping.fillMode == Cropping.FILL) {
				float c = (float) Math.cos(Math.toRadians(cropping.cropAngle));
				if (c != 0f)
					scaleFactor /= c;
			}
			int nwidth = (int) (cwidth * cscale + 0.5f);
			int nheight = (int) (cheight * cscale + 0.5f);
			width = (int) (width * scaleFactor + 0.5f);
			height = (int) (height * scaleFactor + 0.5f);
			float cfac = cscale / scaleFactor;
			Transform transform = new Transform(device);
			transform.scale(cfac, cfac);
			transform.translate(-x1 * scaleFactor, -y1 * scaleFactor);
			transform.translate(width / 2, height / 2);
			transform.rotate(cropping.cropAngle);
			transform.translate(-width / 2, -height / 2);
			transform.scale(scaleFactor, scaleFactor);
			width = nwidth;
			height = nheight;
			if (width >= 0 && height >= 0) {
				Image newImage = new Image(device, width, height);
				GC gc = new GC(newImage);
				if (cropping.fillColor != null) {
					Color color = new Color(device, cropping.fillColor);
					gc.setBackground(color);
					gc.fillRectangle(0, 0, width, height);
					color.dispose();
				}
				gc.setTransform(transform);
				if (advancedRecipe && (cropping.cropAngle != 0f || scaleFactor != 1f)) {
					gc.setAntialias(SWT.ON);
					gc.setInterpolation(SWT.HIGH);
				}
				gc.drawImage(swtImage, 0, 0);
				gc.dispose();
				transform.dispose();
				swtImage.dispose();
				swtImage = newImage;
			}
			break;
		}
		case CROPMASK: {
			Image newImage = new Image(device, width, height);
			GC gc = new GC(newImage);
			gc.drawImage(swtImage, 0, 0);
			swtImage.dispose();
			swtImage = newImage;
			float x1 = cropping.cropLeft * width;
			float y1 = cropping.cropTop * height;
			float x2 = cropping.cropRight * width;
			float y2 = cropping.cropBottom * height;
			float[] points = new float[] { x1, y1, x2, y1, x2, y2, x1, y2 };
			if (cropping.cropAngle != 0) {
				Transform transform = new Transform(device);
				transform.translate(width / 2, height / 2);
				transform.rotate(-cropping.cropAngle);
				transform.translate(-width / 2, -height / 2);
				transform.transform(points);
				transform.dispose();
			}
			int[] pline = new int[] { (int) points[0], (int) points[1], (int) points[2], (int) points[3],
					(int) points[4], (int) points[5], (int) points[6], (int) points[7] };
			gc.setAntialias(SWT.ON);
			// gc.setAdvanced(true);
			Region region = new Region(device);
			region.add(0, 0, width, height);
			region.subtract(pline);
			gc.setClipping(region);
			gc.setBackground(device.getSystemColor(SWT.COLOR_BLACK));
			gc.setAlpha(128);
			gc.fillRectangle(0, 0, width, height);
			gc.setClipping((Rectangle) null);
			region.dispose();
			gc.setForeground(device.getSystemColor(SWT.COLOR_RED));
			gc.setAlpha(255);
			gc.setLineWidth(2);
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawPolygon(pline);
			gc.dispose();
			break;
		}
		}
	}

	private void downscale(int format) {
		int nwidth = (int) (scale * width + 0.5d);
		int nheight = (int) (scale * height + 0.5d);
		convertToRGBData(format);
		if (bufferedImage != null)
			downScaleBufferedImage(nwidth, nheight, format);
		else if (swtImageData != null) {
			ImageData data = ImageUtilities.downSample(swtImageData, nwidth, nheight, 0);
			if (data != null) {
				swtImageData = data;
				width = data.width;
				height = data.height;
			}
		}
	}

	private void downScaleBufferedImage(int nwidth, int nheight, int format) {
		convertToRGB(format);
		BufferedImage downscaled = ImageUtilities.downSample(bufferedImage, nwidth, nheight, raster, advanced);
		if (downscaled != null) {
			bufferedImage = downscaled;
			width = downscaled.getWidth();
			height = downscaled.getHeight();
		}
	}

	private void upscale() {
		int nwidth = (int) (scale * width + 0.5d);
		int nheight = (int) (scale * height + 0.5d);
		convertToRGB(SWTIMAGE);
		if (bufferedImage != null)
			bufferedImage2Swt();
		Rectangle bounds = null;
		if (swtImageData != null) {
			swtImage = ImageUtilities.scaleSWT(
					new Image((currentDevice == null) ? Display.getDefault() : currentDevice, swtImageData), nwidth,
					nheight, advanced, raster, true, frameColor);
			bounds = swtImage.getBounds();
		} else if (swtImage != null) {
			swtImage = ImageUtilities.scaleSWT(swtImage, nwidth, nheight, advanced, raster, true, frameColor);
			bounds = swtImage.getBounds();
		}
		if (bounds != null) {
			width = bounds.width;
			height = bounds.height;
		}
	}

	/**
	 * Disposes the image and its resources
	 */
	public void dispose() {
		dispose(null);
	}

	/**
	 * Disposes the image and its resources
	 */
	public void dispose(Image protect) {
		disposeResources(protect);
		if (bufferedImage != null) {
			bufferedImage.flush();
			bufferedImage = null;
		}
		if (swtImage != null) {
			swtImage.dispose();
			swtImage = null;
		}
		hslImage = null;
		isDisposed = true;
	}

	private void disposeResources(Image protect) {
		if (swtImage != null && swtImage != protect) {
			swtImage.dispose();
			swtImage = null;
		}
	}

	/**
	 * Tests if the image is disposed
	 *
	 * @return - true if image is disposed
	 */
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Save the developed image to on output stream
	 *
	 * @param monitor
	 *            - progress monitor
	 * @param develop
	 *            - true if image is to be developed
	 * @param cropMode
	 *            - crop mode used during development
	 * @param preferredWidth
	 *            - preferred image width. Specify -1 (SWT.DEFAULT) for NO
	 *            preferredWidth
	 * @param preferredHeight
	 *            - preferred image height. 0 indicates that the image is cropped to
	 *            a square format in the size specified with preferredWidth. Specify
	 *            -1 (SWT.DEFAULT) for NO preferredHeight
	 * @param out
	 *            - output stream
	 * @param format
	 *            - format: either SWT.IMAGE_PNG, SWT.IMAGE_JPEG, or
	 *            ZImage.IMAGE_WEBP
	 * @throws IOException
	 */
	public void saveToStream(final IProgressMonitor monitor, boolean develop, final int cropMode,
			final int preferredWidth, final int preferredHeight, OutputStream out, int format) throws IOException {
		saveToStream(monitor, develop, cropMode, preferredWidth, preferredHeight, out, format, -1);
	}

	/**
	 * Save the developed image to on output stream
	 *
	 * @param monitor
	 *            - progress monitor
	 * @param develop
	 *            - true if image is to be developed
	 * @param cropMode
	 *            - crop mode used during development
	 * @param preferredWidth
	 *            - preferred image width. Specify -1 (SWT.DEFAULT) for NO
	 *            preferredWidth
	 * @param preferredHeight
	 *            - preferred image height. 0 indicates that the image is cropped to
	 *            a square format in the size specified with preferredWidth. Specify
	 *            -1 (SWT.DEFAULT) for NO preferredHeight
	 * @param out
	 *            - output stream
	 * @param format
	 *            - format: either SWT.IMAGE_PNG, SWT.IMAGE_JPEG, or
	 *            ZImage.IMAGE_WEBP
	 * @param quality
	 *            - a value between -1 and 100 determining the compression quality
	 *            (100 = high quality, 1 = low quality, -1, 0 = default (only used
	 *            for JPEG and WebP, default = 75)
	 * @throws IOException
	 */
	public synchronized void saveToStream(final IProgressMonitor monitor, boolean develop, final int cropMode,
			final int preferredWidth, final int preferredHeight, OutputStream out, int format, int quality)
			throws IOException {
		ImageLoader loader = null;
		final int pheight = preferredHeight == 0 ? preferredWidth : preferredHeight;
		if (develop && !developed) {
			final Display display = Display.getDefault();
			if (!display.isDisposed())
				display.syncExec(() -> develop(monitor, display, cropMode, preferredWidth, pheight, ANY));
		}
		if (preferredHeight != 0 || format == IMAGE_WEBP) {
			if (format == IMAGE_WEBP)
				convertToBuffered();
			convertToRGB(AWTIMAGE);
			if (bufferedImage != null) {
				try {
					if (ImageUtilities.saveBufferedImageToStream(bufferedImage, out, format, quality))
						return;
				} catch (IOException e) {
					throw new IIOException(Messages.ZImage_cant_create_output_stream, e);
				}
				bufferedImage2Swt();
			}
			makeDeviceIndependent();
			if (swtImageData != null)
				loader = new ImageLoader();
		} else {
			convertToRGB(SWTIMAGE);
			if (bufferedImage != null)
				bufferedImage2Swt();
			else
				makeDeviceIndependent();
			if (swtImageData != null) {
				int w = swtImageData.width;
				int h = swtImageData.height;
				if (w != h) {
					final Display display = Display.getDefault();
					int nwidth = Math.min(w, h);
					Image oldImage = new Image(display, swtImageData);
					swtImageData = null;
					Image newImage = new Image(display, nwidth, nwidth);
					GC gc = new GC(newImage);
					gc.drawImage(oldImage, (w - nwidth) / 2, (h - nwidth) / 2, nwidth, nwidth, 0, 0, nwidth, nwidth);
					gc.dispose();
					oldImage.dispose();
					swtImageData = newImage.getImageData();
					newImage.dispose();
					width = nwidth;
					height = nwidth;
				}
				loader = new ImageLoader();
			}
		}
		if (loader != null) {
			loader.data = new ImageData[] { swtImageData };
			if (format == SWT.IMAGE_JPEG && quality > 0)
				loader.compression = quality;
			loader.save(out, format);
		}
	}

	public int getSuperSamplingFactor() {
		return superSamplingFactor;
	}

	public boolean isSRGB() {
		int type = bufferedImage != null ? bufferedImage.getColorModel().getColorSpace().getType()
				: colorSpace.getType();
		return type == ColorSpace.CS_sRGB || type == ColorSpace.TYPE_CMYK;
	}

}
