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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.flickrrest.internal.photosets.AddPhoto;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;

public class AddToPhotosetTask extends PostProcessingTask {
	private Collection<? extends PhotoSet> sets;
	private RestFlickrApi api;
	private int numberOfSets;
	private boolean hasSetsToAdd;
	private String currentPhoto;
	private String currentSet;
	private int current = 0;

	public AddToPhotosetTask(ExportToCommunityJob exportToCommunityJob,
			Session session) {
		super(
				exportToCommunityJob,
				Messages.getString(
						"juploadr.flickrrest.task.addtophotoset.title", session.getAccount().getPhotosets().size()), session); //$NON-NLS-1$
		this.sets = session.getAccount().getPhotosets();
		this.api = (RestFlickrApi) exportToCommunityJob.getImageUploadApi();
		this.min = 0;
		this.max = getTotalNumberOfPhotosToAdd(sets);
		hasSetsToAdd = this.max != 0;
	}

	@Override
	public String getTitle() {
		return Messages.getString(
				"juploadr.flickrrest.task.addtophotoset.title", max);//$NON-NLS-1$
	}

	@Override
	public void execute(IProgressMonitor monitor) {
		// now add the photos in the set
		for (PhotoSet set : sets) {
			currentSet = set.getTitle();
			List<ImageAttributes> photos = set.getPhotos();
			for (ImageAttributes photo : photos) {
				currentPhoto = photo.getTitle();
				monitor.worked(1);
				if (photo.getPhotoId() != null
						&& !photo.getPhotoId().equals(set.getPrimaryPhotoId()))
					if (api.executeMethod(new AddPhoto(set, photo, session))) {
						// TODO something missing here?
					}
			}
		}
	}

	private int getTotalNumberOfPhotosToAdd(
			Collection<? extends PhotoSet> photosets) {
		int count = 0;
		for (PhotoSet set : photosets)
			if (!set.getPhotos().isEmpty()) {
				numberOfSets++;
				count += set.getPhotos().size();
			}
		return count;
	}

	@Override
	public String getMessage() {
		return Messages
				.getString(
						"juploadr.flickrrest.task.addtophotoset.dialog", (max - current) + 1, numberOfSets, currentPhoto, currentSet); //$NON-NLS-1$
	}

	public boolean hasSetsToAdd() {
		return hasSetsToAdd;
	}

}
