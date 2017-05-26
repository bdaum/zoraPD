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

package org.scohen.juploadr.app;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.scohen.juploadr.app.geo.GeoLocation;
import org.scohen.juploadr.app.tags.Tag;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.ui.AlbumDescriptor;

/**
 * @author steve
 *
 */
public class ImageAttributes implements UploadImage {

	private int rank;
	private int state = STATE_NEW;
	private Set<Tag> tags;

	private boolean publiclyVisible;
	private boolean friendViewable;
	private boolean familyViewable;
	private boolean selected = false;
	private String key;
	private List<PhotoSet> photoSets = new ArrayList<PhotoSet>();
	private String photoId;
	// private ImageData imageData;
	private final Asset asset;
	private final File file;
	private String title;
	private String description;
	private GeoLocation location;
	private GeoLocation objectLocation;
	private final boolean rescaled;
	private final Session session;
	private String url;
	private String secret;

	public ImageAttributes(Session session, Asset asset,
			AlbumDescriptor associatedAlbum, File file, boolean rescaled) {
		this.session = session;
		this.asset = asset;
		this.file = file;
		this.rescaled = rescaled;
		CommunityAccount current = session.getAccount();

		// set up defaults
		if ("public".equals(current.getAccessType())) { //$NON-NLS-1$
			publiclyVisible = true;
		} else {
			publiclyVisible = false;
		}
		if (current.hasAccessDetails("friends")) { //$NON-NLS-1$
			friendViewable = true;
		}
		if (current.hasAccessDetails("family")) { //$NON-NLS-1$
			familyViewable = true;
		}
		switch (asset.getSafety()) {
		case QueryField.SAFETY_MODERATE:
			publiclyVisible = false;
			break;
		case QueryField.SAFETY_RESTRICTED:
			publiclyVisible = false;
			friendViewable = false;
			familyViewable = false;
			break;
		}
		tags = new LinkedHashSet<Tag>(current.getDefaultTags());
		if (current.isKeywordsAsTags()) {
			FilterChain keywordFilter = QueryField.getKeywordFilter();
			for (String kw : asset.getKeyword()) {
				if (keywordFilter.accept(kw))
					tags.add(new Tag(kw));
			}
		}

		if (associatedAlbum != null)
			addToPhotoset(current, associatedAlbum.getName(),
					associatedAlbum.getDescription(), associatedAlbum.getTags());
		else if (current.isAlbumsAsSets())
			for (String album : asset.getAlbum())
				addToPhotoset(current, album, null, null);
		key = file.getAbsolutePath();
	}

	private void addToPhotoset(CommunityAccount current,
			String album, String descr, String[] newTags) {
		List<PhotoSet> sitePhotoSets = current.getPhotosets();
		PhotoSet set = null;
		for (PhotoSet photoSet : sitePhotoSets) {
			if (album.equals(photoSet.getTitle())) {
				set = photoSet;
				break;
			}
		}
		if (set == null) {
			set = ((ImageUploadApi) session.getApi()).newPhotoSet(album, descr, newTags, this);
			sitePhotoSets.add(set);
		}
		if (set != null) {
			photoSets.add(set);
			set.addPhoto(this);
		}
	}

	/**
	 * @return Returns the description.
	 */

	public String getDescription() {
		if (description != null)
			return description;
		return asset.getImageDescription();
	}

	public boolean hasDescription() {
		return getDescription() != null && getDescription().trim().length() > 0;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the tags.
	 */

	public Set<Tag> getTags() {
		return tags;
	}

	public boolean hasTags() {
		return tags.size() > 0;
	}

	/**
	 * @param tags
	 *            The tags to set.
	 */

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	/**
	 * @return Returns the title.
	 */

	public String getTitle() {
		return (title != null && title.length() > 0) ? title
				: (String) QueryField.TITLEORNAME.obtainFieldValue(asset);
	}

	public boolean hasTitle() {
		return ((title != null && title.length() > 0) || (asset.getTitle() != null && asset
				.getTitle().length() > 0));
	}

	/**
	 * @param title
	 *            The title to set.
	 */

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the imagePath.
	 */

	public String getImagePath() {
		return file.getAbsolutePath();
	}

	public String getKey() {
		return key;
	}

	public boolean isRescaled() {
		return rescaled;
	}

	/**
	 * @return Returns the state.
	 */

	public int getState() {
		return state;
	}

	/**
	 * @param state
	 *            The state to set.
	 */

	public void setState(int state) {
		if (state == STATE_UPLOADED) {
			rank -= 100;
		}
		this.state = state;
	}

	/**
	 * @return Returns the familyViewable.
	 */

	public boolean isFamilyViewable() {
		return familyViewable;
	}

	/**
	 * @param familyViewable
	 *            The familyViewable to set.
	 */

	public void setFamilyViewable(boolean familyViewable) {
		this.familyViewable = familyViewable;
	}

	/**
	 * @return Returns the friendViewable.
	 */

	public boolean isFriendViewable() {
		return friendViewable;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void toggleSelected() {
		selected = !selected;
	}

	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param friendViewable
	 *            The friendViewable to set.
	 */

	public void setFriendViewable(boolean friendViewable) {
		this.friendViewable = friendViewable;
	}

	/**
	 * @return Returns the publiclyVisible.
	 */

	public boolean isPubliclyVisible() {
		return publiclyVisible;
	}

	/**
	 * @param publiclyVisible
	 *            The publiclyVisible to set.
	 */

	public void setPubliclyVisible(boolean publiclyVisible) {
		this.publiclyVisible = publiclyVisible;
	}

	/**
	 * @return Returns the rank.
	 */

	public int getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            The rank to set.
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	public void dispose() {
		// do nothing
	}

	public void addPhotoSet(PhotoSet set) {
		if (!photoSets.contains(set)) {
			photoSets.add(set);
		}
	}

	public void removePhotoSet(PhotoSet set) {
		if (photoSets.contains(set)) {
			photoSets.remove(set);
		}
	}

	public List<? extends PhotoSet> getPhotoSets() {
		return photoSets;
	}

	public void setPhotoId(String id) {
		photoId = id;
	}

	public String getPhotoId() {
		return photoId;
	}

	/**
	 * @return Returns the rotation.
	 */

	public int getRotation() {
		return asset.getRotation();
	}

	public GeoLocation getLocation() {
		if (location != null)
			return location;
		if (!Double.isNaN(asset.getGPSLongitude())
				&& !Double.isNaN(asset.getGPSLatitude()))
			return new GeoLocation(getTitle(), asset.getGPSLatitude(),
					asset.getGPSLongitude());
		return null;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	public Asset getAsset() {
		return asset;
	}

	public GeoLocation getObjectLocation() {
		if (objectLocation != null)
			return objectLocation;
		IDbManager dbManager = Core.getCore().getDbManager();
		List<LocationShownImpl> relations = dbManager.obtainStructForAsset(
				LocationShownImpl.class, asset.getStringId(), false);
		for (LocationShownImpl rel : relations) {
			LocationImpl loc = dbManager.obtainById(LocationImpl.class,
					rel.getLocation());
			if (loc != null) {
				if (!Double.isNaN(loc.getLatitude())
						&& !Double.isNaN(loc.getLongitude())) {
					return new GeoLocation(loc.getCity(), loc.getLatitude(),
							loc.getLongitude());
				}
			}
		}
		return null;
	}

	public void setObjectLocation(GeoLocation objectLocation) {
		this.objectLocation = objectLocation;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getUrl() {
		return url;
	}

	public String getSecret() {
		return secret;
	}

}