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
 * Modifications (c) 2009 Berthold Daum  
 */

package org.scohen.juploadr.uploadapi;

import java.util.ArrayList;
import java.util.List;

import org.scohen.juploadr.app.tags.Tag;

public class ImageInfo {

	private String title;
	private String description;
	private List<String> urls;
	private boolean pub;
	private List<Tag> tags;

	public void setPublic(boolean pub) {
		this.pub = pub;
	}

	public void addTag(String tag) {
		if (tags == null)
			tags = new ArrayList<Tag>();
		tags.add(new Tag(tag));
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addUrl(String url) {
		if (urls == null)
			urls = new ArrayList<String>();
		urls.add(url);
	}

	public List<String> getUrls() {
		return urls;
	}

	public boolean isPub() {
		return pub;
	}

	public void setPub(boolean pub) {
		this.pub = pub;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

}
