package com.bdaum.zoom.cat.model.location;

import com.bdaum.zoom.cat.model.locationShown.LocationShown;

import java.util.*;

import com.bdaum.zoom.cat.model.Location_type;

import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset location
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Location extends IAsset, Location_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property locationCreated_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationCreated_parent(String _value);

	/**
	 * Get value of property locationCreated_parent
	 *
	 * @return - value of field locationCreated_parent
	 */
	public String getLocationCreated_parent();

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(Collection<String> _value);

	/**
	 * Set single element of list locationShown_parent
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setLocationShown_parent(String _value, int _i);

	/**
	 * Add an element to list locationShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLocationShown_parent(String _element);

	/**
	 * Remove an element from list locationShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLocationShown_parent(String _element);

	/**
	 * Make locationShown_parent empty 
	 */
	public void clearLocationShown_parent();

	/**
	 * Get value of property locationShown_parent
	 *
	 * @return - value of field locationShown_parent
	 */
	public AomList<String> getLocationShown_parent();

	/**
	 * Get single element of list locationShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list locationShown_parent
	 */
	public String getLocationShown_parent(int _i);

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
