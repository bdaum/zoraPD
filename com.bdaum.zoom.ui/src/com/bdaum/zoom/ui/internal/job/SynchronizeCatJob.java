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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.Ui;

public abstract class SynchronizeCatJob extends Job {

	protected static final int PERCENTCPU = 40;

	protected static final long MAXDELAY = 100;

	protected static final long MINDELAY = 20;

	protected static final ISchedulingRule exclusiveSchedulingRule = new ISchedulingRule() {

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	private long timeLastJobStarted;

	private int factor;


	public SynchronizeCatJob(String name) {
		super(name);
		setSystem(true);
		setPriority(Job.DECORATE);
		setRule(exclusiveSchedulingRule);
		factor = 100 / PERCENTCPU - 1;
	}


	@Override
	public boolean belongsTo(Object family) {
		return Constants.CATALOG == family;
	}


	protected void yield() {
		try {
			Thread.sleep(Math.max(
					MINDELAY,
					Math.min(MAXDELAY, (System.nanoTime() - timeLastJobStarted)
							* factor * (Ui.isWorkbenchActive() ? 1 : 4)
							/ 1000000)));
		} catch (InterruptedException e) {
			// ignore
		}
		setYieldStart();
	}

	protected void setYieldStart() {
		timeLastJobStarted = System.nanoTime();
	}


}
