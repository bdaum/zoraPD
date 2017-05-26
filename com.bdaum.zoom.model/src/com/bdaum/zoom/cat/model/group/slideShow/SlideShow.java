package com.bdaum.zoom.cat.model.group.slideShow;

import com.bdaum.zoom.cat.model.group.Group;

import java.util.*;

import com.bdaum.zoom.cat.model.SlideShow_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slideShow
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SlideShow extends IAsset, SlideShow_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property group_slideshow_parent
	 *
	 * @param _value - new element value
	 */
	public void setGroup_slideshow_parent(String _value);

	/**
	 * Get value of property group_slideshow_parent
	 *
	 * @return - value of field group_slideshow_parent
	 */
	public String getGroup_slideshow_parent();

	/**
	 * Set value of property entry
	 *
	 * @param _value - new element value
	 */
	public void setEntry(Collection<String> _value);

	/**
	 * Set single element of list entry
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setEntry(String _value, int _i);

	/**
	 * Add an element to list entry
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addEntry(String _element);

	/**
	 * Remove an element from list entry
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeEntry(String _element);

	/**
	 * Make entry empty 
	 */
	public void clearEntry();

	/**
	 * Get value of property entry
	 *
	 * @return - value of field entry
	 */
	public AomList<String> getEntry();

	/**
	 * Get single element of list entry
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list entry
	 */
	public String getEntry(int _i);

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
