/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum - reading 16 Bit TIFF, 
 *					   reading compressed 16 Bit TIFF, 
 *                     reading LZW and Deflator compressed TIFF with or without horizontal differencing, 
 *                     reading CMYK TIFFs
 *                     reading RGBA TIFFs
 *******************************************************************************/
package com.bdaum.zoom.image.internal.swt;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

final class TIFFDirectory {

	TIFFRandomFileAccess file;
	boolean isLittleEndian;
	boolean is16Bit; // bd
	ImageLoader loader;
	// int depth; bd
	int sourceDepth; // bd
	int targetDepth;// bd

	/* Directory fields */
	int imageWidth;
	int imageLength;
	int[] bitsPerSample;
	int compression;
	int photometricInterpretation;
	int[] stripOffsets;
	int samplesPerPixel;
	int rowsPerStrip;
	int[] stripByteCounts;
	int t4Options;
	int colorMapOffset;
	int predictor; // bd

	/* Encoder fields */
	ImageData image;
	LEDataOutputStream out;

	static final int NO_VALUE = -1;

	static final short TAG_ImageWidth = 256;
	static final short TAG_ImageLength = 257;
	static final short TAG_BitsPerSample = 258;
	static final short TAG_Compression = 259;
	static final short TAG_PhotometricInterpretation = 262;
	static final short TAG_StripOffsets = 273;
	static final short TAG_SamplesPerPixel = 277;
	static final short TAG_RowsPerStrip = 278;
	static final short TAG_StripByteCounts = 279;
	static final short TAG_XResolution = 282;
	static final short TAG_YResolution = 283;
	static final short TAG_T4Options = 292;
	static final short TAG_ResolutionUnit = 296;
	static final short TAG_Predictor = 317; // bd
	static final short TAG_ColorMap = 320;

	static final int TYPE_BYTE = 1;
	static final int TYPE_ASCII = 2;
	static final int TYPE_SHORT = 3;
	static final int TYPE_LONG = 4;
	static final int TYPE_RATIONAL = 5;

	/* Different compression schemes */
	static final int COMPRESSION_NONE = 1;
	static final int COMPRESSION_CCITT_3_1 = 2;
	static final int COMPRESSION_LZW = 5; // bd
	static final int COMPRESSION_JPEG_ADOBE = 7; // bd
	static final int COMPRESSION_DEFLATE_ADOBE = 8; // bd
	static final int COMPRESSION_DEFLATE = 32946; // bd
	static final int COMPRESSION_PACKBITS = 32773;
	
	static final int METRIC_UNSUPPORTED           = -1; // bd
    static final int METRIC_BILEVEL_WHITE_IS_ZERO = 0; // bd
    static final int METRIC_BILEVEL_BLACK_IS_ZERO = 1; // bd
    static final int METRIC_RGB                   = 2; // bd
    static final int METRIC_PALETTE               = 3; // bd
    static final int METRIC_MASK                  = 4; // bd
    static final int METRIC_CMYK                  = 5; // bd
    static final int METRIC_YCBCR                 = 6; // bd
    static final int METRIC_CIELAB                = 7; // bd
    static final int METRIC_GENERIC               = 8; // bd

	static final int IFD_ENTRY_SIZE = 12;

	public TIFFDirectory(TIFFRandomFileAccess file, boolean isLittleEndian,
			ImageLoader loader) {
		this.file = file;
		this.isLittleEndian = isLittleEndian;
		this.loader = loader;
	}

	public TIFFDirectory(ImageData image) {
		this.image = image;
	}

	/* PackBits decoder */
	// tuned by bd
	int decodePackBits(byte[] src, byte[] dest, int offsetDest) {
		int destIndex = offsetDest;
		int srcIndex = 0;
		while (srcIndex < src.length) {
			byte n = src[srcIndex];
			if (0 <= n && n <= 127) {
				/* Copy next n+1 bytes literally */
				int length = n + 1;
				System.arraycopy(src, ++srcIndex, dest, destIndex, length);
				srcIndex += length;
				destIndex += length;
			} else if (-127 <= n && n <= -1) {
				/* Copy next byte -n+1 times */
				byte value = src[++srcIndex];
				int upper = -n + 1;
				for (int j = 0; j < upper; j++) {
					dest[destIndex++] = value;
				}
				srcIndex++;
			} else {
				/* Noop when n == -128 */
				srcIndex++;
			}
		}
		/* Number of bytes copied */
		return destIndex - offsetDest;
	}

	int getEntryValue(int type, byte[] buffer, int index) {
		return toInt(buffer, index + 8, type);
	}

