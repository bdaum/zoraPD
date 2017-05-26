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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.ISpellCheckingTarget;

public class SpellCheckingJob extends Job {

	private static final ISchedulingRule SCHEDULINGRULE = new ISchedulingRule() {

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	List<ISpellIncident> incidents = new ArrayList<ISpellCheckingService.ISpellIncident>();

	private final int options;

	private final int nmax;

	private final ISpellCheckingTarget target;

	private final String text;

	public SpellCheckingJob(ISpellCheckingTarget target,
			String text, int options, int nmax) {
		super(Messages.SpellCheckingJob_check_spelling);
		this.text = text;
		setRule(SCHEDULINGRULE);
		setSystem(true);
		setPriority(Job.DECORATE);
		this.target = target;
		this.options = options;
		this.nmax = nmax;
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.SPELLING == family;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final BundleContext bundleContext = UiActivator.getDefault()
				.getBundle().getBundleContext();
		final ServiceReference<?> ref = bundleContext
				.getServiceReference(ISpellCheckingService.class.getName());
		if (ref != null) {
			((ISpellCheckingService) bundleContext
					.getService(ref)).checkSpelling(text,
					new ISpellCheckingService.IncendentListener() {

						public boolean handleIncident(ISpellIncident incident) {
							incidents.add(incident);
							return monitor.isCanceled();
						}
					}, options, nmax);
			bundleContext.ungetService(ref);
			if (!monitor.isCanceled())
				target.setSpellIncidents(incidents);
		}
		return Status.OK_STATUS;
	}

}
