package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 */
public interface AomValueChangedNotifier {

	/**
	 * Adds a listener that listens to ValueChangedEvent.
	 * If the listener is already registered this call has no effect.
	 * @param listener - The listener
	 */
	public void addValueChangedListener(ValueChangedListener listener);

	/**
	 * Removes the listener if it exists.
	 * @param listener - The listener
	 */
	public void removeValueChangedListener(ValueChangedListener listener);

	/**
	 * Adds an outer listener that listens to ValueChangedEvent.
	 * @param source - the origin of the event
	 * @param fieldId - the field id as defined in the asset class
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source, int fieldId);

	/**
	 * Adds an outer listener that listens to ValueChangedEvent.
	 * @param source - the origin of the event
	 * @param fieldId - the field id as defined in the asset class
	 * @param index - the array or list index, -1 otherwise
	 */
	public void addOuterChangedListener(
		AomValueChangedNotifier source,
		int fieldId,
		int index);

	/**
	 * Adds an outer listener that listens to ValueChangedEvent.
	 * @param source - the origin of the event
	 * @param fieldId - the field id as defined in the asset class
	 * @param key - the map key
	 */
	public void addOuterChangedListener(
		AomValueChangedNotifier source,
		int fieldId,
		Object key);

	/**
	 * Adds an outer listener that listens to ValueChangedEvent.
	 * @param source - the origin of the event
	 * @param fieldId - the field id as defined in the asset class
	 * @param index - the array or list index, -1 otherwise
	 * @param key - the map key
	 */
	public void addOuterChangedListener(
		AomValueChangedNotifier source,
		int fieldId,
		int index,
		Object key);

	/**
	 * Removes all outer listener belonging to the specified source.
	 * @param source - the origin of events
	 */
	public void removeOuterChangedListeners(AomValueChangedNotifier source);
	
	
	/**
	 * Notifies listeners about a value change
	 * @param source - the origin of the change
	 * @param oldValue - the old value
	 * @param newValue - the new value
	 * @param fieldId - the field id as defined in the asset class
	 * @param index - the array or list index, -1 otherwise
	 */
	public void fireValueChanged(
		AomValueChangedNotifier source,
		Object oldValue,
		Object newValue,
		int fieldId,
		int index);

	/**
	 * Notifies listeners about a value change
	 * @param source - the origin of the change
	 * @param oldValue - the old value
	 * @param newValue - the new value
	 * @param fieldId - the field id as defined in the asset class
	 * @param key - the map key
	 */
	public void fireValueChanged(
		AomValueChangedNotifier source,
		Object oldValue,
		Object newValue,
		int fieldId,
		Object key);
				
	/**
	 * Notifies listeners about a value change
	 * @param source - the origin of the change
	 * @param oldValue - the old value
	 * @param newValue - the new value
	 * @param fieldId - the field id as defined in the asset class
	 * @param index - the array or list index, -1 otherwise
	 * @param key - the map key
	 * @param wrappedEvent - an inner event in case of event bubbling or null
	 */
	public void fireValueChanged(
		AomValueChangedNotifier source,
		Object oldValue,
		Object newValue,
		int fieldId,
		int index,
		Object key,
		ValueChangedEvent wrappedEvent);
	/**
	 * Returns true if this object supports event bubbling
	 * @return boolean
	 */
	public boolean isBubbled();

}
