package com.bdaum.aoModeling.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Berthold Daum
 *
 *         (c) 2002 Berthold Daum
 *
 *         This class emulates the type java.util.HashMap. It adds an event
 *         layer, and validation for minOccurs, maxOccurs and element type.
 *         Note: While this class provides the HashMap API and is a subclass of
 *         java.util.HashMap it does not necessarily store its data in a HashMap
 *         structure. All method calls are delegated to a Map-object which can
 *         be any object implementing the Map interface. In particular, the
 *         method replaceSilently() allows using any external Map-object as data
 *         source for this class. This is to cater for certain persistency
 *         packages such as JPA returning proprietary Map-objects from database
 *         calls. Performance optimization techniques such as lazy fetching are
 *         thus preserved.
 *
 */
public class SafeHashMap<P, Q> extends HashMap<P, Q> implements AomMap<P, Q> {

	private static final long serialVersionUID = -4108667359665890998L;

	protected static final String ARG_MAXOCC = "Argument.maxOccurs"; //$NON-NLS-1$

	protected static final String ARG_MINOCC = "Argument.minOccurs"; //$NON-NLS-1$

	protected int id;

	protected int minOcc;

	protected int maxOcc;

	protected String fieldname;

	protected boolean bubbled;

	protected transient ValueChangedListenerList changeListeners;

	protected ElementValidator elementValidator = null;

	private Map<P, Q> map;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public SafeHashMap() {
		super(0);
		map = new HashMap<P, Q>();
	}

