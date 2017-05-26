package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This interface describes listeners for value modification events.
 */
public interface ValueChangedListener {
	
	/**
	 * Accepts ValueChangedEvents.
	 * @param e - the event issued on value modification
	 */
	public void valueChanged(ValueChangedEvent e);

}
