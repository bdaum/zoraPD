/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 *
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.juploadr.uploadapi.smugrest;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.photosets.CreatePhotoSet;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;

public class CreateAllPhotosetsTask extends PostProcessingTask {
	private Collection<? extends PhotoSet> sets;
	private SmugmugRestApi api;
	private String photoSetName;
	private int current;
	private int max;

	public CreateAllPhotosetsTask(ExportToCommunityJob job, Session session) {
		super(
				job,
				Messages
						.getString(
								"juploadr.ui.task.createphotsets.creating", session.getAccount().getPhotosets().size()), session); //$NON-NLS-1$
		this.sets = getNewPhotoSets(session.getAccount().getPhotosets());
		this.api = (SmugmugRestApi) job.getImageUploadApi();
		this.max = sets.size();
	}


	@Override
	public String getTitle() {
		return Messages.getString(
				"juploadr.ui.task.createphotsets.creating", max); //$NON-NLS-1$
	}

	public boolean hasSetsToCreate() {
		return sets.size() > 0;
	}

	private static Collection<PhotoSet> getNewPhotoSets(Collection<PhotoSet> photoSets) {
		List<PhotoSet> rv = new LinkedList<PhotoSet>();
		Iterator<PhotoSet> iter = photoSets.iterator();
		while (iter.hasNext()) {
			PhotoSet set = iter.next();
			if (set.isNew()) {
				rv.add(set);
			}
		}
		return rv;
	}


	@Override
	public void execute(IProgressMonitor monitor) {
		Iterator<? extends PhotoSet> iter = sets.iterator();

		while (iter.hasNext()) {

			PhotoSet set = iter.next();
			photoSetName = set.getTitle();
			// updateMessage();
			CreatePhotoSet create = new CreatePhotoSet(set, session);
			if (api.executeMethod(create)) {
				set.setNew(false);
			} else {
				// if we can't create, we will not be able to continue.
				if (create.getErrorCode() == 3) {
					MessageDialog
							.openError(
									null,
									Messages
											.getString("CreateAllPhotosetsTask.error_when_creating_photoset"), create //$NON-NLS-1$
											.getResponseHandler()
											.getFailureReason());
					break;
				}
			}
			monitor.worked(1);
		}
	}


	@Override
	public String getMessage() {
		return Messages.getString("juploadr.ui.task.createphotsets.creating", photoSetName + " (" + current + "/" + sets.size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

}
