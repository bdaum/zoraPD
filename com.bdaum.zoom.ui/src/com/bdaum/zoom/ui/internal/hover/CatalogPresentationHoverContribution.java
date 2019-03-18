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

import java.util.List;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class CatalogPresentationHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String NAME = Constants.HV_NAME;
	private static final String TYPE = Constants.HV_TYPE;
	private static final String DESCRIPTION = Constants.HV_DESCRIPTION;
	private static final String STATISTICS = Constants.HV_STATISTICS;
	private static final String[] TAGS = new String[] { TYPE, NAME, DESCRIPTION, STATISTICS };
	private static final String[] LABELS = new String[] { Messages.CatalogPresentationHoverContribution_type,
			Messages.CatalogPresentationHoverContribution_name,
			Messages.CatalogPresentationHoverContribution_description,
			Messages.CatalogPresentationHoverContribution_statistics };

	@Override
	public boolean supportsTitle() {
		return false;
	}

	@Override
	public String getCategory() {
		return Messages.CatalogPresentationHoverContribution_catalog;
	}

	@Override
	public String getName() {
		return Messages.CatalogPresentationHoverContribution_presentation;
	}

	@Override
	public String getDescription() {
		return Messages.CatalogPresentationHoverContribution_presentation_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return "{type} {name}\n\n?{description}\n{statistics}"; //$NON-NLS-1$
	}

	@Override
	public String getDefaultTitleTemplate() {
		return null;
	}

	@Override
	public String[] getItemKeys() {
		return TAGS;
	}

	@Override
	public String[] getTitleItemLabels() {
		return null;
	}

	@Override
	public String[] getItemLabels() {
		return LABELS;
	}

	@Override
	public String[] getTitleItemKeys() {
		return null;
	}

	@Override
	public IHoverItem getHoverItem(String itemkey) {
		return this;
	}

	@Override
	public Object getTarget(Object object) {
		return object;
	}

	@Override
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestObject) {
			if (TYPE.equals(key))
				return Messages.CatalogPresentationHoverContribution_slideshow;
			else if (NAME.equals(key))
				return Messages.CatalogPresentationHoverContribution_trip_to;
			else if (DESCRIPTION.equals(key))
				return Messages.CatalogPresentationHoverContribution_one_of_the_best;
			else if (STATISTICS.equals(key))
				return Messages.CatalogPresentationHoverContribution_nnn_slides;
		} else if (TYPE.equals(key)) {
			if (object instanceof SlideShow)
				return Messages.CatalogPresentationHoverContribution_slideshow;
			if (object instanceof Exhibition)
				return Messages.CatalogPresentationHoverContribution_exhibition;
			if (object instanceof WebGallery)
				return Messages.CatalogPresentationHoverContribution_webgallery;
		} else if (NAME.equals(key)) {
			if (object instanceof SlideShow)
				return ((SlideShow) object).getName();
			if (object instanceof Exhibition)
				return ((Exhibition) object).getName();
			if (object instanceof WebGallery)
				return ((WebGallery) object).getName();
		} else if (DESCRIPTION.equals(key)) {
			if (object instanceof SlideShow)
				return UiUtilities.shortenText(((SlideShow) object).getDescription(), 100);
			if (object instanceof Exhibition) {
				String des = ((Exhibition) object).getDescription();
				if (des == null)
					des = ((Exhibition) object).getInfo();
				return UiUtilities.shortenText(des, 100);
			}
			if (object instanceof WebGallery)
				return UiUtilities.shortenText(((WebGallery) object).getDescription(), 100);
		} else if (STATISTICS.equals(key)) {
			if (object instanceof SlideShow) {
				int size = ((SlideShow) object).getEntry().size();
				if (size == 0)
					return Messages.CatalogPresentationHoverContribution_no_slides;
				if (size == 1)
					return Messages.CatalogPresentationHoverContribution_one_slide;
				return NLS.bind(Messages.CatalogPresentationHoverContribution_n_slides, size);
			}
			if (object instanceof Exhibition) {
				List<Wall> walls = ((Exhibition) object).getWall();
				int cnt = 0;
				for (Wall w : walls)
					cnt += w.getExhibit().size();
				int size = walls.size();
				String exh = cnt == 0 ? Messages.CatalogPresentationHoverContribution_no_exhibts
						: cnt == 1 ? Messages.CatalogPresentationHoverContribution_one_exhibit
								: NLS.bind(Messages.CatalogPresentationHoverContribution_n_exhibits, cnt);
				String wa = size == 1 ? Messages.CatalogPresentationHoverContribution_one_wall
						: NLS.bind(Messages.CatalogPresentationHoverContribution_n_walls, size);
				return NLS.bind(Messages.CatalogPresentationHoverContribution_n_on_m_wall, exh, wa);
			}
			if (object instanceof WebGallery) {
				List<Storyboard> storyboards = ((WebGallery) object).getStoryboard();
				int cnt = 0;
				for (Storyboard sb : storyboards)
					cnt += sb.getExhibit().size();
				int size = storyboards.size();
				String exh = cnt == 0 ? Messages.CatalogPresentationHoverContribution_no_exhibts
						: cnt == 1 ? Messages.CatalogPresentationHoverContribution_one_exhibit
								: NLS.bind(Messages.CatalogPresentationHoverContribution_n_exhibits, cnt);
				String wa = size == 1 ? Messages.CatalogPresentationHoverContribution_one_storyboard
						: NLS.bind(Messages.CatalogPresentationHoverContribution_n_storyboards, size);
				return NLS.bind(Messages.CatalogPresentationHoverContribution_n_on_m_storyboard, exh, wa);
			}
		}
		return null;
	}

}
