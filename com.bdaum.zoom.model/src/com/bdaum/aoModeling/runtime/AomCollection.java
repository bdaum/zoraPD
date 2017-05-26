package com.bdaum.aoModeling.runtime;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Berthold Daum
 *
 * (c) 2007 Berthold Daum
 * 
 * This interface defines a wrapping around collections. It includes
 * event managing and element validation.
 * 
 * @see java.util.Set
 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier
 */
public interface AomCollection<T> extends Collection<T>, AomValueChangedNotifier, Serializable {
	
	/**
	 * Returns the current element validator
	 * @return ElementValidator - the current element validator
	 */
	public ElementValidator getElementValidator();
	
	/**
	 * Sets a new element validator
	 * @param validator - the new element validator
	 */
	public void setElementValidator(ElementValidator validator);
	
	/**
	 * Replaces all elements in the instance with the elements of the parameter.
	 * Change notification is performed on element basis.
	 * @param replacement
	 */
	public void replaceWith(Collection<? extends T> replacement);
	
	/**
	 * Tests if an element can be added to the collection without violating constraints
	 * @param element - the element to be added
	 * @return - true if the element can be added
	 */
	public boolean canAdd(T element);
	
	 /**
	  * Tests if collection elements can be added to the collection without violating constraints
	 * @param c - the collection whose elements are to be added
	 * @return - true if elements can be added
	 */
	public boolean canAddAll(Collection<? extends T> c);
	
	/**
	 * Tests the current collection can be cleared without violating constraints
	 * @return - true if collection can be cleared
	 */
	public boolean canClear();
	
	 /**
	  * Tests the specified element can be removed without violating constraints
	 * @param o - the element to be removed
	 * @return - true if the element can be removed
	 */
	public boolean canRemove(T o);
	
	
	/**
	 * Tests if a collection element satisfies the constraints of the collections's validator
	 * @param o - the element to be tested
	 * @return - true if valid
	 */
	public boolean isValid(T o);
	
	
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


}
