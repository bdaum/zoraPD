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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BagChange<T> {
	private Set<T> add;
	private Set<T> modify;
	private Set<T> remove;
	private T[] display;

	/**
	 * Constructor
	 * 
	 * @param add
	 *            - added items
	 * @param modify
	 *            - modified items
	 * @param remove
	 *            - remove items
	 * @param display
	 *            - optionally the resulting set of items (or null)
	 */
	public BagChange(Collection<T> add, Collection<T> modify, Collection<T> remove, T[] display) {
		this.add = add == null ? null : new HashSet<>(add);
		this.modify = modify == null ? null : new HashSet<>(modify);
		this.remove = remove == null ? null : new HashSet<>(remove);
		this.display = display;
	}

	public void update(Set<Object> set) {
		if (remove != null)
			set.removeAll(remove);
		if (add != null)
			set.addAll(add);
	}

	public boolean hasChanged(T item) {
		return wasRemoved(item) || wasModified(item);
	}
	
	public Set<T> getChanged() {
		if (remove == null)
			return modify;
		if (modify == null)
			return remove;
		Set<T> changed = new HashSet<>(modify);
		changed.addAll(remove);
		return changed;
	}


	public boolean wasAdded(T item) {
		return add != null && add.contains(item);
	}
	
	public Set<T> getAdded() {
		return add;
	}

	public boolean wasModified(T item) {
		return modify != null && modify.contains(item);
	}

	public boolean wasRemoved(T item) {
		return remove != null && remove.contains(item);
	}

	public boolean hasChanges() {
		return remove != null && !remove.isEmpty() || modify != null && !modify.isEmpty()
				|| add != null && !add.isEmpty();
	}
	
	public T[] getDisplay() {
		return display;
	}

	public boolean hasRemoved() {
		return remove != null && !remove.isEmpty();
	}

	public boolean hasStructuralChanges() {
		return hasRemoved() || hasAdded();
	}

	public boolean hasAdded() {
		return add != null && !add.isEmpty();
	}

	public Set<T> getModified() {
		return modify;
	}


}