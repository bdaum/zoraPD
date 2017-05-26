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


package com.bdaum.zoom.flickrrest.internal.photosets;

import java.util.Comparator;

import org.scohen.juploadr.app.AbstractPhotoSet;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;

public class FlickrPhotoSet extends AbstractPhotoSet  {

	public class TitleComparator implements Comparator<PhotoSet> {
		public TitleComparator() {
		}

		public int compare(PhotoSet o1, PhotoSet o2) {
			return o1.getTitle().compareTo(o2.getTitle());
		}
	}

	private String secret;
	private String server;

	public FlickrPhotoSet() {
	}

	public FlickrPhotoSet(String name, String description,
			ImageAttributes primary) {
		this.title = name;
		this.description = description;
		this.primaryPhoto = primary;
		addPhoto(primary);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.scohen.juploadr.uploadapi.flickrrest.photosets.PhotoSet#getSecret()
	 */

	public String getSecret() {
		return secret;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.scohen.juploadr.uploadapi.flickrrest.photosets.PhotoSet#setSecret
	 * (java.lang.String)
	 */

	public void setSecret(String secret) {
		this.secret = secret;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.scohen.juploadr.uploadapi.flickrrest.photosets.PhotoSet#getServer()
	 */

	public String getServer() {
		return server;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.scohen.juploadr.uploadapi.flickrrest.photosets.PhotoSet#setServer
	 * (java.lang.String)
	 */

	public void setServer(String server) {
		this.server = server;
	}




}
