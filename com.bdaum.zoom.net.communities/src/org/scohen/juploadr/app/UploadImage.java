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

import java.util.Set;

import org.scohen.juploadr.app.geo.GeoLocation;
import org.scohen.juploadr.app.tags.Tag;

/**
 * @author steve
 * 
 */
public interface UploadImage {
    public static final int STATE_UPLOADED = 1;

    public static final int STATE_DATA_ENTERED = 2;

    public static final int STATE_REMOVED = 4;

    public static final int STATE_UPLOADING = 8;

    public static final int STATE_NEW = 0;

    /**
     * @return Returns the description.
     */
    public abstract String getDescription();

    public abstract boolean hasDescription();

    /**
     * @return Returns the tags.
     */
    public abstract Set<Tag> getTags();

    public abstract boolean hasTags();

    /**
     * @param tags
     *            The tags to set.
     */
    public abstract void setTags(Set<Tag> tags);

    /**
     * @return Returns the title.
     */
    public abstract String getTitle();

    public abstract boolean hasTitle();

    /**
     * @param title
     *            The title to set.
     */
    public abstract void setTitle(String title);

    public abstract String getKey();

    /**
     * @return Returns the imagePath.
     */
    public abstract String getImagePath();

    /**
     * @return Returns the state.
     */
    public abstract int getState();

    /**
     * @param state
     *            The state to set.
     */
    public abstract void setState(int state);

    /**
     * @return Returns the familyViewable.
     */
    public abstract boolean isFamilyViewable();

    /**
     * @param familyViewable
     *            The familyViewable to set.
     */
    public abstract void setFamilyViewable(boolean familyViewable);

    /**
     * @return Returns the friendViewable.
     */
    public abstract boolean isFriendViewable();

    /**
     * @param friendViewable
     *            The friendViewable to set.
     */
    public abstract void setFriendViewable(boolean friendViewable);

    /**
     * @return Returns the publiclyVisible.
     */
    public abstract boolean isPubliclyVisible();

    /**
     * @param publiclyVisible
     *            The publiclyVisible to set.
     */
    public abstract void setPubliclyVisible(boolean publiclyVisible);

    public abstract boolean isRescaled();

    public abstract int getRank();

    public abstract String getPhotoId();

    public abstract int getRotation();

    public abstract void setLocation(GeoLocation loc);
    
    public abstract GeoLocation getLocation();
}