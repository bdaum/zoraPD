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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A pen tool.
 */
public class DrawingPenTool extends ContinuousPaintSession implements PaintTool {
	private static final double EPSILON = 1.5d;
	private ToolSettings settings;
	private int[] recordedPoints = new int[100];
	private int recCount = 0;
	private double[] vertices = new double[100];
	private int verCount = 0;
	private Rectangle imageBounds = null;

	/**
	 * Constructs a pen tool.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample
	 */
	public DrawingPenTool(ToolSettings toolSettings, PaintExample paintExample) {
		super(paintExample);
		vectored = true;
		set(toolSettings);
	}

	/**
	 * Sets the tool's settings.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 */

	public void set(ToolSettings toolSettings) {
		settings = toolSettings;
	}

	/**
	 * Returns the name associated with this tool.
	 * 
	 * @return the localized name of this tool
	 */

	public String getDisplayName() {
		return PaintExample.getResourceString("tool.DrawingPen.label"); //$NON-NLS-1$
	}

	/*
	 * Template method for drawing
	 */

	@Override
	public void render(final Point point) {
		final PaintSurface ps = getPaintSurface();
		int pencilRadius = Math.min(50, Math.max(1, settings.pencilRadius));
		int opacity = settings.pencilOpacity;
		if (opacity < 255)
			opacity = (int) (opacity / (double) pencilRadius);
		if (pencilRadius <= 1)
			ps.drawFigure(new PointFigure(settings.commonForegroundColor, opacity, point.x, point.y));
		else {
			int r = pencilRadius / 2;
			ps.drawFigure(new SolidEllipseFigure(settings.commonForegroundColor, opacity, point.x - r,
					point.y - r, point.x + r, point.y + r));
		}
		record(point.x, point.y);
		paintExample.setDirty(true);
	}

	private void record(int x, int y) {
		if (verCount == 0)
			addVertex(x, y);
		if (recCount * 2 + 1 >= recordedPoints.length) {
			int[] newPoints = new int[recordedPoints.length * 2];
			System.arraycopy(recordedPoints, 0, newPoints, 0, recordedPoints.length);
			recordedPoints = newPoints;
		}
		recordedPoints[recCount * 2] = x;
		recordedPoints[recCount * 2 + 1] = y;
		if (recCount > 2) {
			int ax = recordedPoints[0];
			int ay = recordedPoints[1];
			for (int i = 2; i < recCount * 2 - 2; i += 2) {
				int x0 = recordedPoints[i];
				int y0 = recordedPoints[i + 1];
				double dist = computeDistance(x0, y0, ax, ay, x, y);
				if (dist > EPSILON) {
					addVertex(recordedPoints[recCount * 2 - 2], recordedPoints[recCount * 2 - 1]);
					recordedPoints[0] = x;
					recordedPoints[1] = y;
					recCount = 0;
					break;
				}
			}
		}
		++recCount;
	}

	private static double computeDistance(double x0, double y0, double ax, double ay, double bx, double by) {
		double d = Math.abs((by - ay) * x0 - (bx - ax) * y0 + bx * ay - by * ax);
		double v = Math.sqrt((by - ay) * (by - ay) + (bx - ax) * (bx - ax));
		return d / v;
	}

	@Override
	public void endSession() {
		if (recCount > 0) {
			addVertex(recordedPoints[recCount * 2], recordedPoints[recCount * 2 + 1]);
			recCount = 0;
		}
		if (verCount > 0) {
			FigureDrawContext fdc = getPaintSurface().getDisplayFDC();
			double lineWidth = (double) settings.pencilRadius / (imageBounds.width * fdc.xScale);
			double[] points = new double[verCount * 2];
			System.arraycopy(vertices, 0, points, 0, points.length);
			executeOperation(new AddVectorOperation(paintExample, new PolylineFigure(settings.commonForegroundColor,
					lineWidth, SWT.LINE_SOLID, points, paintExample)));
			verCount = 0;
		}
		imageBounds = null;
		super.endSession();
	}

	private void addVertex(int x, int y) {
		if (imageBounds == null) {
			ImageFigure imageFigure = paintExample.getImageFigure();
			imageBounds = imageFigure != null ? imageFigure.getImageBounds() : paintExample.paintCanvas.getClientArea();
		}
		if (verCount * 2 + 1 >= vertices.length) {
			double[] newPoints = new double[vertices.length * 2];
			System.arraycopy(vertices, 0, newPoints, 0, vertices.length);
			vertices = newPoints;
		}
		FigureDrawContext fdc = getPaintSurface().getDisplayFDC();
		int xi = (x + fdc.xOffset) / fdc.xScale;
		int yi = (y + fdc.yOffset) / fdc.yScale;
		vertices[verCount * 2] = (double) (xi - imageBounds.x) / imageBounds.width;
		vertices[verCount * 2 + 1] = (double) (yi - imageBounds.y) / imageBounds.height;
		++verCount;
	}
}
