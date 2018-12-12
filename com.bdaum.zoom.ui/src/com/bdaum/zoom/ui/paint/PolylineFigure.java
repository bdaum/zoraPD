/*******************************************************************************
 * Copyright (c) 2018 Berthold Daum
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.ui.paint;

import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

/**
 * 2D Polyline object
 */
public class PolylineFigure extends VectorFigure {
	private Color color;
	private double[] points;
	private double lineWidth;
	private int lineStyle;
	private PaintExample paintExample;
	private java.awt.geom.Rectangle2D.Double minmax;

	/**
	 * Constructs a Polyline. These objects are defined by a sequence of vertices.
	 * 
	 * @param color
	 *            the color for this object
	 * @param lineWidth
	 *            the linewidth relative to the document size (0...1)
	 * @param lineStyle
	 *            the SWT line style
	 * @param points
	 *            the array of x,y-vertices making up the polygon relative to the document size (0...1)
	 * @param paintExample
	 */
	public PolylineFigure(Color color, double lineWidth, int lineStyle, double[] points, PaintExample paintExample) {
		this.color = color;
		this.lineWidth = lineWidth;
		this.lineStyle = lineStyle;
		this.paintExample = paintExample;
		this.points = points;
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < points.length; i += 2) {
			double x = points[i];
			double y = points[i + 1];
			if (x < minX)
				minX = x;
			if (y < minY)
				minY = y;
			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;
		}
		minmax = new Rectangle2D.Double(minX - 2 * lineWidth, minY - 2 * lineWidth, maxX - minX + 4 * lineWidth,
				maxY - minY + 4 * lineWidth);
	}

	@Override
	public void draw(FigureDrawContext fdc) {
		ImageFigure imageFigure = paintExample.getImageFigure();
		Rectangle imageBounds = imageFigure != null ? imageFigure.getImageBounds()
				: paintExample.paintCanvas.getClientArea();
		int[] drawPoints = new int[points.length];
		for (int i = 0; i < drawPoints.length; i += 2) {
			int x = (int) (points[i] * imageBounds.width + imageBounds.x);
			int y = (int) (points[i + 1] * imageBounds.height + imageBounds.y);
			drawPoints[i] = x * fdc.xScale - fdc.xOffset;
			drawPoints[i + 1] = y * fdc.yScale - fdc.yOffset;
		}
		fdc.gc.setForeground(color);
		fdc.gc.setLineStyle(lineStyle);
		fdc.gc.setLineWidth((int) (lineWidth * imageBounds.width * fdc.xScale + 0.5d));
		fdc.gc.setLineCap(SWT.CAP_ROUND);
		fdc.gc.drawPolyline(drawPoints);
	}

	@Override
	public void addDamagedRegion(FigureDrawContext fdc, Region region) {
		int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE, ymax = Integer.MIN_VALUE;
		ImageFigure imageFigure = paintExample.getImageFigure();
		Rectangle imageBounds = imageFigure != null ? imageFigure.getImageBounds()
				: paintExample.paintCanvas.getClientArea();
		for (int i = 0; i < points.length; i += 2) {
			int x = (int) (points[i] * imageBounds.width + imageBounds.x);
			int y = (int) (points[i + 1] * imageBounds.height + imageBounds.y);
			if (points[i] < xmin)
				xmin = x;
			if (points[i] > xmax)
				xmax = x;
			if (points[i + 1] < ymin)
				ymin = y;
			if (points[i + 1] > ymax)
				ymax = y;
		}
		region.add(fdc.toClientRectangle(xmin, ymin, xmax, ymax));
	}

	@Override
	public boolean contains(double x, double y) {
		if (minmax.contains(x, y))
			for (int i = 2; i < points.length; i += 2)
				if (computeDistance(x, y, points[i - 2], points[i - 1], points[i], points[i + 1]) <= 2 * lineWidth)
					return true;
		return false;
	}

	private static double computeDistance(double x0, double y0, double ax, double ay, double bx, double by) {
		double d = Math.abs((by - ay) * x0 - (bx - ax) * y0 + bx * ay - by * ax);
		double v = Math.sqrt((by - ay) * (by - ay) + (bx - ax) * (bx - ax));
		return d / v;
	}

	public Color getColor() {
		return color;
	}

	public double[] getPoints() {
		return points;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public int getLineStyle() {
		return lineStyle;
	}

}
