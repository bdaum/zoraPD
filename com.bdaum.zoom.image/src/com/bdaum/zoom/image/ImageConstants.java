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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.common.internal.IniReader;
import com.bdaum.zoom.image.internal.EipFileHandler;
import com.bdaum.zoom.image.internal.ImageActivator;

/**
 * Commonly used and image related constants. In addition, static image format
 * related methods are provided
 *
 */
public class ImageConstants {

	public static class RawType {

		private String ext;
		private String name;
		private String[] unsupportedBy;

		public RawType(String ext, String name, String[] unsupportedBy) {
			this.ext = ext;
			this.name = name;
			this.unsupportedBy = unsupportedBy;
		}

		public Object getName() {
			return name;
		}

		public boolean isUnsupportedBy(String id) {
			if (unsupportedBy != null)
				for (String s : unsupportedBy) {
					if (s.equals(id))
						return true;
				}
			return false;
		}

		@Override
		public String toString() {
			return NLS.bind("{0} ({1})", ext, name); //$NON-NLS-1$
		}
	}

	private static final String RAW_INI = "/raw.ini"; //$NON-NLS-1$

	public static final int NOCMS = 0;
	public static final int SRGB = 1;
	public static final int ARGB = 2;
	public static final int REC709 = 3;
	public static final int REC2002 = 4;
	public static final int DCIP3 = 5;
	public static final int DCIP60 = 6;
	public static final int DCIP65 = 7;
	public static final int CUSTOM = 8;
	
	public static final String[] ICCFILES = new String[] { null, "/icc/sRGB Color Space Profile.icm", //$NON-NLS-1$
			"/icc/AdobeRGB1998.icc", //$NON-NLS-1$
			"/icc/Rec709-Rec1886.icc", //$NON-NLS-1$
			"/icc/Rec2020-Rec1886.icc", //$NON-NLS-1$
			"/icc/P3DCI.icc", //$NON-NLS-1$
			"/icc/P3D60.icc", //$NON-NLS-1$
			"/icc/P3D65.icc", null //$NON-NLS-1$
	};

	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	public static int NPROCESSORS = AVAILABLE_PROCESSORS > 2 ? AVAILABLE_PROCESSORS - 1 : AVAILABLE_PROCESSORS;

	public static final String APPNAME = Platform.getProduct().getProperty("shortName"); //$NON-NLS-1$
	public final static String APPLICATION_NAME = Platform.getProduct().getName();

