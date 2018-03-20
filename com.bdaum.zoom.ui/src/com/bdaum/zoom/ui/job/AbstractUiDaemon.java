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

package com.bdaum.zoom.ui.job;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiActivator;

/**
 * Implements a daemon job that runs in the background
 *
 */
@SuppressWarnings("restriction")
public abstract class AbstractUiDaemon extends Daemon {

	private long minDelay;
	private int factor;
	protected UiActivator activator;
	protected long timeLastJobStarted;

	/**
	 * @param name
	 *            - daemon name
	 * @param minDelay
	 *            - minimum delay between executions
	 * @param maxDelay
	 *            - maximum delay between executions
	 * @param percentCpu
	 *            - maximum CPU utilization
	 */
	public AbstractUiDaemon(String name, int minDelay, int maxDelay, int percentCpu) {
		super(name, maxDelay);
		this.minDelay = minDelay;
		factor = percentCpu <= 0 ? 4 : 100 / percentCpu - 1;
	}

	@Override
	protected long computeDelay() {
		return Math.max(minDelay, Math.min(maxDelay,
				(System.nanoTime() - timeLastJobStarted) * factor * (Ui.isWorkbenchActive() ? 1 : 4) / 1000000));
	}

	/**
	 * Test if running the daemon is appropriate
	 *
	 * @return returns true if the daemon is allowed to execute
	 */
	@Override
	protected boolean mayRun() {
		if (activator == null)
			activator = UiActivator.getDefault();
		if (activator == null || activator.isSlideShowRunning())
			return false;
		timeLastJobStarted = System.nanoTime();
		return true;
	}

}