	void getEntryValue(int type, byte[] buffer, int index, int[] values)
			throws IOException {
		int start = index + 8;
		int size;
		int offset = toInt(buffer, start, TYPE_LONG);
		switch (type) {
		case TYPE_SHORT:
			size = 2;
			break;
		case TYPE_LONG:
			size = 4;
			break;
		case TYPE_RATIONAL:
			size = 8;
			break;
		case TYPE_ASCII:
		case TYPE_BYTE:
			size = 1;
			break;
		default:
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			return;
		}
		if (values.length * size > 4) {
			buffer = new byte[values.length * size];
			file.seek(offset);
			file.read(buffer);
			start = 0;
		}
		for (int i = 0; i < values.length; i++) {
			values[i] = toInt(buffer, start + i * size, type);
		}
	}

	void decodePixels(ImageData image) throws IOException {
		/* Each row is byte aligned */
		// byte[] imageData = new byte[(imageWidth * depth + 7) / 8 *
		// imageLength]; //bd
		byte[] imageData = new byte[(imageWidth * sourceDepth + 7) / 8
				* imageLength]; // bd
		image.data = imageData;
		int destIndex = 0;
		int length = stripOffsets.length;
		int lastStrip = length - 1;
		for (int i = 0; i < length; i++) {
			// bd
			int nRows = rowsPerStrip;
			if (i == lastStrip) {
				int n = imageLength % nRows;
				if (n != 0)
					nRows = n;
			}
			// -bd
			/* Read a strip */
			byte[] data = new byte[stripByteCounts[i]];
			file.seek(stripOffsets[i]);
			file.read(data);
			/*
			 * bd if (compression == COMPRESSION_NONE) { System.arraycopy(data,
			 * 0, imageData, destIndex, data.length); destIndex += data.length;
			 * } else if (compression == COMPRESSION_PACKBITS) { destIndex +=
			 * decodePackBits(data, imageData, destIndex); } else if
			 * (compression == COMPRESSION_CCITT_3_1 || compression == 3) {
			 * TIFFModifiedHuffmanCodec codec = new TIFFModifiedHuffmanCodec();
			 * int nRows = rowsPerStrip; if (i == length - 1) { int n =
			 * imageLength % rowsPerStrip; if (n != 0) nRows = n; } destIndex +=
			 * codec.decode(data, imageData, destIndex, imageWidth, nRows); }
			 */
			switch (compression) {
			case COMPRESSION_NONE:
				destIndex = transferToImageData(imageData, destIndex, nRows,
						data);
				break;
			case COMPRESSION_PACKBITS:
				destIndex += decodePackBits(data, imageData, destIndex);
				break;
			case 3:
			case COMPRESSION_CCITT_3_1:
				TIFFModifiedHuffmanCodec codec = new TIFFModifiedHuffmanCodec();
				destIndex += codec.decode(data, imageData, destIndex,
						imageWidth, nRows);
				break;
			case COMPRESSION_JPEG_ADOBE:
				if (photometricInterpretation != METRIC_YCBCR)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
				break;
			case COMPRESSION_DEFLATE:
			case COMPRESSION_DEFLATE_ADOBE:
				byte uncompressed[] = new byte[(imageWidth * bitsPerSample[0]
						* samplesPerPixel + 7)
						/ 8 * nRows];
				inflate(data, uncompressed);
				destIndex = transferToImageData(imageData, destIndex, nRows,
						uncompressed);
				break;
			case COMPRESSION_LZW:
				TIFFLZWDecoder lzwcodec = new TIFFLZWDecoder(imageWidth,
						predictor, samplesPerPixel);
				uncompressed = new byte[(imageWidth * bitsPerSample[0]
						* samplesPerPixel + 7)
						/ 8 * nRows];
				lzwcodec.decode(data, uncompressed, nRows);
				destIndex = transferToImageData(imageData, destIndex, nRows,
						uncompressed);
				break;
			}
			// -bd
			if (loader.hasListeners()) {
				loader.notifyListeners(new ImageLoaderEvent(loader, image, i,
						i == length - 1));
			}
		}
		// bd
		if (sourceDepth == targetDepth)
			image.data = imageData;
		else {
			byte[] rgbData = new byte[(imageWidth * targetDepth + 7) / 8
					* imageLength];
			int j = 0;
			int i = 0;
			int l = imageData.length - 3;
			if (photometricInterpretation == 2)
				// RGBA
				while (i < l) {
					rgbData[j++] = imageData[i++]; // red
					rgbData[j++] = imageData[i++]; // green
					rgbData[j++] = imageData[i++]; // blue
					++i; // alpha
				}
			else if (photometricInterpretation == 6)
				// YCbCr
				while (i < l) {
					int y = imageData[i++] & 0xff;
					int cb = imageData[i++] & 0xff;
					int cr = imageData[i++] & 0xff;
					if (y >= 16 && cb >= 16 && cr >= 16 && y <= 235
							&& cb <= 240 && cr <= 240) {
						int r = (int) Math.round((y - 16) * 1.164 + (cr - 128)
								* 1.596);
						int g = (int) Math.round((y - 16) * 1.164 + (cb - 128)
								* -0.391 + (cr - 128) * -0.813);
						int b = (int) Math.round((y - 16) * 1.164 + (cb - 128)
								* 2.018);
						if (r < 0)
							r = 0;
						else if (r > 255)
							r = 255;
						if (g < 0)
							g = 0;
						else if (g > 255)
							g = 255;
						if (b < 0)
							b = 0;
						else if (b > 255)
							b = 255;

						rgbData[j++] = (byte) r; // red
						rgbData[j++] = (byte) g; // green
						rgbData[j++] = (byte) b; // blue
					} else {
						rgbData[j++] = (byte) 255; // red
						rgbData[j++] = (byte) 255; // green
						rgbData[j++] = (byte) 255; // blue
					}
					++i; // alpha
				}
			else
				// CMYK
				while (i < l) {
					int cyan = imageData[i++];
					int magenta = imageData[i++];
					int yellow = imageData[i++];
					int white = 255 - imageData[i++];
					rgbData[j++] = (byte) (((255 - cyan) * white) / 255); // red
					rgbData[j++] = (byte) (((255 - magenta) * white) / 255); // green
					rgbData[j++] = (byte) (((255 - yellow) * white) / 255); // blue
				}
			image.data = rgbData;
		}

		// -bd
	}

