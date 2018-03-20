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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.report.internal.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;

public class CatalogContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// do nothing
	}

	public void dispose() {
		// do nothing
	}

	public Object[] getElements(Object parent) {
		if (GroupImpl.class.equals(parent)) {
			List<GroupImpl> groups = Core.getCore().getDbManager().obtainObjects(GroupImpl.class);
			List<GroupImpl> roots = new ArrayList<GroupImpl>(groups);
			Iterator<GroupImpl> it = roots.iterator();
			while (it.hasNext()) {
				GroupImpl group = it.next();
				if (group.getGroup_subgroup_parent() != null)
					it.remove();
				else if (!hasChildren(group))
					it.remove();
			}
			return roots.toArray();
		}
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof Group)
			return ((Group) child).getGroup_subgroup_parent();
		String groupId = null;
		if (child instanceof SmartCollectionImpl) {
			SmartCollectionImpl coll = (SmartCollectionImpl) child;
			if (coll.getSmartCollection_subSelection_parent() != null)
				return coll.getSmartCollection_subSelection_parent();
			groupId = coll.getGroup_rootCollection_parent();
		}
		return Core.getCore().getDbManager().obtainById(GroupImpl.class, groupId);
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof GroupImpl) {
			IDbManager dbManager = Core.getCore().getDbManager();
			List<IIdentifiableObject> children = new ArrayList<>();
			Set<String> ids = new HashSet<String>();
			GroupImpl group = (GroupImpl) parent;
			String groupId = group.getStringId();
			if (group.getSubgroup() != null)
				for (Group subGroup : group.getSubgroup())
					if (hasChildren(subGroup))
						children.add(subGroup);
			if (group.getRootCollection() != null)
				ids.addAll(group.getRootCollection());
			if (!ids.isEmpty()) {
				List<IdentifiableObject> set = dbManager.obtainByIds(IdentifiableObject.class, ids);
				for (IdentifiableObject obj : set) {
					if (obj instanceof SmartCollectionImpl
							&& groupId.equals(((SmartCollectionImpl) obj).getGroup_rootCollection_parent()))
						children.add(obj);
				}
			}
			return children.toArray();
		}
		if (parent instanceof SmartCollection)
			return new HashSet<>(((SmartCollection) parent).getSubSelection()).toArray(); // avoid
																							// duplicates
		return EMPTY;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof GroupImpl) {
			GroupImpl group = (GroupImpl) parent;
			if (group.getRootCollection() != null && !group.getRootCollection().isEmpty())
				return true;
			if (group.getSubgroup() != null)
				for (Group subGroup : group.getSubgroup())
					if (hasChildren(subGroup))
						return true;
			return false;
		}
		return (parent instanceof SmartCollection) ? !((SmartCollection) parent).getSubSelection().isEmpty() : false;
	}
}