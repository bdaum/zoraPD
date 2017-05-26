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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;

import com.bdaum.zoom.core.QueryField;

public class PropertiesView extends AbstractPropertiesView {


	public static final String ID = "com.bdaum.zoom.ui.views.PropertiesView"; //$NON-NLS-1$


	@Override
	public QueryField getRootElement() {
		return QueryField.IMAGE_ALL;
	}


	@Override
	protected Object getFieldParent(QueryField element) {
		QueryField parent = element.getParent();
		return (parent != QueryField.ALL) ? parent : null;
	}


	@Override
	protected int getExpandLevel() {
		return 1;
	}


	@Override
	protected int[] getColumnWidths() {
		return new int[] { 150, 120, 30 };
	}


	@Override
	protected int[] getColumnMaxWidths() {
		return new int[] { 250, Integer.MAX_VALUE, 30 };
	}

}