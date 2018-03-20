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

package com.bdaum.juploadr.uploadapi.locrrest.albums;

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.tags.TagParser;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;

public class CreateAlbum extends LocrMethod {

	private final LocrPhotoSet photoSet;

	public CreateAlbum(PhotoSet photoSet, Session session) {
		super(session);
		this.photoSet = (LocrPhotoSet) photoSet;
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("method", "create_album_xml.php?"); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("name", photoSet.getTitle()); //$NON-NLS-1$
		if (photoSet.getDescription() != null
				&& !photoSet.getDescription().isEmpty()) {
			params.put("description", photoSet.getDescription()); //$NON-NLS-1$
		}
		if (photoSet.getKeywords() != null && !photoSet.getKeywords().isEmpty()) {
			TagParser tagParser = new TagParser(',');
			params.put(
					"tags", tagParser.toRequestString(photoSet.getKeywords())); //$NON-NLS-1$
		}
		return params;
	}

	
	@Override
	public DefaultLocrHandler getResponseHandler() {
		return new CreateAlbumResponseHandler(photoSet);
	}

	
	@Override
	public boolean isAuthorized() {
		return true;
	}

}
