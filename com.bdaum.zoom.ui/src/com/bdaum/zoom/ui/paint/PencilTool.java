/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.ui.paint;

import org.eclipse.swt.graphics.*;

/**
 * A pencil tool.
 */
public class PencilTool extends ContinuousPaintSession implements PaintTool {
	private ToolSettings settings;

	/**
	 * Constructs a pencil tool.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample
	 */
	public PencilTool(ToolSettings toolSettings, PaintExample paintExample) {
		super(paintExample);
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
		return PaintExample.getResourceString("tool.Pencil.label"); //$NON-NLS-1$
	}

	/*
	 * Template method for drawing
	 */

	@Override
	public void render(final Point point) {
		Figure figure;
		int pencilRadius = Math.min(50, Math.max(1, settings.pencilRadius));
		int opacity = settings.pencilOpacity;
		if (opacity < 255)
			opacity = (int) (opacity / (double) pencilRadius);
		if (pencilRadius <= 1)
			figure = new PointFigure(settings.commonForegroundColor, opacity, point.x, point.y);
		else {
			int r = pencilRadius / 2;
			figure = new SolidEllipseFigure(settings.commonForegroundColor, opacity, point.x - r, point.y - r,
					point.x + r, point.y + r);
		}
		PaintSurface ps = getPaintSurface();
		ps.drawFigure(figure);
		figure.addDamagedRegion(ps.getDisplayFDC(), damaged);
		paintExample.setDirty(true);
	}
}
