package com.bdaum.zoom.cat.model.locationCreated;

import com.bdaum.zoom.cat.model.location.Location;

import java.util.*;

import com.bdaum.zoom.cat.model.LocationCreated_type;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset locationCreated
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface LocationCreated extends IAsset, LocationCreated_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property location
	 *
	 * @param _value - new element value
	 */
	public void setLocation(String _value);

	/**
	 * Get value of property location
	 *
	 * @return - value of field location
	 */
	public String getLocation();

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(Collection<String> _value);

	/**
	 * Set single element of list asset
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setAsset(String _value, int _i);

	/**
	 * Add an element to list asset
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addAsset(String _element);

	/**
	 * Remove an element from list asset
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeAsset(String _element);

	/**
	 * Make asset empty 
	 */
	public void clearAsset();

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public AomList<String> getAsset();

	/**
	 * Get single element of list asset
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list asset
	 */
	public String getAsset(int _i);

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