	// bd
	private int transferToImageData(byte[] imageData, int destIndex, int h,
			byte[] uncompressed) {
		if (is16Bit) {
			if (predictor == 2) {
				int bytesPerPixel = 2 * samplesPerPixel;
				int samplesPerLine = imageWidth * samplesPerPixel;
				for (int j = 0; j < h; j++) {
					int count = bytesPerPixel * (j * imageWidth + 1);
					switch (samplesPerPixel) {
					case 1:
						int v = toInt(uncompressed, count - 2, TYPE_SHORT);
						for (int i = 1; i < imageWidth; i++) {
							v += toInt(uncompressed, count, TYPE_SHORT);
							if (isLittleEndian) {
								uncompressed[count++] = (byte) v;
								uncompressed[count++] = (byte) (v >> 8);
							} else {
								uncompressed[count++] = (byte) (v >> 8);
								uncompressed[count++] = (byte) v;
							}
						}
						break;
					case 3:
						int r = toInt(uncompressed, count - 6, TYPE_SHORT);
						int g = toInt(uncompressed, count - 4, TYPE_SHORT);
						int b = toInt(uncompressed, count - 2, TYPE_SHORT);
						for (int i = 1; i < imageWidth; i++) {
							r += toInt(uncompressed, count, TYPE_SHORT);
							g += toInt(uncompressed, count + 2, TYPE_SHORT);
							b += toInt(uncompressed, count + 4, TYPE_SHORT);
							if (isLittleEndian) {
								uncompressed[count++] = (byte) r;
								uncompressed[count++] = (byte) (r >> 8);
								uncompressed[count++] = (byte) g;
								uncompressed[count++] = (byte) (g >> 8);
								uncompressed[count++] = (byte) b;
								uncompressed[count++] = (byte) (b >> 8);
							} else {
								uncompressed[count++] = (byte) (r >> 8);
								uncompressed[count++] = (byte) r;
								uncompressed[count++] = (byte) (g >> 8);
								uncompressed[count++] = (byte) g;
								uncompressed[count++] = (byte) (b >> 8);
								uncompressed[count++] = (byte) b;
							}
						}
						break;
					case 4:
						int c = toInt(uncompressed, count - 8, TYPE_SHORT);
						int m = toInt(uncompressed, count - 6, TYPE_SHORT);
						int y = toInt(uncompressed, count - 4, TYPE_SHORT);
						int k = toInt(uncompressed, count - 2, TYPE_SHORT);
						for (int i = 1; i < imageWidth; i++) {
							c += toInt(uncompressed, count, TYPE_SHORT);
							m += toInt(uncompressed, count + 2, TYPE_SHORT);
							y += toInt(uncompressed, count + 4, TYPE_SHORT);
							k += toInt(uncompressed, count + 6, TYPE_SHORT);
							if (isLittleEndian) {
								uncompressed[count++] = (byte) c;
								uncompressed[count++] = (byte) (c >> 8);
								uncompressed[count++] = (byte) m;
								uncompressed[count++] = (byte) (m >> 8);
								uncompressed[count++] = (byte) y;
								uncompressed[count++] = (byte) (y >> 8);
								uncompressed[count++] = (byte) k;
								uncompressed[count++] = (byte) (k >> 8);
							} else {
								uncompressed[count++] = (byte) (c >> 8);
								uncompressed[count++] = (byte) c;
								uncompressed[count++] = (byte) (m >> 8);
								uncompressed[count++] = (byte) m;
								uncompressed[count++] = (byte) (y >> 8);
								uncompressed[count++] = (byte) y;
								uncompressed[count++] = (byte) (k >> 8);
								uncompressed[count++] = (byte) k;
							}
						}
						break;
					default:
						for (int i = samplesPerPixel; i < samplesPerLine; i++) {
							int v1 = toInt(uncompressed, count, TYPE_SHORT);
							v1 += toInt(uncompressed, count - bytesPerPixel,
									TYPE_SHORT);
							if (isLittleEndian) {
								uncompressed[count++] = (byte) v1;
								uncompressed[count++] = (byte) (v1 >> 8);
							} else {
								uncompressed[count++] = (byte) (v1 >> 8);
								uncompressed[count++] = (byte) v1;
							}
						}
						break;
					}
				}
			}
			int l = uncompressed.length;
			int remaining = (imageData.length - destIndex) * 2;
			if (remaining < l)
				l = remaining;
			for (int j = (isLittleEndian) ? 1 : 0; j < l; j += 2)
				imageData[destIndex++] = uncompressed[j];
		} else {
			if (predictor == 2) {
				int samplesPerLine = imageWidth * samplesPerPixel;
				for (int j = 0; j < h; j++) {
					int count = samplesPerPixel * (j * imageWidth + 1);
					int count2 = count - samplesPerPixel;
					for (int k = samplesPerPixel; k < samplesPerLine; k++)
						uncompressed[count++] += uncompressed[count2++];
				}
			}
			int l = uncompressed.length;
			int remaining = imageData.length - destIndex;
			if (remaining < l)
				l = remaining;
			System.arraycopy(uncompressed, 0, imageData, destIndex, l);
			destIndex += uncompressed.length;
		}
		return destIndex;
	}

