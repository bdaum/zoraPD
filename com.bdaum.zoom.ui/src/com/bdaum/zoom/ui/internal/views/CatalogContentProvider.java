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
package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;

public class CatalogContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];
	GregorianCalendar cal = null;
	GregorianCalendar today = null;
	Date date = null;
	String calYear = null;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// do nothing
	}

	public void dispose() {
		// do nothing
	}

	public Object[] getElements(Object parent) {
		calYear = null;
		if (GroupImpl.class.equals(parent)) {
			List<GroupImpl> roots = new ArrayList<GroupImpl>(
					Core.getCore().getDbManager().obtainObjects(GroupImpl.class));
			for (Iterator<GroupImpl> it = roots.iterator(); it.hasNext();)
				if (it.next().getGroup_subgroup_parent() != null)
					it.remove();
			roots.add(CatalogView.WASTEBASKET);
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
		} else if (child instanceof SlideShowImpl)
			groupId = ((SlideShowImpl) child).getGroup_slideshow_parent();
		else if (child instanceof ExhibitionImpl)
			groupId = ((ExhibitionImpl) child).getGroup_exhibition_parent();
		else if (child instanceof WebGalleryImpl)
			groupId = ((WebGalleryImpl) child).getGroup_webGallery_parent();
		return Core.getCore().getDbManager().obtainById(GroupImpl.class, groupId);
	}

	public Object[] getChildren(Object parent) {
		return getChildren(parent, getAnnotation(parent));
	}

	protected Object[] getChildren(Object parent, String annotation) {
		if (parent instanceof Group) {
			List<IIdentifiableObject> children = new ArrayList<>();
			Set<String> ids = new HashSet<String>();
			Group group = (Group) parent;
			String groupId = group.getStringId();
			if (group.getSubgroup() != null)
				children.addAll(group.getSubgroup());
			if (group.getRootCollection() != null)
				ids.addAll(group.getRootCollection());
			if (group.getSlideshow() != null)
				ids.addAll(group.getSlideshow());
			if (group.getExhibition() != null)
				ids.addAll(group.getExhibition());
			if (group.getWebGallery() != null)
				ids.addAll(group.getWebGallery());
			if (!ids.isEmpty()) {
				List<IdentifiableObject> set = Core.getCore().getDbManager().obtainByIds(IdentifiableObject.class, ids);
				for (IdentifiableObject obj : set) {
					if (obj instanceof SmartCollectionImpl
							&& groupId.equals(((SmartCollectionImpl) obj).getGroup_rootCollection_parent())) {
						if (annotation == null || annotation.isEmpty())
							children.add(obj);
						else if (filter((SmartCollectionImpl) obj, annotation))
							children.add(obj);
					} else if (obj instanceof ExhibitionImpl
							&& groupId.equals(((ExhibitionImpl) obj).getGroup_exhibition_parent()))
						children.add(obj);
					else if (obj instanceof WebGalleryImpl
							&& groupId.equals(((WebGalleryImpl) obj).getGroup_webGallery_parent()))
						children.add(obj);
					else if (obj instanceof SlideShowImpl
							&& groupId.equals(((SlideShowImpl) obj).getGroup_slideshow_parent()))
						children.add(obj);
				}
			}
			return children.toArray();
		}
		if (parent instanceof SmartCollection) {
			if (annotation == null || annotation.isEmpty())
				return new HashSet<>(((SmartCollection) parent).getSubSelection()).toArray(); // avoid duplicates
			Set<SmartCollection> set = new HashSet<>();
			for (SmartCollection sm : ((SmartCollection) parent).getSubSelection())
				if (filter(sm, annotation))
					set.add(sm);
			return set.toArray();
		}
		return EMPTY;
	}

	private boolean filter(SmartCollection sm, String annotation) {
		String id = sm.getStringId();
		switch (annotation.charAt(0)) {
		case 'R':
			if (annotation.length() < 7)
				return true;
			int p = id.indexOf('=');
			if (p >= 0) {
				try {
					int r = id.charAt(p + 1) & 7;
					if (r <= annotation.length() && annotation.charAt(r + 1) == 't')
						return true;
				} catch (NumberFormatException e) {
					// don't accept
				}
			}
			return false;
		case 'I':
			if (annotation.length() < 2)
				return true;
			char sel = annotation.charAt(1);
			if (sel == 3)
				return true;
			if (Constants.LAST_IMPORT_ID.equals(id))
				return true;
			List<Criterion> crits = sm.getCriterion();
			if (crits.isEmpty())
				return false;
			Criterion criterion = crits.get(0);
			if (criterion == null)
				return false;
			Object value = criterion.getValue();
			Object to = criterion.getTo();
			if (cal != null)
				cal = new GregorianCalendar();
			cal.setTime(to != null ? (Date) to : (Date) value);
			switch (sel) {
			case '0':
				cal.add(Calendar.MONTH, 1);
				break;
			case '1':
				cal.add(Calendar.MONTH, 3);
				break;
			case '2':
				cal.add(Calendar.YEAR, 1);
				break;
			}
			computeToday();
			return (cal.compareTo(today) >= 0);
		case 'L':
			if (annotation.length() < 2)
				return true;
			int asel = annotation.charAt(1) & 3;
			if (asel == 2)
				return true;
			int n = -1;
			for (int i = 0; i < id.length(); i++)
				if (id.charAt(i) == '|')
					++n;
			return n <= asel;
		case 'T':
			if (annotation.length() < 8)
				return true;
			try {
				int s = IDbManager.DATETIMEKEY.length();
				if (id.length() <= s)
					return false;
				int y = id.indexOf('-', s);
				if (y < 0) {
					for (int i = 4, j = s; i < 8; i++, j++) {
						char ca = annotation.charAt(i);
						char ci = id.charAt(j);
						if (ci < ca)
							return false;
						if (ci > ca)
							return true;
					}
					// year
					return true;
				}
				int type = 3;
				if (id.length() > y + 1) {
					if (id.charAt(y + 1) == 'W')
						type = 2;
					if (id.indexOf('-', y + 1) >= 0)
						type = 1;
				}
				switch (annotation.charAt(type)) {
				case '0':
					return false;
				case '1':
					if (calYear == null) {
						computeToday();
						calYear = String.valueOf(today.get(GregorianCalendar.YEAR));
					}
					return calYear.charAt(0) == id.charAt(s) && calYear.charAt(1) == id.charAt(s + 1)
							&& calYear.charAt(2) == id.charAt(s + 2) && calYear.charAt(3) == id.charAt(s + 3);
				case '2':
					return true;
				}
			} catch (NumberFormatException e) {
				return true;
			}
			break;
		}
		return false;

	}

	private void computeToday() {
		if (today == null)
			today = new GregorianCalendar();
		else {
			if (date == null)
				date = new Date();
			else
				date.setTime(System.currentTimeMillis());
			today.setTime(date);
		}
	}

	private String getAnnotation(Object parent) {
		while (parent instanceof SmartCollection)
			parent = getParent(parent);
		if (parent instanceof Group) {
			Group group = (Group) parent;
			while (group != null) {
				if (group.getAnnotations() != null)
					return group.getAnnotations();
				group = group.getGroup_subgroup_parent();
			}
		}
		return null;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof SmartCollection && ((SmartCollection) parent).getSubSelection().isEmpty())
			return false;
		String annotation = getAnnotation(parent);
		if (annotation != null && !annotation.isEmpty())
			return getChildren(parent, annotation).length > 0;
		if (parent instanceof GroupImpl) {
			GroupImpl group = (GroupImpl) parent;
			return (group.getRootCollection() != null && !group.getRootCollection().isEmpty())
					|| (group.getSubgroup() != null && !group.getSubgroup().isEmpty())
					|| (group.getSlideshow() != null && !group.getSlideshow().isEmpty())
					|| (group.getExhibition() != null && !group.getExhibition().isEmpty())
					|| (group.getWebGallery() != null && !group.getWebGallery().isEmpty());
		}
		return parent instanceof SmartCollection;
	}
}