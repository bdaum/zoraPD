package com.bdaum.aoModeling.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Berthold Daum
 *
 *         (c) 2002 Berthold Daum
 *
 *         This class provides an identical API to SafeArrayList but without
 *         validation und event management.
 *
 * @see com.bdaum.aoModeling.runtime.SafeArrayList
 */
public class FastArrayList<T> extends ArrayList<T> implements AomList<T> {

	private static final long serialVersionUID = -3919571575771428803L;

	protected int minOcc;

	protected int maxOcc;

	private List<T> list;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public FastArrayList() {
		super(0);
		list = new ArrayList<T>();
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 *
	 * @param name
	 *            - unused
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum occurrences
	 * @param maxOcc
	 *            - maximum occurrences
	 * @param validator
	 *            - unused
	 */
	public FastArrayList(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(Math.min(minOcc + 10, maxOcc));
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param name
	 *            - unused
	 * @param initialCapacity
	 *            the initial capacity of the list.
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum occurrences
	 * @param maxOcc
	 *            - maximum occurrences
	 * @param validator
	 *            - unused
	 */
	public FastArrayList(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(initialCapacity);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>SafeArrayList</tt> instance has an initial capacity of 110% the size
	 * of the specified collection.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the collection used to fill the list
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum occurrences
	 * @param maxOcc
	 *            - maximum occurrences
	 */
	public FastArrayList(AomList<T> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(c);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>SafeArrayList</tt> instance has an initial capacity of 110% the size
	 * of the specified collection.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the collection used to fill the list
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum occurrences
	 * @param maxOcc
	 *            - maximum occurrences
	 */
	public FastArrayList(Collection<T> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(c);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (AomValueChangedNotifier, Object, Object, int, int)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (AomValueChangedNotifier, Object, Object, int, Object)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, Object key) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (AomValueChangedNotifier, Object, Object, int, int, Object,
	 * ValueChangedEvent)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index,
			Object key, ValueChangedEvent wrappedEvent) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addValueChangedListener
	 * (ValueChangedListener)
	 */
	public void addValueChangedListener(ValueChangedListener listener) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeValueChangedListener(ValueChangedListener)
	 */
	public void removeValueChangedListener(ValueChangedListener listener) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			final int fieldId, final int index, final Object key) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			final int fieldId, final int index) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, Object)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			final int fieldId, final Object key) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeOuterChangedListeners(AomValueChangedNotifier)
	 */
	public void removeOuterChangedListeners(AomValueChangedNotifier source) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#getElementValidator()
	 */
	public ElementValidator getElementValidator() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomList#setElementValidator(com.bdaum.aoModeling
	 * .runtime.ElementValidator)
	 */
	public void setElementValidator(ElementValidator validator) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#isBubbled()
	 */
	public boolean isBubbled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#replaceWith(java.util.Collection
	 * <T>)
	 */
	public void replaceWith(Collection<? extends T> replacement) {
		if (replacement != null)
			list = new ArrayList<T>(replacement);
		else
			list.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#replaceSilently(java.util.List)
	 */
	public void replaceSilently(List<T> replacement) {
		if (replacement != null) {
			if (replacement instanceof SafeArrayList)
				((SafeArrayList<T>) replacement)
						.removeAllValueChangedListeners();
			list = replacement;
		} else
			list.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#replaceWith(java.util.Collection
	 * <T>)
	 */
	public void replaceWith(List<T> replacement) {
		if (replacement != null)
			list = replacement;
		else
			list.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canRemove(int)
	 */
	public boolean canRemove(int index) {
		return index >= 0 && index < size();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canSet(int, T)
	 */
	public boolean canSet(int index, T element) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canAdd(T)
	 */
	public boolean canAdd(T element) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#canAddAll(java.util.Collection
	 * <T>)
	 */
	public boolean canAddAll(Collection<? extends T> c) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canClear()
	 */
	public boolean canClear() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canRemove(T)
	 */
	public boolean canRemove(Object o) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#isValid(T)
	 */
	public boolean isValid(Object o) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canAdd(int, T)
	 */
	public boolean canAdd(int index, T element) {
		return (index >= 0 && index <= size());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canAddAll(int,
	 * java.util.Collection<T>)
	 */
	public boolean canAddAll(int index, Collection<? extends T> c) {
		return (index >= 0 && index <= size());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#getMaxOcc()
	 */
	public int getMaxOcc() {
		return maxOcc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#getMinOcc()
	 */
	public int getMinOcc() {
		return minOcc;
	}

	@Override
	public void add(int index, T element) {
		list.add(index, element);
	}

	@Override
	public boolean add(T o) {
		return list.add(o);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return list.addAll(index, c);
	}

	@Override
	public void clear() {
		if (list == null)
			list = new ArrayList<T>();
		else
			list.clear();
	}

	@Override
	public boolean contains(Object elem) {
		return list.contains(elem);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		if (list instanceof ArrayList)
			((ArrayList<T>) list).ensureCapacity(minCapacity);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AomList<?>)
			return ((AomList<?>) o).equals(list);
		return list.equals(o);
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public int indexOf(Object elem) {
		return list.indexOf(elem);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object elem) {
		return list.lastIndexOf(elem);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public T remove(int index) {
		return list.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public T set(int index, T element) {
		return list.set(index, element);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <U> U[] toArray(U[] a) {
		return list.toArray(a);
	}

	@Override
	public String toString() {
		return getClass().getName() + "{" + list.toString();
	}

	@Override
	public void trimToSize() {
		if (list instanceof ArrayList)
			((ArrayList<T>) list).trimToSize();
	}

	@Override
	public Object clone() {
		return new FastArrayList<T>(this, null, 0, minOcc, maxOcc, null);
	}
}