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

import org.eclipse.jface.preference.IPreferenceStore;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public abstract class AbstractHoverContribution implements IHoverContribution {

	private String id;

	public AbstractHoverContribution() {
		super();
	}

	@Override
	public String getTemplate() {
		IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		return preferenceStore.getString(id + ".text.template"); //$NON-NLS-1$
	}

	@Override
	public String getTitleTemplate() {
		IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		return preferenceStore.getString(id + ".title.template"); //$NON-NLS-1$
	}

	@Override
	public void save(String titleTemplate, String template) {
		IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		if (template != null && !template.isEmpty())
			preferenceStore.setValue(id + ".text.template", //$NON-NLS-1$
					template.contentEquals(getDefaultTemplate()) ? null : template);
		if (supportsTitle() && titleTemplate != null && !titleTemplate.isEmpty())
			preferenceStore.setValue(id + ".title.template", //$NON-NLS-1$
					template.contentEquals(getDefaultTitleTemplate()) ? null : titleTemplate);
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	protected String resolveKey(String key, String[] tags, String[] legend) {
		for (int i = 0; i < tags.length; i++)
			if (tags[i].equals(key))
				return legend[i];
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int getMediaFlags(Object object) {
		object = object instanceof Asset || object instanceof List<?> ? object : getTarget(object);
		if (object instanceof Asset) {
			IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(((Asset) object).getFormat());
			if (mediaSupport != null)
				return mediaSupport.getPropertyFlags();
			return QueryField.PHOTO;
		} else if (object instanceof List<?>) {
			int flags = 0;
			for (Object o : (List<?>) object)
				if (o instanceof Asset) {
					IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(((Asset) o).getFormat());
					flags |= mediaSupport != null ? mediaSupport.getPropertyFlags() : QueryField.PHOTO;
				}
			return flags;
		}
		return 0;
	}
	
	
	@Override
	public Object getTestObject() {
		return new HoverTestObject();
	}

}