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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.batch.internal;

import org.eclipse.core.runtime.jobs.ISchedulingRule;


public abstract class SerializingDaemon extends Daemon {

	protected static ISchedulingRule schedulingRule = new ISchedulingRule() {

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}
		};

	public SerializingDaemon(String name) {
		super(name, -1);
		setRule(schedulingRule);
	}

}