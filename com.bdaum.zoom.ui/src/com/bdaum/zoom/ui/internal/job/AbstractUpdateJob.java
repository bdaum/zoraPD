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

package com.bdaum.zoom.ui.internal.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.job.CustomJob;

public abstract class AbstractUpdateJob extends CustomJob {

	protected static final ISchedulingRule rule = new ISchedulingRule() {

			public boolean isConflicting(ISchedulingRule r) {
				return r == this;
			}

			public boolean contains(ISchedulingRule r) {
				return r == this;
			}
		};

	public AbstractUpdateJob(String name) {
		super(name);
		setPriority(Job.LONG);
		setRule(rule);
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.UPDATING == family;
	}

}