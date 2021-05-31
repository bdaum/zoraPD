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
 * (c) 2011-2021 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bdaum.zoom.ui.internal.codes.Topic;

public class TopicContentProvider implements ITreeContentProvider {
	
	private static final Topic[] EMPTYTOPICS = new Topic[0];

	public void dispose() {
		// do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Topic[])
			return (Topic[]) inputElement;
		return EMPTYTOPICS;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Topic) {
			List<Topic> subTopics = ((Topic) parentElement).getSubTopics();
			if (subTopics != null)
				return subTopics.toArray();
		}
		return EMPTYTOPICS;
	}

	public Object getParent(Object element) {
		if (element instanceof Topic)
			return ((Topic) element).getParent();
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Topic)
			return ((Topic) element).getSubTopics() != null;
		return false;
	}

}