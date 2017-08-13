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

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * A pencil tool.
 */
public class PipetteTool extends ContinuousPaintSession implements PaintTool {
	// private ToolSettings settings;
	private PaintExample paintExample;

	/**
	 * Constructs a pencil tool.
	 *
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample
	 * @param getPaintSurface
	 *            () the PaintSurface we will render on.
	 */
	public PipetteTool(ToolSettings toolSettings, PaintSurface paintSurface,
			PaintExample paintExample) {
		super(paintSurface);
		set(toolSettings);
		this.paintExample = paintExample;
	}

	/**
	 * Sets the tool's settings.
	 *
	 * @param toolSettings
	 *            the new tool settings
	 */

	public void set(ToolSettings toolSettings) {
		// settings = toolSettings;
	}

	/**
	 * Returns the name associated with this tool.
	 *
	 * @return the localized name of this tool
	 */

	public String getDisplayName() {
		return PaintExample.getResourceString("tool.Pipette.label"); //$NON-NLS-1$
	}

	@Override
	public void beginSession() {  // bd
		super.beginSession();
		getPaintSurface().setStatusMessage(
				PaintExample
						.getResourceString("session.Pipette.message")); //$NON-NLS-1$
	}

	/*
	 * Template method for drawing
	 */

	@Override
	public void render(final Point point) {
		PaintSurface ps = getPaintSurface();
		ImageData imageData = ps.getPaintImage().getImageData();
		int pixel = imageData.getPixel(point.x, point.y);
		PaletteData palette = imageData.palette;
		RGB rgb = palette.getRGB(pixel);
		paintExample.setPickedColor(rgb);
	}
}