	public static void inflate(byte[] deflated, byte[] inflated)
			throws IOException {
		Inflater inflater = new Inflater();
		inflater.setInput(deflated);
		try {
			inflater.inflate(inflated);
		} catch (DataFormatException dfe) {
			throw new IOException(NLS.bind(
					Messages.TIFFDirectory_Error_inflating_TIFF, dfe));
		}
	}

	// -bd

	PaletteData getColorMap() throws IOException {
		int numColors = 1 << bitsPerSample[0];
		/* R, G, B entries are 16 bit wide (2 bytes) */
		int numBytes = 3 * 2 * numColors;
		byte[] buffer = new byte[numBytes];
		file.seek(colorMapOffset);
		file.read(buffer);
		RGB[] colors = new RGB[numColors];
		/**
		 * SWT does not support 16-bit depth color formats. Convert the 16-bit
		 * data to 8-bit data. The correct way to do this is to multiply each 16
		 * bit value by the value: (2^8 - 1) / (2^16 - 1). The fast way to do
		 * this is just to drop the low byte of the 16-bit value.
		 */
		int offset = isLittleEndian ? 1 : 0;
		int startG = 2 * numColors;
		int startB = startG + 2 * numColors;
		for (int i = 0; i < numColors; i++) {
			int r = buffer[offset] & 0xFF;
			int g = buffer[startG + offset] & 0xFF;
			int b = buffer[startB + offset] & 0xFF;
			colors[i] = new RGB(r, g, b);
			offset += 2;
		}
		return new PaletteData(colors);
	}

	// PaletteData getGrayPalette() { // bd
	// int numColors = 1 << bitsPerSample[0]; // bd
	PaletteData getGrayPalette(int bitsG) { // bd
		int numColors = 1 << bitsG; // bd
		RGB[] rgbs = new RGB[numColors];
		for (int i = 0; i < numColors; i++) {
			int value = i * 0xFF / (numColors - 1);
			if (photometricInterpretation == 0)
				value = 0xFF - value;
			rgbs[i] = new RGB(value, value, value);
		}
		return new PaletteData(rgbs);
	}

	PaletteData getRGBPalette(int bitsR, int bitsG, int bitsB) {
		int blueMask = 0;
		for (int i = 0; i < bitsB; i++) {
			blueMask |= 1 << i;
		}
		int greenMask = 0;
		for (int i = bitsB; i < bitsB + bitsG; i++) {
			greenMask |= 1 << i;
		}
		int redMask = 0;
		for (int i = bitsB + bitsG; i < bitsB + bitsG + bitsR; i++) {
			redMask |= 1 << i;
		}
		return new PaletteData(redMask, greenMask, blueMask);
	}

