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

import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;

public class SlideHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String TOTAL = Constants.HV_TOTAL;
	private static final String SEQUENCE_NUMBER = "{sequenceNumber}"; //$NON-NLS-1$
	private static final String SECTIONTEXT = "{sectionText}"; //$NON-NLS-1$
	private static final String METADATA = Constants.HV_METADATA;
	private static final String[] TITLETAGS = new String[] { SEQUENCE_NUMBER, TOTAL };
	private static final String[] TITLELABELS = new String[] { Messages.SlideHoverContribution_seq_no,
			Messages.SlideHoverContribution_total_number };
	private static final String[] TAGS = new String[] { SEQUENCE_NUMBER, TOTAL, SECTIONTEXT, METADATA };
	private static final String[] LABELS = new String[] { Messages.SlideHoverContribution_seq_no,
			Messages.SlideHoverContribution_total_number, Messages.SlideHoverContribution_section_text, Messages.SlideHoverContribution_metadata_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.SlideHoverContribution_presentation;
	}

	@Override
	public String getName() {
		return Messages.SlideHoverContribution_slides;
	}

	@Override
	public String getDescription() {
		return Messages.SlideHoverContribution_slides_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.SlideHoverContribution_dflt_template;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return Messages.SlideHoverContribution_slide_n_of_m;
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
			if (SEQUENCE_NUMBER.equals(key))
				return "3"; //$NON-NLS-1$
			if (TOTAL.equals(key))
				return "42"; //$NON-NLS-1$
			if (SECTIONTEXT.equals(key))
				return Messages.SlideHoverContribution_new_section;
		} else if (object instanceof Slide) {
			Slide slide = (Slide) object;
			if (SEQUENCE_NUMBER.equals(key))
				return String.valueOf(slide.getSequenceNo());
			if (TOTAL.equals(key))
				return context.getValue(key, object);
			if (SECTIONTEXT.equals(key))
				return slide.getDescription();
		}
		return null;
	}

	@Override
	public Object getTarget(Object object) {
		if (object instanceof HoverTestObject)
			return new HoverTestAsset();
		if (object instanceof Slide)
			return Core.getCore().getDbManager().obtainAsset(((Slide) object).getAsset());
		return null;
	}

}
