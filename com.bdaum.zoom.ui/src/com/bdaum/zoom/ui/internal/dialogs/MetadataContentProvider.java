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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bdaum.zoom.core.QueryField;

public class MetadataContentProvider implements ITreeContentProvider {
	private static final Object[] EMPTYOBJECTS = new Object[0];

	public void dispose() {
		// do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof QueryField) {
			QueryField qfield = (QueryField) parentElement;
			return qfield.isStruct() ? EMPTYOBJECTS : qfield.getChildren();
		}
		return EMPTYOBJECTS;
	}

	public Object getParent(Object element) {
		return (element instanceof QueryField) ? ((QueryField) element)
				.getParent() : null;

	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof QueryField)
			return ((QueryField) inputElement).getChildren();
		if (inputElement instanceof QueryField[])
			return (QueryField[]) inputElement;
		return EMPTYOBJECTS;
	}

}