	public static final String IMAGE_X_RAW = "image/x-raw"; //$NON-NLS-1$
	public static final String IMAGE_X_DNG = "image/x-adobe-dng"; //$NON-NLS-1$
	public static final String IMAGE_JPEG = "image/jpeg"; //$NON-NLS-1$
	public static final String DNG_ADOBE_DIGITAL_NEGATIVE = "DNG Adobe Digital Negative"; //$NON-NLS-1$
	public static final String JPEG = "JPEG"; //$NON-NLS-1$
	public static final String TIFF = "TIFF"; //$NON-NLS-1$
	public static final String DNG = "DNG"; //$NON-NLS-1$
	public static final String[] JPEGEXTENSIONS = new String[] { ".jpg", ".jpe", ".jpeg", ".jfif" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	public static final String[] VIDEOEXTENSIONS = new String[] { ".aiv", ".mov", ".mp4", ".avchd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private static String[] PredefinedImageFileExtensions = new String[] { "*.bmp", "*.dcm;*.dc3;*.dic;*.dicm", //$NON-NLS-1$ //$NON-NLS-2$
			"*.dcx;*.pcx", "*.dng", "*.fits", "*.fpx", "*.gif", "*.hdr;*.pfm", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"*.iff;*.pchg;*.sham", "*.jpg;*.jpe;*.jpeg;*.jfif", "*.pam", "*.pbm", "*.pict;*.pct", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"*.pgm", "*.png", "*.ppm", "*.psd;*.pdd;*.psdt;*.psb", "*.sgi", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"*.sun;*.ras;*.rast;*.rs;*.sr;*.scr;*.im1;*.im8;*.im24;*.im32", "*.tga", "*.tif;*.tiff", "*.webp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private static String[] PredefinedMimeTypes = new String[] { "image/bmp", "application/dicom", //$NON-NLS-1$ //$NON-NLS-2$
			"image/x-pc-paintbrush", "image/x-adobe-dng", "image/fits", "image/vnd.fpx", "image/gif", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"image/vnd.radiance", //$NON-NLS-1$
			"image/x-iff", IMAGE_JPEG, "image/x-portable-arbitrarymap", "image/x-portable-bitmap", "image/pict", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"image/x-portable-graymap", "image/png", //$NON-NLS-1$ //$NON-NLS-2$
			"image/x-portable-pixmap", "application/vnd.adobe.photoshop", "image/x-sgi", "image/ras", "image/x-targa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"image/tiff", "image/webp" }; //$NON-NLS-1$ //$NON-NLS-2$

	private static String[] PredefinedImageFileNames = new String[] { "Windows or OS/2 Bitmap (*.bmp)", //$NON-NLS-1$
			"DICOM - Digital Imaging and Communications in Medicine (*.dcm,*.dc3,*.dic,*.dicm)", //$NON-NLS-1$
			"ZSoft Paintbrush (*.dcx, *.pcx)", //$NON-NLS-1$
			"Adobe Digital Negative (*.dng)", //$NON-NLS-1$
			"Graphics Interchange Format (*.gif)", //$NON-NLS-1$
			"Flexible Image Transport System (*.fits)", //$NON-NLS-1$
			"Kodak Flashpix (*.fpx)", //$NON-NLS-1$
			"Radiance HDR (*.hdr, *.pfm)", //$NON-NLS-1$
			"Amiga Interchange File Format (*.iff, *.pchg, *.sham)", //$NON-NLS-1$
			"Joint Photographic Experts Group (*.jpg,*.jpe,*.jpeg,*.jfif)", //$NON-NLS-1$
			"Portable Arbitrary Map (*.pam)", //$NON-NLS-1$
			"Portable Bitmap (*.pbm)", //$NON-NLS-1$
			"Apple Mac Paint Picture Format (*.pict, *.PCT)", //$NON-NLS-1$
			"Portable Graymap (*.pgm)", //$NON-NLS-1$
			"Portable Network Graphics (*.png)", //$NON-NLS-1$
			"Portable Pixelmap (*.ppm)", //$NON-NLS-1$
			"PSD Photoshop (*.psd)", //$NON-NLS-1$
			"Silicon Graphics (*.sgi)", //$NON-NLS-1$
			"Sun Raster (*.sun, *.ras, *.rast, *.rs, *.sr, *.scr, *.im1, *.im8, *.im24, *.im32)", //$NON-NLS-1$
			"Truevision Targa Graphic (*.tga)", //$NON-NLS-1$
			"Tagged Image File Format (*.tif,*.tiff)", //$NON-NLS-1$
			"Google WebP (*.webp)" }; //$NON-NLS-1$

	private static Map<String, String> fileNameMap = new HashMap<String, String>(
			PredefinedImageFileExtensions.length * 3 / 2);

	private static Map<String, IFileHandler> FILEHANDLERS = new HashMap<String, IFileHandler>(3);

	private static Map<String, RawType> RAWFORMATS;
	private static final Set<String> SWTFORMATS = new HashSet<String>(15);

	private static Map<String, String> MIMEMAP = null;

	// thumbnail sharpening
	public static final int SHARPEN_NONE = 0;
	public static final int SHARPEN_LIGHT = 20;
	public static final int SHARPEN_MEDIUM = 40;
	public static final int SHARPEN_HEAVY = 60;
	
	// Voicenote file extensions
	public static final String[] VOICEEXT = new String[] {".WAV", ".wav"};  //$NON-NLS-1$//$NON-NLS-2$

	public static final int IMAGE_WEBP = 99; // Extends SWT constants

	public static List<String> getNonRawImageFileExtensions() {
		Map<String, String> mimeMap = getMimeMap();
		List<String> extList = new ArrayList<String>(mimeMap.size());
		for (Entry<String, String> entry : mimeMap.entrySet())
			if (entry.getValue() != IMAGE_X_RAW)
				extList.add("*." + entry.getKey()); //$NON-NLS-1$
		Collections.sort(extList);
		return extList;
	}

	public static Set<String> getAllFormats() {
		Set<String> allFormats = new HashSet<String>(getMimeMap().keySet());
		allFormats.add(""); //$NON-NLS-1$
		return allFormats;
	}

	public static Map<String, String> getMimeMap() {
		if (MIMEMAP == null) {
			MIMEMAP = new HashMap<>(120);
			for (int i = 0; i < PredefinedImageFileExtensions.length; i++) {
				String mime = PredefinedMimeTypes[i];
				StringTokenizer st = new StringTokenizer(PredefinedImageFileExtensions[i], ";"); //$NON-NLS-1$
				while (st.hasMoreTokens())
					MIMEMAP.put(st.nextToken().substring(2), mime);
			}
			for (String ext : getRawFormatMap().keySet())
				MIMEMAP.put(ext, IMAGE_X_RAW);
			MIMEMAP.putAll(ImageActivator.getDefault().getImportedExtensions());
		}
		return MIMEMAP;
	}

	public static String[] getSupportedImageFileExtensionsGroups(boolean includeRaw) {
		List<String> importedNames = ImageActivator.getDefault().getImportedNames();
		int l = PredefinedImageFileExtensions.length;
		String[] result = new String[l + importedNames.size() + (includeRaw ? 1 : 0)];
		System.arraycopy(PredefinedImageFileExtensions, 0, result, 0, l);
		for (String name : importedNames) {
			String exts = ""; //$NON-NLS-1$
			int p = name.indexOf('(');
			if (p >= 0) {
				int q = name.indexOf(')', p);
				if (q >= 0)
					exts = name.substring(p + 1, q);
			}
			result[l++] = exts.replaceAll(", ", ";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (includeRaw)
			result[l] = listRawExtensions(";"); //$NON-NLS-1$
		return result;
	}

	public static String[] getSupportedImageFileNames(boolean includeRaw) {
		List<String> importedNames = ImageActivator.getDefault().getImportedNames();
		List<String> extList = Arrays.asList(PredefinedImageFileExtensions);
		List<String> result = new ArrayList<String>(extList.size() + importedNames.size() + 1);
		for (String ext : extList) {
			String name = fileNameMap.get(ext);
			result.add(name != null ? name : ""); //$NON-NLS-1$
		}
		result.addAll(importedNames);
		if (includeRaw)
			result.add(NLS.bind(Messages.ImageConstants_raw_files, listRawExtensions(", "))); //$NON-NLS-1$
		return result.toArray(new String[result.size()]);
	}

	private static String listRawExtensions(String sep) {
		Set<String> rawFormats = getRawFormatMap().keySet();
		String[] extensions = rawFormats.toArray(new String[rawFormats.size()]);
		Arrays.sort(extensions);
		StringBuilder sb = new StringBuilder();
		for (String ext : extensions) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append("*.").append(ext); //$NON-NLS-1$
		}
		return sb.toString();
	}

	public static Map<String, RawType> getRawFormatMap() {
		if (RAWFORMATS == null) {
			URL iniUri = FileLocator.find(ImageActivator.getDefault().getBundle(), new Path(RAW_INI), null);
			try (InputStream in = iniUri.openStream()) {
				IniReader reader = new IniReader(in, false);
				Collection<String> sections = reader.listSections();
				RAWFORMATS = new HashMap<String, RawType>(sections.size() * 3 / 2);
				for (String ext : sections) {
					String name = reader.getPropertyString(ext, "name", //$NON-NLS-1$
							NLS.bind(Messages.ImageConstants_x_format, ext));
					RAWFORMATS.put(ext, new RawType(ext, name, reader.getStringArray(ext, "unsupportedBy"))); //$NON-NLS-1$
				}
			} catch (IOException e) {
				RAWFORMATS = new HashMap<String, RawType>(0);
			}
		}
		return RAWFORMATS;
	}

	/**
	 * Returns a file handler for a given file, or null if no specific filehandler
	 * exists
	 *
	 * @param file
	 *            - input file
	 * @return - file handler or null
	 */
	public static IFileHandler findFileHandler(File file) {
		String name = file.getName();
		int p = name.lastIndexOf('.');
		return p >= 0 ? FILEHANDLERS.get(name.substring(p + 1).toLowerCase(Locale.ENGLISH)) : null;
	}

	/**
	 * Test if a file is a RAW file
	 *
	 * @param nameOrURI
	 *            - short file name or URI
	 * @param includeDng
	 *            - true if the test is to be extended on the DNG format, too
	 * @return true if the file is a RAW (or DNG) file
	 */
	public static boolean isRaw(String nameOrURI, boolean includeDng) {
		int p = nameOrURI.lastIndexOf('.');
		String extension = (p >= nameOrURI.lastIndexOf('/')) ? nameOrURI.substring(p + 1).toLowerCase(Locale.ENGLISH)
				: ""; //$NON-NLS-1$
		return getRawFormatMap().containsKey(extension) || (includeDng && "dng".equals(extension));//$NON-NLS-1$
	}

	/**
	 * Test if a file is a DNG file
	 *
	 * @param nameOrURI
	 *            - short file name or URI
	 * @return true if the file is a DNG file
	 */
	public static boolean isDng(String nameOrURI) {
		int p = nameOrURI.lastIndexOf('.');
		return "dng".equals((p >= nameOrURI.lastIndexOf('/')) ? nameOrURI.substring(p + 1).toLowerCase(Locale.ENGLISH) //$NON-NLS-1$
				: null);
	}

	/**
	 * Returns the description of the raw format
	 *
	 * @param ext
	 *            - file name extensiono
	 * @return - raw format description
	 */
	public static String getRawFormatDescription(String ext) {
		RawType rawType = getRawFormatMap().get(ext);
		return rawType == null ? null : rawType.toString();
	}

	/**
	 * Test if a an image file can be loaded by the SWT
	 *
	 * @param ext
	 *            - file name extension
	 * @return true if the file can be loaded by the SWT
	 */
	public static boolean isSwtLoadable(String ext) {
		return ImageConstants.SWTFORMATS.contains(ext);
	}

	/**
	 * Test if a an image file is a JPEG file
	 *
	 * @param ext
	 *            - file name extension
	 * @return true if the file is a JPEG file
	 */
	public static boolean isJpeg(String ext) {
		String s = ext.toLowerCase();
		return s.startsWith("j") && ImageConstants.SWTFORMATS.contains(s); //$NON-NLS-1$
	}
	
	/**
	 * Test if a an image file is a JPEG file
	 *
	 * @param ext
	 *            - file name extension
	 * @return true if the file is a JPEG file
	 */
	public static boolean isWebP(String ext) {
		return ext.equalsIgnoreCase("webp"); //$NON-NLS-1$
	}

	
	/**
	 * Test if a an an extension is an image file extension
	 *
	 * @param ext
	 *            - file name extension
	 * @return true if the file is an image file extension
	 */
	public static boolean isImageExt(String ext) {
		return getAllFormats().contains(ext.toLowerCase());
	}

	static {
		Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("jpeg2000"); //$NON-NLS-1$
		if (it.hasNext()) {
			int j = -1;
			int l = PredefinedImageFileExtensions.length;
			for (int i = 0; i < l; i++)
				if (PredefinedImageFileExtensions[i].contains("jpg")) { //$NON-NLS-1$
					j = i;
					break;
				}
			if (j >= 0) {
				String[] newExt = new String[l + 1];
				System.arraycopy(PredefinedImageFileExtensions, 0, newExt, 0, j + 1);
				System.arraycopy(PredefinedImageFileExtensions, j + 1, newExt, j + 2, l - j - 1);
				newExt[j + 1] = "*.jp2;*.j2k;*.jpf;*.jpg2;*.jpx"; //$NON-NLS-1$
				PredefinedImageFileExtensions = newExt;
				String[] newMime = new String[l + 1];
				System.arraycopy(PredefinedMimeTypes, 0, newMime, 0, j + 1);
				System.arraycopy(PredefinedMimeTypes, j + 1, newMime, j + 2, l - j - 1);
				newMime[j + 1] = "image/jp2"; //$NON-NLS-1$
				PredefinedMimeTypes = newMime;
				String[] newNames = new String[l + 1];
				System.arraycopy(PredefinedImageFileNames, 0, newNames, 0, j + 1);
				System.arraycopy(PredefinedImageFileNames, j + 1, newNames, j + 2, l - j - 1);
				newNames[j + 1] = "JPEG 2000 (*.jp2,*.j2k,*.jpf,*.jpg2,*.jpx)"; //$NON-NLS-1$
				PredefinedImageFileNames = newNames;
			}
		}
		for (int i = 0; i < PredefinedImageFileExtensions.length; i++)
			fileNameMap.put(PredefinedImageFileExtensions[i], PredefinedImageFileNames[i]);
		SWTFORMATS.add("bmp"); //$NON-NLS-1$
		SWTFORMATS.add("gif"); //$NON-NLS-1$
		SWTFORMATS.add("png"); //$NON-NLS-1$
		SWTFORMATS.add("tif"); //$NON-NLS-1$
		SWTFORMATS.add("tiff"); //$NON-NLS-1$
		SWTFORMATS.add("jpg"); //$NON-NLS-1$
		SWTFORMATS.add("jpe"); //$NON-NLS-1$
		SWTFORMATS.add("jpeg"); //$NON-NLS-1$
		SWTFORMATS.add("jfif"); //$NON-NLS-1$

		FILEHANDLERS.put("eip", new EipFileHandler()); //$NON-NLS-1$
	}

	public static void setNoProcessors(int np) {
		NPROCESSORS = np;
	}

}
