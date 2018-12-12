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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

public abstract class BasicPaintSession implements PaintSession {
	/**
	 * The paint surface
	 */
	protected Region damaged;
	private Image backupImage;
	protected PaintExample paintExample;
	protected boolean vectored = false;

	/**
	 * Constructs a PaintSession.
	 * 
	 * @param paintExample
	 */
	protected BasicPaintSession(PaintExample paintExample) {
		this.paintExample = paintExample;
	}

	/**
	 * Returns the paint surface associated with this paint session.
	 * 
	 * @return the associated PaintSurface
	 */

	public PaintSurface getPaintSurface() {
		return paintExample.getPaintSurface();
	}

	@Override
	public void beginSession() {
		dispose();
		if (!vectored) {
			damaged = new Region(paintExample.getDisplay());
			Image paintImage = paintExample.getHardcopy();
			backupImage = new Image(paintImage.getDevice(), paintImage.getImageData());
		}
	}

	@Override
	public void endSession() {
		try {
			if (damaged != null) {
				if (backupImage != null && !damaged.isEmpty()) {
					Rectangle bounds = damaged.getBounds();
					if (bounds.x < 0) {
						bounds.width += bounds.x;
						bounds.x = 0;
					}
					if (bounds.y < 0) {
						bounds.height += bounds.y;
						bounds.y = 0;
					}
					if (!bounds.isEmpty()) {
						Image restoreImage = new Image(backupImage.getDevice(), bounds.width, bounds.height);
						GC gc = new GC(restoreImage);
						gc.drawImage(backupImage, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, bounds.width,
								bounds.height);
						gc.dispose();
						PaintSessionOperation op = new PaintSessionOperation(paintExample, restoreImage.getImageData(),
								bounds);
						restoreImage.dispose();
						executeOperation(op);
						paintExample.fireModify();
					}
				}
			}
		} finally {
			dispose();
		}
	}

	protected void dispose() {
		if (damaged != null) {
			damaged.dispose();
			damaged = null;
		}
		if (backupImage != null) {
			backupImage.dispose();
			backupImage = null;
		}
	}

	protected void executeOperation(AbstractOperation op) {
		op.addContext(paintExample.getUndoContext());
		try {
			paintExample.getHistory().execute(op, null, paintExample);
		} catch (ExecutionException e) {
			// should never happen
		}
	}

}
