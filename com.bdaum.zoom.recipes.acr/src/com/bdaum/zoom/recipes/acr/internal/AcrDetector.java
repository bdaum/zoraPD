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
package com.bdaum.zoom.recipes.acr.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;

import Jama.Matrix;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.XMPUtils;
import com.adobe.xmp.options.IteratorOptions;
import com.adobe.xmp.properties.XMPProperty;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.recipe.ColorShift;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.GrayConvert;
import com.bdaum.zoom.image.recipe.PerspectiveCorrection;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.SplitTone;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

public class AcrDetector extends AbstractRecipeDetector {
	private static final String XMP = ".xmp"; //$NON-NLS-1$
	private static final String DNG = ".dng"; //$NON-NLS-1$

	// not handled
	// <crs:ColorNoiseReduction>25</crs:ColorNoiseReduction>
	// <crs:ShadowTint>0</crs:ShadowTint>
	// <crs:RedHue>0</crs:RedHue>
	// <crs:RedSaturation>0</crs:RedSaturation>
	// <crs:GreenHue>0</crs:GreenHue>
	// <crs:GreenSaturation>0</crs:GreenSaturation>
	// <crs:BlueHue>0</crs:BlueHue>
	// <crs:BlueSaturation>0</crs:BlueSaturation>
	// <crs:Defringe>0</crs:Defringe>
	// <crs:PaintBasedCorrections>
	// <crs:LensManualDistortionAmount>0</crs:LensManualDistortionAmount>

	// crs:Whites2012="0"
	// crs:Blacks2012="0"

	public int isRecipeXMPembbedded(String uri) {
		if (isRecipeEmbbedded(uri) >= 0)
			return -1;
		return getIntegerParameterValue(IRecipeDetector.XMPEMBEDDED);
	}

	public int isRecipeEmbbedded(String uri) {
		if (uri.toLowerCase().endsWith(DNG)) {
			try {
				File file = new File(new URI(uri));
				return file.canWrite() ? 0 : -1;
			} catch (URISyntaxException e) {
				// do nothing
			}
		}
		return -1;
	}

	public long getRecipeModificationTimestamp(String uri) {
		if (uri.toLowerCase().endsWith(DNG)) {
			try {
				File file = new File(new URI(uri));
				if (file.canWrite())
					return file.lastModified();
			} catch (URISyntaxException e) {
				// do nothing
			}
		}
		try {
			URI[] sidecarURIs = Core.getSidecarURIs(new URI(uri));
			long modified = 0L;
			for (URI xmpUri : sidecarURIs) {
				File xmpFile = new File(xmpUri);
				modified = Math.max(modified, xmpFile.lastModified());
			}
			return modified;
		} catch (URISyntaxException e1) {
			// should never happen
		}
		return -1L;
	}

	public Recipe loadRecipeForImage(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		String xmpUri = uri;

		InputStream xmpIn = null;
		if (isRecipeEmbbedded(uri) >= 0) {
			try {
				xmpIn = ImageUtilities.extractXMP(uri);
			} catch (IOException e) {
				// ignore
			} catch (URISyntaxException e) {
				// should never happen
			}
		} else {
			File xmpFile;
			try {
				URI[] sidecarURIs = Core.getSidecarURIs(new URI(uri));
				for (URI u : sidecarURIs) {
					xmpFile = new File(u);
					if (xmpFile.exists()) {
						xmpUri = u.toString();
						xmpIn = new FileInputStream(xmpFile);
						break;
					}
				}
			} catch (URISyntaxException e1) {
				// should never happen
			} catch (FileNotFoundException e) {
				// no XMP file
			}
		}
		return (xmpIn != null) ? loadRecipeFile(xmpUri, xmpIn, overlayMap) : null;
	}

