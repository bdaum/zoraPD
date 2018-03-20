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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.QueryField.Category;

public final class GroupComboCatFilter extends ViewerFilter {
	@Override
	public boolean select(Viewer viewer, Object parentElement,
			Object element) {
		return element instanceof Category
				&& ((Category) element).query
				|| element instanceof QueryField
				&& ((QueryField) element).getCategory().query;
	}
}