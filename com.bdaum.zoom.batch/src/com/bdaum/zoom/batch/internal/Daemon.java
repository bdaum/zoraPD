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
 * (c) 2009-2010 Berthold Daum  
 */

package com.bdaum.zoom.batch.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.program.BatchConstants;

public abstract class Daemon extends Job {

	protected long maxDelay;
	protected long pause = -1;

	/**
	 * @param name
	 *            - daemon name
	 * @param delay
	 *            - delay between executions
	 */
	public Daemon(String name, long delay) {
		super(name);
		this.maxDelay = delay;
		setSystem(true);
		setPriority(Job.DECORATE);
	}

	/**
	 * Test if running the daemon is appropriate
	 *
	 * @return returns true if the daemon is allowed to execute
	 */
	protected boolean mayRun() {
		return true;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (mayRun()) {
			try {
				doRun(monitor);
			} catch (Throwable e) {
				System.out.println(e);
				// try again
			}
		}
		if (!monitor.isCanceled() && maxDelay > 0)
			schedule(Math.max(pause, computeDelay()));
		pause = -1;
		return Status.OK_STATUS;
	}

	protected long computeDelay() {
		return maxDelay;
	}

	/**
	 * Subclasses must implement this method
	 *
	 * @param monitor
	 *            - Progress monitor
	 */
	protected abstract void doRun(IProgressMonitor monitor);

	@Override
	public boolean belongsTo(Object family) {
		return family == this || BatchConstants.DAEMONS == family;
	}

	/**
	 * Sets a pause for the next execution
	 *
	 * @param pause
	 *            - pause in msec
	 */
	public void setPause(long msec) {
		this.pause = msec;
	}

}
