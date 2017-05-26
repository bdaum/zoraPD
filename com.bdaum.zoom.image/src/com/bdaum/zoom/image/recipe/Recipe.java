/*******************************************************************************
 * Copyright (c) 2009-2013 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.recipe;

import java.awt.image.BufferedImage;
import java.awt.image.ShortLookupTable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.ImageData;

import com.bdaum.zoom.image.internal.awt.HSLimage;
import com.bdaum.zoom.image.internal.awt.LabImage;

/**
 * This class describes a RAW file development recipe as produced by RAW
 * converters such as Adobe Camera Raw, Capture One, Raw Therapee and others.
 *
 */
public class Recipe {
	/**
	 * Class describing derived images. Some RAW converts (e.g. Capture One)
	 * keep track of the generated output files
	 *
	 */
	public class Derivative {
		/**
		 * Variant ID or number
		 */
		public String id;
		/**
		 * Date when output file was created
		 */
		public Date creationDate;
		/**
		 * URL of output file
		 */
		public String url;

		/**
		 * Constructor.
		 *
		 * @param id
		 *            - variant id or number
		 *
		 * @param creationDate
		 *            - Date when output file was created
		 * @param url
		 *            - URL of output file
		 */
		public Derivative(String id, Date creationDate, String url) {
			this.id = id;
			this.creationDate = creationDate;
			this.url = url;
		}
	}

	/**
	 * White balance from camera will be used
	 */
	public static final int wbASSHOT = 0;
	/**
	 * White balance determination is left to RAW converter (dcraw)
	 */
	public static final int wbAUTO = 1;
	/**
	 * White balance is adjusted by supplying weight factors to dcraw
	 */
	public static final int wbFACTORS = 2;
	/**
	 * White balance is done in some other way
	 */
	public static final int wbNONE = 3;
	/**
	 * Indicates the presence of a recipe but that recipe will not be evaluated
	 */
	public static final Recipe NULL = new Recipe(null, null, false);


	private static final float[][] XYZ_to_RGB = {
			{ 3.24071f, -0.969258f, 0.0556352f },
			{ -1.53726f, 1.87599f, -0.203996f },
			{ -0.498571f, 0.0415557f, 1.05707f } };
	private static final int[] RGB = new int[] { Curve.CHANNEL_RED,
			Curve.CHANNEL_GREEN, Curve.CHANNEL_BLUE };
	private static final float _1F8 = 255f * 256f;

	private static float[] rgb6500 = new float[] { 1f, 1f, 1f };

	static {
		rgb6500 = temperature2RGB(6500);
	}

	// DCRAW
	// Parameters use the value range defined by DCRAW
	/**
	 * wbASSHOT, wbAUTO, wbFACTORS, wbNONE
	 */
	public int whiteBalanceMethod = wbASSHOT;
	/**
	 * Noise reduction threshold (100 - 1000)
	 */
	public int noiseReduction = 0;
	/**
	 * Exposure factor
	 */
	public float exposure = 1f;

	/**
	 * Gamma exponent
	 */
	public float gamma = Float.NaN;

	/**
	 * Highlight recovery (3...9)
	 */
	public int highlightRecovery = 0;
	/**
	 * Chromatic abberation red (0.999 .. 0.001)
	 */
	public float chromaticAberrationR = 1f;
	/**
	 * Chromatic abberation blue (0.999 .. 0.001)
	 */
	public float chromaticAberrationB = 1f;
	/**
	 * Prefer HSL color space
	 */
	public boolean useHSL;

	// Postprocessing
	private double scalingFactor = 1d;
	private List<Curve> curves = new ArrayList<Curve>(4);
	private List<UnsharpMask> unsharpMasks = new ArrayList<UnsharpMask>(2);
	private int sampleFactor = 1;
	private ColorShift colorShift;
	private SplitTone splitTone;
	private Vignette vignette;
	private GrayConvert grayConvert;
	private Cropping cropping;
	private float[] wbFactors;
	// private int rating = -1;
	// private int colorCode = -1;
	private List<Derivative> derivatives;
	// private String ratedBy;
	private String location;
	private String tool;
	private float[][] channelMatrix;
	private Rotation rotation;
	private ColorBoost colorBoost;
	private PerspectiveCorrection perspectiveCorrection;
	private boolean useHSV;
	// private List<String> keywords;
	private Transformation transform;

	public Recipe(String tool, String location, boolean useHSL) {
		this.tool = tool;
		this.location = location;
		this.useHSL = useHSL;
	}

	/**
	 * Sets a custom color temperature and tint
	 *
	 * @param colorTemperature
	 * @param tint
	 */
	public void setColorTemperature(float colorTemperature, float tint) {
		float[] rgb = temperature2RGB(colorTemperature);
		rgb[1] /= tint;
		setColorBalance(rgb);
	}

