/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2010 Berthold Daum  (berthold.daum@bdaum.de)
 *
 * In parts based on Jarek Sacha's JAI-Reader for ImageJ
 */
package com.bdaum.zoom.jai;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.logging.InvalidFileFormatException;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

/**
 * Read image files using JAI image I/O codec
 * (http://developer.java.sun.com/developer/sampsource/jai/) and convert them to
 * Image/J representation.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.4 $
 *
 * @author Berthold Daum - Switched to JAI 1.1.4
 */
public class JAIReader {

	private ImageDecoder decoder = null;
	private String decoderName = null;
	private File file = null;
	private FileSeekableStream fss;

	/**
	 * Open image in the file using registered codecs. A file may contain
	 * multiple images. If all images in the file are of the same type and size
	 * they will be combines into single stack within ImagesPlus object returned
	 * as the first an only element of the image array. If reading from TIFF
	 * files, image resolution and Image/J's description string containing
	 * calibration information are decoded.
	 *
	 * @param file
	 *            File to open image from.
	 * @return First image contained in the file
	 * @throws Exception
	 *             when unable to read image from the specified file.
	 */
	public static BufferedImage read(File file) throws Exception {
		JAIReader reader = new JAIReader();
		reader.open(file);
		BufferedImage image = reader.read();
		reader.close();
		reader = null;
		return image;
	}

	/**
	 * Create image decoder to read the image file.
	 *
	 * @param file
	 *            Image file name.
	 * @throws Exception
	 *             Description of Exception
	 */
	private void open(File file) throws Exception {
		this.file = file;
		fss = new FileSeekableStream(file);
		String[] decoders;
		try {
			decoders = ImageCodec.getDecoderNames(fss);
			if (decoders == null || decoders.length == 0)
				throw new InvalidFileFormatException(NLS.bind(
						Messages.JAIReader_unsupported_file_format, file), null, file.toURI());
		} catch (Exception e) {
			throw new InvalidFileFormatException(NLS.bind(
					Messages.JAIReader_corrupt_file, file), e,
					file.toURI());
		}
		decoderName = decoders[0];
		decoder = ImageCodec.createImageDecoder(decoderName, fss, null);
		if (decoder.getNumPages() < 1)
			throw new InvalidFileFormatException(NLS.bind(
					Messages.JAIReader_image_file_has_no_pages, file), null,
					file.toURI());
	}

	/**
	 * @return image read
	 * @throws Exception
	 *             Description of Exception
	 */
	private BufferedImage read() throws Exception {
		RenderedImage ri = null;
		try {
			ri = decoder.decodeAsRenderedImage(0);
		} catch (Exception ex) {
			// ex.printStackTrace();
			String msg = ex.getMessage();
			if (msg == null || msg.trim().length() < 1)
				msg = NLS.bind(Messages.JAIReader_error_decoding_image, file);
			throw new InvalidFileFormatException(msg, ex, file.toURI());
		}
		WritableRaster wr = forceTileUpdate(ri);
		ColorModel colorModel = ri.getColorModel();
		if (decoderName.equalsIgnoreCase("GIF") //$NON-NLS-1$
				|| decoderName.equalsIgnoreCase("JPEG")) { //$NON-NLS-1$
			if (colorModel.getNumComponents() == 4)
				throw new InvalidFileFormatException(NLS.bind(
						Messages.JAIReader_unsupported_color_space, file),
						null, file.toURI());
		}
		return new BufferedImage(colorModel, wr, false, null);
	}

	/**
	 * Force Rendered image to set all the tails that it may have. In multi-tile
	 * images not all tiles may be updated when a RenderedImage is created.
	 *
	 * @param ri
	 *            image that may need tile update.
	 * @return WritableRaster with all tiles updated.
	 */
	public static WritableRaster forceTileUpdate(RenderedImage ri) {
		Raster r = ri.getData();
		if (!(r instanceof WritableRaster))
			r = Raster.createWritableRaster(r.getSampleModel(),
					r.getDataBuffer(), null);
		WritableRaster wr = (WritableRaster) r;
		int xTiles = ri.getNumXTiles();
		int yTiles = ri.getNumYTiles();
		for (int ty = 0; ty < yTiles; ++ty)
			for (int tx = 0; tx < xTiles; ++tx)
				wr.setRect(ri.getTile(tx, ty));
		return wr;
	}

	private void close() {
		if (fss != null) {
			try {
				fss.close();
			} catch (IOException e) {
				// ignore
			}
			fss = null;
		}
		decoder = null;
		decoderName = null;
		file = null;
	}
}
