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

import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;

public class ExhibitHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String TITLE = Constants.HV_TITLE;
	private static final String CREDITS = "{credits}"; //$NON-NLS-1$
	private static final String TOTAL = Constants.HV_TOTAL;
	private static final String WALL = Constants.HV_WALL;
	private static final String WALLPOS = Constants.HV_WALLPOS;
	private static final String METADATA = Constants.HV_METADATA;
	private static final String[] TITLETAGS = new String[] { TITLE, CREDITS, TOTAL, WALL, WALLPOS };
	private static final String[] TITLELABELS = new String[] { Messages.ExhibitHoverContribution_title,
			Messages.ExhibitHoverContribution_credits, Messages.ExhibitHoverContribution_total_number,
			Messages.ExhibitHoverContribution_wall_number, Messages.ExhibitHoverContribution_wall_position };
	private static final String[] TAGS = new String[] { TITLE, CREDITS, TOTAL, WALL, WALLPOS, METADATA };
	private static final String[] LABELS = new String[] { Messages.ExhibitHoverContribution_title,
			Messages.ExhibitHoverContribution_credits, Messages.ExhibitHoverContribution_total_number,
			Messages.ExhibitHoverContribution_wall_number, Messages.ExhibitHoverContribution_wall_position,
			Messages.ExhibitHoverContribution_metadata_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.ExhibitHoverContribution_presentation;
	}

	@Override
	public String getName() {
		return Messages.ExhibitHoverContribution_exhibit;
	}

	@Override
	public String getDescription() {
		return Messages.ExhibitHoverContribution_exhibit_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.ExhibitHoverContribution_dflt_template;
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
				return Messages.ExhibitHoverContribution_my_best;
			if (CREDITS.equals(key))
				return "Man Ray"; //$NON-NLS-1$
			if (TOTAL.equals(key)) 
				return "42"; //$NON-NLS-1$
			else if (WALL.equals(key))
				return "1"; //$NON-NLS-1$
			else if (WALLPOS.equals(key))
				return "5"; //$NON-NLS-1$
		} else if (object instanceof Exhibit) {
			if (TITLE.equals(key))
				return ((Exhibit) object).getTitle();
			if (CREDITS.equals(key))
				return ((Exhibit) object).getCredits();
			if (TOTAL.equals(key) || WALL.equals(key) || WALLPOS.equals(key))
				return context.getValue(key, object);
		}
		return null;
	}

	@Override
	public Object getTarget(Object object) {
		if (object instanceof HoverTestObject)
			return new HoverTestAsset();
		if (object instanceof Exhibit)
			return Core.getCore().getDbManager().obtainAsset(((Exhibit) object).getAsset());
		return null;
	}

}
