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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

/**
 * The superclass for paint tools that use click-drag-release motions to draw
 * objects.
 */
public abstract class DragPaintSession extends BasicPaintSession {
	/**
	 * True if a click-drag is in progress
	 */
	private boolean dragInProgress = false;

	/**
	 * The position of the first click in a click-drag
	 */
	private Point anchorPosition = new Point(-1, -1);

	/**
	 * A temporary point
	 */
	private Point tempPosition = new Point(-1, -1);

	/**
	 * Constructs a PaintSession.
	 * 
	 * @param paintExample
	 */
	protected DragPaintSession(PaintExample paintExample) {
		super(paintExample);
	}

	/**
	 * Activates the tool.
	 */

	public void beginSession() {
		super.beginSession();
		getPaintSurface().setStatusMessage(PaintExample.getResourceString("session.DragInteractivePaint.message")); //$NON-NLS-1$
		anchorPosition.x = -1;
		dragInProgress = false;
	}

	/**
	 * Resets the tool. Aborts any operation in progress.
	 */

	public void resetSession() {
		getPaintSurface().clearRubberbandSelection();
		anchorPosition.x = -1;
		dragInProgress = false;
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseDown:
			if (e.button == 1 && !dragInProgress) {
				dragInProgress = true;
				anchorPosition.x = e.x;
				anchorPosition.y = e.y;
			}
			break;
		case SWT.MouseUp:
			if (e.button != 1)
				resetSession(); // abort if right or middle mouse button pressed
			else if (dragInProgress) {
				dragInProgress = false;
				if (anchorPosition.x != -1)
					getPaintSurface().commitRubberbandSelection();
			}
			break;
		case SWT.MouseMove:
			final PaintSurface ps = getPaintSurface();
			if (!dragInProgress)
				ps.setStatusCoord(ps.getCurrentPosition());
			else {
				ps.setStatusCoordRange(anchorPosition, ps.getCurrentPosition());
				ps.clearRubberbandSelection();
				tempPosition.x = e.x;
				tempPosition.y = e.y;
				ps.addRubberbandSelection(createFigure(anchorPosition, tempPosition));
			}
			break;
		}
	}

	/**
	 * Template Method: Creates a Figure for drawing rubberband entities and the
	 * final product
	 * 
	 * @param anchor
	 *            the anchor point
	 * @param cursor
	 *            the point marking the current pointer location
	 */
	protected abstract Figure createFigure(Point anchor, Point cursor);
}
