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
package com.bdaum.zoom.recipes.rt.internal;

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
import com.bdaum.zoom.common.internal.IniReader;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.recipe.ColorBoost;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.Rotation;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

public class RTDetector extends AbstractRecipeDetector {

	private static final RGB FILL_COLOR = new RGB(128, 128, 128);
	protected static final String METADATA_PRIORITY = "com.bdaum.recipe.rt.metadatapriority"; //$NON-NLS-1$
	protected static final String METADATA_INPUTFILE = "com.bdaum.zoom.recipes.rt.inputFile"; //$NON-NLS-1$
	protected static final String METADATA_CACHE = "com.bdaum.zoom.recipes.rt.cache"; //$NON-NLS-1$

	private static final String GENERAL = "General"; //$NON-NLS-1$
	private static final String COARSE_TRANSFORMATION = "Coarse Transformation"; //$NON-NLS-1$
	private static final String ROTATION = "Rotation"; //$NON-NLS-1$
	private static final String CROP = "Crop"; //$NON-NLS-1$
	private static final String LUMINANCE_CURVE = "Luminance Curve"; //$NON-NLS-1$
	private static final String VIGNETTING_CORRECTION = "Vignetting Correction"; //$NON-NLS-1$
	private static final String COLOR_SHIFT = "Color Shift"; //$NON-NLS-1$
	private static final String COLOR_BOOST = "Color Boost"; //$NON-NLS-1$
	private static final String CHANNEL_MIXER = "Channel Mixer"; //$NON-NLS-1$
	private static final String SHADOWS_HIGHLIGHTS = "Shadows & Highlights"; //$NON-NLS-1$
	private static final String LUMINANCE_DENOISING = "Luminance Denoising"; //$NON-NLS-1$
	private static final String SHARPENING = "Sharpening"; //$NON-NLS-1$
	private static final String HL_RECOVERY = "HLRecovery"; //$NON-NLS-1$
	private static final String CA_CORRECTION = "CACorrection"; //$NON-NLS-1$
	private static final String WHITE_BALANCE = "White Balance"; //$NON-NLS-1$
	private static final String EXPOSURE = "Exposure"; //$NON-NLS-1$
	private static final float[] REDDFLT = new float[] { 100f, 0f, 0f };
	private static final float[] GREENDFLT = new float[] { 0f, 100f, 0f };
	private static final float[] BLUEDFLT = new float[] { 0f, 0f, 100f };
	private static final String[] RTFOLDERS = new String[] { "" }; //$NON-NLS-1$

	private RtActivator activator = RtActivator.getDefault();

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

