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
package com.bdaum.zoom.recipes.c1.internal;

import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import Jama.Matrix;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.AbstractRecipeDetector;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.ColorShift;
import com.bdaum.zoom.image.recipe.Cropping;
import com.bdaum.zoom.image.recipe.Curve;
import com.bdaum.zoom.image.recipe.PerspectiveCorrection;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.Rotation;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.image.recipe.Vignette;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

public class C1Detector extends AbstractRecipeDetector {
	private static final String EIP = ".EIP"; //$NON-NLS-1$
	private static final SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //$NON-NLS-1$
	private static final String V = "V"; //$NON-NLS-1$
	private static final String K = "K"; //$NON-NLS-1$
	private static final String E2 = "E"; //$NON-NLS-1$
	private static final String AL = "AL"; //$NON-NLS-1$
	private static final String DL = "DL"; //$NON-NLS-1$
	private static final String IMG = "IMG"; //$NON-NLS-1$
	private static final String VAR = "VAR"; //$NON-NLS-1$
	private static final String CAPTURE_ONE_SETTINGS = ".cos"; //$NON-NLS-1$
	public static final String[] CAPTURE_ONE_FOLDERS = new String[] { "CaptureOne/Settings", "CaptureOne/Settings45", //$NON-NLS-1$ //$NON-NLS-2$
			"CaptureOne/Settings50" }; //$NON-NLS-1$
	protected static final Object MD = "MD"; //$NON-NLS-1$
	protected static final int[] COLORS = new int[] { -1, 2, 8, 7, 3, 4, 9, 10 };
	protected static final Object PHS = "PHS"; //$NON-NLS-1$
	protected static final Object PH = "PH"; //$NON-NLS-1$
	protected static final Object UUID = "UUID"; //$NON-NLS-1$

	Recipe recipe;
	// fields
	private float brightness;
	private float contrast;
	private float saturation;
	private float fillLight;
	private float sharpenEdgeMasking;
	private float sharpen;
	private float sharpenRadius;
	private float vibrance;
	private float clarity;
	private float shadows;
	private float cropAngle;
	private float highlightRecovery;
	private float luminanceSmoothing;
	protected float[][] colorCorrections;
	protected float[][] redCurve;
	protected float[][] greenCurve;
	protected float[][] blueCurve;
	protected float[] targetShadow;
	protected float[] targetHighlight;
	protected float[] midtone;
	protected float[] lensLightFalloff;
	protected float width;
	protected float height;
	protected float virtualWidth;
	protected float virtualHeight;
	protected float[] cropping;
	protected Date phDate;
	protected String phUrl;
	protected String uuid;
	protected int variant;
	protected float exposure;
	protected int compatibleVersion = 7;
	protected boolean isDeveloped;
	protected float geometryKeystoneAmount;
	protected float geometryKeystoneTiltX;
	protected float geometryKeystoneTiltY;
	protected float geometryKeystoneAspect;
	protected float geometryKeystoneFocalLength;
	protected String filmCurve;
	protected int rotation;

	private StringBuilder contact = new StringBuilder();
	private StringBuilder location = new StringBuilder();
	private StringBuilder artwork = new StringBuilder();

	// <DL>
	// <E K="ICCProfile" V="GenericDngFile-Neutral.icm" />
	// <E K="CNRAmount" V="43.000" />
	// <E K="CleanLongExposureAmount" V="0.000" />
	// <E K="BRAmount" V="0.000" />
	// <E K="Basic_Caption" V="" />
	// <E K="LensChromAbrOrigin" V="0" />
	// <E K="Vignetting" V="0.0|0|0|1" />
	// <E K="LensOpticCenterEnabled" V="0" />
	// <E K="LensOpticCenter" V="0.000000|0.000000" />
	// <E K="Moire" V="0.000;8" />
	// <E K="TargetEnable" V="1" />
	// </DL>

	public int isRecipeXMPembbedded(String uri) {
		return -1;
	}

	public int isRecipeEmbbedded(String uri) {
		return -1;
	}

	public long getRecipeModificationTimestamp(String uri) {
		try {
			File imageFile = new File(new URI(uri));
			String fileName = imageFile.getName();
			if (fileName.toUpperCase().endsWith(EIP))
				return imageFile.lastModified();
			File folder = imageFile.getParentFile();
			long lastMod = -1;
			for (String c1folder : CAPTURE_ONE_FOLDERS) {
				StringBuilder sb = new StringBuilder();
				sb.append(c1folder).append('/').append(fileName).append(CAPTURE_ONE_SETTINGS);
				File metaFile = new File(folder, sb.toString());
				long timestamp = metaFile.lastModified();
				if (timestamp > lastMod)
					lastMod = timestamp;
			}
			return lastMod;
		} catch (URISyntaxException e) {
			// do nothing
			return -1L;
		}
	}

