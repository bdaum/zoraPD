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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;

@SuppressWarnings("restriction")
public class JpegSegmentWriter extends JpegSegmentReader {

	private FileWatchManager fileWatchManager = CoreActivator.getDefault().getFileWatchManager();

	/**
	 * Creates a JpegSegmentWriter for a specific file.
	 *
	 * @param file
	 *            the Jpeg file to replace a segment
	 */
	public JpegSegmentWriter(File file) throws JpegException {
		super(file);
	}

	public boolean replaceSegment(byte segmentMarker, byte[] data, String opId) throws JpegException {
		return replaceSegment(segmentMarker, 0, data, opId);
	}

	public boolean replaceSegment(byte segmentMarker, int occurrence, byte[] data, String opId) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("ZoRaJPeGSegment_", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(tempFile));
					BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file))) {
				int offset = 0;
				// first two bytes should be jpeg magic number
				byte[] header = getValidJpegHeaderBytes(inStream);
				if (header == null)
					throw new JpegException(Messages.JpegSegmentReader_not_a_jpeg_file);
				outStream.write(header);
				offset += 2;
				do {
					// next byte is 0xFF
					byte segmentIdentifier = (byte) (inStream.read() & 0xFF);
					if ((segmentIdentifier & 0xFF) != 0xFF)
						throw new JpegException(NLS.bind(Messages.JpegSegmentReader_expected_jpeg_segment, offset,
								Integer.toHexString(segmentIdentifier & 0xFF)));
					outStream.write(segmentIdentifier);
					offset++;
					// next byte is <segment-marker>
					byte thisSegmentMarker = (byte) (inStream.read() & 0xFF);
					if ((thisSegmentMarker & 0xFF) == (SEGMENT_SOS & 0xFF))
						return false;
					offset++;
					outStream.write(thisSegmentMarker);
					byte[] segmentLengthBytes = new byte[2];
					inStream.read(segmentLengthBytes, 0, 2);
					offset += 2;
					int segmentLength = ((segmentLengthBytes[0] << 8) & 0xFF00) | (segmentLengthBytes[1] & 0xFF);
					// segment length includes size bytes, so subtract two
					segmentLength -= 2;
					if (segmentLength > inStream.available())
						throw new JpegException(Messages.JpegSegmentReader_size_exceeds);
					else if (segmentLength < 0)
						throw new JpegException(Messages.JpegSegmentReader_size_negative);
					byte[] segmentBytes = new byte[segmentLength];
					inStream.read(segmentBytes, 0, segmentLength);
					offset += segmentLength;
					if ((thisSegmentMarker & 0xFF) == (MARKER_EOI & 0xFF))
						return false;
					if (thisSegmentMarker == segmentMarker && occurrence-- == 0) {
						int newLength = data.length + 2;
						segmentLengthBytes[0] = (byte) (newLength >> 8);
						segmentLengthBytes[1] = (byte) newLength;
						outStream.write(segmentLengthBytes);
						outStream.write(data);
						byte[] bytes = new byte[inStream.available()];
						inStream.read(bytes);
						outStream.write(bytes);
						inStream.close();
						outStream.close();
						fileWatchManager.ignore(file, opId);
						if (!file.delete())
							return false;
						tempFile.renameTo(file);
						return true;
					}
					outStream.write(segmentLengthBytes);
					outStream.write(segmentBytes);
				} while (true);
			}
		} catch (IOException ioe) {
			throw new JpegException(NLS.bind(Messages.JpegSegmentReader_io_exception, file), ioe);
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}

	}

}