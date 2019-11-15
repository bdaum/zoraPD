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

package com.bdaum.zoom.ui.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class CaptionProcessor extends TemplateProcessor {
	
	public static class CaptionConfiguration {

		public int showLabel = Constants.TITLE_LABEL;
		private String labelTemplate = null;
		public int labelFontsize = 8;
		public int labelAlignment = SWT.CENTER;
		
		private CaptionConfiguration() {
			super();
		}

		private CaptionConfiguration(int showLabel, String labelTemplate, int labelFontsize, int labelAlignment) {
			this.showLabel = showLabel >= 0 ? showLabel : Constants.TITLE_LABEL;
			this.labelTemplate = labelTemplate;
			this.labelFontsize = labelFontsize;
			this.labelAlignment = labelAlignment;
		}

		public String getLabelTemplate() {
			return showLabel == Constants.CUSTOM_LABEL ? labelTemplate : null;
		}

	}


	private static CaptionConfiguration defaultConfiguration =  new CaptionConfiguration(); 
	private CaptionConfiguration globalConfiguration;
	private SmartCollection context;

	public CaptionProcessor(String[] variables) {
		super(variables);
		globalConfiguration = new CaptionConfiguration(); 
		updateGlobalConfiguration();
	}

	public void updateGlobalConfiguration() {
		IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		globalConfiguration.showLabel = preferenceStore.getInt(PreferenceConstants.SHOWLABEL);
		globalConfiguration.labelFontsize = preferenceStore.getInt(PreferenceConstants.LABELFONTSIZE);
		globalConfiguration.labelAlignment = preferenceStore.getInt(PreferenceConstants.LABELALIGNMENT);
		globalConfiguration.labelTemplate = preferenceStore.getString(PreferenceConstants.THUMBNAILTEMPLATE);
	}

	public CaptionConfiguration computeCaptionConfiguration(SmartCollection context) {
		this.context = context;
		return computeCaptionConfiguration();
	}

	public CaptionConfiguration computeCaptionConfiguration() {
		int showLabel = Constants.INHERIT_LABEL;
		if (context != null) {
			while (true) {
				showLabel = context.getShowLabel();
				if (showLabel != Constants.INHERIT_LABEL)
					return new CaptionConfiguration(showLabel, context.getLabelTemplate(), context.getFontSize(),
							context.getAlignment());
				if (context.getSmartCollection_subSelection_parent() == null)
					break;
				context = context.getSmartCollection_subSelection_parent();
			}
			if (showLabel == Constants.INHERIT_LABEL) {
				String groupId = context.getGroup_rootCollection_parent();
				Group group = Core.getCore().getDbManager().obtainById(GroupImpl.class, groupId);
				while (group != null) {
					showLabel = group.getShowLabel();
					if (showLabel != Constants.INHERIT_LABEL)
						return new CaptionConfiguration(showLabel, group.getLabelTemplate(), group.getFontSize(),
								group.getAlignment());
					group = group.getGroup_subgroup_parent();
				}
			}
		}
		return showLabel == Constants.INHERIT_LABEL ? globalConfiguration : defaultConfiguration;
	}

	public String computeImageCaption(Asset asset, IScoreFormatter scoreFormatter, Integer cardinality,
			WildCardFilter filter, String template, boolean presentation) {
		if (asset == null)
			return ""; //$NON-NLS-1$
		StringBuilder sb;
		if (template != null) {
			String s = processTemplate(template, asset);
			if (scoreFormatter == null)
				return s;
			sb = new StringBuilder(s);
		} else if (cardinality != null) {
			String name = asset.getName();
			if (filter != null) {
				String s = presentation ? UiUtilities.createSlideTitle(asset) : Core.getFileName(asset.getUri(), true);
				String[] capture = filter.capture(s);
				for (String c : capture)
					if (c.length() < s.length())
						name = c;
			}
			sb = new StringBuilder(name).append(" (").append(cardinality.intValue()).append(')'); //$NON-NLS-1$
		} else {
			String s = presentation ? UiUtilities.createSlideTitle(asset) : Core.getFileName(asset.getUri(), true);
			if (scoreFormatter == null)
				return s;
			sb = new StringBuilder(s);
		}
		if (scoreFormatter != null) {
			String remark = scoreFormatter.format(asset.getScore());
			if (!remark.isEmpty())
				sb.append(" (").append(remark).append(')'); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
