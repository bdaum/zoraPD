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
package com.bdaum.zoom.video.internal;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.ui.internal.hover.AbstractHoverContribution;
import com.bdaum.zoom.ui.internal.hover.HoverTestAsset;
import com.bdaum.zoom.ui.internal.hover.IHoverContext;
import com.bdaum.zoom.ui.internal.hover.IHoverItem;

@SuppressWarnings("restriction")
public class VideoHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String ORIGIN = Constants.HV_ORIGIN;
	private static final String METADATA = Constants.HV_METADATA;

	private static final String[] TITLETAGS = new String[] {};
	private static final String[] TITLELABELS = new String[] {};
	private static final String[] TAGS = new String[] { ORIGIN, METADATA };
	private static final String[] LABELS = new String[] { Messages.VideoHoverContribution_network_orig,
			Messages.VideoHoverContribution_meta_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.VideoHoverContribution_gallery;
	}

	@Override
	public String getName() {
		return Messages.VideoHoverContribution_videos;
	}

	@Override
	public String getDescription() {
		return Messages.VideoHoverContribution_videos_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return "{origin}{metadata}"; //$NON-NLS-1$
	}

	@Override
	public String[] getItemKeys() {
		return TAGS;
	}

	@Override
	public IHoverItem getHoverItem(String itemkey) {
		return this;
	}

	@Override
	public String[] getTitleItemKeys() {
		return TITLETAGS;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return "{meta=name}"; //$NON-NLS-1$
	}

	@Override
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestAsset) {
			if (ORIGIN.equals(key) && Core.getCore().getPeerService() != null)
				return new StringBuilder().append(Messages.VideoHoverContribution_origin)
						.append(Core.getCore().getDbManager().getFile().getName()).append('\n').toString();
			return null;
		}
		if (object instanceof Asset && ORIGIN.equals(key))
			return context.getValue(key, object);
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
			return IMediaSupport.VIDEO;
		return super.getMediaFlags(object);
	}

	@Override
	public String[] getTitleItemLabels() {
		return TITLELABELS;
	}

	@Override
	public String[] getItemLabels() {
		return LABELS;
	}

}