	private Recipe loadRecipeFile(String xmpUri, InputStream xmpIn, Map<String, String> overlayMap) {
		Recipe recipe = null;
		// Values
		int orientation = 1;
		float colorTemperature = 6500f;
		float tint = 1f;
		float brightness = 0f;
		float contrast = 0f;
		float saturation = 1f;
		float fillLight = 0f;
		float shadow2012 = 0f;
		float highlights = 0f;
		float whites = 0f;
		float blacks = 0f;
		boolean convertToGrayScale = false;
		float sharpenEdgeMasking = 0f;
		float sharpen = 0f;
		float sharpenRadius = 0f;
		float sharpenDetail = 0f;
		float hueAdjustmentMagenta = 0f;
		float hueAdjustmentPurple = 0f;
		float hueAdjustmentBlue = 0f;
		float hueAdjustmentAqua = 0f;
		float hueAdjustmentGreen = 0f;
		float hueAdjustmentYellow = 0f;
		float hueAdjustmentOrange = 0f;
		float hueAdjustmentRed = 0f;
		float saturationAdjustmentMagenta = 1f;
		float saturationAdjustmentPurple = 1f;
		float saturationAdjustmentBlue = 1f;
		float saturationAdjustmentAqua = 1f;
		float saturationAdjustmentGreen = 1f;
		float saturationAdjustmentYellow = 1f;
		float saturationAdjustmentOrange = 1f;
		float saturationAdjustmentRed = 1f;
		float luminanceAdjustmentMagenta = 1f;
		float luminanceAdjustmentPurple = 1f;
		float luminanceAdjustmentBlue = 1f;
		float luminanceAdjustmentAqua = 1f;
		float luminanceAdjustmentGreen = 1f;
		float luminanceAdjustmentYellow = 1f;
		float luminanceAdjustmentOrange = 1f;
		float luminanceAdjustmentRed = 1f;
		float vibrance = 1f;
		float grayMixerRed = 1f;
		float grayMixerOrange = 1f;
		float grayMixerYellow = 1f;
		float grayMixerGreen = 1f;
		float grayMixerAqua = 1f;
		float grayMixerBlue = 1f;
		float grayMixerPurple = 1f;
		float grayMixerMagenta = 1f;
		float parametricHighlights = 0f;
		float parametricLights = 0f;
		float parametricDarks = 0f;
		float parametricShadows = 0f;
		float parametricShadowSplit = 0.25f;
		float parametricMidtoneSplit = 0.5f;
		float parametricHighlightSplit = 0.75f;
		float clarity = 0f;
		float shadows = 0f;
		float cropLeft = 0f;
		float cropBottom = 1f;
		float cropRight = 1f;
		float cropTop = 0f;
		float cropAngle = 0f;
		boolean cropConstrainToWarp = false;
		float vignetteAmount = 0f;
		float vignetteMidpoint = 0.5f;
		boolean hasCrop = false;
		float splitToningShadowHue = 0f;
		float splitToningShadowSaturation = 0f;
		float splitToningHighlightHue = 0f;
		float splitToningHighlightSaturation = 0f;
		float splitToningBalance = 0.5f;
		Curve pointCurve = null;
		String whiteBalanceMethod = null;
		float exposure = 0f;
		float chromaticAberrationR = 0f;
		float chromaticAberrationB = 0f;
		float highlightRecovery = 0f;
		float luminanceSmoothing = 0f;
		float perspectiveVertical = 0f;
		float perspectiveHorizontal = 0f;
		float perspectiveRotate = 0f;
		float perspectiveScale = 1f;
		double focalLength = DIA35MM;
		try {
			XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
			String crs = QueryField.NS_ACR.defaultPrefix;
			String crsPrefix = crs + ':';
			schemaRegistry.registerNamespace(QueryField.NS_ACR.uri, crs);
			schemaRegistry.registerNamespace(QueryField.NS_EXIF.uri, QueryField.NS_EXIF.defaultPrefix);
			schemaRegistry.registerNamespace(QueryField.NS_TIFF.uri, QueryField.NS_TIFF.defaultPrefix);

			XMPMeta xmpMeta = XMPMetaFactory.parse(xmpIn);
			Integer v = xmpMeta.getPropertyInteger(QueryField.NS_TIFF.uri, "Orientation"); //$NON-NLS-1$
			if (v != null)
				orientation = v;
			IteratorOptions options = new IteratorOptions();
			options.setJustLeafnodes(true).setJustLeafname(true);
			XMPProperty prop = xmpMeta.getProperty(QueryField.NS_EXIF.uri, "FocalLengthIn35mmFilm"); //$NON-NLS-1$
			if (prop != null)
				focalLength = convertToFloat(prop.getValue().toString());
			XMPIterator iterator = xmpMeta.iterator(QueryField.NS_ACR.uri, null, options);
			while (iterator.hasNext()) {
				XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
				String key = info.getPath();
				if (!key.startsWith(crsPrefix))
					continue;
				if (recipe == null)
					recipe = new Recipe(getName(), xmpUri, true);
				key = key.substring(crsPrefix.length()).intern();
				String value = info.getValue().toString();
				try {
					if (key == "WhiteBalance") { //$NON-NLS-1$
						whiteBalanceMethod = value;
					} else if (key == "Temperature") { //$NON-NLS-1$
						colorTemperature = convertToFloat(value);
					} else if (key == "Tint") { //$NON-NLS-1$
						tint = convertToFloat(value);
					} else if (key == "Exposure") { //$NON-NLS-1$
						exposure = convertToFloat(value);
					} else if (key == "Exposure2012") { //$NON-NLS-1$
						exposure = convertToFloat(value);
					} else if (key == "ChromaticAberrationR") { //$NON-NLS-1$
						chromaticAberrationR = convertToFloat(value);
					} else if (key == "ChromaticAberrationB") { //$NON-NLS-1$
						chromaticAberrationB = convertToFloat(value);
					} else if (key == "HighlightRecovery") { //$NON-NLS-1$
						highlightRecovery = convertToFloat(value);
					} else if (key == "Brightness") { //$NON-NLS-1$
						brightness = convertToFloat(value);
					} else if (key == "Contrast") { //$NON-NLS-1$
						contrast = convertToFloat(value);
					} else if (key == "Contrast2012") { //$NON-NLS-1$
						contrast = convertToFloat(value) / 2;
					} else if (key == "Shadows") { //$NON-NLS-1$
						shadows = (convertToFloat(value) - 3f) / 255f;
					} else if (key == "Shadows2012") { //$NON-NLS-1$
						shadow2012 = convertToFloat(value) / 255f;
					} else if (key == "Highlights2012") { //$NON-NLS-1$
						highlights = convertToFloat(value) / 255f;
					} else if (key == "Whites2012") { //$NON-NLS-1$
						whites = convertToFloat(value) / 255f;
					} else if (key == "Blacks2012") { //$NON-NLS-1$
						blacks = convertToFloat(value) / 255f;
					} else if (key == "FillLight") { //$NON-NLS-1$
						fillLight = convertToFloat(value);
					} else if (key == "Saturation") { //$NON-NLS-1$
						saturation = convertToFloat(value);
					} else if (key == "ConvertToGrayscale") { //$NON-NLS-1$
						convertToGrayScale = Boolean.parseBoolean(value);
					} else if (key == "Sharpness") { //$NON-NLS-1$
						sharpen = convertToFloat(value);
					} else if (key == "SharpenRadius") { //$NON-NLS-1$
						sharpenRadius = convertToFloat(value);
					} else if (key == "SharpenEdgeMasking") { //$NON-NLS-1$
						sharpenEdgeMasking = convertToFloat(value);
					} else if (key == "SharpenDetail") { //$NON-NLS-1$
						sharpenDetail = convertToFloat(value);
					} else if (key == "HueAdjustmentRed") { //$NON-NLS-1$
						hueAdjustmentRed = convertToFloat(value);
					} else if (key == "HueAdjustmentOrange") { //$NON-NLS-1$
						hueAdjustmentOrange = convertToFloat(value);
					} else if (key == "HueAdjustmentYellow") { //$NON-NLS-1$
						hueAdjustmentYellow = convertToFloat(value);
					} else if (key == "HueAdjustmentGreen") { //$NON-NLS-1$
						hueAdjustmentGreen = convertToFloat(value);
					} else if (key == "HueAdjustmentAqua") { //$NON-NLS-1$
						hueAdjustmentAqua = convertToFloat(value);
					} else if (key == "HueAdjustmentBlue") { //$NON-NLS-1$
						hueAdjustmentBlue = convertToFloat(value);
					} else if (key == "HueAdjustmentPurple") { //$NON-NLS-1$
						hueAdjustmentPurple = convertToFloat(value);
					} else if (key == "HueAdjustmentMagenta") { //$NON-NLS-1$
						hueAdjustmentMagenta = convertToFloat(value);
					} else if (key == "SaturationAdjustmentRed") { //$NON-NLS-1$
						saturationAdjustmentRed = convertToFloat(value);
					} else if (key == "SaturationAdjustmentOrange") { //$NON-NLS-1$
						saturationAdjustmentOrange = convertToFloat(value);
					} else if (key == "SaturationAdjustmentYellow") { //$NON-NLS-1$
						saturationAdjustmentYellow = convertToFloat(value);
					} else if (key == "SaturationAdjustmentGreen") { //$NON-NLS-1$
						saturationAdjustmentGreen = convertToFloat(value);
					} else if (key == "SaturationAdjustmentAqua") { //$NON-NLS-1$
						saturationAdjustmentAqua = convertToFloat(value);
					} else if (key == "SaturationAdjustmentBlue") { //$NON-NLS-1$
						saturationAdjustmentBlue = convertToFloat(value);
					} else if (key == "SaturationAdjustmentPurple") { //$NON-NLS-1$
						saturationAdjustmentPurple = convertToFloat(value);
					} else if (key == "SaturationAdjustmentMagenta") { //$NON-NLS-1$
						saturationAdjustmentMagenta = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentRed") { //$NON-NLS-1$
						luminanceAdjustmentRed = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentOrange") { //$NON-NLS-1$
						luminanceAdjustmentOrange = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentYellow") { //$NON-NLS-1$
						luminanceAdjustmentYellow = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentGreen") { //$NON-NLS-1$
						luminanceAdjustmentGreen = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentAqua") { //$NON-NLS-1$
						luminanceAdjustmentAqua = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentBlue") { //$NON-NLS-1$
						luminanceAdjustmentBlue = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentPurple") { //$NON-NLS-1$
						luminanceAdjustmentPurple = convertToFloat(value);
					} else if (key == "LuminanceAdjustmentMagenta") { //$NON-NLS-1$
						luminanceAdjustmentMagenta = convertToFloat(value);
					} else if (key == "Vibrance") { //$NON-NLS-1$
						vibrance = convertToFloat(value);
					} else if (key == "ParametricShadows") { //$NON-NLS-1$
						parametricShadows = convertToFloat(value);
					} else if (key == "ParametricDarks") { //$NON-NLS-1$
						parametricDarks = convertToFloat(value);
					} else if (key == "ParametricLights") { //$NON-NLS-1$
						parametricLights = convertToFloat(value);
					} else if (key == "ParametricHighlights") { //$NON-NLS-1$
						parametricHighlights = convertToFloat(value);
					} else if (key == "ParametricShadowSplit") { //$NON-NLS-1$
						parametricShadowSplit = convertToFloat(value);
					} else if (key == "ParametricMidtoneSplit") { //$NON-NLS-1$
						parametricMidtoneSplit = convertToFloat(value);
					} else if (key == "ParametricHighlightSplit") { //$NON-NLS-1$
						parametricHighlightSplit = convertToFloat(value);
					} else if (key == "Clarity") { //$NON-NLS-1$
						clarity = convertToFloat(value);
					} else if (key == "Clarity2012") { //$NON-NLS-1$
						clarity = convertToFloat(value) * 5;
					} else if (key == "CropLeft") { //$NON-NLS-1$
						cropLeft = convertToFloat(value);
					} else if (key == "CropBottom") { //$NON-NLS-1$
						cropBottom = convertToFloat(value);
					} else if (key == "CropRight") { //$NON-NLS-1$
						cropRight = convertToFloat(value);
					} else if (key == "CropTop") { //$NON-NLS-1$
						cropTop = convertToFloat(value);
					} else if (key == "CropAngle") { //$NON-NLS-1$
						cropAngle = convertToFloat(value);
					} else if (key == "HasCrop") { //$NON-NLS-1$
						hasCrop = Boolean.parseBoolean(value);
					} else if (key == "CropConstrainToWarp") { //$NON-NLS-1$
						cropConstrainToWarp = Boolean.parseBoolean(value);
					} else if (key == "VignetteAmount") { //$NON-NLS-1$
						vignetteAmount = convertToFloat(value);
					} else if (key == "VignetteMidpoint") { //$NON-NLS-1$
						vignetteMidpoint = convertToFloat(value);
					} else if (key == "GrayMixerRed") { //$NON-NLS-1$
						grayMixerRed = convertToFloat(value);
					} else if (key == "GrayMixerOrange") { //$NON-NLS-1$
						grayMixerOrange = convertToFloat(value);
					} else if (key == "GrayMixerYellow") { //$NON-NLS-1$
						grayMixerYellow = convertToFloat(value);
					} else if (key == "GrayMixerGreen") { //$NON-NLS-1$
						grayMixerGreen = convertToFloat(value);
					} else if (key == "GrayMixerAqua") { //$NON-NLS-1$
						grayMixerAqua = convertToFloat(value);
					} else if (key == "GrayMixerBlue") { //$NON-NLS-1$
						grayMixerBlue = convertToFloat(value);
					} else if (key == "GrayMixerPurple") { //$NON-NLS-1$
						grayMixerPurple = convertToFloat(value);
					} else if (key == "GrayMixerMagenta") { //$NON-NLS-1$
						grayMixerMagenta = convertToFloat(value);
					} else if (key == "SplitToningShadowHue") { //$NON-NLS-1$
						splitToningShadowHue = convertToFloat(value);
					} else if (key == "SplitToningShadowSaturation") { //$NON-NLS-1$
						splitToningShadowSaturation = convertToFloat(value);
					} else if (key == "SplitToningHighlightHue") { //$NON-NLS-1$
						splitToningHighlightHue = convertToFloat(value);
					} else if (key == "SplitToningHighlightSaturation") { //$NON-NLS-1$
						splitToningHighlightSaturation = convertToFloat(value);
					} else if (key == "SplitToningBalance") { //$NON-NLS-1$
						splitToningBalance = convertToFloat(value);
					} else if (key == "LuminanceSmoothing") { //$NON-NLS-1$
						luminanceSmoothing = convertToFloat(value);
					} else if (key == "PerspectiveVertical") { //$NON-NLS-1$
						perspectiveVertical = convertToFloat(value);
					} else if (key == "PerspectiveHorizontal") { //$NON-NLS-1$
						perspectiveHorizontal = convertToFloat(value);
					} else if (key == "PerspectiveRotate") { //$NON-NLS-1$
						perspectiveRotate = convertToFloat(value);
					} else if (key == "PerspectiveScale") { //$NON-NLS-1$
						perspectiveScale = convertToFloat(value) / 100f;
					}
				} catch (XMPException e) {
					AcrActivator.getDefault().logError(NLS.bind(Messages.AcrDetector_cannot_decode, key, value), e);
				}
			}
			// Point curve
			iterator = xmpMeta.iterator(QueryField.NS_ACR.uri, "crs:ToneCurve", options); //$NON-NLS-1$
			if (iterator.hasNext()) {
				if (recipe == null)
					recipe = new Recipe(getName(), xmpUri, true);
				pointCurve = new Curve(Curve.TYPE_CATMULL_ROM, "PointCurve", Curve.CHANNEL_ALL, 0f); //$NON-NLS-1$
				while (iterator.hasNext()) {
					XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
					String s = info.getValue().toString();
					int q = s.indexOf(',');
					if (q > 0) {
						try {
							pointCurve.addKnot(convertToFloat(s.substring(0, q)) / 255f,
									convertToFloat(s.substring(q + 1).trim()) / 255f);
						} catch (XMPException e) {
							AcrActivator.getDefault()
									.logError(NLS.bind(Messages.AcrDetector_cannot_decode, "crs:ToneCurve", //$NON-NLS-1$
											s), e);
						}
					}
				}
			}
		} catch (XMPException e) {
			AcrActivator.getDefault().logError(NLS.bind(Messages.AcrDetector_cannot_read_xmp_file, xmpUri), e);
		} finally {
			try {
				xmpIn.close();
			} catch (IOException e) {
				// ignore
			}
		}

		if (recipe != null) {
			double horizontalTilt, verticalTilt;
			float leftCrop, rightCrop, topCrop, bottomCrop;
			switch (orientation) {
			case 2:
				// 2 = The 0th row represents the visual top of the image, and
				// the 0th column represents the visual right-hand side.
				horizontalTilt = -perspectiveHorizontal;
				verticalTilt = perspectiveVertical;
				leftCrop = 1f - cropRight;
				rightCrop = 1f - cropLeft;
				topCrop = cropTop;
				bottomCrop = cropBottom;
				break;
			case 3:
				// 3 = The 0th row represents the visual bottom of the image,
				// and the 0th column represents the visual right-hand side.
				horizontalTilt = -perspectiveHorizontal;
				verticalTilt = -perspectiveVertical;
				leftCrop = 1f - cropRight;
				rightCrop = 1f - cropLeft;
				topCrop = 1f - cropBottom;
				bottomCrop = 1f - cropTop;
				break;
			case 4:
				// 4 = The 0th row represents the visual bottom of the image,
				// and the 0th column represents the visual left-hand side.
				horizontalTilt = perspectiveHorizontal;
				verticalTilt = -perspectiveVertical;
				leftCrop = cropLeft;
				rightCrop = cropRight;
				topCrop = 1f - cropBottom;
				bottomCrop = 1f - cropTop;
				break;
			case 5:
				// 5 = The 0th row represents the visual left-hand side of the
				// image, and the 0th column represents the visual top.
				horizontalTilt = perspectiveVertical;
				verticalTilt = perspectiveHorizontal;
				leftCrop = cropTop;
				rightCrop = cropBottom;
				topCrop = cropLeft;
				bottomCrop = cropRight;
				break;
			case 6:
				// 6 = The 0th row represents the visual right-hand side of the
				// image, and the 0th column represents the visual top.
				horizontalTilt = -perspectiveVertical;
				verticalTilt = perspectiveHorizontal;
				leftCrop = 1f - cropBottom;
				rightCrop = 1f - cropTop;
				topCrop = cropLeft;
				bottomCrop = cropRight;
				break;
			case 7:
				// 7 = The 0th row represents the visual right-hand side of the
				// image, and the 0th column represents the visual bottom.
				horizontalTilt = -perspectiveVertical;
				verticalTilt = -perspectiveHorizontal;
				leftCrop = 1f - cropBottom;
				rightCrop = 1f - cropTop;
				topCrop = 1f - cropRight;
				bottomCrop = 1f - cropLeft;
				break;
			case 8:
				// 8 = The 0th row represents the visual left-hand side of the
				// image, and the 0th column represents the visual bottom.
				horizontalTilt = perspectiveVertical;
				verticalTilt = -perspectiveHorizontal;
				leftCrop = cropTop;
				rightCrop = cropBottom;
				topCrop = 1f - cropRight;
				bottomCrop = 1f - cropLeft;
				break;
			default:
				// 1 = The 0th row represents the visual top of the image, and
				// the 0th column represents the visual left-hand side.
				horizontalTilt = perspectiveHorizontal;
				verticalTilt = perspectiveVertical;
				leftCrop = cropLeft;
				rightCrop = cropRight;
				topCrop = cropTop;
				bottomCrop = cropBottom;
				break;
			}

			// White balance
			if ("As Shot".equals(whiteBalanceMethod)) //$NON-NLS-1$
				recipe.whiteBalanceMethod = Recipe.wbASSHOT;
			else if ("Auto".equals(whiteBalanceMethod)) //$NON-NLS-1$
				recipe.whiteBalanceMethod = Recipe.wbAUTO;
			else
				recipe.whiteBalanceMethod = Recipe.wbNONE;
			if (recipe.whiteBalanceMethod == Recipe.wbNONE && (colorTemperature != 6500f || tint != 0f))
				recipe.setColorTemperature(colorTemperature, tint / 255f + 1f);
			// Exposure
			recipe.exposure = (float) Math.pow(2d, exposure);
			if (exposure < 0)
				recipe.highlightRecovery = 2;
			// Hightlightrecovery
			int h = (int) (highlightRecovery / 100 * 7);
			recipe.highlightRecovery = h == 0 ? 0 : 2 + h;
			// Chromatic abberation
			recipe.chromaticAberrationR = 1f + chromaticAberrationR / 50000f;
			recipe.chromaticAberrationB = 1f + chromaticAberrationB / 50000f;
			// Cropping
			if (hasCrop)
				recipe.setCropping(new Cropping(topCrop, bottomCrop, leftCrop, rightCrop, cropAngle,
						cropConstrainToWarp ? Cropping.FILL : Cropping.NOFILL, new RGB(128, 128, 128)));
			if (vignetteAmount != 0f)
				recipe.setVignette(new Vignette(vignetteAmount / 100f, vignetteMidpoint / 100f, Vignette.HSL));
			// Brightness and contrast
			if (brightness != 0d || contrast != 0d || shadows != 0f || fillLight != 0f || shadow2012 != 0f
					|| highlights != 0f || blacks != 0 || whites != 0) {
				float b = brightness / 255f + 1f;
				float c = contrast / 255f + 1f;
				float f = fillLight / 255f;
				Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE, "BrightnessContrast", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
				// Equation: y = (brightness*x-0.5) * contrast + 0.5
				float xHigh = (c + 1f) / (2f * c * b);
				float yHigh;
				float xShadow = (c - 1f) / (2f * c * b);
				float yShadow;
				xShadow += shadows + blacks;
				xHigh += whites;
				if (xShadow <= 0f) {
					float diff = Math.min(-blacks, -xShadow);
					yShadow = (1f - c) / 2f + f;
					if (diff > 0) {
						brightnessCurve.addKnot(0f, 0f);
						xShadow = diff;
					} else
						xShadow = 0;
				} else {
					brightnessCurve.addKnot(0f, f);
					yShadow = f + (1 - xShadow);
				}
				brightnessCurve.addKnot(xShadow, yShadow);
				if (xHigh >= 1f) {
					float diff = Math.min(whites, xHigh - 1f);
					yHigh = ((2f * b - 1f) * c + 1f) / 2f + f + diff;
					xHigh = 1f;
				} else {
					yHigh = 1f;
				}
				if (shadow2012 != 0f) {
					float xs = xShadow + (xHigh - xShadow) / 4;
					float ys = yShadow + (yHigh - yShadow) / 4 + shadow2012;
					brightnessCurve.addKnot(xs, ys);
				}
				if (shadow2012 != 0f || highlights != 0f) {
					float xMed = xShadow + (xHigh - xShadow) / 2;
					float yMed = yShadow + (yHigh - yShadow) / 2;
					brightnessCurve.addKnot(xMed, yMed);
				}
				if (highlights != 0f) {
					float xs = xShadow + (xHigh - xShadow) / 4 * 3;
					float ys = yShadow + (yHigh - yShadow) / 4 * 3 + highlights;
					brightnessCurve.addKnot(xs, ys);
				}
				if (shadow2012 < 0 || blacks < 0 || highlights < 0 || whites < 0)
					brightnessCurve.setPreserveShadow(0f);
				brightnessCurve.addKnot(xHigh, yHigh);
				if (xHigh < 1f)
					brightnessCurve.addKnot(1f, 1f);
				recipe.addCurve(brightnessCurve);
			}
			// Parametric curve
			if (parametricHighlights != 0f || parametricLights != 0f || parametricDarks != 0f || parametricShadows != 0f
					|| parametricShadowSplit != 25f || parametricMidtoneSplit != 50f
					|| parametricHighlightSplit != 75f) {
				Curve parmCurve = new Curve(Curve.TYPE_B_SPLINE, "ParametricCurve", Curve.CHANNEL_ALL, 0f); //$NON-NLS-1$
				parmCurve.addKnot(0f, 0f);
				float px1 = parametricShadowSplit / 100f * 0.5f;
				float py1 = px1 + parametricShadows / 100f + parametricShadowSplit / 100f;
				parmCurve.addKnot(px1, py1);
				float px2 = parametricShadowSplit / 100f;
				float py2 = px2 + parametricDarks / 100f * parametricMidtoneSplit / 100f;
				parmCurve.addKnot(px2, py2);
				float px3 = parametricHighlightSplit / 100f;
				float py3 = px3 + parametricLights / 100f * (1f - parametricMidtoneSplit / 100f);
				parmCurve.addKnot(px3, py3);
				float px4 = (parametricHighlightSplit / 100f + 1f) * 0.5f;
				float py4 = px4 + parametricHighlights / 100f * (1f - parametricHighlightSplit / 100f);
				parmCurve.addKnot(px4, py4);
				parmCurve.addKnot(1f, 1f);
				recipe.addCurve(parmCurve);
			}
			// Color
			if (convertToGrayScale)
				recipe.setGrayConvert(new GrayConvert(grayMixerRed / 100f + 1f, grayMixerOrange / 100f + 1f,
						grayMixerYellow / 100f + 1f, grayMixerGreen / 100f + 1f, grayMixerAqua / 100f + 1f,
						grayMixerBlue / 100f + 1f, grayMixerPurple / 100f + 1f, grayMixerMagenta / 100f + 1f));
			else {
				ColorShift shift = new ColorShift(0f, Float.NaN, saturation / 100f + 1f, ColorShift.SAT_MULT, 1f,
						vibrance / 100f + 1f);
				shift.addSector(0.98f, 0.0666f, 0.0666f, hueAdjustmentRed / 100f, saturationAdjustmentRed / 100f + 1f,
						luminanceAdjustmentRed / 255f + 1f, ColorShift.SAT_MULT);
				shift.addSector(0.082f, 0.045f, 0.045f, hueAdjustmentOrange / 100f,
						saturationAdjustmentOrange / 100f + 1f, luminanceAdjustmentOrange / 255f + 1f,
						ColorShift.SAT_MULT);
				shift.addSector(0.22f, 0.0666f, 0.0666f, hueAdjustmentYellow / 100f,
						saturationAdjustmentYellow / 100f + 1f, luminanceAdjustmentYellow / 255f + 1f,
						ColorShift.SAT_MULT);
				shift.addSector(0.333f, 0.086f, 0.086f, hueAdjustmentGreen / 100f,
						saturationAdjustmentGreen / 100f + 1f, luminanceAdjustmentGreen / 255f + 1f,
						ColorShift.SAT_MULT);
				shift.addSector(0.5f, 0.086f, 0.086f, hueAdjustmentAqua / 100f, saturationAdjustmentAqua / 100f + 1f,
						luminanceAdjustmentAqua / 255f + 1f, ColorShift.SAT_MULT);
				shift.addSector(0.647f, 0.0666f, 0.0666f, hueAdjustmentBlue / 100f,
						saturationAdjustmentBlue / 100f + 1f, luminanceAdjustmentBlue / 255f + 1f, ColorShift.SAT_MULT);
				shift.addSector(0.749f, 0.045f, 0.045f, hueAdjustmentPurple / 100f,
						saturationAdjustmentPurple / 100f + 1f, luminanceAdjustmentPurple / 255f + 1f,
						ColorShift.SAT_MULT);
				shift.addSector(0.855f, 0.0666f, 0.0666f, hueAdjustmentMagenta / 100f,
						saturationAdjustmentMagenta / 100f + 1f, luminanceAdjustmentMagenta / 255f + 1f,
						ColorShift.SAT_MULT);
				recipe.setHSL(shift);
			}
			// Point curve
			recipe.addCurve(pointCurve);
			// Split toning
			Curve splitMask = new Curve(Curve.TYPE_B_SPLINE, "splitMask", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
			splitMask.addKnot(0, 0);
			splitMask.addKnot(0.5f - splitToningBalance / 200f, 0.5f);
			splitMask.addKnot(1f, 1f);
			recipe.setSplitToning(new SplitTone(splitToningShadowHue / 360f, splitToningShadowSaturation / 255,
					splitToningHighlightHue / 360f, splitToningHighlightSaturation / 255, splitMask));
			// Sharpening
			if (sharpen > 0d && sharpenRadius > 0f)
				recipe.addUnsharpFilter(new UnsharpMask(sharpen / 150f, sharpenRadius, sharpenEdgeMasking / 255f,
						sharpenDetail / 100f, null, UnsharpMask.SHARPEN));
			// Local contrast
			if (clarity != 0f) {
				Curve toneMask = new Curve(Curve.TYPE_LINEAR, "toneMask", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
				toneMask.addKnot(0, 0);
				toneMask.addKnot(1 / 3f, 1f);
				toneMask.addKnot(2 / 3f, 1f);
				toneMask.addKnot(1f, 0f);
				recipe.addUnsharpFilter(new UnsharpMask(clarity / 30f, Math.abs(clarity / 150f) + 30, -05f, 0f,
						toneMask, UnsharpMask.LOCAL_CONTRAST));
			}
			// Noise reduction
			recipe.noiseReduction = 100 + 9 * (int) luminanceSmoothing;
			// Perspective
			if (horizontalTilt != 0f || verticalTilt != 0f || perspectiveScale != 1f || perspectiveRotate != 0f) {
				double flen = focalLength / DIA35MM;

				Matrix trans = new Matrix(new double[][] { { 1d, 0d, 0d, 0d }, { 0d, 1d, 0d, 0d },
						{ 0d, 0d, 1d, -flen }, { 0d, 0d, 0d, 1d } });
				double theta = verticalTilt / 200d;
				Matrix xrot = new Matrix(
						new double[][] { { 1d, 0d, 0d, 0d }, { 0d, Math.cos(theta), -Math.sin(theta), 0d },
								{ 0d, Math.sin(theta), Math.cos(theta), 0d }, { 0d, 0d, 0d, 1d } });
				theta = -horizontalTilt / 200d;
				Matrix yrot = new Matrix(new double[][] { { Math.cos(theta), 0d, Math.sin(theta), 0d },
						{ 0d, 1d, 0d, 0d }, { -Math.sin(theta), 0d, Math.cos(theta), 0d }, { 0d, 0d, 0d, 1d } });
				theta = -Math.toRadians(perspectiveRotate);
				Matrix zrot = new Matrix(new double[][] { { Math.cos(theta), -Math.sin(theta), 0d, 0d },
						{ Math.sin(theta), Math.cos(theta), 0d, 0d }, { 0d, 0d, 1d, 0d }, { 0d, 0d, 0d, 1d } });
				double f = 1d / perspectiveScale;
				Matrix persp = new Matrix(new double[][] { { flen, 0d, 0d, 0d }, { 0d, flen, 0d, 0d },
						{ 0d, 0d, flen, 0d }, { 0d, 0d, 1d, 0d } });
				Matrix pm = new Matrix(new double[][] { { f, 0d, 0d, 0d }, { 0d, f, 0d, 0d }, { 0d, 0d, 1d, 0d },
						{ 0d, 0d, 0d, 1d } });
				pm = pm.times(persp);
				pm = pm.times(trans.inverse());
				pm = pm.times(zrot);
				pm = pm.times(yrot);
				pm = pm.times(xrot);
				pm = pm.times(trans);
				recipe.setPerspectiveCorrection(new PerspectiveCorrection(pm, flen, new RGB(128, 128, 128)));
			}
		}
		return recipe;
	}

	private static float convertToFloat(String value) throws XMPException {
		return (float) XMPUtils.convertToDouble(value.startsWith("+") ? value.substring(1) : value); //$NON-NLS-1$
	}

	public List<RecipeFolder> computeWatchedMetaFilesOrFolders(WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap, Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean update, boolean remove) {
		return null;
	}

	public File getChangedImageFile(File metaFile) {
		return null;
	}

	public File getChangedImageFile(File metaFile, WatchedFolder[] watchedFolders) {
		return null;
	}

	public boolean usesIncrementalUpdate() {
		return false;
	}

	public File[] getMetafiles(String uri) {
		if (isRecipeEmbbedded(uri) < 0) {
			try {
				URI[] sidecarURIs = Core.getSidecarURIs(new URI(uri));
				for (URI u : sidecarURIs) {
					File xmpFile = new File(u);
					if (xmpFile.exists())
						return new File[] { xmpFile };
				}
			} catch (URISyntaxException e1) {
				// should never happen
			}
		}
		return null;
	}

	public void archiveRecipes(File targetFolder, String uri, String newUri, boolean readOnly)
			throws IOException, DiskFullException {
		File[] metafiles = getMetafiles(uri);
		if (metafiles != null) {
			for (File file : metafiles) {
				File targetFile = new File(targetFolder, Core.getFileName(newUri, false) + XMP);
				BatchUtilities.copyFile(file, targetFile, null);
				if (readOnly)
					targetFile.setReadOnly();
			}
		}

	}

	public Recipe loadRecipe(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		uri = uri.toLowerCase();
		try {
			if (uri.endsWith(XMP))
				return loadRecipeFile(uri, new FileInputStream(new File(new URI(uri))), overlayMap);
			if (uri.endsWith(DNG)) {
				InputStream xmpIn = ImageUtilities.extractXMP(uri);
				if (xmpIn != null)
					return loadRecipeFile(uri, xmpIn, overlayMap);
			}
		} catch (URISyntaxException e) {
			AcrActivator.getDefault().logError(NLS.bind(Messages.AcrDetector_bad_uri, uri), e);
		} catch (IOException e) {
			AcrActivator.getDefault().logError(NLS.bind(Messages.AcrDetector_io_exception, uri), e);
		}
		return null;
	}

}
