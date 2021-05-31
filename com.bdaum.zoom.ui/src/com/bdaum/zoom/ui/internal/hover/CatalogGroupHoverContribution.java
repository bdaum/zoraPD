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

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.core.Constants;

public class CatalogGroupHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String NAME = Constants.HV_NAME;
	private static final String ANNOTATIONS = Constants.HV_ANNOTATIONS;
	private static final String STATISTICS = Constants.HV_STATISTICS;
	private static final String[] TAGS = new String[] { NAME, ANNOTATIONS, STATISTICS };
	private static final String[] LABELS = new String[] { Messages.CatalogGroupHoverContribution_name,
			Messages.CatalogGroupHoverContribution_annotations, Messages.CatalogGroupHoverContribution_statistics };

	@Override
	public boolean supportsTitle() {
		return false;
	}

	@Override
	public String getCategory() {
		return Messages.CatalogGroupHoverContribution_catalog;
	}

	@Override
	public String getName() {
		return Messages.CatalogGroupHoverContribution_group;
	}

	@Override
	public String getDescription() {
		return Messages.CatalogGroupHoverContribution_group_in_cat;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.CatalogGroupHoverContribution_dflt_template;
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
			if (NAME.equals(key))
				return Messages.CatalogGroupHoverContribution_timeline;
			if (ANNOTATIONS.equals(key))
				return Messages.CatalogGroupHoverContribution_filter_set;
			if (STATISTICS.equals(key))
				return Messages.CatalogGroupHoverContribution_nine_folders;
		} else if (object instanceof Group) {
			Group group = (Group) object;
			if (NAME.equals(key))
				return group.getName();
			if (ANNOTATIONS.equals(key)) {
				String anno = group.getAnnotations();
				if (anno != null && !anno.isEmpty()) {
					if (Constants.GROUP_ID_AUTO.equals(group.getStringId()))
						return Messages.CatalogGroupHoverContribution_cat_rules_def;
					return Messages.CatalogGroupHoverContribution_filter_set;
				}
			} else if (STATISTICS.equals(key)) {
				int groupsize = group.getSubgroup().size();
				int foldersize = group.getExhibition().size() + group.getWebGallery().size()
						+ group.getSlideshow().size() + group.getRootCollection().size();
				if (groupsize > 0) {
					if (foldersize > 0) {
						if (groupsize == 1) {
							if (foldersize == 1)
								return Messages.CatalogGroupHoverContribution_subgroup_folder_11;
							return NLS.bind(Messages.CatalogGroupHoverContribution_subgroup_folder_1n, foldersize);
						}
						if (foldersize == 1)
							return NLS.bind(Messages.CatalogGroupHoverContribution_subgroup_folder_n1, groupsize);
						return NLS.bind(Messages.CatalogGroupHoverContribution_subgroups_folders, groupsize,
								foldersize);
					}
					if (groupsize == 1)
						return Messages.CatalogGroupHoverContribution_one_subgroup;
					return NLS.bind(Messages.CatalogGroupHoverContribution_subgroups, groupsize);
				}
				if (foldersize == 1)
					return Messages.CatalogGroupHoverContribution_one_folder;
				if (foldersize > 1)
					return NLS.bind(Messages.CatalogGroupHoverContribution_folders, foldersize);
			}
		}
		return null;
	}
}