	public Recipe loadRecipeForImage(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		try {
			List<File> recipeFiles = new ArrayList<File>(3);
			File imageFile = new File(new URI(uri));
			IFileHandler fileHandler = ImageConstants.findFileHandler(imageFile);
			if (fileHandler != null) {
				File sidecar = fileHandler.getSidecar(imageFile);
				if (sidecar != null)
					recipeFiles.add(sidecar);
			} else {
				String fileName = imageFile.getName();
				File folder = imageFile.getParentFile();
				for (String c1folder : CAPTURE_ONE_FOLDERS) {
					StringBuilder sb = new StringBuilder();
					sb.append(c1folder).append('/').append(fileName).append(CAPTURE_ONE_SETTINGS);
					File metaFile = new File(folder, sb.toString());
					if (metaFile.exists())
						recipeFiles.add(metaFile);
				}
				Collections.sort(recipeFiles, new Comparator<File>() {
					public int compare(File f1, File f2) {
						long t1 = f1.lastModified();
						long t2 = f2.lastModified();
						return t1 == t2 ? 0 : t1 > t2 ? -1 : 1;
					}
				});
			}
			loadRecipeFiles(recipeFiles.toArray(new File[recipeFiles.size()]), focalLengthProvider, overlayMap);
		} catch (IOException e) {
			C1Activator.getDefault().logError(NLS.bind(Messages.C1Detector_io_error, uri), e);
		} catch (URISyntaxException e) {
			// do nothing
		}
		return null;
	}

	public File[] getMetafiles(String uri) {
		try {
			List<File> recipeFiles = new ArrayList<File>(3);
			File imageFile = new File(new URI(uri));
			IFileHandler fileHandler = ImageConstants.findFileHandler(imageFile);
			if (fileHandler == null) {
				String fileName = imageFile.getName();
				File folder = imageFile.getParentFile();
				for (String c1folder : CAPTURE_ONE_FOLDERS) {
					StringBuilder sb = new StringBuilder();
					sb.append(c1folder).append('/').append(fileName).append(CAPTURE_ONE_SETTINGS);
					File metaFile = new File(folder, sb.toString());
					if (metaFile.exists())
						recipeFiles.add(metaFile);
				}
			}
			return recipeFiles.toArray(new File[recipeFiles.size()]);
		} catch (URISyntaxException e) {
			// do nothing
		}
		return null;
	}

	public void archiveRecipes(File targetFolder, String uri, String newUri, boolean readOnly)
			throws IOException, DiskFullException {
		try {
			File imageFile = new File(new URI(uri));
			IFileHandler fileHandler = ImageConstants.findFileHandler(imageFile);
			if (fileHandler == null) {
				String fileName = imageFile.getName();
				File folder = imageFile.getParentFile();
				for (String c1folder : CAPTURE_ONE_FOLDERS) {
					StringBuilder sb = new StringBuilder();
					sb.append(c1folder).append('/').append(fileName).append(CAPTURE_ONE_SETTINGS);
					File metaFile = new File(folder, sb.toString());
					if (metaFile.exists()) {
						String newFileName = Core.getFileName(newUri, false);
						File cosFolder = new File(targetFolder, c1folder);
						cosFolder.mkdirs();
						File targetFile = new File(cosFolder, newFileName + CAPTURE_ONE_SETTINGS);
						BatchUtilities.copyFile(metaFile, targetFile, null);
						if (readOnly)
							targetFile.setReadOnly();
					}
				}
			}
		} catch (URISyntaxException e) {
			// do nothing
		}
	}

