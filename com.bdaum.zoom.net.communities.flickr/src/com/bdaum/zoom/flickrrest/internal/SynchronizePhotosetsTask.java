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

package com.bdaum.zoom.flickrrest.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.photosets.GetPhotosetList;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;

public class SynchronizePhotosetsTask extends PostProcessingTask {
	private RestFlickrApi api;

	public SynchronizePhotosetsTask(ExportToCommunityJob exportToCommunityJob,
			Session session) {
		super(
				exportToCommunityJob,
				Messages
						.getString("SynchronizePhotosetsTask.synchronize_photosets"), session); //$NON-NLS-1$
		this.api = (RestFlickrApi) exportToCommunityJob.getImageUploadApi();
	}


	@Override
	public String getTitle() {
		return Messages
				.getString("SynchronizePhotosetsTask.synchronizing_photosets"); //$NON-NLS-1$
	}


	@Override
	public void execute(IProgressMonitor monitor) {
		session.getAccount().getPhotosets().clear();
		api.executeMethod(new GetPhotosetList(session));
	}


	@Override
	public String getMessage() {
		return Messages
				.getString("SynchronizePhotosetsTask.synchronizing_photosets"); //$NON-NLS-1$
	}

}
