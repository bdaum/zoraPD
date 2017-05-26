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
 *         This class emulates the type java.util.HashSet. It adds an event
 *         layer, and validation for minOccurs, maxOccurs and element type.
 *         Note: While this class provides the HashSet API and is a subclass of
 *         java.util.HashSet it does not necessarily store its data in a HashSet
 *         structure. All method calls are delegated to a Set-object which can
 *         be any object implementing the Set interface. In particular, the
 *         method replaceSilently() allows using any external Set-object as data
 *         source for this class. This is to cater for certain persistency
 *         packages such as JPA returning proprietary Set-objects from database
 *         calls. Performance optimization techniques such as lazy fetching are
 *         thus preserved.
 *
 */
public class SafeHashSet<T> extends HashSet<T> implements AomSet<T> {

	private class Itr<U> implements Iterator<U> {
		private Iterator<U> itr;

		private U currentElement;

		public Itr(Iterator<U> itr) {
			this.itr = itr;
		}

		public boolean hasNext() {
			return itr.hasNext();
		}

		public U next() {
			currentElement = itr.next();
			return currentElement;
		}

		public void remove() {
			itr.remove();
			if (set.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			if (bubbled && currentElement instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) currentElement)
						.removeOuterChangedListeners(SafeHashSet.this);
			if (currentElement != null)
				fireValueChanged(SafeHashSet.this, currentElement, null, id, -1);
			currentElement = null;
		}
	}

	private static final long serialVersionUID = 5386582105018199747L;

	protected static final String ARG_MAXOCC = "Argument.maxOccurs"; //$NON-NLS-1$

	protected static final String ARG_MINOCC = "Argument.minOccurs"; //$NON-NLS-1$

	protected int id;

	protected int minOcc;

	protected int maxOcc;

	protected String fieldname;

	protected boolean bubbled;

	protected transient ValueChangedListenerList changeListeners;

	protected ElementValidator elementValidator = null;

	private Set<T> set;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public SafeHashSet() {
		super(0);
		set = new HashSet<T>();
	}

