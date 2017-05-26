package com.bdaum.aoModeling.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Berthold Daum
 *
 *         (c) 2007 Berthold Daum
 *
 *         This class emulates the type java.util.ArrayList. It adds an event
 *         layer, and validation for minOccurs, maxOccurs and element type.
 *         Note: While this class provides the ArrayList API and is a subclass
 *         of java.util.ArrayList it does not necessarily store its data in an
 *         array structure. All method calls are delegated to a List-object
 *         which can be any object implementing the List interface. In
 *         particular, the method replaceSilently() allows using any external
 *         List-object as data source for this class. This is to cater for
 *         certain persistency packages such as JPA returning proprietary
 *         List-objects from database calls. Performance optimization techniques
 *         such as lazy fetching are thus preserved.
 *
 */
public class SafeArrayList<T> extends ArrayList<T> implements AomList<T> {

	private static final long serialVersionUID = 5386582105018199747L;

	private class ListItr<U> implements ListIterator<U> {

		ListIterator<U> it;

		ListItr(ListIterator<U> it) {
			this.it = it;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			int index = it.nextIndex();
			Object currentObject = (index < 0) ? null : list.get(index);
			it.remove();
			if (list.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			if (bubbled && currentObject instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) currentObject)
						.removeOuterChangedListeners(SafeArrayList.this);
			if (currentObject != null)
				fireValueChanged(SafeArrayList.this, currentObject, null, id,
						index);
			currentObject = null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return it.hasNext();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#next()
		 */
		public U next() {
			return it.next();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex() {
			return it.nextIndex();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex() {
			return it.previousIndex();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#hasPrevious()
		 */
		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#previous()
		 */
		public U previous() {
			return it.previous();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		public void add(U o) {
			int index = list.size();
			it.add(o);
			if (list.size() > maxOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MAXOCC, String.valueOf(maxOcc), fieldname));
			if (bubbled && o instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) o).addOuterChangedListener(
						SafeArrayList.this, id, index);
			fireValueChanged(SafeArrayList.this, null, o, id, index);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		public void set(U element) {
			int index = it.nextIndex();
			Object currentObject = (index < 0) ? null : get(index);
			it.set(element);
			if (bubbled) {
				if (currentObject instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) currentObject)
							.removeOuterChangedListeners(SafeArrayList.this);
				if (element instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) element)
							.addOuterChangedListener(SafeArrayList.this, id,
									index);
			}
			if (currentObject == null) {
				if (element != null)
					fireValueChanged(SafeArrayList.this, null, element, id,
							index);
				return;
			}
			if (!currentObject.equals(element))
				fireValueChanged(SafeArrayList.this, currentObject, element,
						id, index);
		}
	}

	protected static final String ARG_MAXOCC = "Argument.maxOccurs"; //$NON-NLS-1$

	protected static final String ARG_MINOCC = "Argument.minOccurs"; //$NON-NLS-1$

	protected int id;

	protected int minOcc;

	protected int maxOcc;

	protected String fieldname;

	protected boolean bubbled;

	protected transient ValueChangedListenerList changeListeners;

	protected ElementValidator elementValidator = null;

	private List<T> list;

	/**
	 * Default constructor. Only used to be serializable.
	 */
	public SafeArrayList() {
		super(0);
		list = new ArrayList<T>();
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
	public SafeArrayList(String name, int id, int minOcc, int maxOcc,
			ElementValidator validator, AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(Math.min(minOcc + 10, maxOcc));
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
	public SafeArrayList(int initialCapacity, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(Math.max(minOcc, Math.min(initialCapacity,
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
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>SafeArrayList</tt> instance has an initial capacity of 110% the size
	 * of the specified collection.
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
	public SafeArrayList(Collection<T> c, String name, int id, int minOcc,
			int maxOcc, ElementValidator validator,
			AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(c);
		if (list.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (list.size() < minOcc)
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
	 * <tt>SafeArrayList</tt> instance has an initial capacity of 110% the size
	 * of the specified list. The element validator of the new list is inherited
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
	public SafeArrayList(AomList<T> c, String name, int id, int minOcc,
			int maxOcc, AomValueChangedNotifier listener) {
		super(0);
		list = new ArrayList<T>(c);
		if (list.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), name));
		if (list.size() < minOcc)
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
	 * @see com.bdaum.aoModeling.runtime.AomList#canAdd(int, T)
	 */
	public boolean canAdd(int index, T element) {
		return (index >= 0 && index <= list.size() && canAdd(element));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#add(int, T)
	 */
	@Override
	public void add(int index, T element) {
		if (elementValidator != null)
			elementValidator.validate(element);
		list.add(index, element);
		if (list.size() > maxOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MAXOCC, String.valueOf(maxOcc), fieldname));

		if (bubbled && element instanceof AomValueChangedNotifier)
			((AomValueChangedNotifier) element).addOuterChangedListener(this,
					id, index);
		fireValueChanged(this, null, element, id, index);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomCollection#canAdd(java.lang.Object)
	 */
	public boolean canAdd(T element) {
		return (isValid(element) && list.size() < maxOcc);
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
		int index = list.size();
		list.add(o);
		if (list.size() > maxOcc)
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
		if (list.size() + c.size() > maxOcc)
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
		int s = list.size();
		if (list.addAll(c)) {
			if (list.size() > maxOcc)
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
	 * @see com.bdaum.aoModeling.runtime.AomList#canAddAll(int,
	 * java.util.Collection)
	 */
	public boolean canAddAll(int index, Collection<? extends T> c) {
		return (index >= 0 && index <= list.size() && canAddAll(c));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#addAll(int, Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (elementValidator != null) {
			Iterator<? extends T> it = c.iterator();
			while (it.hasNext())
				elementValidator.validate(it.next());
		}
		if (list.addAll(index, c)) {
			if (list.size() > maxOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MAXOCC, String.valueOf(maxOcc), fieldname));
			if (bubbled) {
				Iterator<? extends T> it = c.iterator();
				int cnt = index;
				while (it.hasNext()) {
					T element = it.next();
					if (element instanceof AomValueChangedNotifier)
						((AomValueChangedNotifier) element)
								.addOuterChangedListener(this, id, cnt++);
				}
			}
			fireValueChanged(this, null, c, id, index);
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
		if (list == null)
			list = new ArrayList<T>();
		if (bubbled) {
			Iterator<T> it = list.iterator();
			while (it.hasNext()) {
				T element = it.next();
				if (element instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) element)
							.removeOuterChangedListeners(this);

			}
		}
		if (list.size() > 0) {
			SafeArrayList<T> oldList = new SafeArrayList<T>(list, fieldname,
					id, minOcc, maxOcc, null, null);
			list.clear();
			if (minOcc > 0)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			fireValueChanged(this, oldList, this, id, -1);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canRemove(int)
	 */
	public boolean canRemove(int index) {
		return (index >= 0 && index < list.size() && list.size() > minOcc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#remove(int)
	 */
	@Override
	public T remove(int index) {
		T o = list.remove(index);
		if (list.size() < minOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MINOCC, String.valueOf(minOcc), fieldname));

		if (bubbled && o instanceof AomValueChangedNotifier)
			((AomValueChangedNotifier) o).removeOuterChangedListeners(this);
		if (o != null)
			fireValueChanged(this, o, null, id, index);
		return o;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.AbstractList#removeRange(int, int)
	 */
	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		List<T> s = list.subList(fromIndex, toIndex);
		if (bubbled) {
			Iterator<T> it = s.iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof AomValueChangedNotifier)
					((AomValueChangedNotifier) element)
							.removeOuterChangedListeners(this);
			}
		}
		for (int i = fromIndex; i < toIndex; i++)
			list.remove(fromIndex);
		if (list.size() < minOcc)
			throw new IllegalArgumentException(ModelMessages.getString(
					ARG_MINOCC, String.valueOf(minOcc), fieldname));
		fireValueChanged(this, s, null, id, fromIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.aoModeling.runtime.AomList#canSet(int, java.lang.Object)
	 */
	public boolean canSet(int index, Object element) {
		return (index >= 0 && index < list.size() && isValid(element));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#set(int, Object)
	 */
	@Override
	public T set(int index, T element) {
		if (elementValidator != null)
			elementValidator.validate(element);
		T o = list.set(index, element);
		if (bubbled) {
			if (o instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) o).removeOuterChangedListeners(this);
			if (element instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) element).addOuterChangedListener(
						this, id, index);
		}
		if (o == null) {
			if (element != null)
				fireValueChanged(this, o, element, id, index);
			return o;
		}
		if (!o.equals(element))
			fireValueChanged(this, o, element, id, index);
		return o;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.aoModeling.runtime.AomCollection#canRemove(java.lang.Object)
	 */
	public boolean canRemove(Object o) {
		return (list.size() > minOcc && list.indexOf(o) >= 0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	@Override
	public boolean remove(Object o) {
		int i = list.indexOf(o);
		if (i >= 0) {
			list.remove(i);
			if (list.size() < minOcc)
				throw new IllegalArgumentException(ModelMessages.getString(
						ARG_MINOCC, String.valueOf(minOcc), fieldname));
			if (bubbled && o instanceof AomValueChangedNotifier)
				((AomValueChangedNotifier) o).removeOuterChangedListeners(this);
			fireValueChanged(this, o, null, id, i);
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
						owner.fireValueChanged(owner, SafeArrayList.this,
								SafeArrayList.this, fieldId, index, key, e);
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
			Iterator<T> it1 = list.iterator();
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
			list.clear();
			list.addAll(replacement);
		} else {
			if (list.isEmpty())
				return;
			clear();
		}
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
		return listIterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator() {
		return new ListItr<T>(list.listIterator());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		return new ListItr<T>(list.listIterator(index));
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
		return list.contains(elem);
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		if (list instanceof ArrayList)
			((ArrayList<T>) list).ensureCapacity(minCapacity);
	}

	@Override
	public T get(int index) {
		return list.get(index);
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
	public int lastIndexOf(Object elem) {
		return list.lastIndexOf(elem);
	}

	@Override
	public int size() {
		return list.size();
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
	public void trimToSize() {
		if (list instanceof ArrayList)
			((ArrayList<T>) list).trimToSize();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AomList<?>)
			return ((AomList<?>) o).equals(list);
		return list.equals(o);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public String toString() {
		return getClass().getName() + "{" + list.toString();
	}

	@Override
	public Object clone() {
		return new SafeArrayList<T>(this, fieldname, id, minOcc, maxOcc, null);
	}

}