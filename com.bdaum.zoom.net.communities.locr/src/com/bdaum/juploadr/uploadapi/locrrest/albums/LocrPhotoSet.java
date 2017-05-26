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
package com.bdaum.juploadr.uploadapi.locrrest.albums;

import java.util.ArrayList;
import java.util.List;

import org.scohen.juploadr.app.AbstractPhotoSet;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.tags.Tag;

public class LocrPhotoSet extends AbstractPhotoSet {

	private List<Tag> keywords;

	public LocrPhotoSet(String name, String description, String[] keywords,
			ImageAttributes primaryPhoto) {
		this.primaryPhoto = primaryPhoto;
		this.description = description;
		this.title = name;
		addPhoto(primaryPhoto);
		if (keywords != null) {
			this.keywords = new ArrayList<Tag>();
			for (String kw : keywords) {
				this.keywords.add(new Tag(kw));
			}
		}
	}


	public LocrPhotoSet() {
	}


	
	public String getSecret() {
		// do nothing
		return null;
	}

	
	public String getServer() {
		// do nothing
		return null;
	}



	
	public void setSecret(String secret) {
		// do nothing
	}

	
	public void setServer(String server) {
		// do nothing

	}


	public List<Tag> getKeywords() {
		return keywords;
	}


	public void setKeywords(List<Tag> keywords) {
		this.keywords = keywords;
	}



}
