package com.bdaum.aoModeling.runtime;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Berthold Daum
 *
 *         (c) 2007 Berthold Daum
 *
 *         This class provides an identical API to SafeHashSet but without
 *         validation und event management.
 *
 */
public class FastHashSet<T> extends HashSet<T> implements AomSet<T> {

	private static final long serialVersionUID = 5386582105018199747L;

	protected int minOcc;

	protected int maxOcc;

	private Set<T> set;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public FastHashSet() {
		super(0);
		set = new HashSet<T>();
	}

	/**
	 * Constructs an empty list with an initial capacity of
	 * Math.min(minOcc+10,maxOcc).
	 *
	 * @param name
	 *            - unused
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - unused
	 * @param listener
	 *            - unused
	 */
	public FastHashSet(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(Math.min(minOcc + 10, maxOcc));
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
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - unused
	 * @param listener
	 *            - unused
	 */
	public FastHashSet(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(
				Math.max(minOcc, Math.min(initialCapacity, maxOcc)));
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>FastHashSet</tt> instance has an initial capacity of 110% the size of
	 * the specified collection.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the collection used to fill the list
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - unused
	 * @param listener
	 *            - unused
	 */
	public FastHashSet(Collection<T> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(c);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a list containing the elements of the specified list. The
	 * <tt>FastHashSet</tt> instance has an initial capacity of 110% the size of
	 * the specified list. The element validator of the new list is inherited
	 * from the specified list.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the list used to fill the new list
	 * @param id
	 *            - unused
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param listener
	 *            - unused
	 */
	public FastHashSet(AomList<T> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(c);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canAdd(java.lang.Object)
	 */
	public boolean canAdd(T element) {
		return (isValid(element) && set.size() < maxOcc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#add(Object)
	 */
	@Override
	public boolean add(T o) {
		return set.add(o);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#canAddAll(java.util.Collection
	 * )
	 */
	public boolean canAddAll(Collection<? extends T> c) {
		return (set.size() + c.size() <= maxOcc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#addAll(Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		return set.addAll(c);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canClear()
	 */
	public boolean canClear() {
		return minOcc == 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		if (set == null)
			set = new HashSet<T>();
		else
			set.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#canRemove(java.lang.Object)
	 */
	public boolean canRemove(Object o) {
		return (set.size() > minOcc && set.contains(o));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	@Override
	public boolean remove(Object o) {
		return set.remove(o);
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
	 * (com.bdaum.aoModeling.runtime.AomValueChangedNotifier, java.lang.Object,
	 * java.lang.Object, int, java.lang.Object)
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
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#
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
			int fieldId, final int index) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, Object)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId, final Object key) {
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
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#
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
	 * @see com.bdaum.aoModeling.runtime.AomCollection#isValid(java.lang.Object)
	 */
	public boolean isValid(Object o) {
		return true;
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
		set.clear();
		if (replacement != null)
			set.addAll(replacement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomSet#replaceSilently(java.util.Set)
	 */
	public void replaceSilently(Set<T> replacement) {
		if (replacement != null) {
			if (replacement instanceof SafeHashSet)
				((SafeHashSet<T>) replacement).removeAllValueChangedListeners();
			set = replacement;
		} else
			set.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return set.iterator();
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
	public boolean contains(Object elem) {
		return set.contains(elem);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AomSet<?>)
			return ((AomSet<?>) o).equals(set);
		return set.equals(o);
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <U> U[] toArray(U[] a) {
		return set.toArray(a);
	}

	@Override
	public Object clone() {
		return new FastHashSet<T>(this, null, 0, minOcc, maxOcc, null, null);
	}

}