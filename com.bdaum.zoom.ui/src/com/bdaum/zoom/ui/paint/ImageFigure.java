package com.bdaum.zoom.ui.paint;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Canvas;

public class ImageFigure extends Figure {
	private Image image;

	private int x, y, w, h;

	private Rectangle imageBounds = new Rectangle(0, 0, 1, 1);

	private Canvas paintCanvas;

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

	public ImageFigure(Image image, Canvas paintCanvas) {
		this.image = image;
		this.paintCanvas = paintCanvas;
	}

	@Override
	public void draw(FigureDrawContext fdc) {
		Rectangle bounds = image.getBounds();
		if (paintCanvas != null) {
			Rectangle clientArea = paintCanvas.getClientArea();
			x = clientArea.x;
			y = clientArea.y;
			w = clientArea.width;
			h = clientArea.height;
		}
		if (w > 0 && h > 0) {
			double f = Math.min((double) w / bounds.width, (double) h / bounds.height);
			if (f == 1d && fdc.xScale == 1 && fdc.yScale == 1 && fdc.xOffset == 0 && fdc.yOffset == 0) {
				int xoff = (w - bounds.width);
				int yoff = (h - bounds.height);
				fdc.gc.drawImage(image, x + xoff, y + yoff);
				imageBounds.x = x + xoff;
				imageBounds.y = y + yoff;
				imageBounds.width = bounds.width;
				imageBounds.height = bounds.height;
			} else if (f > 0) {
				int wi = (int) (bounds.width * f + 0.5d);
				int hi = (int) (bounds.height * f + 0.5d);
				int xi = (w - wi) / 2;
				int yi = (h - hi) / 2;
				Rectangle target = fdc.toClientRectangle(xi, yi, xi + wi, yi + hi);
				fdc.gc.drawImage(image, 0, 0, bounds.width, bounds.height, target.x, target.y, target.width,
						target.height);
				imageBounds.x = xi;
				imageBounds.y = yi;
				imageBounds.width = wi;
				imageBounds.height = hi;
			}
		}
	}

	@Override
	public void addDamagedRegion(FigureDrawContext fdc, Region region) {
		region.add(fdc.toClientRectangle(x, y, x + w, y + h));
	}
	
	public Rectangle getImageBounds() {
		return imageBounds;
	}

}