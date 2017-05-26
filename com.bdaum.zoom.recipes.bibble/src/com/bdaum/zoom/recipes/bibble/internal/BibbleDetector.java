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
package com.bdaum.zoom.recipes.bibble.internal;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.XMPUtils;
import com.adobe.xmp.options.IteratorOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.recipe.ColorShift;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.GrayConvert;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

public class BibbleDetector extends AbstractRecipeDetector {
	public class Section {
		float deg = 0f;
		float hue = 0f;
		float sat = 1f;
		float lum = 0f;
		float window = 0.2f;
		float dist = 30;
		float satFalloff = 0.5f;
		float valFalloff = 0.15f;
		float red = 255;
		float grn = 255;
		float blu = 255;
		float showMask = 0;

		public Section(String line) throws XMPException {
			StringTokenizer st = new StringTokenizer(line, ";"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				int p = token.indexOf('=');
				if (p >= 0) {
					String value = token.substring(p + 1).trim();
					String key = token.substring(0, p).trim().intern();
					if (key == "Deg") //$NON-NLS-1$
						deg = convertToFloat(value);
					else if (key == "Hue") //$NON-NLS-1$
						hue = convertToFloat(value) / 360f;
					else if (key == "Sat") //$NON-NLS-1$
						sat = convertToFloat(value) / 100f;
					else if (key == "Lum") //$NON-NLS-1$
						lum = convertToFloat(value) / 100f;
					else if (key == "Window") //$NON-NLS-1$
						window = convertToFloat(value) / 360f;
					else if (key == "Dist") //$NON-NLS-1$
						dist = convertToFloat(value);
					else if (key == "SatFalloff") //$NON-NLS-1$
						satFalloff = convertToFloat(value);
					else if (key == "ValFalloff") //$NON-NLS-1$
						valFalloff = convertToFloat(value);
					else if (key == "Red") //$NON-NLS-1$
						red = convertToFloat(value);
					else if (key == "Grn") //$NON-NLS-1$
						grn = convertToFloat(value);
					else if (key == "Blu") //$NON-NLS-1$
						blu = convertToFloat(value);
					else if (key == "ShowMask") //$NON-NLS-1$
						showMask = convertToFloat(value);
				}
			}
		}
	}

	private static final String XMP = ".xmp"; //$NON-NLS-1$
	private static final float P16 = 65535f;
	private static final int[] CURVETYPES = new int[] { Curve.CHANNEL_ALL,
			Curve.CHANNEL_RED, Curve.CHANNEL_GREEN, Curve.CHANNEL_BLUE };

