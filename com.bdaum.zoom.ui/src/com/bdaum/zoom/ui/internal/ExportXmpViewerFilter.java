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
 * (c) 2013-2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.bdaum.zoom.core.QueryField;

public class ExportXmpViewerFilter extends ViewerFilter {

	public static final ExportXmpViewerFilter INSTANCE = new ExportXmpViewerFilter();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof QueryField) {
			QueryField qfield = (QueryField) element;
			return (qfield.getCategory() == QueryField.CATEGORY_ALL || qfield
					.testFlags(QueryField.PHOTO))
					&& (qfield.hasChildren() || qfield.isUiField()
							&& qfield.getPath() != null
							&& qfield.getXmpNs() != null);
		}
		return false;
	}
}