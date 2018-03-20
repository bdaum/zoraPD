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
package com.bdaum.juploadr.uploadapi.locrrest;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.albums.AddPhoto;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;

public class AddToAlbumTask  extends PostProcessingTask {
	private Collection<? extends PhotoSet> sets;
	private RestLocrApi api;
	private int numberOfSets;
	private boolean hasSetsToAdd;
	private String currentPhoto;
	private String currentSet;
	private int current = 0;

	public AddToAlbumTask(ExportToCommunityJob exportToCommunityJob,
			Session session) {
		super(exportToCommunityJob, Messages.getString(
				"juploadr.locrrest.task.addtophotoset.title", session.getAccount().getPhotosets().size()), session); //$NON-NLS-1$
		this.sets = session.getAccount().getPhotosets();
		this.api = (RestLocrApi) exportToCommunityJob.getImageUploadApi();
		this.min = 0;
		this.max = getTotalNumberOfPhotosToAdd(sets);
		hasSetsToAdd = this.max != 0;
	}

	
	@Override
	public String getTitle() {
		return Messages.getString(
				"juploadr.locrrest.task.addtophotoset.title", max);//$NON-NLS-1$ 
	}

	
	@Override
	public void execute(IProgressMonitor monitor) {
		// now add the photos in the set
		Iterator<? extends PhotoSet> iter = sets.iterator();
		while (iter.hasNext()) {
			PhotoSet set = iter.next();
			currentSet = set.getTitle();
			List<ImageAttributes> photos = set.getPhotos();
			for (ImageAttributes photo : photos) {
				currentPhoto = photo.getTitle();
				monitor.worked(1);
				// the photo might not have uploaded yet if the user dropped it
				// into the
				// window during an upload. In this case, the photoId will be
				// null.
				if (photo.getPhotoId() != null) {
					if (!photo.getPhotoId().equals(set.getPrimaryPhotoId())) {
						AddPhoto add = new AddPhoto(set, photo, session);
						if (api.executeMethod(add)) {
							//TODO What should happen here?
						}
					}
				}
			}
		}
	}

	private int getTotalNumberOfPhotosToAdd(Collection<? extends PhotoSet> psets) {
		Iterator<? extends PhotoSet> iter = psets.iterator();
		int count = 0;
		while (iter.hasNext()) {
			PhotoSet set = iter.next();
			if (!set.getPhotos().isEmpty()) {
				numberOfSets++;
				count += set.getPhotos().size();
			}
		}
		return count;
	}

	
	@Override
	public String getMessage() {
		return Messages
				.getString(
						"juploadr.locrrest.task.addtophotoset.dialog", (max - current) + 1, numberOfSets, currentPhoto, currentSet); //$NON-NLS-1$ 
	}

	public boolean hasSetsToAdd() {
		return hasSetsToAdd;
	}

}