	// <blay:options
	// bopt:colormode="1"
	// bopt:workingspace="2"
	// bopt:whiteauto="true"
	// bopt:whitetype="7"
	// bopt:lwhitetype="8"
	// bopt:lmr="1"
	// bopt:lmb="1"
	// bopt:whitered="65384"
	// bopt:whitegreen="65384"
	// bopt:whiteblue="65384"
	// bopt:whiteowbr="0"
	// bopt:whiteowbb="0"
	// bopt:autolevel="true"
	// bopt:shadowval="0.01"
	// bopt:highlightval="0.001"
	// bopt:exposureval="1.35"
	// bopt:highlightrecval="0"
	// bopt:highlightrecthresh="0"
	// bopt:highlightrecmono="false"
	// bopt:fillamount="0"
	// bopt:fillrange="0.25"
	// bopt:fillmask="0"
	// bopt:tonechange="0"
	// bopt:autorotate="0"
	// bopt:rotateangle="0"
	// bopt:sharpensense="6"
	// bopt:sharpenrad="0"
	// bopt:newsharpen="100"
	// bopt:sharpenon="true"
	// bopt:fringe="0"
	// bopt:bnn_enabled="false"
	// bopt:bnn_auto="false"
	// bopt:bnn_profile=""
	// bopt:bnn_currentprofile=""
	// bopt:bnn_turbo="true"
	// bopt:bnn_coarse="false"
	// bopt:bnn_lstrength="10"
	// bopt:bnn_lsmooth="10"
	// bopt:bnn_lcontrast="10"
	// bopt:bnn_cstrength="10"
	// bopt:bnn_csmooth="10"
	// bopt:bnn_ccontrast="10"
	// bopt:bnn_usmamount="0"
	// bopt:bnn_usmradius="1.2"
	// bopt:pcenabled="false"
	// bopt:pctintmeta="2"
	// bopt:red="0"
	// bopt:green="0"
	// bopt:blue="0"
	// bopt:sat="0"
	// bopt:cont="0"
	// bopt:hue="0"
	// bopt:vibe="0"
	// bopt:useHSL="false"
	// bopt:useHSV="true"
	// bopt:invertRegions="false"
	// bopt:selective_color=""
	// bopt:curveson="false"
	// bopt:curves_m_cn="4,1,2,2,2,2"
	// bopt:curves_m_cy="4,20,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"
	// bopt:curves_m_cx="4,20,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65535,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"
	// bopt:curves_m_imid="4,1,1,1,1,1"
	// bopt:curves_m_olo="4,1,0,0,0,0"
	// bopt:curves_m_ilo="4,1,0,0,0,0"
	// bopt:curves_m_ihi="4,1,65535,65535,65535,65535"
	// bopt:curves_m_ohi="4,1,65535,65535,65535,65535"
	// bopt:newTone="4"
	// bopt:kelvin="5049"
	// bopt:tint="18"
	// bopt:okelvin="5599"
	// bopt:otint="18"
	// bopt:lkelvin="5599"
	// bopt:ltint="18"
	// bopt:ckelvin="5049"
	// bopt:ctint="18"
	// bopt:cropon="false"
	// bopt:croplocked="true"
	// bopt:cropstyle="0"
	// bopt:cropleft="-1"
	// bopt:croptop="-1"
	// bopt:cropheight="2"
	// bopt:cropwidth="3"
	// bopt:cropdpi="-1"
	// bopt:cropmenuitem=""
	// bopt:croppercent="1"
	// bopt:Date="2008-11-30 16:41:22.000"
	// bopt:DigitizedDateTime="2008-11-30 16:41:22.000"
	// bopt:description="x-default|OLYMPUS DIGITAL CAMERA         "
	// bopt:tag="0"
	// bopt:rating="0"
	// bopt:label="0"
	// bopt:warpon="false"
	// bopt:warpa="-0.0033"
	// bopt:warpb="-0.00551"
	// bopt:warpc="0"
	// bopt:warpresize="true"
	// bopt:warpfocal="14"
	// bopt:warpcaon="false"
	// bopt:warpcarc="0"
	// bopt:warpcaby="0"
	// bopt:vignetteon="false"
	// bopt:vignetterad="50"
	// bopt:vignetteamt="0.5"
	// bopt:vignettecrop="false"
	// bopt:profilemake="Olympus SLR"
	// bopt:profilemodel="E-520"
	// bopt:profilelens="Zuiko Digital ED 14-42mm f/3.5-5.6"
	// bopt:lens="Zuiko Digital ED 14-42mm f/3.5-5.6"
	// bopt:bMirrorOn="false"
	// bopt:bInvertOn="false"
	// bopt:SMP.Andrea_resetbutton="false"
	// bopt:SMP.Andrea_enabled="false"
	// bopt:SMP.Andrea_film="BN Agfa Agfapan 25"
	// bopt:SMP.Andrea_paper="BN Agfa MultiContrast Premium"
	// bopt:SMP.Andrea_filmgrade="Refinal 6min (13.4)"
	// bopt:SMP.Andrea_papergrade="Filter 0 (5.4)"
	// bopt:SMP.Andrea_filmid="160185"
	// bopt:SMP.Andrea_paperid="627452"
	// bopt:SMP.Andrea_dr="8"
	// bopt:SMP.Andrea_exposure="0"
	// bopt:SMP.Andrea_darkroom="0"
	// bopt:SMP.Andrea_final="0"
	// bopt:SMP.Andrea_dmax="5"
	// bopt:SMP.Andrea_fred="0"
	// bopt:SMP.Andrea_fgreen="0"
	// bopt:SMP.Andrea_fblue="0"
	// bopt:SMP.Andrea_ptoe="0"
	// bopt:SMP.Andrea_fshoulder="0"
	// bopt:SMP.Andrea_flatitude="0"
	// bopt:SMP.Andrea_platitude="0"
	// bopt:SMP.Andrea_rgb="true"
	// bopt:SMP.Andrea_print="true"
	// bopt:SMP.Andrea_indicator="false"
	// bopt:blplug.bbwenabled="false"
	// bopt:blplug.bbwspotA="false"
	// bopt:blplug.bbwspotB="false"
	// bopt:blplug.bbwhueA="0"
	// bopt:blplug.bbwfuzzyA="25"
	// bopt:blplug.bbwcolorA="0"
	// bopt:blplug.bbwhueB="200"
	// bopt:blplug.bbwcolorB="200"
	// bopt:blplug.bbwfuzzyB="25"
	// bopt:blplug.bbwmode="0"/>

