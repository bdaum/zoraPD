package com.bdaum.zoom.cat.model.composedTo;

import com.bdaum.zoom.cat.model.ComposedTo_type;

import java.util.*;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset composedTo
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface ComposedTo extends ComposedTo_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property component
	 *
	 * @param _value - new element value
	 */
	public void setComponent(Collection<String> _value);

	/**
	 * Set single element of list component
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setComponent(String _value, int _i);

	/**
	 * Add an element to list component
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addComponent(String _element);

	/**
	 * Remove an element from list component
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeComponent(String _element);

	/**
	 * Make component empty 
	 */
	public void clearComponent();

	/**
	 * Get value of property component
	 *
	 * @return - value of field component
	 */
	public AomList<String> getComponent();

	/**
	 * Get single element of list component
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list component
	 */
	public String getComponent(int _i);

	/**
	 * Set value of property composite
	 *
	 * @param _value - new element value
	 */
	public void setComposite(String _value);

	/**
	 * Get value of property composite
	 *
	 * @return - value of field composite
	 */
	public String getComposite();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