	private Recipe loadRecipeFile(File metafile, Map<String, String> overlayMap) {
		Recipe recipe = null;
		IniReader iniReader = null;
		try {
			iniReader = new IniReader(metafile, false);
		} catch (IOException e) {
			activator.logError(
					NLS.bind(Messages.RTDetector_cannot_read_rt_pp2, metafile),
					e);
		}
		if (iniReader != null) {
			recipe = new Recipe(getName(), metafile.getAbsolutePath(), false);
			processPP2(recipe, iniReader);
			File profileFolder = metafile.getParentFile();
			File cacheFolder = profileFolder.getParentFile();
			File dataFolder = new File(cacheFolder, "data"); //$NON-NLS-1$
			String dataname = metafile.getName();
			if (dataname.endsWith(RtActivator.PP2) && overlayMap != null) {
				File dataFile = new File(dataFolder, dataname.substring(0,
						dataname.length() - RtActivator.PP2.length()) + ".txt"); //$NON-NLS-1$
				if (dataFile.exists()) {
					iniReader = null;
					try {
						iniReader = new IniReader(dataFile, false);
					} catch (IOException e) {
						activator.logError(NLS.bind(
								Messages.RTDetector_cannot_read_rt_data_file,
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

	private static void processPP2(Recipe recipe, IniReader iniReader) {
		// Exposure
		float exposure = iniReader.getPropertyFloat(EXPOSURE,
				"Compensation", 0f); //$NON-NLS-1$
		recipe.exposure = (float) Math.pow(2d, exposure);
		if (exposure < 0)
			recipe.highlightRecovery = 2;
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
			recipe.setVignette(new Vignette(vamount / 100f, vradius / 100f,
					Vignette.RGB));
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

		// Color boost
		float aBoost = 1f + iniReader.getPropertyFloat(COLOR_BOOST,
				"ChannelA", 0f) / 100f; //$NON-NLS-1$
		float bBoost = 1f + iniReader.getPropertyFloat(COLOR_BOOST,
				"ChannelB", 0f) / 100f; //$NON-NLS-1$
		// Color shift
		float aShift = iniReader.getPropertyFloat(COLOR_SHIFT, "ChannelA", 0f) / 100f; //$NON-NLS-1$
		float bShift = iniReader.getPropertyFloat(COLOR_SHIFT, "ChannelB", 0f) / 100f; //$NON-NLS-1$
		if (aBoost != 1f || bBoost != 1f || aShift != 0f || bShift != 0f)
			recipe.setColorBoost(new ColorBoost(aBoost, bBoost, aShift, bShift));

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
				|| shadowCompr != 0f || highlightCompr != 0f) {
			float b = (float) Math.pow(2d, brightness);
			float c = contrast / 100f + 1f;
			float sc = shadowCompr / 3251f;
			float hc = highlightCompr / 3251f;
			Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE,
					"BrightnessContrast", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
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
		createPointsCurve(recipe, iniReader, EXPOSURE, Curve.CHANNEL_ALL, 1f);

		// Luminance
		// Brightness and contrast
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
					"BrightnessContrast", Curve.CHANNEL_LUMINANCE, 1f); //$NON-NLS-1$
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
		createPointsCurve(recipe, iniReader, LUMINANCE_CURVE,
				Curve.CHANNEL_LUMINANCE, 0f);

		// Sharpening
		boolean sharp = iniReader.getPropertyBool(SHARPENING, "Enabled", false); //$NON-NLS-1$
		if (sharp) {
			float sharpen = iniReader
					.getPropertyFloat(SHARPENING, "Amount", 0f); //$NON-NLS-1$
			float sharpenRadius = iniReader.getPropertyFloat(SHARPENING,
					"Radius", 0f); //$NON-NLS-1$
			if (sharpen > 0d && sharpenRadius > 0f) {
				int threshold = iniReader.getPropertyInt(SHARPENING,
						"Threshold", 0); //$NON-NLS-1$
				recipe.addUnsharpFilter(new UnsharpMask(sharpen / 1000f,
						sharpenRadius, threshold / 65536f, 0f, null,
						UnsharpMask.SHARPEN));
			}
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
		// Transform
		int rot = iniReader.getPropertyInt(COARSE_TRANSFORMATION, "e", 0); //$NON-NLS-1$
		boolean flipH = iniReader.getPropertyBool(COARSE_TRANSFORMATION,
				"HorizontalFlip", false); //$NON-NLS-1$
		boolean flipV = iniReader.getPropertyBool(COARSE_TRANSFORMATION,
				"VerticalFlip", false); //$NON-NLS-1$
		if (rot != 0 || flipH || flipV)
			recipe.setRotation(new Rotation(rot, flipH, flipV));

		// Crop
		float cropAngle = iniReader.getPropertyFloat(ROTATION, "Degree", 0f); //$NON-NLS-1$
		int fill = iniReader.getPropertyInt(ROTATION, "Fill", 0); //$NON-NLS-1$
		boolean crop = iniReader.getPropertyBool(CROP, "Enabled", false); //$NON-NLS-1$
		Rectangle rect = null;
		if (crop) {
			int cropX = iniReader.getPropertyInt(CROP, "X", 0); //$NON-NLS-1$
			int cropY = iniReader.getPropertyInt(CROP, "Y", 0); //$NON-NLS-1$
			int cropW = iniReader.getPropertyInt(CROP, "W", 0); //$NON-NLS-1$
			int cropH = iniReader.getPropertyInt(CROP, "H", 0); //$NON-NLS-1$
			rect = new Rectangle(cropX, cropY, cropW, cropH);
		}
		if (rect != null || cropAngle != 0f)
			recipe.setCropping(new Cropping(rect, -cropAngle, fill, FILL_COLOR));
	}

	private static void createPointsCurve(Recipe recipe, IniReader iniReader,
			String section, int channel, float preserveShadows) {
		float[] points = iniReader.getFloatArray(section, "Curve", //$NON-NLS-1$
				null);
		if (points != null && points.length > 0 && points[0] != 0f) {
			Curve pointCurve = new Curve(Curve.TYPE_CATMULL_ROM,
					"PointCurve", channel, preserveShadows); //$NON-NLS-1$
			for (int i = 1; i < points.length - 1; i += 2)
				pointCurve.addKnot(points[i], points[i + 1]);
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
		if (metaName.endsWith(RtActivator.PP2)) {
			try {
				int p = metaName.lastIndexOf('.', metaName.length()
						- RtActivator.PP2.length() - 1);
				if (p > 0) {
					String hash = null;
					if (metaName.length() - RtActivator.PP2.length() - p == 32) {
						hash = metaName.substring(p + 1, metaName.length()
								- RtActivator.PP2.length());
						metaName = metaName.substring(0, p);
					} else
						metaName = metaName.substring(0, metaName.length()
								- RtActivator.PP2.length());
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

	public File[] getMetafiles(String uri) {
		if (activator == null)
			return null;
		File metafile = activator.getMetafile(uri,
				getParameterValue(METADATA_PRIORITY));
		return (metafile == null) ? null : new File[] { metafile };
	}

	public void archiveRecipes(File targetFolder, String uri, String newUri,
			boolean readOnly) throws IOException, DiskFullException {
		File[] metafiles = getMetafiles(uri);
		if (metafiles != null)
			for (File file : metafiles) {
				File targetFile = new File(targetFolder, Core.getFileName(
						newUri, false) + RtActivator.PP2);
				BatchUtilities.copyFile(file, targetFile, null);
				if (readOnly)
					targetFile.setReadOnly();
			}
	}

	public Recipe loadRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		if (uri.toLowerCase().endsWith(RtActivator.PP2))
			try {
				return loadRecipeFile(new File(new URI(uri)), overlayMap);
			} catch (URISyntaxException e) {
				activator.logError(NLS.bind(Messages.RTDetector_bad_uri, uri),
						e);
			}
		return null;
	}

}
