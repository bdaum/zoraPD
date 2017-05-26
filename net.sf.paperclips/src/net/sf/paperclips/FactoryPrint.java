/*
 * Copyright (c) 2005 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Hall - initial API and implementation
 */
package net.sf.paperclips;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An ill-conceived class I wish I could take back.
 * 
 * @author Matthew
 * @deprecated This class will be removed in a future release.
 */
public abstract class FactoryPrint implements Print {
	private Print print;

	/**
	 * Default constructor.
	 */
	public FactoryPrint() {
	}

	/**
	 * Returns the Print created by this factory.
	 * 
	 * @return the Print created by this factory.
	 */
	public Print getPrint() {
		if (print == null)
			print = createPrint();
		return print;
	}

	/**
	 * Returns a PrintIterator for the Print returned from a call to
	 * createPrint().
	 */
	public PrintIterator iterator(Device device, GC gc) {
		return getPrint().iterator(device, gc);
	}

	/**
	 * Compose and return a Print appropriate for the concrete class. Subclasses
	 * must override this method to create the Print.
	 * 
	 * @return a Print appropriate for the concrete class.
	 */
	protected abstract Print createPrint();

	/**
	 * Converts the argument to a String. This method is used by the TextPrint
	 * factory methods which accept an Object.
	 * 
	 * @param obj
	 *            the object to convert.
	 * @return This implementation returns the result of obj.toString(), or an
	 *         empty string if the argument is null. Override this method to
	 *         change this behavior.
	 */
	protected String convertToString(Object obj) {
		return (obj == null) ? "" : obj.toString(); //$NON-NLS-1$
	}

	/**
	 * Returns the default text font. All TextPrints returned from the text(...)
	 * methods are set to this font. This implementation uses
	 * TextPrint.DEFAULT_FONT_DATA as the default text font. Override this
	 * method to change the default text font.
	 * 
	 * @return the default text font.
	 */
	protected FontData getDefaultTextFont() {
		return TextPrint.DEFAULT_FONT_DATA;
	}

	/**
	 * Returns the default label font. All TextPrints returned from the
	 * label(...) methods are set to this font. This implementation uses
	 * TextPrint.DEFAULT_FONT_DATA as the default label font. Override this
	 * method to change the default label font.
	 * 
	 * @return the default label font.
	 */
	protected FontData getDefaultLabelFont() {
		return TextPrint.DEFAULT_FONT_DATA;
	}

	/**
	 * Returns the default spacing, in points, for GridPrints. All GridPrints
	 * returned from the grid(String) method(s) have their vertical and
	 * horizontal spacing set to this value. This implementation returns 0 as
	 * the default spacing. Override this method to change the default grid
	 * spacing.
	 * 
	 * @return the default spacing, in points, between GridPrint cells.
	 */
	protected int getDefaultGridSpacing() {
		return 2;
	}

	/**
	 * Returns a TextPrint whose text represents the given parameter. The
	 * returned object will be set to the default text font.
	 * 
	 * @param obj
	 *            the object that the returned TextPrint will represent.
	 * @return a TextPrint whose text represents the given parameter.
	 * @see #convertToString(Object)
	 * @see #getDefaultTextFont()
	 */
	protected TextPrint text(Object obj) {
		return text(convertToString(obj));
	}

	/**
	 * Returns a TextPrint whose text represents the given parameter. The
	 * returned object will be set to the default text font.
	 * 
	 * @param obj
	 *            the object that the returned TextPrint will represent.
	 * @param align
	 *            the alignment property for the returned TextPrint.
	 * @return a TextPrint whose text represents the given parameter.
	 * @see #convertToString(Object)
	 * @see #getDefaultTextFont()
	 */
	protected TextPrint text(Object obj, int align) {
		return text(convertToString(obj), align);
	}

	/**
	 * Returns a TextPrint with the given text. The returned object will be set
	 * to the default text font.
	 * 
	 * @param text
	 *            the text property for the returned TextPrint
	 * @return a TextPrint with the given text.
	 * @see #getDefaultTextFont()
	 */
	protected TextPrint text(String text) {
		return text(text, SWT.DEFAULT);
	}

	/**
	 * Returns a TextPrint with the given text and alignment. The returned
	 * object will be set to the default text font.
	 * 
	 * @param text
	 *            the text property for the returned TextPrint
	 * @param align
	 *            the alignment property for the returned TextPrint.
	 * @return a TextPrint with the given text and alignment.
	 * @see #convertToString(Object)
	 * @see #getDefaultTextFont()
	 */
	protected TextPrint text(String text, int align) {
		return new TextPrint(text, getDefaultTextFont(), align);
	}

	/**
	 * Returns a TextPrint with the given text. The returned object will be set
	 * to the default label font.
	 * 
	 * @param text
	 *            the text property for the returned TextPrint
	 * @return a TextPrint with the given text.
	 * @see #convertToString(Object)
	 * @see #getDefaultLabelFont()
	 */
	protected TextPrint label(String text) {
		return label(text, SWT.DEFAULT);
	}

