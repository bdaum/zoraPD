package com.bdaum.aoModeling.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Berthold Daum
 *
 *         (c) 2007 Berthold Daum
 *
 *         This class provides an identical API to SafeHashMap but without
 *         validation und event management.
 *
 * @see com.bdaum.aoModeling.runtime.SafeHashMap
 *
 */
public class FastHashMap<P, Q> extends HashMap<P, Q> implements AomMap<P, Q> {

	private static final long serialVersionUID = -3729260736943195891L;

	protected int minOcc;

	protected int maxOcc;

	private Map<P, Q> map;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public FastHashMap() {
		super(0);
		map = new HashMap<P, Q>();
	}

	/**
	 * Constructs an empty FastHashMap with an initial capacity of 16 and the
	 * default load factor (0.75).
	 *
	 * @param name
	 *            - unused
	 * @param id
	 *            - field id for event notification
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - element validator. Executed when a new element is added to
	 *            the list
	 * @param listener
	 *            - listener for bubbled events
	 */
	public FastHashMap(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(minOcc + 16, 0.75f);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs an empty FastHashMap with the specified initial capacity and
	 * the default load factor of 0.75.
	 *
	 * @param name
	 *            - unused
	 * @param initialCapacity
	 *            - the initial capacity of the list.
	 * @param id
	 *            - field id for event notification
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - element validator. Executed when a new element is added to
	 *            the list
	 * @param listener
	 *            - listener for bubbled events
	 */
	public FastHashMap(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(initialCapacity, 0.75f);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs an empty FastHashMap with the specified initial capacity and
	 * the default load factor of 0.75.
	 *
	 * @param name
	 *            - unused
	 * @param initialCapacity
	 *            - the initial capacity of the list.
	 * @param loadFactor
	 *            - the load factor
	 * @param id
	 *            - field id for event notification
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - element validator. Executed when a new element is added to
	 *            the list
	 * @param listener
	 *            - listener for bubbled events
	 */
	public FastHashMap(int initialCapacity, float loadFactor, String name,
			int id, int minOcc, int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(initialCapacity, loadFactor);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a FastHashMap containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator.
	 * The <tt>SafeHashMap</tt> is created with default load factor (0.75) and
	 * an initial capacity sufficient to hold the mappings in the specified
	 * <tt>Map</tt>.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the map used to fill the list
	 * @param id
	 *            - field id for event notification
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param validator
	 *            - element validator. Executed when a new element is added to
	 *            the list
	 * @param listener
	 *            - listener for bubbled events
	 */
	public FastHashMap(Map<P, Q> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(c);
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
	}

	/**
	 * Constructs a FastHashMap containing the elements of the specified list.
	 * The <tt>SafeHashMap</tt> is created with default load factor (0.75) and
	 * an initial capacity sufficient to hold the mappings in the specified
	 * <tt>Map</tt>.
	 *
	 * @param name
	 *            - unused
	 * @param c
	 *            - the list used to fill the new list
	 * @param id
	 *            - field id for event notification
	 * @param minOcc
	 *            - minimum Size of list
	 * @param maxOcc
	 *            - maximum Size of list
	 * @param listener
	 *            - listener for bubbled events
	 */
	public FastHashMap(AomMap<P, Q> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(c);
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
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int, Object)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			final int fieldId, final int index, final Object key) {
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
	 * @see com.bdaum.aoModeling.runtime.AomMap#replaceWith(java.util.Map)
	 */
	public void replaceWith(Map<? extends P, ? extends Q> replacement) {
		map.clear();
		if (replacement != null)
			map.putAll(replacement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#replaceSilently(java.util.Map)
	 */
	public void replaceSilently(Map<P, Q> replacement) {
		if (replacement != null) {
			if (replacement instanceof SafeHashMap)
				((SafeHashMap<P, Q>) replacement)
						.removeAllValueChangedListeners();
			map = replacement;
		} else
			map.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canClear()
	 */
	public boolean canClear() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canPut(java.lang.Object,
	 * java.lang.Object)
	 */
	public boolean canPut(P key, Q element) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canPutAll(java.util.Map)
	 */
	public boolean canPutAll(Map<? extends P, ? extends Q> t) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canRemove(java.lang.Object)
	 */
	public boolean canRemove(Object key) {
		return containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canReplaceWith(java.util.Map)
	 */
	public boolean canReplaceWith(Map<? extends P, ? extends Q> replacement) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#isValid(java.lang.Object)
	 */
	public boolean isValid(Object o) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#getMaxOcc()
	 */
	public int getMaxOcc() {
		return maxOcc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#getMinOcc()
	 */
	public int getMinOcc() {
		return minOcc;
	}

	@Override
	public void clear() {
		if (map == null)
			map = new HashMap<P, Q>();
		else
			map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Set<Entry<P, Q>> entrySet() {
		return map.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AomMap<?, ?>)
			return ((AomMap<?, ?>) o).equals(map);
		return map.equals(o);
	}

	@Override
	public Q get(Object key) {
		return map.get(key);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<P> keySet() {
		return map.keySet();
	}

	@Override
	public Q put(P key, Q value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends P, ? extends Q> m) {
		map.putAll(m);
	}

	@Override
	public Q remove(Object key) {
		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return getClass().getName() + "{" + map.toString();
	}

	@Override
	public Collection<Q> values() {
		return map.values();
	}

	@Override
	public Object clone() {
		return new FastHashMap<P, Q>(this, null, 0, minOcc, maxOcc, null);
	}

}