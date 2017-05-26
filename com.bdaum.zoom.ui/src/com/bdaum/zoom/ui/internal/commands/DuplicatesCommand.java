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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.commands;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.window.Window;

import com.bdaum.zoom.ui.internal.dialogs.FindDuplicatesDialog;
import com.bdaum.zoom.ui.internal.job.FindDuplicatesJob;

public class DuplicatesCommand extends AbstractCommandHandler {

	private boolean running;

	@Override
	public void run() {
		if (!running) {
			FindDuplicatesDialog dialog = new FindDuplicatesDialog(getShell());
			if (dialog.open() == Window.OK) {
				FindDuplicatesJob job = new FindDuplicatesJob(getActiveWorkbenchWindow(), dialog.getKind(),
						dialog.getMethod(), dialog.getIgnoreDerivates(), dialog.isWithExtension());
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						running = false;
					}
				});
				running = true;
				job.schedule();
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return !running;
	}

}
