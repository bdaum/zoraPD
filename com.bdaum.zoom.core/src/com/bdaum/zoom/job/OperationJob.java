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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.job;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

/**
 * Job for executing most of the applications operations in the background
 *
 */
public class OperationJob extends CustomJob {


	private static ListenerList<IJobChangeListener> jobChangeListeners = new ListenerList<IJobChangeListener>();

	private IProfiledOperation operation;
	private IAdaptable info;
	private boolean slave;

	/**
	 * Executes an operation in the background
	 *
	 * @param operation
	 *            - operation to be executed
	 * @param info
	 *            - an Adaptable instance answering at least Shell.class
	 * @return - the job running in the background
	 */
	public static OperationJob executeOperation(IProfiledOperation operation,
			IAdaptable info) {
		return executeOperation(operation, info, false);
	}

	/**
	 * Executes an operation in the background
	 *
	 * @param operation
	 *            - operation to be executed
	 * @param info
	 *            - an Adaptable instance answering at least Shell.class
	 * @param user
	 *            - true if user should be asked for background operation
	 * @return - the job running in the background
	 */
	public static OperationJob executeOperation(IProfiledOperation operation,
			IAdaptable info, boolean user) {
		return executeOperation(operation, info, user, 0L);
	}

	/**
	 * Executes an operation in the background
	 *
	 * @param operation
	 *            - operation to be executed
	 * @param info
	 *            - an Adaptable instance answering at least Shell.class
	 * @param user
	 *            - true if user should be asked for background operation
	 * @param delay
	 *            - a delay in msec
	 * @return - the job running in the background
	 */
	public static OperationJob executeOperation(IProfiledOperation operation,
			IAdaptable info, boolean user, long delay) {
		OperationJob job = new OperationJob(operation, info);
		for (Object listener : jobChangeListeners.getListeners())
			job.addJobChangeListener((IJobChangeListener) listener);
		job.setUser(user);
		job.schedule(delay);
		return job;
	}

	/**
	 * Executes a slave operation in the background. Slave operations don't
	 * contribute to the Undo history.
	 *
	 * @param operation
	 *            - operation to be executed
	 * @param info
	 *            - an Adaptable instance answering at least Shell.class
	 * @return - the job running in the background
	 */
	public static OperationJob executeSlaveOperation(
			IProfiledOperation operation, IAdaptable info) {
		OperationJob job = new OperationJob(operation, info);
		job.setUser(false);
		job.schedule(0L);
		job.setSlave(true);
		return job;
	}

	/**
	 * Adds a listener that will be notified for each operation job executed
	 * @param listener - job change listener
	 */
	public static void addOperationJobListener(IJobChangeListener listener) {
		jobChangeListeners.add(listener);
	}

	/**
	 * Removes an operation job listener
	 * @param listener - job change listener
	 */
	public static void removeOperationJobListener(IJobChangeListener listener) {
		jobChangeListeners.remove(listener);
	}


	// New jobs are created through executeOperation()
	private OperationJob(IProfiledOperation operation, IAdaptable info) {
		super(operation.getLabel());
		this.operation = operation;
		setRule(new ProfiledSchedulingRule(getJobOperationType(),
				getJobProfile()));
		this.info = info;
		setPriority(operation.getPriority());
		if (operation instanceof DbOperation)
			((DbOperation) operation).setJob(this);
	}

	private void setSlave(boolean slave) {
		this.slave = slave;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family
				|| Constants.CRITICAL == family;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */

	@Override
	protected IStatus runJob(final IProgressMonitor monitor) {
		IStatus status;
		if (slave) {
			try {
				status = operation.execute(monitor, info);
			} catch (ExecutionException e) {
				status = new Status(
						IStatus.ERROR,
						CoreActivator.PLUGIN_ID,
						NLS.bind(
								Messages.getString("OperationJob.error_executing_operation"), //$NON-NLS-1$
								operation.getLabel()), e);
			}
		} else {
			long startTime = System.currentTimeMillis();
			status = CoreActivator.getDefault().performOperation(operation,
					monitor, info);
			if (!isSilent())
				signalJobEnd(startTime);
		}
		System.gc();
		return status;
	}

	/**
	 * Indicates if the job is running silently (not visible at the UI)
	 *
	 * @return true if the job is running silently
	 */
	public boolean isSilent() {
		return operation.isSilent();
	}

	/**
	 * Signals the end of the job
	 *
	 * @param startTime
	 *            - time when the job was started
	 */
	public static void signalJobEnd(long startTime) {
		IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
		if (errorHandler != null)
			errorHandler
					.signalEOJ((System.currentTimeMillis() - startTime > 3000) ? "chimes" //$NON-NLS-1$
							: "done"); //$NON-NLS-1$
	}

	private int getJobProfile() {
		return (operation == null) ? 0 : operation.getExecuteProfile();
	}

	private Class<? extends Object> getJobOperationType() {
		return (operation == null) ? this.getClass() : operation.getClass();
	}

	public IProfiledOperation getOperation() {
		return operation;
	}
}