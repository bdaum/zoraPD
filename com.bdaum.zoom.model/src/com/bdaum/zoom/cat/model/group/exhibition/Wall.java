package com.bdaum.zoom.cat.model.group.exhibition;

import com.bdaum.zoom.cat.model.Wall_type;

import java.util.*;

import com.bdaum.zoom.cat.model.Rgb_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset wall
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Wall extends IAsset, Wall_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property exhibition_wall_parent
	 *
	 * @param _value - new element value
	 */
	public void setExhibition_wall_parent(Exhibition _value);

	/**
	 * Get value of property exhibition_wall_parent
	 *
	 * @return - value of field exhibition_wall_parent
	 */
	public Exhibition getExhibition_wall_parent();

	/**
	 * Set value of property exhibit
	 *
	 * @param _value - new element value
	 */
	public void setExhibit(Collection<String> _value);

	/**
	 * Set single element of list exhibit
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setExhibit(String _value, int _i);

	/**
	 * Add an element to list exhibit
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addExhibit(String _element);

	/**
	 * Remove an element from list exhibit
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeExhibit(String _element);

	/**
	 * Make exhibit empty 
	 */
	public void clearExhibit();

	/**
	 * Get value of property exhibit
	 *
	 * @return - value of field exhibit
	 */
	public AomList<String> getExhibit();

	/**
	 * Get single element of list exhibit
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list exhibit
	 */
	public String getExhibit(int _i);

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
