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

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DeleteVectorOperation extends AbstractOperation {

	private PaintExample paintExample;
	private VectorFigure selectedFigure;
	private int position = -1;

	public DeleteVectorOperation(PaintExample paintExample, VectorFigure selectedFigure) {
		super(PaintExample.getResourceString("operation.DeleteVector.label")); //$NON-NLS-1$
		this.paintExample = paintExample;
		this.selectedFigure = selectedFigure;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		List<Figure> vectorLayer = paintExample.getVectorLayer();
		position = vectorLayer.indexOf(selectedFigure);
		if (position >= 0) {
			vectorLayer.remove(position);
			paintExample.repaintSurface();
			paintExample.fireModify();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (position >= 0) {
			List<Figure> vectorLayer = paintExample.getVectorLayer();
			vectorLayer.add(position, selectedFigure);
			paintExample.repaintSurface();
			paintExample.fireModify();
			position = -1;
		}
		return Status.OK_STATUS;
	}

}
