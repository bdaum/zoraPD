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

package com.bdaum.zoom.flickrrest.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.scohen.juploadr.Messages;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.photosets.CreatePhotoSet;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;
import com.flickr4java.flickr.FlickrException;

public class CreateAllPhotosetsTask extends PostProcessingTask {
	private Collection<? extends PhotoSet> sets;
	private RestFlickrApi api;
	private int current;

	public CreateAllPhotosetsTask(ExportToCommunityJob job, Session session) {
		super(job, Messages
				.getString("CreateAllPhotosetsTask.create_photosets"), session); //$NON-NLS-1$
		this.sets = getNewPhotoSets(session.getAccount().getPhotosets());
		this.api = (RestFlickrApi) job.getImageUploadApi();
		this.max = sets.size();
		this.min = 0;
	}

	@Override
	public String getTitle() {
		return Messages.getString(
				"juploadr.ui.task.createphotsets.creating.title", max); //$NON-NLS-1$
	}

	public boolean hasSetsToCreate() {
		return !sets.isEmpty();
	}

	private static Collection<PhotoSet> getNewPhotoSets(Collection<PhotoSet> photoSets) {
		List<PhotoSet> rv = new LinkedList<PhotoSet>();
		for (PhotoSet set : photoSets)
			if (set.isNew())
				rv.add(set);
		return rv;
	}

	@Override
	public void execute(IProgressMonitor monitor) {
		for (PhotoSet set : sets) {
			try {
				new CreatePhotoSet(set, session).execute();
				set.setNew(false);
			} catch (ProtocolException e) {
				// should not happen
			} catch (CommunicationException e) {
				if (e.getCause() instanceof FlickrException) {
					MessageDialog
							.openError(
									null,
									Messages.getString("CreateAllPhotosetsTask.error_when_creating_photosets"), //$NON-NLS-1$
									((FlickrException) e.getCause())
											.getErrorMessage());
					break;
				}
				api.handleCommunicationError(e);
			}
			monitor.worked(1);
		}
	}

	@Override
	public String getMessage() {
		return Messages.getString(
				"juploadr.ui.task.createphotsets.creating", current, max); //$NON-NLS-1$
	}

}
