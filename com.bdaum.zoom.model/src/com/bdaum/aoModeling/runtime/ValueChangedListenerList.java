package com.bdaum.aoModeling.runtime;

import java.lang.reflect.Array;

/**
 * This class is used to maintain a list of change value listeners.
 * 
 * Use the <code>fireValueChangedEvent</code> method when notifying listeners.
 *  
 */
public class ValueChangedListenerList implements Cloneable {
    /**
     * The initial capacity of the list. Always >= 1.
     */
    private int capacity;

    /**
     * The current number of listeners.
     */
    private int size;

    /**
     * The list of listeners.
     */
    private ValueChangedListener[] listeners = null;

    /**
     * The empty array instance
     */
    private static final ValueChangedListener[] EmptyArray = new ValueChangedListener[0];

    /**
     * Creates a listener list with an initial capacity of 1.
     */
    public ValueChangedListenerList() {
        this(1);
    }

    /**
     * Creates a listener list with the given initial capacity.
     * 
     * @param capacity
     *            the number of listeners which this list can initially accept
     *            without growing its internal representation; should be at
     *            least 1
     */
    public ValueChangedListenerList(int capacity) {
        this.capacity = (capacity >= 1) ? capacity : 1;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
    	ValueChangedListenerList clone = (ValueChangedListenerList) super.clone();
    	if (listeners != null)
    		clone.listeners = copyOf(listeners, capacity);
		return clone;
    }
    
    @SuppressWarnings("unchecked")
	private static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }
    
    @SuppressWarnings("unchecked")
	private static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }


    /**
     * Adds the given listener to this list. Has no effect if an identical
     * listener is already registered.
     * 
     * @param listener
     *            the listener
     */
    public void add(ValueChangedListener listener) {
        if (listener != null) {
            if (size == 0)
                listeners = new ValueChangedListener[capacity];
            else {
                // check for duplicates using identity
                for (int i = 0; i < size; ++i) {
                    if (listeners[i] == listener)
                        return;
                }
                // grow array if necessary
                if (size == listeners.length) {
                    System.arraycopy(listeners, 0,
                            listeners = new ValueChangedListener[size * 2 + 1],
                            0, size);
                }
            }
            listeners[size] = listener;
            size++;
        }
    }

    /**
     * Appends the given listener to this list. Does not check for duplicates.
     * 
     * @param listener
     *            the listener
     */
    public void append(ValueChangedListener listener) {
        if (listener != null) {
            if (size == 0)
                listeners = new ValueChangedListener[capacity];
            else if (size == listeners.length)
                // grow array if necessary
                System.arraycopy(listeners, 0,
                        listeners = new ValueChangedListener[size * 2 + 1], 0,
                        size);
            listeners[size] = listener;
            size++;
        }
    }

    /**
     * Searches for a bubbled value changed listener whose owner matches the
     * specified event source
     * 
     * @param source
     *            The event source when adding a BubbledValueChangedListener
     * @return the bubbled change value listener matching the query or null
     *  
     */
    public BubbledValueChangedListener findBubbledValueChangedListener(
            AomValueChangedNotifier source) {
        if (source != null && size > 0)
            for (int i = 0; i < size; ++i) {
                if (listeners[i] instanceof BubbledValueChangedListener
                        && ((BubbledValueChangedListener) listeners[i]).owner == source)
                    return (BubbledValueChangedListener) listeners[i];
            }
        return null;
    }

    /**
     * Removes all listeners from this list.
     */
    public void clear() {
        size = 0;
        listeners = null;
    }

    /**
     * Returns an array containing all the registered listeners, in the order in
     * which they were added.
     * <p>
     * The resulting array is unaffected by subsequent adds or removes.
     * 
     * @return the list of registered listeners
     */
    public ValueChangedListener[] getListeners() {
        if (size == 0)
            return EmptyArray;
        ValueChangedListener[] result = new ValueChangedListener[size];
        System.arraycopy(listeners, 0, result, 0, size);
        return result;
    }

    /**
     * Fires the specified event to all listeners registered at the time of the
     * invocation
     * 
     * @param event -
     *            the value changed event
     */
    public void fireValueChangedEvent(ValueChangedEvent event) {
        switch (size) {
        case 0:
            return;
        case 1:
            listeners[0].valueChanged(event);
            return;
        default:
            ValueChangedListener[] snapshot = new ValueChangedListener[size];
            System.arraycopy(listeners, 0, snapshot, 0, size);
            for (int i = 0; i < size; i++)
                snapshot[i].valueChanged(event);
            break;
        }
    }

    /**
     * Returns whether this listener list is empty.
     * 
     * @return <code>true</code> if there are no registered listeners, and
     *         <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes the given listener from this list. Has no effect if an identical
     * listener was not already registered.
     * 
     * @param listener
     *            the listener
     * @return <code>true</code> if a listener was removed
     */
    public boolean remove(ValueChangedListener listener) {
        for (int i = 0; i < size; ++i) {
            if (listeners[i] == listener) {
                if (size == 1) {
                    listeners = null;
                    size = 0;
                } else {
                    System
                            .arraycopy(listeners, i + 1, listeners, i, --size
                                    - i);
                    listeners[size] = null;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all bubbled change value listeners whose owner match the
     * specified event source
     * 
     * @param source
     *            the event source
     * @return the number of removed listeners
     */
    public int remove(AomValueChangedNotifier source) {
        int removed = 0;
        for (int i = 0; i < size; ++i) {
            if (listeners[i] instanceof BubbledValueChangedListener
                    && ((BubbledValueChangedListener) listeners[i]).owner == source) {
                ++removed;
                continue;
            }
            if (removed > 0)
                listeners[i - removed] = listeners[i];
        }
        size -= removed;
        if (size == 0)
            listeners = null;
        return removed;
    }

    /**
     * Returns the number of registered listeners.
     * 
     * @return the number of registered listeners
     */
    public int size() {
        return size;
    }
}