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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

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

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseDown:
			if (e.button == 1) {
				anchor.x = e.x;
				anchor.y = e.y;
				beginSession();
			}
			break;
		case SWT.MouseUp:
			if (e.button != 1)
				resetSession(); // abort if right or middle mouse button pressed
			else {
				endSession();
				anchor.x = -1;
				anchor.y = -1;
			}
			break;
		case SWT.MouseMove:
			getPaintSurface().setStatusCoord(getPaintSurface().getCurrentPosition());
			break;
		}
	}

	public Point getAnchor() {
		return anchor;
	}

}
