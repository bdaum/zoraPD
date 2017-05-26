package com.bdaum.zoom.lal.internal.ui.paint;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

public class ImageFigure extends Figure {
	private Image image;

	private int x, y, w, h;

	/**
	 * Displays an image These objects are defined by their two end-points.
	 * 
	 * @param image
	 *            the color for this object
	 * @param x
	 *            the virtual X coordinate
	 * @param y
	 *            the virtual Y coordinate
	 * @param w
	 *            width of target area
	 * @param h
	 *            height of target area
	 */
	public ImageFigure(Image image, int x, int y, int w, int h) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	
	@Override
	public void draw(FigureDrawContext fdc) {
		Rectangle bounds = image.getBounds();
		double f = Math.min(((float) w) / ((float) bounds.width), ((float) h)
				/ ((float) bounds.height));
		if (f == 1d) {
			int xoff = (w - bounds.width);
			fdc.gc.drawImage(image, x + xoff, y);
		} else {
			Rectangle target = fdc.toClientRectangle(x, y, x + w, y + h);
			int wi = (int) (bounds.width * f * fdc.xScale + 0.5d);
			int hi = (int) (bounds.height * f * fdc.yScale + 0.5d);
			int xoff = (target.width - wi) / 2;
			int yoff = (target.height - hi) / 2;
			fdc.gc.drawImage(image, 0, 0, bounds.width, bounds.height, target.x
					+ xoff, target.y + yoff, target.width, target.height);
		}
	}
	
	
	@Override
	public void addDamagedRegion(FigureDrawContext fdc, Region region) {
		region.add(fdc.toClientRectangle(x, y, x+w, y+h));
	}

}