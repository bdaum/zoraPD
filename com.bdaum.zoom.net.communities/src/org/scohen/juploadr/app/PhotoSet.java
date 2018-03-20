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

import java.util.List;


public interface PhotoSet extends Comparable<PhotoSet> {

    /**
     * @return Returns the description.
     */
    public String getDescription();

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description);

    /**
     * @return Returns the id.
     */
    public String getId();

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id);

    /**
     * @return Returns the numberOfPhotos.
     */
    public int getNumberOfPhotos();

    /**
     * @param numberOfPhotos
     *            The numberOfPhotos to set.
     */
    public void setNumberOfPhotos(int numberOfPhotos);

    /**
     * @return Returns the secret.
     */
    public String getSecret();

    /**
     * @param secret
     *            The secret to set.
     */
    public abstract void setSecret(String secret);

    /**
     * @return Returns the server.
     */
    public abstract String getServer();

    /**
     * @param server
     *            The server to set.
     */
    public abstract void setServer(String server);

    /**
     * @return Returns the title.
     */
    public abstract String getTitle();

    /**
     * @param title
     *            The title to set.
     */
    public abstract void setTitle(String title);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
	public abstract boolean equals(Object o);

    /**
     * @return Returns the new.
     */
    public abstract boolean isNew();

    /**
     * @param new1
     *            The new to set.
     */
    public abstract void setNew(boolean new1);

    /**
     * @return Returns the primaryPhoto.
     */
    public abstract ImageAttributes getPrimaryPhoto();

    /**
     * @param primaryPhoto
     *            The primaryPhoto to set.
     */
    public void setPrimaryPhoto(ImageAttributes primaryPhoto);

    /**
     * @return Returns the primaryPhotoId.
     */
    public String getPrimaryPhotoId();

    /**
     * @param primaryPhotoId
     *            The primaryPhotoId to set.
     */
    public void setPrimaryPhotoId(String primaryPhotoId);

    public List<ImageAttributes> getPhotos();

    public void addPhoto(ImageAttributes atts);

    public void removePhoto(ImageAttributes atts);

    /**
     * @return Returns the url.
     */
    public String getUrl();

    /**
     * @param url
     *            The url to set.
     */
    public void setUrl(String url);
    
    /**
     * Returns whether or not this set contains the photo specified.
     * @param photo
     * @return
     */
    public boolean hasPhoto(ImageAttributes photo);

	public int match(ImageAttributes image, boolean privacy);

	public Category getCategory();

	public void setCategory(Category category);

	public Category getSubcategory();

	public void setSubcategory(Category subcategory);

	public void setPublicAlbum(boolean publicAlbum);

	public boolean isPublicAlbum();

	public boolean isUnsafe(ImageAttributes imageAttributes);

}