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
package com.bdaum.zoom.job;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TaskBar;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CoreActivator;

public abstract class CustomJob extends Job {

	int twork, cwork;

	private static List<CustomJob> taskBarJobs = Collections.synchronizedList(new LinkedList<CustomJob>());

	public CustomJob(String name) {
		super(name);
		setSystem(CoreActivator.getDefault().isNoProgress());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		if (belongsTo(Constants.CRITICAL)) {
			twork = 0;
			cwork = 0;
			IProgressMonitor monitorWrapper = new IProgressMonitor() {

				public void worked(int work) {
					cwork += work;
					monitor.worked(work);
					updateTaskBar();
				}

				public void subTask(String name) {
					monitor.subTask(name);
				}

				public void setTaskName(String name) {
					monitor.setTaskName(name);
				}

				public void setCanceled(boolean value) {
					monitor.setCanceled(value);
				}

				public boolean isCanceled() {
					return monitor.isCanceled();
				}

				public void internalWorked(double work) {
					monitor.internalWorked(work);
				}

				public void done() {
					cwork = 0;
					monitor.done();
					updateTaskBar();
				}

				public void beginTask(String name, int totalWork) {
					twork = totalWork;
					monitor.beginTask(name, totalWork);
					addToTaskBar();
				}
			};
			IStatus status;
			try {
				status = runJob(monitorWrapper);
			} finally {
				removeFromTaskBar();
			}
			return status;
		}
		return runJob(monitor);
	}

	private void removeFromTaskBar() {
		taskBarJobs.remove(this);
		updateTaskBar();
	}

	protected void addToTaskBar() {
		if (!taskBarJobs.contains(this))
			taskBarJobs.add(this);
		updateTaskBar();
	}

	protected synchronized static void updateTaskBar() {
		int n = 0;
		double complet = 0d;
		CustomJob[] tasks = taskBarJobs.toArray(new CustomJob[taskBarJobs.size()]);
		for (CustomJob job : tasks) {
			if (job.twork == IProgressMonitor.UNKNOWN) {
				complet = Double.POSITIVE_INFINITY;
				++n;
				break;
			}
			if (job.twork > 0) {
				complet += (double) job.cwork / job.twork;
				++n;
			}
		}
		final int state = n == 0 ? SWT.DEFAULT : Double.isInfinite(complet) ? SWT.INDETERMINATE : SWT.NORMAL;
		final int percent = n == 0 ? 0 : Double.isInfinite(complet) ? -1 : (int) (complet / n * 100);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows.length > 0) {
			final Shell shell = windows[0].getShell();
			final Display display = shell.getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (!display.isDisposed()) {
						TaskBar taskBar = display.getSystemTaskBar();
						if (taskBar != null) {
							TaskItem item = taskBar.getItem(shell);
							if (item == null)
								item = taskBar.getItem(null);
							if (item != null) {
								item.setProgressState(state);
								if (percent >= 0)
									item.setProgress(percent);
							}
						}
					}
				}
			});
		}
	}

	protected abstract IStatus runJob(IProgressMonitor monitorWrapper);

}
