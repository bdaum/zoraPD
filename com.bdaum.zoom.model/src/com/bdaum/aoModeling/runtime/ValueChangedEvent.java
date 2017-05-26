package com.bdaum.aoModeling.runtime;

import java.util.EventObject;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002 Berthold Daum
 * 
 *         This class implements event objects for value modification
 *         notification.
 */
public class ValueChangedEvent extends EventObject {

	private static final long serialVersionUID = 3653126082487738946L;
	private int fieldId;
	private Object oldValue;
	private Object newValue;
	private int index;
	private ValueChangedEvent wrappedEvent;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            - the sender of this event
	 * @param oldValue
	 *            - the former value (or null when a value is appended)
	 * @param newValue
	 *            - the new value ( or null wehn a value is removed)
	 * @param fieldId
	 *            - the field identifier
	 * @param index
	 *            - the array or list index, -1 for scalars
	 * @param wrappedEvent
	 *            - a wrapped event from inner objects or null
	 */
	public ValueChangedEvent(AomValueChangedNotifier source, Object oldValue,
			Object newValue, int fieldId, int index, Object key,
			ValueChangedEvent wrappedEvent) {
		super(source);
		this.fieldId = fieldId;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.index = index;
		this.wrappedEvent = wrappedEvent;
	}

	/**
	 * Returns the fieldId.
	 * 
	 * @return int
	 */
	public int getFieldId() {
		return fieldId;
	}

	/**
	 * Returns the newValue.
	 * 
	 * @return Object
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * Returns the oldValue.
	 * 
	 * @return Object
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * Returns the index.
	 * 
	 * @return int
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the wrappedEvent.
	 * 
	 * @return ValueChangeEvent
	 */
	public ValueChangedEvent getWrappedEvent() {
		return wrappedEvent;
	}

	/**
	 * Returns a String representation of this EventObject.
	 * 
	 * @return A a String representation of this EventObject.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[source=").append(source).append(", fieldId=").append(
				fieldId).append(", oldValue=").append(oldValue).append(
				", newValue=").append(newValue);
		if (index >= 0)
			sb.append(", index=").append(index);
		if (wrappedEvent != null)
			sb.append(", wrappedEvent=").append(wrappedEvent);
		sb.append("]");
		return sb.toString();
	}

}
