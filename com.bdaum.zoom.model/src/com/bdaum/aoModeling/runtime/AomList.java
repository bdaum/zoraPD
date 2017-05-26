package com.bdaum.aoModeling.runtime;

import java.util.Collection;
import java.util.List;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This interface defines a wrapping around java.util.List. It includes
 * event managing and element validation.
 * 
 * @see java.util.List
 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier
 */
public interface AomList<T> extends List<T>, AomCollection<T> {
    
    /**
     * Test if an element can be added at the given index without violating constraints
     * @param index - the index where the element shall be added
     * @param element - the element to be added
     * @return - true if the element can be added
     */
    public boolean canAdd(int index, T element);
    
    /**
     * Test if an element from a collection can be added at the given index without violating constraints
     * @param index - the index where the elements shall be added
     * @param c - the collection containing the elements to be added
     * @return - true if the elements can be added
     */
    public boolean canAddAll(int index, Collection<? extends T> c);
    
    /**
     * Test if an element can be removed at the given index without violating constraints
     * @param index - the index of the element that shall be removed
     * @return - true if the element can be removed
     */
    public boolean canRemove(int index);
    
    /**
     * Test if an element can be set at the given index without violating constraints
     * @param index
     * @param element
     * @return true if the element can be set
     */
    public boolean canSet(int index, T element);
    
	/**
	 * Replaces the whole list with the argument list.
	 * No change notification is performed. Note that the argument will continue to live
	 * within this AomList instance. Changes will affect the argument list.
	 * <b>This method is not part of the public API. Its purpose is to support
	 * persistency packages.</b>
	 * @param replacement - the list to replace the current list
	 */
	public void replaceSilently(List<T> replacement);

}
