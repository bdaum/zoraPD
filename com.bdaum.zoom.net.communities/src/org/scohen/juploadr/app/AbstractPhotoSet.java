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

package org.scohen.juploadr.app;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPhotoSet implements Comparable<PhotoSet>,
		PhotoSet {

	private String id;
	private int numberOfPhotos;
	protected String title;
	protected String description;
	private boolean New = true;
	private String primaryPhotoId;
	private String url;
	public ImageAttributes primaryPhoto;
	private List<ImageAttributes> photos = new ArrayList<ImageAttributes>();
	protected boolean publicAlbum = true;

	
	public String getDescription() {
		return description;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	
	public String getId() {
		return id;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	
	public int getNumberOfPhotos() {
		return numberOfPhotos;
	}

	
	public void setNumberOfPhotos(int numberOfPhotos) {
		this.numberOfPhotos = numberOfPhotos;
	}

	
	public String getTitle() {
		return title;
	}

	
	public void setTitle(String title) {
		this.title = title;
	}

	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractPhotoSet) {
			PhotoSet p = (PhotoSet) o;
			if (id != null) {
				return id.equals(p.getId());
			}
			return title.equals(p.getTitle());
		}
		return false;
	}

	
	@Override
	public int hashCode() {
		if (id != null)
			return id.hashCode();
		return title.hashCode();
	}

	
	public boolean isNew() {
		return New;
	}

	
	public void setNew(boolean new1) {
		New = new1;
	}

	
	public int compareTo(PhotoSet p1) {
		return title.compareToIgnoreCase(p1.getTitle());
	}

	
	@Override
	public String toString() {
		return new StringBuffer("FlickrPhotoSet").append(" ") //$NON-NLS-1$//$NON-NLS-2$
				.append(getTitle()).append(" new?").append(isNew()).toString(); //$NON-NLS-1$
	}

	
	public void setPrimaryPhotoId(String primaryPhotoId) {
		this.primaryPhotoId = primaryPhotoId;
	}

	
	public String getUrl() {
		return url;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}

	
	public void setPrimaryPhoto(ImageAttributes primaryPhoto) {
		this.primaryPhoto = primaryPhoto;
	}

	
	public String getPrimaryPhotoId() {
		if (primaryPhoto != null && primaryPhoto.getPhotoId() != null) {
			return primaryPhoto.getPhotoId();

		}
		return primaryPhotoId;
	}

	
	public ImageAttributes getPrimaryPhoto() {
		return primaryPhoto;
	}

	
	public List<ImageAttributes> getPhotos() {
		return photos;
	}

	
	public void addPhoto(ImageAttributes atts) {
		if (!photos.contains(atts)) {
			atts.addPhotoSet(this);
			photos.add(atts);
		}
	}

	
	public void removePhoto(ImageAttributes atts) {
		if (photos.contains(atts)) {
			photos.remove(atts);
			atts.removePhotoSet(this);
		}
	}

	
	public boolean hasPhoto(ImageAttributes photo) {
		return photos.contains(photo);
	}

	
	public int match(ImageAttributes image, boolean privacy) {
		return -1;
	}

	
	public Category getCategory() {
		return null;
	}

	
	public void setCategory(Category category) {
		// do nothing
	}

	
	public Category getSubcategory() {
		return null;
	}

	
	public void setSubcategory(Category subcategory) {
		// do nothing
	}

	
	public void setPublicAlbum(boolean publicAlbum) {
		this.publicAlbum = publicAlbum;
	}

	
	public boolean isPublicAlbum() {
		return publicAlbum;
	}

	
	public boolean isUnsafe(ImageAttributes imageAttributes) {
		return false;
	}
}