	/**
	 * Constructs an empty list with an initial capacity of
	 * Math.min(minOcc+10,maxOcc).
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
	public SafeHashSet(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(Math.min(minOcc + 10, maxOcc));
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
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param name
	 *            - field name for error reporting
	 * @param initialCapacity
	 *            the initial capacity of the list.
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
	public SafeHashSet(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(
				Math.max(minOcc, Math.min(initialCapacity, maxOcc)));
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
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>SafeHashSet</tt> instance has an initial capacity of 110% the size of
	 * the specified collection.
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
	public SafeHashSet(Collection<T> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(c);
		if (set.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (set.size() < minOcc)
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
	 * Constructs a list containing the elements of the specified list. The
	 * <tt>SafeHashSet</tt> instance has an initial capacity of 110% the size of
	 * the specified list. The element validator of the new list is inherited
	 * from the specified list.
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
	public SafeHashSet(AomList<T> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		set = new HashSet<T>(c);
		if (set.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (set.size() < minOcc)
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
		if (elementValidator != null)
			elementValidator.validate(o);
		int index = set.size();
		set.add(o);
		if (set.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), fieldname));
		if (bubbled && o instanceof AomValueChangedNotifier)
			((AomValueChangedNotifier) o).addOuterChangedListener(this, id,
					index);
		fireValueChanged(this, null, o, id, index);
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#canAddAll(java.util.Collection
	 * )
	 */
	public boolean canAddAll(Collection<? extends T> c) {
		if (set.size() + c.size() > maxOcc)
			return false;
		if (elementValidator != null) {
			Iterator<? extends T> it = c.iterator();
			while (it.hasNext())
				if (!isValid(it.next()))
					return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#addAll(Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (elementValidator != null) {
			Iterator<? extends T> it = c.iterator();
			while (it.hasNext())
				elementValidator.validate(it.next());
		}
		int s = set.size();
		if (set.addAll(c)) {
			if (set.size() > maxOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MAXOCC, String.valueOf(maxOcc), fieldname));
			if (bubbled) {
				Iterator<? extends T> it = c.iterator();
				int cnt = s;
				while (it.hasNext()) {
					T element = it.next();
					if (element instanceof AomValueChangedNotifier)
						((AomValueChangedNotifier) element)
								.addOuterChangedListener(this, id, cnt++);
				}
			}
			fireValueChanged(this, null, c, id, s);
			return true;
		}
		return false;
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
		if (bubbled) {
			Iterator<T> it = set.iterator();
			while (it.hasNext()) {
				T element = it.next();
				if (element instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) element)
							.removeOuterChangedListeners(this);

			}
		}
		if (set.size() > 0) {
			SafeHashSet<T> oldSet = new SafeHashSet<T>(set, fieldname, id,
					minOcc, maxOcc, null, null);
			set.clear();
			if (minOcc > 0)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			fireValueChanged(this, oldSet, this, id, -1);
		}
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
		if (set.remove(o)) {
			if (set.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			if (bubbled && o instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) o).removeOuterChangedListeners(this);
			fireValueChanged(this, o, null, id, -1);
			return true;
		}
		return false;
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
	 * (com.bdaum.aoModeling.runtime.AomValueChangedNotifier, java.lang.Object,
	 * java.lang.Object, int, java.lang.Object)
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
	 * (AomValueChangedNotifier, Object, Object, int, int, Object,
	 * ValueChangedEvent)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index,
			Object key, ValueChangedEvent wrappedEvent) {
		if (changeListeners != null && changeListeners.size() > 0
				&& !isCyclic(wrappedEvent))
			changeListeners.fireValueChangedEvent(new ValueChangedEvent(source,
					oldValue, newValue, fieldId, index, null, wrappedEvent));
	}

	/**
	 * Checks if the event is cyclic.
	 *
	 * @param wrappedEvent
	 *            - the inner event to be checked for cycles
	 * @return boolean - true if the current AomValueChangedNotifier instance is
	 *         in the event path
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
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#
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
		addOuterChangedListener(source, fieldId, -1);
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
			int fieldId, final Object key) {
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
						owner.fireValueChanged(owner, SafeHashSet.this,
								SafeHashSet.this, fieldId, index, key, e);
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#
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
	 * @see com.bdaum.aoModeling.runtime.AomCollection#isValid(java.lang.Object)
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
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#isBubbled()
	 */
	public boolean isBubbled() {
		return bubbled;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#replaceWith(java.util.Collection
	 * <T>)
	 */
	public void replaceWith(Collection<? extends T> replacement) {
		if (replacement != null) {
			if (replacement.size() > maxOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MAXOCC, String.valueOf(maxOcc), fieldname));
			if (replacement.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			Iterator<T> it1 = set.iterator();
			Iterator<? extends T> it2 = replacement.iterator();
			T o1, o2;
			int index = 0;
			while (true) {
				o1 = (it1.hasNext()) ? it1.next() : null;
				o2 = (it2.hasNext()) ? it2.next() : null;
				if (o1 == null && o2 == null)
					break;
				if (o1 != null && !o1.equals(o2)) {
					// signal removal
					if (bubbled && o1 instanceof AomValueChangedNotifier)
						((AomValueChangedNotifier) o1)
								.removeOuterChangedListeners(this);
					fireValueChanged(this, o1, null, id, index);
				}
				if (o2 != null && !o2.equals(o1)) {
					// signal insertion
					if (bubbled && o2 instanceof AomValueChangedNotifier)
						((AomValueChangedNotifier) o2).addOuterChangedListener(
								this, id, index);
					fireValueChanged(this, null, o2, id, index);
				}
				++index;
			}
			set.clear();
			set.addAll(replacement);
		} else {
			if (set.isEmpty())
				return;
			clear();
		}
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

	protected void removeAllValueChangedListeners() {
		changeListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new Itr<T>(set.iterator());
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
		return new SafeHashSet<T>(this, fieldname, id, minOcc, maxOcc,
				elementValidator, null);
	}
}