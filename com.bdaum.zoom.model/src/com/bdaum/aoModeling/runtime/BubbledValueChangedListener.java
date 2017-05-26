package com.bdaum.aoModeling.runtime;


/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This class is the base for listeners that listen to bubbled events.
 * In addition to the methods of com.bdaum.aoModeling.runtime.ValueChangeListener it 
 * defines a field owner which identifies the owner of this listener.
 * This field can be used to add and remove listeners depending on their
 * owners.
 * 
 * @see com.bdaum.aoModeling.runtime.ValueChangeListener
 */
abstract class BubbledValueChangedListener
	implements ValueChangedListener {

	/** The owner of this listener **/
	
	public AomValueChangedNotifier owner;

	/**
	 * Constructor.
	 * @param owner - the owner of the new listener
	 */
	BubbledValueChangedListener(AomValueChangedNotifier owner) {
		this.owner = owner;
	}
}