	private Recipe loadRecipeFiles(File[] recipeFiles, IFocalLengthProvider focalLengthProvider,
			final Map<String, String> overlayMap) throws IOException {
		recipe = null;
		brightness = 0f;
		filmCurve = null;
		contrast = 0f;
		saturation = 1f;
		fillLight = 0f;
		sharpenEdgeMasking = 0f;
		sharpen = 0f;
		sharpenRadius = 0f;
		colorCorrections = null;
		vibrance = 1f;
		clarity = 0f;
		shadows = 0f;
		cropAngle = 0f;
		highlightRecovery = 0f;
		luminanceSmoothing = 0f;
		redCurve = null;
		greenCurve = null;
		blueCurve = null;
		targetShadow = null;
		targetHighlight = null;
		midtone = null;
		lensLightFalloff = null;
		cropping = null;
		phDate = null;
		phUrl = null;
		variant = 0;
		exposure = 0f;
		geometryKeystoneAmount = 1f;
		geometryKeystoneTiltX = 0f;
		geometryKeystoneTiltY = 0f;
		geometryKeystoneAspect = 0f;
		geometryKeystoneFocalLength = 1f;
		width = 0f;
		height = 0f;
		virtualWidth = 0f;
		virtualHeight = 0f;
		rotation = 0;
		SAXParserFactory factory = null;
		try {
			for (File metaFile : recipeFiles) {
				isDeveloped = false;
				try (InputStream in = new BufferedInputStream(new FileInputStream(metaFile))) {
					if (recipe == null)
						recipe = new Recipe(getName(), metaFile.toURI().toString(), true);
					if (factory == null)
						factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					DefaultHandler handler = new DefaultHandler() {
						Stack<String> stack = new Stack<String>();

						@Override
						public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
								throws SAXException {
							if (stack.isEmpty() && IMG.equals(qName)) {
								stack.push(qName);
							} else if (IMG.equals(stack.peek())) {
								if (VAR.equals(qName))
									stack.push(qName);
								else if (E2.equals(qName)) {
									String key = atts.getValue(K);
									String value = atts.getValue(V);
									if ("ImageDimensions".equals(key)) { //$NON-NLS-1$
										try {
											float[] dim = parseArray(value, ";"); //$NON-NLS-1$
											virtualWidth = width = dim[0];
											virtualHeight = height = dim[1];
										} catch (ParseException e) {
											C1Activator.getDefault().logError(Messages.C1Detector_bad_field_value, e);
										}
									} else if ("CompatibleVersion".equals(key)) { //$NON-NLS-1$
										try {
											compatibleVersion = (int) parseFloat(value);
										} catch (ParseException e) {
											compatibleVersion = 7;
										}
									}
								}

							} else if (VAR.equals(stack.peek())) {
								if (AL.equals(qName) || DL.equals(qName) || MD.equals(qName) || PHS.equals(qName))
									stack.push(qName);
								else if (E2.equals(qName)) {
									String key = atts.getValue(K).intern();
									if (UUID.equals(key)) {
										uuid = atts.getValue(V);
										variant++;
									}
								}
							} else if (DL.equals(stack.peek()) && E2.equals(qName) && variant == 1) {
								processFields(recipe, atts, true);
							} else if (AL.equals(stack.peek()) && E2.equals(qName) && variant == 1) {
								isDeveloped = true;
								processFields(recipe, atts, false);
							} else if (PHS.equals(stack.peek()) && PH.equals(qName)) {
								stack.push(qName);
							} else if (PH.equals(stack.peek()) && E2.equals(qName)) {
								String key = atts.getValue(K).intern();
								String value = atts.getValue(V);
								if ("Date".equals(key)) //$NON-NLS-1$
									phDate = parseDate(value);
								else if ("Url".equals(key)) { //$NON-NLS-1$
									try {
										URI uri = new URI(value);
										phUrl = uri.toString();
									} catch (URISyntaxException e) {
										File file = new File(value);
										phUrl = file.toURI().toString();
									}
								}
							} else if (MD.equals(stack.peek()) && E2.equals(qName) && variant == 1) {
								isDeveloped = true;
								String key = atts.getValue(K).intern();
								String value = atts.getValue(V);
								if (overlayMap != null) {
									try {
										if ("Basic_Rating" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.RATING.getExifToolKey(), value);
											overlayMap.put(QueryField.RATEDBY.getExifToolKey(), getName());
										} else if ("Contact_Creator" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_BYLINE.getExifToolKey(), value);
										} else if ("Contact_Creatorstitle" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_BYLINETITLE.getExifToolKey(), value);
										} else if ("Contact_Address" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_ADDRESS, value);
										} else if ("Contact_City" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_CITY, value);
										} else if ("Contact_State_Province" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_STATE, value);
										} else if ("Contact_Postalcode" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_POSTALCODE, value);
										} else if ("Contact_Country" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_COUNTRY, value);
										} else if ("Contact_Phones" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_PHONE, value);
										} else if ("Contact_Emails" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_EMAIL, value);
										} else if ("Contact_Websites" == key) { //$NON-NLS-1$
											addStructField(contact, QueryField.CONTACT_WEBURL, value);
										} else if ("Content_Headline" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_HEADLINE.getExifToolKey(), value);
										} else if ("Content_Description" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_DESCRIPTION.getExifToolKey(), value);
										} else if ("Content_Subjectcode" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_SUBJECTCODE.getExifToolKey(), value);
										} else if ("Content_Descriptionwriter" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_WRITEREDITOR.getExifToolKey(), value);
										} else if ("Image_Intellectualgenre" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_INTELLECTUAL_GENRE.getExifToolKey(), value);
										} else if ("Image_Scene" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_SCENECODE.getExifToolKey(), value);
										} else if ("Image_Location" == key) { //$NON-NLS-1$
											addStructField(location, QueryField.LOCATION_DETAILS, value);
										} else if ("Image_City" == key) { //$NON-NLS-1$
											addStructField(location, QueryField.LOCATION_CITY, value);
										} else if ("Image_State" == key) { //$NON-NLS-1$
											addStructField(location, QueryField.LOCATION_STATE, value);
										} else if ("Image_Country" == key) { //$NON-NLS-1$
											addStructField(location, QueryField.LOCATION_COUNTRYNAME, value);
										} else if ("Image_Isocountrycode" == key) { //$NON-NLS-1$
											addStructField(location, QueryField.LOCATION_COUNTRYCODE, value);
										} else if ("Status_Title" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_TITLE.getExifToolKey(), value);
										} else if ("Status_Jobidentifier" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_JOBID.getExifToolKey(), value);
										} else if ("Status_Instructions" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_SPECIALINSTRUCTIONS.getExifToolKey(), value);
										} else if ("Status_Provider" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_CREDITS.getExifToolKey(), value);
										} else if ("Status_Source" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_SOURCE.getExifToolKey(), value);
										} else if ("Status_Copyrightnotice" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_OWNER.getExifToolKey(), value);
										} else if ("Status_Usageterms" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_USAGE.getExifToolKey(), value);
										} else if ("Content_Category" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_CATEGORY.getExifToolKey(), value);
										} else if ("Content_SupplementalCategories" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_SUPPLEMENTALCATEGORIES.getExifToolKey(),
													value);
										} else if ("getty_OriginalFileName" == key) { //$NON-NLS-1$
											addStructField(artwork, QueryField.ARTWORKOROBJECT_TITLE, value);
										} else if ("getty_ParentMEID" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_EVENT.getExifToolKey(), value);
										} else if ("getty_Personality" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.IPTC_PERSONSHOWN.getExifToolKey(), value);
										} else if ("Color_tag_index" == key) { //$NON-NLS-1$
											overlayMap.put(QueryField.COLORCODE.getExifToolKey(),
													String.valueOf(COLORS[(int) parseFloat(value)]));
										}
									} catch (ParseException e) {
										C1Activator.getDefault().logError(Messages.C1Detector_bad_field_value, e);
									}
								}
							}
						}

						private void processFields(final Recipe recip, Attributes atts, boolean dflt) {
							String key = atts.getValue(K).intern();
							String value = atts.getValue(V);
							try {
								if (key == "Exposure") //$NON-NLS-1$
									exposure = parseFloat(value);
								else if (key == "FilmCurve") //$NON-NLS-1$
									filmCurve = value;
								else if (key == "WhiteBalance") { //$NON-NLS-1$
									float[] whiteBalance = parseArray(value, ";"); //$NON-NLS-1$
									recip.setWbFactors(whiteBalance);

								} else if (key == "ColorBalance") { //$NON-NLS-1$
									float[] colorBalance = parseArray(value, ";"); //$NON-NLS-1$
									recip.setColorBalance(colorBalance);
								} else if (key == "NRAmount") //$NON-NLS-1$
									luminanceSmoothing = parseFloat(value);
								else if (key == "Brightness") //$NON-NLS-1$
									brightness = parseFloat(value);
								else if (key == "Contrast") //$NON-NLS-1$
									contrast = parseFloat(value);
								else if (key == "ShadowRecovery") //$NON-NLS-1$
									fillLight = parseFloat(value);
								else if (key == "HighlightRecovery") //$NON-NLS-1$
									highlightRecovery = parseFloat(value);
								else if (key == "Saturation") //$NON-NLS-1$
									saturation = parseFloat(value);
								else if (key == "ColorCorrections") //$NON-NLS-1$
									colorCorrections = parseArray2(value, ";", ","); //$NON-NLS-1$ //$NON-NLS-2$
								else if (key == "GCurve") { //$NON-NLS-1$
									float[][] gcurve = parseArray2(value, ";", //$NON-NLS-1$
											","); //$NON-NLS-1$
									recip.addCurve(createCurve(gcurve, Curve.CHANNEL_ALL));
								} else if (key == "GCurveR") //$NON-NLS-1$
									redCurve = parseArray2(value, ";", ","); //$NON-NLS-1$ //$NON-NLS-2$
								else if (key == "GCurveG") //$NON-NLS-1$
									greenCurve = parseArray2(value, ";", ","); //$NON-NLS-1$ //$NON-NLS-2$
								else if (key == "GCurveB") //$NON-NLS-1$
									blueCurve = parseArray2(value, ";", ","); //$NON-NLS-1$ //$NON-NLS-2$
								else if (key == "TargetShadow") //$NON-NLS-1$
									targetShadow = parseArray(value, ";"); //$NON-NLS-1$
								else if (key == "TargetHighlight") //$NON-NLS-1$
									targetHighlight = parseArray(value, ";"); //$NON-NLS-1$
								else if (key == "Midtone") //$NON-NLS-1$
									midtone = parseArray(value, ";"); //$NON-NLS-1$
								else if (key == "USMAmount") //$NON-NLS-1$
									sharpen = parseFloat(value);
								else if (key == "USMRadius") //$NON-NLS-1$
									sharpenRadius = parseFloat(value);
								else if (key == "USMThreshold") //$NON-NLS-1$
									sharpenEdgeMasking = parseFloat(value);
								else if (key == "Clarity") //$NON-NLS-1$
									clarity = parseFloat(value);
								else if (key == "LensLightFalloff") //$NON-NLS-1$
									lensLightFalloff = parseArray(value, "|"); //$NON-NLS-1$
								else if (key == "Crop") { //$NON-NLS-1$
									float[] a = parseArray(value, ";");//$NON-NLS-1$
									if (a.length == 4 && (a[0] != 0 || a[1] != 0 || a[2] != 0 || a[3] != 0))
										cropping = a;
								} else if (key == "Rotation") { //$NON-NLS-1$
									if (dflt) {
										rotation = (int) parseFloat(value);
										if (rotation % 180 != 0) {
											float h = width;
											width = height;
											height = h;
											h = virtualWidth;
											virtualWidth = virtualHeight;
											virtualHeight = h;
										}
									} else {
										cropAngle = parseFloat(value) - rotation;
									}
								} else if (key == "GeometryKeystoneAmount") //$NON-NLS-1$
									geometryKeystoneAmount = parseFloat(value) / 100f;
								else if (key == "GeometryKeystoneTiltX") //$NON-NLS-1$
									geometryKeystoneTiltX = parseFloat(value);
								else if (key == "GeometryKeystoneTiltY") //$NON-NLS-1$
									geometryKeystoneTiltY = parseFloat(value);
								else if (key == "GeometryKeystoneAspect") //$NON-NLS-1$
									geometryKeystoneAspect = parseFloat(value) / 100f;
								else if (key == "GeometryKeystoneFocalLength") //$NON-NLS-1$
									geometryKeystoneFocalLength = parseFloat(value);
							} catch (ParseException e) {
								C1Activator.getDefault()
										.logError(NLS.bind(Messages.C1Detector_bad_field_value, value, key), e);
							}
						}

						@Override
						public void endElement(String uri, String localName, String qName) throws SAXException {
							if (qName.equals(stack.peek())) {
								if (PH.equals(qName)) {
									if (phDate != null && phUrl != null) {
										isDeveloped = true;
										recipe.addDerivative(uuid, phUrl, phDate);
									}
									phDate = null;
									phUrl = null;
								}
								stack.pop();
							}
						}
					};
					saxParser.parse(in, handler);
					if (overlayMap != null) {
						setStruct(overlayMap, QueryField.IPTC_CONTACT, contact);
						setStruct(overlayMap, QueryField.IPTC_LOCATIONSHOWN, location);
						setStruct(overlayMap, QueryField.IPTC_ARTWORK, artwork);
					}
				} catch (ParserConfigurationException e) {
					throw new IOException(e.toString());
				} catch (SAXException e) {
					throw new IOException(e.toString());
				}
				if (!isDeveloped) {
					recipe = null;
					continue;
				}
				if (focalLengthProvider != null) {
					double flen35mm = focalLengthProvider.get35mm();
					if (!Double.isNaN(flen35mm) && flen35mm > 0d)
						geometryKeystoneFocalLength = (float) (flen35mm / Math.sqrt(36 * 36 + 24 * 24));
				}
				// Exposure
				recipe.exposure = (float) Math.pow(2d, exposure);
				if (exposure < 0)
					recipe.highlightRecovery = 2;

				// Brightness and contrast
				if (brightness != 0d || contrast != 0d || shadows != 0f || fillLight != 0f) {
					float b = brightness / 100f + 1f;
					float c = contrast / 100f + 1f;
					float f = fillLight / 100f;
					Curve brightnessCurve = new Curve(Curve.TYPE_B_SPLINE, "BrightnessContrast", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
					// Equation: y = (brightness*x-0.5) * contrast + 0.5
					float xHigh = (c + 1f) / (2f * c * b);
					float xShadow = (c - 1f) / (2f * c * b);
					xShadow += shadows;
					if (xShadow <= 0f) {
						float yShadow = (1f - c) / 2f;
						brightnessCurve.addKnot(0f, yShadow + f);
					} else {
						brightnessCurve.addKnot(0f, 0f);
						brightnessCurve.addKnot(xShadow, 0f);
					}
					if (xHigh >= 1f) {
						float yHigh = ((2f * b - 1f) * c + 1f) / 2f;
						brightnessCurve.addKnot(1f, yHigh + f);
					} else {
						brightnessCurve.addKnot(xHigh, 1f);
						brightnessCurve.addKnot(1f, 1f);
					}
					recipe.addCurve(brightnessCurve);
				}
				if (filmCurve != null && !filmCurve.contains("Linear Response")) { //$NON-NLS-1$
					float gamma = 1f;
					float median = 0f;
					if (filmCurve.contains("High Contrast")) //$NON-NLS-1$
						gamma = 1.2f;
					else if (filmCurve.contains("Extra Shadow")) //$NON-NLS-1$
						median = 0.2f;
					recipe.addCurve(new Curve(gamma, median, "FilmCurve", Curve.CHANNEL_ALL, 0f)); //$NON-NLS-1$
				}
				// Hightlightrecovery
				int h = (int) (highlightRecovery / 100 * 7);
				recipe.highlightRecovery = h == 0 ? 0 : 2 + h;
				// ColorCorrections
				if (colorCorrections != null) {
					ColorShift shift = new ColorShift(0f, Float.NaN, saturation / 100f + 1f, ColorShift.SAT_MULT, 1f,
							vibrance / 100f + 1f);
					for (float[] valueArray : colorCorrections) {
						if (valueArray[0] == 0f) {
							float h2 = valueArray[3] / 255f;
							float s2 = valueArray[4] / 100f + 1f;
							float l2 = valueArray[5] / 100f + 1f;
							shift.setGlobalHue(shift.globHShift * h2);
							shift.setGlobalSaturation(shift.globS * s2, ColorShift.SAT_MULT);
							shift.setGlobalLuminance(shift.globL * l2);
						} else {
							float red = valueArray[5] / 127f;
							float green = valueArray[6] / 127f;
							float blue = valueArray[7] / 127f;
							float hue;
							float max = (red > green) ? ((blue > red) ? blue : red) : ((blue > green) ? blue : green);
							float min = (red < green) ? ((blue < red) ? blue : red) : ((blue < green) ? blue : green);
							float lum2 = max + min;
							if (max != min && lum2 != 0f) {
								float diff = max - min;
								if (max == red)
									hue = (green - blue) / diff + (green < blue ? 6f : 0);
								else if (max == green)
									hue = (blue - red) / diff + 2f;
								else
									hue = (red - green) / diff + 4f;
								hue /= 6f;
							} else
								hue = 0f;
							shift.addSector(hue, valueArray[10] / 255f, valueArray[13] / 255f, valueArray[3] / 255f,
									valueArray[4] / 100f + 1f, valueArray[5] / 100f + 1f, ColorShift.SAT_MULT);
						}
					}
					recipe.setHSL(shift);
				}
				if (redCurve != null)
					recipe.addCurve(createCurve(redCurve, Curve.CHANNEL_RED));
				if (greenCurve != null)
					recipe.addCurve(createCurve(greenCurve, Curve.CHANNEL_GREEN));
				if (blueCurve != null)
					recipe.addCurve(createCurve(blueCurve, Curve.CHANNEL_BLUE));
				if (targetHighlight != null || targetShadow != null || midtone != null) {
					Curve rCurve = new Curve(Curve.TYPE_CATMULL_ROM, "parametricCurveRed", Curve.CHANNEL_RED, 0f); //$NON-NLS-1$
					Curve gCurve = new Curve(Curve.TYPE_CATMULL_ROM, "parametricCurveGreen", Curve.CHANNEL_GREEN, 0f); //$NON-NLS-1$
					Curve bCurve = new Curve(Curve.TYPE_CATMULL_ROM, "parametricCurveBlue", Curve.CHANNEL_BLUE, 0f); //$NON-NLS-1$
					Curve globalCurve = new Curve(Curve.TYPE_CATMULL_ROM, "parametricCurveGlobal", Curve.CHANNEL_ALL, //$NON-NLS-1$
							0f);
					if (targetShadow != null) {
						if (targetShadow.length > 0 && targetShadow[0] != 0f)
							rCurve.addKnot(0f, targetShadow[0]);
						else
							rCurve.addKnot(0f, 0f);
						if (targetShadow.length > 1 && targetShadow[1] != 0f)
							gCurve.addKnot(0f, targetShadow[1]);
						else
							gCurve.addKnot(0f, 0f);
						if (targetShadow.length > 2 && targetShadow[2] != 0f)
							bCurve.addKnot(0f, targetShadow[2]);
						else
							bCurve.addKnot(0f, 0f);
						if (targetShadow.length > 3 && targetShadow[3] != 0f)
							globalCurve.addKnot(0f, targetShadow[3]);
						else
							globalCurve.addKnot(0f, 0f);
					}
					if (midtone != null) {
						if (midtone.length > 0 && midtone[0] != 0f)
							rCurve.addKnot(0.5f - midtone[0], 0.5f);
						if (midtone.length > 1 && midtone[1] != 0f)
							gCurve.addKnot(0.5f - midtone[1], 0.5f);
						if (midtone.length > 2 && midtone[2] != 0f)
							bCurve.addKnot(0.5f - midtone[2], 0.5f);
						if (midtone.length > 3 && midtone[3] != 0f)
							globalCurve.addKnot(0.5f - midtone[3], 0.5f);
					}
					if (targetHighlight != null) {
						if (targetHighlight.length > 0 && targetHighlight[0] != 1f)
							rCurve.addKnot(1f, targetHighlight[0]);
						else
							rCurve.addKnot(1f, 1f);
						if (targetHighlight.length > 1 && targetHighlight[1] != 1f)
							gCurve.addKnot(1f, targetHighlight[1]);
						else
							gCurve.addKnot(1f, 1f);
						if (targetHighlight.length > 2 && targetHighlight[2] != 1f)
							bCurve.addKnot(1f, targetHighlight[2]);
						else
							bCurve.addKnot(1f, 1f);
						if (targetHighlight.length > 3 && targetHighlight[3] != 1f)
							globalCurve.addKnot(1f, targetHighlight[3]);
						else
							globalCurve.addKnot(1f, 1f);
					}
					recipe.addCurve(globalCurve);
					recipe.addCurve(rCurve);
					recipe.addCurve(gCurve);
					recipe.addCurve(bCurve);
					// Vignette
					if (lensLightFalloff != null && lensLightFalloff.length >= 2 && lensLightFalloff[1] != 0f) {
						float amount = lensLightFalloff[0];
						recipe.setVignette(new Vignette(amount, 0.5f, Vignette.HSL));
					}
					// Geometry
					PerspectiveCorrection perspectiveCorrection = null;
					if (geometryKeystoneAmount != 0f && (geometryKeystoneTiltX != 0f || geometryKeystoneTiltY != 0f)
							|| geometryKeystoneAspect != 0f) {
						double flen = geometryKeystoneFocalLength * 1.2;

						Matrix trans = new Matrix(new double[][] { { 1d, 0d, 0d, 0d }, { 0d, 1d, 0d, 0d },
								{ 0d, 0d, 1d, -flen }, { 0d, 0d, 0d, 1d } });
						double theta = -geometryKeystoneTiltY * geometryKeystoneAmount / 100f;
						Matrix xrot = new Matrix(
								new double[][] { { 1d, 0d, 0d, 0d }, { 0d, Math.cos(theta), -Math.sin(theta), 0d },
										{ 0d, Math.sin(theta), Math.cos(theta), 0d }, { 0d, 0d, 0d, 1d } });
						theta = geometryKeystoneTiltX * geometryKeystoneAmount / 100f;
						Matrix yrot = new Matrix(
								new double[][] { { Math.cos(theta), 0d, Math.sin(theta), 0d }, { 0d, 1d, 0d, 0d },
										{ -Math.sin(theta), 0d, Math.cos(theta), 0d }, { 0d, 0d, 0d, 1d } });
						Matrix persp = new Matrix(new double[][] { { flen, 0d, 0d, 0d }, { 0d, flen, 0d, 0d },
								{ 0d, 0d, flen, 0d }, { 0d, 0d, 1d, 0d } });
						Matrix pm = new Matrix(new double[][] { { 1d, 0d, 0d, 0d }, { 0d, 1d, 0d, 0d },
								{ 0d, 0d, 1d, 0d }, { 0d, 0d, 0d, 1d } });
						pm = pm.times(persp);
						pm = pm.times(trans.inverse());
						pm = pm.times(yrot);
						pm = pm.times(xrot);
						pm = pm.times(trans);
						final double[][] m = pm.getArray();
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
						double m32_flen_m33 = m32 * flen + m33;
						double m02_flen_m03 = m02 * flen + m03;
						double m12_flen_m13 = m12 * flen + m13;
						// x=-0.5, y=-0.5
						double s = m30 * -0.5d + m31 * -0.5 + m32_flen_m33;
						double px00 = (m00 * -0.5d + m01 * -0.5d + m02_flen_m03) / s;
						double py00 = (m10 * -0.5d + m11 * -0.5d + m12_flen_m13) / s;
						// x=0.5, y=-0.5
						s = m30 * 0.5d + m31 * -0.5d + m32_flen_m33;
						double px01 = (m00 * 0.5d + m01 * -0.5d + m02_flen_m03) / s;
						double py01 = (m10 * 0.5d + m11 * -0.5d + m12_flen_m13) / s;
						// x=-0.5, y=0.5
						s = m30 * -0.5d + m31 * 0.5d + m32_flen_m33;
						double px10 = (m00 * -0.5d + m01 * 0.5d + m02_flen_m03) / s;
						double py10 = (m10 * -0.5d + m11 * 0.5d + m12_flen_m13) / s;
						// x=0.5, y=0.5
						s = m30 * 0.5d + m31 * 0.5d + m32_flen_m33;
						double px11 = (m00 * 0.5d + m01 * 0.5d + m02_flen_m03) / s;
						double py11 = (m10 * 0.5d + m11 * 0.5d + m12_flen_m13) / s;
						double dx = Math.min(Math.abs(px01 - px00), Math.abs(px11 - px10));
						double dy = Math.min(Math.abs(py10 - py00), Math.abs(py11 - py01));
						double x0 = (px00 + px01 + px10 + px11) / 4;
						double y0 = (py00 + py01 + py10 + py11) / 4;
						double xfac = (dx != 0) ? 1d / dx : 1d;
						double yfac = (dy != 0) ? 1d / dy : 1d;
						double yaspect = (geometryKeystoneAspect > 0) ? 1 / (1 - geometryKeystoneAspect) : 1;
						double xaspect = (geometryKeystoneAspect < 0) ? 1 / (1 + geometryKeystoneAspect) : 1;
						pm = new Matrix(new double[][] { { xaspect, 0d, 0d, 0d }, { 0d, yaspect, 0d, 0d },
								{ 0d, 0d, 1d, 0d }, { 0d, 0d, 0d, 1d } });
						pm = pm.times(persp);
						pm = pm.times(trans.inverse());
						pm = pm.times(yrot);
						pm = pm.times(xrot);
						pm = pm.times(trans);
						double[][] a = pm.getArray();
						a[0][0] *= xfac;
						a[0][1] *= xfac;
						a[0][2] *= xfac;
						a[0][3] *= xfac;
						a[1][0] *= yfac;
						a[1][1] *= yfac;
						a[1][2] *= yfac;
						a[1][3] *= yfac;
						perspectiveCorrection = new PerspectiveCorrection(pm, flen, new RGB(64, 64, 64));
						perspectiveCorrection.finalTranslation = new Point2D.Double(x0 * xfac, y0 * yfac);
						recipe.setPerspectiveCorrection(perspectiveCorrection);
						virtualWidth *= xfac * xaspect;
						virtualHeight *= yfac * yaspect;
					}
					// Cropping
					cropAngle = cropAngle + 3600f % 360f;
					int coarseRot = ((int) cropAngle + 45) / 90 * 90;
					cropAngle -= coarseRot;
					if (cropping != null || cropAngle != 0f) {
						double cx, cy, cw, ch;
						if (cropping != null) {
							switch (rotation) {
							case 90:
								cy = cropping[0] / height;
								cx = (width - cropping[1]) / width;
								cw = cropping[2] / virtualWidth;
								ch = cropping[3] / virtualHeight;
								break;
							case 180:
								cx = (width - cropping[0]) / width;
								cy = (height - cropping[1]) / height;
								cw = cropping[2] / virtualWidth;
								ch = cropping[3] / virtualHeight;
								break;
							case 270:
								cy = (height - cropping[0]) / height;
								cx = cropping[1] / width;
								cw = cropping[2] / virtualWidth;
								ch = cropping[3] / virtualHeight;
								break;

							default:
								cx = cropping[0] / width;
								cy = cropping[1] / height;
								cw = cropping[2] / virtualWidth;
								ch = cropping[3] / virtualHeight;
								break;
							}
						} else {
							cx = cy = 0.5f;
							cw = ch = 1f;
						}
						if (perspectiveCorrection != null) {
							cx += perspectiveCorrection.finalTranslation.x;
							cy += perspectiveCorrection.finalTranslation.y;
						}
						if (cropAngle != 0f) {
							double r = Math.toRadians(-cropAngle);
							double c = Math.cos(r);
							double s = Math.sin(r);
							double nx = (cx * c * width + cy * s * height) / width;
							double ny = (-cx * s * width + cy * c * height) / height;
							cx = nx;
							cy = ny;
							switch (rotation) {
							case 270:
							case 90:
								if (s < 0) {
									// TODO where do this heuristic values come
									// from?
									cx -= 0.87 * s;
									cy += 0.17 * s;
								} else {
									cx -= 0.465 * s;
									cy += 0.585 * s;
								}
								break;
							default:
								if (s < 0) {
									// TODO where do this heuristic values come
									// from?
									cx -= 0.585 * s;
									cy += 0.465 * s;
								} else {
									cx -= 0.17 * s;
									cy += 0.87 * s;
								}
								break;
							}
						}
						float x1 = (float) (cx - cw / 2);
						float x3 = (float) (cx + cw / 2);
						float y1 = (float) (cy - ch / 2);
						float y3 = (float) (cy + ch / 2);
						recipe.setCropping(
								new Cropping(y1, y3, x1, x3, cropAngle, Cropping.NOFILL, new RGB(128, 128, 128)));
					}
					if (coarseRot != 0)
						recipe.setRotation(new Rotation(coarseRot, false, false));
					// Sharpening
					if (sharpen > 0d && sharpenRadius > 0f)
						recipe.addUnsharpFilter(new UnsharpMask(sharpen / 100f, sharpenRadius,
								sharpenEdgeMasking / 255f, 0f, null, UnsharpMask.SHARPEN));
					// Local contrast
					if (clarity != 0f) {
						Curve toneMask = new Curve(Curve.TYPE_LINEAR, "toneMask", Curve.CHANNEL_ALL, 1f); //$NON-NLS-1$
						toneMask.addKnot(0, 0);
						toneMask.addKnot(1 / 3f, 1f);
						toneMask.addKnot(2 / 3f, 1f);
						toneMask.addKnot(1f, 0f);
						recipe.addUnsharpFilter(new UnsharpMask(clarity / 30f, Math.abs(clarity / 150f) + 30, -0.5f, 0f,
								toneMask, UnsharpMask.LOCAL_CONTRAST));
					}
					// Noise reduction
					recipe.noiseReduction = 100 + 9 * (int) luminanceSmoothing;
				}
			}
			return recipe;
		} catch (IOException e) {
			throw e;
		}
	}

	protected Date parseDate(String value) {
		try {
			return sf.parse(value);
		} catch (ParseException e) {
			C1Activator.getDefault().logError(NLS.bind(Messages.C1Detector_bad_date, value), e);
			return null;
		}
	}

	protected Curve createCurve(float[][] array, int channel) {
		if (array == null)
			return null;
		Curve curve = new Curve(Curve.TYPE_CATMULL_ROM, "PointCurve", channel, 1f); //$NON-NLS-1$
		for (int i = 0; i < array.length; i++)
			curve.addKnot(array[i][0], array[i][1]);
		return curve;
	}

	protected float[][] parseArray2(String value, String sep1, String sep2) throws ParseException {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(value, sep1);
		while (st.hasMoreTokens())
			tokens.add(st.nextToken());
		float[][] result = new float[tokens.size()][];
		int i = 0;
		for (String t : tokens)
			result[i++] = parseArray(t, sep2);
		return result;
	}

	protected float[] parseArray(String value, String sep) throws ParseException {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(value, sep);
		while (st.hasMoreTokens())
			tokens.add(st.nextToken());
		float[] result = new float[tokens.size()];
		int i = 0;
		for (String t : tokens)
			result[i++] = parseFloat(t);
		return result;
	}

	public List<RecipeFolder> computeWatchedMetaFilesOrFolders(WatchedFolder[] watchedFolders,
			Map<File, List<IRecipeDetector>> detectorMap, Map<File, List<IRecipeDetector>> recursiveDetectorMap,
			boolean update, boolean remove) {
		return computeWatchedMetaFolders(watchedFolders, detectorMap, recursiveDetectorMap, remove,
				CAPTURE_ONE_FOLDERS);
	}

	public File getChangedImageFile(File metaFile, WatchedFolder[] watchedFolders) {
		File settings = metaFile.getParentFile();
		if (settings != null) {
			File c1 = settings.getParentFile();
			if (c1 != null) {
				File home = c1.getParentFile();
				if (home != null) {
					String fileName = metaFile.getName();
					if (fileName.endsWith(CAPTURE_ONE_SETTINGS)) {
						fileName = fileName.substring(0, fileName.length() - CAPTURE_ONE_SETTINGS.length());
						String huri = home.toURI().toString();
						for (WatchedFolder watchedFolder : watchedFolders) {
							String wuri = watchedFolder.getUri();
							if (huri.equals(wuri) || (watchedFolder.getRecursive() && huri.startsWith(wuri))) {
								File imageFile = new File(home, fileName);
								if (imageFile.exists())
									return imageFile;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public boolean usesIncrementalUpdate() {
		return true;
	}

	public Recipe loadRecipe(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap) {
		if (uri.toLowerCase().endsWith(CAPTURE_ONE_SETTINGS)) {
			try {
				return loadRecipeFiles(new File[] { new File(new URI(uri)) }, focalLengthProvider, overlayMap);
			} catch (URISyntaxException e) {
				C1Activator.getDefault().logError(NLS.bind(Messages.C1Detector_bad_uri, uri), e);
			} catch (IOException e) {
				C1Activator.getDefault().logError(NLS.bind(Messages.C1Detector_io_error, uri), e);
			}
		}
		return null;
	}

}
