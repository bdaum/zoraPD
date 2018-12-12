/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2018 Berthold Daum  
 */

package com.bdaum.zoom.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * A simple implementation of the IOperationHistory interface. Not thread safe;
 * does not require disposal nor does it dispose operations. The concept of
 * contexts is not supported. Open composite operations are not supported. Event listening is not supported.
 * To be used within local contexts with components
 * that are only disposed via garbage collection
 *
 */
public class LocalOperationHistory implements IOperationHistory {

	static final int DEFAULT_LIMIT = 20;

	private int limit = DEFAULT_LIMIT;
	private List<IUndoableOperation> redoList = new ArrayList<IUndoableOperation>();
	private List<IUndoableOperation> undoList = new ArrayList<IUndoableOperation>();

	@Override
	public void add(IUndoableOperation operation) {
		Assert.isNotNull(operation);
		if (checkUndoLimit(operation)) {
			undoList.add(operation);
			redoList.clear();
		}
	}

	@Override
	public void addOperationApprover(IOperationApprover approver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addOperationHistoryListener(IOperationHistoryListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canRedo(IUndoContext context) {
		IUndoableOperation operation = getRedoOperation(context);
		return (operation != null && operation.canRedo());
	}

	@Override
	public boolean canUndo(IUndoContext context) {
		IUndoableOperation operation = getUndoOperation(context);
		return (operation != null && operation.canUndo());
	}

	private boolean checkRedoLimit(IUndoableOperation operation) {
		if (limit > 0)
			forceRedoLimit(null, limit - 1);
		return true;
	}

	private boolean checkUndoLimit(IUndoableOperation operation) {
		if (limit > 0)
			forceUndoLimit(null, limit - 1);
		return true;
	}

	@Override
	public void dispose(IUndoContext context, boolean flushUndo, boolean flushRedo, boolean flushContext) {
		// do nothing
	}

	private IStatus doRedo(IProgressMonitor monitor, IAdaptable info, IUndoableOperation operation)
			throws ExecutionException {
		try {
			IStatus status = operation.redo(monitor, info);
			if (status.isOK()) {
				redoList.remove(operation);
				if (checkUndoLimit(operation))
					undoList.add(operation);
			}
			return status;
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException("While redoing the operation, an exception occurred", e); //$NON-NLS-1$
		}
	}

	private IStatus doUndo(IProgressMonitor monitor, IAdaptable info, IUndoableOperation operation)
			throws ExecutionException {
		try {
			IStatus status = operation.undo(monitor, info);
			if (status.isOK()) {
				undoList.remove(operation);
				if (checkRedoLimit(operation))
					redoList.add(operation);
			}
			return status;
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException("While undoing the operation, an exception occurred", e); //$NON-NLS-1$
		}

	}

	@Override
	public IStatus execute(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);
		if (!operation.canExecute())
			return IOperationHistory.OPERATION_INVALID_STATUS;
		try {
			IStatus status = operation.execute(monitor, info);
			if (status.isOK())
				add(operation);
			return status;
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException("While executing the operation, an exception occurred", e); //$NON-NLS-1$
		}
	}

	private void forceRedoLimit(IUndoContext context, int max) {
		IUndoableOperation[] ops = redoList.toArray(new IUndoableOperation[redoList.size()]);
		if (!redoList.isEmpty()) {
			int index = 0;
			while (redoList.size() > max)
				redoList.remove(ops[index++]);
		}
	}

	private void forceUndoLimit(IUndoContext context, int max) {
		IUndoableOperation[] ops = redoList.toArray(new IUndoableOperation[undoList.size()]);
		if (!undoList.isEmpty()) {
			int index = 0;
			while (undoList.size() > max)
				undoList.remove(ops[index++]);
		}
	}

	@Override
	public int getLimit(IUndoContext context) {
		return limit;
	}

	@Override
	public IUndoableOperation[] getRedoHistory(IUndoContext context) {
		return undoList.toArray(new IUndoableOperation[undoList.size()]);
	}

	@Override
	public IUndoableOperation getRedoOperation(IUndoContext context) {
		return redoList.isEmpty() ? null : redoList.get(redoList.size() - 1);
	}

	@Override
	public IUndoableOperation[] getUndoHistory(IUndoContext context) {
		return undoList.toArray(new IUndoableOperation[undoList.size()]);
	}

	@Override
	public IUndoableOperation getUndoOperation(IUndoContext context) {
		return undoList.isEmpty() ? null : undoList.get(undoList.size() - 1);
	}

	@Override
	public IStatus redo(IUndoContext context, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IUndoableOperation operation = getRedoOperation(context);
		if (operation == null)
			return IOperationHistory.NOTHING_TO_REDO_STATUS;
		if (!operation.canRedo())
			return IOperationHistory.OPERATION_INVALID_STATUS;
		return doRedo(monitor, info, operation);
	}

	@Override
	public IStatus redoOperation(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);
		return operation.canRedo() ? doRedo(monitor, info, operation) : IOperationHistory.OPERATION_INVALID_STATUS;
	}

	@Override
	public void removeOperationApprover(IOperationApprover approver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeOperationHistoryListener(IOperationHistoryListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceOperation(IUndoableOperation operation, IUndoableOperation[] replacements) {
		boolean inUndo = false;
		int index = undoList.indexOf(operation);
		if (index > -1) {
			inUndo = true;
			undoList.remove(operation);
			for (IUndoableOperation replacement : replacements)
				undoList.add(index, replacement);
			forceUndoLimit(null, limit);
		}
		if (inUndo)
			return;
		index = redoList.indexOf(operation);
		if (index == -1)
			return;
		redoList.remove(operation);
		for (IUndoableOperation replacement : replacements)
			redoList.add(index, replacement);
		forceRedoLimit(null, limit);
	}

	@Override
	public void setLimit(IUndoContext context, int limit) {
		Assert.isTrue(limit >= 0);
		this.limit = limit;
		forceUndoLimit(context, limit);
		forceRedoLimit(context, limit);
	}

	@Override
	public IStatus undo(IUndoContext context, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IUndoableOperation operation = getUndoOperation(context);
		if (operation == null)
			return IOperationHistory.NOTHING_TO_UNDO_STATUS;
		if (!operation.canUndo())
			return IOperationHistory.OPERATION_INVALID_STATUS;
		return doUndo(monitor, info, operation);
	}

	@Override
	public IStatus undoOperation(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);
		return operation.canUndo() ? doUndo(monitor, info, operation) : IOperationHistory.OPERATION_INVALID_STATUS;
	}

	@Override
	public void openOperation(ICompositeOperation operation, int mode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeOperation(boolean operationOK, boolean addToHistory, int mode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void operationChanged(IUndoableOperation operation) {
		// do nothing
	}

}
