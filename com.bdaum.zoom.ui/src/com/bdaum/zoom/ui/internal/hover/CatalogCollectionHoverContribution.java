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

import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class CatalogCollectionHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String NAME = Constants.HV_NAME;
	private static final String TYPE = Constants.HV_TYPE;
	private static final String HINTS = "{hints}"; //$NON-NLS-1$
	private static final String DESCRIPTION = Constants.HV_DESCRIPTION;
	private static final String SORT = "{sort}"; //$NON-NLS-1$
	private static final String CONTENT = "{content}"; //$NON-NLS-1$
	private static final String SCOPE = "{scope}"; //$NON-NLS-1$
	private static final String STATISTICS = Constants.HV_STATISTICS;
	private static final String[] TAGS = new String[] { TYPE, NAME, HINTS, DESCRIPTION, CONTENT, SORT, SCOPE,
			STATISTICS };
	private static final String[] LABELS = new String[] { Messages.CatalogCollectionHoverContribution_type,
			Messages.CatalogCollectionHoverContribution_name, Messages.CatalogCollectionHoverContribution_annotations,
			Messages.CatalogCollectionHoverContribution_description,
			Messages.CatalogCollectionHoverContribution_content, Messages.CatalogCollectionHoverContribution_sort,
			Messages.CatalogCollectionHoverContribution_scope, Messages.CatalogCollectionHoverContribution_statistics };

	@Override
	public boolean supportsTitle() {
		return false;
	}

	@Override
	public String getCategory() {
		return Messages.CatalogCollectionHoverContribution_catalog;
	}

	@Override
	public String getName() {
		return Messages.CatalogCollectionHoverContribution_collection;
	}

	@Override
	public String getDescription() {
		return Messages.CatalogCollectionHoverContribution_coll_in_cat;
	}

	@Override
	public String getDefaultTemplate() {
		return "{type} {name}(? ({hints})?)\n\n?{description}\n?{content}\n?{sort}\n{scope}"; //$NON-NLS-1$
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
				return Messages.CatalogCollectionHoverContribution_sys_coll;
			if (NAME.equals(key))
				return Messages.CatalogCollectionHoverContribution_sample_coll;
			if (DESCRIPTION.equals(key))
				return Messages.CatalogCollectionHoverContribution_just_a_sample_coll;
			if (CONTENT.equals(key))
				return Messages.CatalogCollectionHoverContribution_focal_length;
			if (SCOPE.equals(key))
				return Messages.CatalogCollectionHoverContribution_local_search;
			if (SORT.equals(key))
				return Messages.CatalogCollectionHoverContribution_sorted_by_creationdate;
			if (HINTS.equals(key))
				return Messages.CatalogCollectionHoverContribution_album_size;
			if (STATISTICS.equals(key))
				return Messages.CatalogCollectionHoverContribution_two_subfolders;
		} else if (object instanceof SmartCollection) {
			SmartCollection sm = (SmartCollection) object;
			if (TYPE.equals(key))
				return sm.getAlbum()
						? sm.getSystem() ? Messages.CatalogCollectionHoverContribution_person_album
								: Messages.CatalogCollectionHoverContribution_album
						: sm.getSystem() ? Messages.CatalogCollectionHoverContribution_sys_coll
								: Messages.CatalogCollectionHoverContribution_user_coll;
			if (NAME.equals(key))
				return sm.getName();
			if (DESCRIPTION.equals(key))
				return UiUtilities.shortenText(sm.getDescription(), 100);
			if (SCOPE.equals(key))
				return sm.getNetwork() ? Messages.CatalogCollectionHoverContribution_netword_search
						: Messages.CatalogCollectionHoverContribution_local_search;
			if (CONTENT.equals(key)) {
				if (!sm.getSystem() && !UiUtilities.isImport(sm))
					return UiUtilities.composeContentDescription(sm, "\n", false, false); //$NON-NLS-1$
			} else if (SORT.equals(key)) {
				List<SortCriterion> sortCriteria = sm.getSortCriterion();
				if (!sortCriteria.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					UiUtilities.composeSortDescription(sm, sb, false);
					return sb.toString();
				}
			} else if (HINTS.equals(key)) {
				if (sm.getAlbum())
					return NLS.bind(Messages.CatalogCollectionHoverContribution_images, sm.getAsset().size());
				if (sm.getSystem()) {
					List<Criterion> crits = sm.getCriterion();
					if (!crits.isEmpty()) {
						Criterion criterion = crits.get(0);
						if (criterion.getField().equals(QueryField.RATING.getKey()))
							return QueryField.RATING.formatScalarValue(criterion.getValue());
					}
				}
			} else if (STATISTICS.equals(key)) {
				int size = sm.getSubSelection().size();
				if (size == 1)
					return Messages.CatalogCollectionHoverContribution_one_subfolder;
				if (size > 1)
					return NLS.bind(Messages.CatalogCollectionHoverContribution_subfolders, size);
			}
		}
		return null;
	}

}
