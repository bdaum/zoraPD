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
package com.bdaum.zoom.operations.jobs;

import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IGalleryGenerator;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;

/**
 * Job for generating web galleries
 */
@SuppressWarnings("restriction")
public class WebGalleryJob extends CustomJob {

	private final WebGalleryImpl gallery;
	private final IGalleryGenerator generator;
	private final IAdaptable adaptable;
	private boolean wasAborted = false;

	/**
	 * Constructor
	 *
	 * @param gallery
	 *            - gallery from which the web gallery is generated
	 * @param generator
	 *            - generator used
	 * @param adaptable
	 *            - Adaptable instance answering at least Shell.class
	 */
	public WebGalleryJob(WebGalleryImpl gallery, IGalleryGenerator generator,
			IAdaptable adaptable) {
		super(Messages.getString("WebGalleryJob.generating_web_gallery")); //$NON-NLS-1$
		this.gallery = gallery;
		this.generator = generator;
		this.adaptable = adaptable;
		setPriority(Job.LONG);
		setUser(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family
				|| Constants.CRITICAL == family;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		MultiStatus status = new MultiStatus(CoreActivator.PLUGIN_ID, 0,
				Messages.getString("WebGalleryJob.web_gallery_generator"), null); //$NON-NLS-1$
		try {
			generator.generate(gallery, monitor, adaptable, status);
		} catch (IOException e) {
			status.add(new Status(
					IStatus.ERROR,
					CoreActivator.PLUGIN_ID,
					Messages.getString("WebGalleryJob.error_when_generatung_web_gallery"), //$NON-NLS-1$
					e));
			wasAborted = true;
		}
		wasAborted = monitor.isCanceled();
		if (!wasAborted)
			OperationJob.signalJobEnd(startTime);
		return status;
	}

	/**
	 * Returns true if job was aborted
	 *
	 * @return true if job was aborted
	 */
	public boolean wasAborted() {
		return wasAborted;
	}

}