	/**
	 * Constructs an empty SafeHashMap with an initial capacity of
	 * Math.min(minOcc+16,maxOcc/0.75) and the default load factor (0.75).
	 *
	 * @param name
	 *            - field name for error reporting
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
	public SafeHashMap(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(minOcc + 16);
		this.id = id;
		this.fieldname = name;
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
		this.elementValidator = validator;
		if (listener != null) {
			this.bubbled = true;
			addOuterChangedListener(listener, id, -1);
		}
	}

	/**
	 * Constructs an empty SafeHashMap with the specified initial capacity.
	 *
	 * @param name
	 *            - field name for error reporting
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
	public SafeHashMap(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(Math.max(minOcc, Math.min(initialCapacity,
				maxOcc)));
		this.id = id;
		this.fieldname = name;
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
		this.elementValidator = validator;
		if (listener != null) {
			this.bubbled = true;
			addOuterChangedListener(listener, id, -1);
		}
	}

	/**
	 * Constructs a SafeHashMap containing the elements of the specified map, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>SafeHashMap</tt> is created with default load factor (0.75) and an
	 * initial capacity sufficient to hold the mappings in the specified
	 * <tt>Map</tt>.
	 *
	 * @param name
	 *            - field name for error reporting
	 * @param c
	 *            - the collection used to fill the list
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
	public SafeHashMap(Map<P, Q> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(c);
		if (map.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (map.size() < minOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MINOCC, String.valueOf(minOcc), name));
		this.id = id;
		this.fieldname = name;
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
		this.elementValidator = validator;
		if (listener != null) {
			this.bubbled = true;
			addOuterChangedListener(listener, id, -1);
		}
	}

	/**
	 * Constructs a SafeHashMap containing the elements of the specified map.
	 * The <tt>SafeHashMap</tt> is created with default load factor (0.75) and
	 * an initial capacity sufficient to hold the mappings in the specified
	 * <tt>Map</tt>.
	 *
	 * @param name
	 *            - field name for error reporting
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
	public SafeHashMap(AomMap<P, Q> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		map = new HashMap<P, Q>(c);
		if (map.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (map.size() < minOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MINOCC, String.valueOf(minOcc), name));
		this.id = id;
		this.fieldname = name;
		this.minOcc = minOcc;
		this.maxOcc = maxOcc;
		this.elementValidator = c.getElementValidator();
		if (listener != null) {
			this.bubbled = true;
			addOuterChangedListener(listener, id, -1);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canClear()
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
		if (map == null)
			map = new HashMap<P, Q>();
		if (bubbled) {
			Iterator<Q> it = values().iterator();
			while (it.hasNext()) {
				Q element = it.next();
				if (element instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) element)
							.removeOuterChangedListeners(this);

			}
		}
		if (map.size() > 0) {
			SafeHashMap<P, Q> oldMap = new SafeHashMap<P, Q>(this, fieldname,
					id, minOcc, maxOcc, null);
			map.clear();
			if (minOcc > 0)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			fireValueChanged(this, oldMap, this, id, -1);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canRemove(java.lang.Object)
	 */
	public boolean canRemove(Object key) {
		return (containsKey(key) && map.size() > minOcc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Map#remove(Object)
	 */
	@Override
	public Q remove(Object key) {
		Q o = map.remove(key);
		if (o != null) {
			if (map.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			if (bubbled && o instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) o).removeOuterChangedListeners(this);
			fireValueChanged(this, o, null, id, key);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canPut(P, Q)
	 */
	public boolean canPut(P key, Q element) {
		return (isValid(element) && (map.size() < maxOcc || !containsKey(key)));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Map#put(P, Q)
	 */
	@Override
	public Q put(P key, Q element) {
		if (elementValidator != null)
			elementValidator.validate(element);
		Q o = map.put(key, element);
		if (map.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), fieldname));

		if (bubbled && element instanceof AomValueChangedNotifier)
			((AomValueChangedNotifier) element).addOuterChangedListener(this,
					id, -1, key);
		if (o == null) {
			if (element != null)
				fireValueChanged(this, o, element, id, key);
			return o;
		}
		if (!o.equals(element))
			fireValueChanged(this, o, element, id, key);
		return o;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (AomValueChangedNotifier, Object, Object, int, int)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, Object key) {
		fireValueChanged(source, oldValue, newValue, fieldId, -1, key, null);
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
		fireValueChanged(source, oldValue, newValue, fieldId, index, null, null);
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
		if (changeListeners != null && changeListeners.size() > 0
				&& !isCyclic(wrappedEvent)) {
			changeListeners.fireValueChangedEvent(new ValueChangedEvent(source,
					oldValue, newValue, fieldId, index, key, wrappedEvent));
		}
	}

	/*
	 * (non-Javadoc) Checks if an event is cyclic. @param wrappedEvent - the
	 * inner event to be checked for cycles @return boolean - true if the
	 * current AomValueChangedNotifier instance is in the event path
	 */
	private boolean isCyclic(ValueChangedEvent wrappedEvent) {
		while (wrappedEvent != null) {
			if (wrappedEvent.getSource() == this)
				return true;
			wrappedEvent = wrappedEvent.getWrappedEvent();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addValueChangedListener
	 * (ValueChangedListener)
	 */
	public void addValueChangedListener(ValueChangedListener listener) {
		if (changeListeners == null)
			changeListeners = new ValueChangedListenerList(3);
		changeListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeValueChangedListener(ValueChangedListener)
	 */
	public void removeValueChangedListener(ValueChangedListener listener) {
		if (changeListeners != null)
			changeListeners.remove(listener);
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
		addOuterChangedListener(source, fieldId, -1, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId, int index) {
		addOuterChangedListener(source, fieldId, index, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, Object)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId, Object key) {
		addOuterChangedListener(source, fieldId, -1, key);
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
		if (bubbled) {
			if (changeListeners == null)
				changeListeners = new ValueChangedListenerList(3);
			if (changeListeners.findBubbledValueChangedListener(source) == null) {
				changeListeners.append(new BubbledValueChangedListener(source) {
					public void valueChanged(ValueChangedEvent e) {
						owner.fireValueChanged(SafeHashMap.this, null, null,
								fieldId, index, key, e);
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeOuterChangedListeners(AomValueChangedNotifier)
	 */
	public void removeOuterChangedListeners(AomValueChangedNotifier source) {
		if (changeListeners != null)
			changeListeners.remove(source);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#getElementValidator()
	 */
	public ElementValidator getElementValidator() {
		return elementValidator;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomList#setElementValidator(com.bdaum.aoModeling
	 * .runtime.ElementValidator)
	 */
	public void setElementValidator(ElementValidator validator) {
		elementValidator = validator;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#isBubbled()
	 */
	public boolean isBubbled() {
		return bubbled;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canPutAll(java.util.Map)
	 */
	public boolean canPutAll(Map<? extends P, ? extends Q> t) {
		if (t == null)
			return false;
		if (maxOcc < Integer.MAX_VALUE) {
			Map<P, Q> m = new HashMap<P, Q>(map);
			for (P key : t.keySet()) {
				Q value = t.get(key);
				if (!isValid(value))
					return false;
				m.put(key, value);
				if (m.size() > maxOcc)
					return false;
			}
		} else if (elementValidator != null) {
			Iterator<? extends Q> it = t.values().iterator();
			while (it.hasNext())
				if (!isValid(it.next()))
					return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Map#putAll(Map)
	 */
	@Override
	public void putAll(Map<? extends P, ? extends Q> t) {
		if (t != null) {
			for (Map.Entry<? extends P, ? extends Q> entry : t.entrySet())
				put(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#canReplaceWith(java.util.Map)
	 */
	public boolean canReplaceWith(Map<? extends P, ? extends Q> replacement) {
		if (replacement == null || replacement.size() > maxOcc
				|| replacement.size() < minOcc)
			return false;
		Iterator<? extends Q> it = replacement.values().iterator();
		while (it.hasNext())
			if (!isValid(it.next()))
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#replaceWith(java.util.Map)
	 */
	public void replaceWith(Map<? extends P, ? extends Q> replacement) {
		if (replacement != null) {
			if (replacement.size() < minOcc && !isEmpty())
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			Set<P> oldKeys = new HashSet<P>(map.keySet());
			Iterator<P> it = oldKeys.iterator();
			while (it.hasNext()) {
				P key = it.next();
				if (replacement.get(key) == null) {
					Q o = map.remove(key);
					if (bubbled && o instanceof AomValueChangedNotifier)
						((AomValueChangedNotifier) o)
								.removeOuterChangedListeners(this);
					fireValueChanged(this, o, null, id, key);
				}
			}
			map.putAll(replacement);
		} else {
			if (map.isEmpty())
				return;
			clear();
		}
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

	protected void removeAllValueChangedListeners() {
		changeListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomMap#isValid(java.lang.Object)
	 */
	public boolean isValid(Object o) {
		if (elementValidator == null)
			return true;
		try {
			elementValidator.validate(o);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
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
	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public Collection<Q> values() {
		return map.values();
	}

	@Override
	public Object clone() {
		return new SafeHashMap<P, Q>(this, fieldname, id, minOcc, maxOcc, null);
	}

}