	int formatStrips(int rowByteSize, int nbrRows, byte[] data,
			int maxStripByteSize, int offsetPostIFD, int extraBytes,
			int[][] strips) {
		/*
		 * Calculate the nbr of required strips given the following
		 * requirements: - each strip should, if possible, not be greater than
		 * maxStripByteSize - each strip should contain 1 or more entire rows
		 * 
		 * Format the strip fields arrays so that the image data is stored in
		 * one contiguous block. This block is stored after the IFD and after
		 * any tag info described in the IFD.
		 */
		int n, nbrRowsPerStrip;
		if (rowByteSize > maxStripByteSize) {
			/* Each strip contains 1 row */
			n = data.length / rowByteSize;
			nbrRowsPerStrip = 1;
		} else {
			int nbr = (data.length + maxStripByteSize - 1) / maxStripByteSize;
			nbrRowsPerStrip = nbrRows / nbr;
			n = (nbrRows + nbrRowsPerStrip - 1) / nbrRowsPerStrip;
		}
		int stripByteSize = rowByteSize * nbrRowsPerStrip;

		int[] offsets = new int[n];
		int[] counts = new int[n];
		/*
		 * Nbr of bytes between the end of the IFD directory and the start of
		 * the image data. Keep space for at least the offsets and counts data,
		 * each field being TYPE_LONG (4 bytes). If other tags require space
		 * between the IFD and the image block, use the extraBytes parameter. If
		 * there is only one strip, the offsets and counts data is stored
		 * directly in the IFD and we need not reserve space for it.
		 */
		int postIFDData = n == 1 ? 0 : n * 2 * 4;
		int startOffset = offsetPostIFD + extraBytes + postIFDData; /*
																	 * offset of
																	 * image
																	 * data
																	 */

		int offset = startOffset;
		for (int i = 0; i < n; i++) {
			/*
			 * Store all strips sequentially to allow us to copy all pixels in
			 * one contiguous area.
			 */
			offsets[i] = offset;
			counts[i] = stripByteSize;
			offset += stripByteSize;
		}
		/* The last strip may contain fewer rows */
		int mod = data.length % stripByteSize;
		if (mod != 0)
			counts[counts.length - 1] = mod;

		strips[0] = offsets;
		strips[1] = counts;
		return nbrRowsPerStrip;
	}

	int[] formatColorMap(RGB[] rgbs) {
		/*
		 * In a TIFF ColorMap, all red come first, followed by green and blue.
		 * All values must be converted from 8 bit to 16 bit.
		 */
		int[] colorMap = new int[rgbs.length * 3];
		int offsetGreen = rgbs.length;
		int offsetBlue = rgbs.length * 2;
		for (int i = 0; i < rgbs.length; i++) {
			colorMap[i] = rgbs[i].red << 8 | rgbs[i].red;
			colorMap[i + offsetGreen] = rgbs[i].green << 8 | rgbs[i].green;
			colorMap[i + offsetBlue] = rgbs[i].blue << 8 | rgbs[i].blue;
		}
		return colorMap;
	}

	void parseEntries(byte[] buffer) throws IOException {
		for (int offset = 0; offset < buffer.length; offset += IFD_ENTRY_SIZE) {
			int tag = toInt(buffer, offset, TYPE_SHORT);
			int type = toInt(buffer, offset + 2, TYPE_SHORT);
			int count = toInt(buffer, offset + 4, TYPE_LONG);
			switch (tag) {
			case TAG_ImageWidth: {
				imageWidth = getEntryValue(type, buffer, offset);
				break;
			}
			case TAG_ImageLength: {
				imageLength = getEntryValue(type, buffer, offset);
				break;
			}
			case TAG_BitsPerSample: {
				if (type != TYPE_SHORT)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				bitsPerSample = new int[count];
				getEntryValue(type, buffer, offset, bitsPerSample);
				break;
			}
			case TAG_Compression: {
				compression = getEntryValue(type, buffer, offset);
				break;
			}
			case TAG_PhotometricInterpretation: {
				photometricInterpretation = getEntryValue(type, buffer, offset);
				break;
			}
			case TAG_StripOffsets: {
				if (type != TYPE_LONG && type != TYPE_SHORT)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				stripOffsets = new int[count];
				getEntryValue(type, buffer, offset, stripOffsets);
				break;
			}
			case TAG_SamplesPerPixel: {
				if (type != TYPE_SHORT)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				samplesPerPixel = getEntryValue(type, buffer, offset);
				/*
				 * Only the basic 1 and 3 values are supported // bd if
				 * (samplesPerPixel != 1 && samplesPerPixel != 3)
				 * SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
				 */
				/* Only the basic 1, 3 and 4 values are supported */
				if (samplesPerPixel != 1 && samplesPerPixel != 3
						&& samplesPerPixel != 4)
					SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
				// -bd
				break;
			}
			case TAG_RowsPerStrip: {
				rowsPerStrip = getEntryValue(type, buffer, offset);
				break;
			}
			case TAG_StripByteCounts: {
				stripByteCounts = new int[count];
				getEntryValue(type, buffer, offset, stripByteCounts);
				break;
			}
			case TAG_XResolution: {
				/* Ignored */
				break;
			}
			case TAG_YResolution: {
				/* Ignored */
				break;
			}
			case TAG_T4Options: {
				if (type != TYPE_LONG)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				t4Options = getEntryValue(type, buffer, offset);
				if ((t4Options & 0x1) == 1) {
					/* 2-dimensional coding is not supported */
					SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
				}
				break;
			}
			case TAG_ResolutionUnit: {
				/* Ignored */
				break;
			}
			case TAG_Predictor: { // bd
				predictor = getEntryValue(type, buffer, offset); // bd
				break;
			}

			case TAG_ColorMap: {
				if (type != TYPE_SHORT)
					SWT.error(SWT.ERROR_INVALID_IMAGE);
				/* Get the offset of the colorMap (use TYPE_LONG) */
				colorMapOffset = getEntryValue(TYPE_LONG, buffer, offset);
				break;
			}
			}
		}
	}

