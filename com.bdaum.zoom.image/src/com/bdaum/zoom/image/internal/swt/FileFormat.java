/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.internal.swt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

/**
 * Abstract factory class for loading/unloading images from files or streams in
 * various image file formats.
 *
 */
public abstract class FileFormat {
	//	static final String FORMAT_PACKAGE = "org.eclipse.swt.internal.image"; //$NON-NLS-1$ // bd
	static final String FORMAT_PACKAGE = "com.bdaum.zoom.image.internal.swt"; //$NON-NLS-1$ // bd
	static final String FORMAT_SUFFIX = "FileFormat"; //$NON-NLS-1$
	static final String[] FORMATS = {
			"WinBMP", "WinBMP", "GIF", "WinICO", "JPEG", "PNG", "TIFF", "OS2BMP" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$
	static final FileFormat[] instances = new FileFormat[FORMATS.length];
	static final int[] formatOrder = new int[] { SWT.IMAGE_JPEG,
			SWT.IMAGE_TIFF, SWT.IMAGE_PNG, SWT.IMAGE_GIF, SWT.IMAGE_BMP,
			SWT.IMAGE_BMP_RLE, SWT.IMAGE_ICO, SWT.IMAGE_OS2_BMP };
	LEDataInputStream inputStream;
	LEDataOutputStream outputStream;
	ImageLoader loader;
	int compression;

	static FileFormat getFileFormat(LEDataInputStream stream, String format)
			throws Exception {
		Class<?> clazz = Class.forName(FORMAT_PACKAGE + '.' + format
				+ FORMAT_SUFFIX);
		FileFormat fileFormat = (FileFormat) clazz.newInstance();
		if (fileFormat.isFileFormat(stream))
			return fileFormat;
		return null;
	}

	/**
	 * Return whether or not the specified input stream represents a supported
	 * file format.
	 */
	abstract boolean isFileFormat(LEDataInputStream stream);

	abstract ImageData[] loadFromByteStream();

	/**
	 * Read the specified input stream, and return the device independent image
	 * array represented by the stream.
	 */
	public ImageData[] loadFromStream(LEDataInputStream stream) {
		try {
			inputStream = stream;
			return loadFromByteStream();
		} catch (Exception e) {
			if (e instanceof IOException) {
				SWT.error(SWT.ERROR_IO, e);
			} else {
				SWT.error(SWT.ERROR_INVALID_IMAGE, e);
			}
			return null;
		}
	}

	/**
	 * Read the specified input stream using the specified loader, and return
	 * the device independent image array represented by the stream.
	 *
	 * @param formatHint
	 */
	@SuppressWarnings("null")
	public static ImageData[] load(InputStream is, ImageLoader loader,
			int formatHint) {
		FileFormat fileFormat = null;
		LEDataInputStream stream = new LEDataInputStream(is);
		if (formatHint >= 0) {
			try {
				fileFormat = getInstance(formatHint);
			} catch (ClassNotFoundException e) {
				FORMATS[formatHint] = null;
			} catch (Exception e) {
			}
		} else
			for (int i = 0; i < FORMATS.length; i++) {
				int k = formatOrder[i];
				if (FORMATS[k] != null) {
					try {
						fileFormat = getInstance(k);
						if (fileFormat.isFileFormat(stream)) {
							instances[k] = null;
							break;
						}
					} catch (ClassNotFoundException e) {
						FORMATS[k] = null;
					} catch (Exception e) {
					}
				}
			}
		if (fileFormat == null)
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		fileFormat.loader = loader;
		return fileFormat.loadFromStream(stream);
	}

	private static FileFormat getInstance(int i) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (instances[i] == null) {
			Class<?> clazz = Class.forName(FORMAT_PACKAGE + '.' + FORMATS[i]
					+ FORMAT_SUFFIX);
			instances[i] = (FileFormat) clazz.newInstance();
		}
		return instances[i];
	}

	/**
	 * Write the device independent image array stored in the specified loader
	 * to the specified output stream using the specified file format.
	 */
	@SuppressWarnings("null")
	public static void save(OutputStream os, int format, ImageLoader loader) {
		if (format < 0 || format >= FORMATS.length)
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		if (FORMATS[format] == null)
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		if (loader.data == null || loader.data.length < 1)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		LEDataOutputStream stream = new LEDataOutputStream(os);
		FileFormat fileFormat = null;
		try {
			fileFormat = getInstance(format);
		} catch (Exception e) {
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		}
		instances[format] = null;
		if (format == SWT.IMAGE_BMP_RLE) {
			switch (loader.data[0].depth) {
			case 8:
				fileFormat.compression = 1;
				break;
			case 4:
				fileFormat.compression = 2;
				break;
			}
		}
		fileFormat.unloadIntoStream(loader, stream);
	}

	abstract void unloadIntoByteStream(ImageLoader loader);

	/**
	 * Write the device independent image array stored in the specified loader
	 * to the specified output stream.
	 */
	public void unloadIntoStream(ImageLoader loader, LEDataOutputStream stream) {
		try {
			outputStream = stream;
			unloadIntoByteStream(loader);
			outputStream.flush();
		} catch (Exception e) {
			try {
				outputStream.flush();
			} catch (Exception f) {
			}
			SWT.error(SWT.ERROR_IO, e);
		}
	}
}
