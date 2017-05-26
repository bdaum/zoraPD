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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.ImageUploadApi;

import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.flickr4java.flickr.FlickrException;

public class FlickrErrorHandler implements IErrorHandler {

	private static final String FLICKRGEOURI = "https://www.flickr.com/account/geo/privacy/"; //$NON-NLS-1$

	public void handleError(Object source, final Exception e) {
		IStatus aStatus = null;
		Throwable cause = e.getCause();
		if (cause instanceof FlickrException) {
			FlickrException fe = (FlickrException) cause;
			String errorCode = fe.getErrorCode();
			String msg = null;
			if ("1".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages.getString("FlickrErrorHandler.user_not_found"); //$NON-NLS-1$
			} else if ("3".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages.getString("FlickrErrorHandler.upload_error"); //$NON-NLS-1$
			} else if ("4".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages.getString("FlickrErrorHandler.filesize_zero"); //$NON-NLS-1$
			} else if ("5".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages
						.getString("FlickrErrorHandler.wrong_image_format"); //$NON-NLS-1$
			} else if ("6".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages
						.getString("FlickrErrorHandler.bandwidth_exceeded"); //$NON-NLS-1$
			} else if ("98".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages
						.getString("FlickrErrorHandler.wrong_login_details"); //$NON-NLS-1$
			} else if ("99".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages
						.getString("FlickrErrorHandler.user_auth_required"); //$NON-NLS-1$
			} else if ("105".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages
						.getString("FlickrErrorHandler.service_unavailable"); //$NON-NLS-1$
			} else if ("106".equals(errorCode)) { //$NON-NLS-1$
				msg = Messages.getString("FlickrErrorHandler.write_failed"); //$NON-NLS-1$
			} else if ("7".equals(errorCode)) { //$NON-NLS-1$
				msg = NLS.bind(Messages
						.getString("FlickrErrorHandler.viewing_settings_info"), //$NON-NLS-1$
						FLICKRGEOURI);
			}
			if (msg != null)
				aStatus = new Status(IStatus.ERROR,
						CommunitiesActivator.PLUGIN_ID, msg);
			else
				aStatus = new Status(
						IStatus.ERROR,
						CommunitiesActivator.PLUGIN_ID,
						Messages.getString("FlickrErrorHandler.flickr_exception"), cause); //$NON-NLS-1$
		}
		if (aStatus == null)
			aStatus = new Status(
					IStatus.ERROR,
					CommunitiesActivator.PLUGIN_ID,
					Messages.getString("FlickrErrorHandler.communication_exception"), e); //$NON-NLS-1$
		if (source instanceof ImageUploadApi) {
			MultiStatus mStatus = ((ImageUploadApi) source).getStatus();
			if (mStatus != null) {
				mStatus.add(aStatus);
				return;
			}
		}
		CommunitiesActivator.getDefault().getLog().log(aStatus);
	}
}
