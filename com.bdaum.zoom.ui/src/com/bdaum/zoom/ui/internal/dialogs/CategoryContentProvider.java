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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bdaum.zoom.cat.model.meta.Category;

public class CategoryContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	public void dispose() {
		// do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Category) {
			Map<String, Category> subCategories = ((Category) parentElement).getSubCategory();
			if (subCategories != null) {
				List<Category> children = new ArrayList<>(subCategories.size());
				for (Category category : subCategories.values())
					if (category != null)
						children.add(category);
				return children.toArray();
			}
		}
		return EMPTY;
	}

	public Object getParent(Object element) {
		if (element instanceof Category) {
			Category subCategory_parent = ((Category) element).getCategory_subCategory_parent();
			if (subCategory_parent != null)
				return subCategory_parent;
			return ((Category) element).getMeta_parent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Category) {
			Map<String, Category> subCategories = ((Category) element).getSubCategory();
			return subCategories != null && !subCategories.isEmpty();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Map<?, ?>)
			return ((Map<?, ?>) inputElement).values().toArray();
		return new Object[0];
	}

}