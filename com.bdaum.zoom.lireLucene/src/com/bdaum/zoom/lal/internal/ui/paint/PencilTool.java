/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.lal.internal.ui.paint;

import org.eclipse.swt.graphics.*;

/**
 * A pencil tool.
 */
public class PencilTool extends ContinuousPaintSession implements PaintTool {
	private ToolSettings settings;
	private final PaintExample paintExample;

	/**
	 * Constructs a pencil tool.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample 
	 * @param getPaintSurface
	 *            () the PaintSurface we will render on.
	 */
	public PencilTool(ToolSettings toolSettings, PaintSurface paintSurface, PaintExample paintExample) {
		super(paintSurface);
		this.paintExample = paintExample;
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
		final PaintSurface ps = getPaintSurface();
		if (settings.pencilRadius <= 1)
			ps.drawFigure(new PointFigure(settings.commonForegroundColor,
					point.x, point.y));
		else {
			int r = settings.pencilRadius / 2;
			ps.drawFigure(new SolidEllipseFigure(
					settings.commonForegroundColor, point.x - r, point.y - r,
					point.x + r, point.y + r));
		}
		paintExample.setDirty(true);
	}
}