	public ImageData read() throws IOException {
		/* Set TIFF default values */
		bitsPerSample = new int[] { 1 };
		colorMapOffset = NO_VALUE;
		compression = 1;
		imageLength = NO_VALUE;
		imageWidth = NO_VALUE;
		photometricInterpretation = NO_VALUE;
		rowsPerStrip = Integer.MAX_VALUE;
		samplesPerPixel = 1;
		stripByteCounts = null;
		stripOffsets = null;

		byte[] buffer = new byte[2];
		file.read(buffer);
		int numberEntries = toInt(buffer, 0, TYPE_SHORT);
		buffer = new byte[IFD_ENTRY_SIZE * numberEntries];
		file.read(buffer);
		parseEntries(buffer);

		PaletteData palette = null;
		// depth = 0; // bd
		sourceDepth = targetDepth = 0; // bd
		switch (photometricInterpretation) {
		case METRIC_BILEVEL_WHITE_IS_ZERO:
		case METRIC_BILEVEL_BLACK_IS_ZERO: {
			/*
			 * bd depth = bitsPerSample[0];
			 */
			if (bitsPerSample[0] == 16) {
				sourceDepth = 8;
				palette = getGrayPalette(sourceDepth);
				is16Bit = true;
			} else {
				sourceDepth = bitsPerSample[0];
				palette = getGrayPalette(sourceDepth);
			}
			targetDepth = sourceDepth;
			// -bd
			break;
		}
		case METRIC_RGB: {
			/*
			 * // bd if (colorMapOffset != NO_VALUE)
			 * SWT.error(SWT.ERROR_INVALID_IMAGE);
			 */
			/* SamplesPerPixel 3 is the only value supported */
			/*
			 * palette = getRGBPalette(bitsPerSample[0], bitsPerSample[1],
			 * bitsPerSample[2]); depth = bitsPerSample[0] + bitsPerSample[1] +
			 * bitsPerSample[2];
			 */
			if (colorMapOffset != NO_VALUE)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			if (samplesPerPixel == 3) {
				if (bitsPerSample.length == 1)
					bitsPerSample = new int[] { bitsPerSample[0],
							bitsPerSample[0], bitsPerSample[0] };
				if (bitsPerSample[0] == 16 && bitsPerSample[1] == 16
						&& bitsPerSample[2] == 16) {
					palette = getRGBPalette(8, 8, 8);
					sourceDepth = 24;
					is16Bit = true;
				} else {
					palette = getRGBPalette(bitsPerSample[0], bitsPerSample[1],
							bitsPerSample[2]);
					sourceDepth = bitsPerSample[0] + bitsPerSample[1]
							+ bitsPerSample[2];
				}
				targetDepth = sourceDepth;
			} else if (samplesPerPixel == 4) {
				if (bitsPerSample.length == 1)
					bitsPerSample = new int[] { bitsPerSample[0],
							bitsPerSample[0], bitsPerSample[0],
							bitsPerSample[0] };
				if (bitsPerSample[0] == 16 && bitsPerSample[1] == 16
						&& bitsPerSample[2] == 16 && bitsPerSample[3] == 16) {
					palette = getRGBPalette(8, 8, 8);
					sourceDepth = 32;
					targetDepth = 24;
					is16Bit = true;
				} else {
					palette = getRGBPalette(bitsPerSample[0], bitsPerSample[1],
							bitsPerSample[2]);
					targetDepth = bitsPerSample[0] + bitsPerSample[1]
							+ bitsPerSample[2];
					sourceDepth = targetDepth + bitsPerSample[3];
				}
			}
			// -bd
			break;
		}
		case METRIC_PALETTE: {
			if (colorMapOffset == NO_VALUE)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			palette = getColorMap();
			// depth = bitsPerSample[0]; // bd
			targetDepth = sourceDepth = bitsPerSample[0]; // bd
			break;
		}
			// bd
		case METRIC_CMYK: {
			if (colorMapOffset != NO_VALUE)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			/* SamplesPerPixel 4 is the only value supported */
			if (bitsPerSample.length == 1)
				bitsPerSample = new int[] { bitsPerSample[0], bitsPerSample[0],
						bitsPerSample[0], bitsPerSample[0] };
			if (bitsPerSample[0] == 16 && bitsPerSample[1] == 16
					&& bitsPerSample[2] == 16 && bitsPerSample[3] == 16) {
				is16Bit = true;
				sourceDepth = 64;
			} else if (!(bitsPerSample[0] == 8 && bitsPerSample[1] == 8
					&& bitsPerSample[2] == 8 && bitsPerSample[3] == 8))
				SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
			palette = getRGBPalette(8, 8, 8);
			sourceDepth = 32;
			targetDepth = 24;
			break;
		}
			// -bd
		case METRIC_YCBCR: { // bd
			if (colorMapOffset != NO_VALUE)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			if (samplesPerPixel == 3) {
				if (bitsPerSample.length == 1)
					bitsPerSample = new int[] { bitsPerSample[0],
							bitsPerSample[0], bitsPerSample[0] };
				if (bitsPerSample[0] == 16 && bitsPerSample[1] == 16
						&& bitsPerSample[2] == 16) {
					palette = getRGBPalette(8, 8, 8);
					sourceDepth = 24;
					is16Bit = true;
				} else {
					palette = getRGBPalette(bitsPerSample[0], bitsPerSample[1],
							bitsPerSample[2]);
					sourceDepth = bitsPerSample[0] + bitsPerSample[1]
							+ bitsPerSample[2];
				}
				targetDepth = sourceDepth;
			}
			break;
		} // -bd
		default: {
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
		}

		// ImageData image = ImageData.internal_new(imageWidth, imageLength, //
		// bd
		// depth, palette, 1, null, 0, null, null, -1, -1, SWT.IMAGE_TIFF,
		// 0, 0, 0, 0);
		ImageData image = ImageData.internal_new(imageWidth,
				imageLength, // bd
				targetDepth, palette, 1, null, 0, null, null, -1, -1,
				SWT.IMAGE_TIFF, 0, 0, 0, 0);
		decodePixels(image);
		return image;
	}

	int toInt(byte[] buffer, int i, int type) {
		if (type == TYPE_LONG) {
			return isLittleEndian ? (buffer[i] & 0xFF)
					| ((buffer[i + 1] & 0xFF) << 8)
					| ((buffer[i + 2] & 0xFF) << 16)
					| ((buffer[i + 3] & 0xFF) << 24) : (buffer[i + 3] & 0xFF)
					| ((buffer[i + 2] & 0xFF) << 8)
					| ((buffer[i + 1] & 0xFF) << 16)
					| ((buffer[i] & 0xFF) << 24);
		}
		if (type == TYPE_SHORT) {
			return isLittleEndian ? (buffer[i] & 0xFF)
					| ((buffer[i + 1] & 0xFF) << 8) : (buffer[i + 1] & 0xFF)
					| ((buffer[i] & 0xFF) << 8);
		}
		/* Invalid type */
		SWT.error(SWT.ERROR_INVALID_IMAGE);
		return -1;
	}

	@SuppressWarnings("null")
	// bd
	void write(int photometricInterpretation) throws IOException {
		boolean isRGB = photometricInterpretation == 2;
		boolean isColorMap = photometricInterpretation == 3;
		boolean isBiLevel = photometricInterpretation == 0
				|| photometricInterpretation == 1;

		int imageWidth = image.width;
		int imageLength = image.height;
		int rowByteSize = image.bytesPerLine;

		int numberEntries = isBiLevel ? 9 : 11;
		int lengthDirectory = 2 + 12 * numberEntries + 4;
		/* Offset following the header and the directory */
		int nextOffset = 8 + lengthDirectory;

		/* Extra space used by XResolution and YResolution values */
		int extraBytes = 16;

		int[] colorMap = null;
		if (isColorMap) {
			PaletteData palette = image.palette;
			RGB[] rgbs = palette.getRGBs();
			colorMap = formatColorMap(rgbs);
			/*
			 * The number of entries of the Color Map must match the
			 * bitsPerSample field
			 */
			if (colorMap.length != 3 * 1 << image.depth)
				SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			/* Extra space used by ColorMap values */
			extraBytes += colorMap.length * 2;
		}
		if (isRGB) {
			/* Extra space used by BitsPerSample values */
			extraBytes += 6;
		}
		/* TIFF recommends storing the data in strips of no more than 8 Ko */
		byte[] data = image.data;
		int[][] strips = new int[2][];
		int nbrRowsPerStrip = formatStrips(rowByteSize, imageLength, data,
				8192, nextOffset, extraBytes, strips);
		int[] stripOffsets = strips[0];
		int[] stripByteCounts = strips[1];

		int bitsPerSampleOffset = NO_VALUE;
		if (isRGB) {
			bitsPerSampleOffset = nextOffset;
			nextOffset += 6;
		}
		int stripOffsetsOffset = NO_VALUE, stripByteCountsOffset = NO_VALUE;
		int xResolutionOffset, yResolutionOffset, colorMapOffset = NO_VALUE;
		int cnt = stripOffsets.length;
		if (cnt > 1) {
			stripOffsetsOffset = nextOffset;
			nextOffset += 4 * cnt;
			stripByteCountsOffset = nextOffset;
			nextOffset += 4 * cnt;
		}
		xResolutionOffset = nextOffset;
		nextOffset += 8;
		yResolutionOffset = nextOffset;
		nextOffset += 8;
		if (isColorMap) {
			colorMapOffset = nextOffset;
			nextOffset += colorMap.length * 2;
		}
		/* TIFF header */
		writeHeader();

		/* Image File Directory */
		out.writeShort(numberEntries);
		writeEntry(TAG_ImageWidth, TYPE_LONG, 1, imageWidth);
		writeEntry(TAG_ImageLength, TYPE_LONG, 1, imageLength);
		if (isColorMap)
			writeEntry(TAG_BitsPerSample, TYPE_SHORT, 1, image.depth);
		if (isRGB)
			writeEntry(TAG_BitsPerSample, TYPE_SHORT, 3, bitsPerSampleOffset);
		writeEntry(TAG_Compression, TYPE_SHORT, 1, COMPRESSION_NONE);
		writeEntry(TAG_PhotometricInterpretation, TYPE_SHORT, 1,
				photometricInterpretation);
		writeEntry(TAG_StripOffsets, TYPE_LONG, cnt,
				cnt > 1 ? stripOffsetsOffset : stripOffsets[0]);
		if (isRGB)
			writeEntry(TAG_SamplesPerPixel, TYPE_SHORT, 1, 3);
		writeEntry(TAG_RowsPerStrip, TYPE_LONG, 1, nbrRowsPerStrip);
		writeEntry(TAG_StripByteCounts, TYPE_LONG, cnt,
				cnt > 1 ? stripByteCountsOffset : stripByteCounts[0]);
		writeEntry(TAG_XResolution, TYPE_RATIONAL, 1, xResolutionOffset);
		writeEntry(TAG_YResolution, TYPE_RATIONAL, 1, yResolutionOffset);
		if (isColorMap)
			writeEntry(TAG_ColorMap, TYPE_SHORT, colorMap.length,
					colorMapOffset);
		/* Offset of next IFD (0 for last IFD) */
		out.writeInt(0);

		/* Values longer than 4 bytes Section */

		/* BitsPerSample 8,8,8 */
		if (isRGB)
			for (int i = 0; i < 3; i++)
				out.writeShort(8);
		if (cnt > 1) {
			for (int i = 0; i < cnt; i++)
				out.writeInt(stripOffsets[i]);
			for (int i = 0; i < cnt; i++)
				out.writeInt(stripByteCounts[i]);
		}
		/* XResolution and YResolution set to 300 dpi */
		for (int i = 0; i < 2; i++) {
			out.writeInt(300);
			out.writeInt(1);
		}
		/* ColorMap */
		if (isColorMap)
			for (int i = 0; i < colorMap.length; i++)
				out.writeShort(colorMap[i]);

		/* Image Data */
		out.write(data);
	}

	void writeEntry(short tag, int type, int count, int value)
			throws IOException {
		out.writeShort(tag);
		out.writeShort(type);
		out.writeInt(count);
		out.writeInt(value);
	}

	void writeHeader() throws IOException {
		/* little endian */
		out.write(0x49);
		out.write(0x49);

		/* TIFF identifier */
		out.writeShort(42);
		/*
		 * Offset of the first IFD is chosen to be 8. It is word aligned and
		 * immediately after this header.
		 */
		out.writeInt(8);
	}

	@SuppressWarnings("null")
	// bd
	void writeToStream(LEDataOutputStream byteStream) throws IOException {
		out = byteStream;
		int photometricInterpretation = -1;

		/* Scanline pad must be 1 */
		if (image.scanlinePad != 1)
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		switch (image.depth) {
		case 1: {
			/* Palette must be black and white or white and black */
			PaletteData palette = image.palette;
			RGB[] rgbs = palette.colors;
			if (palette.isDirect || rgbs == null || rgbs.length != 2)
				SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			RGB rgb0 = rgbs[0];
			RGB rgb1 = rgbs[1];
			if (!(rgb0.red == rgb0.green && rgb0.green == rgb0.blue
					&& rgb1.red == rgb1.green && rgb1.green == rgb1.blue && ((rgb0.red == 0x0 && rgb1.red == 0xFF) || (rgb0.red == 0xFF && rgb1.red == 0x0)))) {
				SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			}
			/* 0 means a color index of 0 is imaged as white */
			photometricInterpretation = image.palette.colors[0].red == 0xFF ? 0
					: 1;
			break;
		}
		case 4:
		case 8: {
			photometricInterpretation = 3;
			break;
		}
		case 24: {
			photometricInterpretation = 2;
			break;
		}
		default: {
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
		}
		}
		write(photometricInterpretation);
	}

}