	public int isRecipeXMPembbedded(String uri) {
		return getIntegerParameterValue(IRecipeDetector.XMPEMBEDDED);
	}

	public int isRecipeEmbbedded(String uri) {
		return -1;
	}

	public long getRecipeModificationTimestamp(String uri) {
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

	public File[] getMetafiles(String uri) {
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
		return null;
	}

	public void archiveRecipes(File targetFolder, String uri, String newUri, boolean readOnly)
			throws IOException, DiskFullException {
		File[] metafiles = getMetafiles(uri);
		if (metafiles != null) {
			for (File file : metafiles) {
				File targetFile = new File(targetFolder, Core.getFileName(
						newUri, false) + XMP);
				BatchUtilities.copyFile(file, targetFile, null);
				if (readOnly)
					targetFile.setReadOnly();
			}
		}

	}

	public Recipe loadRecipeForImage(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		String xmpUri = uri;
		InputStream xmpIn = null;
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
		return (xmpIn != null) ? loadRecipeFile(xmpUri, xmpIn, overlayMap)
				: null;
	}

	private Recipe loadRecipeFile(String xmpUri, InputStream xmpIn,
			Map<String, String> overlayMap) {
		Recipe recipe = null;
		// Values
		float whiteBalanceMethod = 8f;
		float colorTemperature = 6500f;
		float tint = 1f;
		float exposure = 0f;
		float chromaticAberrationR = 0f;
		float chromaticAberrationB = 0f;
		boolean hasChromaticAberration = false;
		float highlightRecovery = 0f;
		float contrast = 0f;
		float fillLight = 0f;
		float fillRange = 0.25f;
		String selective_color = null;
		float saturation = 0f;
		float vibrance = 0f;
		float hue = 0f;
		float redShift = 0f;
		float greenShift = 0f;
		float blueShift = 0f;
		boolean useHSV = false;
		float cropLeft = 0f;
		float cropHeight = 1f;
		float cropWidth = 1f;
		float cropTop = 0f;
		float cropDpi = 1f;
		float cropAngle = 0f;
		boolean hasCrop = false;
		float vignetteAmount = 0f;
		float vignetteMidpoint = 0.5f;
		boolean hasVignette = false;
		int[] cn = null;
		int[] cy = null;
		int[] cx = null;
		float sharpensense = 0f;
		float sharpenrad = 1f;
		float newsharpen = 100f;
		boolean sharpenon = false;
		boolean bnn_enabled = false;
		float bnn_lstrength = 10f;
		int newTone = -1;

		try {
			XMPSchemaRegistry schemaRegistry = XMPMetaFactory
					.getSchemaRegistry();
			schemaRegistry.registerNamespace(QueryField.NS_BIBBLE.uri,
					QueryField.NS_BIBBLE.defaultPrefix);
			XMPMeta xmpMeta = XMPMetaFactory.parse(xmpIn);
			IteratorOptions options = new IteratorOptions();
			options.setJustLeafnodes(true).setJustLeafname(true);
			XMPIterator iterator = xmpMeta.iterator(null, null, options);
			while (iterator.hasNext()) {
				XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
				String key = info.getPath();
				if (!key.startsWith("bopt:")) //$NON-NLS-1$
					continue;
				if (recipe == null)
					recipe = new Recipe(getName(), xmpUri, true);
				key = key.substring(5).intern();
				String value = info.getValue().toString();
				try {
					if (key == "whitetype") { //$NON-NLS-1$
						whiteBalanceMethod = convertToFloat(value);
					} else if (key == "ckelvin") { //$NON-NLS-1$
						colorTemperature = convertToFloat(value);
					} else if (key == "ctint") { //$NON-NLS-1$
						tint = convertToFloat(value);
					} else if (key == "newTone") { //$NON-NLS-1$
						newTone = (int) convertToFloat(value);
					} else if (key == "exposureval") { //$NON-NLS-1$
						exposure = convertToFloat(value);
					} else if (key == "warpcarc") { //$NON-NLS-1$
						chromaticAberrationR = convertToFloat(value);
					} else if (key == "warpcaby") { //$NON-NLS-1$
						chromaticAberrationB = convertToFloat(value);
					} else if (key == "warpcaon") { //$NON-NLS-1$
						hasChromaticAberration = Boolean.parseBoolean(value);
					} else if (key == "highlightrecval") { //$NON-NLS-1$
						highlightRecovery = convertToFloat(value);
					} else if (key == "cont") { //$NON-NLS-1$
						contrast = convertToFloat(value);
					} else if (key == "fillamount") { //$NON-NLS-1$
						fillLight = convertToFloat(value);
					} else if (key == "fillrange") { //$NON-NLS-1$
						fillRange = convertToFloat(value);
					} else if (key == "selective_color") { //$NON-NLS-1$
						selective_color = value;
					} else if (key == "sat") { //$NON-NLS-1$
						saturation = convertToFloat(value);
					} else if (key == "hue") { //$NON-NLS-1$
						hue = convertToFloat(value);
					} else if (key == "vibe") { //$NON-NLS-1$
						vibrance = convertToFloat(value);
					} else if (key == "red") { //$NON-NLS-1$
						redShift = convertToFloat(value);
					} else if (key == "green") { //$NON-NLS-1$
						greenShift = convertToFloat(value);
					} else if (key == "blue") { //$NON-NLS-1$
						blueShift = convertToFloat(value);
					} else if (key == "useHSV") { //$NON-NLS-1$
						useHSV = Boolean.parseBoolean(value);
					} else if (key == "cropleft") { //$NON-NLS-1$
						cropLeft = convertToFloat(value);
					} else if (key == "cropheight") { //$NON-NLS-1$
						cropHeight = convertToFloat(value);
					} else if (key == "cropwidth") { //$NON-NLS-1$
						cropWidth = convertToFloat(value);
					} else if (key == "croptop") { //$NON-NLS-1$
						cropTop = convertToFloat(value);
					} else if (key == "cropdpi") { //$NON-NLS-1$
						cropDpi = convertToFloat(value);
					} else if (key == "rotateangle") { //$NON-NLS-1$
						cropAngle = convertToFloat(value);
					} else if (key == "cropon") { //$NON-NLS-1$
						hasCrop = Boolean.parseBoolean(value);
					} else if (key == "vignetteamt") { //$NON-NLS-1$
						vignetteAmount = convertToFloat(value);
					} else if (key == "vignetterad") { //$NON-NLS-1$
						vignetteMidpoint = convertToFloat(value);
					} else if (key == "vignetteon") { //$NON-NLS-1$
						hasVignette = Boolean.parseBoolean(value);
					} else if (key == "curves_m_cn") { //$NON-NLS-1$
						cn = convertIntArray(value);
					} else if (key == "curves_m_cy") { //$NON-NLS-1$
						cy = convertIntArray(value);
					} else if (key == "curves_m_cx") { //$NON-NLS-1$
						cx = convertIntArray(value);
					} else if (key == "sharpensense") { //$NON-NLS-1$
						sharpensense = convertToFloat(value);
					} else if (key == "sharpenrad") { //$NON-NLS-1$
						sharpenrad = convertToFloat(value);
					} else if (key == "newsharpen") { //$NON-NLS-1$
						newsharpen = convertToFloat(value);
					} else if (key == "sharpenon") { //$NON-NLS-1$
						sharpenon = Boolean.parseBoolean(value);
					} else if (key == "bnn_enabled") { //$NON-NLS-1$
						bnn_enabled = Boolean.parseBoolean(value);
					} else if (key == "bnn_lstrength") { //$NON-NLS-1$
						bnn_lstrength = convertToFloat(value);
					} else if (key == "rating") { //$NON-NLS-1$
						if (overlayMap != null) {
							overlayMap.put(QueryField.RATING.getExifToolKey(),
									value);
							overlayMap.put(QueryField.RATEDBY.getExifToolKey(),
									getName());
						}
					} else if (key == "keywordlist") { //$NON-NLS-1$
						if (overlayMap != null)
							overlayMap.put(
									QueryField.IPTC_KEYWORDS.getExifToolKey(),
									value);
					} else if (key == "label") { //$NON-NLS-1$
						if (overlayMap != null) {
							int label = (int) convertToFloat(value);
							int code = -1;
							switch (label) {
							case 1:
								code = Constants.COLOR_RED;
								break;
							case 2:
								code = Constants.COLOR_YELLOW;
								break;
							case 34:
								code = Constants.COLOR_GREEN;
								break;
							case 4:
								code = Constants.COLOR_BLUE;
								break;
							case 5:
								code = Constants.COLOR_VIOLET;
								break;
							}
							if (code >= 0)
								overlayMap.put(
										QueryField.COLORCODE.getExifToolKey(),
										String.valueOf(code));
						}
					}
				} catch (XMPException e) {
					BibbleActivator.getDefault().logError(
							NLS.bind(Messages.BibbleDetector_cannot_decode,
									key, value), e);
				}
			}
			if (recipe != null) {
				// Look profile
				switch (newTone) {
				case 0: // Portrait
					contrast -= 30f;
					newsharpen += 30f;
					sharpenon = true;
					break;
				case 1: // Product
					newsharpen += 50f;
					break;
				case 2: // Wedding
					break;
				case 3: // Event
					newsharpen += 50f;
					sharpenon = true;
					break;
				case 4: // Product reduced
					saturation -= 10f;
					break;
				case 5: // Product grayscale
					sharpenon = true;
					newsharpen += 50f;
					GrayConvert grayConvert = new GrayConvert(1f, 01f, 1f, 1f,
							1f, 1f, 1f, 1f);
					recipe.setGrayConvert(grayConvert);
					break;
				case 6: // Portrait reduced
					contrast -= 30f;
					saturation -= 10f;
					newsharpen += 30f;
					sharpenon = true;
					break;
				case 7: // Portrait grayscale
					contrast -= 30f;
					newsharpen += 30f;
					sharpenon = true;
					grayConvert = new GrayConvert(1f, 01f, 1f, 1f, 1f, 1f, 1f,
							1f);
					recipe.setGrayConvert(grayConvert);
					break;

				default:
					break;
				}
				// White balance
				if (whiteBalanceMethod == 8)
					recipe.whiteBalanceMethod = Recipe.wbASSHOT;
				else
					recipe.whiteBalanceMethod = Recipe.wbNONE;
				if (recipe.whiteBalanceMethod == Recipe.wbNONE
						&& (colorTemperature != 6500f || tint != 0f))
					recipe.setColorTemperature(colorTemperature,
							tint / 255f + 1f);
				// Exposure
				recipe.exposure = (float) Math.pow(2d, exposure);
				if (exposure < 0)
					recipe.highlightRecovery = 2;
				// Chromatic abberation
				if (hasChromaticAberration) {
					recipe.chromaticAberrationR = 1f + chromaticAberrationR / 250f;
					recipe.chromaticAberrationB = 1f + chromaticAberrationB / 250f;
				}
				// Hightlightrecovery
				int h = (int) (highlightRecovery / 100 * 7);
				recipe.highlightRecovery = h == 0 ? 0 : 2 + h;
				// Brightness and contrast
				if (contrast != 0d) {
					float c = contrast / 255f + 1f;
					Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE,
							"BrightnessContrast", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
					// Equation: y = (brightness*x-0.5) * contrast + 0.5
					float xHigh = (c + 1f) / (2f * c);
					float xShadow = (c - 1f) / (2f * c);
					if (xShadow <= 0f) {
						float yShadow = (1f - c) / 2f;
						brightnessCurve.addKnot(0f, yShadow);
					} else {
						brightnessCurve.addKnot(0f, 0f);
						brightnessCurve.addKnot(xShadow, 0f);
					}
					float xM = xShadow + fillRange * (xHigh - xShadow);
					float yM = fillRange;
					brightnessCurve.addKnot(xM, yM);
					if (xHigh >= 1f) {
						float yHigh = ((2f - 1f) * c + 1f) / 2f;
						brightnessCurve.addKnot(1f, yHigh);
					} else {
						brightnessCurve.addKnot(xHigh, 1f);
						brightnessCurve.addKnot(1f, 1f);
					}
					recipe.addCurve(brightnessCurve);
				}
				if (fillLight != 0f) {
					float f = fillLight / 4f; // 6.3f;
					Curve lightnessCurve = new Curve(Curve.TYPE_B_SPLINE,
							"Lightness", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
					lightnessCurve.addKnot(0f, Math.min(fillRange, f));
					lightnessCurve.addKnot(fillRange, fillRange);
					lightnessCurve.addKnot(1f, 1f);
					recipe.addCurve(lightnessCurve);
				}
				// Point curve
				if (cn != null && cx != null && cy != null && cn.length > 1
						&& cx.length > 1 && cy.length > 1 && cn[0] == cx[0]
						&& cn[0] == cy[0] && cn[1] == 1 && cx[1] == cy[1]) {
					int n = cn[0];
					int pn = cx[1];
					int ln = n * pn + 2;
					if ((n == 1 || n == 4) && cx.length == ln
							&& cy.length == ln && cn.length == n + 2) {
						for (int c = 0; c < n; c++) {
							int m = cn[c + 2];
							int off = c * pn + 2;
							Curve curve = new Curve(Curve.TYPE_CATMULL_ROM,
									"pointCurve", CURVETYPES[c], 0f); //$NON-NLS-1$
							for (int k = 0; k < m; k++) {
								int x = cx[off + k];
								int y = cy[off + k];
								curve.addKnot(x / P16, y / P16);
							}
							recipe.addCurve(curve);
						}
					}
				}
				// Color
				float globS = saturation / 100f + 1f;
				ColorShift shift = new ColorShift(hue, Float.NaN, globS,
						ColorShift.SAT_MULT, 1f, vibrance / 100f + 1f);
				if (redShift != 0) {
					Curve redCurve = new Curve(Curve.TYPE_CATMULL_ROM,
							"redShift", Curve.CHANNEL_RED, 0f); //$NON-NLS-1$
					redCurve.addKnot(0f, 0f);
					redCurve.addKnot(0.5f, 0.5f + redShift / 200f);
					redCurve.addKnot(1f, 1f);
					recipe.addCurve(redCurve);
					if (redShift < 0) {
						Curve greenCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"greenShift", //$NON-NLS-1$
								Curve.CHANNEL_GREEN, 0f);
						greenCurve.addKnot(0f, 0f);
						greenCurve.addKnot(0.5f, 0.5f - redShift / 200f);
						greenCurve.addKnot(1f, 1f);
						recipe.addCurve(greenCurve);
						Curve blueCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"blueShift", Curve.CHANNEL_BLUE, 0f); //$NON-NLS-1$
						blueCurve.addKnot(0f, 0f);
						blueCurve.addKnot(0.5f, 0.5f - redShift / 200f);
						blueCurve.addKnot(1f, 1f);
						recipe.addCurve(blueCurve);
					}
				}
				if (greenShift != 0) {
					Curve greenCurve = new Curve(Curve.TYPE_CATMULL_ROM,
							"greenShift", Curve.CHANNEL_GREEN, 0f); //$NON-NLS-1$
					greenCurve.addKnot(0f, 0f);
					greenCurve.addKnot(0.5f, 0.5f + greenShift / 200f);
					greenCurve.addKnot(1f, 1f);
					recipe.addCurve(greenCurve);
					if (greenShift < 0) {
						Curve redCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"redShift", Curve.CHANNEL_RED, 0f); //$NON-NLS-1$
						redCurve.addKnot(0f, 0f);
						redCurve.addKnot(0.5f, 0.5f - greenShift / 200f);
						redCurve.addKnot(1f, 1f);
						recipe.addCurve(redCurve);
						Curve blueCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"blueShift", Curve.CHANNEL_BLUE, 0f); //$NON-NLS-1$
						blueCurve.addKnot(0f, 0f);
						blueCurve.addKnot(0.5f, 0.5f - greenShift / 200f);
						blueCurve.addKnot(1f, 1f);
						recipe.addCurve(blueCurve);
					}
				}
				if (blueShift != 0) {
					Curve blueCurve = new Curve(Curve.TYPE_CATMULL_ROM,
							"blueShift", Curve.CHANNEL_BLUE, 0f); //$NON-NLS-1$
					blueCurve.addKnot(0f, 0f);
					blueCurve.addKnot(0.5f, 0.5f + blueShift / 200f);
					blueCurve.addKnot(1f, 1f);
					recipe.addCurve(blueCurve);
					if (greenShift < 0) {
						Curve redCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"redShift", Curve.CHANNEL_RED, 0f); //$NON-NLS-1$
						redCurve.addKnot(0f, 0f);
						redCurve.addKnot(0.5f, 0.5f - blueShift / 200f);
						redCurve.addKnot(1f, 1f);
						recipe.addCurve(redCurve);
						Curve greenCurve = new Curve(Curve.TYPE_CATMULL_ROM,
								"greenShift", Curve.CHANNEL_GREEN, 0f); //$NON-NLS-1$
						greenCurve.addKnot(0f, 0f);
						greenCurve.addKnot(0.5f, 0.5f - blueShift / 200f);
						greenCurve.addKnot(1f, 1f);
						recipe.addCurve(greenCurve);
					}
				}
				recipe.setHSL(shift);
				recipe.setUseHSV(useHSV);
				if (selective_color != null) {
					List<Section> sections = null;
					int p = 0;
					while (true) {
						int q = selective_color.indexOf("&#xA;", p); //$NON-NLS-1$
						if (q < 0)
							break;
						String line = selective_color.substring(0, q).trim();
						p = q + 5;
						if (line.startsWith("Version")) { //$NON-NLS-1$
							int k = line.indexOf('=');
							if (k >= 0) {
								int v = Integer.parseInt(line.substring(k + 1)
										.trim());
								if (v == 1) {
									sections = new ArrayList<BibbleDetector.Section>(
											12);
									continue;
								}
							}
							break;
						}
						sections.add(new Section(line));
					}
					if (sections != null) {
						for (Section section : sections) {
							shift.addSector(section.deg, section.window / 2f,
									section.valFalloff, section.hue,
									section.sat, section.lum,
									ColorShift.SAT_MULT);
						}
					}
				}
				// Sharpness and Noise
				if (sharpenon)
					recipe.addUnsharpFilter(new UnsharpMask(newsharpen / 150f,
							sharpenrad < 1f ? 1f : sharpenrad,
							sharpensense / 255f, 0f, null, UnsharpMask.SHARPEN));
				if (bnn_enabled && bnn_lstrength > 0f)
					recipe.noiseReduction = (int) (100 * Math.pow(10d,
							bnn_lstrength / 20d));
				// Cropping
				if (hasCrop)
					recipe.setCropping(new Cropping(cropTop, cropLeft,
							new Dimension((int) (cropWidth * cropDpi),
									(int) (cropHeight * cropDpi)), cropAngle,
							Cropping.NOFILL, new RGB(128, 128, 128)));
				if (hasVignette && vignetteAmount != 0f)
					recipe.setVignette(new Vignette(vignetteAmount,
							vignetteMidpoint / 100f, Vignette.HSL));
			}
		} catch (XMPException e) {
			BibbleActivator.getDefault().logError(
					NLS.bind(Messages.BibbleDetector_cannot_read_xmp_file,
							xmpUri), e);
		}
		return recipe;
	}

	private static int[] convertIntArray(String value) throws NumberFormatException {
		List<String> l = Core.fromStringList(value, ","); //$NON-NLS-1$
		int[] result = new int[l.size()];
		int i = 0;
		for (String t : l)
			result[i++] = Integer.parseInt(t);
		return result;
	}

	public List<RecipeFolder> computeWatchedMetaFilesOrFolders(
			WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap,
			Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean update, boolean remove) {
		return null;
	}

	public File getChangedImageFile(File metaFile,
			WatchedFolder[] watchedFolders) {
		return null;
	}

	public boolean usesIncrementalUpdate() {
		return true;
	}

	private static float convertToFloat(String value) throws XMPException {
		if (value.startsWith("+")) //$NON-NLS-1$
			value = value.substring(1);
		return (float) XMPUtils.convertToDouble(value);
	}

	public Recipe loadRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		try {
			if (uri.toLowerCase().endsWith(XMP))
				return loadRecipeFile(uri, new FileInputStream(new File(
						new URI(uri))), overlayMap);
		} catch (URISyntaxException e) {
			BibbleActivator.getDefault().logError(
					NLS.bind(Messages.BibbleDetector_bad_uri, uri), e);
		} catch (IOException e) {
			BibbleActivator.getDefault().logError(
					NLS.bind(Messages.BibbleDetector_io_exception, uri), e);
		}
		return null;
	}

}
