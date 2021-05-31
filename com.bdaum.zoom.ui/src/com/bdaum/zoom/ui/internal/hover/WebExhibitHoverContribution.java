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

import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;

public class WebExhibitHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String TITLE = Constants.HV_TITLE;
	private static final String TOTAL = Constants.HV_TOTAL;
	private static final String STORYBOARD = Constants.HV_STORYBOARD;
	private static final String STORYBOARDPOS = Constants.HV_STORYBOARDPOS;
	private static final String METADATA = Constants.HV_METADATA;
	private static final String[] TITLETAGS = new String[] { TITLE, TOTAL, STORYBOARD, STORYBOARDPOS };
	private static final String[] TITLELABELS = new String[] { Messages.WebExhibitHoverContribution_title,
			Messages.WebExhibitHoverContribution_total_number, Messages.WebExhibitHoverContribution_storyboard_title,
			Messages.WebExhibitHoverContribution_storyboard_pos };
	private static final String[] TAGS = new String[] { TITLE, TOTAL, STORYBOARD, STORYBOARDPOS, METADATA };
	private static final String[] LABELS = new String[] { Messages.WebExhibitHoverContribution_title,
			Messages.WebExhibitHoverContribution_total_number, Messages.WebExhibitHoverContribution_storyboard_title,
			Messages.WebExhibitHoverContribution_storyboard_pos, Messages.WebExhibitHoverContribution_metadata_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.WebExhibitHoverContribution_presentation;
	}

	@Override
	public String getName() {
		return Messages.WebExhibitHoverContribution_webgallery_exh;
	}

	@Override
	public String getDescription() {
		return Messages.WebExhibitHoverContribution_hover_webexhibits;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.WebExhibitHoverContribution_dflt_template;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return "{title}"; //$NON-NLS-1$
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
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestObject) {
			if (TITLE.equals(key))
				return Messages.WebExhibitHoverContribution_my_best_image;
			if (STORYBOARDPOS.equals(key))
				return "5"; //$NON-NLS-1$
			if (TOTAL.equals(key))
				return "42"; //$NON-NLS-1$
			if (STORYBOARD.equals(key))
				return Messages.WebExhibitHoverContribution_roadtrip;
		} else if (object instanceof WebExhibit) {
			if (TITLE.equals(key))
				return ((WebExhibit) object).getCaption();
			if (STORYBOARDPOS.equals(key))
				return String.valueOf(((WebExhibit) object).getSequenceNo());
			if (TOTAL.equals(key) || STORYBOARD.equals(key))
				return context.getValue(key, object);
		}
		return null;
	}

	@Override
	public Object getTarget(Object object) {
		if (object instanceof HoverTestObject)
			return new HoverTestAsset();
		if (object instanceof WebExhibit)
			return Core.getCore().getDbManager().obtainAsset(((WebExhibit) object).getAsset());
		return null;
	}
}
