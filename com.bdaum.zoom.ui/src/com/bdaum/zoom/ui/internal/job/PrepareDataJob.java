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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

public abstract class PrepareDataJob extends Job implements DisposeListener {

	protected Control control;

	public PrepareDataJob(String name, Control control) {
		super(name);
		this.control = control;
		setSystem(true);
		setPriority(Job.INTERACTIVE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		asyncExec(new Runnable() {
			public void run() {
				if (!control.isDisposed()) {
					control.addDisposeListener(PrepareDataJob.this);
					control.setCursor(control.getDisplay().getSystemCursor(
							SWT.CURSOR_APPSTARTING));
				}
			}
		});
		doRun(monitor);
		asyncExec(new Runnable() {
			public void run() {
				if (!control.isDisposed()) {
					control.removeDisposeListener(PrepareDataJob.this);
					control.setCursor(control.getDisplay().getSystemCursor(
							SWT.CURSOR_ARROW));
				}
			}
		});
		return Status.OK_STATUS;
	}

	protected void asyncExec(Runnable runnable) {
		if (!control.isDisposed())
			control.getDisplay().asyncExec(runnable);
	}

	protected abstract void doRun(IProgressMonitor monitor);

	public void widgetDisposed(DisposeEvent e) {
		cancel();
	}
}