	/**
	 * Returns a TextPrint with the given text and alignment. The returned
	 * object will be set to the default label font.
	 * 
	 * @param text
	 *            the text property for the returned TextPrint
	 * @param align
	 *            the alignment property for the returned TextPrint.
	 * @return a TextPrint with the given text and alignment.
	 * @see #convertToString(Object)
	 * @see #getDefaultLabelFont()
	 */
	protected TextPrint label(String text, int align) {
		return new TextPrint(text, getDefaultLabelFont(), align);
	}

	/**
	 * Returns the default image DPI. All ImagePrints returned from the
	 * image(...) methods are set to this DPI. This implementation uses (300,
	 * 300) as the default DPI. Override this method to change the default image
	 * DPI.
	 * 
	 * @return the default image DPI.
	 */
	protected Point getDefaultImageDPI() {
		return new Point(300, 300);
	}

	/**
	 * Creates and returns an ImageData using the given filename. All
	 * ImagePrints returned from the image(...) methods are set to this DPI.
	 * This implementation uses the ImageData(String filename) constructor to
	 * generate the ImageData. Override this method to change this behavior.
	 * 
	 * @param filename
	 *            the filename of the image to load.
	 * @return an ImageData containing the image from the given filename.
	 */
	protected ImageData getImageData(String filename) {
		return new ImageData(filename);
	}

	/**
	 * Returns an ImagePrint with the given image, and the default DPI.
	 * 
	 * @param filename
	 *            the filename of the image to load.
	 * @return an ImagePrint with the given image.
	 * @see #getDefaultImageDPI()
	 */
	protected ImagePrint image(String filename) {
		return image(filename, getDefaultImageDPI());
	}

	/**
	 * Returns an ImagePrint with the given image and DPI.
	 * 
	 * @param filename
	 *            the filename of the image to load.
	 * @param dpi
	 *            the DPI at which the image is to be printed.
	 * @return an ImagePrint with the given image and DPI.
	 */
	protected ImagePrint image(String filename, Point dpi) {
		return new ImagePrint(getImageData(filename), dpi);
	}

	/**
	 * Returns an ImagePrint with the given image, and the default DPI.
	 * 
	 * @param is
	 *            an input stream the image will be loaded from.
	 * @return an ImagePrint with the given image, and the default DPI.
	 */
	protected ImagePrint image(InputStream is) {
		return image(is, getDefaultImageDPI());
	}

	/**
	 * Returns an ImagePrint with the given image and DPI.
	 * 
	 * @param is
	 *            an InputStream which the image will be loaded from.
	 * @param dpi
	 *            the DPI the image will be printed at.
	 * @return an ImagePrint with the given image and DPI.
	 */
	protected ImagePrint image(InputStream is, Point dpi) {
		return new ImagePrint(new ImageData(is), dpi);
	}

	/**
	 * Returns an ImagePrint with the given image data, using the default DPI.
	 * 
	 * @param imageData
	 *            the ImageData of the image to print.
	 * @return an ImagePrint with the given image data, using the default DPI.
	 */
	protected ImagePrint image(ImageData imageData) {
		return image(imageData, getDefaultImageDPI());
	}

	/**
	 * Returns an ImagePrint with the given image data and DPI.
	 * 
	 * @param imageData
	 *            the ImageData of the image to print.
	 * @param dpi
	 *            the DPI the image will be printed at.
	 * @return an ImagePrint with the given image data and DPI.
	 */
	protected ImagePrint image(ImageData imageData, Point dpi) {
		return new ImagePrint(imageData, dpi);
	}

	/**
	 * Returns a GridPrint with columns using the given argument. The returned
	 * object will have the horizontal and vertical cell spacing set to the
	 * default spacing.
	 * 
	 * @param columns
	 *            comma-separated list of column specs.
	 * @return a GridPrint with columns using the given argument.
	 * @see GridColumn#parse(String)
	 * @see #getDefaultGridSpacing()
	 */
	protected GridPrint grid(String columns) {
		return grid(columns, getDefaultGridSpacing());
	}

	/**
	 * Returns a GridPrint with the given columns and spacing. The returned
	 * object will have the horizontal and vertical cell spacing set to the
	 * default spacing.
	 * 
	 * @param columns
	 *            comma-separated list of column specs.
	 * @param spacing
	 *            the spacing, in points, between cells in he GridPrint.
	 * @return a GridPrint with the specified column configuration and spacing.
	 * @see GridColumn#parse(String)
	 */
	protected GridPrint grid(String columns, int spacing) {
		return new GridPrint(columns, new DefaultGridLook(spacing, spacing));
	}

	/**
	 * Constructs and returns a new LayerPrint.
	 * 
	 * @return a new LayerPrint.
	 */
	protected LayerPrint layer() {
		return new LayerPrint();
	}

	/**
	 * Constructs and returns an EmptyPrint of size (0, 0).
	 * 
	 * @return a new EmptyPrint of size (0, 0).
	 */
	protected EmptyPrint empty() {
		return empty(0, 0);
	}

	/**
	 * Constructs and returns an EmptyPrint with the given size.
	 * 
	 * @param width
	 *            the width, in points.
	 * @param height
	 *            the height, in points.
	 * @return a new EmptyPrint with the given size.
	 */
	protected EmptyPrint empty(int width, int height) {
		return new EmptyPrint(width, height);
	}
}
