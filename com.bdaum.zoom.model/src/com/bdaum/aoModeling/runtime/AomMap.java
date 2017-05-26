package com.bdaum.aoModeling.runtime;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Berthold Daum
 * 
 * (c) 2002 Berthold Daum
 * 
 * This interface defines a wrapping around java.util.Map. It includes event
 * managing and element validation.
 * 
 * @see java.util.Map
 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier
 */
public interface AomMap<P,Q> extends Map<P,Q>, AomValueChangedNotifier, Serializable {

    /**
     * Returns the current element validator
     * 
     * @return ElementValidator - the current element validator
     */
    public ElementValidator getElementValidator();

    /**
     * Sets a new element validator
     * 
     * @param validator -
     *            the new element validator
     */
    public void setElementValidator(ElementValidator validator);

    /**
     * Replaces all elements in the instance with the elements of the specified map
     * 
     * @param replacement
     */
    public void replaceWith(Map<? extends P, ? extends Q> replacement);

    /**
     * Tests the current collection can be cleared without violating constraints
     * 
     * @return - true if collection can be cleared
     */
    public boolean canClear();

    /**
     * Tests if the given element with the given key can be put into the current
     * map without violating constraints without validating constraints
     * 
     * @param key -
     *            the element key
     * @param element -
     *            element value
     * @return - true if the element can be put into the map
     */
    public boolean canPut(P key, Q element);

    /**
     * Tests if all elements in the specified map can be put into the current
     * map without violating constraints
     * 
     * @param t -
     *            the map whose elements are to be added
     * @return - true if all elements can be added
     */
    public boolean canPutAll(Map<? extends P,? extends Q> t);
    
    
    /**
     * Tests if the element with the specified key can be removed without violating constraints. 
     * Returns false if an element with that key does not exist in the map.
     * @param key - the key of the element to be removed
     * @return - true if the element can be removed
     */
    public boolean canRemove(P key);
    
    
    /**
     * Tests if the elements of the current map can be replaced with the elements of the specified key without violating constraints. 
     * @param replacement - the map whose elements will replace the content of the current map
     * @return - true if the replacement operation can be performed
     */
    public boolean canReplaceWith(Map<? extends P,? extends Q> replacement);

    /**
     * Tests if a collection element satisfies the constraints of the
     * collections's validator
     * 
     * @param o -
     *            the element to be tested
     * @return - true if valid
     */
    public boolean isValid(Object o);

	/**
	 * Returns the maximum number of elements allowed in this collection
	 * @return - the maximum number of elements allowed
	 */
	public int getMaxOcc();

	/**
	 * Returns the minimum number of elements allowed in this collection
	 * @return - the minimum number of elements allowed
	 */
	public int getMinOcc();

	/**
	 * Replaces the content of this map with the argument content. No change
	 * notification is performed. Note that the argument will continue to live
	 * within this AomMap instance. Changes will affect the argument map.
	 * <b>This method is not part of the public API. Its purpose is to support
	 * persistency packages.</b>
	 * 
	 * @param replacement
	 */
	public void replaceSilently(Map<P, Q> replacement);

}

