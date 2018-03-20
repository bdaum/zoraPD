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

package com.bdaum.juploadr.uploadapi.smugrest.photosets;

import java.util.Comparator;

import org.scohen.juploadr.app.AbstractPhotoSet;
import org.scohen.juploadr.app.Category;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;

public class SmugmugPhotoSet extends AbstractPhotoSet {

	public Category category;
	public Category subcategory;
	private String secret;

	public SmugmugPhotoSet() {
	}

	public SmugmugPhotoSet(String name, String description) {
		this.title = name;
		this.description = description;
	}

	/**
	 * @return Returns the secret.
	 */
	
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret
	 *            The secret to set.
	 */
	
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return Returns the server.
	 */
	
	public String getServer() {
		return null;
	}

	public class TitleComparator implements Comparator<PhotoSet> {

		
		public int compare(PhotoSet p1, PhotoSet p2) {
			return p1.getTitle().compareTo(p2.getTitle());
		}

	}

	
	@Override
	public String toString() {
		return new StringBuffer("SmugmugPhotoSet").append(" ") //$NON-NLS-1$//$NON-NLS-2$
				.append(getTitle()).append(" new?").append(isNew()).toString(); //$NON-NLS-1$
	}

	
	public void setServer(String server) {
		// do nothing
	}

	
	@Override
	public String getTitle() {
		return title;
	}

	
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	
	@Override
	public boolean isUnsafe(ImageAttributes imageAttributes) {
		int safety = imageAttributes.getAsset().getSafety();
		return (publicAlbum && safety > QueryField.SAFETY_SAFE && safety < QueryField.SAFETY_RESTRICTED);
	}

	
	@Override
	public int match(ImageAttributes image, boolean privacy) {
		if (privacy && isUnsafe(image))
			return -1;
		int score = -1;
		Asset asset = image.getAsset();
		if (category != null) {
			String cattit = category.getTitle();
			if (cattit != null && cattit.equals(asset.getCategory())) {
				score = 0;
				if (subcategory != null) {
					String subtit = subcategory.getTitle();
					if (subtit != null && asset.getSupplementalCats() != null) {
						for (String sub : asset.getSupplementalCats()) {
							if (subtit.equals(sub)) {
								score = 1;
								break;
							}
						}
					}
				}
			}
		}
		for (String album : asset.getAlbum())
			if (album.equals(title))
				return score + 1;
		return score;
	}

	
	@Override
	public Category getCategory() {
		return category;
	}

	
	@Override
	public void setCategory(Category category) {
		this.category = category;
	}

	
	@Override
	public Category getSubcategory() {
		return subcategory;
	}

	
	@Override
	public void setSubcategory(Category subcategory) {
		this.subcategory = subcategory;
	}
}
