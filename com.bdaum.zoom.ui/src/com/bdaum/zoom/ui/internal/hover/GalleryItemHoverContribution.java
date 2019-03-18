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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.hover;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.peer.IPeerService;

@SuppressWarnings("restriction")
public class GalleryItemHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String IMAGE_REGIONS = Constants.HV_IMAGE_REGIONS;
	private static final String ORIGIN = Constants.HV_ORIGIN;
	private static final String METADATA = Constants.HV_METADATA;

	private static final String[] TITLETAGS = new String[] {};
	private static final String[] TITLELABELS = new String[] {};

	private static final String[] TAGS = new String[] { ORIGIN, IMAGE_REGIONS, METADATA };
	private static final String[] LABELS = new String[] { Messages.GalleryItemHoverContribution_network_orig,
			Messages.GalleryItemHoverContribution_image_regions, Messages.GalleryItemHoverContribution_meta_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.GalleryItemHoverContribution_gallery;
	}

	@Override
	public String getName() {
		return Messages.GalleryItemHoverContribution_images;
	}

	@Override
	public String getDescription() {
		return Messages.GalleryItemHoverContribution_gallery_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return "{origin}?  {imageRegions}{metadata}"; //$NON-NLS-1$
	}

	@Override
	public String[] getItemKeys() {
		return TAGS;
	}

	@Override
	public String[] getTitleItemLabels() {
		return TITLELABELS;
	}

	@Override
	public String[] getItemLabels() {
		return LABELS;
	}

	@Override
	public String[] getTitleItemKeys() {
		return TITLETAGS;
	}

	@Override
	public IHoverItem getHoverItem(String itemkey) {
		return this;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return "{meta=name}"; //$NON-NLS-1$
	}

	@Override
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestAsset) {
			if (ORIGIN.equals(key)) { // $NON-NLS-1$
				IPeerService peerService = Core.getCore().getPeerService();
				if (peerService != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(Messages.GalleryItemHoverContribution_origin);
					sb.append(Core.getCore().getDbManager().getFile().getName());
					sb.append('\n');
					return sb.toString();
				}
			} else if (IMAGE_REGIONS.equals(key)) // $NON-NLS-1$
				return "[Tina Turner]\n"; //$NON-NLS-1$
		} else if (object instanceof Asset) {
			if (ORIGIN.equals(key))
				return context.getValue(key, object);
			if (IMAGE_REGIONS.equals(key))
				return context.getValue(key, object);
		}
		return null;
	}

	@Override
	public Object getTarget(Object object) {
		return object;
	}

	@Override
	public Object getTestObject() {
		return new HoverTestAsset();
	}

	@Override
	public int getMediaFlags(Object object) {
		if (object instanceof HoverTestAsset)
			return IMediaSupport.PHOTO;
		return super.getMediaFlags(object);
	}
}
