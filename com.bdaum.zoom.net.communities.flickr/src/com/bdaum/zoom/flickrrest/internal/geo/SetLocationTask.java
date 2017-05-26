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

package com.bdaum.zoom.flickrrest.internal.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.geo.UserNotConfiguredException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;
import com.bdaum.zoom.net.communities.jobs.PostProcessingTask;
import com.bdaum.zoom.net.communities.ui.AuthDialog;

public class SetLocationTask extends PostProcessingTask {
	private static final String PRIVACYLINK = "http://www.flickr.com/account/geo/privacy/"; //$NON-NLS-1$

	private static final String PRIVACYMESSAGE = Messages.SetLocationTask_in_order_to_set_locations;

	private List<ImageAttributes> toSetLocations = new ArrayList<ImageAttributes>();
	private String photoName;

	public SetLocationTask(ExportToCommunityJob job,
			Collection<? extends ImageAttributes> images,
			Session session) {
		super(job, Messages.SetLocationTask_setting_location, session);
		Set<QueryField> filter = job.getFilter();
		if (filter != null && filter.contains(QueryField.EXIF_GPSLATITUDE)
				&& filter.contains(QueryField.EXIF_GPSLONGITUDE)) {
			for (ImageAttributes img : images)
				if (img.getLocation() != null)
					toSetLocations.add(img);
		}
		this.min = 0;
		this.max = toSetLocations.size();
		if (!toSetLocations.isEmpty()) {
			photoName = toSetLocations.get(0).getTitle();
		}
	}


	@Override
	public void execute(IProgressMonitor monitor) {
		if (!toSetLocations.isEmpty()) {
			try {
				setLocations(monitor);
			} catch (UserNotConfiguredException unc) {
				if (AuthDialog.show(PRIVACYLINK, PRIVACYMESSAGE,
						850, 800, true) == null) //TODO geo link?
					execute(monitor);
			} catch (ProtocolException e) {
				handleError(e);
			} catch (CommunicationException e) {
				handleError(e);
			}
		}
	}

	/**
	 * @param monitor
	 * @throws ProtocolException
	 * @throws CommunicationException
	 */
	private void setLocations(IProgressMonitor monitor)
			throws ProtocolException, CommunicationException {
		for (ImageAttributes img : toSetLocations) {
			photoName = img.getTitle();
			monitor.worked(1);
			new SetLocation(img, session).execute();
		}
	}

	public boolean hasPhotosToGeoTag() {
		return !toSetLocations.isEmpty();
	}


	@Override
	public String getMessage() {
		return NLS.bind(Messages.SetLocationTask_adding_location,photoName);
	}


	@Override
	public String getTitle() {
		return NLS.bind(Messages.SetLocationTask_adding_locations, toSetLocations.size());
	}

}
