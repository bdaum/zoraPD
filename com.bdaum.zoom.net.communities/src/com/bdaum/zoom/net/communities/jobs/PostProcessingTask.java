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
package com.bdaum.zoom.net.communities.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.net.communities.CommunityApi;

public abstract class PostProcessingTask extends CustomJob {

	final class PostProcessingRule implements ISchedulingRule {

		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof PostProcessingTask.PostProcessingRule
					|| rule instanceof UploadJob.UploadRule;
		}

		public ExportToCommunityJob getParentJob() {
			return PostProcessingTask.this.exportToCommunityJob;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}

	protected int min;
	protected int max;
	protected final Session session;
	private final ExportToCommunityJob exportToCommunityJob;

	public PostProcessingTask(ExportToCommunityJob exportToCommunityJob,
			String name, Session session) {
		super(name);
		this.exportToCommunityJob = exportToCommunityJob;
		this.session = session;
		setRule(new PostProcessingRule());
		setPriority(Job.LONG);

	}

	public abstract void execute(IProgressMonitor monitor);

	public abstract String getMessage();

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family
				|| exportToCommunityJob.getExportId().equals(family)
				|| Constants.CRITICAL == family;
	}

	protected void handleError(Exception e) {
		CommunityApi api = session.getApi();
		api.setStatus(exportToCommunityJob.getStatus());
		api.handleCommunicationError(e);
	}


	@Override
	public IStatus runJob(IProgressMonitor monitor) {
		monitor.beginTask(getTitle(), max);
		execute(monitor);
		monitor.done();
		return Status.OK_STATUS;
	}

	public abstract String getTitle();
}
