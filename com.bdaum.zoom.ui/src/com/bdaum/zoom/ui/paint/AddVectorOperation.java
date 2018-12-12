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

public class AddVectorOperation extends AbstractOperation {

	private PaintExample paintExample;
	private PolylineFigure polylineFigure;

	public AddVectorOperation(PaintExample paintExample, PolylineFigure polylineFigure) {
		super(PaintExample.getResourceString("operation.AddVector.label")); //$NON-NLS-1$
		this.paintExample = paintExample;
		this.polylineFigure = polylineFigure;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		paintExample.addVectorFigure(polylineFigure);
		paintExample.repaintSurface();
		paintExample.fireModify();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		List<Figure> vectorLayer = paintExample.getVectorLayer();
		if (!vectorLayer.isEmpty()) {
			vectorLayer.remove(vectorLayer.size() - 1);
			paintExample.repaintSurface();
			paintExample.fireModify();
		}
		return Status.OK_STATUS;
	}

}
