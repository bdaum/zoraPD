/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package org.scohen.juploadr.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

public class ObservableListDecorator<T> extends Observable implements List<T> {
    private List<T> decorated;

    public ObservableListDecorator(List<T> toDecorate,Observer toAdd) {
        this.decorated = toDecorate;
        addObserver(toAdd);
        changed();
    }
    
    public ObservableListDecorator(List<T> toDecorate,List<Observer> observers) {
        Iterator<Observer> iter = observers.iterator();
        while(iter.hasNext()) {
            Observer next = iter.next();
            addObserver(next);
        }
        this.decorated = toDecorate;
        changed();
    }

    
	public int size() {
        return decorated.size();
    }

    
	public void add(int index, T element) {
        decorated.add(index, element);
        changed();
    }

    /**
     * 
     */
    private void changed() {
        setChanged();
        notifyObservers();
    }

    
	public boolean add(T o) {
        changed();
        return decorated.add(o);
    }

    
	public boolean addAll(Collection<? extends T> c) {
        boolean rv = decorated.addAll(c);
        changed();
        return rv;
    }

    
	public boolean addAll(int index, Collection<? extends T> c) {
        boolean rv = decorated.addAll(index, c);
        changed();
        return rv;
    }

    
	public void clear() {
        decorated.clear();
        changed();
    }

    
	public boolean contains(Object o) {
        return decorated.contains(o);
    }

    
	public boolean containsAll(Collection<?> c) {
        return decorated.containsAll(c);
    }

    
	@Override
	public boolean equals(Object o) {
        return decorated.equals(o);
    }

    
	public T get(int index) {
        return decorated.get(index);
    }

    
	@Override
	public int hashCode() {
        return decorated.hashCode();
    }

    
	public int indexOf(Object o) {
        return decorated.indexOf(o);
    }

    
	public boolean isEmpty() {
        return decorated.isEmpty();
    }

    
	public Iterator<T> iterator() {
        return decorated.iterator();
    }

    
	public int lastIndexOf(Object o) {
        return decorated.lastIndexOf(o);
    }

    
	public ListIterator<T> listIterator() {
        return decorated.listIterator();
    }

    
	public ListIterator<T> listIterator(int index) {
        return decorated.listIterator(index);
    }

    
	public T remove(int index) {
        return decorated.remove(index);
    }

    
	public boolean remove(Object o) {
        boolean rv = decorated.remove(o);
        changed();
        return rv;
    }

    
	public boolean removeAll(Collection<?> c) {
        boolean rv = decorated.removeAll(c);
        changed();
        return rv;
    }

    
	public boolean retainAll(Collection<?> c) {
        boolean rv = decorated.retainAll(c);
        changed();
        return rv;
    }

    
	public T set(int index, T element) {
        T rv = decorated.set(index, element); 
        changed();
        return rv;
    }

    
	public List<T> subList(int fromIndex, int toIndex) {
        return decorated.subList(fromIndex, toIndex);
    }

    
	public Object[] toArray() {
        return decorated.toArray();
    }

    
	@SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return decorated.toArray(a);
    }

    
	@Override
	public void notifyObservers(Object arg) {
        super.notifyObservers(decorated);
    }

}
