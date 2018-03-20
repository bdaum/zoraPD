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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.internal.CoreActivator;

public class CompoundOperation extends AbstractOperation implements IProfiledOperation {

	private List<IUndoableOperation> operations = new ArrayList<IUndoableOperation>();

	public CompoundOperation(String label) {
		super(label);
	}

	/**
	 * Adds an operation to this compound operation
	 *
	 * @param op
	 *            - operation to add
	 */
	public void addOperation(IUndoableOperation op) {
		operations.add(op);
	}

	/**
	 * @return - true if this compound operations has no sub-operations
	 */
	public boolean isEmpty() {
		return operations.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#getExecuteProfile()
	 */
	public int getExecuteProfile() {
		int profile = IProfiledOperation.NONE;
		for (IUndoableOperation op : operations)
			if (op instanceof IProfiledOperation)
				profile |= ((IProfiledOperation) op).getExecuteProfile();
		return profile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#getUndoProfile()
	 */
	public int getUndoProfile() {
		int profile = IProfiledOperation.NONE;
		for (IUndoableOperation op : operations)
			if (op instanceof IProfiledOperation)
				profile |= ((IProfiledOperation) op).getUndoProfile();
		return profile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#getPriority()
	 */
	public int getPriority() {
		int min = Job.DECORATE;
		for (IUndoableOperation op : operations)
			if (op instanceof IProfiledOperation)
				min = Math.min(min, ((IProfiledOperation) op).getPriority());
		return min;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#isSilent()
	 */
	public boolean isSilent() {
		for (IUndoableOperation op : operations)
			if (op instanceof IProfiledOperation && !((IProfiledOperation) op).isSilent())
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canExecute()
	 */
	@Override
	public boolean canExecute() {
		for (IUndoableOperation op : operations)
			if (!op.canExecute())
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canUndo()
	 */
	@Override
	public boolean canUndo() {
		for (IUndoableOperation op : operations)
			if (!op.canUndo())
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canRedo()
	 */
	@Override
	public boolean canRedo() {
		for (IUndoableOperation op : operations)
			if (!op.canRedo())
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.
	 * eclipse .core.runtime.IProgressMonitor,
	 * org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		OperationStatus status = new OperationStatus(CoreActivator.PLUGIN_ID, 0, getLabel(), null);
		try {
			monitor.beginTask(getLabel(), operations.size());
			for (IUndoableOperation op : operations) {
				monitor.subTask(op.getLabel());
				status.add(op.execute(SubMonitor.convert(monitor, 1), info));
				if (!status.isOK())
					break;
			}
		} finally {
			monitor.done();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse
	 * .core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		OperationStatus status = new OperationStatus(CoreActivator.PLUGIN_ID, 0, getLabel(), null);
		try {
			monitor.beginTask(NLS.bind(Messages.getString("CompoundOperation.redo"), getLabel()), //$NON-NLS-1$
					operations.size());
			for (IUndoableOperation op : operations) {
				status.add(op.redo(SubMonitor.convert(monitor, 1), info));
				if (!status.isOK())
					break;
			}
		} finally {
			monitor.done();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse
	 * .core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		OperationStatus status = new OperationStatus(CoreActivator.PLUGIN_ID, 0, NLS.bind(Messages.getString("CompoundOperation.undo"), getLabel()), //$NON-NLS-1$
				null);
		try {
			monitor.beginTask(NLS.bind(Messages.getString("CompoundOperation.undo"), getLabel()), //$NON-NLS-1$
					operations.size());
			for (int i = operations.size() - 1; i >= 0; i--) {
				status.add(operations.get(i).undo(SubMonitor.convert(monitor, 1), info));
				if (!status.isOK())
					break;
			}
		} finally {
			monitor.done();
		}
		return status;
	}

}
