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

package com.bdaum.juploadr.uploadapi.smugrest.urls;

import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.DefaultSmugmugHandler;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugMethod;
import com.bdaum.juploadr.uploadapi.smugrest.SmugmugRestApi;

public class GetUserPhotos extends SmugmugMethod {
	public GetUserPhotos(Session session) {
		super(session);
	}

	private URL url;

	
	@Override
	public SortedMap<String, String> getParams() {
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", SmugmugRestApi.SMUGMUG_API_KEY); //$NON-NLS-1$
		params.put("method", "smugmug.urls.getUserPhotos"); //$NON-NLS-1$ //$NON-NLS-2$
		return params;
	}

	
	@Override
	public DefaultSmugmugHandler getResponseHandler() {

		return new GetUserPhotosHandler(this);
	}

	
	@Override
	public boolean isAuthorized() {
		return true;
	}

	void setURL(URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

}
