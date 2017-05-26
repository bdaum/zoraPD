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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.juploadr.uploadapi.locrrest.info;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.scohen.juploadr.uploadapi.ImageInfo;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;
import com.bdaum.zoom.net.communities.PhotoNotFoundException;

public class LocrInfo extends LocrMethod {

	private final String photoId;
	private ImageInfo imageInfo = new LocrImageInfo();

	public LocrInfo(String photoId, Session session) {
		super(session);
		this.photoId = photoId;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("method", "get_photos_xml.php?"); //$NON-NLS-1$//$NON-NLS-2$
		params.put("photo_id", photoId); //$NON-NLS-1$
		return params;
	}

	
	@Override
	protected ProtocolException buildProtocolException(int errorCode) {
		switch (errorCode) {
		case 1:
			imageInfo = null;
			return new PhotoNotFoundException();
		default:
			return null;
		}
	}

	
	@Override
	public DefaultLocrHandler getResponseHandler() {
		return new LocrInfoHandler(imageInfo);
	}

	
	@Override
	public boolean isAuthorized() {
		return true;
	}

	public String getErrorText() {
		return "get Info for " + photoId; //$NON-NLS-1$
	}

	public ImageInfo getImageInfo() {
		return imageInfo;
	}

}
