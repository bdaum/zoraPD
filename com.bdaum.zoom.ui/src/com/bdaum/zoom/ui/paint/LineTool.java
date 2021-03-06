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
package com.bdaum.zoom.ui.paint;


import org.eclipse.swt.graphics.*;

/**
 * A line drawing tool
 */
public class LineTool extends DragPaintSession implements PaintTool {
	private ToolSettings settings;

	/**
	 * Constructs a LineTool.
	 * 
	 * @param toolSettings the new tool settings
	 * @param paintExample
	 */
	public LineTool(ToolSettings toolSettings, PaintExample paintExample) {
		super(paintExample);
		set(toolSettings);
	}
	
	/**
	 * Sets the tool's settings.
	 * 
	 * @param toolSettings the new tool settings
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
		return PaintExample.getResourceString("tool.Line.label"); //$NON-NLS-1$
	}

	/*
	 * Template methods for drawing
	 */
	
	@Override
	protected Figure createFigure(Point a, Point b) {
		return new LineFigure(settings.commonForegroundColor, settings.commonBackgroundColor, settings.commonLineStyle,
			a.x, a.y, b.x, b.y);
	}
}