	/**
	 * Sets the color balance
	 *
	 * @param rgb
	 *            - red, green, blue factors
	 */
	public void setColorBalance(float[] rgb) {
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] != 0f) {
				Curve c = new Curve(Curve.TYPE_LINEAR, "color", RGB[i], 0f); //$NON-NLS-1$
				c.addKnot(0f, 0f);
				c.addKnot(1f, 1 / rgb[i]);
				addCurve(c);
			}
		}
		// byte[][] table = colorCurve == null ? null : colorCurve.getTable();
		// if (table == null)
		// table = new byte[3][256];
		// float f, d;
		// for (int i = 0; i < table.length; i++) {
		// byte[] comp = table[i];
		// f = rgb[i];
		// if (f != 0f) {
		// f = 1f / f;
		// for (int j = 0; j < comp.length; j++) {
		// d = (colorCurve == null ? j : (comp[j] & 0xFF)) * f + 0.5f;
		// if (d > 255f)
		// comp[j] = -1;
		// else if (d > 0)
		// comp[j] = (byte) d;
		// }
		// }
		// }
		// colorCurve = new ByteLookupTable(0, table);
	}

	// /**
	// * Adds color curves
	// *
	// * @param redCurve
	// * @param greenCurve
	// * @param blueCurve
	// */
	// public void addColorCurves(Curve redCurve, Curve greenCurve, Curve
	// blueCurve) {
	// short[][] ctables = new short[3][];
	// ctables[0] = redCurve == null || redCurve.isEmpty()
	// || redCurve.isIdentity() ? null : redCurve.makeTable(255);
	// ctables[1] = greenCurve == null || greenCurve.isEmpty()
	// || greenCurve.isIdentity() ? null : greenCurve.makeTable(255);
	// ctables[2] = blueCurve == null || blueCurve.isEmpty()
	// || blueCurve.isIdentity() ? null : blueCurve.makeTable(255);
	// byte[][] table = colorCurve == null ? null : colorCurve.getTable();
	// if (table == null)
	// table = new byte[3][256];
	// float d;
	// for (int i = 0; i < table.length; i++) {
	// byte[] comp = table[i];
	// short[] ctable = ctables[i];
	// for (int j = 0; j < comp.length; j++) {
	// if (colorCurve == null)
	// d = ctable == null ? j : ctable[j];
	// else
	// d = ctable == null ? comp[j] & 0xFF
	// : comp[ctable[j]] & 0xFF;
	// if (d > 255f)
	// comp[j] = -1;
	// else if (d > 0)
	// comp[j] = (byte) d;
	// }
	// }
	// colorCurve = new ByteLookupTable(0, table);
	// }

	/**
	 * Converts a color temperature to RGB factors Based on UFRaws
	 * Temperature_to_RGB routine
	 *
	 * @param t
	 *            - temperature
	 * @return RGB factors
	 */
	public static float[] temperature2RGB(float t) {
		int c;
		float xD, yD;
		// Fit for CIE Daylight illuminant
		if (t <= 4000) {
			xD = 0.27475e9f / (t * t * t) - 0.98598e6f / (t * t) + 1.17444e3f
					/ t + 0.145986f;
		} else if (t <= 7000) {
			xD = -4.6070e9f / (t * t * t) + 2.9678e6f / (t * t) + 0.09911e3f
					/ t + 0.244063f;
		} else {
			xD = -2.0064e9f / (t * t * t) + 1.9018e6f / (t * t) + 0.24748e3f
					/ t + 0.237040f;
		}
		yD = -3f * xD * xD + 2.87f * xD - 0.275f;

		// Fit for Blackbody using CIE standard observer function at 2 degrees
		// xD = -1.8596e9/(T*T*T) + 1.37686e6/(T*T) + 0.360496e3/T + 0.232632;
		// yD = -2.6046*xD*xD + 2.6106*xD - 0.239156;

		// Fit for Blackbody using CIE standard observer function at 10 degrees
		// xD = -1.98883e9/(T*T*T) + 1.45155e6/(T*T) + 0.364774e3/T + 0.231136;
		// yD = -2.35563*xD*xD + 2.39688*xD - 0.196035;

		float x = xD / yD;
		float y = 1;
		float z = (1 - xD - yD) / yD;
		float max = 0f;
		float min = Float.MAX_VALUE;
		float[] rgb = new float[3];
		for (c = 0; c < 3; c++) {
			rgb[c] = (x * XYZ_to_RGB[0][c] + y * XYZ_to_RGB[1][c] + z
					* XYZ_to_RGB[2][c])
					/ rgb6500[c];
			if (rgb[c] > max)
				max = rgb[c];
			if (rgb[c] < min)
				min = rgb[c];
		}
		for (c = 0; c < 3; c++)
			rgb[c] /= ((max + min) / 2);
		return rgb;
	}

	private static ShortLookupTable constructLookupTable(Curve c, int scale, int filter) {
		short[][] result = null;
		int channel = c.getChannel();
		if ((filter & channel) == 0)
			return null;
		short[] t = c.makeTable(scale);
		if (t == null)
			return null;
		switch (channel) {
		case Curve.CHANNEL_ALL:
		case Curve.CHANNEL_LUMINANCE:
			result = new short[][] { t };
			break;
		case Curve.CHANNEL_RED:
			result = new short[][] { t, c.makeIdCurve(scale),
					c.makeIdCurve(scale) };
			break;
		case Curve.CHANNEL_GREEN:
			result = new short[][] { c.makeIdCurve(scale), t,
					c.makeIdCurve(scale) };
			break;
		case Curve.CHANNEL_BLUE:
			result = new short[][] { c.makeIdCurve(scale), t,
					c.makeIdCurve(scale) };
			break;
		}
		return result == null ? null : new ShortLookupTable(0, result);
	}

	public ShortLookupTable constructLookupTable(List<Curve> c, int scale,
			int filter) {
		return constructLookupTable(c, scale, scale, filter);
	}

	private static ShortLookupTable constructLookupTable(List<Curve> curveList,
			int maxl, int maxab, int filter) {

		short[][] result = null;
		for (Curve c : curveList) {
			if (c.getMask() != null)
				continue;
			int channel = c.getChannel();
			if ((filter & channel) == 0 || c.isEmpty() || c.isIdentity())
				continue;
			int scale;
			switch (channel) {
			case Curve.CHANNEL_ALL:
			case Curve.CHANNEL_LUMINANCE:
			case Curve.CHANNEL_RED:
			case Curve.CHANNEL_L:
			case Curve.CHANNEL_HUE:
				scale = maxl;
				break;
			default:
				scale = maxab;
				break;
			}
			short[] t = c.makeTable(scale);
			if (t == null)
				return null;
			if (result == null) {
				switch (channel) {
				case Curve.CHANNEL_ALL:
				case Curve.CHANNEL_LUMINANCE:
				case Curve.CHANNEL_CHROMA:
					result = new short[][] { t };
					break;
				case Curve.CHANNEL_RED:
				case Curve.CHANNEL_L:
				case Curve.CHANNEL_HUE:
					result = new short[][] { t, c.makeIdCurve(maxab),
							c.makeIdCurve(maxab) };
					break;
				case Curve.CHANNEL_GREEN:
				case Curve.CHANNEL_A:
				case Curve.CHANNEL_SATURATION:
					result = new short[][] { c.makeIdCurve(maxl), t,
							c.makeIdCurve(maxab) };
					break;
				case Curve.CHANNEL_BLUE:
				case Curve.CHANNEL_B:
				case Curve.CHANNEL_VALUE:
					result = new short[][] { c.makeIdCurve(maxl),
							c.makeIdCurve(maxab), t };
					break;
				}
			} else if (result.length == 3) {
				switch (channel) {
				case Curve.CHANNEL_ALL:
					result[0] = c.fold(result[0], t);
					result[1] = c.fold(result[1], t);
					result[2] = c.fold(result[2], t);
					break;
				case Curve.CHANNEL_RED:
				case Curve.CHANNEL_LUMINANCE:
				case Curve.CHANNEL_L:
				case Curve.CHANNEL_HUE:
					result[0] = c.fold(result[0], t);
					break;
				case Curve.CHANNEL_GREEN:
				case Curve.CHANNEL_A:
				case Curve.CHANNEL_SATURATION:
					result[1] = c.fold(result[1], t);
					break;
				case Curve.CHANNEL_BLUE:
				case Curve.CHANNEL_B:
				case Curve.CHANNEL_VALUE:
					result[2] = c.fold(result[2], t);
					break;
				}
			} else {
				switch (channel) {
				case Curve.CHANNEL_ALL:
				case Curve.CHANNEL_LUMINANCE:
				case Curve.CHANNEL_L:
				case Curve.CHANNEL_HUE:
					result[0] = c.fold(result[0], t);
					break;
				case Curve.CHANNEL_RED:
					result = new short[][] { c.fold(result[0], t),
							cloneCurve(result[0]), cloneCurve(result[0]) };
					break;
				case Curve.CHANNEL_GREEN:
					result = new short[][] { cloneCurve(result[0]),
							c.fold(result[0], t), cloneCurve(result[0]) };
					break;
				case Curve.CHANNEL_BLUE:
					result = new short[][] { cloneCurve(result[0]),
							cloneCurve(result[0]), c.fold(result[0], t) };
					break;
				case Curve.CHANNEL_A:
				case Curve.CHANNEL_SATURATION:
					result = new short[][] { cloneCurve(result[0]), t,
							c.makeIdCurve(maxab) };
					break;
				case Curve.CHANNEL_B:
				case Curve.CHANNEL_VALUE:
					result = new short[][] { cloneCurve(result[0]),
							c.makeIdCurve(maxab), t };
					break;
				}
			}
		}
		return result == null ? null : new ShortLookupTable(0, result);
	}

	private static short[] cloneCurve(short[] s) {
		short[] nc = new short[s.length];
		for (int j = 0; j < s.length; j++)
			nc[j] = s[j];
		return nc;
	}

	/**
	 * Set shift values for hue and saturation Can only be used for recipes that
	 * use the HSL color space
	 *
	 * @param colorShift
	 */
	public void setHSL(ColorShift colorShift) {
		if (colorShift != null && colorShift.isShifting)
			this.colorShift = colorShift;
		else
			this.colorShift = null;
	}

	/**
	 * Set the parameters for converting the image to grayscale. Can only be
	 * used for recipes that use the HSL color space
	 *
	 * @param grayConvert
	 */
	public void setGrayConvert(GrayConvert grayConvert) {
		if (grayConvert != null && grayConvert.isConverting())
			this.grayConvert = grayConvert;
		else
			this.grayConvert = null;
	}

	/**
	 * Sets the scaling factor for all operations with pixel dimensions, e.g.
	 * the unsharp mask radius
	 *
	 * @param scale
	 *            the scale to set
	 */
	public void setScaling(double scale) {
		this.scalingFactor = scale;
	}

	/**
	 * Returns the current image scaling factor
	 *
	 * @return - scaling
	 */
	public double getScaling() {
		return scalingFactor;
	}

	/**
	 * Perform all image transformations that can take place in the HSL color
	 * space
	 *
	 * @param hslImage
	 *            - HSL image
	 */
	public void develop(HSLimage hslImage) {
		if (vignette != null && vignette.type == Vignette.HSL)
			hslImage.applyVignette(vignette);
		ShortLookupTable hsvTable = constructLookupTable(getCurves(), 255,
				Curve.CHANNEL_HUE | Curve.CHANNEL_SATURATION
						| Curve.CHANNEL_VALUE);
		if (hsvTable != null)
			hslImage.applyLookup(hsvTable);
		hslImage.toHSL();
		if (!curves.isEmpty()) {
			ShortLookupTable table = constructLookupTable(curves, 255,
					Curve.CHANNEL_LUMINANCE | Curve.CHANNEL_ALL);
			if (table != null)
				hslImage.applyLookup(table);
			for (Curve c : curves) {
				IMask mask = c.getMask();
				if (mask != null) {
					table = constructLookupTable(c, 255,
							Curve.CHANNEL_LUMINANCE);
					if (table != null)
						hslImage.applyMaskedLookup(table, mask);
				}
			}
			removeCurves(curves, Curve.CHANNEL_LUMINANCE | Curve.CHANNEL_ALL);
		}
		if (grayConvert != null) {
			grayConvert(hslImage, grayConvert);
			grayConvert = null;
		} else if (colorShift != null) {
			hslImage.applyColorShift(colorShift);
			colorShift = null;
		}
		Iterator<UnsharpMask> iterator = unsharpMasks.iterator();
		while (iterator.hasNext()) {
			UnsharpMask mask = iterator.next();
			float radius = mask.radius;
			double r = radius * (scalingFactor / sampleFactor);
			if (r <= 0.1f)
				continue;
			mask.radius = (float) r;
			hslImage.applyUnsharpMask(mask);
			mask.radius = radius;
			iterator.remove();
		}
	}

	/**
	 * Perform all image transformations that can take place in the XYZ color
	 * space
	 *
	 * @param labImage
	 *            - L*a*b* image
	 */
	public void develop(LabImage labImage) {
		if (!curves.isEmpty()) {
			int filter = Curve.CHANNEL_LUMINANCE | Curve.CHANNEL_L
					| Curve.CHANNEL_A | Curve.CHANNEL_B;
			ShortLookupTable table = constructLookupTable(curves,
					LabImage.MAXL, LabImage.MAXAB, filter);
			if (table != null)
				labImage.applyLookup(table, filter);
			for (Curve c : curves) {
				IMask mask = c.getMask();
				if (mask != null) {
					table = constructLookupTable(c, LabImage.MAXL,
							Curve.CHANNEL_LUMINANCE);
					if (table != null)
						labImage.applyMaskedLookup(table, mask);
				}
			}
			table = constructLookupTable(curves, LabImage.MAXL, LabImage.MAXL,
					Curve.CHANNEL_CHROMA);
			if (table != null)
				labImage.applyLookup(table, Curve.CHANNEL_CHROMA);
			removeCurves(curves, filter | Curve.CHANNEL_CHROMA);
		}
		if (colorBoost != null) {
			labImage.applyColorBoost(colorBoost);
			colorBoost = null;
		}
		Iterator<UnsharpMask> iterator = unsharpMasks.iterator();
		while (iterator.hasNext()) {
			UnsharpMask mask = iterator.next();
			float radius = mask.radius;
			float r = (float) (radius * (scalingFactor / sampleFactor));
			if (r <= 0.1f)
				continue;
			mask.radius = r;
			labImage.applyUnsharpMask(mask);
			mask.radius = radius;
			iterator.remove();
		}
	}

	/**
	 * Test if there is a luminance transformation scheduled
	 *
	 * @return true if a luminance transformation is scheduled
	 */
	public boolean hasLuminanceCurve() {
		for (Curve c : curves)
			if (c.getChannel() == Curve.CHANNEL_LUMINANCE && !c.isEmpty()
					&& !c.isIdentity())
				return true;
		return false;
	}

	public boolean hasLabCurves() {
		for (Curve c : curves) {
			int channel = c.getChannel();
			if (channel == Curve.CHANNEL_L || channel == Curve.CHANNEL_A
					|| channel == Curve.CHANNEL_B
					|| channel == Curve.CHANNEL_CHROMA && !c.isEmpty()
					&& !c.isIdentity())
				return true;
		}
		return false;
	}

	private static void grayConvert(HSLimage hslImage, GrayConvert gConvert) {
		float sum = 0f;
		float[] lumByHue = new float[256];
		for (int i = 0; i < 256; i++) {
			lumByHue[i] = bell(i, gConvert.grayMixerRed, 0, 64)
					+ bell(i, gConvert.grayMixerOrange, 32, 64)
					+ bell(i, gConvert.grayMixerYellow, 64, 64)
					+ bell(i, gConvert.grayMixerGreen, 96, 64)
					+ bell(i, gConvert.grayMixerAqua, 128, 64)
					+ bell(i, gConvert.grayMixerBlue, 160, 64)
					+ bell(i, gConvert.grayMixerPurple, 192, 64)
					+ bell(i, gConvert.grayMixerMagenta, 224, 64);
			sum += lumByHue[i];
		}
		short[] c = new short[256];
		for (int i = 0; i < 256; i++)
			c[i] = (short) (lumByHue[i] * _1F8 / sum);
		hslImage.applyGrayConvert(new ShortLookupTable(0, c));
	}

	private static float bell(int x, float a, int x0, int w) {
		float radius2 = w * w;
		float sigma22 = radius2 / 4.5f;
		int i = Math.abs(x - x0);
		if (i >= 128)
			i = 255 - i;
		float distance = i * i;
		return a
				* ((distance > radius2) ? 0f : (float) Math.exp(-(distance)
						/ sigma22));
	}

	/**
	 * Add a brightness curve. Several curves can be added and will be combined
	 * before application
	 *
	 * @param curve
	 */
	public void addCurve(Curve curve) {
		if (curve != null && !curve.isEmpty() && !curve.isIdentity())
			curves.add(curve);
	}

	/**
	 * Add an unsharp mask. Several filters can be added and will be applied one
	 * after the other
	 *
	 * @param mask
	 *            - unsharp mask
	 */
	public void addUnsharpFilter(UnsharpMask mask) {
		unsharpMasks.add(mask);
	}

	/**
	 * Sets the cropping parameters
	 *
	 * @param cropping
	 */
	public void setCropping(Cropping cropping) {
		this.cropping = cropping;
	}

	/**
	 * Set the vignetting parameters
	 *
	 * @param vignette
	 *            - vignette parameters
	 */
	public void setVignette(Vignette vignette) {
		if (vignette != null && vignette.isVignetting())
			this.vignette = vignette;
		else
			this.vignette = null;
	}

	/**
	 * Sets the channel mixer matrix Can only be used for recipes that use the
	 * XYZ color space
	 *
	 * @param m11
	 * @param m12
	 * @param m13
	 * @param m21
	 * @param m22
	 * @param m23
	 * @param m31
	 * @param m32
	 * @param m33
	 */
	public void setChannelMixer(float m11, float m12, float m13, float m21,
			float m22, float m23, float m31, float m32, float m33) {
		channelMatrix = new float[][] { new float[] { m11, m12, m13 },
				new float[] { m21, m22, m23 }, new float[] { m31, m32, m33 } };
	}

	/**
	 * Adds the hue and saturation value to the pixels hue and saturation value
	 * If the pixel luminance is larger than the splitToningBalance, the
	 * highlight values are used, otherwise the shadow values are used. Can only
	 * be used for recipes that use the HSL color space.
	 *
	 * @param splitTone
	 *            -splittone parameters
	 */
	public void setSplitToning(SplitTone splitTone) {
		if (splitTone != null && splitTone.isToning)
			this.splitTone = splitTone;
		else
			this.splitTone = null;
	}

	/**
	 * Sets the image sampling factor. Typically 1 when the raw image is loaded
	 * in full resolution, and 2 if the image is loaded in half resolution
	 *
	 * @param sampleFactor
	 */
	public void setSampleFactor(int sampleFactor) {
		this.sampleFactor = sampleFactor;
	}

	/**
	 * Returns the image sampling factor. Typically 1 when the raw image is
	 * loaded in full resolution, and 2 if the image is loaded in half
	 * resolution
	 *
	 * @return the sampleFactor
	 */
	public int getSampleFactor() {
		return sampleFactor;
	}

	/**
	 * Determines if a conversion in the HSL color space is necessary
	 *
	 * @return
	 */
	public boolean needsHslOrLab() {
		return needsHsl() || needsLab();
	}

	private boolean hslOrLabCommon() {
		return hasLuminanceCurve() || !unsharpMasks.isEmpty();
	}

	/**
	 * Determines if a conversion in the HSL color space is necessary
	 *
	 * @return
	 */
	public boolean needsHsl() {
		return grayConvert != null || colorShift != null || vignette != null
				&& vignette.type == Vignette.HSL || hasHsvChannelCurves()
				|| useHSL && hslOrLabCommon();
	}

	/**
	 * Determines if a conversion in the Lab color space is necessary
	 *
	 * @return
	 */
	public boolean needsLab() {
		return channelMatrix != null || colorBoost != null || hasLabCurves()
				|| !useHSL && hslOrLabCommon();
	}

	/**
	 * Tests if there are different curves for each RGB channel
	 *
	 * @return true if there are different curves for each RGB channel
	 */
	public boolean hasRgbChannelCurves() {
		for (Curve c : curves) {
			int channel = c.getChannel();
			if ((channel == Curve.CHANNEL_RED || channel == Curve.CHANNEL_GREEN || channel == Curve.CHANNEL_BLUE)
					&& !c.isEmpty() && !c.isIdentity())
				return true;
		}
		return false;
	}

	/**
	 * Tests if there are different curves for each HSV channel
	 *
	 * @return true if there are different curves for each HSV channel
	 */
	public boolean hasHsvChannelCurves() {
		for (Curve c : curves) {
			int channel = c.getChannel();
			if ((channel == Curve.CHANNEL_HUE
					|| channel == Curve.CHANNEL_SATURATION || channel == Curve.CHANNEL_VALUE)
					&& !c.isEmpty() && !c.isIdentity())
				return true;
		}
		return false;
	}

	/**
	 * Creates an HSL or HSV image from a buffered image depending on the
	 * initialization of the recipe
	 *
	 * @param bufferedImage
	 * @return
	 */
	public HSLimage createHslImage(BufferedImage bufferedImage) {
		return createHslImage(bufferedImage, useHSV);
	}

	/**
	 * Creates an HSL or HSV image from a buffered image depending on the value
	 * of parameter hsv
	 *
	 * @param bufferedImage
	 * @param hsv
	 * @return
	 */
	public HSLimage createHslImage(BufferedImage bufferedImage, boolean hsv) {
		HSLimage hslImage = new HSLimage(bufferedImage, constructLookupTable(
				curves, 255, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
						| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE), hsv);
		removeCurves(curves, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
				| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE);
		return hslImage;
	}

	/**
	 * Creates an XYZ image from a buffered image
	 *
	 * @param bufferedImage
	 * @return
	 */
	public LabImage createLabImage(BufferedImage bufferedImage) {
		LabImage labImage = new LabImage(bufferedImage, constructLookupTable(
				curves, 255, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
						| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE),
				channelMatrix, LabImage.WP_D50);
		removeCurves(curves, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
				| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE);
		channelMatrix = null;
		return labImage;
	}

	/**
	 * Creates an HSL or HSV image from SWT image data depending on the
	 * initialization of the recipe
	 *
	 * @param swtImageData
	 * @return
	 */
	public HSLimage createHslImage(ImageData swtImageData) {
		return createHslImage(swtImageData, useHSV);
	}

	/**
	 * Creates an HSL or HSV image from SWT image data depending on parameter
	 * hsv
	 *
	 * @param swtImageData
	 * @param hsv
	 * @return
	 */
	public HSLimage createHslImage(ImageData swtImageData, boolean hsv) {
		HSLimage hslImage = new HSLimage(swtImageData, constructLookupTable(
				curves, 255, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
						| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE), hsv);
		removeCurves(curves, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
				| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE);
		return hslImage;
	}

	/**
	 * Creates an XYZ image from SWT image data
	 *
	 * @param swtImageData
	 * @return
	 */
	public LabImage createLabImage(ImageData swtImageData) {
		LabImage labImage = new LabImage(swtImageData, constructLookupTable(
				curves, 255, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
						| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE),
				channelMatrix, LabImage.WP_D50);
		removeCurves(curves, Curve.CHANNEL_ALL | Curve.CHANNEL_RED
				| Curve.CHANNEL_GREEN | Curve.CHANNEL_BLUE);
		channelMatrix = null;
		return labImage;
	}

	private static void removeCurves(List<Curve> curvList, int filter) {
		Iterator<Curve> it = curvList.iterator();
		while (it.hasNext()) {
			Curve c = it.next();
			if ((c.getChannel() & filter) != 0)
				it.remove();
		}
	}

	/**
	 * Returns the cropping parameters
	 *
	 * @return the cropping
	 */
	public Cropping getCropping() {
		return cropping;
	}

	/**
	 * Returns the split tone parameters
	 *
	 * @return
	 */
	public SplitTone getSplitTone() {
		return splitTone;
	}

	public float[] getWbFactors() {
		return wbFactors;
	}

	public void setWbFactors(float[] factors) {
		if (factors != null && (factors.length == 3 || factors.length == 4)) {
			wbFactors = factors;
			whiteBalanceMethod = wbFACTORS;
		}
	}

	// /**
	// * Sets a rating value for the image
	// *
	// * @param rating
	// * (1..5)
	// */
	// public void setRating(int rating) {
	// this.rating = rating;
	// }
	//
	// /**
	// * Sets the images color code
	// *
	// * @param colorCode
	// * : -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 = undefined,
	// * Black,White,Red,Green, Blue,Cyan,Magenta,Yellow, Orange, Pink,
	// * Violet
	// */
	// public void setColorCode(int colorCode) {
	// this.colorCode = colorCode;
	// }
	//
	// /**
	// * Returns the rating value
	// *
	// * @return the rating (1..5) or -1 if undefined
	// */
	// public int getRating() {
	// return rating;
	// }

	// /**
	// * Returns the images color code: : -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 =
	// * undefined, Black,White,Red,Green, Blue,Cyan,Magenta,Yellow, Orange,
	// Pink,
	// * Violet
	// *
	// * @return the colorCode
	// */
	// public int getColorCode() {
	// return colorCode;
	// }

	/**
	 * Adds an entry for a derived (output) image
	 *
	 * @param id
	 *            - variant id or number
	 *
	 * @param url
	 *            - URL of output file
	 * @param date
	 *            - creation date of output file
	 */
	public void addDerivative(String id, String url, Date date) {
		if (derivatives == null)
			derivatives = new ArrayList<Recipe.Derivative>();
		derivatives.add(new Derivative(id, date, url));
	}

	/**
	 * Retrieves derived images
	 *
	 * @return - list of derived images or null
	 */
	public List<Derivative> getDerivatives() {
		return derivatives;
	}

	/**
	 * Retrieves the location of the recipe
	 *
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location of the recipe
	 *
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Returns the tool that produced this recipe
	 *
	 * @return tool name
	 */
	public String getTool() {
		return tool;
	}

	/**
	 * Sets the tool that produced this recipe
	 *
	 * @param tool
	 *            - tool name
	 */
	public void setTool(String tool) {
		this.tool = tool;
	}

	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the rotation
	 */
	public Rotation getRotation() {
		return rotation;
	}

	/**
	 * Sets color boost and shift. Can only be used for recipes that use the XYZ
	 * color space
	 *
	 * @param colorBoost
	 */
	public void setColorBoost(ColorBoost colorBoost) {
		this.colorBoost = colorBoost;
	}

	/**
	 * @return the colorBoost
	 */
	public ColorBoost getColorBoost() {
		return colorBoost;
	}

	/**
	 * @return the vignette
	 */
	public Vignette getVignette() {
		return vignette;
	}

	/**
	 * @return the perspectiveCorrection
	 */
	public PerspectiveCorrection getPerspectiveCorrection() {
		return perspectiveCorrection;
	}

	/**
	 * @param perspectiveCorrection
	 *            the perspectiveCorrection to set
	 */
	public void setPerspectiveCorrection(
			PerspectiveCorrection perspectiveCorrection) {
		this.perspectiveCorrection = perspectiveCorrection;
	}

	/**
	 * Set to true if the HSV color space is to be used instead of HSL
	 *
	 * @param useHSV
	 *            true for the HSV color space
	 */
	public void setUseHSV(boolean useHSV) {
		this.useHSV = useHSV;
	}

	/**
	 * Retrieves all curves added to the recipe
	 *
	 * @return list of curves
	 */
	public List<Curve> getCurves() {
		return curves;
	}

	// /**
	// * Set a list of keywords
	// *
	// * @param keywords
	// * - list of keywords
	// */
	// public void setKeywords(List<String> keywords) {
	// this.keywords = keywords;
	// }
	//
	// /**
	// * Returns list of keywords
	// *
	// * @return list of keywords or null
	// */
	// public List<String> getKeywords() {
	// return keywords;
	// }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (exposure != 1f || vignette != null && vignette.isVignetting()
				|| highlightRecovery != 0f) {
			sb.append(Messages.Recipe_exposure_adjustments);
			int i = 0;
			if (exposure != 1f) {
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(1);
				sb.append(NLS.bind(Messages.Recipe_stops,
						nf.format(Math.log(exposure) / Math.log(2d))));
				++i;
			}
			if (highlightRecovery != 0f) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_highlight_rec);
			}
			if (vignette != null && vignette.isVignetting()) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_vignette);
			}
			sb.append('\n');
		}
		if (whiteBalanceMethod != wbNONE) {
			sb.append(Messages.Recipe_white_balance);
			switch (whiteBalanceMethod) {
			case wbASSHOT:
				sb.append(Messages.Recipe_as_shot);
				break;
			case wbAUTO:
				sb.append(Messages.Recipe_auto);
				break;
			default:
				sb.append(Messages.Recipe_manual);
				break;
			}
			sb.append('\n');
		}
		boolean tonalRange = false;
		boolean colorCurves = false;
		for (Curve curve : curves) {
			if (curve.getChannel() == Curve.CHANNEL_ALL
					|| curve.getChannel() == Curve.CHANNEL_LUMINANCE) {
				tonalRange = true;
			} else
				colorCurves = true;
		}
		boolean sharpening = false;
		boolean localContrast = false;
		for (UnsharpMask mask : unsharpMasks) {
			if (mask.type == UnsharpMask.SHARPEN)
				sharpening = true;
			else
				localContrast = true;
		}
		if (tonalRange || localContrast) {
			sb.append(Messages.Recipe_tonal_range);
			int i = 0;
			if (tonalRange) {
				sb.append(Messages.Recipe_gray_curve);
				++i;
			}
			if (localContrast) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_local_contrast);
			}
			sb.append('\n');
		}
		if (colorCurves || channelMatrix != null || colorBoost != null
				&& (colorBoost.isBoosting() || colorBoost.isShifting())
				|| colorShift != null && colorShift.isShifting
				|| grayConvert != null && grayConvert.isConverting()
				|| splitTone != null && splitTone.isToning) {
			sb.append(Messages.Recipe_color_adjustments);
			int i = 0;
			if (channelMatrix != null) {
				sb.append(Messages.Recipe_color_balance);
				++i;
			}
			if (colorBoost != null && colorBoost.isBoosting()
					|| colorShift != null && colorShift.isColorBoost) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_color_boost);
			}
			if (colorBoost != null && colorBoost.isShifting()
					|| colorShift != null && colorShift.isColorShift) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_color_shift);
			}
			if (colorCurves) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_color_curves);
			}
			if (colorShift != null && colorShift.vibrance != 1f) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_vibrance);
			}
			if (grayConvert != null && grayConvert.isConverting()) {
				if (i > 1)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_gray_scale);
				++i;
			}
			if (splitTone != null && splitTone.isToning) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_split_toning);
			}
			sb.append('\n');
		}
		if (noiseReduction != 0 || chromaticAberrationB != 1f
				|| chromaticAberrationR != 1f || sharpening) {
			sb.append(Messages.Recipe_detail_enhancements);
			int i = 0;
			if (noiseReduction != 0) {
				sb.append(Messages.Recipe_noise_reduction);
				++i;
			}
			if (chromaticAberrationB != 1f || chromaticAberrationR != 1f) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_chromatic_abberation);
			}
			if (sharpening) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_sharpening);
			}
			sb.append('\n');
		}
		if (cropping != null || perspectiveCorrection != null
				&& perspectiveCorrection.isCorrecting() || rotation != null
				|| transform != null && transform.isCorrecting()) {
			sb.append(Messages.Recipe_geometry);
			int i = 0;
			if (perspectiveCorrection != null
					&& perspectiveCorrection.isCorrecting()
					|| transform != null
					&& (transform.horizontal != 0d || transform.vertical != 0d)) {
				sb.append(Messages.Recipe_perspective);
				++i;
			}
			if (cropping != null) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_cropped);
			}
			if (rotation != null && rotation.angle != 0f || transform != null
					&& transform.rotation != 0d) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_rotated);
			}
			if (rotation != null && rotation.flipH) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_filp_h);
			}
			if (rotation != null && rotation.flipV) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_flip_v);
			}
			if (transform != null && transform.distortion != 0d) {
				if (i++ > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(Messages.Recipe_lens_distortion);
			}
			sb.append('\n');
		}
		if (sb.length() == 0)
			sb.append(Messages.Recipe_no_adjust);
		return sb.toString();
	}

	/**
	 * @return the useHSV
	 */
	public boolean isUseHSV() {
		return useHSV;
	}

	/**
	 * Sets a geometric transformation
	 *
	 * @param transform
	 *            - transformation
	 */
	public void setTransform(Transformation transform) {
		this.transform = transform;
	}

	/**
	 * @return the transform
	 */
	public Transformation getTransform() {
		return transform;
	}

}
