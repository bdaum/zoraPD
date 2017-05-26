package com.bdaum.zoom.cat.model.group.exhibition;

import com.bdaum.zoom.cat.model.Exhibition_type;

import com.bdaum.zoom.cat.model.group.Group;

import java.util.*;

import com.bdaum.zoom.cat.model.Rgb_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibition
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Exhibition extends Exhibition_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property group_exhibition_parent
	 *
	 * @param _value - new element value
	 */
	public void setGroup_exhibition_parent(String _value);

	/**
	 * Get value of property group_exhibition_parent
	 *
	 * @return - value of field group_exhibition_parent
	 */
	public String getGroup_exhibition_parent();

	/**
	 * Set value of property wall
	 *
	 * @param _value - new element value
	 */
	public void setWall(Collection<Wall> _value);

	/**
	 * Set single element of list wall
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setWall(Wall _value, int _i);

	/**
	 * Add an element to list wall
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWall(Wall _element);

	/**
	 * Remove an element from list wall
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWall(Wall _element);

	/**
	 * Make wall empty 
	 */
	public void clearWall();

	/**
	 * Get value of property wall
	 *
	 * @return - value of field wall
	 */
	public AomList<Wall> getWall();

	/**
	 * Get single element of list wall
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list wall
	 */
	public Wall getWall(int _i);

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
