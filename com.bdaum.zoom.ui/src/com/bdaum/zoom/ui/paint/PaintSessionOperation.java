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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

public class PaintSessionOperation extends AbstractOperation {

	private PaintExample paintExample;
	private ImageData imageData;
	private Rectangle bounds;
	private ImageData redoImageData;

	public PaintSessionOperation(PaintExample paintExample, ImageData imageData, Rectangle bounds) {
		super(PaintExample.getResourceString("operation.PaintSession.label")); //$NON-NLS-1$
		this.paintExample = paintExample;
		this.imageData = imageData;
		this.bounds = bounds;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// do nothing
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Image paintImage = paintExample.getHardcopy();
		if (paintImage != null && redoImageData != null) {
			Image redoImage = new Image(paintImage.getDevice(), redoImageData);
			paintExample.getPaintSurface().getImageFDC().gc.drawImage(redoImage, 0, 0, bounds.width, bounds.height,
					bounds.x, bounds.y, bounds.width, bounds.height);
			redoImage.dispose();
			redoImageData = null;
			paintExample.getPaintSurface().getPaintCanvas().redraw(bounds.x, bounds.y, bounds.width, bounds.height,
					true);
			paintExample.fireModify();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Image paintImage = paintExample.getHardcopy();
		if (paintImage != null) {
			Image redoImage = new Image(paintImage.getDevice(), bounds.width, bounds.height);
			GC gc = new GC(redoImage);
			gc.drawImage(paintImage, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, bounds.width,
					bounds.height);
			gc.dispose();
			redoImageData = redoImage.getImageData();
			redoImage.dispose();
			Image restoreImage = new Image(paintImage.getDevice(), imageData);
			paintExample.getPaintSurface().getImageFDC().gc.drawImage(restoreImage, 0, 0, bounds.width, bounds.height,
					bounds.x, bounds.y, bounds.width, bounds.height);
			restoreImage.dispose();
			paintExample.getPaintSurface().getPaintCanvas().redraw(bounds.x, bounds.y, bounds.width, bounds.height,
					true);
			paintExample.fireModify();
		}
		return Status.OK_STATUS;
	}

}
