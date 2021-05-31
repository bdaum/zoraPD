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
 * (c) 2009-2019 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.hover;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.ImageRegion;

@SuppressWarnings("restriction")
public class HoverInfo implements IHoverInfo, IHoverContext {

	private static final String ITEM_PHOTO = "com.bdaum.zoom.ui.hover.galleryItem.photo"; //$NON-NLS-1$
	private Object object;
	private ImageRegion[] regions;
	private String info, title;

	/**
	 * @param object
	 *            - asset, list of assets
	 * @param regions
	 *            - regionIds
	 */
	public HoverInfo(Object object, ImageRegion[] regions) {
		this.object = object;
		this.regions = regions;
	}

	/**
	 * @param object
	 *            - asset, list of assets
	 * @param tooltip
	 *            - hover text
	 */
	public HoverInfo(Object object, String tooltip) {
		this.object = object;
		info = tooltip;
	}

	/**
	 * @param object
	 *            - asset, list of assets
	 * @param tooltip
	 *            - hover text
	 */
	public HoverInfo(Object object, String title, String tooltip) {
		this.object = object;
		this.title = title;
		info = tooltip;
	}

	/**
	 * @param tooltip
	 *            - hover text
	 */
	public HoverInfo(String tooltip) {
		info = tooltip;
		this.object = tooltip;
	}

	@Override
	public String getText() {
		if (info == null) {
			String hoverId = computeHoverId();
			if (hoverId != null)
				info = UiActivator.getDefault().getHoverManager().getHoverText(hoverId, object, this); // $NON-NLS-1$
		}
		return info;
	}

	@SuppressWarnings("unchecked")
	private String computeHoverId() {
		String hoverId = null;
		if (object instanceof Asset || object instanceof List<?>) {
			if (object instanceof Asset) {
				IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(((Asset) object).getFormat());
				hoverId = mediaSupport != null ? mediaSupport.getGalleryHoverId() : ITEM_PHOTO;
			} else
				for (Asset asset : (List<Asset>) object) {
					IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
					String id = mediaSupport != null ? mediaSupport.getGalleryHoverId() : ITEM_PHOTO;
					if (hoverId == null)
						hoverId = id;
					else if (!hoverId.equals(id))
						hoverId = ITEM_PHOTO;
				}
		}
		return hoverId;
	}

	private String compileOrigin() {
		IPeerService peerService = Core.getCore().getPeerService();
		if (peerService != null) {
			AssetOrigin assetOrigin = peerService.getAssetOrigin(((Asset) object).getStringId());
			if (assetOrigin != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(Messages.HoverInfo_origin);
				String peer = assetOrigin.getLocation();
				if (!peerService.isLocal(peer))
					sb.append(peer).append(", "); //$NON-NLS-1$
				sb.append(assetOrigin.getCatFile().getName()).append('\n');
				return sb.toString();
			}
		}
		return null;
	}

	private static String compileRegionData(ImageRegion[] imageRegions) {
		if (imageRegions != null) {
			LinkedList<String> lines = new LinkedList<String>();
			IDbManager dbManager = Core.getCore().getDbManager();
			for (ImageRegion imageRegion : imageRegions) {
				List<RegionImpl> regions = Core.getCore().getDbManager().obtainObjects(RegionImpl.class, false,
						"asset_person_parent", //$NON-NLS-1$
						imageRegion.owner.getStringId(), QueryField.EQUALS, Constants.OID, imageRegion.regionId,
						QueryField.EQUALS);
				if (!regions.isEmpty()) {
					Region region = regions.get(0);
					String albumId = region.getAlbum();
					String name = "?"; //$NON-NLS-1$
					String description = ""; //$NON-NLS-1$
					if (albumId != null) {
						SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, albumId);
						if (album != null) {
							name = album.getName();
							if (album.getDescription() != null && !album.getDescription().equals(name))
								description = album.getDescription();
						}
					}
					if (region.getDescription() != null && !region.getDescription().isEmpty()) {
						if (!description.isEmpty())
							description += "; "; //$NON-NLS-1$
						description += region.getDescription();
					}
					StringBuilder lsb = new StringBuilder();
					lsb.append(name);
					if (!description.isEmpty() && !(description.endsWith(name)
							&& description.charAt(description.length() - name.length() - 2) == ':'))
						lsb.append(" - ").append(description); //$NON-NLS-1$
					String line = lsb.toString();
					lines.remove(line);
					lines.add(line);
				}
				Collections.sort(lines);
			}
			if (!lines.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String line : lines)
					sb.append('[').append(line).append("]\n"); //$NON-NLS-1$
				return sb.toString();
			}
		}
		return null;
	}

	@Override
	public String getTitle() {
		if (title == null) {
			String hoverId = computeHoverId();
			if (hoverId != null)
				return UiActivator.getDefault().getHoverManager().getHoverTitle(hoverId, object, this);
		}
		return title;
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public ImageRegion[] getRegions() {
		return regions;
	}

	@Override
	public String getValue(String tv, Object object) {
		if (Constants.HV_ORIGIN.equals(tv))
			return compileOrigin();
		if (Constants.HV_IMAGE_REGIONS.equals(tv))
			return compileRegionData(regions);
		return null;
	}

}