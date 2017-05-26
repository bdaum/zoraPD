/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.lightzone.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.osgi.util.NLS;

public class JpegSegmentReader {

	protected final File file;

	public static final byte SEGMENT_SOS = (byte) 0xDA;

	public static final byte MARKER_EOI = (byte) 0xD9;

	/** APP0 Jpeg segment identifier -- Jfif data. */
	public static final byte SEGMENT_APP0 = (byte) 0xE0;
	/** APP1 Jpeg segment identifier -- where Exif data is kept. */
	public static final byte SEGMENT_APP1 = (byte) 0xE1;
	/** APP2 Jpeg segment identifier. */
	public static final byte SEGMENT_APP2 = (byte) 0xE2;
	/** APP3 Jpeg segment identifier. */
	public static final byte SEGMENT_APP3 = (byte) 0xE3;
	/** APP4 Jpeg segment identifier. */
	public static final byte SEGMENT_APP4 = (byte) 0xE4;
	/** APP5 Jpeg segment identifier. */
	public static final byte SEGMENT_APP5 = (byte) 0xE5;
	/** APP6 Jpeg segment identifier. */
	public static final byte SEGMENT_APP6 = (byte) 0xE6;
	/** APP7 Jpeg segment identifier. */
	public static final byte SEGMENT_APP7 = (byte) 0xE7;
	/** APP8 Jpeg segment identifier. */
	public static final byte SEGMENT_APP8 = (byte) 0xE8;
	/** APP9 Jpeg segment identifier. */
	public static final byte SEGMENT_APP9 = (byte) 0xE9;
	/** APPA Jpeg segment identifier -- can hold Unicode comments. */
	public static final byte SEGMENT_APPA = (byte) 0xEA;
	/** APPB Jpeg segment identifier. */
	public static final byte SEGMENT_APPB = (byte) 0xEB;
	/** APPC Jpeg segment identifier. */
	public static final byte SEGMENT_APPC = (byte) 0xEC;
	/** APPD Jpeg segment identifier -- IPTC data in here. */
	public static final byte SEGMENT_APPD = (byte) 0xED;
	/** APPE Jpeg segment identifier. */
	public static final byte SEGMENT_APPE = (byte) 0xEE;
	/** APPF Jpeg segment identifier. */
	public static final byte SEGMENT_APPF = (byte) 0xEF;
	/** Start Of Image segment identifier. */
	public static final byte SEGMENT_SOI = (byte) 0xD8;
	/** Define Quantization Table segment identifier. */
	public static final byte SEGMENT_DQT = (byte) 0xDB;
	/** Define Huffman Table segment identifier. */
	public static final byte SEGMENT_DHT = (byte) 0xC4;
	/** Start-of-Frame Zero segment identifier. */
	public static final byte SEGMENT_SOF0 = (byte) 0xC0;
	/** Jpeg comment segment identifier. */
	public static final byte SEGMENT_COM = (byte) 0xFE;

	/**
	 * Creates a JpegSegmentReader for a specific file.
	 *
	 * @param file
	 *            the Jpeg file to read segments from
	 */
	public JpegSegmentReader(File file) throws JpegException {
		this.file = file;
	}

	/**
	 * Reads the first instance of a given Jpeg segment, returning the contents
	 * as a byte array.
	 *
	 * @param segmentMarker
	 *            the byte identifier for the desired segment
	 * @return the byte array if found, else null
	 * @throws JpegException
	 *             for any problems processing the Jpeg data, including inner
	 *             IOExceptions
	 */
	public byte[] readSegment(byte segmentMarker) throws JpegException {
		return readSegment(segmentMarker, 0);
	}

	/**
	 * Reads the first instance of a given Jpeg segment, returning the contents
	 * as a byte array.
	 *
	 * @param segmentMarker
	 *            the byte identifier for the desired segment
	 * @param occurrence
	 *            the occurrence of the specified segment within the jpeg file
	 * @return the byte array if found, else null
	 */
	public byte[] readSegment(byte segmentMarker, int occurrence) {
		BufferedInputStream inStream = null;
		try {
			inStream = new BufferedInputStream(new FileInputStream(file));
			int offset = 0;
			// first two bytes should be jpeg magic number
			if (getValidJpegHeaderBytes(inStream) == null)
				throw new JpegException(
						Messages.JpegSegmentReader_not_a_jpeg_file);
			offset += 2;
			do {
				// next byte is 0xFF
				byte segmentIdentifier = (byte) (inStream.read() & 0xFF);
				if ((segmentIdentifier & 0xFF) != 0xFF)
					throw new JpegException(NLS.bind(
							Messages.JpegSegmentReader_expected_jpeg_segment,
							offset,
							Integer.toHexString(segmentIdentifier & 0xFF)));
				offset++;
				// next byte is <segment-marker>
				byte thisSegmentMarker = (byte) (inStream.read() & 0xFF);
				// System.out.println(thisSegmentMarker);
				offset++;
				if ((thisSegmentMarker & 0xFF) == (SEGMENT_SOS & 0xFF))
					return null;
				byte[] segmentLengthBytes = new byte[2];
				inStream.read(segmentLengthBytes, 0, 2);
				offset += 2;
				int segmentLength = ((segmentLengthBytes[0] << 8) & 0xFF00)
						| (segmentLengthBytes[1] & 0xFF);
				// segment length includes size bytes, so subtract two
				segmentLength -= 2;
				if (segmentLength > inStream.available())
					throw new JpegException(
							Messages.JpegSegmentReader_size_exceeds);
				else if (segmentLength < 0)
					throw new JpegException(
							Messages.JpegSegmentReader_size_negative);
				byte[] segmentBytes = new byte[segmentLength];
				inStream.read(segmentBytes, 0, segmentLength);
				offset += segmentLength;
				if ((thisSegmentMarker & 0xFF) == (MARKER_EOI & 0xFF))
					return null;
				if (thisSegmentMarker == segmentMarker && occurrence-- == 0)
					return segmentBytes;
			} while (true);
		} catch (IOException ioe) {
			throw new JpegException(NLS.bind(
					Messages.JpegSegmentReader_io_exception, file), ioe);
		} finally {
			try {
				if (inStream != null)
					inStream.close();
			} catch (IOException ioe) {
				// ignore
			}
		}

	}

	protected byte[] getValidJpegHeaderBytes(InputStream fileStream)
			throws IOException {
		byte[] header = new byte[2];
		fileStream.read(header, 0, 2);
		return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 ? header
				: null;
	}

}