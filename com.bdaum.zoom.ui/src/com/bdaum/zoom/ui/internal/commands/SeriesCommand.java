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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.window.Window;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.dialogs.FindSeriesDialog;
import com.bdaum.zoom.ui.internal.job.FindSeriesJob;

public class SeriesCommand extends AbstractCommandHandler {

	private boolean running;

	@Override
	public void run() {
		if (!running) {
			FindSeriesDialog dialog = new FindSeriesDialog(getShell());
			if (dialog.open() == Window.OK) {
				int type = dialog.getType();
				FindSeriesJob job = new FindSeriesJob(getActiveWorkbenchWindow(), dialog.getResult(),
						type != Constants.SERIES_RAPID ? dialog.getInterval() : 250, dialog.getSize(),
						dialog.getIgnoreDerivates() || type != Constants.SERIES_ALL,
						dialog.isSeparateFormats() || type != Constants.SERIES_ALL, type);
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
