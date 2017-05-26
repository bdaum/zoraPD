/*******************************************************************************
 * Copyright (c) 2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.rt3.internal;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.IniReader;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.recipe.ColorShift;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.Rotation;
import com.bdaum.zoom.image.recipe.Transformation;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class RT3Detector extends AbstractRecipeDetector {

	private static final RGB FILL_COLOR = new RGB(64, 64, 64);
	private static final String DISTORTION = "Distortion"; //$NON-NLS-1$
	private static final String COMMON_PROPERTIES_FOR_TRANSFORMATIONS = "Common Properties for Transformations"; //$NON-NLS-1$
	protected static final String METADATA_PRIORITY = "com.bdaum.recipe.rt.metadatapriority"; //$NON-NLS-1$
	protected static final String METADATA_INPUTFILE = "com.bdaum.zoom.recipes.rt.inputFile"; //$NON-NLS-1$
	protected static final String METADATA_CACHE = "com.bdaum.zoom.recipes.rt.cache"; //$NON-NLS-1$

	private static final String GENERAL = "General"; //$NON-NLS-1$
	private static final String COARSE_TRANSFORMATION = "Coarse Transformation"; //$NON-NLS-1$
	private static final String ROTATION = "Rotation"; //$NON-NLS-1$
	private static final String CROP = "Crop"; //$NON-NLS-1$
	private static final String LUMINANCE_CURVE = "Luminance Curve"; //$NON-NLS-1$
	private static final String VIGNETTING_CORRECTION = "Vignetting Correction"; //$NON-NLS-1$
	private static final String HSV_EQUALIZER = "HSV Equalizer"; //$NON-NLS-1$
	private static final String CHANNEL_MIXER = "Channel Mixer"; //$NON-NLS-1$
	private static final String SHADOWS_HIGHLIGHTS = "Shadows & Highlights"; //$NON-NLS-1$
	private static final String LUMINANCE_DENOISING = "Luminance Denoising"; //$NON-NLS-1$
	private static final String SHARPENING = "Sharpening"; //$NON-NLS-1$
	private static final String HL_RECOVERY = "HLRecovery"; //$NON-NLS-1$
	private static final String CA_CORRECTION = "CACorrection"; //$NON-NLS-1$
	private static final String WHITE_BALANCE = "White Balance"; //$NON-NLS-1$
	private static final String EXPOSURE = "Exposure"; //$NON-NLS-1$
	private static final String PERSPECTIVE = "Perspective"; //$NON-NLS-1$
	private static final float[] REDDFLT = new float[] { 100f, 0f, 0f };
	private static final float[] GREENDFLT = new float[] { 0f, 100f, 0f };
	private static final float[] BLUEDFLT = new float[] { 0f, 0f, 100f };
	private static final String[] RTFOLDERS = new String[] { "" }; //$NON-NLS-1$
	private static final String RAW = "RAW"; //$NON-NLS-1$

	private Rt3Activator activator = Rt3Activator.getDefault();

	public int isRecipeXMPembbedded(String uri) {
		return -1;
	}

	public int isRecipeEmbbedded(String uri) {
		return -1;
	}

	public long getRecipeModificationTimestamp(String uri) {
		if (activator == null)
			return -1;
		File metafile = activator.getMetafile(uri,
				getParameterValue(METADATA_PRIORITY));
		return (metafile != null) ? metafile.lastModified() : -1;
	}

	public Recipe loadRecipeForImage(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		if (activator == null)
			return null;
		File metafile = activator.getMetafile(uri,
				getParameterValue(METADATA_PRIORITY));
		return (metafile == null) ? null : loadRecipeFile(metafile, overlayMap);
	}

	public File[] getMetafiles(String uri) {
		File metafile = activator.getMetafile(uri,
				getParameterValue(METADATA_PRIORITY));
		return metafile == null ? null : new File[] { metafile };
	}

	public void archiveRecipes(File targetFolder, String uri, String newUri,
			boolean readOnly) throws IOException, DiskFullException {
		File[] metafiles = getMetafiles(uri);
		if (metafiles != null)
			for (File file : metafiles) {
				File targetFile = new File(targetFolder, Core.getFileName(
						newUri, false) + Rt3Activator.PP3);
				BatchUtilities.copyFile(file, targetFile, null);
				if (readOnly)
					targetFile.setReadOnly();
			}
	}

	private Recipe loadRecipeFile(File metafile, Map<String, String> overlayMap) {
		Recipe recipe = null;
		IniReader iniReader = null;
		try {
			iniReader = new IniReader(metafile, false);
		} catch (IOException e) {
			activator
					.logError(NLS.bind(Messages.RT3Detector_cannot_read_rt_pp3,
							metafile), e);
		}
		if (iniReader != null) {
			recipe = new Recipe(getName(), metafile.getAbsolutePath(), false);
			processPP3(recipe, iniReader);
			File profileFolder = metafile.getParentFile();
			File cacheFolder = profileFolder.getParentFile();
			File dataFolder = new File(cacheFolder, "data"); //$NON-NLS-1$
			String dataname = metafile.getName();
			if (dataname.endsWith(Rt3Activator.PP3) && overlayMap != null) {
				File dataFile = new File(dataFolder, dataname.substring(0,
						dataname.length() - Rt3Activator.PP3.length()) + ".txt"); //$NON-NLS-1$
				if (dataFile.exists()) {
					iniReader = null;
					try {
						iniReader = new IniReader(dataFile, false);
					} catch (IOException e) {
						activator.logError(NLS.bind(
								Messages.RT3Detector_cannot_read_rt_data_file,
								dataFile), e);
					}
					if (iniReader != null) {
						int rank = iniReader
								.getPropertyInt(GENERAL, "Rank", -1); //$NON-NLS-1$
						if (rank > 0) {
							overlayMap.put(QueryField.RATING.getExifToolKey(),
									String.valueOf(rank));
							overlayMap.put(QueryField.RATEDBY.getExifToolKey(),
									getName());
						}
					}
				}
			}
		}
		return recipe;
	}

	private static void processPP3(Recipe recipe, IniReader iniReader) {
		// RAW
		float preExposure = (float) Math.pow(
				iniReader.getPropertyFloat(RAW, "PreExposure", 1f), 1.4d); //$NON-NLS-1$
		// Exposure
		float exposure = iniReader.getPropertyFloat(EXPOSURE,
				"Compensation", 0f); //$NON-NLS-1$
		if (exposure < 0)
			recipe.highlightRecovery = 2;
		// Heuristics for exposure
		// RT analyzes preserves white point by constructing a LUT
		if (exposure > 0f) {
			float d = exposure / 8f;
			exposure /= Math.pow(2.2222d, d);
			recipe.gamma = 2.222f * (1f + d);
		}
		recipe.exposure = preExposure * (float) Math.pow(2d, exposure);
		// Hightlightrecovery
		boolean hlr = iniReader.getPropertyBool(HL_RECOVERY, "Enabled", //$NON-NLS-1$
				false);
		if (hlr) {
			String method = iniReader.getPropertyString(HL_RECOVERY,
					"Method", "Color"); //$NON-NLS-1$ //$NON-NLS-2$
			if ("Luminance".equals(method)) //$NON-NLS-1$
				recipe.highlightRecovery = 3;
			else if ("Color".equals(method)) //$NON-NLS-1$
				recipe.highlightRecovery = 7;
			else
				recipe.highlightRecovery = 5;
		}
		// Vignetting
		float vamount = iniReader.getPropertyFloat(VIGNETTING_CORRECTION,
				"Amount", 0f); //$NON-NLS-1$
		if (vamount != 0f) {
			float vradius = iniReader.getPropertyFloat(VIGNETTING_CORRECTION,
					"Radius", 50f); //$NON-NLS-1$
			int strength = iniReader.getPropertyInt(VIGNETTING_CORRECTION,
					"Strength", 1); //$NON-NLS-1$
			float centerX = iniReader.getPropertyFloat(VIGNETTING_CORRECTION,
					"CenterX", 0f); //$NON-NLS-1$
			float centerY = iniReader.getPropertyFloat(VIGNETTING_CORRECTION,
					"CenterY", 0f); //$NON-NLS-1$
			recipe.setVignette(new Vignette(-vamount / 100f, vradius / 100f,
					strength, centerX / 100f, centerY / 100f, Vignette.RGB));
		}
		// White balance
		String whiteBalanceMethod = iniReader.getPropertyString(WHITE_BALANCE,
				"Setting", "Camera"); //$NON-NLS-1$ //$NON-NLS-2$
		if ("Camera".equals(whiteBalanceMethod)) //$NON-NLS-1$
			recipe.whiteBalanceMethod = Recipe.wbASSHOT;
		else {
			recipe.whiteBalanceMethod = Recipe.wbNONE;
			int temp = iniReader.getPropertyInt(WHITE_BALANCE,
					"Temperature", 6500); //$NON-NLS-1$
			float tint = iniReader.getPropertyFloat(WHITE_BALANCE, "Green", 1f); //$NON-NLS-1$
			if (temp != 6500f || tint != 0f)
				recipe.setColorTemperature(temp, tint);
		}
		// Channel mixer
		float[] red = iniReader.getFloatArray(CHANNEL_MIXER, "Red", //$NON-NLS-1$
				REDDFLT);
		float[] green = iniReader.getFloatArray(CHANNEL_MIXER, "Green", //$NON-NLS-1$
				GREENDFLT);
		float[] blue = iniReader.getFloatArray(CHANNEL_MIXER, "Blue", //$NON-NLS-1$
				BLUEDFLT);
		if (red.length != 3 || green.length != 3 || blue.length != 3
				|| !Arrays.equals(red, REDDFLT)
				|| !Arrays.equals(green, GREENDFLT)
				|| !Arrays.equals(blue, BLUEDFLT)) {
			recipe.setChannelMixer(red[0] / 100f, red[1] / 100f, red[2] / 100f,
					green[0] / 100f, green[1] / 100f, green[2] / 100f,
					blue[0] / 100f, blue[1] / 100f, blue[2] / 100f);
		}
		float saturation = iniReader.getPropertyFloat(EXPOSURE,
				"Saturation", 0f); //$NON-NLS-1$
		if (saturation != 0f)
			recipe.setHSL(new ColorShift(0f, Float.NaN, saturation / 100f + 1f,
					ColorShift.SAT_MULT_DEGRESSIVE, 1f, 1f));
		// Chromatic abberation
		recipe.chromaticAberrationR = 1f + iniReader.getPropertyFloat(
				CA_CORRECTION, "Red", 0f); //$NON-NLS-1$
		recipe.chromaticAberrationB = 1f + iniReader.getPropertyFloat(
				CA_CORRECTION, "Blue", 0f); //$NON-NLS-1$
		// Brightness and contrast
		float brightness = iniReader.getPropertyFloat(EXPOSURE,
				"Brightness", 0f); //$NON-NLS-1$
		float contrast = iniReader.getPropertyFloat(EXPOSURE, "Contrast", 0f); //$NON-NLS-1$

		float shadows = iniReader.getPropertyFloat(EXPOSURE, "Black", //$NON-NLS-1$
				0f);
		float shadowCompr = iniReader.getPropertyFloat(EXPOSURE,
				"ShadowCompr", 0f); //$NON-NLS-1$
		float highlightCompr = iniReader.getPropertyFloat(EXPOSURE,
				"HighlightCompr", 0f); //$NON-NLS-1$

		if (brightness != 0d || contrast != 0d || shadows != 0f
				|| shadowCompr != 0f || highlightCompr != 0f)
			createContrastCurve(recipe,
					"BrightnessContrast", //$NON-NLS-1$
					Curve.CHANNEL_ALL, brightness * 2, contrast, shadows,
					shadowCompr, highlightCompr, 1f);
		createPointsCurve(recipe, iniReader, EXPOSURE,
				"Curve", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$

		// HSV Equalizer
		createHSVCurve(recipe, iniReader, "HCurve", Curve.CHANNEL_HUE, 0f, true); //$NON-NLS-1$
		createHSVCurve(recipe, iniReader,
				"SCurve", Curve.CHANNEL_SATURATION, 0f, true); //$NON-NLS-1$
		createHSVCurve(recipe, iniReader,
				"VCurve", Curve.CHANNEL_VALUE, 0f, true); //$NON-NLS-1$
		// Lab
		// Brightness and contrast
		saturation = iniReader.getPropertyFloat(LUMINANCE_CURVE,
				"Saturation", 0f); //$NON-NLS-1$
		if (saturation != 0f)
			createContrastCurve(recipe,
					"Chroma", //$NON-NLS-1$
					Curve.CHANNEL_CHROMA, saturation, -saturation / 2, 0f, 0f,
					0f, 0f);
		brightness = iniReader.getPropertyFloat(LUMINANCE_CURVE,
				"Brightness", 0f); //$NON-NLS-1$
		contrast = iniReader.getPropertyFloat(LUMINANCE_CURVE, "Contrast", 0f); //$NON-NLS-1$
		shadows = iniReader.getPropertyFloat(LUMINANCE_CURVE, "Black", //$NON-NLS-1$
				0f);
		shadowCompr = iniReader.getPropertyFloat(LUMINANCE_CURVE,
				"ShadowCompr", 0f); //$NON-NLS-1$
		highlightCompr = iniReader.getPropertyFloat(LUMINANCE_CURVE,
				"HighlightCompr", 0f); //$NON-NLS-1$

		if (brightness != 0d || contrast != 0d || shadows != 0f
				|| shadowCompr != 0f || highlightCompr != 0f) {
			float b = (float) Math.pow(2d, brightness);
			float c = contrast / 100f + 1f;
			float sc = shadowCompr / 3251f;
			float hc = highlightCompr / 3251f;
			Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE,
					"Luminance", Curve.CHANNEL_LUMINANCE, 1f); //$NON-NLS-1$
			// Equation: y = (brightness*x-0.5) * contrast + 0.5
			float xHigh = (c + 1f) / (2f * c * b);
			float xShadow = (c - 1f) / (2f * c * b);
			xShadow += shadows / 32768f;
			if (xShadow <= 0f) {
				float yShadow = (1f - c) / 2f;
				brightnessCurve.addKnot(0f, yShadow + sc);
			} else {
				brightnessCurve.addKnot(0f, 0f);
				brightnessCurve.addKnot(xShadow, 0f);
			}
			if (xHigh >= 1f) {
				float yHigh = ((2f * b - 1f) * c + 1f) / 2f;
				brightnessCurve.addKnot(1f, yHigh + sc - hc);
			} else {
				brightnessCurve.addKnot(xHigh, 1f - hc);
				brightnessCurve.addKnot(1f, 1f - hc);
			}
			recipe.addCurve(brightnessCurve);
		}
		createPointsCurve(recipe, iniReader, LUMINANCE_CURVE, "LCurve", //$NON-NLS-1$
				Curve.CHANNEL_L, 0f);
		createPointsCurve(recipe, iniReader, LUMINANCE_CURVE, "aCurve", //$NON-NLS-1$
				Curve.CHANNEL_A, 0f);
		createPointsCurve(recipe, iniReader, LUMINANCE_CURVE, "bCurve", //$NON-NLS-1$
				Curve.CHANNEL_B, 0f);

		// Sharpening
		boolean sharp = iniReader.getPropertyBool(SHARPENING, "Enabled", false); //$NON-NLS-1$
		if (sharp) {
			String method = iniReader.getPropertyString(SHARPENING,
					"Method", ""); //$NON-NLS-1$//$NON-NLS-2$
			float sharpen, sharpenRadius;
			int threshold;
			if ("rld".equals(method)) { //$NON-NLS-1$
				sharpen = iniReader.getPropertyFloat(SHARPENING,
						"DeconvAmount", 0f) * 5; //$NON-NLS-1$
				sharpenRadius = iniReader.getPropertyFloat(SHARPENING,
						"DeconvRadius", 0f); //$NON-NLS-1$
				threshold = iniReader.getPropertyInt(SHARPENING,
						"DeconvDamping", 0) * 25; //$NON-NLS-1$
			} else {
				sharpen = iniReader.getPropertyFloat(SHARPENING, "Amount", 0f); //$NON-NLS-1$
				sharpenRadius = iniReader.getPropertyFloat(SHARPENING,
						"Radius", 0f); //$NON-NLS-1$
				threshold = iniReader
						.getPropertyInt(SHARPENING, "Threshold", 0); //$NON-NLS-1$
			}
			if (sharpen > 0d && sharpenRadius > 0f)
				recipe.addUnsharpFilter(new UnsharpMask(sharpen / 1000f,
						sharpenRadius, threshold / 65536f, 0f, null,
						UnsharpMask.SHARPEN));
		}
		// Local contrast
		boolean shadowsHighlights = iniReader.getPropertyBool(
				SHADOWS_HIGHLIGHTS, "Enabled", false); //$NON-NLS-1$
		if (shadowsHighlights) {
			int highlights = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"Highlights", 0); //$NON-NLS-1$
			int shadow = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"Shadows", 0); //$NON-NLS-1$
			int hWidth = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"HighlightTonalWidth", 10); //$NON-NLS-1$
			int sWidth = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"ShadowTonalWidth", 10); //$NON-NLS-1$
			Curve shadowHighlightsCurve = new Curve(Curve.TYPE_B_SPLINE,
					"ShadowHighlights", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
			float xShadow = sWidth / 300f;
			float xHighlight = 1f - hWidth / 300f;
			shadowHighlightsCurve.addKnot(0f, shadow / 255f);
			shadowHighlightsCurve.addKnot(xShadow, xShadow);
			shadowHighlightsCurve.addKnot(xHighlight, xHighlight);
			shadowHighlightsCurve.addKnot(1f, 1f - highlights / 255f);
			recipe.addCurve(shadowHighlightsCurve);
			int amount = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"LocalContrast", 0); //$NON-NLS-1$
			int radius = iniReader.getPropertyInt(SHADOWS_HIGHLIGHTS,
					"Radius", 5); //$NON-NLS-1$
			Curve toneMask = new Curve(Curve.TYPE_LINEAR,
					"toneMask", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
			toneMask.addKnot(0, shadow / 100f);
			toneMask.addKnot(1 / 3f, amount / 100f);
			toneMask.addKnot(2 / 3f, amount / 100f);
			toneMask.addKnot(1f, highlights / 100f);
			recipe.addUnsharpFilter(new UnsharpMask(1f, radius, -0.8f, 0f,
					toneMask, UnsharpMask.LOCAL_CONTRAST));
		}
		boolean noise = iniReader.getPropertyBool(LUMINANCE_DENOISING,
				"Enabled", false); //$NON-NLS-1$
		if (noise) {
			// Noise reduction
			float blurRadius = iniReader.getPropertyFloat(SHARPENING,
					"Radius", 0f); //$NON-NLS-1$
			recipe.noiseReduction = (int) Math.max(400 * blurRadius - 100, 100);
		}
		// Coarse Transform
		int rot = iniReader.getPropertyInt(COARSE_TRANSFORMATION, "e", 0); //$NON-NLS-1$
		boolean flipH = iniReader.getPropertyBool(COARSE_TRANSFORMATION,
				"HorizontalFlip", false); //$NON-NLS-1$
		boolean flipV = iniReader.getPropertyBool(COARSE_TRANSFORMATION,
				"VerticalFlip", false); //$NON-NLS-1$
		if (rot != 0 || flipH || flipV)
			recipe.setRotation(new Rotation(rot, flipH, flipV));
		// Geometry
		boolean autofill = iniReader.getPropertyBool(
				COMMON_PROPERTIES_FOR_TRANSFORMATIONS, "AutoFill", false); //$NON-NLS-1$
		float rotAngle = iniReader.getPropertyFloat(ROTATION, "Degree", 0f); //$NON-NLS-1$
		float distortion = iniReader.getPropertyFloat(DISTORTION, "Amount", 0f); //$NON-NLS-1$
		int perspectiveHorizontal = iniReader.getPropertyInt(PERSPECTIVE,
				"Horizontal", 0); //$NON-NLS-1$
		int perspectiveVertical = iniReader.getPropertyInt(PERSPECTIVE,
				"Vertical", 0); //$NON-NLS-1$
		if (rotAngle != 0f || distortion != 0f || perspectiveHorizontal != 0
				|| perspectiveVertical != 0) {
			recipe.setTransform(new Transformation(rotAngle,
					perspectiveHorizontal * 45 / 100d,
					perspectiveVertical * 45 / 100d, distortion,
					autofill ? null : FILL_COLOR));
		}
		// Crop
		boolean crop = iniReader.getPropertyBool(CROP, "Enabled", false); //$NON-NLS-1$
		Rectangle rect = null;
		if (crop) {
			int cropX = iniReader.getPropertyInt(CROP, "X", 0); //$NON-NLS-1$
			int cropY = iniReader.getPropertyInt(CROP, "Y", 0); //$NON-NLS-1$
			int cropW = iniReader.getPropertyInt(CROP, "W", 0); //$NON-NLS-1$
			int cropH = iniReader.getPropertyInt(CROP, "H", 0); //$NON-NLS-1$
			rect = new Rectangle(cropX, cropY, cropW, cropH);
			recipe.setCropping(new Cropping(rect, 0f, Cropping.NOFILL,
					FILL_COLOR));
		}
	}

	private static void createContrastCurve(Recipe recipe, String name, int filter,
			float brightness, float contrast, float shadows, float shadowCompr,
			float highlightCompr, float preserveShadows) {
		float b = (float) Math.pow(2d, brightness / 50);
		float c = contrast / 100f + 1f;
		float sc = shadowCompr / 3251f;
		float hc = highlightCompr / 3251f;
		Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE, name, filter,
				preserveShadows);
		// Equation: y = (brightness*x-0.5) * contrast + 0.5
		float xHigh = (c + 1f) / (2f * c * b);
		float xShadow = (c - 1f) / (2f * c * b);
		xShadow += shadows / 32768f;
		if (xShadow <= 0f) {
			float yShadow = (1f - c) / 2f;
			brightnessCurve.addKnot(0f, yShadow + sc);
		} else {
			brightnessCurve.addKnot(0f, 0f);
			brightnessCurve.addKnot(xShadow, 0f);
		}
		if (xHigh >= 1f) {
			float yHigh = ((2f * b - 1f) * c + 1f) / 2f;
			brightnessCurve.addKnot(1f, yHigh + sc - hc);
		} else {
			brightnessCurve.addKnot(xHigh, 1f - hc);
			brightnessCurve.addKnot(1f, 1f - hc);
		}
		recipe.addCurve(brightnessCurve);
	}

	private static void createPointsCurve(Recipe recipe, IniReader iniReader,
			String section, String name, int channel, float preserveShadows) {
		float[] points = iniReader.getFloatArray(section, name, null);
		if (points != null && points.length > 0 && points[0] != 0f) {
			Curve pointCurve = new Curve(Curve.TYPE_CATMULL_ROM,
					"PointCurve", channel, preserveShadows); //$NON-NLS-1$
			for (int i = 1; i < points.length - 1; i += 2)
				pointCurve.addKnot(points[i], points[i + 1]);
			recipe.addCurve(pointCurve);
		}
	}

	private static void createHSVCurve(Recipe recipe, IniReader iniReader,
			String name, int channel, float preserveShadow, boolean periodic) {
		float[] points = iniReader.getFloatArray(HSV_EQUALIZER, name, null);
		if (points != null && points.length > 0 && points[0] != 0f) {
			Curve pointCurve = new Curve(Curve.TYPE_MINMAX,
					"HSVCurve", channel, preserveShadow, periodic); //$NON-NLS-1$
			for (int i = 1; i < points.length - 3; i += 4)
				pointCurve.addKnot(points[i], points[i + 1], points[i + 2],
						points[i + 3]);
			recipe.addCurve(pointCurve);
		}
	}

	public List<RecipeFolder> computeWatchedMetaFilesOrFolders(
			WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap,
			Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean update, boolean remove) {
		if (update)
			return null;
		List<RecipeFolder> result = computeWatchedMetaFolders(watchedFolders,
				detectorMap, recursiveDetectorMap, remove, RTFOLDERS);
		File[] metaFolders = activator.getMetaFolders();
		for (File folder : metaFolders)
			updateWatchedMetaFolders(detectorMap, result, folder, this, remove,
					false);
		return result;
	}

	public File getChangedImageFile(File metaFile,
			WatchedFolder[] watchedFolders) {
		String metaName = metaFile.getName();
		if (metaName.endsWith(Rt3Activator.PP3)) {
			try {
				int p = metaName.lastIndexOf('.', metaName.length()
						- Rt3Activator.PP3.length() - 1);
				if (p > 0) {
					String hash = null;
					if (metaName.length() - Rt3Activator.PP3.length() - p == 32) {
						hash = metaName.substring(p + 1, metaName.length()
								- Rt3Activator.PP3.length());
						metaName = metaName.substring(0, p);
					} else
						metaName = metaName.substring(0, metaName.length()
								- Rt3Activator.PP3.length());
					for (WatchedFolder watchedFolder : watchedFolders) {
						try {
							File imageFile = findFileInFolder(new File(new URI(
									watchedFolder.getUri())), metaName, hash,
									watchedFolder.getRecursive());
							if (imageFile != null)
								return imageFile;
						} catch (URISyntaxException e) {
							// do nothing;
						}
					}
				}
			} catch (Exception e) {
				// do nothing
			}
		}
		return null;
	}

	private File findFileInFolder(File wfolder, String fileName, String hash,
			boolean recursive) {
		File file = new File(wfolder, fileName);
		if (file.exists()) {
			try {
				if (hash == null
						|| activator.computeHashForFile(file).equals(hash))
					return file;
			} catch (Exception e) {
				// do nothing
			}
		}
		if (recursive) {
			File[] members = wfolder.listFiles();
			if (members != null)
				for (File member : members) {
					if (member.isDirectory()) {
						File found = findFileInFolder(member, fileName, hash,
								recursive);
						if (found != null)
							return found;
					}
				}
		}
		return null;
	}

	public boolean usesIncrementalUpdate() {
		return false;
	}

	public Recipe loadRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		if (uri.toLowerCase().endsWith(Rt3Activator.PP3))
			try {
				return loadRecipeFile(new File(new URI(uri)), overlayMap);
			} catch (URISyntaxException e) {
				activator.logError(NLS.bind(Messages.RT3Detector_bad_uri, uri),
						e);
			}
		return null;
	}
}
