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
 * Modifications (c) 2009 Berthold Daum  
 */

package org.scohen.juploadr.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
/**
 * A list that doesn't allow null values. Inserting null into this list results
 * in a no-op
 * @author steve
 *
 */
public class NonNullList<E> implements List<E> {
    private List<E> toDecorate;

    public NonNullList() {
        this(new ArrayList<E>());
    }

    public NonNullList(List<E> l) {
        this.toDecorate = new ObservableListDecorator<E>(l,new NullElementeObserver());
    }

    
	public void add(int index, E element) {
        toDecorate.add(index, element);
    }

    
	public boolean add(E o) {
        return toDecorate.add(o);
    }

    
	public boolean addAll(Collection<? extends E> c) {
        return toDecorate.addAll(c);
    }

    
	public boolean addAll(int index, Collection<? extends E> c) {
        return toDecorate.addAll(index, c);
    }

    
	public void clear() {
        toDecorate.clear();
    }

    
	public boolean contains(Object o) {
        return toDecorate.contains(o);
    }

    
	public boolean containsAll(Collection<?> c) {
        return toDecorate.containsAll(c);
    }

    
	@Override
	public boolean equals(Object o) {
        return toDecorate.equals(o);
    }

    
	public E get(int index) {
        return toDecorate.get(index);
    }

    
	@Override
	public int hashCode() {
        return toDecorate.hashCode();
    }

    
	public int indexOf(Object o) {
        return toDecorate.indexOf(o);
    }

    
	public boolean isEmpty() {
        return toDecorate.isEmpty();
    }

    
	public Iterator<E> iterator() {
        return toDecorate.iterator();
    }

    
	public int lastIndexOf(Object o) {
        return toDecorate.lastIndexOf(o);
    }

    
	public ListIterator<E> listIterator() {
        return toDecorate.listIterator();
    }

    
	public ListIterator<E> listIterator(int index) {
        return toDecorate.listIterator(index);
    }

    
	public E remove(int index) {
        return toDecorate.remove(index);
    }

    
	public boolean remove(Object o) {
        return toDecorate.remove(o);
    }

    
	public boolean removeAll(Collection<?> c) {
        return toDecorate.removeAll(c);
    }

    
	public boolean retainAll(Collection<?> c) {
        return toDecorate.retainAll(c);
    }

    
	public E set(int index, E element) {
        return toDecorate.set(index, element);
    }

    
	public int size() {
        return toDecorate.size();
    }

    
	public List<E> subList(int fromIndex, int toIndex) {
        return toDecorate.subList(fromIndex, toIndex);
    }

    
	public Object[] toArray() {
        return toDecorate.toArray();
    }

    
	@SuppressWarnings("hiding")
    public <E> E[] toArray(E[] a) {
        return toDecorate.toArray(a);
    }

}
