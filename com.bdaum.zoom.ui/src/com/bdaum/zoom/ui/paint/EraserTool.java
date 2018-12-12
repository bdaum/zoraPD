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

import org.eclipse.swt.graphics.*;

/**
 * A drawing tool.
 */
public class EraserTool extends ClickPaintSession implements PaintTool {
	@SuppressWarnings("unused")
	private ToolSettings settings;
	private VectorFigure selectedFigure;

	/**
	 * Constructs a EllipseTool.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample
	 */
	public EraserTool(ToolSettings toolSettings,  PaintExample paintExample) {
		super(paintExample);
		set(toolSettings);
		vectored = true;
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
	 * Returns name associated with this tool.
	 * 
	 * @return the localized name of this tool
	 */

	public String getDisplayName() {
		return PaintExample.getResourceString("tool.Eraser.label"); //$NON-NLS-1$
	}

	@Override
	public void beginSession() {
		super.beginSession();
		Point mousePos = getAnchor();
		if (mousePos.x >= 0 && mousePos.y >= 0) {
			ImageFigure imageFigure = paintExample.getImageFigure();
			Rectangle imageBounds = imageFigure != null ? imageFigure.getImageBounds()
					: paintExample.paintCanvas.getClientArea();
			FigureDrawContext fdc = getPaintSurface().getDisplayFDC();
			int xi = (mousePos.x + fdc.xOffset) / fdc.xScale;
			int yi = (mousePos.y + fdc.yOffset) / fdc.yScale;
			double x = (double) (xi - imageBounds.x) / imageBounds.width;
			double y = (double) (yi - imageBounds.y) / imageBounds.height;
			selectedFigure = paintExample.findVectorFigure(x, y);
		}
	}

	@Override
	public void endSession() {
		if (selectedFigure != null) {
			executeOperation(new DeleteVectorOperation(paintExample, selectedFigure));
			selectedFigure = null;
		}
		super.endSession();
	}
}
