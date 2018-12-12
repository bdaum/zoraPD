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

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * The superclass for paint tools that use click-drag-release motions to draw
 * objects.
 */
public abstract class ClickPaintSession extends BasicPaintSession {
	
	private Point anchor = new Point(-1, -1);

	/**
	 * Constructs a PaintSession.
	 * 
	 * @param paintExample
	 */
	protected ClickPaintSession(PaintExample paintExample) {
		super(paintExample);
	}

	/**
	 * Resets the tool. Aborts any operation in progress.
	 */

	public void resetSession() {
		// do nothing
	}

	/**
	 * Handles a mouseDown event.
	 * 
	 * @param event
	 *            the mouse event detail information
	 */

	public void mouseDown(MouseEvent event) {
		if (event.button != 1)
			return;
		anchor.x = event.x;
		anchor.y = event.y;
		beginSession();
	}
	
	public Point getAnchor() {
		return anchor;
	}

	/**
	 * Handles a mouseDoubleClick event.
	 * 
	 * @param event
	 *            the mouse event detail information
	 */

	public void mouseDoubleClick(MouseEvent event) {
		// do nothing
	}

	/**
	 * Handles a mouseUp event.
	 * 
	 * @param event
	 *            the mouse event detail information
	 */

	public void mouseUp(MouseEvent event) {
		if (event.button != 1) {
			resetSession(); // abort if right or middle mouse button pressed
			return;
		}
		endSession();
		anchor.x = -1;
		anchor.y = -1;
	}

	/**
	 * Handles a mouseMove event.
	 * 
	 * @param event
	 *            the mouse event detail information
	 */

	public void mouseMove(MouseEvent event) {
		getPaintSurface().setStatusCoord(getPaintSurface().getCurrentPosition());
	}

}
